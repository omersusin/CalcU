package calc.u.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import kotlin.math.log2
import kotlin.math.pow

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
                    "tiles" to "Tiles",
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
                "tiles" -> TilesSection()
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
    var step by rememberSaveable { mutableStateOf(1) }
    val history = remember { mutableStateListOf<String>() }
    fun pushHistory(entry: String) {
        runCatching {
            history.add(0, entry)
            if (history.size > 50) history.removeAt(history.size - 1)
        }
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Tally counter") {
                Text(
                    "$count",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Step size",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(1, 2, 5, 10)) { s ->
                        FilterChip(
                            selected = step == s,
                            onClick = { step = s },
                            label = { Text("±$s") }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    runCatching { repo.setTallyCount(count - step) }
                                        .onSuccess { pushHistory("−$step → ${count - step}") }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("−$step") }
                    }
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    runCatching { repo.setTallyCount(count + step) }
                                        .onSuccess { pushHistory("+$step → ${count + step}") }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("+$step") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    runCatching { repo.setTallyCount(0) }
                                        .onSuccess { pushHistory("Reset → 0") }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Reset") }
                    }
                }
                ResultLine("Count", "$count")
            }
        }
        item {
            SectionCard("History & undo") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = {
                                if (history.isNotEmpty()) {
                                    scope.launch {
                                        runCatching {
                                            history.removeAt(0)
                                            val prev = runCatching {
                                                history.firstOrNull()
                                                    ?.substringAfter("→")?.trim()?.toIntOrNull()
                                            }.getOrNull() ?: 0
                                            repo.setTallyCount(prev)
                                        }
                                    }
                                }
                            },
                            enabled = history.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Undo last") }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { runCatching { history.clear() } },
                            enabled = history.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Clear history") }
                    }
                }
                if (history.isEmpty()) {
                    ResultLine("History", "No steps yet")
                } else {
                    history.take(50).forEachIndexed { i, h ->
                        ResultLine("#${history.size - i}", h)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiceCoinSection() {
    val sidesOptions = listOf(4, 6, 8, 10, 12, 20)
    var sides by rememberSaveable { mutableStateOf(6) }
    var sidesIn by rememberSaveable { mutableStateOf("6") }
    var sidesError by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val rolls = remember { mutableStateListOf<Int>() }
    var lastFlip by rememberSaveable { mutableStateOf<String?>(null) }
    var heads by rememberSaveable { mutableStateOf(0) }
    var tails by rememberSaveable { mutableStateOf(0) }
    var streakLabel by rememberSaveable { mutableStateOf("") }
    var streakCount by rememberSaveable { mutableStateOf(0) }
    fun validatedSides(): Int? {
        val v = runCatching { sidesIn.trim().toIntOrNull() }.getOrNull()
        if (v == null || v !in 2..100) {
            sidesError = "Sides must be 2..100"
            return null
        }
        sidesError = ""
        return v
    }
    val avg = runCatching {
        if (rolls.isEmpty()) null else rolls.average()
    }.getOrNull()
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Dice") {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = "d$sides",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Preset sides") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        sidesOptions.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("d$s") },
                                onClick = { sides = s; sidesIn = "$s"; sidesError = ""; expanded = false }
                            )
                        }
                    }
                }
                CalcUNumberBox(
                    value = sidesIn,
                    onValueChange = {
                        sidesIn = it
                        runCatching {
                            it.trim().toIntOrNull()?.let { v ->
                                if (v in 2..100) { sides = v; sidesError = "" }
                            }
                        }
                    },
                    label = "Custom sides (2..100)",
                    integer = true,
                    isError = sidesError.isNotEmpty(),
                    supportingText = sidesError.ifEmpty { null }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                val s = validatedSides() ?: return@Button
                                sides = s
                                runCatching { Engine.diceRoll(s) }.onSuccess { r ->
                                    runCatching {
                                        rolls.add(0, r)
                                        while (rolls.size > 50) rolls.removeAt(rolls.size - 1)
                                    }
                                }
                            },
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
                if (sidesError.isNotEmpty()) {
                    Text(sidesError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                if (rolls.isEmpty()) {
                    ResultLine("History", "—")
                } else {
                    ResultLine("Last", "${rolls.firstOrNull() ?: "—"}")
                    ResultLine("Rolls", "${rolls.size} (max 50 kept)")
                    if (avg != null && avg.isFinite()) ResultLine("Average", fmt(avg))
                    HorizontalDivider()
                    Text(
                        "Distribution",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val dist = runCatching { rolls.groupingBy { it }.eachCount().toSortedMap() }.getOrDefault(emptyMap())
                    dist.forEach { (face, n) ->
                        val pct = runCatching { n * 100.0 / rolls.size.coerceAtLeast(1) }.getOrDefault(0.0)
                        ResultLine("$face", "$n × (${fmt(pct)}%)")
                    }
                    HorizontalDivider()
                    rolls.take(50).forEachIndexed { i, r ->
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

private val TrOnes = listOf("", "bir", "iki", "üç", "dört", "beş", "altı", "yedi", "sekiz", "dokuz")
private val TrTens = listOf("", "on", "yirmi", "otuz", "kırk", "elli", "altmış", "yetmiş", "seksen", "doksan")
private val EnDigits = listOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
private val TrDigits = listOf("sıfır", "bir", "iki", "üç", "dört", "beş", "altı", "yedi", "sekiz", "dokuz")

private fun trUnderThousand(v: Long): String {
    val parts = mutableListOf<String>()
    val h = v / 100
    val rest = v % 100
    if (h > 0) {
        if (h == 1L) parts.add("yüz") else parts.add("${TrOnes[h.toInt()]} yüz")
    }
    if (rest > 0) {
        val t = rest / 10
        val o = rest % 10
        if (t > 0) parts.add(TrTens[t.toInt()])
        if (o > 0) parts.add(TrOnes[o.toInt()])
    }
    return parts.joinToString(" ")
}

private fun numberToWordsTr(n: Long): String {
    require(n in 0..999_999_999_999L) { "n must be in 0..999_999_999_999" }
    if (n == 0L) return "sıfır"
    val scales = listOf("", "bin", "milyon", "milyar")
    val chunks = mutableListOf<String>()
    var rem = n
    var idx = 0
    while (rem > 0) {
        require(idx < scales.size) { "n must be in 0..999_999_999_999" }
        val cur = rem % 1000
        if (cur > 0) {
            val words = trUnderThousand(cur)
            val scale = scales[idx]
            chunks.add(
                when {
                    scale.isEmpty() -> words
                    scale == "bin" && cur == 1L -> "bin"
                    else -> "$words $scale"
                }
            )
        }
        rem /= 1000
        idx += 1
    }
    return chunks.reversed().joinToString(" ")
}

@Composable
private fun WordsSection() {
    var input by rememberSaveable { mutableStateOf("123") }
    var turkish by rememberSaveable { mutableStateOf(false) }
    val words = runCatching {
        val raw = input.trim().replace(',', '.')
        if (raw.isEmpty()) return@runCatching "Enter a number"
        val neg = raw.startsWith("-") || raw.startsWith("−")
        val body = raw.trimStart('-', '−', '+').trim()
        if (body.isEmpty()) return@runCatching "Enter a number"
        val parts = body.split('.')
        if (parts.size > 2) return@runCatching if (turkish) "Geçersiz sayı" else "Invalid number"
        val intPart = parts[0].ifEmpty { "0" }
        val intVal = runCatching { intPart.toLong() }.getOrNull()
            ?: return@runCatching if (turkish) "Aralık dışı (0..999999999999)" else "Out of range (0..999999999999)"
        if (intVal !in 0..999_999_999_999L) {
            return@runCatching if (turkish) "Aralık dışı (0..999999999999)" else "Out of range (0..999999999999)"
        }
        val intWords = if (turkish) numberToWordsTr(intVal) else Engine.numberToWords(intVal)
        val frac = if (parts.size == 2) parts[1] else ""
        if (frac.isEmpty() || frac.all { it == '0' } && parts.size == 1) {
            (if (neg && intVal != 0L) (if (turkish) "eksi " else "minus ") else "") + intWords
        } else {
            if (frac.any { !it.isDigit() }) return@runCatching if (turkish) "Geçersiz sayı" else "Invalid number"
            val digits = frac.map { d ->
                val idx = d - '0'
                if (turkish) TrDigits[idx] else EnDigits[idx]
            }.joinToString(" ")
            val point = if (turkish) "virgül" else "point"
            (if (neg) (if (turkish) "eksi " else "minus ") else "") + "$intWords $point $digits"
        }
    }.getOrElse { if (turkish) "Aralık dışı (0..999999999999)" else "Out of range (0..999999999999)" }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Number to words") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !turkish, onClick = { turkish = false }, label = { Text("English") })
                    FilterChip(selected = turkish, onClick = { turkish = true }, label = { Text("Türkçe") })
                }
                CalcUNumberBox(
                    value = input,
                    onValueChange = { input = it },
                    label = if (turkish) "Sayı (-999999999999,99..999999999999,99)" else "Number (-999999999999.99..999999999999.99)"
                )
                Text(
                    if (turkish) "Negatif ve ondalık desteklenir." else "Negatives and decimals supported.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    var doorsIn by rememberSaveable { mutableStateOf("1") }
    var doorAreaIn by rememberSaveable { mutableStateOf("2") }
    var windowsIn by rememberSaveable { mutableStateOf("1") }
    var windowAreaIn by rememberSaveable { mutableStateOf("1.5") }
    var includeCeiling by rememberSaveable { mutableStateOf(false) }
    val len = lenIn.toDoubleOrNull()
    val wid = widIn.toDoubleOrNull()
    val height = heightIn.toDoubleOrNull()
    val coats = coatsIn.toIntOrNull()
    val coverage = coverageIn.toDoubleOrNull()
    val doors = doorsIn.toIntOrNull() ?: 0
    val doorArea = doorAreaIn.toDoubleOrNull() ?: 0.0
    val windows = windowsIn.toIntOrNull() ?: 0
    val windowArea = windowAreaIn.toDoubleOrNull() ?: 0.0
    val dimError = when {
        len == null || len <= 0 -> "Length must be > 0"
        wid == null || wid <= 0 -> "Width must be > 0"
        height == null || height <= 0 -> "Height must be > 0"
        coats == null || coats <= 0 -> "Coats must be > 0"
        coverage == null || coverage <= 0 -> "Coverage must be > 0"
        doors < 0 || windows < 0 -> "Door/window count must be ≥ 0"
        doorArea < 0 || windowArea < 0 -> "Door/window area must be ≥ 0"
        else -> ""
    }
    val grossWall = if (len != null && wid != null && height != null && len > 0 && wid > 0 && height > 0) {
        2 * (len + wid) * height
    } else 0.0
    val ceilingArea = if (includeCeiling && len != null && wid != null && len > 0 && wid > 0) len * wid else 0.0
    val netWall = (grossWall + ceilingArea - doors * doorArea - windows * windowArea).coerceAtLeast(0.0)
    val liters = if (dimError.isEmpty()) {
        runCatching { PaintKit.paintLiters(netWall, coats ?: 0, coverage ?: 0.0) }.getOrNull()
    } else null
    val paintError = runCatching {
        if (dimError.isEmpty()) PaintKit.paintLiters(netWall, coats ?: 0, coverage ?: 0.0)
        null
    }.exceptionOrNull()?.message ?: ""
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = doorsIn, onValueChange = { doorsIn = it }, label = "Doors", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = doorAreaIn, onValueChange = { doorAreaIn = it }, label = "m² / door")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = windowsIn, onValueChange = { windowsIn = it }, label = "Windows", integer = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = windowAreaIn, onValueChange = { windowAreaIn = it }, label = "m² / window")
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include ceiling", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = includeCeiling, onCheckedChange = { includeCeiling = it })
                }
                if (dimError.isNotEmpty()) {
                    Text(dimError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                if (paintError.isNotEmpty()) {
                    Text(paintError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                HorizontalDivider()
                ResultLine("Wall area (gross)", "${fmt(grossWall)} m²")
                ResultLine("Subtracted", "${fmt((doors * doorArea + windows * windowArea).coerceAtLeast(0.0))} m²")
                if (includeCeiling) ResultLine("Ceiling", "${fmt(ceilingArea)} m²")
                ResultLine("Net area", "${fmt(netWall)} m²")
                ResultLine("Paint", if (liters == null) "—" else "${fmt(liters)} L")
            }
        }
    }
}

@Composable
private fun TilesSection() {
    var lenIn by rememberSaveable { mutableStateOf("4") }
    var widIn by rememberSaveable { mutableStateOf("3") }
    var tileLenIn by rememberSaveable { mutableStateOf("30") }
    var tileWidIn by rememberSaveable { mutableStateOf("30") }
    var wasteIn by rememberSaveable { mutableStateOf("10") }
    val len = lenIn.toDoubleOrNull()
    val wid = widIn.toDoubleOrNull()
    val floorArea = if (len != null && wid != null && len > 0 && wid > 0) len * wid else 0.0
    val tileLen = tileLenIn.toDoubleOrNull()
    val tileWid = tileWidIn.toDoubleOrNull()
    val waste = wasteIn.toDoubleOrNull()
    val tileError = when {
        len == null || (len ?: 0.0) <= 0 -> "Room length must be > 0"
        wid == null || (wid ?: 0.0) <= 0 -> "Room width must be > 0"
        tileLen == null || tileLen <= 0 -> "Tile length must be > 0"
        tileWid == null || tileWid <= 0 -> "Tile width must be > 0"
        waste == null || waste < 0 -> "Waste must be ≥ 0"
        else -> ""
    }
    val tiles = if (tileError.isEmpty()) {
        runCatching {
            PaintKit.tilesNeeded(floorArea, tileLen ?: 0.0, tileWid ?: 0.0, waste ?: 0.0)
        }.getOrNull()
    } else null
    val tilesErrText = if (tileError.isEmpty()) {
        runCatching { PaintKit.tilesNeeded(floorArea, tileLen ?: 0.0, tileWid ?: 0.0, waste ?: 0.0) }
            .exceptionOrNull()?.message ?: ""
    } else ""
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Tiles — floor") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = lenIn, onValueChange = { lenIn = it }, label = "Room length (m)")
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = widIn, onValueChange = { widIn = it }, label = "Room width (m)")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = tileLenIn, onValueChange = { tileLenIn = it }, label = "Tile length (cm)")
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = tileWidIn, onValueChange = { tileWidIn = it }, label = "Tile width (cm)")
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = wasteIn, onValueChange = { wasteIn = it }, label = "Waste %")
                    }
                }
                Text(
                    "Tiles = ceil(floor area ÷ tile area × (1 + waste %))",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (tileError.isNotEmpty()) {
                    Text(tileError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                if (tilesErrText.isNotEmpty()) {
                    Text(tilesErrText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                HorizontalDivider()
                ResultLine("Floor area", "${fmt(floorArea)} m²")
                ResultLine("Tiles needed", if (tiles == null) "—" else "$tiles")
            }
        }
    }
}

private fun hamwi(heightCm: Double, male: Boolean): Double {
    val inches = heightCm / 2.54
    val over5ft = (inches - 60.0).coerceAtLeast(0.0)
    val base = if (male) 48.0 else 45.5
    val perInch = if (male) 2.7 else 2.2
    return (base + perInch * over5ft).coerceAtLeast(0.0)
}

private fun miller(heightCm: Double, male: Boolean): Double {
    val inches = heightCm / 2.54
    val over5ft = (inches - 60.0).coerceAtLeast(0.0)
    val base = if (male) 56.2 else 53.1
    val perInch = if (male) 1.41 else 1.36
    return (base + perInch * over5ft).coerceAtLeast(0.0)
}

@Composable
private fun WeightSection() {
    var heightIn by rememberSaveable { mutableStateOf("178") }
    var feetIn by rememberSaveable { mutableStateOf("5") }
    var inchesIn by rememberSaveable { mutableStateOf("10") }
    var useFtIn by rememberSaveable { mutableStateOf(false) }
    var male by rememberSaveable { mutableStateOf(true) }
    var formula by rememberSaveable { mutableStateOf("Devine") }
    val heightCm = if (!useFtIn) {
        heightIn.toDoubleOrNull()
    } else {
        val ft = runCatching { feetIn.toDoubleOrNull() }.getOrNull()
        val inch = runCatching { inchesIn.toDoubleOrNull() }.getOrNull()
        if (ft == null || inch == null) null else (ft * 12 + inch) * 2.54
    }
    val heightError = when {
        heightCm == null -> "Enter a valid height"
        heightCm <= 0 -> "Height must be > 0"
        heightCm > 300 -> "Height must be ≤ 300 cm"
        else -> ""
    }
    val devine = if (heightError.isEmpty()) runCatching { IdealWeight.devine(heightCm ?: 0.0, male) }.getOrNull() else null
    val robinson = if (heightError.isEmpty()) runCatching { IdealWeight.robinson(heightCm ?: 0.0, male) }.getOrNull() else null
    val hamwiV = if (heightError.isEmpty()) runCatching { hamwi(heightCm ?: 0.0, male) }.getOrNull() else null
    val millerV = if (heightError.isEmpty()) runCatching { miller(heightCm ?: 0.0, male) }.getOrNull() else null
    val formulaLabel = when (formula) {
        "Robinson" -> "Robinson (1983)"
        "Hamwi" -> "Hamwi (1964)"
        "Miller" -> "Miller (1983)"
        else -> "Devine (1974)"
    }
    val mainValue = when (formula) {
        "Robinson" -> robinson
        "Hamwi" -> hamwiV
        "Miller" -> millerV
        else -> devine
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Ideal weight") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !useFtIn, onClick = { useFtIn = false }, label = { Text("cm") })
                    FilterChip(selected = useFtIn, onClick = { useFtIn = true }, label = { Text("ft/in") })
                }
                if (!useFtIn) {
                    CalcUNumberBox(value = heightIn, onValueChange = { heightIn = it }, label = "Height (cm)")
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            CalcUNumberBox(value = feetIn, onValueChange = { feetIn = it }, label = "Feet", integer = true)
                        }
                        Box(Modifier.weight(1f)) {
                            CalcUNumberBox(value = inchesIn, onValueChange = { inchesIn = it }, label = "Inches")
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = male, onClick = { male = true }, label = { Text("Male") })
                    FilterChip(selected = !male, onClick = { male = false }, label = { Text("Female") })
                }
                Text(
                    "Formula",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Devine", "Robinson", "Hamwi", "Miller")) { f ->
                        FilterChip(selected = formula == f, onClick = { formula = f }, label = { Text(f) })
                    }
                }
                if (heightError.isNotEmpty()) {
                    Text(heightError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                Text(
                    formulaLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        if (mainValue == null) "—" else "${fmt(mainValue)} kg",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ResultLine("Ideal weight ($formulaLabel)", if (mainValue == null) "—" else "${fmt(mainValue)} kg")
                HorizontalDivider()
                ResultLine("Devine (1974)", if (devine == null) "—" else "${fmt(devine)} kg")
                ResultLine("Robinson (1983)", if (robinson == null) "—" else "${fmt(robinson)} kg")
                ResultLine("Hamwi (1964)", if (hamwiV == null) "—" else "${fmt(hamwiV)} kg")
                ResultLine("Miller (1983)", if (millerV == null) "—" else "${fmt(millerV)} kg")
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
    var volume by rememberSaveable { mutableStateOf(70) }
    var accent by rememberSaveable { mutableStateOf(true) }
    val taps = remember { mutableStateListOf<Long>() }
    var tapLabel by remember { mutableStateOf("") }
    val haptics = LocalHapticFeedback.current
    var toneGen by remember { mutableStateOf<ToneGenerator?>(null) }
    LaunchedEffect(volume) {
        runCatching { toneGen?.release() }
        toneGen = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, volume.coerceIn(0, 100)) }.getOrNull()
    }
    DisposableEffect(Unit) {
        onDispose { runCatching { toneGen?.release() } }
    }
    LaunchedEffect(running, bpm, beats, accent) {
        if (!running) return@LaunchedEffect
        beat = 0
        val safeBeats = beats.coerceIn(1, 12)
        val interval = (60000f / bpm.coerceIn(1f, 600f)).toLong().coerceIn(50L, 5000L)
        while (running) {
            val isAccent = accent && (beat % safeBeats.coerceAtLeast(1)) == 0
            runCatching {
                if (isAccent) toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 90)
                else toneGen?.startTone(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE, 50)
            }
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
                Text(
                    "Beats per bar",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 3, 4, 6).forEach { b ->
                        FilterChip(
                            selected = beats == b,
                            onClick = { beats = b; beat = 0 },
                            label = { Text("$b beats") }
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Accent first beat", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = accent, onCheckedChange = { accent = it })
                }
                Text("Click volume ($volume%)", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = volume.toFloat(),
                    onValueChange = { volume = it.toInt().coerceIn(0, 100) },
                    valueRange = 0f..100f
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(onClick = { running = !running }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (running) "Stop" else "Start")
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = {
                                val now = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L)
                                runCatching {
                                    if (taps.isNotEmpty() && now - (taps.lastOrNull() ?: 0L) > 2500L) taps.clear()
                                    taps.add(now)
                                    if (taps.size > 8) taps.removeAt(0)
                                    if (taps.size >= 2) {
                                        val intervals = taps.zipWithNext { a, b -> (b - a).coerceAtLeast(1L) }
                                        val avgMs = intervals.average()
                                        if (avgMs.isFinite() && avgMs > 0) {
                                            val nb = (60000.0 / avgMs).toFloat().coerceIn(30f, 240f)
                                            if (nb.isFinite()) {
                                                bpm = nb
                                                tapLabel = "${nb.toInt()} BPM from ${taps.size} taps"
                                            }
                                        }
                                    } else {
                                        tapLabel = "Tap again…"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Tap tempo") }
                    }
                }
                if (tapLabel.isNotEmpty()) {
                    Text(tapLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    if (running) "Beat ${beat + 1} of $beats" else "Paused — audio click + haptic",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val PassphraseWords = listOf(
    "amber", "basin", "cabin", "drift", "ember", "flint", "grove", "harbor",
    "ivory", "jungle", "karma", "lemon", "meadow", "north", "ocean", "piano",
    "quartz", "river", "sunset", "tiger", "unity", "valley", "willow", "xenon",
    "yogurt", "zebra", "anchor", "bridge", "cloud", "delta", "eagle", "forest"
)

private fun crackTimeLabel(entropyBits: Double): String {
    if (!entropyBits.isFinite() || entropyBits <= 0) return "—"
    val seconds = runCatching { 2.0.pow((entropyBits - 1).coerceAtLeast(0.0)) / 1e10 }.getOrDefault(Double.NaN)
    if (!seconds.isFinite()) return " effectively forever"
    if (seconds < 1) return "instant (< 1 s)"
    val mins = seconds / 60
    val hours = mins / 60
    val days = hours / 24
    val years = days / 365.25
    return when {
        seconds < 60 -> "${fmt(seconds)} s"
        mins < 60 -> "${fmt(mins)} min"
        hours < 48 -> "${fmt(hours)} hours"
        days < 730 -> "${fmt(days)} days"
        years < 1e6 -> "${fmt(years)} years"
        else -> "millions of years"
    }
}

@Composable
private fun PasswordSection() {
    var mode by rememberSaveable { mutableStateOf("random") }
    var length by rememberSaveable { mutableStateOf(16f) }
    var digits by rememberSaveable { mutableStateOf(4) }
    var specials by rememberSaveable { mutableStateOf(2) }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val len = length.toInt().coerceIn(4, 64)
    val safeDigits = digits.coerceIn(0, len)
    val safeSpecials = specials.coerceIn(0, len - safeDigits)
    // Fixed pool counting: letters always span upper+lower (52), plus digits/specials only when used.
    val poolSize = 52 + (if (safeDigits > 0) 10 else 0) + (if (safeSpecials > 0) 27 else 0)
    val entropyBits = runCatching { len * log2(poolSize.coerceAtLeast(2).toDouble()) }.getOrDefault(0.0)
    val passphraseEntropy = runCatching { 4 * log2(PassphraseWords.size.toDouble()) }.getOrDefault(0.0)
    val shownEntropy = if (mode == "passphrase") passphraseEntropy else entropyBits
    val pools = 2 + (if (safeDigits > 0) 1 else 0) + (if (safeSpecials > 0) 1 else 0)
    val strength = when {
        len >= 20 && pools == 4 -> "Very strong"
        len >= 14 && pools >= 3 -> "Strong"
        len >= 12 && pools >= 2 -> "Good"
        len >= 8 && pools >= 2 -> "Fair"
        else -> "Weak"
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Password generator") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = mode == "random", onClick = { mode = "random" }, label = { Text("Random") })
                    FilterChip(selected = mode == "passphrase", onClick = { mode = "passphrase" }, label = { Text("Passphrase") })
                }
                if (mode == "random") {
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
                } else {
                    Text(
                        "4-word passphrase from a built-in ${PassphraseWords.size}-word list",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                if (mode == "random") {
                                    runCatching { PasswordKit.generate(len, safeDigits, safeSpecials) }
                                        .onSuccess { password = it; error = "" }
                                        .onFailure { error = it.message ?: "Invalid options" }
                                } else {
                                    runCatching {
                                        val rng = java.security.SecureRandom()
                                        (0 until 4).map { PassphraseWords[rng.nextInt(PassphraseWords.size)] }
                                            .joinToString("-")
                                    }.onSuccess { password = it; error = "" }
                                        .onFailure { error = it.message ?: "Could not generate passphrase" }
                                }
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
                if (mode == "random") ResultLine("Strength", strength)
                ResultLine("Entropy", "${fmt(shownEntropy)} bits")
                ResultLine("Crack time*", crackTimeLabel(shownEntropy))
                Text(
                    "Pool: upper (26) + lower (26)${if (safeDigits > 0 && mode == "random") " + digits (10)" else ""}${if (safeSpecials > 0 && mode == "random") " + specials (27)" else ""}. *≈10B guesses/s.",
                    style = MaterialTheme.typography.bodySmall,
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
