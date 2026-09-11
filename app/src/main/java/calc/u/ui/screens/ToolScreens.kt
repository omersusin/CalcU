package calc.u.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import calc.u.core.Engine
import calc.u.core.Finance
import calc.u.core.Geometry
import calc.u.core.HealthDate
import calc.u.core.Units
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.sqrt

private fun fmt(v: Double, digits: Int = 4): String {
    if (!v.isFinite()) return "—"
    return "%.${digits}f".format(v)
}

private fun num(s: String): Double = s.toDoubleOrNull() ?: 0.0

private fun mapFor(cat: String): Map<String, Units.UnitDef> = when (cat) {
    "length" -> Units.length
    "mass" -> Units.mass
    "volume" -> Units.volume
    "area" -> Units.area
    "speed" -> Units.speed
    "pressure" -> Units.pressure
    "energy" -> Units.energy
    "power" -> Units.power
    "data" -> Units.data
    "fuel" -> Units.fuel
    else -> emptyMap()
}

// mpg and km/L are inverse units, so they cannot go through the linear toBase path.
private fun convertFuel(v: Double, from: String, to: String): Double {
    if (from == to) return v
    val l100km = when (from) {
        "mpg" -> if (v == 0.0) Double.NaN else 235.214 / v
        "km/L" -> if (v == 0.0) Double.NaN else 100.0 / v
        else -> v
    }
    return when (to) {
        "mpg" -> 235.214 / l100km
        "km/L" -> 100.0 / l100km
        else -> l100km
    }
}

private fun factorial(n: Long): Long? {
    if (n < 0 || n > 20) return null
    var r = 1L
    for (i in 2..n) r *= i
    return r
}

private fun parseList(s: String): List<Double> =
    s.split(",", ";", " ", "\n").mapNotNull { it.trim().toDoubleOrNull() }

private data class AmortRow(val n: Int, val interest: Double, val principal: Double, val balance: Double)

private fun amortPreview(principal: Double, annualRate: Double, months: Int): Pair<List<AmortRow>, Double> {
    val emi = Finance.emi(principal, annualRate, months)
    val r = annualRate / 1200
    var bal = principal
    val rows = mutableListOf<AmortRow>()
    var last = AmortRow(0, 0.0, 0.0, principal)
    for (i in 1..months) {
        val interest = bal * r
        val princ = (emi - interest).coerceAtLeast(0.0)
        bal = (bal - princ).coerceAtLeast(0.0)
        last = AmortRow(i, interest, princ, bal)
        if (i <= 3) rows.add(last)
    }
    if (months > 3) rows.add(last)
    return rows to (emi * months - principal)
}

private fun ageYMD(y: Int, m: Int, d: Int): Triple<Int, Int, Int>? {
    return try {
        val now = Calendar.getInstance()
        val birth = Calendar.getInstance()
        birth.isLenient = false
        birth.set(y, m - 1, d)
        birth.timeInMillis
        if (birth.after(now)) return null
        var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        var months = now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
        var days = now.get(Calendar.DAY_OF_MONTH) - birth.get(Calendar.DAY_OF_MONTH)
        if (days < 0) {
            months -= 1
            val prev = Calendar.getInstance()
            prev.timeInMillis = now.timeInMillis
            prev.add(Calendar.MONTH, -1)
            days += prev.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years -= 1
            months += 12
        }
        Triple(years, months, days)
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun NumField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    integer: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (integer) KeyboardType.Number else KeyboardType.Decimal
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertersScreen() {
    var input by remember { mutableStateOf("1") }
    var cat by remember { mutableStateOf("length") }
    var from by remember { mutableStateOf("m") }
    var to by remember { mutableStateOf("ft") }
    var feet by remember { mutableStateOf("5") }
    var inches by remember { mutableStateOf("9") }
    var baseInput by remember { mutableStateOf("42") }
    val cats = listOf(
        "length", "mass", "volume", "temp", "area", "speed",
        "pressure", "energy", "power", "data", "fuel"
    )
    val v = num(input)
    val units: List<String> = if (cat == "temp") Units.temperature else mapFor(cat).keys.toList()
    val safeFrom = if (from in units) from else units.firstOrNull() ?: ""
    val safeTo = if (to in units) to else units.getOrNull(1) ?: units.firstOrNull() ?: ""
    val result: String = runCatching {
        if (cat == "temp") fmt(Units.convertTemp(v, safeFrom, safeTo))
        else if (cat == "fuel") fmt(convertFuel(v, safeFrom, safeTo))
        else {
            val map = mapFor(cat)
            val f = map[safeFrom]
            val t = map[safeTo]
            if (f == null || t == null) "—" else fmt(Units.convert(v, f, t))
        }
    }.getOrDefault("—")
    val baseLong = baseInput.toLongOrNull()
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard("Value") {
                NumField(input, { input = it }, "Value to convert")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cats) { c ->
                        FilterChip(selected = c == cat, onClick = { cat = c }, label = { Text(c) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        UnitDropdown(safeFrom, units, { from = it }, "From")
                    }
                    Box(Modifier.weight(1f)) {
                        UnitDropdown(safeTo, units, { to = it }, "To")
                    }
                }
                HorizontalDivider()
                ResultLine("Result", "$result $safeTo")
            }
        }
        if (cat == "length") {
            item {
                SectionCard("Feet and inches") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { NumField(feet, { feet = it }, "Feet") }
                        Box(Modifier.weight(1f)) { NumField(inches, { inches = it }, "Inches") }
                    }
                    val totalCm = Units.ftInToCm(num(feet), num(inches))
                    val cmDef = Units.length["cm"]
                    HorizontalDivider()
                    ResultLine("Centimeters", fmt(totalCm, 2))
                    if (cmDef != null) {
                        Units.length.forEach { (name, def) ->
                            ResultLine(name, fmt(Units.convert(totalCm, cmDef, def), 4))
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Bases and Roman") {
                NumField(baseInput, { baseInput = it }, "Integer", integer = true)
                HorizontalDivider()
                ResultLine("Binary", if (baseLong == null) "—" else Units.fromBase(baseLong.toDouble(), 2))
                ResultLine("Octal", if (baseLong == null) "—" else Units.fromBase(baseLong.toDouble(), 8))
                ResultLine("Hex", if (baseLong == null) "—" else Units.fromBase(baseLong.toDouble(), 16))
                val roman = if (baseLong == null || baseLong < 1 || baseLong > 3999) "—"
                else runCatching { Units.toRoman(baseLong.toInt()) }.getOrDefault("—").ifEmpty { "—" }
                ResultLine("Roman", roman)
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
    var monthsF by remember { mutableFloatStateOf(24f) }
    var years by remember { mutableStateOf("5") }
    var price by remember { mutableStateOf("100") }
    var taxRate by remember { mutableStateOf("10") }
    var inclusive by remember { mutableStateOf(false) }
    var priceA by remember { mutableStateOf("3.99") }
    var qtyA by remember { mutableStateOf("500") }
    var priceB by remember { mutableStateOf("5.49") }
    var qtyB by remember { mutableStateOf("750") }
    val billV = num(bill)
    val splitV = split.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val (tipAmt, grand, per) = Finance.tip(billV, tipPct.toDouble(), splitV)
    val p = num(principal)
    val annual = num(rate)
    val months = monthsF.toInt().coerceIn(1, 360)
    val emi = Finance.emi(p, annual, months)
    val (sched, totalInt) = amortPreview(p, annual, months)
    val yrs = num(years)
    val (si, siTotal) = Finance.simple(p, annual, yrs)
    val ci = Finance.compound(p, annual, yrs)
    val priceV = num(price)
    val taxV = num(taxRate)
    val (taxTotal, taxAmt) = Finance.withTax(priceV, taxV, inclusive)
    val unitA = Finance.unitPrice(num(priceA), num(qtyA))
    val unitB = Finance.unitPrice(num(priceB), num(qtyB))
    val verdict = when {
        unitA == 0.0 && unitB == 0.0 -> "Enter quantities"
        unitA == 0.0 -> "B is the better buy"
        unitB == 0.0 -> "A is the better buy"
        unitA < unitB -> "A is the better buy"
        unitB < unitA -> "B is the better buy"
        else -> "Tie"
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard("Tip and split") {
                NumField(bill, { bill = it }, "Bill")
                Text("Tip: ${tipPct.toInt()}%", style = MaterialTheme.typography.labelLarge)
                Slider(value = tipPct, onValueChange = { tipPct = it }, valueRange = 0f..30f)
                NumField(split, { split = it }, "Split between", integer = true)
                HorizontalDivider()
                ResultLine("Tip", fmt(tipAmt, 2))
                ResultLine("Total", fmt(grand, 2))
                ResultLine("Per person", fmt(per, 2))
            }
        }
        item {
            SectionCard("Loan EMI") {
                NumField(principal, { principal = it }, "Principal")
                NumField(rate, { rate = it }, "Annual %")
                Text(
                    "Term: $months months (${fmt(months / 12.0, 1)} years)",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(value = monthsF, onValueChange = { monthsF = it }, valueRange = 6f..360f)
                HorizontalDivider()
                ResultLine("Monthly EMI", fmt(emi, 2))
                ResultLine("Total interest", fmt(totalInt, 2))
                sched.forEach { row ->
                    val tag = if (row.n == months && months > 3) "Month $months (last)" else "Month ${row.n}"
                    ResultLine(tag, "int ${fmt(row.interest, 2)} · bal ${fmt(row.balance, 2)}")
                }
            }
        }
        item {
            SectionCard("Interest over time") {
                NumField(years, { years = it }, "Years")
                HorizontalDivider()
                ResultLine("Simple interest", fmt(si, 2))
                ResultLine("Simple total", fmt(siTotal, 2))
                ResultLine("Compound total", fmt(ci, 2))
            }
        }
        item {
            SectionCard("Sales tax") {
                NumField(price, { price = it }, "Price")
                NumField(taxRate, { taxRate = it }, "Tax rate %")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = !inclusive,
                            onClick = { inclusive = false },
                            label = { Text("Price excludes tax") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = inclusive,
                            onClick = { inclusive = true },
                            label = { Text("Price includes tax") }
                        )
                    }
                }
                HorizontalDivider()
                ResultLine(if (inclusive) "Net" else "Total", fmt(taxTotal, 2))
                ResultLine("Tax", fmt(taxAmt, 2))
            }
        }
        item {
            SectionCard("Unit price compare") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(priceA, { priceA = it }, "Price A") }
                    Box(Modifier.weight(1f)) { NumField(qtyA, { qtyA = it }, "Qty A") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(priceB, { priceB = it }, "Price B") }
                    Box(Modifier.weight(1f)) { NumField(qtyB, { qtyB = it }, "Qty B") }
                }
                HorizontalDivider()
                ResultLine("Unit price A", fmt(unitA, 4))
                ResultLine("Unit price B", fmt(unitB, 4))
                ResultLine("Verdict", verdict)
            }
        }
    }
}

@Composable
fun MathScreen() {
    var tab by remember { mutableStateOf("numbers") }
    val tabs = listOf("numbers" to "Numbers", "geometry" to "Geometry", "health" to "Health")
    Column(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tabs) { (id, label) ->
                FilterChip(selected = tab == id, onClick = { tab = id }, label = { Text(label) })
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                "numbers" -> NumbersContent()
                "geometry" -> GeometryScreen()
                "health" -> HealthScreen()
            }
        }
    }
}

@Composable
private fun NumbersContent() {
    var a by remember { mutableStateOf("12") }
    var b by remember { mutableStateOf("18") }
    var pctX by remember { mutableStateOf("200") }
    var pctP by remember { mutableStateOf("15") }
    var discPrice by remember { mutableStateOf("80") }
    var discPct by remember { mutableStateOf("25") }
    var listInput by remember { mutableStateOf("4, 8, 15, 16, 23, 42") }
    var stat by remember { mutableStateOf("Mean") }
    var qa by remember { mutableStateOf("1") }
    var qb by remember { mutableStateOf("-3") }
    var qc by remember { mutableStateOf("2") }
    var a1 by remember { mutableStateOf("2") }
    var b1 by remember { mutableStateOf("3") }
    var c1 by remember { mutableStateOf("7") }
    var a2 by remember { mutableStateOf("1") }
    var b2 by remember { mutableStateOf("-1") }
    var c2 by remember { mutableStateOf("1") }
    var fracN by remember { mutableStateOf("24") }
    var fracD by remember { mutableStateOf("36") }
    val av = a.toLongOrNull() ?: 0L
    val bv = b.toLongOrNull() ?: 0L
    val values = parseList(listInput)
    val sorted = values.sorted()
    val statValue: String = when (stat) {
        "Median" -> if (sorted.isEmpty()) "—" else fmt(
            if (sorted.size % 2 == 1) sorted[sorted.size / 2]
            else (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2
        )
        "Min" -> sorted.firstOrNull()?.let { fmt(it) } ?: "—"
        "Max" -> sorted.lastOrNull()?.let { fmt(it) } ?: "—"
        "Sum" -> if (values.isEmpty()) "—" else fmt(values.sum())
        "Count" -> "${values.size}"
        else -> if (values.isEmpty()) "—" else fmt(values.sum() / values.size)
    }
    val qav = num(qa)
    val qbv = num(qb)
    val qcv = num(qc)
    val roots = Engine.solveQuadratic(qav, qbv, qcv)
    val m1 = num(a1)
    val n1 = num(b1)
    val o1 = num(c1)
    val m2 = num(a2)
    val n2 = num(b2)
    val o2 = num(c2)
    val det = m1 * n2 - m2 * n1
    val sys = if (det == 0.0) null else Pair((o1 * n2 - o2 * n1) / det, (m1 * o2 - m2 * o1) / det)
    val fn = fracN.toLongOrNull()
    val fd = fracD.toLongOrNull()
    val frac: String = if (fn == null || fd == null || fd == 0L) "—" else {
        val g = Engine.gcd(fn, fd)
        val rn = fn / g
        val rd = fd / g
        val sign = if (rd < 0) "-" else ""
        "$sign${kotlin.math.abs(rn)}/${kotlin.math.abs(rd)} = ${fmt(fn.toDouble() / fd.toDouble(), 6)}"
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard("Number theory") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(a, { a = it }, "a", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(b, { b = it }, "b", integer = true) }
                }
                HorizontalDivider()
                ResultLine("GCD", "${Engine.gcd(av, bv)}")
                ResultLine("LCM", "${runCatching { Engine.lcm(av, bv) }.getOrDefault(0)}")
                ResultLine("a is prime", if (Engine.isPrime(av)) "yes" else "no")
                ResultLine("b is prime", if (Engine.isPrime(bv)) "yes" else "no")
                ResultLine("nCr", "${runCatching { Engine.nCr(av, bv) }.getOrDefault(0)}")
                ResultLine("nPr", "${runCatching { Engine.nPr(av, bv) }.getOrDefault(0)}")
                ResultLine("a!", factorial(av)?.toString() ?: "too large")
            }
        }
        item {
            SectionCard("Percent and discount") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(pctP, { pctP = it }, "%") }
                    Box(Modifier.weight(1f)) { NumField(pctX, { pctX = it }, "of value") }
                }
                ResultLine("Result", fmt(num(pctX) * num(pctP) / 100))
                HorizontalDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(discPrice, { discPrice = it }, "Price") }
                    Box(Modifier.weight(1f)) { NumField(discPct, { discPct = it }, "Off %") }
                }
                val save = num(discPrice) * num(discPct) / 100
                ResultLine("You save", fmt(save, 2))
                ResultLine("Final price", fmt(num(discPrice) - save, 2))
            }
        }
        item {
            SectionCard("Statistics") {
                NumField(listInput, { listInput = it }, "Values, comma separated")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Mean", "Median", "Min", "Max", "Sum", "Count")) { s ->
                        FilterChip(selected = stat == s, onClick = { stat = s }, label = { Text(s) })
                    }
                }
                HorizontalDivider()
                ResultLine(stat, statValue)
            }
        }
        item {
            SectionCard("Quadratic solver") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(qa, { qa = it }, "a") }
                    Box(Modifier.weight(1f)) { NumField(qb, { qb = it }, "b") }
                    Box(Modifier.weight(1f)) { NumField(qc, { qc = it }, "c") }
                }
                HorizontalDivider()
                ResultLine("Roots", if (roots.isEmpty()) "no real roots" else roots.joinToString { fmt(it, 6) })
            }
        }
        item {
            SectionCard("2x2 system solver") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(a1, { a1 = it }, "a1") }
                    Box(Modifier.weight(1f)) { NumField(b1, { b1 = it }, "b1") }
                    Box(Modifier.weight(1f)) { NumField(c1, { c1 = it }, "c1") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(a2, { a2 = it }, "a2") }
                    Box(Modifier.weight(1f)) { NumField(b2, { b2 = it }, "b2") }
                    Box(Modifier.weight(1f)) { NumField(c2, { c2 = it }, "c2") }
                }
                HorizontalDivider()
                ResultLine(
                    "Solution",
                    if (sys == null) "no unique solution" else "x=${fmt(sys.first, 6)}, y=${fmt(sys.second, 6)}"
                )
            }
        }
        item {
            SectionCard("Fraction simplifier") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(fracN, { fracN = it }, "Numerator", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(fracD, { fracD = it }, "Denominator", integer = true) }
                }
                HorizontalDivider()
                ResultLine("Reduced", frac)
            }
        }
    }
}

@Composable
fun GeometryScreen() {
    var shape by remember { mutableStateOf("circle") }
    var f1 by remember { mutableStateOf("5") }
    var f2 by remember { mutableStateOf("4") }
    var f3 by remember { mutableStateOf("3") }
    val shapes = listOf(
        "circle", "rectangle", "triangle", "sphere", "cylinder",
        "cone", "cube", "prism", "pyramid", "ellipse"
    )
    val labels: List<String> = when (shape) {
        "circle", "sphere" -> listOf("Radius")
        "rectangle" -> listOf("Width", "Height")
        "triangle" -> listOf("Base", "Height")
        "cylinder", "cone" -> listOf("Radius", "Height")
        "cube" -> listOf("Side")
        "prism" -> listOf("Width", "Height", "Depth")
        "pyramid" -> listOf("Base side", "Height")
        "ellipse" -> listOf("Semi-axis a", "Semi-axis b")
        else -> emptyList()
    }
    val dims = listOf(f1, f2, f3)
    val setters: List<(String) -> Unit> = listOf({ f1 = it }, { f2 = it }, { f3 = it })
    val x = num(f1)
    val y = num(f2)
    val z = num(f3)
    val outputs: List<Pair<String, String>> = when (shape) {
        "circle" -> listOf(
            "Area" to fmt(Geometry.circleArea(x), 2),
            "Circumference" to fmt(Geometry.circleCirc(x), 2)
        )
        "rectangle" -> listOf(
            "Area" to fmt(Geometry.rectArea(x, y), 2),
            "Perimeter" to fmt(2 * (x + y), 2)
        )
        "triangle" -> listOf("Area" to fmt(Geometry.triangleArea(x, y), 2))
        "sphere" -> listOf(
            "Volume" to fmt(Geometry.sphereVolume(x), 2),
            "Surface" to fmt(Geometry.sphereArea(x), 2)
        )
        "cylinder" -> listOf(
            "Volume" to fmt(Geometry.cylinderVolume(x, y), 2),
            "Surface" to fmt(2 * PI * x * (x + y), 2)
        )
        "cone" -> listOf(
            "Volume" to fmt(Geometry.coneVolume(x, y), 2),
            "Surface" to fmt(PI * x * (x + sqrt(x * x + y * y)), 2)
        )
        "cube" -> listOf(
            "Volume" to fmt(Geometry.cubeVolume(x), 2),
            "Surface" to fmt(6 * x * x, 2)
        )
        "prism" -> listOf(
            "Volume" to fmt(Geometry.prismVolume(x, y, z), 2),
            "Surface" to fmt(2 * (x * y + x * z + y * z), 2)
        )
        "pyramid" -> listOf(
            "Volume" to fmt(Geometry.pyramidVolume(x, y), 2),
            "Surface" to fmt(x * x + 2 * x * sqrt((x / 2) * (x / 2) + y * y), 2)
        )
        "ellipse" -> listOf("Area" to fmt(Geometry.ellipseArea(x, y), 2))
        else -> emptyList()
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard("Shape") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(shapes) { s ->
                        FilterChip(selected = shape == s, onClick = { shape = s }, label = { Text(s) })
                    }
                }
            }
        }
        item {
            SectionCard("Dimensions") {
                labels.forEachIndexed { i, label ->
                    NumField(dims[i], setters[i], label)
                }
                HorizontalDivider()
                outputs.forEach { (label, value) -> ResultLine(label, value) }
            }
        }
    }
}

@Composable
fun HealthScreen() {
    var weight by remember { mutableStateOf("70") }
    var height by remember { mutableStateOf("175") }
    var male by remember { mutableStateOf(true) }
    var waist by remember { mutableStateOf("85") }
    var neck by remember { mutableStateOf("38") }
    var hips by remember { mutableStateOf("95") }
    var tdeeMale by remember { mutableStateOf(true) }
    var age by remember { mutableStateOf("30") }
    var activity by remember { mutableStateOf(1.55) }
    var by by remember { mutableStateOf("1990") }
    var bm by remember { mutableStateOf("6") }
    var bd by remember { mutableStateOf("15") }
    val w = num(weight)
    val h = num(height)
    val bmi = HealthDate.bmi(w, h)
    val bmiCat = when {
        !bmi.isFinite() || (w == 0.0 && h == 0.0) -> "—"
        bmi < 18.5 -> "Underweight"
        bmi < 25 -> "Normal"
        bmi < 30 -> "Overweight"
        else -> "Obese"
    }
    val fat = HealthDate.bodyFatNavy(num(waist), num(neck), h, num(hips), male)
    val ageInt = age.toIntOrNull() ?: 0
    val tdee = HealthDate.tdee(w, h, ageInt, tdeeMale, activity)
    val ageRes = ageYMD(by.toIntOrNull() ?: 0, bm.toIntOrNull() ?: 0, bd.toIntOrNull() ?: 0)
    val activities = listOf(
        1.2 to "Sedentary",
        1.375 to "Light",
        1.55 to "Moderate",
        1.725 to "Active",
        1.9 to "Athlete"
    )
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard("BMI") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(weight, { weight = it }, "Weight kg") }
                    Box(Modifier.weight(1f)) { NumField(height, { height = it }, "Height cm") }
                }
                HorizontalDivider()
                ResultLine("BMI", fmt(bmi, 1))
                ResultLine("Category", bmiCat)
            }
        }
        item {
            SectionCard("Body fat (Navy)") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = male, onClick = { male = true }, label = { Text("Male") })
                    }
                    item {
                        FilterChip(selected = !male, onClick = { male = false }, label = { Text("Female") })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(waist, { waist = it }, "Waist cm") }
                    Box(Modifier.weight(1f)) { NumField(neck, { neck = it }, "Neck cm") }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.weight(1f)) { NumField(height, { height = it }, "Height cm") }
                    Box(Modifier.weight(1f)) {
                        if (!male) NumField(hips, { hips = it }, "Hips cm")
                    }
                }
                HorizontalDivider()
                ResultLine("Body fat", if (fat.isFinite()) fmt(fat, 1) + " %" else "—")
            }
        }
        item {
            SectionCard("TDEE") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(weight, { weight = it }, "Weight kg") }
                    Box(Modifier.weight(1f)) { NumField(height, { height = it }, "Height cm") }
                    Box(Modifier.weight(1f)) { NumField(age, { age = it }, "Age", integer = true) }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = tdeeMale, onClick = { tdeeMale = true }, label = { Text("Male") })
                    }
                    item {
                        FilterChip(selected = !tdeeMale, onClick = { tdeeMale = false }, label = { Text("Female") })
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activities) { (factor, label) ->
                        FilterChip(
                            selected = activity == factor,
                            onClick = { activity = factor },
                            label = { Text(label) }
                        )
                    }
                }
                HorizontalDivider()
                ResultLine("Daily calories", fmt(tdee, 0) + " kcal")
            }
        }
        item {
            SectionCard("Age calculator") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(by, { by = it }, "Year", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(bm, { bm = it }, "Month", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(bd, { bd = it }, "Day", integer = true) }
                }
                HorizontalDivider()
                ResultLine(
                    "Age",
                    if (ageRes == null) "invalid date"
                    else "${ageRes.first}y ${ageRes.second}m ${ageRes.third}d"
                )
            }
        }
    }
}
