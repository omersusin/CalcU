package calc.u.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import calc.u.core.TimeLab
import calc.u.system.TimerService
import calc.u.ui.CalcUNumberBox
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private fun fmtMs(ms: Long): String = runCatching { TimeLab.formatHMS(ms.coerceAtLeast(0L)) }.getOrDefault("—")

@Composable
fun TimeLabScreen() {
    var tab by remember { mutableStateOf("stopwatch") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("stopwatch" to "Stopwatch", "timer" to "Timer", "pomodoro" to "Pomodoro")) { (id, label) ->
                FilterChip(selected = tab == id, onClick = { tab = id }, label = { Text(label) })
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                "timer" -> TimerScreen()
                "pomodoro" -> PomodoroScreen()
                else -> StopwatchScreen()
            }
        }
    }
}

@Composable
fun StopwatchScreen() {
    var running by remember { mutableStateOf(false) }
    var elapsed by remember { mutableStateOf(0L) }
    val totals = remember { mutableStateListOf<Long>() }
    LaunchedEffect(running) {
        var last = 0L
        while (running) {
            try {
                delay(10)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                break
            }
            val now = runCatching { System.currentTimeMillis() }.getOrNull() ?: break
            if (last != 0L) elapsed += (now - last).coerceAtLeast(0L)
            last = now
        }
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Stopwatch") {
                Text(
                    fmtMs(elapsed),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(onClick = { running = !running }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (running) "Stop" else "Start")
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { if (running || elapsed > 0) totals.add(elapsed) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Lap") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { running = false; elapsed = 0L; totals.clear() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Reset") }
                    }
                }
            }
        }
        if (totals.isNotEmpty()) {
            item {
                SectionCard("Laps") {
                    val last = totals.lastOrNull()
                    val laps = if (last == null) emptyList() else runCatching { TimeLab.addLap(totals.dropLast(1), last) }.getOrDefault(emptyList())
                    laps.forEach { lap ->
                        ResultLine("Lap ${lap.index}", "${fmtMs(lap.totalMs)} (+${fmtMs(lap.splitMs)})")
                    }
                }
            }
        }
    }
}

@Composable
fun TimerScreen() {
    val ctx = LocalContext.current
    var minIn by remember { mutableStateOf("1") }
    var secIn by remember { mutableStateOf("30") }
    var style by remember { mutableStateOf("dial") }
    var serviceRemaining by remember { mutableStateOf<Long?>(null) }
    var serviceRunning by remember { mutableStateOf(false) }
    var serviceTotal by remember { mutableStateOf(0L) }
    val notifPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }
    LaunchedEffect(Unit) {
        while (true) {
            try {
                delay(250)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                break
            }
            runCatching {
                val target = TimerService.activeTargetEndMs
                val running = TimerService.activeRunning
                serviceRunning = running
                serviceTotal = TimerService.activeTotalMs
                serviceRemaining = when {
                    running && target > 0L -> (target - System.currentTimeMillis()).coerceAtLeast(0L)
                    !running && TimerService.activeRemainingMs > 0L -> TimerService.activeRemainingMs
                    else -> null
                }
            }
        }
    }
    fun send(action: String, totalSec: Long = 0L, label: String = "") {
        runCatching {
            val intent = Intent(ctx, TimerService::class.java)
                .setAction(action)
                .putExtra(TimerService.EXTRA_TOTAL_SEC, totalSec)
                .putExtra(TimerService.EXTRA_LABEL, label)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }
    }
    fun ensureNotifThen(proceed: () -> Unit) {
        runCatching {
            if (Build.VERSION.SDK_INT >= 33) {
                val granted = runCatching {
                    ContextCompat.checkSelfPermission(
                        ctx,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                }.getOrDefault(false)
                if (!granted) runCatching { notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
            }
        }
        proceed()
    }
    val inputTotal =
        (((minIn.toLongOrNull() ?: 0L).coerceIn(0L, 1440L)) * 60000) +
            (((secIn.toLongOrNull() ?: 0L).coerceIn(0L, 59L)) * 1000)
    val effectiveTotal =
        if (serviceTotal > 0L && serviceRemaining != null) serviceTotal else inputTotal
    val shown = (serviceRemaining ?: inputTotal).coerceAtLeast(0L)
    val frac =
        if (effectiveTotal <= 0L) 0f else (shown.toFloat() / effectiveTotal.coerceAtLeast(1L)
            .toFloat()).coerceIn(0f, 1f)
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Countdown") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = style == "dial",
                        onClick = { style = "dial" },
                        label = { Text("Dial") }
                    )
                    FilterChip(
                        selected = style == "flip",
                        onClick = { style = "flip" },
                        label = { Text("Flip") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = minIn, onValueChange = { minIn = it }, label = "Min", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = secIn, onValueChange = { secIn = it }, label = "Sec", integer = true)
                    }
                }
                if (style == "dial") {
                    TimerDialDisplay(shownMs = shown, frac = frac)
                } else {
                    TimerFlipDisplay(shownMs = shown)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                when {
                                    serviceRunning -> send(TimerService.ACTION_PAUSE)
                                    (serviceRemaining ?: 0L) > 0L -> send(TimerService.ACTION_RESUME)
                                    else -> {
                                        val totalSec = (inputTotal / 1000L).coerceAtLeast(0L)
                                        if (totalSec > 0L) {
                                            ensureNotifThen {
                                                send(
                                                    TimerService.ACTION_START,
                                                    totalSec,
                                                    "Timer"
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (serviceRunning) "Pause" else "Start") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = {
                                send(TimerService.ACTION_STOP)
                                serviceRemaining = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Reset") }
                    }
                }
                val status = when {
                    serviceRunning -> "Running"
                    serviceRemaining == 0L -> "Done"
                    serviceRemaining != null -> "Paused"
                    else -> null
                }
                if (status != null) {
                    HorizontalDivider()
                    ResultLine("Status", status)
                }
            }
        }
    }
}

@Composable
private fun TimerDialDisplay(shownMs: Long, frac: Float) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val center = runCatching {
        TimeLab.formatHMS(shownMs.coerceAtLeast(0L)).substringAfter(":").substringBefore(".")
    }.getOrDefault("—")
    val minuteFrac =
        ((shownMs.coerceAtLeast(0L) % 60000L).toFloat() / 60000f).coerceIn(0f, 1f)
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(220.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val tickOuterR = size.minDimension / 2f - 2.dp.toPx()
                val arcOuterR = tickOuterR - 16.dp.toPx()
                val tickInnerR = arcOuterR - 14.dp.toPx()
                val arcInnerR = tickInnerR - 12.dp.toPx()
                val arcStroke = 10.dp.toPx()
                drawArc(
                    color = track,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(cx - arcOuterR, cy - arcOuterR),
                    size = androidx.compose.ui.geometry.Size(arcOuterR * 2f, arcOuterR * 2f),
                    style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = primary,
                    startAngle = -90f,
                    sweepAngle = 360f * frac,
                    useCenter = false,
                    topLeft = Offset(cx - arcOuterR, cy - arcOuterR),
                    size = androidx.compose.ui.geometry.Size(arcOuterR * 2f, arcOuterR * 2f),
                    style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = track,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(cx - arcInnerR, cy - arcInnerR),
                    size = androidx.compose.ui.geometry.Size(arcInnerR * 2f, arcInnerR * 2f),
                    style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = secondary,
                    startAngle = -90f,
                    sweepAngle = 360f * minuteFrac,
                    useCenter = false,
                    topLeft = Offset(cx - arcInnerR, cy - arcInnerR),
                    size = androidx.compose.ui.geometry.Size(arcInnerR * 2f, arcInnerR * 2f),
                    style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                )
                for (i in 0 until 60) {
                    val rad = Math.toRadians((i * 6).toDouble())
                    val long = i % 5 == 0
                    val len = (if (long) 8.dp else 4.dp).toPx()
                    val w = (if (long) 2.dp else 1.dp).toPx()
                    val dx = cos(rad).toFloat()
                    val dy = sin(rad).toFloat()
                    drawLine(
                        color = tickColor,
                        start = Offset(cx + dx * tickOuterR, cy + dy * tickOuterR),
                        end = Offset(cx + dx * (tickOuterR - len), cy + dy * (tickOuterR - len)),
                        strokeWidth = w,
                        cap = StrokeCap.Round
                    )
                }
                for (i in 0 until 60) {
                    val rad = Math.toRadians((i * 6).toDouble())
                    val long = i % 5 == 0
                    val len = (if (long) 6.dp else 3.dp).toPx()
                    val w = (if (long) 2.dp else 1.dp).toPx()
                    val dx = cos(rad).toFloat()
                    val dy = sin(rad).toFloat()
                    drawLine(
                        color = tickColor,
                        start = Offset(cx + dx * tickInnerR, cy + dy * tickInnerR),
                        end = Offset(cx + dx * (tickInnerR - len), cy + dy * (tickInnerR - len)),
                        strokeWidth = w,
                        cap = StrokeCap.Round
                    )
                }
            }
            Text(center, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun TimerFlipDisplay(shownMs: Long) {
    val parts = runCatching { TimeLab.countdownParts(shownMs.coerceAtLeast(0L)) }
        .getOrDefault(Triple(0L, 0L, 0L))
    val mm = parts.first.toString().padStart(2, '0')
    val ss = parts.second.toString().padStart(2, '0')
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimerFlipCard(value = mm, caption = "MIN")
        Text(
            ":",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        TimerFlipCard(value = ss, caption = "SEC")
    }
}

@Composable
private fun TimerFlipCard(value: String, caption: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.85f)) togetherWith
                        (fadeOut() + scaleOut(targetScale = 0.85f))
                },
                label = "flip-$caption"
            ) { target ->
                Text(target, style = MaterialTheme.typography.displayLarge)
            }
            Text(caption, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun PomodoroScreen() {
    var focusIn by remember { mutableStateOf("25") }
    var shortIn by remember { mutableStateOf("5") }
    var longIn by remember { mutableStateOf("15") }
    var roundsIn by remember { mutableStateOf("4") }
    var completed by remember { mutableStateOf(0) }
    var inBreak by remember { mutableStateOf(false) }
    var running by remember { mutableStateOf(false) }
    var leftMs by remember { mutableStateOf<Long?>(null) }
    val cfg = runCatching {
        TimeLab.PomoConfig(
            focusMin = (focusIn.toIntOrNull() ?: 25).coerceIn(1, 480),
            shortMin = (shortIn.toIntOrNull() ?: 5).coerceIn(1, 120),
            longMin = (longIn.toIntOrNull() ?: 15).coerceIn(1, 240),
            roundsUntilLong = (roundsIn.toIntOrNull() ?: 4).coerceIn(1, 12)
        )
    }.getOrDefault(TimeLab.PomoConfig(focusMin = 25, shortMin = 5, longMin = 15, roundsUntilLong = 4))
    val breakKind = runCatching { TimeLab.pomoPhase(completed.coerceAtLeast(0), cfg) }.getOrDefault("short")
    val phaseLabel = if (!inBreak) "focus" else breakKind
    val phaseMin = if (!inBreak) cfg.focusMin else if (breakKind == "long") cfg.longMin else cfg.shortMin
    LaunchedEffect(running, phaseLabel, completed) {
        if (running && (leftMs ?: 0L) > 0) {
            while (running && (leftMs ?: 0L) > 0) {
                try {
                    delay(1000)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                    break
                }
                leftMs = ((leftMs ?: 0L) - 1000).coerceAtLeast(0L)
            }
            if ((leftMs ?: 0L) <= 0) running = false
        }
    }
    val shown = (leftMs ?: (phaseMin.coerceAtLeast(0) * 60000L)).coerceAtLeast(0L)
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Pomodoro") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = focusIn, onValueChange = { focusIn = it }, label = "Focus", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = shortIn, onValueChange = { shortIn = it }, label = "Short", integer = true)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = longIn, onValueChange = { longIn = it }, label = "Long", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = roundsIn, onValueChange = { roundsIn = it }, label = "Rounds", integer = true)
                    }
                }
                HorizontalDivider()
                ResultLine("Phase", phaseLabel)
                ResultLine("Sessions", "$completed")
                Text(fmtMs(shown), style = MaterialTheme.typography.displaySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(cfg.roundsUntilLong.coerceIn(1, 12)) { i ->
                        Text(
                            if (i < completed % cfg.roundsUntilLong.coerceAtLeast(1)) "●" else "○",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                if (leftMs == null || leftMs == 0L) leftMs = phaseMin * 60000L
                                if ((leftMs ?: 0L) > 0) running = !running
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (running) "Pause" else "Start") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = {
                                running = false
                                leftMs = null
                                if (!inBreak) {
                                    completed += 1
                                    inBreak = true
                                } else {
                                    inBreak = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Skip") }
                    }
                }
            }
        }
    }
}
