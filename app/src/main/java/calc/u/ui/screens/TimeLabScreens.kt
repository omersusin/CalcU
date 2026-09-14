package calc.u.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import java.time.LocalDate
import java.time.Period
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private fun fmtMs(ms: Long): String = runCatching { TimeLab.formatHMS(ms.coerceAtLeast(0L)) }.getOrDefault("—")

private fun fmtHMSLong(ms: Long): String = runCatching {
    val t = ms.coerceAtLeast(0L)
    val h = t / 3600000
    val m = ((t / 60000) % 60).toString().padStart(2, '0')
    val s = ((t / 1000) % 60).toString().padStart(2, '0')
    "$h:$m:$s"
}.getOrDefault("—")

@Composable
fun TimeLabScreen() {
    var tab by remember { mutableStateOf("stopwatch") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("stopwatch" to "Stopwatch", "timer" to "Timer", "pomodoro" to "Pomodoro", "dates" to "Dates")) { (id, label) ->
                FilterChip(selected = tab == id, onClick = { tab = id }, label = { Text(label) })
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                "timer" -> TimerScreen()
                "pomodoro" -> PomodoroScreen()
                "dates" -> DatesScreen()
                else -> StopwatchScreen()
            }
        }
    }
}

@Composable
fun DatesScreen() {
    var birth by rememberSaveable { mutableStateOf("") }
    var from by rememberSaveable { mutableStateOf("") }
    var to by rememberSaveable { mutableStateOf("") }
    var base by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf("Days") }
    var sign by rememberSaveable { mutableStateOf("+") }
    fun weekdayOf(d: LocalDate): String = runCatching {
        d.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }.getOrDefault("")
    val ageRes = remember(birth) {
        runCatching {
            val b = LocalDate.parse(birth.trim())
            val today = LocalDate.now()
            require(!b.isAfter(today)) { "birth date is in the future" }
            val p = Period.between(b, today)
            val totalDays = ChronoUnit.DAYS.between(b, today)
            var next = b.withYearSafe(today.year)
            if (!next.isAfter(today)) next = next.plusYears(1)
            Triple(p, totalDays, ChronoUnit.DAYS.between(today, next))
        }
    }
    val intervalRes = remember(from, to) {
        runCatching {
            val a = LocalDate.parse(from.trim())
            val b = LocalDate.parse(to.trim())
            ChronoUnit.DAYS.between(a, b)
        }
    }
    val shiftRes = remember(base, amount, unit, sign) {
        runCatching {
            val d = LocalDate.parse(base.trim())
            val n = amount.trim().toLong()
            val moved = when (unit) {
                "Weeks" -> d.plusWeeks(n)
                "Months" -> d.plusMonths(n)
                "Years" -> d.plusYears(n)
                else -> d.plusDays(n)
            }
            if (sign == "−") d.plusDays(ChronoUnit.DAYS.between(moved, d)) else moved
        }
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Age") {
                OutlinedTextField(
                    value = birth,
                    onValueChange = { birth = it },
                    label = { Text("Birth date (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(onClick = { birth = "2000-01-01" }) { Text("Try 2000-01-01") }
                HorizontalDivider()
                val age = ageRes.getOrNull()
                if (birth.isBlank()) {
                    ResultLine("Age", "—")
                } else if (age == null) {
                    Text(
                        ageRes.exceptionOrNull()?.message ?: "invalid date",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    val (p, totalDays, untilNext) = age
                    ResultLine("Age", "${p.years}y ${p.months}m ${p.days}d")
                    ResultLine("Total days", totalDays.toString())
                    ResultLine("Born", weekdayOf(LocalDate.parse(birth.trim())))
                    ResultLine("Next birthday", "in $untilNext days")
                }
            }
        }
        item {
            SectionCard("Interval") {
                OutlinedTextField(
                    value = from,
                    onValueChange = { from = it },
                    label = { Text("From (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text("To (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(onClick = { to = LocalDate.now().toString() }) { Text("To = today") }
                HorizontalDivider()
                val days = intervalRes.getOrNull()
                if (from.isBlank() || to.isBlank()) {
                    ResultLine("Between", "—")
                } else if (days == null) {
                    Text(
                        intervalRes.exceptionOrNull()?.message ?: "invalid date",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    val d = kotlin.math.abs(days)
                    ResultLine("Days", d.toString())
                    ResultLine("Weeks", "${d / 7}w ${d % 7}d")
                    val p = Period.between(LocalDate.parse(from.trim()), LocalDate.parse(to.trim()))
                    ResultLine("Months", "${kotlin.math.abs(p.toTotalMonths())}m ${kotlin.math.abs(p.days)}d")
                }
            }
        }
        item {
            SectionCard("Add / subtract") {
                OutlinedTextField(
                    value = base,
                    onValueChange = { base = it },
                    label = { Text("Date (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("+", "−")) { s ->
                        FilterChip(selected = sign == s, onClick = { sign = s }, label = { Text(s) })
                    }
                    items(listOf("Days", "Weeks", "Months", "Years")) { u ->
                        FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u) })
                    }
                }
                HorizontalDivider()
                val shifted = shiftRes.getOrNull()
                if (base.isBlank() || amount.isBlank()) {
                    ResultLine("Result", "—")
                } else if (shifted == null) {
                    Text(
                        shiftRes.exceptionOrNull()?.message ?: "invalid input",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    ResultLine("Date", shifted.toString())
                    ResultLine("Weekday", weekdayOf(shifted))
                }
            }
        }
    }
}

private fun LocalDate.withYearSafe(year: Int): LocalDate = runCatching {
    this.withYear(year)
}.getOrDefault(this)

@Composable
fun StopwatchScreen() {
    val ctx = LocalContext.current
    var running by rememberSaveable { mutableStateOf(false) }
    var baseMs by rememberSaveable { mutableStateOf(0L) }
    var startStamp by rememberSaveable { mutableStateOf(0L) }
    var elapsed by remember { mutableStateOf(0L) }
    val totals = remember { mutableStateListOf<Long>() }
    var exportError by remember { mutableStateOf("") }
    // Monotonic clock: SystemClock.elapsedRealtime() is immune to wall-clock changes.
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (running) {
            try {
                delay(31)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                break
            }
            val now = runCatching { SystemClock.elapsedRealtime() }.getOrNull() ?: break
            elapsed = (baseMs + (now - startStamp).coerceAtLeast(0L)).coerceAtLeast(0L)
        }
    }
    fun start() {
        runCatching {
            startStamp = SystemClock.elapsedRealtime()
            running = true
        }
    }
    fun pause() {
        runCatching {
            val now = SystemClock.elapsedRealtime()
            baseMs = (baseMs + (now - startStamp).coerceAtLeast(0L)).coerceAtLeast(0L)
            elapsed = baseMs
            running = false
        }
    }
    fun reset() {
        runCatching {
            running = false
            baseMs = 0L
            startStamp = 0L
            elapsed = 0L
            totals.clear()
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
                Text(
                    "Monotonic clock (elapsedRealtime) · hundredths",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = { if (running) pause() else start() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (running) "Stop" else "Start") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { if (running || elapsed > 0) totals.add(elapsed) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Lap") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { reset() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Reset") }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    val laps = TimeLab.addLap(totals.dropLast(1), totals.lastOrNull() ?: 0L)
                                    val sb = StringBuilder("Stopwatch export\n")
                                    laps.forEach { lap ->
                                        sb.append("Lap ${lap.index}: total ${fmtMs(lap.totalMs)} (split ${fmtMs(lap.splitMs)})\n")
                                    }
                                    val send = Intent(Intent.ACTION_SEND)
                                        .setType("text/plain")
                                        .putExtra(Intent.EXTRA_TEXT, sb.toString())
                                    val chooser = runCatching { Intent.createChooser(send, "Share laps") }.getOrNull()
                                    if (chooser != null) ctx.startActivity(chooser) else exportError = "Share unavailable"
                                }.onFailure { exportError = it.message ?: "Share failed" }
                            },
                            enabled = totals.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Export") }
                    }
                }
                if (exportError.isNotEmpty()) {
                    Text(exportError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        if (totals.isNotEmpty()) {
            item {
                SectionCard("Laps") {
                    val last = totals.lastOrNull()
                    val laps = if (last == null) emptyList() else runCatching { TimeLab.addLap(totals.dropLast(1), last) }.getOrDefault(emptyList())
                    val fastest = runCatching { laps.minByOrNull { it.splitMs } }.getOrNull()
                    val slowest = runCatching { laps.maxByOrNull { it.splitMs } }.getOrNull()
                    if (fastest != null) ResultLine("Fastest", "Lap ${fastest.index} (+${fmtMs(fastest.splitMs)})")
                    if (slowest != null) ResultLine("Slowest", "Lap ${slowest.index} (+${fmtMs(slowest.splitMs)})")
                    HorizontalDivider()
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
    var hourIn by rememberSaveable { mutableStateOf("0") }
    var minIn by rememberSaveable { mutableStateOf("1") }
    var secIn by rememberSaveable { mutableStateOf("30") }
    var style by rememberSaveable { mutableStateOf("dial") }
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
                    !running && TimerService.activeRemainingMs == 0L && TimerService.activeTotalMs == 0L && serviceRemaining != null -> 0L
                    else -> serviceRemaining
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
    val hRaw = hourIn.toLongOrNull()
    val mRaw = minIn.toLongOrNull()
    val sRaw = secIn.toLongOrNull()
    val rangeError = when {
        hRaw == null || hRaw !in 0..23 -> "Hours must be 0..23"
        mRaw == null || mRaw !in 0..59 -> "Minutes must be 0..59"
        sRaw == null || sRaw !in 0..59 -> "Seconds must be 0..59"
        else -> ""
    }
    val inputTotal = if (rangeError.isEmpty()) {
        (hRaw ?: 0L) * 3600000 + (mRaw ?: 0L) * 60000 + (sRaw ?: 0L) * 1000
    } else 0L
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
                        CalcUNumberBox(value = hourIn, onValueChange = { hourIn = it }, label = "Hours", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = minIn, onValueChange = { minIn = it }, label = "Min", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = secIn, onValueChange = { secIn = it }, label = "Sec", integer = true)
                    }
                }
                if (rangeError.isNotEmpty()) {
                    Text(rangeError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                Text(
                    fmtHMSLong(shown),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "H:MM:SS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (style == "dial") {
                    TimerDialDisplay(shownMs = shown, frac = frac)
                } else {
                    TimerFlipDisplay(shownMs = shown)
                }
                if (inputTotal <= 0L && serviceRemaining == null) {
                    Text(
                        "Set a duration above to start.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                when {
                                    serviceRunning -> send(TimerService.ACTION_PAUSE)
                                    (serviceRemaining ?: 0L) > 0L -> send(TimerService.ACTION_RESUME)
                                    else -> {
                                        if (rangeError.isNotEmpty() || inputTotal <= 0L) return@Button
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
    val center = fmtHMSLong(shownMs)
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
    val t = shownMs.coerceAtLeast(0L)
    val hh = (t / 3600000).toString().padStart(2, '0')
    val parts = runCatching { TimeLab.countdownParts(t) }
        .getOrDefault(Triple(0L, 0L, 0L))
    val mm = (parts.first % 60).toString().padStart(2, '0')
    val ss = parts.second.toString().padStart(2, '0')
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimerFlipCard(value = hh, caption = "HRS")
        Text(
            ":",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
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
    val ctx = LocalContext.current
    var focusIn by rememberSaveable { mutableStateOf("25") }
    var shortIn by rememberSaveable { mutableStateOf("5") }
    var longIn by rememberSaveable { mutableStateOf("15") }
    var roundsIn by rememberSaveable { mutableStateOf("4") }
    var completed by rememberSaveable { mutableStateOf(0) }
    var inBreak by rememberSaveable { mutableStateOf(false) }
    var sessionActive by rememberSaveable { mutableStateOf(false) }
    var serviceRemaining by remember { mutableStateOf<Long?>(null) }
    var serviceRunning by remember { mutableStateOf(false) }
    var autoNote by remember { mutableStateOf("") }
    val notifPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }
    // Config-change reset: any edit to the phase config resets the session.
    LaunchedEffect(focusIn, shortIn, longIn, roundsIn) {
        runCatching {
            if (sessionActive || completed != 0 || inBreak) {
                autoNote = "Config changed — session reset."
            }
            sessionActive = false
            completed = 0
            inBreak = false
            serviceRemaining = null
            runCatching {
                ctx.stopService(Intent(ctx, TimerService::class.java).setAction(TimerService.ACTION_STOP))
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
                    ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
                }.getOrDefault(false)
                if (!granted) runCatching { notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
            }
        }
        proceed()
    }
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
    fun startPhase() {
        val totalSec = (phaseMin.coerceAtLeast(1) * 60L).coerceIn(60L, 86400L)
        ensureNotifThen {
            send(TimerService.ACTION_START, totalSec, "Pomodoro $phaseLabel")
            sessionActive = true
            autoNote = ""
        }
    }
    // Poll the shared TimerService (foreground-service pattern mirrored from TimerScreen).
    LaunchedEffect(Unit) {
        while (true) {
            try {
                delay(500)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                break
            }
            runCatching {
                val target = TimerService.activeTargetEndMs
                val running = TimerService.activeRunning
                serviceRunning = running
                serviceRemaining = when {
                    running && target > 0L -> (target - System.currentTimeMillis()).coerceAtLeast(0L)
                    !running && TimerService.activeRemainingMs > 0L -> TimerService.activeRemainingMs
                    !running && TimerService.activeRemainingMs == 0L && sessionActive -> 0L
                    else -> serviceRemaining
                }
            }
        }
    }
    // Auto phase transition when the foreground service finishes a phase.
    LaunchedEffect(serviceRemaining, sessionActive) {
        if (!sessionActive) return@LaunchedEffect
        if (serviceRemaining == 0L) {
            runCatching {
                if (!inBreak) {
                    completed += 1
                    inBreak = true
                } else {
                    inBreak = false
                }
                autoNote = "Phase done — next: ${if (!inBreak) "focus" else runCatching { TimeLab.pomoPhase(completed.coerceAtLeast(0), cfg) }.getOrDefault("short")}"
                val nextMin = if (!inBreak) cfg.focusMin else {
                    val bk = runCatching { TimeLab.pomoPhase(completed.coerceAtLeast(0), cfg) }.getOrDefault("short")
                    if (bk == "long") cfg.longMin else cfg.shortMin
                }
                val totalSec = (nextMin.coerceAtLeast(1) * 60L).coerceIn(60L, 86400L)
                ensureNotifThen {
                    send(
                        TimerService.ACTION_START,
                        totalSec,
                        "Pomodoro ${if (!inBreak) "focus" else runCatching { TimeLab.pomoPhase(completed.coerceAtLeast(0), cfg) }.getOrDefault("short")}"
                    )
                }
            }
        }
    }
    val shown = (serviceRemaining ?: (phaseMin.coerceAtLeast(0) * 60000L)).coerceAtLeast(0L)
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Pomodoro") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = focusIn, onValueChange = { focusIn = it }, label = "Focus (min)", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = shortIn, onValueChange = { shortIn = it }, label = "Short (min)", integer = true)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = longIn, onValueChange = { longIn = it }, label = "Long (min)", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = roundsIn, onValueChange = { roundsIn = it }, label = "Rounds", integer = true)
                    }
                }
                Text(
                    "Runs on the Timer foreground service (ongoing notification); phases auto-advance. Editing config resets the session.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                ResultLine("Phase", phaseLabel)
                ResultLine("Sessions", "$completed")
                Text(fmtHMSLong(shown), style = MaterialTheme.typography.displaySmall)
                Text(
                    if (serviceRunning) "Running in foreground service" else if (sessionActive) "Paused" else "Idle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (autoNote.isNotEmpty()) {
                    Text(autoNote, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
                                if (!sessionActive) startPhase()
                                else if (serviceRunning) send(TimerService.ACTION_PAUSE)
                                else if ((serviceRemaining ?: 0L) > 0L) send(TimerService.ACTION_RESUME)
                                else startPhase()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (serviceRunning) "Pause" else "Start") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    if (!inBreak) {
                                        completed += 1
                                        inBreak = true
                                    } else {
                                        inBreak = false
                                    }
                                    sessionActive = true
                                    val bk = runCatching { TimeLab.pomoPhase(completed.coerceAtLeast(0), cfg) }.getOrDefault("short")
                                    val nm = if (!inBreak) cfg.focusMin else if (bk == "long") cfg.longMin else cfg.shortMin
                                    ensureNotifThen {
                                        send(
                                            TimerService.ACTION_START,
                                            (nm.coerceAtLeast(1) * 60L).coerceIn(60L, 86400L),
                                            "Pomodoro ${if (!inBreak) "focus" else bk}"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Skip") }
                    }
                }
                OutlinedButton(
                    onClick = {
                        runCatching {
                            send(TimerService.ACTION_STOP)
                            sessionActive = false
                            serviceRemaining = null
                            completed = 0
                            inBreak = false
                            autoNote = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reset session") }
            }
        }
    }
}
