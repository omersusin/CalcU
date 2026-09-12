package calc.u.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import calc.u.core.TimeLab
import calc.u.ui.CalcUNumberBox
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import kotlinx.coroutines.delay

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
    var minIn by remember { mutableStateOf("1") }
    var secIn by remember { mutableStateOf("0") }
    var csIn by remember { mutableStateOf("0") }
    var remaining by remember { mutableStateOf<Long?>(null) }
    var running by remember { mutableStateOf(false) }
    val total = (((minIn.toLongOrNull() ?: 0L).coerceIn(0L, 1440L)) * 60000) + (((secIn.toLongOrNull() ?: 0L).coerceIn(0L, 3600L)) * 1000) + (((csIn.toLongOrNull() ?: 0L).coerceIn(0L, 99L)) * 10)
    LaunchedEffect(running) {
        while (running && (remaining ?: 0L) > 0) {
            try {
                delay(10)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                break
            }
            remaining = ((remaining ?: 0L) - 10).coerceAtLeast(0L)
            if ((remaining ?: 0L) <= 0) running = false
        }
    }
    val shown = (remaining ?: total).coerceAtLeast(0L)
    val frac = if (total <= 0) 0f else (shown.toFloat() / total.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val primary = MaterialTheme.colorScheme.primary
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Countdown") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = minIn, onValueChange = { minIn = it }, label = "Min", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = secIn, onValueChange = { secIn = it }, label = "Sec", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = csIn, onValueChange = { csIn = it }, label = "Cs", integer = true)
                    }
                }
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(Modifier.size(160.dp)) {
                            drawArc(
                                color = track,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = primary,
                                startAngle = -90f,
                                sweepAngle = 360f * frac,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(fmtMs(shown), style = MaterialTheme.typography.titleLarge)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                if (remaining == null || remaining == 0L) remaining = total
                                if ((remaining ?: 0L) > 0) running = !running
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (running) "Pause" else "Start") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { running = false; remaining = null },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Reset") }
                    }
                }
                if (shown == 0L && remaining != null) {
                    HorizontalDivider()
                    ResultLine("Status", "Done")
                }
            }
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
