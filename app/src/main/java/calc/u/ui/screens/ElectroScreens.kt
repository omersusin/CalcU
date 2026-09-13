package calc.u.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import calc.u.core.Electro
import calc.u.core.Network
import calc.u.ui.CalcUNumberBox
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val DigitColors = listOf("black", "brown", "red", "orange", "yellow", "green", "blue", "violet", "gray", "white")
private val MultiplierColors = DigitColors + listOf("gold", "silver")
private val ToleranceColors = listOf("brown", "red", "green", "blue", "violet", "gray", "gold", "silver", "none")
private val TempcoColors = listOf("brown", "red", "orange", "yellow", "blue", "violet", "white")
private val TempcoPpm = mapOf(
    "brown" to "100 ppm/K", "red" to "50 ppm/K", "orange" to "15 ppm/K",
    "yellow" to "25 ppm/K", "blue" to "10 ppm/K", "violet" to "5 ppm/K", "white" to "1 ppm/K"
)
private val DigitToColor = mapOf(
    0 to "black", 1 to "brown", 2 to "red", 3 to "orange", 4 to "yellow",
    5 to "green", 6 to "blue", 7 to "violet", 8 to "gray", 9 to "white"
)
private val ExpToMultiplierColor = mapOf(
    -2 to "silver", -1 to "gold", 0 to "black", 1 to "brown", 2 to "red",
    3 to "orange", 4 to "yellow", 5 to "green", 6 to "blue", 7 to "violet",
    8 to "gray", 9 to "white"
)

private fun resistorSwatch(name: String): Color {
    return when (runCatching { name.trim().lowercase() }.getOrDefault("")) {
        "black" -> Color(0xFF1A1A1A)
        "brown" -> Color(0xFF6D4C41)
        "red" -> Color(0xFFD32F2F)
        "orange" -> Color(0xFFF57C00)
        "yellow" -> Color(0xFFFBC02D)
        "green" -> Color(0xFF388E3C)
        "blue" -> Color(0xFF1976D2)
        "violet" -> Color(0xFF7B1FA2)
        "gray", "grey" -> Color(0xFF9E9E9E)
        "white" -> Color(0xFFFAFAFA)
        "gold" -> Color(0xFFC9A227)
        "silver" -> Color(0xFFB0BEC5)
        else -> Color.Transparent
    }
}

@Composable
private fun BandStrip(colors: List<String>) {
    Row(
        Modifier.fillMaxWidth().height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.forEach { c ->
            Box(
                Modifier.weight(1f).height(28.dp)
                    .background(resistorSwatch(c))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { o ->
                DropdownMenuItem(text = { Text(o) }, onClick = { onSelect(o); expanded = false })
            }
        }
    }
}

@Composable
fun ElectroScreen() {
    var tab by remember { mutableStateOf("resistor") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("resistor" to "Resistor", "subnet" to "Subnet")) { (id, label) ->
                FilterChip(selected = tab == id, onClick = { tab = id }, label = { Text(label) })
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (tab == "subnet") SubnetScreen() else ResistorScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResistorScreen() {
    var bands by remember { mutableStateOf(4) }
    var b1 by remember { mutableStateOf("brown") }
    var b2 by remember { mutableStateOf("black") }
    var b3 by remember { mutableStateOf("red") }
    var b4 by remember { mutableStateOf("red") }
    var b5 by remember { mutableStateOf("gold") }
    var b6 by rememberSaveable { mutableStateOf("brown") }
    var reverse by rememberSaveable { mutableStateOf(false) }
    var valueIn by rememberSaveable { mutableStateOf("4.7k") }
    var revTol by rememberSaveable { mutableStateOf("gold") }
    val decoded = runCatching {
        when (bands) {
            4 -> Electro.decode4Band(listOf(b1, b2, b3, b4))
            6 -> Electro.decode5Band(listOf(b1, b2, b3, b4, b5)) + " " + (TempcoPpm[b6.trim().lowercase()] ?: "")
            else -> Electro.decode5Band(listOf(b1, b2, b3, b4, b5))
        }
    }.getOrElse { "—" }
    val decodeError = runCatching {
        when (bands) {
            4 -> Electro.decode4Band(listOf(b1, b2, b3, b4))
            6 -> Electro.decode5Band(listOf(b1, b2, b3, b4, b5))
            else -> Electro.decode5Band(listOf(b1, b2, b3, b4, b5))
        }
        null
    }.exceptionOrNull()?.message ?: ""
    fun parseOhms(raw: String): Double? {
        val t = runCatching { raw.trim().lowercase().replace("ω", "").replace("ohms", "").replace("ohm", "") }.getOrDefault("")
        if (t.isEmpty()) return null
        var mult = 1.0
        var num = t
        when {
            num.endsWith("meg") -> { mult = 1e6; num = num.dropLast(3) }
            num.endsWith("m") && !num.endsWith("mm") -> { mult = 1e6; num = num.dropLast(1) }
            num.endsWith("k") -> { mult = 1e3; num = num.dropLast(1) }
            num.endsWith("g") -> { mult = 1e9; num = num.dropLast(1) }
            num.endsWith("r") -> { num = num.dropLast(1) }
        }
        val v = runCatching { num.trim().toDoubleOrNull() }.getOrNull() ?: return null
        if (!v.isFinite() || v <= 0) return null
        return v * mult
    }
    val revResult: List<String>? = runCatching {
        val ohms = parseOhms(valueIn) ?: return@runCatching null
        if (ohms < 10 || ohms > 99e9) return@runCatching null
        val exp = floor(log10(ohms / 10.0)).toInt().coerceIn(-2, 9)
        val sig = (ohms / 10.0.pow(exp.toDouble())).toInt()
        if (sig !in 10..99) return@runCatching null
        val d1 = DigitToColor[sig / 10] ?: return@runCatching null
        val d2 = DigitToColor[sig % 10] ?: return@runCatching null
        val mult = ExpToMultiplierColor[exp] ?: return@runCatching null
        listOf(d1, d2, mult, revTol)
    }.getOrNull()
    val revError = if (reverse) {
        when {
            parseOhms(valueIn) == null -> "Enter a value like 4700, 4.7k, 1M"
            revResult == null -> "Out of range for 4-band reverse (10 Ω..99 GΩ)"
            else -> ""
        }
    } else ""
    val shownBands: List<String> = if (reverse && revResult != null) revResult else when (bands) {
        4 -> listOf(b1, b2, b3, b4)
        6 -> listOf(b1, b2, b3, b4, b5, b6)
        else -> listOf(b1, b2, b3, b4, b5)
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Resistor color code") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(4, 5, 6)) { n ->
                        FilterChip(selected = bands == n, onClick = { bands = n }, label = { Text("$n-band") })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !reverse, onClick = { reverse = false }, label = { Text("Colors → value") })
                    FilterChip(selected = reverse, onClick = { reverse = true }, label = { Text("Value → colors") })
                }
                if (!reverse) {
                    if (bands == 4) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { ColorDropdown(b1, DigitColors, { b1 = it }, "Band 1 (digit)") }
                            Box(Modifier.weight(1f)) { ColorDropdown(b2, DigitColors, { b2 = it }, "Band 2 (digit)") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { ColorDropdown(b3, MultiplierColors, { b3 = it }, "Multiplier") }
                            Box(Modifier.weight(1f)) { ColorDropdown(b4, ToleranceColors, { b4 = it }, "Tolerance") }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { ColorDropdown(b1, DigitColors, { b1 = it }, "Band 1 (digit)") }
                            Box(Modifier.weight(1f)) { ColorDropdown(b2, DigitColors, { b2 = it }, "Band 2 (digit)") }
                            Box(Modifier.weight(1f)) { ColorDropdown(b3, DigitColors, { b3 = it }, "Band 3 (digit)") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { ColorDropdown(b4, MultiplierColors, { b4 = it }, "Multiplier") }
                            Box(Modifier.weight(1f)) { ColorDropdown(b5, ToleranceColors, { b5 = it }, "Tolerance") }
                        }
                        if (bands == 6) {
                            ColorDropdown(b6, TempcoColors, { b6 = it }, "Tempco (ppm/K)")
                        }
                    }
                } else {
                    CalcUNumberBox(value = valueIn, onValueChange = { valueIn = it }, label = "Value (e.g. 4700, 4.7k, 1M)")
                    ColorDropdown(revTol, ToleranceColors, { revTol = it }, "Tolerance")
                    if (revError.isNotEmpty()) {
                        Text(revError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                    if (revResult != null) {
                        ResultLine("Colors", revResult.joinToString(" · "))
                    }
                }
                HorizontalDivider()
                Text(
                    "Band preview",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BandStrip(shownBands)
                ResultLine("Value", decoded)
                if (decodeError.isNotEmpty()) {
                    Text(decodeError, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        item { DividerCard() }
        item { LedCard() }
        item { RcCard() }
    }
}

@Composable
private fun DividerCard() {
    var vin by rememberSaveable { mutableStateOf("5") }
    var r1 by rememberSaveable { mutableStateOf("1000") }
    var r2 by rememberSaveable { mutableStateOf("1000") }
    val vout = runCatching {
        Electro.voltageDivider(vin.toDoubleOrNull() ?: 0.0, r1.toDoubleOrNull() ?: 0.0, r2.toDoubleOrNull() ?: 0.0)
    }.getOrNull()
    val err = runCatching {
        Electro.voltageDivider(vin.toDoubleOrNull() ?: 0.0, r1.toDoubleOrNull() ?: 0.0, r2.toDoubleOrNull() ?: 0.0)
        null
    }.exceptionOrNull()?.message ?: ""
    val friendlyErr = when {
        err.isEmpty() -> ""
        err.contains("r1") || err.contains("r2") -> "R1 and R2 must be > 0 Ω"
        else -> err
    }
    SectionCard("Voltage divider") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) { CalcUNumberBox(value = vin, onValueChange = { vin = it }, label = "Vin (V)") }
            Box(Modifier.weight(1f)) { CalcUNumberBox(value = r1, onValueChange = { r1 = it }, label = "R1 (Ω)") }
            Box(Modifier.weight(1f)) { CalcUNumberBox(value = r2, onValueChange = { r2 = it }, label = "R2 (Ω)") }
        }
        Text(
            "Vout = Vin × R2 ÷ (R1 + R2)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (friendlyErr.isNotEmpty()) {
            Text(friendlyErr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        HorizontalDivider()
        ResultLine("Vout", vout?.let { runCatching { "%.4f V".format(it) }.getOrDefault("—") } ?: "—")
    }
}

@Composable
private fun LedCard() {
    var vs by rememberSaveable { mutableStateOf("5") }
    var vf by rememberSaveable { mutableStateOf("2") }
    var ma by rememberSaveable { mutableStateOf("20") }
    val rled = runCatching {
        Electro.ledResistor(vs.toDoubleOrNull() ?: 0.0, vf.toDoubleOrNull() ?: 0.0, ma.toDoubleOrNull() ?: 0.0)
    }.getOrNull()
    val err = runCatching {
        Electro.ledResistor(vs.toDoubleOrNull() ?: 0.0, vf.toDoubleOrNull() ?: 0.0, ma.toDoubleOrNull() ?: 0.0)
        null
    }.exceptionOrNull()?.message ?: ""
    val friendlyErr = when {
        err.isEmpty() -> ""
        err.contains("exceed") -> "Vf must be less than Vs (supply must exceed LED drop)"
        err.contains("maMilliamps") || err.contains("> 0") -> "Current must be > 0 mA"
        else -> err
    }
    SectionCard("LED series resistor") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) { CalcUNumberBox(value = vs, onValueChange = { vs = it }, label = "Vsupply (V)") }
            Box(Modifier.weight(1f)) { CalcUNumberBox(value = vf, onValueChange = { vf = it }, label = "Vf (V)") }
            Box(Modifier.weight(1f)) { CalcUNumberBox(value = ma, onValueChange = { ma = it }, label = "Current (mA)") }
        }
        Text(
            "R = (Vs − Vf) ÷ I",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (friendlyErr.isNotEmpty()) {
            Text(friendlyErr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        HorizontalDivider()
        ResultLine("R (LED series)", rled?.let { runCatching { "%.2f Ω".format(it) }.getOrDefault("—") } ?: "—")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RcCard() {
    var rIn by rememberSaveable { mutableStateOf("10") }
    var rUnit by rememberSaveable { mutableStateOf("kΩ") }
    var cIn by rememberSaveable { mutableStateOf("100") }
    var cUnit by rememberSaveable { mutableStateOf("nF") }
    var rExpanded by remember { mutableStateOf(false) }
    var cExpanded by remember { mutableStateOf(false) }
    val rMult = when (rUnit) { "MΩ" -> 1e6; "kΩ" -> 1e3; else -> 1.0 }
    val cMult = when (cUnit) { "pF" -> 1e-12; "nF" -> 1e-9; "µF" -> 1e-6; "mF" -> 1e-3; else -> 1.0 }
    val rOhms = runCatching { (rIn.toDoubleOrNull() ?: 0.0) * rMult }.getOrDefault(0.0)
    val cFarads = runCatching { (cIn.toDoubleOrNull() ?: 0.0) * cMult }.getOrDefault(0.0)
    val tau = runCatching { Electro.rcTimeConstant(rOhms, cFarads) }.getOrNull()
    val fc = if (tau != null && tau.isFinite() && tau > 0) runCatching { 1.0 / (2 * Math.PI * tau) }.getOrNull() else null
    val charge5t = if (tau != null && tau.isFinite() && tau > 0) runCatching { 5 * tau }.getOrNull() else null
    val err = runCatching { Electro.rcTimeConstant(rOhms, cFarads); null }.exceptionOrNull()?.message ?: ""
    val friendlyErr = when {
        err.isEmpty() -> ""
        err.contains("rOhms") -> "R must be > 0 Ω"
        err.contains("cFarads") -> "C must be > 0 F"
        else -> err
    }
    SectionCard("RC time constant") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(2f)) { CalcUNumberBox(value = rIn, onValueChange = { rIn = it }, label = "Resistance") }
            Box(Modifier.weight(1f)) {
                ExposedDropdownMenuBox(expanded = rExpanded, onExpandedChange = { rExpanded = it }) {
                    OutlinedTextField(
                        value = rUnit, onValueChange = {}, readOnly = true, label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = rExpanded, onDismissRequest = { rExpanded = false }) {
                        listOf("Ω", "kΩ", "MΩ").forEach { u ->
                            DropdownMenuItem(text = { Text(u) }, onClick = { rUnit = u; rExpanded = false })
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(2f)) { CalcUNumberBox(value = cIn, onValueChange = { cIn = it }, label = "Capacitance") }
            Box(Modifier.weight(1f)) {
                ExposedDropdownMenuBox(expanded = cExpanded, onExpandedChange = { cExpanded = it }) {
                    OutlinedTextField(
                        value = cUnit, onValueChange = {}, readOnly = true, label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = cExpanded, onDismissRequest = { cExpanded = false }) {
                        listOf("pF", "nF", "µF", "mF", "F").forEach { u ->
                            DropdownMenuItem(text = { Text(u) }, onClick = { cUnit = u; cExpanded = false })
                        }
                    }
                }
            }
        }
        Text(
            "τ = R × C · fc = 1 ÷ (2πτ) · full charge ≈ 5τ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (friendlyErr.isNotEmpty()) {
            Text(friendlyErr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        HorizontalDivider()
        ResultLine("τ = RC", tau?.let { runCatching { "%.6f s".format(it) }.getOrDefault("—") } ?: "—")
        ResultLine("Cutoff fc", fc?.let { runCatching { "%.3f Hz".format(it) }.getOrDefault("—") } ?: "—")
        ResultLine("Charge time (5τ)", charge5t?.let { runCatching { "%.6f s".format(it) }.getOrDefault("—") } ?: "—")
    }
}

@Composable
fun SubnetScreen() {
    var ip by remember { mutableStateOf("192.168.1.10") }
    var prefix by remember { mutableStateOf("24") }
    val info = runCatching {
        Network.subnet(ip, prefix.toIntOrNull() ?: -1)
    }.getOrNull()
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Subnet calculator") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = ip,
                            onValueChange = { ip = it },
                            label = { Text("IP") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        CalcUNumberBox(value = prefix, onValueChange = { prefix = it }, label = "Prefix", integer = true)
                    }
                }
                HorizontalDivider()
                if (info == null) {
                    ResultLine("Result", "—")
                } else {
                    ResultLine("Network", info.network)
                    ResultLine("Broadcast", info.broadcast)
                    ResultLine("Mask", info.mask)
                    ResultLine("Hosts", "${info.hosts}")
                    ResultLine("First", info.firstHost)
                    ResultLine("Last", info.lastHost)
                }
            }
        }
    }
}
