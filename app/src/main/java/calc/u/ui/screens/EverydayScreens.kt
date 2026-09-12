package calc.u.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import calc.u.core.Engine
import calc.u.core.IdealWeight
import calc.u.core.PaintKit
import calc.u.ui.CalcUNumberBox
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import kotlinx.coroutines.delay

@Composable
fun EverydayScreen() {
    var tab by remember { mutableStateOf("tally") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(
                listOf(
                    "tally" to "Tally",
                    "dice" to "Dice & Coin",
                    "words" to "Words",
                    "paint" to "Paint",
                    "weight" to "Weight",
                    "metro" to "Metro"
                )
            ) { (id, label) ->
                FilterChip(selected = tab == id, onClick = { tab = id }, label = { Text(label) })
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                "dice" -> DiceCoinSection()
                "words" -> WordsSection()
                "paint" -> PaintSection()
                "weight" -> WeightSection()
                "metro" -> MetronomeSection()
                else -> TallySection()
            }
        }
    }
}

@Composable
private fun TallySection() {
    var count by rememberSaveable { mutableStateOf(0) }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Tally counter") {
                Text(
                    "$count",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(onClick = { count -= 1 }, modifier = Modifier.fillMaxWidth()) { Text("−1") }
                    }
                    Box(Modifier.weight(1f)) {
                        Button(onClick = { count += 1 }, modifier = Modifier.fillMaxWidth()) { Text("+1") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(onClick = { count = 0 }, modifier = Modifier.fillMaxWidth()) { Text("Reset") }
                    }
                }
                ResultLine("Count", "$count")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiceCoinSection() {
    val sidesOptions = listOf(4, 6, 8, 10, 12, 20)
    var sides by rememberSaveable { mutableStateOf(6) }
    var expanded by remember { mutableStateOf(false) }
    val rolls = remember { mutableStateListOf<Int>() }
    var lastFlip by rememberSaveable { mutableStateOf<String?>(null) }
    var heads by rememberSaveable { mutableStateOf(0) }
    var tails by rememberSaveable { mutableStateOf(0) }
    var streakLabel by rememberSaveable { mutableStateOf("") }
    var streakCount by rememberSaveable { mutableStateOf(0) }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Dice") {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = "d$sides",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sides") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        sidesOptions.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("d$s") },
                                onClick = { sides = s; expanded = false }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = { rolls.add(0, Engine.diceRoll(sides)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Roll d$sides") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { rolls.clear() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Clear") }
                    }
                }
                if (rolls.isEmpty()) {
                    ResultLine("History", "—")
                } else {
                    ResultLine("Last", "${rolls.first()}")
                    ResultLine("Rolls", "${rolls.size}")
                    HorizontalDivider()
                    rolls.take(20).forEachIndexed { i, r ->
                        ResultLine("#${rolls.size - i}", "$r")
                    }
                }
            }
        }
        item {
            SectionCard("Coin") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                val res = Engine.coinFlip()
                                if (res == lastFlip) streakCount += 1 else streakCount = 1
                                lastFlip = res
                                streakLabel = res
                                if (res == "Heads") heads += 1 else tails += 1
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Flip") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = {
                                lastFlip = null
                                heads = 0
                                tails = 0
                                streakCount = 0
                                streakLabel = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Reset") }
                    }
                }
                ResultLine("Last", lastFlip ?: "—")
                ResultLine("Heads", "$heads")
                ResultLine("Tails", "$tails")
                if (streakCount > 1 && streakLabel.isNotEmpty()) {
                    ResultLine("Streak", "$streakCount × $streakLabel")
                }
            }
        }
    }
}

@Composable
private fun WordsSection() {
    var input by rememberSaveable { mutableStateOf("123") }
    val words = runCatching {
        val n = input.trim().toLongOrNull() ?: return@runCatching "Enter 0..999999999999"
        Engine.numberToWords(n)
    }.getOrElse { "Out of range (0..999999999999)" }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Number to words") {
                CalcUNumberBox(
                    value = input,
                    onValueChange = { input = it },
                    label = "Number (0..999999999999)",
                    integer = true
                )
                HorizontalDivider()
                Text(
                    words,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PaintSection() {
    var lenIn by rememberSaveable { mutableStateOf("4") }
    var widIn by rememberSaveable { mutableStateOf("3") }
    var heightIn by rememberSaveable { mutableStateOf("2.6") }
    var coatsIn by rememberSaveable { mutableStateOf("2") }
    var coverageIn by rememberSaveable { mutableStateOf("10") }
    var tileLenIn by rememberSaveable { mutableStateOf("30") }
    var tileWidIn by rememberSaveable { mutableStateOf("30") }
    var wasteIn by rememberSaveable { mutableStateOf("10") }
    val len = lenIn.toDoubleOrNull() ?: 0.0
    val wid = widIn.toDoubleOrNull() ?: 0.0
    val height = heightIn.toDoubleOrNull() ?: 0.0
    val wallArea = 2 * (len + wid) * height
    val floorArea = len * wid
    val coats = coatsIn.toIntOrNull() ?: 0
    val coverage = coverageIn.toDoubleOrNull() ?: 0.0
    val liters = runCatching { PaintKit.paintLiters(wallArea, coats, coverage) }.getOrNull()
    val tiles = runCatching {
        PaintKit.tilesNeeded(
            floorArea,
            tileLenIn.toDoubleOrNull() ?: 0.0,
            tileWidIn.toDoubleOrNull() ?: 0.0,
            wasteIn.toDoubleOrNull() ?: 0.0
        )
    }.getOrNull()
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Paint — walls") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = lenIn, onValueChange = { lenIn = it }, label = "Length (m)")
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = widIn, onValueChange = { widIn = it }, label = "Width (m)")
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = heightIn, onValueChange = { heightIn = it }, label = "Height (m)")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = coatsIn, onValueChange = { coatsIn = it }, label = "Coats", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = coverageIn, onValueChange = { coverageIn = it }, label = "m² / liter")
                    }
                }
                HorizontalDivider()
                ResultLine("Wall area", "${fmt(wallArea)} m²")
                ResultLine("Paint", if (liters == null) "—" else "${fmt(liters)} L")
            }
        }
        item {
            SectionCard("Tiles — floor") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = tileLenIn, onValueChange = { tileLenIn = it }, label = "Tile cm")
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = tileWidIn, onValueChange = { tileWidIn = it }, label = "Tile cm")
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = wasteIn, onValueChange = { wasteIn = it }, label = "Waste %")
                    }
                }
                HorizontalDivider()
                ResultLine("Floor area", "${fmt(floorArea)} m²")
                ResultLine("Tiles needed", if (tiles == null) "—" else "$tiles")
            }
        }
    }
}

@Composable
private fun WeightSection() {
    var heightIn by rememberSaveable { mutableStateOf("178") }
    var male by rememberSaveable { mutableStateOf(true) }
    val height = heightIn.toDoubleOrNull() ?: 0.0
    val devine = runCatching { IdealWeight.devine(height, male) }.getOrNull()
    val robinson = runCatching { IdealWeight.robinson(height, male) }.getOrNull()
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Ideal weight") {
                CalcUNumberBox(value = heightIn, onValueChange = { heightIn = it }, label = "Height (cm)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = male, onClick = { male = true }, label = { Text("Male") })
                    FilterChip(selected = !male, onClick = { male = false }, label = { Text("Female") })
                }
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        if (devine == null) "—" else "${fmt(devine)} kg",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider()
                ResultLine("Devine", if (devine == null) "—" else "${fmt(devine)} kg")
                ResultLine("Robinson", if (robinson == null) "—" else "${fmt(robinson)} kg")
            }
        }
    }
}

@Composable
private fun MetronomeSection() {
    var bpm by rememberSaveable { mutableStateOf(120f) }
    var beats by rememberSaveable { mutableStateOf(4) }
    var running by rememberSaveable { mutableStateOf(false) }
    var beat by remember { mutableStateOf(0) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(running, bpm, beats) {
        if (!running) return@LaunchedEffect
        beat = 0
        while (true) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            delay((60000f / bpm).toLong())
            beat = (beat + 1) % beats
        }
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Metronome") {
                Text("${bpm.toInt()} BPM", style = MaterialTheme.typography.displaySmall)
                Slider(value = bpm, onValueChange = { bpm = it }, valueRange = 30f..240f)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 3, 4, 6).forEach { b ->
                        FilterChip(
                            selected = beats == b,
                            onClick = { beats = b; beat = 0 },
                            label = { Text("$b/4") }
                        )
                    }
                }
                Button(onClick = { running = !running }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (running) "Stop" else "Start")
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(beats) { i ->
                        Box(
                            Modifier.size(20.dp).clip(CircleShape).background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (running && i == beat) 1f else 0.2f
                                )
                            )
                        )
                    }
                }
                Text(
                    if (running) "Beat ${beat + 1} of $beats" else "Paused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun fmt(d: Double): String {
    if (!d.isFinite()) return "—"
    return try {
        java.math.BigDecimal.valueOf(d).stripTrailingZeros().toPlainString()
    } catch (e: Exception) {
        "—"
    }
}
