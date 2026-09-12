package calc.u.system

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import calc.u.MainActivity
import calc.u.core.TimeLab
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerService : Service() {

    companion object {
        const val ACTION_START = "calc.u.timer.START"
        const val ACTION_PAUSE = "calc.u.timer.PAUSE"
        const val ACTION_RESUME = "calc.u.timer.RESUME"
        const val ACTION_STOP = "calc.u.timer.STOP"

        const val EXTRA_TOTAL_SEC = "totalSec"
        const val EXTRA_LABEL = "label"

        const val CHANNEL_ID = "timer"
        const val NOTIF_ID = 1001
        const val DONE_ID = 1002

        @Volatile var activeTargetEndMs: Long = 0L
        @Volatile var activeTotalMs: Long = 0L
        @Volatile var activeRemainingMs: Long = 0L
        @Volatile var activeRunning: Boolean = false
        @Volatile var activeLabel: String = ""
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null
    private var lastNotifUpdateMs: Long = 0L

    override fun onCreate() {
        super.onCreate()
        runCatching { createChannel() }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        runCatching { tickJob?.cancel() }
        runCatching { scope.cancel() }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching {
            when (intent?.action) {
                ACTION_START -> handleStart(intent)
                ACTION_PAUSE -> handlePause()
                ACTION_RESUME -> handleResume()
                ACTION_STOP -> handleStop()
                else -> handleRestore()
            }
        }
        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        val totalSec = runCatching {
            val asLong = intent.getLongExtra(EXTRA_TOTAL_SEC, -1L)
            if (asLong >= 0L) asLong else intent.getIntExtra(EXTRA_TOTAL_SEC, 0).toLong()
        }.getOrDefault(0L).coerceIn(0L, 86400L)
        val totalMs = totalSec * 1000L
        if (totalMs <= 0L) {
            runCatching { stopSelf() }
            return
        }
        val label = runCatching { intent.getStringExtra(EXTRA_LABEL).orEmpty() }.getOrDefault("")
        val now = runCatching { System.currentTimeMillis() }.getOrDefault(0L)
        runCatching {
            activeTotalMs = totalMs
            activeLabel = label
            activeRemainingMs = totalMs
            activeTargetEndMs = now + totalMs
            activeRunning = true
        }
        runCatching { createChannel() }
        val notif = runCatching { buildOngoing(totalMs, true) }.getOrNull()
        if (notif != null) runCatching { goForeground(notif) } else runCatching { stopSelf() }
        runCatching { startTick() }
    }

    private fun handlePause() {
        if (!activeRunning) return
        val now = runCatching { System.currentTimeMillis() }.getOrDefault(0L)
        val rem = runCatching { (activeTargetEndMs - now).coerceAtLeast(0L) }.getOrDefault(0L)
        runCatching {
            activeRemainingMs = rem
            activeRunning = false
            activeTargetEndMs = 0L
        }
        runCatching { tickJob?.cancel() }
        runCatching { tickJob = null }
        runCatching { buildOngoing(rem, false)?.let { goForeground(it) } }
    }

    private fun handleResume() {
        if (activeRunning) return
        val rem = activeRemainingMs
        if (rem <= 0L) {
            runCatching { handleStop() }
            return
        }
        val now = runCatching { System.currentTimeMillis() }.getOrDefault(0L)
        runCatching {
            activeTargetEndMs = now + rem
            activeRunning = true
        }
        runCatching { buildOngoing(rem, true)?.let { goForeground(it) } }
        runCatching { startTick() }
    }

    private fun handleStop() {
        runCatching { tickJob?.cancel() }
        runCatching { tickJob = null }
        runCatching {
            activeTargetEndMs = 0L
            activeTotalMs = 0L
            activeRemainingMs = 0L
            activeRunning = false
            activeLabel = ""
        }
        runCatching { stopForeground(Service.STOP_FOREGROUND_REMOVE) }
        runCatching { nm()?.cancel(NOTIF_ID) }
        runCatching { stopSelf() }
    }

    private fun handleRestore() {
        val now = runCatching { System.currentTimeMillis() }.getOrDefault(0L)
        val target = activeTargetEndMs
        if (activeRunning && target > now) {
            val rem = (target - now).coerceAtLeast(0L)
            runCatching { buildOngoing(rem, true)?.let { goForeground(it) } }
            runCatching { startTick() }
        } else {
            runCatching { stopSelf() }
        }
    }

    private fun startTick() {
        runCatching { tickJob?.cancel() }
        lastNotifUpdateMs = 0L
        tickJob = scope.launch {
            while (isActive) {
                try {
                    delay(500)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    break
                }
                val target = activeTargetEndMs
                if (target <= 0L) break
                val now = runCatching { System.currentTimeMillis() }.getOrNull() ?: break
                val rem = target - now
                if (rem <= 0L) {
                    onFinish()
                    break
                }
                if (now - lastNotifUpdateMs >= 1000L) {
                    lastNotifUpdateMs = now
                    runCatching {
                        buildOngoing(rem, true)?.let { nm()?.notify(NOTIF_ID, it) }
                    }
                }
            }
        }
    }

    private fun onFinish() {
        runCatching { tickJob?.cancel() }
        runCatching { tickJob = null }
        val label = runCatching { activeLabel }.getOrDefault("")
        runCatching {
            activeRunning = false
            activeRemainingMs = 0L
            activeTargetEndMs = 0L
        }
        runCatching { stopForeground(Service.STOP_FOREGROUND_REMOVE) }
        runCatching { nm()?.cancel(NOTIF_ID) }
        runCatching { buildDone(label)?.let { nm()?.notify(DONE_ID, it) } }
        runCatching {
            activeTotalMs = 0L
            activeLabel = ""
        }
        runCatching { stopSelf() }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        runCatching {
            val ch = NotificationChannel(CHANNEL_ID, "Timer", NotificationManager.IMPORTANCE_LOW)
            nm()?.createNotificationChannel(ch)
        }
    }

    private fun nm(): NotificationManager? =
        runCatching { getSystemService(NotificationManager::class.java) }.getOrNull()

    private fun goForeground(n: Notification) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIF_ID, n)
            }
        }
    }

    private fun contentIntent(): PendingIntent? = runCatching {
        val launch = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra("dest", "time")
        PendingIntent.getActivity(
            this, 10, launch,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }.getOrNull()

    private fun serviceIntent(action: String): PendingIntent? = runCatching {
        val i = Intent(this, TimerService::class.java).setAction(action)
        PendingIntent.getService(
            this, action.hashCode(), i,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }.getOrNull()

    private fun buildOngoing(remainingMs: Long, running: Boolean): Notification? = runCatching {
        val label = activeLabel.ifBlank { "Timer" }
        val text = fmtShort(remainingMs) + if (running) "" else " • Paused"
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        b.setContentTitle(label)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(running)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(running)
        runCatching { contentIntent()?.let { b.setContentIntent(it) } }
        runCatching {
            if (running) {
                b.setChronometerCountDown(true)
                b.setWhen(activeTargetEndMs)
            }
        }
        runCatching {
            val a = if (running) ACTION_PAUSE else ACTION_RESUME
            serviceIntent(a)?.let { pi ->
                val iconRes = if (running) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                val icon = Icon.createWithResource(this, iconRes)
                b.addAction(
                    Notification.Action.Builder(icon, if (running) "Pause" else "Resume", pi).build()
                )
            }
        }
        runCatching {
            serviceIntent(ACTION_STOP)?.let { pi ->
                val icon = Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel)
                b.addAction(Notification.Action.Builder(icon, "Stop", pi).build())
            }
        }
        b.build()
    }.getOrNull()

    private fun buildDone(label: String): Notification? = runCatching {
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        b.setContentTitle("Timer done")
            .setContentText(label.ifBlank { "Countdown finished" })
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(false)
            .setAutoCancel(true)
        runCatching { contentIntent()?.let { b.setContentIntent(it) } }
        b.build()
    }.getOrNull()

    private fun fmtShort(ms: Long): String = runCatching {
        TimeLab.formatHMS(ms.coerceAtLeast(0L)).substringAfter(":").substringBefore(".")
    }.getOrDefault("--:--")
}
