package calc.u.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import calc.u.core.ClockKit
import calc.u.core.ColorKit
import calc.u.core.Constants
import calc.u.core.Currency
import calc.u.core.Engine
import calc.u.core.Finance
import calc.u.core.Geometry
import calc.u.core.HealthDate
import calc.u.core.Matrix
import calc.u.core.ScreenKit
import calc.u.core.TripKit
import calc.u.core.Units
import calc.u.data.CurrencyRepository
import calc.u.data.UnitPrefsRepository
import com.microsoft.fluentui.tokenized.bottomsheet.BottomSheet
import com.microsoft.fluentui.tokenized.bottomsheet.BottomSheetValue
import com.microsoft.fluentui.tokenized.bottomsheet.rememberBottomSheetState
import calc.u.ui.CalcUNumberBox
import calc.u.ui.FluentStagger
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import calc.u.ui.theme.FluentMotion
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.sqrt
import kotlinx.coroutines.launch

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
    "cooking" -> Units.cooking
    "shoe" -> Units.shoe
    "ring" -> Units.ring
    "historic" -> Units.historic
    "angle" -> Units.angle
    "force" -> Units.force
    "torque" -> Units.torque
    "acceleration" -> Units.acceleration
    "flow" -> Units.flow
    "datarate" -> Units.datarate
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
    CalcUNumberBox(
        value = value,
        onValueChange = onChange,
        label = label,
        integer = integer,
        modifier = modifier
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

@Composable
private fun CurrencyCard() {
    val appCtx = LocalContext.current.applicationContext
    val repo = remember { CurrencyRepository(appCtx) }
    val rates by repo.rates.collectAsState(initial = Currency.fallbackUsdRates)
    val stale by repo.isStale.collectAsState(initial = true)
    val source by repo.source.collectAsState()
    var amount by remember { mutableStateOf("100") }
    var from by remember { mutableStateOf("USD") }
    var to by remember { mutableStateOf("EUR") }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { repo.refresh() }
    val options = remember(rates) { (Currency.codes + rates.keys).distinct().sorted() }
    val safeFrom = if (from in options) from else "USD"
    val safeTo = if (to in options) to else "EUR"
    val fromRate = rates[safeFrom] ?: Currency.fallbackUsdRates[safeFrom] ?: 0.0
    val toRate = rates[safeTo] ?: Currency.fallbackUsdRates[safeTo] ?: 0.0
    val result = Currency.convert(num(amount), fromRate, toRate)
    SectionCard("Currency") {
        NumField(amount, { amount = it }, "Amount")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) { UnitDropdown(safeFrom, options, { from = it }, "From") }
            Box(Modifier.weight(1f)) { UnitDropdown(safeTo, options, { to = it }, "To") }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (stale && source.label == "live") "live · stale" else source.label,
                style = MaterialTheme.typography.labelLarge
            )
            Button(onClick = { scope.launch { repo.refresh() } }) { Text("Refresh") }
        }
        HorizontalDivider()
        ResultLine("Result", "${fmt(result, 2)} $safeTo")
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
    var cookCups by remember { mutableStateOf("1") }
    var gramsPerCup by remember { mutableStateOf("128") }
    val cats = listOf(
        "length", "mass", "volume", "temp", "area", "speed",
        "pressure", "energy", "power", "data", "fuel",
        "cooking", "shoe", "ring", "historic",
        "angle", "force", "torque", "acceleration", "flow", "datarate"
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
    val appCtx = LocalContext.current.applicationContext
    val prefs = remember { UnitPrefsRepository(appCtx) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(BottomSheetValue.Hidden)
    var sheetTarget by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var swapped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (swapped) 180f else 0f, label = "swap")
    val favorites by prefs.favoritesFlow(cat).collectAsState(initial = emptySet())
    LaunchedEffect(cat) {
        val (savedFrom, savedTo) = prefs.getPair(cat)
        if (savedFrom != null && savedFrom in units) from = savedFrom
        if (savedTo != null && savedTo in units) to = savedTo
    }
    LaunchedEffect(cat, from, to) {
        if (from in units && to in units) prefs.savePair(cat, from, to)
    }
    fun previewFor(candidate: String): String = runCatching {
        if (cat == "temp") {
            if (sheetTarget == "from") fmt(Units.convertTemp(v, candidate, safeTo))
            else fmt(Units.convertTemp(v, safeFrom, candidate))
        } else if (cat == "fuel") {
            if (sheetTarget == "from") fmt(convertFuel(v, candidate, safeTo))
            else fmt(convertFuel(v, safeFrom, candidate))
        } else {
            val map = mapFor(cat)
            val anchor = map[if (sheetTarget == "from") safeTo else safeFrom]
            val cand = map[candidate]
            if (anchor == null || cand == null) "—"
            else if (sheetTarget == "from") fmt(Units.convert(v, cand, anchor))
            else fmt(Units.convert(v, anchor, cand))
        }
    }.getOrDefault("—")
    fun openPicker(target: String) {
        sheetTarget = target
        query = ""
        scope.launch { sheetState.show() }
    }
    fun pick(unit: String) {
        if (sheetTarget == "from") from = unit else to = unit
        scope.launch { sheetState.hide() }
        sheetTarget = null
        query = ""
    }
    val visible = units
        .filter { query.isBlank() || it.contains(query, ignoreCase = true) }
        .sortedWith(compareBy({ it !in favorites }, { it }))
    BottomSheet(
        sheetContent = {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search units") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(Modifier.fillMaxWidth().height(360.dp)) {
                    items(visible) { u ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { pick(u) }.padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(u, style = MaterialTheme.typography.bodyLarge)
                                Text(previewFor(u), style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { scope.launch { prefs.toggleFavorite(cat, u) } }) {
                                Icon(
                                    if (u in favorites) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = if (u in favorites) "Unfavorite $u" else "Favorite $u"
                                )
                            }
                        }
                    }
                }
            }
        },
        sheetState = sheetState,
        expandable = true,
        peekHeight = 420.dp,
        scrimVisible = true,
        enableSwipeDismiss = true,
        onDismiss = { sheetTarget = null; query = "" }
    ) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f).clickable { openPicker("from") }) {
                        OutlinedTextField(
                            value = safeFrom,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("From") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    IconButton(onClick = {
                        val f = from
                        from = to
                        to = f
                        swapped = !swapped
                    }) {
                        Icon(
                            Icons.Filled.SwapVert,
                            contentDescription = "Swap units",
                            modifier = Modifier.graphicsLayer { rotationZ = rotation }
                        )
                    }
                    Box(Modifier.weight(1f).clickable { openPicker("to") }) {
                        OutlinedTextField(
                            value = safeTo,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("To") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                HorizontalDivider()
                FluentStagger(0) {
                    Column {
                        ResultLine("Result", "$result $safeTo")
                    }
                }
            }
        }
        item {
            CurrencyCard()
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
                    FluentStagger(1) {
                        Column {
                            ResultLine("Centimeters", fmt(totalCm, 2))
                            if (cmDef != null) {
                                Units.length.forEach { (name, def) ->
                                    ResultLine(name, fmt(Units.convert(totalCm, cmDef, def), 4))
                                }
                            }
                        }
                    }
                }
            }
        }
        if (cat == "cooking") {
            item {
                SectionCard("Cups to grams") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { NumField(cookCups, { cookCups = it }, "Cups") }
                        Box(Modifier.weight(1f)) { NumField(gramsPerCup, { gramsPerCup = it }, "Grams per cup") }
                    }
                    val volMl = runCatching {
                        Units.convert(num(cookCups), Units.cooking["cup"]!!, Units.cooking["ml"]!!)
                    }.getOrDefault(Double.NaN)
                    val weight = runCatching {
                        Units.convertCookingToWeight(volMl, num(gramsPerCup))
                    }.getOrDefault(Double.NaN)
                    HorizontalDivider()
                    FluentStagger(2) {
                        Column {
                            ResultLine("Volume", "${fmt(volMl, 2)} mL")
                            ResultLine("Weight", "${fmt(weight, 2)} g")
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Bases and Roman") {
                NumField(baseInput, { baseInput = it }, "Integer", integer = true)
                HorizontalDivider()
                FluentStagger(3) {
                    Column {
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
        item {
            var hex by remember { mutableStateOf("#FF0000") }
            var rs by remember { mutableStateOf("255") }
            var gs by remember { mutableStateOf("0") }
            var bs by remember { mutableStateOf("0") }
            val rgbFromHex = runCatching { ColorKit.hexToRgb(hex) }.getOrNull()
            val ri = rs.toIntOrNull()
            val gi = gs.toIntOrNull()
            val bi = bs.toIntOrNull()
            val hexFromRgb = if (ri != null && gi != null && bi != null) runCatching { ColorKit.rgbToHex(ri, gi, bi) }.getOrNull() else null
            val hsl = if (ri != null && gi != null && bi != null) runCatching { ColorKit.rgbToHsl(ri, gi, bi) }.getOrNull() else null
            val swatch = if (ri != null && gi != null && bi != null && ri in 0..255 && gi in 0..255 && bi in 0..255) Color(ri, gi, bi) else Color.Gray
            SectionCard("Color") {
                OutlinedTextField(value = hex, onValueChange = { hex = it }, label = { Text("Hex") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(rs, { rs = it }, "R", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(gs, { gs = it }, "G", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(bs, { bs = it }, "B", integer = true) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        Button(onClick = {
                            val t = runCatching { ColorKit.hexToRgb(hex) }.getOrNull()
                            if (t != null) { rs = "${t.first}"; gs = "${t.second}"; bs = "${t.third}" }
                        }) { Text("Hex→RGB") }
                    }
                    Box(Modifier.weight(1f)) {
                        Button(onClick = {
                            val t = if (ri != null && gi != null && bi != null) runCatching { ColorKit.rgbToHex(ri, gi, bi) }.getOrNull() else null
                            if (t != null) hex = t
                        }) { Text("RGB→Hex") }
                    }
                }
                HorizontalDivider()
                FluentStagger(4) {
                    Column {
                        ResultLine("Hex→RGB", rgbFromHex?.let { "${it.first}, ${it.second}, ${it.third}" } ?: "—")
                        ResultLine("RGB→Hex", hexFromRgb ?: "—")
                        ResultLine("HSL", hsl?.let { "${fmt(it.first, 1)}°, ${fmt(it.second * 100, 1)}%, ${fmt(it.third * 100, 1)}%" } ?: "—")
                    }
                }
                Box(Modifier.fillMaxWidth().height(48.dp).background(swatch))
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
        item {
            var sipM by remember { mutableStateOf("5000") }
            var sipR by remember { mutableStateOf("12") }
            var sipY by remember { mutableStateOf("10") }
            val res = runCatching { Finance.sip(num(sipM), num(sipR), num(sipY)) }.getOrNull()
            SectionCard("SIP") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(sipM, { sipM = it }, "Monthly") }
                    Box(Modifier.weight(1f)) { NumField(sipR, { sipR = it }, "Annual %") }
                    Box(Modifier.weight(1f)) { NumField(sipY, { sipY = it }, "Years") }
                }
                HorizontalDivider()
                ResultLine("Invested", fmt(res?.first ?: Double.NaN, 2))
                ResultLine("Gain", fmt(res?.second ?: Double.NaN, 2))
                ResultLine("Total", fmt(res?.third ?: Double.NaN, 2))
                val sipInvested = res?.first ?: 0.0
                val sipGain = res?.second ?: 0.0
                val sipTotal = res?.third ?: 0.0
                val sipPrimary = MaterialTheme.colorScheme.primary
                val sipTertiary = MaterialTheme.colorScheme.tertiary
                val surfaceTrack = MaterialTheme.colorScheme.surfaceContainerHighest
                val sipSweep by animateFloatAsState(
                    if (sipTotal > 0) 1f else 0f,
                    animationSpec = tween(FluentMotion.Medium, easing = FluentMotion.Standard),
                    label = "sip-sweep"
                )
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(120.dp)) {
                        val invFrac = if (sipTotal <= 0) 0f else (sipInvested / sipTotal).toFloat().coerceIn(0f, 1f)
                        drawArc(
                            color = surfaceTrack,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                        )
                        if (sipSweep > 0f) {
                            drawArc(
                                color = sipPrimary,
                                startAngle = -90f,
                                sweepAngle = 360f * invFrac * sipSweep,
                                useCenter = false,
                                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = sipTertiary,
                                startAngle = -90f + 360f * invFrac * sipSweep,
                                sweepAngle = 360f * (1f - invFrac) * sipSweep,
                                useCenter = false,
                                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }
        }
        item {
            var cagrI by remember { mutableStateOf("10000") }
            var cagrF by remember { mutableStateOf("20000") }
            var cagrY by remember { mutableStateOf("10") }
            val r = runCatching { Finance.cagr(num(cagrI), num(cagrF), num(cagrY)) }.getOrNull()
            SectionCard("CAGR") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(cagrI, { cagrI = it }, "Initial") }
                    Box(Modifier.weight(1f)) { NumField(cagrF, { cagrF = it }, "Final") }
                    Box(Modifier.weight(1f)) { NumField(cagrY, { cagrY = it }, "Years") }
                }
                HorizontalDivider()
                ResultLine("CAGR %", if (r == null) "—" else fmt(r * 100, 2))
            }
        }
        item {
            var fdP by remember { mutableStateOf("10000") }
            var fdR by remember { mutableStateOf("6") }
            var fdY by remember { mutableStateOf("5") }
            val res = runCatching { Finance.fd(num(fdP), num(fdR), num(fdY), 4) }.getOrNull()
            SectionCard("Fixed deposit") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(fdP, { fdP = it }, "Principal") }
                    Box(Modifier.weight(1f)) { NumField(fdR, { fdR = it }, "Rate %") }
                    Box(Modifier.weight(1f)) { NumField(fdY, { fdY = it }, "Years") }
                }
                HorizontalDivider()
                ResultLine("Interest", fmt(res?.second ?: Double.NaN, 2))
                ResultLine("Total", fmt(res?.third ?: Double.NaN, 2))
            }
        }
        item {
            var vatA by remember { mutableStateOf("100") }
            var vatP by remember { mutableStateOf("18") }
            var vatIncl by remember { mutableStateOf(false) }
            val res = runCatching { Finance.vat(num(vatA), num(vatP), vatIncl) }.getOrNull()
            SectionCard("VAT") {
                NumField(vatA, { vatA = it }, "Amount")
                NumField(vatP, { vatP = it }, "VAT %")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = !vatIncl,
                            onClick = { vatIncl = false },
                            label = { Text("Excl. VAT") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = vatIncl,
                            onClick = { vatIncl = true },
                            label = { Text("Incl. VAT") }
                        )
                    }
                }
                HorizontalDivider()
                ResultLine("Net", fmt(res?.first ?: Double.NaN, 2))
                ResultLine("Tax", fmt(res?.second ?: Double.NaN, 2))
                ResultLine("Gross", fmt(res?.third ?: Double.NaN, 2))
            }
        }
        item {
            var dist by remember { mutableStateOf("500") }
            var cons by remember { mutableStateOf("7.5") }
            var fuelPrice by remember { mutableStateOf("1.8") }
            var avg by remember { mutableStateOf("90") }
            val cost = runCatching { TripKit.fuelCost(num(dist), num(cons), num(fuelPrice)) }.getOrNull()
            val time = runCatching { TripKit.tripTime(num(dist), num(avg)) }.getOrNull()
            SectionCard("Trip cost") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(dist, { dist = it }, "Km") }
                    Box(Modifier.weight(1f)) { NumField(cons, { cons = it }, "L/100km") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(fuelPrice, { fuelPrice = it }, "Price/L") }
                    Box(Modifier.weight(1f)) { NumField(avg, { avg = it }, "Avg km/h") }
                }
                HorizontalDivider()
                ResultLine("Fuel cost", cost?.let { fmt(it, 2) } ?: "—")
                ResultLine("Drive time h", time?.let { fmt(it, 2) } ?: "—")
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
                ResultLine("Roots", if (roots.isEmpty()) "—" else roots.joinToString())
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
        item {
            ProgrammerScreen()
        }
        item {
            var a11 by remember { mutableStateOf("1") }
            var a12 by remember { mutableStateOf("2") }
            var a21 by remember { mutableStateOf("3") }
            var a22 by remember { mutableStateOf("4") }
            val m = runCatching { Matrix.of2x2(num(a11), num(a12), num(a21), num(a22)) }.getOrNull()
            SectionCard("Matrix 2x2") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(a11, { a11 = it }, "a11") }
                    Box(Modifier.weight(1f)) { NumField(a12, { a12 = it }, "a12") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(a21, { a21 = it }, "a21") }
                    Box(Modifier.weight(1f)) { NumField(a22, { a22 = it }, "a22") }
                }
                HorizontalDivider()
                ResultLine("det", m?.let { runCatching { fmt(it.determinant()) }.getOrDefault("—") } ?: "—")
                ResultLine(
                    "transpose",
                    m?.transpose()?.let { t -> "${fmt(t[0, 0])}, ${fmt(t[0, 1])} / ${fmt(t[1, 0])}, ${fmt(t[1, 1])}" } ?: "—"
                )
                ResultLine(
                    "inverse",
                    m?.let { runCatching { it.inverse().pretty().replace("\n", " ") }.getOrDefault("singular") } ?: "—"
                )
            }
        }
        item {
            var ca by remember { mutableStateOf("1") }
            var cb by remember { mutableStateOf("-6") }
            var cc by remember { mutableStateOf("11") }
            var cd by remember { mutableStateOf("-6") }
            val roots = runCatching { Engine.solveCubic(num(ca), num(cb), num(cc), num(cd)) }.getOrDefault(emptyList())
            SectionCard("Cubic solver") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(ca, { ca = it }, "a") }
                    Box(Modifier.weight(1f)) { NumField(cb, { cb = it }, "b") }
                    Box(Modifier.weight(1f)) { NumField(cc, { cc = it }, "c") }
                    Box(Modifier.weight(1f)) { NumField(cd, { cd = it }, "d") }
                }
                HorizontalDivider()
                ResultLine("Roots", if (roots.isEmpty()) "—" else roots.joinToString())
            }
        }
        item {
            var statsIn by remember { mutableStateOf("1, 2, 3, 4, 5") }
            val vals = parseList(statsIn)
            SectionCard("Distribution stats") {
                NumField(statsIn, { statsIn = it }, "Values, comma separated")
                HorizontalDivider()
                ResultLine("Median", vals.let { runCatching { fmt(Engine.statsMedian(it)) }.getOrDefault("—") })
                ResultLine("Mode", vals.let { runCatching { fmt(Engine.statsMode(it)) }.getOrDefault("—") })
                ResultLine("Variance", vals.let { runCatching { fmt(Engine.statsVariance(it)) }.getOrDefault("—") })
                ResultLine("Stdev", vals.let { runCatching { fmt(Engine.statsStdev(it)) }.getOrDefault("—") })
            }
        }
        item {
            var cq by remember { mutableStateOf("") }
            val clipboard = LocalClipboardManager.current
            val hits = remember(cq) { Constants.search(cq).take(30) }
            SectionCard("Constants") {
                OutlinedTextField(
                    value = cq,
                    onValueChange = { cq = it },
                    label = { Text("Search constants") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                hits.forEach { c ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            clipboard.setText(AnnotatedString(c.value.toString()))
                        }.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${c.symbol} · ${c.name}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(fmt(c.value, 6), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (hits.isEmpty()) ResultLine("No match", "—")
            }
        }
        item {
            var sw by remember { mutableStateOf("1920") }
            var sh by remember { mutableStateOf("1080") }
            var diag by remember { mutableStateOf("6.1") }
            val aspect = runCatching { ScreenKit.aspectRatio(sw.toIntOrNull() ?: 0, sh.toIntOrNull() ?: 0) }.getOrNull()
            val ppiV = runCatching { ScreenKit.ppi(sw.toIntOrNull() ?: 0, sh.toIntOrNull() ?: 0, num(diag)) }.getOrNull()
            SectionCard("Screen") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(sw, { sw = it }, "W px", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(sh, { sh = it }, "H px", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(diag, { diag = it }, "Inch") }
                }
                HorizontalDivider()
                ResultLine("Aspect", aspect ?: "—")
                ResultLine("PPI", ppiV?.let { fmt(it, 1) } ?: "—")
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
        item {
            var sa by remember { mutableStateOf("3") }
            var sb by remember { mutableStateOf("4") }
            var sc by remember { mutableStateOf("5") }
            val tri = runCatching { Geometry.solveTriangleSSS(num(sa), num(sb), num(sc)) }.getOrNull()
            SectionCard("Triangle SSS") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(sa, { sa = it }, "a") }
                    Box(Modifier.weight(1f)) { NumField(sb, { sb = it }, "b") }
                    Box(Modifier.weight(1f)) { NumField(sc, { sc = it }, "c") }
                }
                HorizontalDivider()
                ResultLine("Angle A", tri?.get("angleA")?.let { fmt(it, 2) + "°" } ?: "—")
                ResultLine("Angle B", tri?.get("angleB")?.let { fmt(it, 2) + "°" } ?: "—")
                ResultLine("Angle C", tri?.get("angleC")?.let { fmt(it, 2) + "°" } ?: "—")
                ResultLine("Perimeter", tri?.get("perimeter")?.let { fmt(it, 2) } ?: "—")
                ResultLine("Area", tri?.get("area")?.let { fmt(it, 2) } ?: "—")
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
        item {
            var cy by remember { mutableStateOf("2026") }
            var cm by remember { mutableStateOf("9") }
            var cd by remember { mutableStateOf("11") }
            var zone by remember { mutableStateOf("UTC") }
            val weekday = runCatching { ClockKit.weekdayName(cy.toIntOrNull() ?: 0, cm.toIntOrNull() ?: 0, cd.toIntOrNull() ?: 0) }.getOrNull()
            val until = runCatching { ClockKit.daysUntil(cy.toIntOrNull() ?: 0, cm.toIntOrNull() ?: 0, cd.toIntOrNull() ?: 0) }.getOrNull()
            SectionCard("Date and world clock") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(cy, { cy = it }, "Year", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(cm, { cm = it }, "Month", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(cd, { cd = it }, "Day", integer = true) }
                }
                OutlinedTextField(value = zone, onValueChange = { zone = it }, label = { Text("Zone ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                HorizontalDivider()
                ResultLine("Weekday", weekday ?: "—")
                ResultLine("Days until", until?.toString() ?: "—")
                ResultLine(zone, ClockKit.worldTime(zone))
            }
        }
    }
}

@Composable
fun StepsScreen() {
    var tab by remember { mutableStateOf("quad") }
    var qa by remember { mutableStateOf("1") }
    var qb by remember { mutableStateOf("-3") }
    var qc by remember { mutableStateOf("2") }
    var ep by remember { mutableStateOf("10000") }
    var er by remember { mutableStateOf("5") }
    var en by remember { mutableStateOf("24") }
    var g1 by remember { mutableStateOf("48") }
    var g2 by remember { mutableStateOf("18") }
    var cv by remember { mutableStateOf("1") }
    var cf by remember { mutableStateOf("km") }
    var ct by remember { mutableStateOf("m") }
    val tabs = listOf("quad" to "Quadratic", "emi" to "EMI", "gcd" to "GCD", "units" to "Units")
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
                "emi" -> {
                    val p = ep.toDoubleOrNull()
                    val annual = er.toDoubleOrNull()
                    val months = en.toIntOrNull()
                    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            SectionCard("Inputs") {
                                NumField(ep, { ep = it }, "Principal")
                                NumField(er, { er = it }, "Annual %")
                                NumField(en, { en = it }, "Months", integer = true)
                            }
                        }
                        item {
                            SectionCard("Steps") {
                                if (p == null || annual == null || months == null || months <= 0 || p <= 0) {
                                    Text(
                                        "Enter a principal above 0, a valid rate, and whole months above 0.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    val r = annual / 1200
                                    val emi = Finance.emi(p, annual, months)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("1. Monthly rate r = $annual / 12 / 100 = ${fmt(r, 6)}", style = MaterialTheme.typography.bodyMedium)
                                        if (r == 0.0) {
                                            Text("2. No interest, so EMI = P / n = ${fmt(p, 2)} / $months", style = MaterialTheme.typography.bodyMedium)
                                        } else {
                                            val f = Math.pow(1 + r, months.toDouble())
                                            Text("2. Growth factor (1 + r)^n = (1 + ${fmt(r, 6)})^$months = ${fmt(f, 6)}", style = MaterialTheme.typography.bodyMedium)
                                            Text("3. EMI = P·r·(1+r)^n / ((1+r)^n − 1) = ${fmt(p, 2)}·${fmt(r, 6)}·${fmt(f, 6)} / ${fmt(f - 1, 6)}", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Text("4. Pay ${fmt(emi, 2)} each month for $months months", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    HorizontalDivider()
                                    ResultLine("Monthly EMI", fmt(emi, 2))
                                    ResultLine("Total interest", fmt(emi * months - p, 2))
                                }
                            }
                        }
                    }
                }
                "gcd" -> {
                    val a = g1.toLongOrNull()
                    val b = g2.toLongOrNull()
                    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            SectionCard("Inputs") {
                                NumField(g1, { g1 = it }, "a", integer = true)
                                NumField(g2, { g2 = it }, "b", integer = true)
                            }
                        }
                        item {
                            SectionCard("Steps") {
                                if (a == null || b == null) {
                                    Text(
                                        "Enter two whole numbers.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (a == 0L && b == 0L) {
                                    Text(
                                        "GCD(0, 0) is undefined. Enter at least one non-zero value.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    var x = Math.abs(a)
                                    var y = Math.abs(b)
                                    val lines = mutableListOf<String>()
                                    var i = 1
                                    while (y != 0L) {
                                        lines.add("$i. $x = $y × ${x / y} + ${x % y}")
                                        val t = x % y
                                        x = y
                                        y = t
                                        i++
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                                        Text("$i. Remainder is 0, so the last non-zero remainder is the GCD", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    HorizontalDivider()
                                    ResultLine("GCD", "$x")
                                }
                            }
                        }
                    }
                }
                "units" -> {
                    val v = cv.toDoubleOrNull()
                    val f = Units.length[cf.trim()]
                    val t = Units.length[ct.trim()]
                    val names = Units.length.keys.sorted().joinToString(", ")
                    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            SectionCard("Inputs") {
                                NumField(cv, { cv = it }, "Value")
                                NumField(cf, { cf = it }, "From unit")
                                NumField(ct, { ct = it }, "To unit")
                                Text(
                                    "Available: $names",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        item {
                            SectionCard("Steps") {
                                if (v == null) {
                                    Text(
                                        "Enter a valid number to convert.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (f == null || t == null) {
                                    Text(
                                        "Unknown unit. Use one of: $names.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    val base = v * f.toBase / 1.0
                                    val out = Units.convert(v, f, t)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("1. Factors to metres: 1 ${f.id} = ${fmt(f.toBase, 6)} m, 1 ${t.id} = ${fmt(t.toBase, 6)} m", style = MaterialTheme.typography.bodyMedium)
                                        Text("2. To base: $v × ${fmt(f.toBase, 6)} = ${fmt(base, 6)} m", style = MaterialTheme.typography.bodyMedium)
                                        Text("3. To target: ${fmt(base, 6)} ÷ ${fmt(t.toBase, 6)} = ${fmt(out, 6)} ${t.id}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    HorizontalDivider()
                                    ResultLine("Result", "${fmt(out)} ${t.id}")
                                }
                            }
                        }
                    }
                }
                else -> {
                    val a = qa.toDoubleOrNull()
                    val b = qb.toDoubleOrNull()
                    val c = qc.toDoubleOrNull()
                    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            SectionCard("Inputs") {
                                NumField(qa, { qa = it }, "a")
                                NumField(qb, { qb = it }, "b")
                                NumField(qc, { qc = it }, "c")
                            }
                        }
                        item {
                            SectionCard("Steps") {
                                if (a == null || b == null || c == null) {
                                    Text(
                                        "Enter valid numbers for a, b, and c.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (a == 0.0) {
                                    Text(
                                        "Coefficient a must not be zero for a quadratic. Got a = 0.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    val d = b * b - 4 * a * c
                                    val nature = when {
                                        d > 0 -> "D > 0, so two distinct real roots"
                                        d == 0.0 -> "D = 0, so one repeated real root"
                                        else -> "D < 0, so a pair of complex roots"
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("1. Equation: ${fmt(a)}x² + ${fmt(b)}x + ${fmt(c)} = 0", style = MaterialTheme.typography.bodyMedium)
                                        Text("2. D = b² − 4ac = (${fmt(b)})² − 4·(${fmt(a)})·(${fmt(c)}) = ${fmt(d)}", style = MaterialTheme.typography.bodyMedium)
                                        Text("3. $nature", style = MaterialTheme.typography.bodyMedium)
                                        if (d >= 0) {
                                            val s = sqrt(d)
                                            Text("4. x = (−b ± √D) / 2a = (${fmt(-b)} ± ${fmt(s)}) / ${fmt(2 * a)}", style = MaterialTheme.typography.bodyMedium)
                                        } else {
                                            Text("4. x = (−b ± i√|D|) / 2a = (${fmt(-b)} ± ${fmt(sqrt(-d))}i) / ${fmt(2 * a)}", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                    HorizontalDivider()
                                    ResultLine("Roots", Engine.solveQuadratic(a, b, c).joinToString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgrammerScreen() {
    var aStr by remember { mutableStateOf("12") }
    var bStr by remember { mutableStateOf("5") }
    var base by remember { mutableStateOf(10) }
    var minStr by remember { mutableStateOf("1") }
    var maxStr by remember { mutableStateOf("100") }
    var rolls by remember { mutableStateOf(listOf<Int>()) }
    fun parse(s: String): Long? {
        val t = s.trim()
        if (t.isEmpty()) return null
        val neg = t.startsWith("-")
        var body = if (neg || t.startsWith("+")) t.drop(1) else t
        if (base == 16 && body.startsWith("0x", ignoreCase = true)) body = body.drop(2)
        if (base == 2 && body.startsWith("0b", ignoreCase = true)) body = body.drop(2)
        if (base == 8 && body.startsWith("0o", ignoreCase = true)) body = body.drop(2)
        if (body.isEmpty()) return null
        val v = body.toLongOrNull(base) ?: return null
        return if (neg) -v else v
    }
    fun show(v: Long): String = when (base) {
        16 -> v.toString(16).uppercase()
        8 -> v.toString(8)
        2 -> v.toString(2)
        else -> v.toString()
    }
    val av = parse(aStr)
    val bv = parse(bStr)
    val bases = listOf(10 to "dec", 16 to "hex", 8 to "oct", 2 to "bin")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard("Programmer & random") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bases) { (b, label) ->
                    FilterChip(selected = base == b, onClick = { base = b }, label = { Text(label) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(value = aStr, onValueChange = { aStr = it }, label = { Text("a") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(value = bStr, onValueChange = { bStr = it }, label = { Text("b (shift)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
            HorizontalDivider()
            if (av == null || bv == null) {
                ResultLine("Result", "—")
            } else {
                ResultLine("AND", show(Engine.bitwiseAnd(av, bv)))
                ResultLine("OR", show(Engine.bitwiseOr(av, bv)))
                ResultLine("XOR", show(Engine.bitwiseXor(av, bv)))
                ResultLine("NOT a", show(Engine.bitwiseNot(av)))
                ResultLine("a shl b", show(Engine.shl(av, bv.toInt())))
                ResultLine("a shr b", show(Engine.shr(av, bv.toInt())))
            }
            HorizontalDivider()
            ResultLine("a dec", av?.toString() ?: "—")
            ResultLine("a hex", av?.toString(16)?.uppercase() ?: "—")
            ResultLine("a oct", av?.toString(8) ?: "—")
            ResultLine("a bin", av?.toString(2) ?: "—")
        }
        SectionCard("RNG") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) { NumField(minStr, { minStr = it }, "Min", integer = true) }
                Box(Modifier.weight(1f)) { NumField(maxStr, { maxStr = it }, "Max", integer = true) }
            }
            Button(onClick = {
                val lo = minStr.toIntOrNull() ?: 0
                val hi = maxStr.toIntOrNull() ?: 0
                rolls = (listOf(Engine.randomInt(lo, hi)) + rolls).take(5)
            }) { Text("Generate") }
            HorizontalDivider()
            if (rolls.isEmpty()) {
                ResultLine("Last roll", "—")
            } else {
                rolls.forEachIndexed { i, r -> ResultLine(if (i == 0) "Last roll" else "Roll ${i + 1}", "$r") }
            }
        }
    }
}
