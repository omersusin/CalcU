package calc.u.system

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.math.BigDecimal
import java.math.MathContext

class FloatCalcService : Service() {

    companion object {
        const val ACTION_STOP = "calc.u.FLOAT_CALC_STOP"
        private const val CHANNEL_ID = "float_calc"
        private const val NOTIF_ID = 4401

        fun start(context: Context) {
            runCatching {
                val intent = Intent(context, FloatCalcService::class.java)
                if (Build.VERSION.SDK_INT >= 26) {
                    runCatching { context.startForegroundService(intent) }
                } else {
                    runCatching { context.startService(intent) }
                }
            }
        }

        fun stop(context: Context) {
            runCatching { context.stopService(Intent(context, FloatCalcService::class.java)) }
        }

        fun isOverlayGranted(context: Context): Boolean =
            runCatching { Settings.canDrawOverlays(context) }.getOrDefault(false)

        fun startOrRequestPermission(context: Context): Boolean {
            return runCatching {
                if (Settings.canDrawOverlays(context)) {
                    start(context)
                    true
                } else {
                    runCatching {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + context.packageName)
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                    false
                }
            }.getOrDefault(false)
        }
    }

    private var windowManager: WindowManager? = null
    private var root: LinearLayout? = null
    private var display: TextView? = null
    private var params: WindowManager.LayoutParams? = null

    private var current = "0"
    private var pending: BigDecimal? = null
    private var op: Char? = null
    private var freshEntry = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        runCatching { startInForeground() }
        runCatching { showOverlay() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching {
            if (intent?.action == ACTION_STOP) {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        runCatching {
            root?.let { v -> runCatching { windowManager?.removeView(v) } }
        }
        root = null
        display = null
        params = null
        windowManager = null
        super.onDestroy()
    }

    private fun startInForeground() {
        runCatching {
            if (Build.VERSION.SDK_INT >= 26) {
                val mgr = runCatching { getSystemService(NotificationManager::class.java) }.getOrNull()
                runCatching {
                    if (mgr?.getNotificationChannel(CHANNEL_ID) == null) {
                        mgr?.createNotificationChannel(
                            NotificationChannel(
                                CHANNEL_ID,
                                "Floating calculator",
                                NotificationManager.IMPORTANCE_LOW
                            )
                        )
                    }
                }
            }
        }
        val stopIntent = Intent(this, FloatCalcService::class.java).setAction(ACTION_STOP)
        val stopPi = runCatching {
            PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }.getOrNull()
        val notif = runCatching {
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CalcU floating calculator")
                .setContentText("Tap Stop to dismiss")
                .setSmallIcon(android.R.drawable.ic_dialog_dialer)
                .setOngoing(true)
                .apply { runCatching { stopPi?.let { addAction(0, "Stop", it) } } }
                .build()
        }.getOrNull() ?: return
        if (Build.VERSION.SDK_INT >= 34) {
            runCatching {
                startForeground(
                    NOTIF_ID,
                    notif,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            }
        } else {
            runCatching { startForeground(NOTIF_ID, notif) }
        }
    }

    private fun showOverlay() {
        if (root != null) return
        if (!runCatching { Settings.canDrawOverlays(this) }.getOrDefault(false)) {
            stopSelf()
            return
        }
        val wm = runCatching { getSystemService(WINDOW_SERVICE) as WindowManager }.getOrNull() ?: return
        windowManager = wm
        val density = runCatching { resources.displayMetrics.density }.getOrDefault(1f)
        val widthPx = (260 * density).toInt()
        val lp = WindowManager.LayoutParams(
            widthPx,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 32
            y = 200
        }
        params = lp
        val rootView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xF2222222.toInt())
            setPadding(12, 12, 12, 12)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = "CalcU"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(4, 8, 4, 8)
        }
        val close = Button(this).apply {
            text = "✕"
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { runCatching { stopSelf() } }
        }
        header.addView(title)
        header.addView(close)
        runCatching {
            header.setOnTouchListener(object : View.OnTouchListener {
                private var downX = 0
                private var downY = 0
                private var downRawX = 0f
                private var downRawY = 0f
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event == null) return false
                    val p = params ?: return false
                    val wmgr = windowManager ?: return false
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = p.x
                            downY = p.y
                            downRawX = event.rawX
                            downRawY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            p.x = downX + (event.rawX - downRawX).toInt()
                            p.y = downY + (event.rawY - downRawY).toInt()
                            runCatching { wmgr.updateViewLayout(root, p) }
                            return true
                        }
                    }
                    return false
                }
            })
        }
        val disp = TextView(this).apply {
            text = current
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 24f
            gravity = Gravity.END
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(8, 12, 8, 12)
        }
        display = disp
        rootView.addView(header)
        rootView.addView(disp)
        val rows: List<List<String>> = listOf(
            listOf("C", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+")
        )
        rows.forEach { row -> rootView.addView(makeRow(row, 1f)) }
        val last = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 4f
        }
        last.addView(makeButton("0", 1f))
        last.addView(makeButton(".", 1f))
        last.addView(makeButton("=", 2f))
        rootView.addView(last)
        root = rootView
        runCatching { wm.addView(rootView, lp) }
        runCatching { refreshDisplay() }
    }

    private fun makeRow(labels: List<String>, weight: Float): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 4f
            labels.forEach { label -> addView(makeButton(label, weight)) }
        }
    }

    private fun makeButton(label: String, weight: Float): Button {
        return Button(this).apply {
            text = label
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            setOnClickListener { runCatching { onKey(label) } }
        }
    }

    private fun onKey(label: String) {
        runCatching {
            when (label) {
                "C" -> onClear()
                "⌫" -> onBackspace()
                "%" -> onPercent()
                "=" -> onEquals()
                "÷", "×", "−", "+" -> onOperator(label.first())
                ".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> onDigit(label)
            }
        }
        runCatching { refreshDisplay() }
    }

    private fun onDigit(d: String) {
        if (d == ".") {
            if (freshEntry || current == "Error") {
                current = "0."
                freshEntry = false
            } else if (!current.contains(".")) {
                current += "."
            }
            return
        }
        if (freshEntry || current == "0" || current == "Error") {
            current = d
            freshEntry = false
        } else {
            current += d
        }
    }

    private fun onClear() {
        current = "0"
        pending = null
        op = null
        freshEntry = true
    }

    private fun onBackspace() {
        if (freshEntry || current == "Error") {
            onClear()
            return
        }
        current = runCatching { current.dropLast(1).ifBlank { "0" } }.getOrDefault("0")
    }

    private fun onPercent() {
        val v = runCatching { BigDecimal(current) }.getOrNull() ?: return
        val r = runCatching { v.divide(BigDecimal(100), MathContext.DECIMAL64) }.getOrNull() ?: return
        current = format(r)
        freshEntry = true
    }

    private fun onOperator(o: Char) {
        if (op != null && pending != null && !freshEntry) {
            runCatching { onEquals() }
        }
        pending = runCatching { BigDecimal(current) }.getOrNull()
        op = o
        freshEntry = true
    }

    private fun onEquals() {
        val o = op ?: return
        val a = pending ?: return
        val b = runCatching { BigDecimal(current) }.getOrNull() ?: return
        val r = runCatching {
            when (o) {
                '+' -> a.add(b)
                '−' -> a.subtract(b)
                '×' -> a.multiply(b)
                '÷' -> {
                    if (b.compareTo(BigDecimal.ZERO) == 0) {
                        current = "Error"
                        pending = null
                        op = null
                        freshEntry = true
                        return
                    }
                    a.divide(b, MathContext.DECIMAL64)
                }
                else -> b
            }
        }.getOrNull() ?: run {
            current = "Error"
            pending = null
            op = null
            freshEntry = true
            return
        }
        current = format(r)
        pending = null
        op = null
        freshEntry = true
    }

    private fun format(v: BigDecimal): String {
        return runCatching {
            val s = v.stripTrailingZeros().toPlainString()
            if (s == "-0") "0" else s
        }.getOrDefault("Error")
    }

    private fun refreshDisplay() {
        runCatching { display?.text = current }
    }
}
