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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import calc.u.core.Engine
import calc.u.core.IdealWeight
import calc.u.core.PaintKit
import calc.u.core.PasswordKit
import calc.u.data.SettingsRepository
import calc.u.ui.CalcUNumberBox
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                    "metro" to "Metro",
                    "password" to "Password"
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
                "password" -> PasswordSection()
                else -> TallySection()
            }
        }
    }
}

@Composable
private fun TallySection() {
    val appCtx = LocalContext.current.applicationContext
    val repo = remember { SettingsRepository(appCtx) }
    val scope = rememberCoroutineScope()
    val count by repo.tallyCount.collectAsState(initial = 0)
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
                        Button(
                            onClick = { scope.launch { runCatching { repo.setTallyCount(count - 1) } } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("−1") }
                    }
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = { scope.launch { runCatching { repo.setTallyCount(count + 1) } } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("+1") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { scope.launch { runCatching { repo.setTallyCount(0) } } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Reset") }
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
                            onClick = { runCatching { Engine.diceRoll(sides.coerceIn(2, 100)) }.onSuccess { rolls.add(0, it) } },
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
                    ResultLine("Last", "${rolls.firstOrNull() ?: "—"}")
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
                                runCatching { Engine.coinFlip() }.onSuccess { res ->
                                    if (res == lastFlip) streakCount += 1 else streakCount = 1
                                    lastFlip = res
                                    streakLabel = res
                                    if (res == "Heads") heads += 1 else tails += 1
                                }
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
        val safeBeats = beats.coerceIn(1, 12)
        val interval = (60000f / bpm.coerceIn(1f, 600f)).toLong().coerceIn(50L, 5000L)
        while (running) {
            runCatching { haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
            try {
                delay(interval)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                break
            }
            beat = (beat + 1) % safeBeats.coerceAtLeast(1)
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
                    repeat(beats.coerceIn(1, 12)) { i ->
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

@Composable
private fun PasswordSection() {
    var length by rememberSaveable { mutableStateOf(16f) }
    var digits by rememberSaveable { mutableStateOf(4) }
    var specials by rememberSaveable { mutableStateOf(2) }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val len = length.toInt().coerceIn(4, 64)
    val safeDigits = digits.coerceIn(0, len)
    val safeSpecials = specials.coerceIn(0, len - safeDigits)
    val pools = 1 + (if (safeDigits > 0) 1 else 0) + (if (safeSpecials > 0) 1 else 0)
    val strength = when {
        len >= 20 && pools == 3 -> "Very strong"
        len >= 14 && pools == 3 -> "Strong"
        len >= 12 && pools >= 2 -> "Good"
        len >= 8 && pools >= 2 -> "Fair"
        else -> "Weak"
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Password generator") {
                Text("$len characters", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = length,
                    onValueChange = { length = it },
                    valueRange = 4f..64f,
                    steps = 59
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Digits ($safeDigits)", style = MaterialTheme.typography.bodyLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { digits = (digits - 1).coerceAtLeast(0) }) { Text("−") }
                        OutlinedButton(
                            onClick = { digits = (digits + 1).coerceAtMost(len - safeSpecials) }
                        ) { Text("+") }
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Specials ($safeSpecials)", style = MaterialTheme.typography.bodyLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { specials = (specials - 1).coerceAtLeast(0) }) { Text("−") }
                        OutlinedButton(
                            onClick = { specials = (specials + 1).coerceAtMost(len - safeDigits) }
                        ) { Text("+") }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                runCatching { PasswordKit.generate(len, safeDigits, safeSpecials) }
                                    .onSuccess { password = it; error = "" }
                                    .onFailure { error = it.message ?: "Invalid options" }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Generate") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { runCatching { clipboard.setText(AnnotatedString(password)) } },
                            enabled = password.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Copy") }
                    }
                }
                if (error.isNotEmpty()) {
                    Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                if (password.isNotEmpty()) {
                    Text(
                        password,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    ResultLine("Password", "—")
                }
                HorizontalDivider()
                ResultLine("Strength", strength)
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
