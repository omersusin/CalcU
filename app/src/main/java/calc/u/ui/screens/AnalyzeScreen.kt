package calc.u.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val DAY_MILLIS = 86400000L

private fun todayKey(): Long =
    runCatching { System.currentTimeMillis() / DAY_MILLIS }.getOrDefault(0L)

private fun weekBuckets(activity: Map<Long, Int>, today: Long): List<Pair<String, Int>> {
    return runCatching {
        val fmt = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()
        (6 downTo 0).map { back ->
            val day = today - back
            cal.timeInMillis = day * DAY_MILLIS
            val label = runCatching { fmt.format(cal.time).take(1) }.getOrNull().orEmpty()
            label to (activity[day] ?: 0)
        }
    }.getOrDefault(emptyList())
}

private fun currentStreak(days: Set<Long>, today: Long): Int {
    return runCatching {
        var cursor = if (days.contains(today)) today else today - 1
        var n = 0
        while (days.contains(cursor)) {
            n++
            cursor--
        }
        n
    }.getOrDefault(0)
}

private fun bestStreak(days: Set<Long>): Int {
    return runCatching {
        val sorted = days.sorted()
        var best = 0
        var run = 0
        var prev = Long.MIN_VALUE
        for (d in sorted) {
            run = if (d == prev + 1) run + 1 else 1
            if (run > best) best = run
            prev = d
        }
        best
    }.getOrDefault(0)
}

private data class MonthBucket(val label: String, val count: Int)

private fun monthBuckets(activity: Map<Long, Int>): List<MonthBucket> {
    return runCatching {
        val labelFmt = SimpleDateFormat("MMM", Locale.getDefault())
        val now = Calendar.getInstance()
        val perMonth = mutableMapOf<Int, Int>()
        for ((day, count) in activity) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = day * DAY_MILLIS
            val key = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)
            perMonth[key] = (perMonth[key] ?: 0) + count
        }
        (11 downTo 0).map { back ->
            val cal = Calendar.getInstance()
            cal.time = now.time
            cal.add(Calendar.MONTH, -back)
            val key = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)
            val label = runCatching { labelFmt.format(cal.time) }.getOrNull().orEmpty()
            MonthBucket(label, perMonth[key] ?: 0)
        }
    }.getOrDefault(emptyList())
}

@Composable
fun AnalyzeScreen(
    activity: Map<Long, Int> = emptyMap(),
    historyCount: Int = 0,
) {
    var tab by remember { mutableStateOf(0) }
    val today = remember { todayKey() }
    val week = remember(activity, today) { weekBuckets(activity, today) }
    val weekTotal = remember(week) { runCatching { week.sumOf { it.second } }.getOrDefault(0) }
    val weekMax = remember(week) { week.maxOfOrNull { it.second } ?: 0 }
    val months = remember(activity) { monthBuckets(activity) }
    val monthMax = remember(months) { months.maxOfOrNull { it.count } ?: 0 }
    val cur = remember(activity, today) { currentStreak(activity.keys, today) }
    val best = remember(activity) { bestStreak(activity.keys) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Analyze",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Streak: $cur day(s) • Best: $best day(s) • $historyCount saved",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tab == 0,
                onClick = { tab = 0 },
                label = { Text("Week") },
            )
            FilterChip(
                selected = tab == 1,
                onClick = { tab = 1 },
                label = { Text("Year") },
            )
        }
        if (tab == 0) {
            Text(
                text = "Last 7 days • $weekTotal calculations",
                style = MaterialTheme.typography.titleMedium,
            )
            val barColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                if (week.isEmpty()) return@Canvas
                val n = week.size
                val slot = size.width / n
                val barW = slot * 0.52f
                week.forEachIndexed { i, (_, count) ->
                    val frac = if (weekMax > 0) count.toFloat() / weekMax.toFloat() else 0f
                    val bh = if (count > 0) (size.height * frac).coerceAtLeast(6f) else 4f
                    val left = i * slot + (slot - barW) / 2f
                    drawRoundRect(
                        color = if (count > 0) barColor else trackColor,
                        topLeft = Offset(left, size.height - bh),
                        size = Size(barW, bh),
                        cornerRadius = CornerRadius(8f, 8f),
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { (label, count) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = label, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        } else {
            Text(
                text = "Last 12 months",
                style = MaterialTheme.typography.titleMedium,
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(months, key = { it.label }) { m ->
                    Card {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = m.label.ifEmpty { "—" },
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                text = m.count.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            LinearProgressIndicator(
                                progress = {
                                    if (monthMax > 0) m.count.toFloat() / monthMax.toFloat() else 0f
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            if (months.isEmpty()) {
                Text(
                    text = "No activity yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
