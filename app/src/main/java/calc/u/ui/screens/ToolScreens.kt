package calc.u.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calc.u.core.Currency
import calc.u.core.Engine
import calc.u.core.Finance
import calc.u.core.Geometry
import calc.u.core.HealthDate
import calc.u.core.Units
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertersScreen() {
    var input by remember { mutableStateOf("1") }
    var cat by remember { mutableStateOf("length") }
    val v = input.toDoubleOrNull() ?: 0.0
    val cats = listOf("length", "mass", "volume", "temp", "area", "speed", "pressure", "energy", "power", "data")
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard("Value") {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Value to convert") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cats) { c ->
                        FilterChip(selected = c == cat, onClick = { cat = c }, label = { Text(c) })
                    }
                }
            }
        }
        if (cat == "temp") {
            item {
                SectionCard("Temperature (from Celsius)") {
                    ResultLine("Fahrenheit", "%.4f".format(Units.convertTemp(v, "C", "F")))
                    ResultLine("Kelvin", "%.4f".format(Units.convertTemp(v, "C", "K")))
                    HorizontalDivider()
                    ResultLine("5 ft 9 in in cm", "%.2f".format(Units.ftInToCm(5.0, 9.0)))
                    ResultLine("Binary", Units.fromBase(v, 2))
                    ResultLine("Octal", Units.fromBase(v, 8))
                    ResultLine("Hex", Units.fromBase(v, 16))
                    ResultLine("Roman", runCatching { Units.toRoman(v.toInt()) }.getOrDefault("—"))
                }
            }
        } else {
            val map: Map<String, Units.UnitDef>? = when (cat) {
                "length" -> Units.length
                "mass" -> Units.mass
                "volume" -> Units.volume
                "area" -> Units.area
                "speed" -> Units.speed
                "pressure" -> Units.pressure
                "energy" -> Units.energy
                "power" -> Units.power
                "data" -> Units.data
                else -> null
            }
            if (map != null) {
                val base = map.values.first()
                val baseName = map.keys.first()
                items(map.entries.toList()) { (name, def) ->
                    val out = runCatching { Units.convert(v, base, def) }.getOrDefault(0.0)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(name, style = MaterialTheme.typography.labelLarge)
                            Text(
                                "$v $baseName = $out $name",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceScreen() {
    var bill by remember { mutableStateOf("100") }
    var tipPct by remember { mutableFloatStateOf(15f) }
    var split by remember { mutableStateOf("2") }
    var principal by remember { mutableStateOf("10000") }
    var rate by remember { mutableStateOf("5") }
    var months by remember { mutableStateOf("24") }
    val billV = bill.toDoubleOrNull() ?: 0.0
    val splitV = split.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val (tipAmt, grand, per) = Finance.tip(billV, tipPct.toDouble(), splitV)
    val emi = Finance.emi(principal.toDoubleOrNull() ?: 0.0, rate.toDoubleOrNull() ?: 0.0, months.toIntOrNull() ?: 0)
    val (si, siTotal) = Finance.simple(principal.toDoubleOrNull() ?: 0.0, rate.toDoubleOrNull() ?: 0.0, 2.0)
    val ci = Finance.compound(principal.toDoubleOrNull() ?: 0.0, rate.toDoubleOrNull() ?: 0.0, 2.0)
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard("Tip and split") {
                OutlinedTextField(bill, { bill = it }, label = { Text("Bill") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Tip: ${tipPct.toInt()}%", style = MaterialTheme.typography.labelLarge)
                Slider(value = tipPct, onValueChange = { tipPct = it }, valueRange = 0f..30f)
                OutlinedTextField(split, { split = it }, label = { Text("Split between") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                HorizontalDivider()
                ResultLine("Tip", "%.2f".format(tipAmt))
                ResultLine("Total", "%.2f".format(grand))
                ResultLine("Per person", "%.2f".format(per))
            }
        }
        item {
            SectionCard("Loan EMI") {
                OutlinedTextField(principal, { principal = it }, label = { Text("Principal") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(rate, { rate = it }, label = { Text("Annual %") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(months, { months = it }, label = { Text("Months") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                HorizontalDivider()
                ResultLine("Monthly EMI", "%.2f".format(emi))
                ResultLine("Simple 2y interest", "%.2f".format(si))
                ResultLine("Simple 2y total", "%.2f".format(siTotal))
                ResultLine("Compound 2y", "%.2f".format(ci))
            }
        }
        item {
            SectionCard("Sales tax") {
                val (gross, tax) = Finance.withTax(100.0, 10.0, false)
                val (net, taxIn) = Finance.withTax(110.0, 10.0, true)
                ResultLine("100 + 10% tax", "%.2f (tax %.2f)".format(gross, tax))
                ResultLine("110 incl. 10% tax", "net %.2f (tax %.2f)".format(net, taxIn))
            }
        }
    }
}

@Composable
fun MathScreen() {
    var a by remember { mutableStateOf("12") }
    var b by remember { mutableStateOf("18") }
    var quad by remember { mutableStateOf("1,-3,2") }
    val av = a.toLongOrNull() ?: 0L
    val bv = b.toLongOrNull() ?: 0L
    val parts = quad.split(",").mapNotNull { it.trim().toDoubleOrNull() }
    val roots = if (parts.size == 3) Engine.solveQuadratic(parts[0], parts[1], parts[2]) else emptyList()
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard("Number theory") {
                OutlinedTextField(a, { a = it }, label = { Text("a") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(b, { b = it }, label = { Text("b") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                HorizontalDivider()
                ResultLine("GCD", "${Engine.gcd(av, bv)}")
                ResultLine("LCM", "${runCatching { Engine.lcm(av, bv) }.getOrDefault(0)}")
                ResultLine("a is prime", if (Engine.isPrime(av)) "yes" else "no")
                ResultLine("nCr", "${runCatching { Engine.nCr(av, bv) }.getOrDefault(0)}")
                ResultLine("nPr", "${runCatching { Engine.nPr(av, bv) }.getOrDefault(0)}")
                ResultLine("a in hex", Units.fromBase(av.toDouble(), 16))
                ResultLine("a in roman", runCatching { Units.toRoman(av.toInt()) }.getOrDefault("—"))
                ResultLine("1/3 as fraction", "${Engine.toFraction(1.0 / 3)}")
            }
        }
        item {
            SectionCard("Equation solver") {
                OutlinedTextField(quad, { quad = it }, label = { Text("Quadratic a,b,c") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                ResultLine("Roots", if (roots.isEmpty()) "no real roots" else roots.joinToString())
            }
        }
        item {
            SectionCard("Geometry") {
                ResultLine("Circle r=5 area", "%.2f".format(Geometry.circleArea(5.0)))
                ResultLine("Circle r=5 circumference", "%.2f".format(Geometry.circleCirc(5.0)))
                ResultLine("Sphere r=3 volume", "%.2f".format(Geometry.sphereVolume(3.0)))
                ResultLine("Cylinder r=2 h=5", "%.2f".format(Geometry.cylinderVolume(2.0, 5.0)))
            }
        }
        item {
            SectionCard("Health and everyday") {
                ResultLine("BMI 70kg/175cm", "%.1f".format(HealthDate.bmi(70.0, 175.0)))
                ResultLine("Ohm 12V/4Ω", "${HealthDate.ohm(12.0, null, 4.0).second} A")
                ResultLine("Currencies", "${Currency.codes.size} codes")
                ResultLine("100 USD in EUR", "%.2f".format(Currency.convert(100.0, 1.0, 0.92)))
            }
        }
    }
}
