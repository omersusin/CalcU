package calc.u.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calc.u.core.Electro
import calc.u.core.Network
import calc.u.ui.CalcUNumberBox
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard

private val DigitColors = listOf("black", "brown", "red", "orange", "yellow", "green", "blue", "violet", "gray", "white")
private val MultiplierColors = DigitColors + listOf("gold", "silver")
private val ToleranceColors = listOf("brown", "red", "green", "blue", "violet", "gray", "gold", "silver", "none")

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

@Composable
fun ResistorScreen() {
    var bands by remember { mutableStateOf(4) }
    var b1 by remember { mutableStateOf("brown") }
    var b2 by remember { mutableStateOf("black") }
    var b3 by remember { mutableStateOf("red") }
    var b4 by remember { mutableStateOf("red") }
    var b5 by remember { mutableStateOf("gold") }
    var vin by remember { mutableStateOf("5") }
    var r1 by remember { mutableStateOf("1000") }
    var r2 by remember { mutableStateOf("1000") }
    var vs by remember { mutableStateOf("5") }
    var vf by remember { mutableStateOf("2") }
    var ma by remember { mutableStateOf("20") }
    var rrc by remember { mutableStateOf("10000") }
    var crc by remember { mutableStateOf("0.0001") }
    val decoded = runCatching {
        if (bands == 4) Electro.decode4Band(listOf(b1, b2, b3, b4))
        else Electro.decode5Band(listOf(b1, b2, b3, b4, b5))
    }.getOrDefault("—")
    val vout = runCatching {
        Electro.voltageDivider(vin.toDoubleOrNull() ?: 0.0, r1.toDoubleOrNull() ?: 0.0, r2.toDoubleOrNull() ?: 0.0)
    }.getOrNull()
    val rled = runCatching {
        Electro.ledResistor(vs.toDoubleOrNull() ?: 0.0, vf.toDoubleOrNull() ?: 0.0, ma.toDoubleOrNull() ?: 0.0)
    }.getOrNull()
    val tau = runCatching {
        Electro.rcTimeConstant(rrc.toDoubleOrNull() ?: 0.0, crc.toDoubleOrNull() ?: 0.0)
    }.getOrNull()
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Resistor color code") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(4, 5)) { n ->
                        FilterChip(selected = bands == n, onClick = { bands = n }, label = { Text("$n-band") })
                    }
                }
                if (bands == 4) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { ColorDropdown(b1, DigitColors, { b1 = it }, "Band 1") }
                        Box(Modifier.weight(1f)) { ColorDropdown(b2, DigitColors, { b2 = it }, "Band 2") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { ColorDropdown(b3, MultiplierColors, { b3 = it }, "Mult") }
                        Box(Modifier.weight(1f)) { ColorDropdown(b4, ToleranceColors, { b4 = it }, "Tol") }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { ColorDropdown(b1, DigitColors, { b1 = it }, "Band 1") }
                        Box(Modifier.weight(1f)) { ColorDropdown(b2, DigitColors, { b2 = it }, "Band 2") }
                        Box(Modifier.weight(1f)) { ColorDropdown(b3, DigitColors, { b3 = it }, "Band 3") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { ColorDropdown(b4, MultiplierColors, { b4 = it }, "Mult") }
                        Box(Modifier.weight(1f)) { ColorDropdown(b5, ToleranceColors, { b5 = it }, "Tol") }
                    }
                }
                HorizontalDivider()
                ResultLine("Value", decoded)
            }
        }
        item {
            SectionCard("Divider / LED / RC") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { CalcUNumberBox(value = vin, onValueChange = { vin = it }, label = "Vin") }
                    Box(Modifier.weight(1f)) { CalcUNumberBox(value = r1, onValueChange = { r1 = it }, label = "R1 Ω") }
                    Box(Modifier.weight(1f)) { CalcUNumberBox(value = r2, onValueChange = { r2 = it }, label = "R2 Ω") }
                }
                ResultLine("Vout", vout?.let { "%.4f V".format(it) } ?: "—")
                HorizontalDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { CalcUNumberBox(value = vs, onValueChange = { vs = it }, label = "Vsupply") }
                    Box(Modifier.weight(1f)) { CalcUNumberBox(value = vf, onValueChange = { vf = it }, label = "Vf") }
                    Box(Modifier.weight(1f)) { CalcUNumberBox(value = ma, onValueChange = { ma = it }, label = "mA") }
                }
                ResultLine("R LED", rled?.let { "%.2f Ω".format(it) } ?: "—")
                HorizontalDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { CalcUNumberBox(value = rrc, onValueChange = { rrc = it }, label = "R Ω") }
                    Box(Modifier.weight(1f)) { CalcUNumberBox(value = crc, onValueChange = { crc = it }, label = "C F") }
                }
                ResultLine("τ = RC", tau?.let { "%.6f s".format(it) } ?: "—")
            }
        }
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
