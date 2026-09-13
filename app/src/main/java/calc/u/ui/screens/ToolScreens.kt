package calc.u.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import calc.u.core.ClockAngle
import calc.u.core.ClockKit
import calc.u.core.ColorKit
import calc.u.core.Constants
import calc.u.core.Currency
import calc.u.core.Engine
import calc.u.core.Finance
import calc.u.core.Geometry
import calc.u.core.HealthDate
import calc.u.core.HealthPlus
import calc.u.core.Matrix
import calc.u.core.NumberTheory
import calc.u.core.ScreenKit
import calc.u.core.TripKit
import calc.u.core.UnitExpr
import calc.u.core.Units
import calc.u.core.VectorKit
import calc.u.data.CurrencyRepository
import calc.u.data.UnitPrefsRepository
import calc.u.ui.BottomBackChevron
import calc.u.ui.CalcUNumberBox
import calc.u.ui.FluentStagger
import calc.u.ui.JumpToCalcFab
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import calc.u.ui.theme.FluentMotion
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.sqrt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.converterRowsStore by preferencesDataStore("calcu-converter-rows")

private fun rowsKey(cat: String) = stringPreferencesKey("rows_${cat}_order")

private fun fmt(v: Double, digits: Int = 4): String {
    if (!v.isFinite()) return "—"
    return try {
        "%.${digits.coerceIn(0, 10)}f".format(v)
    } catch (_: Exception) {
        "—"
    }
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
    "viscosity" -> Units.viscosity
    "radiation" -> Units.radiation
    "illuminance" -> Units.illuminance
    "magnetic" -> Units.magnetic
    "density" -> Units.density
    "specificenergy" -> Units.specificenergy
    "glucose" -> Units.glucose
    "pace" -> Units.pace
    "luminance" -> Units.luminance
    "magflux" -> Units.magflux
    "luminous" -> Units.luminous
    "ev" -> Units.ev
    "scheduling" -> Units.scheduling
    "conductance" -> Units.conductance
    "gasflow" -> Units.gasflow
    "rvalue" -> Units.rvalue
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
        "mpg" -> if (!l100km.isFinite() || l100km == 0.0) Double.NaN else 235.214 / l100km
        "km/L" -> if (!l100km.isFinite() || l100km == 0.0) Double.NaN else 100.0 / l100km
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
private fun PieChart(
    parts: List<Pair<String, Double>>,
    centerLabel: String,
    centerValue: String
) {
    val safe = parts.map { it.first to (it.second.takeIf { v -> v.isFinite() }?.coerceAtLeast(0.0) ?: 0.0) }
    val total = safe.sumOf { it.second }.takeIf { it > 0 } ?: 1.0
    val firstFrac = ((safe.getOrNull(0)?.second ?: 0.0) / total).toFloat().coerceIn(0f, 1f)
    val hasData = safe.any { it.second > 0 }
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val sweep by animateFloatAsState(
        if (hasData) 1f else 0f,
        animationSpec = tween(FluentMotion.Medium, easing = FluentMotion.Standard),
        label = "pie-sweep"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(132.dp)) {
                val sw = 18.dp.toPx()
                val inset = sw / 2f + 1.dp.toPx()
                val arcTopLeft = Offset(inset, inset)
                val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                drawArc(
                    color = track,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
                if (sweep > 0f) {
                    drawArc(
                        color = primary,
                        startAngle = -90f,
                        sweepAngle = 360f * firstFrac * sweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = sw, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = tertiary,
                        startAngle = -90f + 360f * firstFrac * sweep,
                        sweepAngle = 360f * (1f - firstFrac) * sweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = sw, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(centerLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(
                    centerValue,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        safe.forEach { (label, v) ->
            ResultLine(label, runCatching { fmt(v, 2) }.getOrDefault("—"))
        }
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
    val pad = calc.u.ui.rememberNumPadState()
    Box(modifier.clickable { pad.open(value, onChange) }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
    if (pad.show) {
        calc.u.ui.NumPadSheet(pad, label)
    }
}

@Composable
private fun ToolResultRow(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp)
                .background(tint.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ToolErrorLine(msg: String?) {
    if (msg != null) {
        Text(
            msg,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun HelperCaption(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MethodDropdown(
    selected: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val current = options.firstOrNull { it.first == selected } ?: options.firstOrNull()
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = current?.first ?: selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            supportingText = { current?.second?.let { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (name, formula) ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(name)
                            Text(
                                formula,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = { onSelect(name); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun PrimePill(label: String, isPrime: Boolean) {
    val bg = if (isPrime) Color(0xFF2E7D32) else Color(0xFFC62828)
    Box(
        modifier = Modifier.background(bg, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$label: " + if (isPrime) "Yes ✓" else "No ✕",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
    }
}

@Composable
private fun BmiBar(bmi: Double) {
    val segments = listOf(
        Color(0xFF4CAF50),
        Color(0xFF9CCC65),
        Color(0xFFFFC107),
        Color(0xFFFF9800),
        Color(0xFFF44336)
    )
    val frac = if (!bmi.isFinite()) -1f else ((bmi - 14.0) / (36.0 - 14.0)).toFloat().coerceIn(0f, 1f)
    val marker = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
        val gap = 4.dp.toPx()
        val segW = (size.width - gap * (segments.size - 1)) / segments.size
        segments.forEachIndexed { i, c ->
            drawRoundRect(
                color = c,
                topLeft = Offset(x = i * (segW + gap), y = 0f),
                size = Size(width = segW, height = size.height),
                cornerRadius = CornerRadius(x = 6.dp.toPx(), y = 6.dp.toPx())
            )
        }
        if (frac >= 0f) {
            val markerW = 3.dp.toPx()
            val x = (frac * size.width).coerceIn(0f, size.width)
            drawRoundRect(
                color = marker,
                topLeft = Offset(
                    x = (x - markerW / 2).coerceIn(0f, (size.width - markerW).coerceAtLeast(0f)),
                    y = 0f
                ),
                size = Size(width = markerW, height = size.height),
                cornerRadius = CornerRadius(x = markerW / 2, y = markerW / 2)
            )
        }
    }
}

private fun bmiPlainLabel(category: String, bmi: Double): String = when {
    !bmi.isFinite() -> "Enter your weight and height to see your BMI."
    category == "Underweight" -> "Below the healthy range — consider checking with your doctor."
    category == "Normal" -> "In the healthy range — nice work."
    category == "Overweight" -> "A little above the healthy range — small steps help."
    category == "Obese" -> "Well above the healthy range — consider checking with your doctor."
    else -> "Enter your weight and height to see your BMI."
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
    LaunchedEffect(Unit) { runCatching { repo.refresh() }.onFailure { } }
    val options = remember(rates) { (Currency.codes + rates.keys).distinct().sorted() }
    val safeFrom = if (from in options) from else "USD"
    val safeTo = if (to in options) to else "EUR"
    val fromRate = rates[safeFrom] ?: Currency.fallbackUsdRates[safeFrom] ?: 0.0
    val toRate = rates[safeTo] ?: Currency.fallbackUsdRates[safeTo] ?: 0.0
    val convRes = runCatching { Currency.convert(num(amount), fromRate, toRate) }
    val result = convRes.getOrDefault(Double.NaN)
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
            Button(onClick = { scope.launch { runCatching { repo.refresh() } } }) { Text("Refresh") }
        }
        HorizontalDivider()
        Column(
            Modifier.fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                safeTo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                fmt(result, 2),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            ToolErrorLine(convRes.exceptionOrNull()?.message)
        }
    }
}

@Composable
private fun UnitExprCard() {
    var expr by remember { mutableStateOf("ft*lbf") }
    var target by remember { mutableStateOf("J") }
    val targets = listOf("N", "J", "W", "Pa", "m/s", "km/h", "N*m", "kWh", "psi", "gal")
    val safeTarget = if (target in targets) target else "J"
    val out = runCatching { UnitExpr.convertExpr(1.0, expr, safeTarget) }.getOrNull()
    SectionCard("Unit expression") {
        OutlinedTextField(
            value = expr,
            onValueChange = { expr = it },
            label = { Text("Expression, e.g. ft*lbf") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        UnitDropdown(safeTarget, targets, { target = it }, "Target unit")
        HorizontalDivider()
        AnimatedContent(
            targetState = out?.let { fmt(it, 6) } ?: "error",
            transitionSpec = {
                (fadeIn(tween(250)) + slideInVertically(tween(250) { it / 4 })) togetherWith
                    (fadeOut(tween(250)) + slideOutVertically(tween(250) { -it / 4 }))
            },
            label = "expr-output"
        ) { state ->
            if (state == "error") {
                Text(
                    "Incompatible or unknown units",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                ResultLine("1 ($expr) in $safeTarget", state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConvertersScreen() {
    var input by remember { mutableStateOf("1") }
    var rowOverrides by remember { mutableStateOf(mapOf<String, String>()) }
    var lastCleared by remember { mutableStateOf<String?>(null) }
    var undoVisible by remember { mutableStateOf(false) }
    var cat by remember { mutableStateOf("length") }
    var from by remember { mutableStateOf("m") }
    var to by remember { mutableStateOf("ft") }
    var toRows by remember { mutableStateOf(listOf<String>()) }
    var feet by remember { mutableStateOf("5") }
    var inches by remember { mutableStateOf("9") }
    var baseInput by remember { mutableStateOf("42") }
    var cookCups by remember { mutableStateOf("1") }
    var gramsPerCup by remember { mutableStateOf("128") }
    val cats = listOf(
        "length", "mass", "volume", "temp", "area", "speed",
        "pressure", "energy", "power", "data", "fuel",
        "cooking", "shoe", "ring", "historic",
        "angle", "force", "torque", "acceleration", "flow", "datarate",
        "viscosity", "radiation", "illuminance", "magnetic", "density", "specificenergy",
        "glucose", "pace", "luminance", "magflux", "luminous", "ev",
        "scheduling", "conductance", "gasflow", "rvalue"
    )
    val v = num(input)
    val units: List<String> = if (cat == "temp") Units.temperature else mapFor(cat).keys.toList()
    val safeFrom = if (from in units) from else units.firstOrNull() ?: ""
    val legacyTo = if (to in units) to else units.getOrNull(1) ?: units.firstOrNull() ?: ""
    val effectiveRows: List<String> = run {
        val clean = toRows.filter { it in units }.distinct()
        if (clean.isNotEmpty()) clean
        else {
            val extras = units.filter { it != safeFrom && it != legacyTo }.take(2)
            (listOf(legacyTo) + extras).filter { it.isNotBlank() }.distinct()
        }
    }
    val safeTo = effectiveRows.firstOrNull() ?: legacyTo
    fun convertOrNull(amount: Double, f: String, t: String): Double? = runCatching {
        if (f.isBlank() || t.isBlank()) null
        else if (f == t) amount
        else if (cat == "temp") Units.convertTemp(amount, f, t)
        else if (cat == "fuel") convertFuel(amount, f, t)
        else {
            val map = mapFor(cat)
            val ff = map[f] ?: return@runCatching null
            val tt = map[t] ?: return@runCatching null
            Units.convert(amount, ff, tt)
        }?.takeIf { it.isFinite() }
    }.getOrNull()?.takeIf { it?.isFinite() == true }
    fun factorFor(target: String): String = runCatching {
        convertOrNull(1.0, safeFrom, target)?.let { fmt(it) } ?: "—"
    }.getOrDefault("—")
    fun rowText(unit: String): String {
        rowOverrides[unit]?.let { return it }
        return convertOrNull(v, safeFrom, unit)?.let { fmt(it) } ?: ""
    }
    fun reverseFromRow(newText: String, rowUnit: String) {
        rowOverrides = mapOf(rowUnit to newText)
        val parsed = newText.toDoubleOrNull() ?: return
        convertOrNull(parsed, rowUnit, safeFrom)?.let { back ->
            if (back.isFinite()) input = fmt(back)
        }
    }
    // Reorder helper modeled on ToolsHub move()/hubOrder: index-based splice,
    // re-implemented here for converter rows (independent implementation).
    fun moveRow(unit: String, delta: Int) {
        val idx = effectiveRows.indexOf(unit)
        if (idx < 0) return
        val target = idx + delta
        if (target !in effectiveRows.indices) return
        val next = effectiveRows.toMutableList()
        next.add(target, next.removeAt(idx))
        toRows = next
        rowOverrides = emptyMap()
        if (idx == 0 || target == 0) to = next.firstOrNull() ?: to
    }
    val baseLong = baseInput.toLongOrNull()
    val appCtx = LocalContext.current.applicationContext
    val prefs = remember { UnitPrefsRepository(appCtx) }
    val scope = rememberCoroutineScope()
    var pickerOpen by remember { mutableStateOf(false) }
    var sheetTarget by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var swapped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        if (swapped) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "swap"
    )
    val snackbar = remember { SnackbarHostState() }
    val converterListState = rememberLazyListState()
    val favorites by prefs.favoritesFlow(cat).collectAsState(initial = emptySet())
    val hidden by prefs.hiddenFlow(cat).collectAsState(initial = emptySet())
    LaunchedEffect(cat) {
        rowOverrides = emptyMap()
        runCatching {
            val (savedFrom, savedTo) = prefs.getPair(cat)
            if (savedFrom != null && savedFrom in units) from = savedFrom
            if (savedTo != null && savedTo in units) to = savedTo
        }
        runCatching {
            val saved = appCtx.converterRowsStore.data.map { it[rowsKey(cat)] }.first()
            val restored = saved?.split(",")?.map { it.trim() }?.filter { it in units }?.distinct().orEmpty()
            if (restored.isNotEmpty()) {
                toRows = restored
                if (restored.firstOrNull() in units) to = restored.firstOrNull() ?: to
            }
        }
    }
    LaunchedEffect(safeTo) {
        if (safeTo.isNotBlank() && safeTo != to && safeTo in units) to = safeTo
    }
    LaunchedEffect(cat, from, to) {
        runCatching {
            if (from in units && to in units) prefs.savePair(cat, from, to)
        }
    }
    LaunchedEffect(cat, toRows) {
        runCatching {
            val clean = toRows.filter { it in units }.distinct()
            if (clean.isNotEmpty()) {
                appCtx.converterRowsStore.edit { it[rowsKey(cat)] = clean.joinToString(",") }
            }
        }
    }
    if (undoVisible) {
        LaunchedEffect(lastCleared) {
            delay(5000)
            undoVisible = false
        }
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
        pickerOpen = true
    }
    fun pick(unit: String) {
        val t = sheetTarget
        when {
            t == "from" -> from = unit
            t == "to" -> {
                to = unit
                toRows = (listOf(unit) + effectiveRows.filter { it != unit })
                    .take(maxOf(1, effectiveRows.size))
            }
            t != null && t.startsWith("row:") -> {
                val old = t.removePrefix("row:")
                toRows = if (unit == old) effectiveRows
                else if (unit in effectiveRows) effectiveRows.filter { it != old }
                else effectiveRows.map { if (it == old) unit else it }
                if (effectiveRows.firstOrNull() == old) to = unit
            }
            t == "add" -> {
                if (unit !in effectiveRows) toRows = effectiveRows + unit
            }
            else -> to = unit
        }
        rowOverrides = emptyMap()
        pickerOpen = false
        sheetTarget = null
        query = ""
    }
    val filtered = units.filter { query.isBlank() || it.contains(query, ignoreCase = true) }
    val visible = filtered.filterNot { it in hidden }
        .sortedWith(compareBy({ it !in favorites }, { it })) +
        filtered.filter { it in hidden }.sorted()
    fun unitRegion(name: String): String {
        if (name == "JP" || name.startsWith("JP_")) return "Japan"
        if (name == "EU" || name.startsWith("EU_")) return "EU"
        val lower = name.lowercase()
        if (name == "UK" || name.startsWith("UK_") || lower.endsWith("_uk") ||
            lower.startsWith("imp") || name == "longton" || name == "stone"
        ) return "UK"
        if (name == "US" || name.startsWith("US") || lower.endsWith("_us") ||
            lower.contains("survey") || lower == "league" || lower == "cable"
        ) return "US"
        return "Standard"
    }
    val regionOrder = listOf("Standard", "UK", "US", "Japan", "EU")
    val groupedVisible: List<Pair<String, List<String>>> = run {
        val byRegion = visible.groupBy { unitRegion(it) }
        regionOrder.filter { it in byRegion }.map { it to (byRegion[it] ?: emptyList()) }
    }
    if (pickerOpen) {
        ModalBottomSheet(
            onDismissRequest = { pickerOpen = false; query = "" },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search units") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(Modifier.fillMaxWidth().height(360.dp)) {
                    groupedVisible.forEach { (region, regionUnits) ->
                        stickyHeader(key = "region-$region") {
                            Text(
                                region,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                            )
                        }
                        items(regionUnits, key = { "$region-$it" }) { u ->
                        val isHidden = u in hidden
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { pick(u) }
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    u,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isHidden) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    previewFor(u).let { pv -> if (pv == "—") pv else "$pv $u" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isHidden) 0.5f else 1f)
                                )
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        runCatching { prefs.setHidden(cat, if (u in hidden) hidden - u else hidden + u) }
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    if (isHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (isHidden) "Unhide $u" else "Hide $u"
                                )
                            }
                            IconButton(
                                onClick = { scope.launch { runCatching { prefs.toggleFavorite(cat, u) } } },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    if (u in favorites) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = if (u in favorites) "Unfavorite $u" else "Favorite $u"
                                )
                            }
                        }
                        }
                    }
                }
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = converterListState,
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            UnitExprCard()
        }
        item {
            SectionCard("Value") {
                NumField(input, { input = it; rowOverrides = emptyMap() }, "Value in ${safeFrom.ifBlank { "source" }}")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            if (input.isNotEmpty() || rowOverrides.isNotEmpty()) {
                                val saved = input
                                val savedOverrides = rowOverrides
                                lastCleared = saved
                                input = ""
                                rowOverrides = emptyMap()
                                undoVisible = true
                                scope.launch {
                                    val res = snackbar.showSnackbar(
                                        message = "Cleared",
                                        actionLabel = "Undo",
                                        withDismissAction = true
                                    )
                                    if (res == SnackbarResult.ActionPerformed) {
                                        input = saved
                                        rowOverrides = savedOverrides
                                        lastCleared = null
                                        undoVisible = false
                                    }
                                }
                            }
                        },
                        enabled = input.isNotEmpty() || rowOverrides.isNotEmpty()
                    ) { Text("Clear") }
                    if (undoVisible && lastCleared != null) {
                        TextButton(onClick = {
                            input = lastCleared ?: ""
                            rowOverrides = emptyMap()
                            lastCleared = null
                            undoVisible = false
                        }) { Text("Undo") }
                    }
                }
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
                        AnimatedContent(
                            targetState = safeFrom,
                            transitionSpec = {
                                (fadeIn(tween(250)) + slideInVertically(tween(250) { it / 4 })) togetherWith
                                    (fadeOut(tween(250)) + slideOutVertically(tween(250) { -it / 4 }))
                            },
                            label = "from-label"
                        ) { target ->
                            OutlinedTextField(
                                value = target,
                                onValueChange = {},
                                enabled = false,
                                label = { Text("From") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    FilledTonalIconButton(onClick = {
                        val f = from
                        val first = effectiveRows.firstOrNull() ?: safeTo
                        val carried = convertOrNull(v, safeFrom, first)?.let { fmt(it) }
                        from = first
                        to = f
                        toRows = if (effectiveRows.isEmpty()) listOf(f)
                        else effectiveRows.toMutableList().also { it[0] = f }
                        if (carried != null && carried != "—" && carried.isNotBlank()) input = carried
                        rowOverrides = emptyMap()
                        swapped = !swapped
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Filled.SwapVert,
                            contentDescription = "Swap units",
                            modifier = Modifier.graphicsLayer { rotationZ = rotation }
                        )
                    }
                }
                HorizontalDivider()
                Crossfade(
                    targetState = effectiveRows.isEmpty(),
                    animationSpec = tween(250),
                    label = "rows-empty"
                ) { isEmpty ->
                    if (isEmpty) {
                        Text(
                            "No units — add one below.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FluentStagger(0) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                effectiveRows.forEachIndexed { index, u ->
                                    val output = rowText(u)
                                    val factor = factorFor(u)
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.pointerInput(u) {
                                            var acc = 0f
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { acc = 0f },
                                                onDragEnd = { acc = 0f },
                                                onDragCancel = { acc = 0f },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    acc += dragAmount.y
                                                    if (acc > 96f) {
                                                        moveRow(u, 1)
                                                        acc = 0f
                                                    } else if (acc < -96f) {
                                                        moveRow(u, -1)
                                                        acc = 0f
                                                    }
                                                }
                                            )
                                        }
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.DragHandle,
                                                contentDescription = "Reorder $u",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Box(Modifier.weight(1f).clickable { openPicker("row:$u") }) {
                                                AnimatedContent(
                                                    targetState = u,
                                                    transitionSpec = {
                                                        (fadeIn(tween(250)) + slideInVertically(tween(250) { it / 4 })) togetherWith
                                                            (fadeOut(tween(250)) + slideOutVertically(tween(250) { -it / 4 }))
                                                    },
                                                    label = "row-unit"
                                                ) { target ->
                                                    OutlinedTextField(
                                                        value = target,
                                                        onValueChange = {},
                                                        enabled = false,
                                                        label = { Text("To unit") },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                            Box(Modifier.weight(1f)) {
                                                AnimatedContent(
                                                    targetState = output,
                                                    transitionSpec = {
                                                        (fadeIn(tween(250)) + slideInVertically(tween(250) { it / 4 })) togetherWith
                                                            (fadeOut(tween(250)) + slideOutVertically(tween(250) { -it / 4 }))
                                                    },
                                                    label = "row-output"
                                                ) { target ->
                                                    NumField(target, { reverseFromRow(it, u) }, "Value in $u")
                                                }
                                            }
                                            IconButton(
                                                onClick = { moveRow(u, -1) },
                                                enabled = index > 0,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.KeyboardArrowUp,
                                                    contentDescription = "Move $u up"
                                                )
                                            }
                                            IconButton(
                                                onClick = { moveRow(u, 1) },
                                                enabled = index < effectiveRows.size - 1,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.KeyboardArrowDown,
                                                    contentDescription = "Move $u down"
                                                )
                                            }
                                            TextButton(
                                                onClick = {
                                                    toRows = effectiveRows.filter { it != u }
                                                    if (effectiveRows.firstOrNull() == u) {
                                                        to = effectiveRows.getOrNull(1) ?: safeFrom
                                                    }
                                                    rowOverrides = emptyMap()
                                                },
                                                enabled = effectiveRows.size > 1
                                            ) { Text("X") }
                                        }
                                        AnimatedContent(
                                            targetState = factor,
                                            transitionSpec = {
                                                fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                                            },
                                            label = "row-factor"
                                        ) { target ->
                                            Text(
                                                "1 ${safeFrom.ifBlank { "source" }} = $target $u",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                TextButton(onClick = { openPicker("add") }) { Text("+ Add unit") }
                Text(
                    "Long-press drag or ↑ ↓ to reorder • X to remove a row • + to add more units.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Editing any value recomputes all others via ${safeFrom.ifBlank { "source" }}.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                    val totalCm = runCatching { Units.ftInToCm(num(feet), num(inches)) }.getOrDefault(Double.NaN)
                    val cmDef = Units.length["cm"]
                    HorizontalDivider()
                    FluentStagger(1) {
                        AnimatedContent(
                            targetState = fmt(totalCm, 2),
                            transitionSpec = {
                                (fadeIn(tween(250)) + slideInVertically(tween(250) { it / 4 })) togetherWith
                                    (fadeOut(tween(250)) + slideOutVertically(tween(250) { -it / 4 }))
                            },
                            label = "ft-output"
                        ) { cmState ->
                            Column {
                                ResultLine("Centimeters", cmState)
                                if (cmDef != null) {
                                    Units.length.forEach { (name, def) ->
                                        ResultLine(name, runCatching { fmt(Units.convert(totalCm, cmDef, def), 4) }.getOrDefault("—"))
                                    }
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
                        val cup = Units.cooking["cup"] ?: throw IllegalStateException("Unknown unit")
                        val ml = Units.cooking["ml"] ?: throw IllegalStateException("Unknown unit")
                        Units.convert(num(cookCups), cup, ml)
                    }.getOrDefault(Double.NaN)
                    val weight = runCatching {
                        Units.convertCookingToWeight(volMl, num(gramsPerCup))
                    }.getOrDefault(Double.NaN)
                    val volFrac = runCatching {
                        Engine.toFraction(volMl)?.let { "${it.first}/${it.second}" }
                    }.getOrNull()
                    val wtFrac = runCatching {
                        Engine.toFraction(weight)?.let { "${it.first}/${it.second}" }
                    }.getOrNull()
                    HorizontalDivider()
                    FluentStagger(2) {
                        AnimatedContent(
                            targetState = "${fmt(volMl, 2)}|${fmt(weight, 2)}",
                            transitionSpec = {
                                (fadeIn(tween(250)) + slideInVertically(tween(250) { it / 4 })) togetherWith
                                    (fadeOut(tween(250)) + slideOutVertically(tween(250) { -it / 4 }))
                            },
                            label = "cups-output"
                        ) { target ->
                            key(target) {
                            Column {
                                ResultLine("Volume", "${fmt(volMl, 2)} mL" + (if (volFrac != null) " ($volFrac)" else ""))
                                ResultLine("Weight", "${fmt(weight, 2)} g" + (if (wtFrac != null) " ($wtFrac)" else ""))
                            }
                            }
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
                    AnimatedContent(
                        targetState = "$baseInput",
                        transitionSpec = {
                            (fadeIn(tween(250)) + slideInVertically(tween(250) { it / 4 })) togetherWith
                                (fadeOut(tween(250)) + slideOutVertically(tween(250) { -it / 4 }))
                        },
                        label = "base-output"
                    ) { target ->
                        key(target) {
                        Column {
                            ResultLine("Binary", if (baseLong == null) "—" else runCatching { Units.fromBase(baseLong.toDouble(), 2) }.getOrDefault("—"))
                            ResultLine("Octal", if (baseLong == null) "—" else runCatching { Units.fromBase(baseLong.toDouble(), 8) }.getOrDefault("—"))
                            ResultLine("Hex", if (baseLong == null) "—" else runCatching { Units.fromBase(baseLong.toDouble(), 16) }.getOrDefault("—"))
                            val roman = if (baseLong == null || baseLong < 1 || baseLong > 3999) "—"
                            else runCatching { Units.toRoman(baseLong.toInt()) }.getOrDefault("—").ifEmpty { "—" }
                            ResultLine("Roman", roman)
                            Text(
                                "I=1 V=5 X=10 L=50 C=100 D=500 M=1000",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        }
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
                    AnimatedContent(
                        targetState = "${rgbFromHex?.let { "${it.first},${it.second},${it.third}" } ?: "—"}|${hexFromRgb ?: "—"}",
                        transitionSpec = {
                            (fadeIn(tween(250)) + slideInVertically(tween(250) { it / 4 })) togetherWith
                                (fadeOut(tween(250)) + slideOutVertically(tween(250) { -it / 4 }))
                        },
                        label = "color-output"
                    ) { target ->
                        key(target) {
                        Column {
                            ResultLine("Hex→RGB", rgbFromHex?.let { "${it.first}, ${it.second}, ${it.third}" } ?: "—")
                            ResultLine("RGB→Hex", hexFromRgb ?: "—")
                            ResultLine("HSL", hsl?.let { "${fmt(it.first, 1)}°, ${fmt(it.second * 100, 1)}%, ${fmt(it.third * 100, 1)}%" } ?: "—")
                        }
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(48.dp).background(swatch))
            }
        }
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

@Composable
fun FinanceScreen(onNavigate: (String) -> Unit = {}) {
    var bill by remember { mutableStateOf("100") }
    var tipPct by remember { mutableFloatStateOf(15f) }
    var tipPctText by remember { mutableStateOf("15") }
    var tipRound by remember { mutableStateOf("None") }
    var split by remember { mutableStateOf("2") }
    var principal by remember { mutableStateOf("10000") }
    var rate by remember { mutableStateOf("5") }
    var monthsF by remember { mutableFloatStateOf(24f) }
    var monthsText by remember { mutableStateOf("24") }
    var years by remember { mutableStateOf("5") }
    var price by remember { mutableStateOf("100") }
    var taxRate by remember { mutableStateOf("10") }
    var inclusive by remember { mutableStateOf(false) }
    var priceA by remember { mutableStateOf("3.99") }
    var qtyA by remember { mutableStateOf("500") }
    var unitAName by remember { mutableStateOf("g") }
    var priceB by remember { mutableStateOf("5.49") }
    var qtyB by remember { mutableStateOf("750") }
    var unitBName by remember { mutableStateOf("g") }
    val billV = num(bill)
    val splitParsed = split.toIntOrNull()
    val splitV = splitParsed?.coerceAtLeast(1) ?: 1
    val tipPctEff = tipPctText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: tipPct.toDouble()
    val tipBase = runCatching { Finance.tip(billV, tipPctEff, splitV) }.getOrNull()
    val tipRounded: Triple<Double, Double, Double>? = runCatching {
        val base = tipBase ?: throw IllegalStateException("bad tip")
        when (tipRound) {
            "Up" -> {
                val roundedTotal = kotlin.math.ceil(base.second)
                Triple(roundedTotal - billV, roundedTotal, roundedTotal / splitV)
            }
            "Nearest" -> {
                val roundedTotal = kotlin.math.round(base.second)
                Triple(roundedTotal - billV, roundedTotal, roundedTotal / splitV)
            }
            else -> base
        }
    }.getOrNull()
    val (tipAmt, grand, per) = tipRounded ?: Triple(Double.NaN, Double.NaN, Double.NaN)
    val tipBillErr = if (bill.trim().toDoubleOrNull() == null || billV <= 0) "Enter a bill amount greater than 0." else null
    val tipSplitErr = if (splitParsed == null || (splitParsed ?: 0) < 1) "People must be at least 1." else null
    val p = num(principal)
    val annual = num(rate)
    val monthsParsed = monthsText.toIntOrNull()
    val months = (monthsParsed ?: monthsF.toInt()).coerceIn(1, 360)
    val emiRes = runCatching { Finance.emi(p, annual, months) }
    val emi = emiRes.getOrDefault(Double.NaN)
    val emiPrincipalErr = if (p <= 0) "Principal must be greater than 0." else null
    val emiMonthsErr = if (monthsParsed != null && (monthsParsed < 1 || monthsParsed > 360)) "Months must be 1..360." else null
    val (schedPreview, totalIntPreview) = runCatching { amortPreview(p, annual, months) }.getOrDefault(emptyList<AmortRow>() to Double.NaN)
    val fullSched: List<AmortRow> = runCatching {
        if (p <= 0 || months < 1) throw IllegalStateException("invalid loan")
        val payment = Finance.emi(p, annual, months)
        val r = annual / 1200
        var bal = p
        buildList {
            for (i in 1..months) {
                val interest = if (r == 0.0) 0.0 else bal * r
                var princ = (payment - interest).coerceAtLeast(0.0)
                if (i == months) princ = bal.coerceAtLeast(0.0)
                bal = (bal - princ).coerceAtLeast(0.0)
                add(AmortRow(i, interest, princ, bal))
            }
        }
    }.getOrDefault(emptyList())
    val loanTotalPaid = if (emi.isFinite() && months >= 1 && p > 0) emi * months else Double.NaN
    val loanTotalInt = if (loanTotalPaid.isFinite() && p > 0) loanTotalPaid - p else Double.NaN
    val yrs = num(years)
    val (si, siTotal) = runCatching { Finance.simple(p, annual, yrs) }.getOrDefault(0.0 to Double.NaN)
    val ci = runCatching { Finance.compound(p, annual, yrs) }.getOrDefault(Double.NaN)
    val priceV = num(price)
    val taxV = num(taxRate)
    val (taxTotal, taxAmt) = runCatching { Finance.withTax(priceV, taxV, inclusive) }.getOrDefault(Double.NaN to Double.NaN)
    val unitARes = runCatching { Finance.unitPrice(num(priceA), num(qtyA)) }
    val unitBRes = runCatching { Finance.unitPrice(num(priceB), num(qtyB)) }
    val unitA = unitARes.getOrDefault(Double.NaN)
    val unitB = unitBRes.getOrDefault(Double.NaN)
    val verdict = when {
        unitARes.isFailure && unitBRes.isFailure -> "Enter quantities"
        unitARes.isFailure -> "B is the better buy"
        unitBRes.isFailure -> "A is the better buy"
        unitA < unitB -> "A is the better buy"
        unitB < unitA -> "B is the better buy"
        else -> "Tie"
    }
    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            var pctMode by remember { mutableStateOf(0) }
            var pctX by remember { mutableStateOf("15") }
            var pctY by remember { mutableStateOf("200") }
            val pctModes = listOf("X% of Y", "X is what % of Y", "% difference")
            val pctValue: Double = runCatching {
                val x = num(pctX)
                val y = num(pctY)
                when (pctMode) {
                    0 -> x * y / 100.0
                    1 -> if (y == 0.0) Double.NaN else x / y * 100.0
                    else -> if ((x + y) == 0.0) Double.NaN else kotlin.math.abs(x - y) / (kotlin.math.abs(x + y) / 2.0) * 100.0
                }
            }.getOrDefault(Double.NaN)
            val pctOut = if (!pctValue.isFinite()) "—"
            else if (pctMode == 0) fmt(pctValue, 2)
            else fmt(pctValue, 2) + " %"
            SectionCard("Percent") {
                MethodDropdown(
                    selected = pctModes[pctMode],
                    options = listOf(
                        "X% of Y" to "x · y / 100",
                        "X is what % of Y" to "x / y · 100",
                        "% difference" to "|x − y| / avg · 100"
                    ),
                    onSelect = { pctMode = pctModes.indexOf(it).coerceAtLeast(0) },
                    label = "Method"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        NumField(pctX, { pctX = it }, if (pctMode == 2) "A" else "X")
                    }
                    Box(Modifier.weight(1f)) {
                        NumField(pctY, { pctY = it }, if (pctMode == 2) "B" else "Y")
                    }
                }
                HelperCaption(if (pctMode == 2) "Symmetric difference of A and B." else "X is the percent value, Y is the base.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Percent, "Result", pctOut)
            }
        }
        item {
            SectionCard("Tip and split") {
                NumField(bill, { bill = it }, "Bill")
                Text("Tip: ${fmt(tipPctEff, 2)}%", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = tipPct.coerceIn(0f, 30f),
                    onValueChange = {
                        tipPct = it
                        tipPctText = fmt(it.toDouble(), 0)
                    },
                    valueRange = 0f..30f
                )
                NumField(tipPctText, { v ->
                    tipPctText = v
                    v.toDoubleOrNull()?.let { tipPct = it.toFloat().coerceIn(0f, 30f) }
                }, "Tip % (any value)")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("None", "Up", "Nearest").forEach { mode ->
                        item {
                            FilterChip(
                                selected = tipRound == mode,
                                onClick = { tipRound = mode },
                                label = { Text(mode) }
                            )
                        }
                    }
                }
                NumField(split, { split = it }, "Split between", integer = true)
                HelperCaption("Total is split equally across people. Slider caps at 30% — type any % above for higher tips. Rounding applies to the grand total (Up = ceil, Nearest = round). Amounts shown without currency.")
                ToolErrorLine(tipBillErr)
                ToolErrorLine(tipSplitErr)
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Tip", if (tipBillErr != null || tipSplitErr != null) "—" else fmt(tipAmt, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Total", if (tipBillErr != null || tipSplitErr != null) "—" else fmt(grand, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Per person", if (tipBillErr != null || tipSplitErr != null) "—" else fmt(per, 2))
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
                Slider(
                    value = monthsF.coerceIn(6f, 360f),
                    onValueChange = {
                        monthsF = it
                        monthsText = it.toInt().toString()
                    },
                    valueRange = 6f..360f
                )
                NumField(monthsText, { v ->
                    monthsText = v
                    v.toIntOrNull()?.let { monthsF = it.toFloat().coerceIn(6f, 360f) }
                }, "Months (1..360)")
                HelperCaption("Slider covers 6..360 months — type 1..5 below for shorter terms. Term minimum is 1 month. Full schedule below scrolls independently.")
                ToolErrorLine(emiPrincipalErr)
                ToolErrorLine(emiMonthsErr)
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Monthly EMI", if (emiPrincipalErr != null) "—" else fmt(emi, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Total paid", if (emiPrincipalErr != null) "—" else fmt(loanTotalPaid, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Total interest", if (emiPrincipalErr != null) "—" else fmt(loanTotalInt, 2))
                if (emiPrincipalErr == null && fullSched.isNotEmpty()) {
                    schedPreview.take(3).forEach { row ->
                        ToolResultRow(Icons.Filled.DateRange, "Month ${row.n}", "int ${fmt(row.interest, 2)} · bal ${fmt(row.balance, 2)}")
                    }
                    val yearGroups = fullSched.groupBy { (it.n - 1) / 12 }
                    yearGroups.toSortedMap().forEach { (y, rows) ->
                        ToolResultRow(
                            Icons.Filled.Info,
                            "Year ${y + 1} subtotal",
                            "int ${fmt(rows.sumOf { it.interest }, 2)} · princ ${fmt(rows.sumOf { it.principal }, 2)}"
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(280.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(fullSched) { row ->
                                val tag = if (row.n == months) "Month $months (last)" else "Month ${row.n}"
                                ToolResultRow(Icons.Filled.DateRange, tag, "int ${fmt(row.interest, 2)} · bal ${fmt(row.balance, 2)}")
                            }
                        }
                    }
                } else if (emiPrincipalErr == null) {
                    HelperCaption("Schedule unavailable — check inputs.")
                }
            }
        }
        item {
            var ioP by remember { mutableStateOf("10000") }
            var ioR by remember { mutableStateOf("5") }
            var ioY by remember { mutableStateOf("5") }
            var ioFreq by remember { mutableStateOf("Yearly") }
            val ioFreqN = when (ioFreq) {
                "Quarterly" -> 4
                "Monthly" -> 12
                else -> 1
            }
            val ioPv = num(ioP)
            val ioRv = num(ioR)
            val ioYv = num(ioY)
            val ioRes = runCatching {
                val (siI, siT) = Finance.simple(ioPv, ioRv, ioYv)
                val ciT = Finance.compound(ioPv, ioRv, ioYv, ioFreqN)
                Triple(siI to siT, ciT, ciT - ioPv)
            }
            val ioErr = when {
                ioPv <= 0 -> "Principal must be greater than 0."
                ioYv <= 0 -> "Years must be greater than 0."
                else -> ioRes.exceptionOrNull()?.message
            }
            SectionCard("Interest over time") {
                NumField(ioP, { ioP = it }, "Principal")
                NumField(ioR, { ioR = it }, "Annual %")
                NumField(ioY, { ioY = it }, "Years")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Yearly", "Quarterly", "Monthly").forEach { f ->
                        item {
                            FilterChip(selected = ioFreq == f, onClick = { ioFreq = f }, label = { Text(f) })
                        }
                    }
                }
                HelperCaption("Simple: I = P·r·t. Compound: A = P·(1 + r/$ioFreqN)^(${ioFreqN}·t). Independent of the EMI card.")
                ToolErrorLine(ioErr)
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Simple interest", if (ioErr != null) "—" else fmt(ioRes.getOrNull()?.first?.first ?: Double.NaN, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Simple total", if (ioErr != null) "—" else fmt(ioRes.getOrNull()?.first?.second ?: Double.NaN, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Compound total", if (ioErr != null) "—" else fmt(ioRes.getOrNull()?.second ?: Double.NaN, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Compound interest", if (ioErr != null) "—" else fmt(ioRes.getOrNull()?.third ?: Double.NaN, 2))
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
                ToolResultRow(Icons.Filled.AttachMoney, if (inclusive) "Net" else "Total", fmt(taxTotal, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Tax", fmt(taxAmt, 2))
            }
        }
        item {
            SectionCard("Unit price compare") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(priceA, { priceA = it }, "Price A") }
                    Box(Modifier.weight(1f)) { NumField(qtyA, { qtyA = it }, "Qty A") }
                }
                OutlinedTextField(value = unitAName, onValueChange = { unitAName = it }, label = { Text("Unit A (e.g. g, ml)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(priceB, { priceB = it }, "Price B") }
                    Box(Modifier.weight(1f)) { NumField(qtyB, { qtyB = it }, "Qty B") }
                }
                OutlinedTextField(value = unitBName, onValueChange = { unitBName = it }, label = { Text("Unit B (e.g. g, ml)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                val qtyAv = qtyA.toDoubleOrNull()
                val qtyBv = qtyB.toDoubleOrNull()
                val qtyErr = when {
                    qtyAv == null || qtyAv <= 0 -> "Quantity A must be greater than 0."
                    qtyBv == null || qtyBv <= 0 -> "Quantity B must be greater than 0."
                    else -> null
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Unit price A", if (qtyErr != null || unitARes.isFailure) "—" else fmt(unitA, 4) + " / " + unitAName.ifBlank { "unit" })
                ToolResultRow(Icons.Filled.AttachMoney, "Unit price B", if (qtyErr != null || unitBRes.isFailure) "—" else fmt(unitB, 4) + " / " + unitBName.ifBlank { "unit" })
                ToolErrorLine(qtyErr)
                ToolErrorLine(unitARes.exceptionOrNull()?.message)
                ToolErrorLine(unitBRes.exceptionOrNull()?.message)
                ToolResultRow(Icons.Filled.Info, "Verdict", if (qtyErr != null) "Enter valid quantities" else verdict)
            }
        }
        item {
            var sipMf by remember { mutableFloatStateOf(5000f) }
            var sipMText by remember { mutableStateOf("5000") }
            var sipRf by remember { mutableFloatStateOf(12f) }
            var sipRText by remember { mutableStateOf("12") }
            var sipYf by remember { mutableFloatStateOf(10f) }
            var sipYText by remember { mutableStateOf("10") }
            val sipM = sipMText.toDoubleOrNull() ?: sipMf.toDouble()
            val sipR = sipRText.toDoubleOrNull() ?: sipRf.toDouble()
            val sipY = sipYText.toDoubleOrNull() ?: sipYf.toDouble()
            val res = runCatching { Finance.sip(sipM, sipR, sipY) }.getOrNull()
            val sipErr = if (sipY <= 0) "Years must be greater than 0." else if (sipM < 0) "Monthly must be >= 0." else null
            val sipYears = sipY.toInt().coerceIn(0, 40)
            val sipBreakdown: List<Triple<Int, Double, Double>> = runCatching {
                if (sipErr != null) throw IllegalStateException("bad sip")
                val r = sipR / 1200
                buildList {
                    for (y in 1..sipYears) {
                        val n = y * 12
                        val invested = sipM * n
                        val total = if (r == 0.0) invested else sipM * (Math.pow(1 + r, n.toDouble()) - 1) / r * (1 + r)
                        add(Triple(y, invested, total))
                    }
                }
            }.getOrDefault(emptyList())
            SectionCard("SIP") {
                Text("Monthly: ${sipMf.toInt()}", style = MaterialTheme.typography.labelLarge)
                Slider(value = sipMf, onValueChange = { sipMf = it; sipMText = it.toInt().toString() }, valueRange = 500f..100000f)
                NumField(sipMText, { v -> sipMText = v; v.toDoubleOrNull()?.let { sipMf = it.toFloat().coerceIn(500f, 100000f) } }, "Monthly (exact)")
                Text("Annual %: ${fmt(sipRf.toDouble(), 1)}", style = MaterialTheme.typography.labelLarge)
                Slider(value = sipRf, onValueChange = { sipRf = it; sipRText = fmt(it.toDouble(), 1) }, valueRange = 0f..30f)
                NumField(sipRText, { v -> sipRText = v; v.toDoubleOrNull()?.let { sipRf = it.toFloat().coerceIn(0f, 30f) } }, "Annual % (exact)")
                Text("Years: ${sipYf.toInt()}", style = MaterialTheme.typography.labelLarge)
                Slider(value = sipYf, onValueChange = { sipYf = it; sipYText = it.toInt().toString() }, valueRange = 1f..40f)
                NumField(sipYText, { v -> sipYText = v; v.toDoubleOrNull()?.let { sipYf = it.toFloat().coerceIn(1f, 40f) } }, "Years (exact)")
                ToolErrorLine(sipErr)
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Invested", if (sipErr != null) "—" else fmt(res?.first ?: Double.NaN, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Gain", if (sipErr != null) "—" else fmt(res?.second ?: Double.NaN, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Total", if (sipErr != null) "—" else fmt(res?.third ?: Double.NaN, 2))
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
                        val ssw = 18.dp.toPx()
                        val sInset = ssw / 2f + 1.dp.toPx()
                        val sTopLeft = Offset(sInset, sInset)
                        val sSize = Size(size.width - sInset * 2f, size.height - sInset * 2f)
                        drawArc(
                            color = surfaceTrack,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = sTopLeft,
                            size = sSize,
                            style = Stroke(width = ssw, cap = StrokeCap.Round)
                        )
                        if (sipSweep > 0f) {
                            drawArc(
                                color = sipPrimary,
                                startAngle = -90f,
                                sweepAngle = 360f * invFrac * sipSweep,
                                useCenter = false,
                                topLeft = sTopLeft,
                                size = sSize,
                                style = Stroke(width = ssw, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = sipTertiary,
                                startAngle = -90f + 360f * invFrac * sipSweep,
                                sweepAngle = 360f * (1f - invFrac) * sipSweep,
                                useCenter = false,
                                topLeft = sTopLeft,
                                size = sSize,
                                style = Stroke(width = ssw, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).background(sipPrimary, RoundedCornerShape(4.dp)))
                    Text("Invested ${if (sipErr != null) "—" else fmt(sipInvested, 2)}", style = MaterialTheme.typography.labelSmall)
                    Box(Modifier.size(12.dp).background(sipTertiary, RoundedCornerShape(4.dp)))
                    Text("Gain ${if (sipErr != null) "—" else fmt(sipGain, 2)}", style = MaterialTheme.typography.labelSmall)
                }
                if (sipErr == null && sipBreakdown.isNotEmpty()) {
                    HelperCaption("Year-end invested vs total (monthly compounding).")
                    Box(Modifier.fillMaxWidth().height(220.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(sipBreakdown) { row ->
                                ToolResultRow(Icons.Filled.DateRange, "Year ${row.first}", "in ${fmt(row.second, 0)} · tot ${fmt(row.third, 0)}")
                            }
                        }
                    }
                }
            }
        }
        item {
            var cagrI by remember { mutableStateOf("10000") }
            var cagrF by remember { mutableStateOf("20000") }
            var cagrY by remember { mutableStateOf("10") }
            val cagrIv = num(cagrI)
            val cagrYv = num(cagrY)
            val cagrRes = runCatching { Finance.cagr(cagrIv, num(cagrF), cagrYv) }
            val r = cagrRes.getOrNull()
            val cagrErr = when {
                cagrIv <= 0 -> "Initial must be greater than 0."
                cagrYv <= 0 -> "Years must be greater than 0."
                else -> cagrRes.exceptionOrNull()?.message
            }
            SectionCard("CAGR") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(cagrI, { cagrI = it }, "Initial") }
                    Box(Modifier.weight(1f)) { NumField(cagrF, { cagrF = it }, "Final") }
                    Box(Modifier.weight(1f)) { NumField(cagrY, { cagrY = it }, "Years") }
                }
                HelperCaption("CAGR = (Final / Initial)^(1 / Years) − 1.")
                ToolErrorLine(cagrErr)
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Percent, "CAGR %", if (cagrErr != null) "—" else fmt((r ?: Double.NaN) * 100, 2))
            }
        }
        item {
            var fdP by remember { mutableStateOf("10000") }
            var fdR by remember { mutableStateOf("6") }
            var fdY by remember { mutableStateOf("5") }
            var fdMode by remember { mutableStateOf(2) }
            val fdLabels = listOf("Monthly", "Quarterly", "Half-yearly", "Yearly")
            val fdFreqs = listOf(12, 4, 2, 1)
            val fdFreq = fdFreqs.getOrNull(fdMode) ?: 4
            val fdPv = num(fdP)
            val fdYv = num(fdY)
            val fdRes = runCatching { Finance.fd(fdPv, num(fdR), fdYv, fdFreq) }
            val fdErr = when {
                fdPv < 0 -> "Principal must be >= 0."
                fdYv <= 0 -> "Years must be greater than 0."
                else -> fdRes.exceptionOrNull()?.message
            }
            val res = fdRes.getOrNull()
            SectionCard("Fixed deposit") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(fdP, { fdP = it }, "Principal") }
                    Box(Modifier.weight(1f)) { NumField(fdR, { fdR = it }, "Rate %") }
                    Box(Modifier.weight(1f)) { NumField(fdY, { fdY = it }, "Years") }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(fdLabels.size) { i ->
                        FilterChip(selected = fdMode == i, onClick = { fdMode = i }, label = { Text(fdLabels[i]) })
                    }
                }
                HelperCaption("A = P·(1 + r/$fdFreq)^(${fdFreq}·t). Frequency matches the Bank deposit card.")
                ToolErrorLine(fdErr)
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Interest", if (fdErr != null) "—" else fmt(res?.second ?: Double.NaN, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Total", if (fdErr != null) "—" else fmt(res?.third ?: Double.NaN, 2))
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
                ToolResultRow(Icons.Filled.AttachMoney, "Net", fmt(res?.first ?: Double.NaN, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Tax", fmt(res?.second ?: Double.NaN, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Gross", fmt(res?.third ?: Double.NaN, 2))
            }
        }
        item {
            var dist by remember { mutableStateOf("500") }
            var cons by remember { mutableStateOf("7.5") }
            var fuelPrice by remember { mutableStateOf("1.8") }
            var avg by remember { mutableStateOf("90") }
            val distV = num(dist)
            val consV = num(cons)
            val avgV = avg.toDoubleOrNull()
            val avgErr = if (avgV == null || avgV <= 0) "Average speed must be greater than 0." else null
            val cost = runCatching { TripKit.fuelCost(distV, consV, num(fuelPrice)) }.getOrNull()
            val time = if (avgErr != null) null else runCatching { TripKit.tripTime(distV, avgV ?: 0.0) }.getOrNull()
            val liters = runCatching { distV / 100 * consV }.getOrNull()
            val timeHmm = runCatching {
                val t = time ?: throw IllegalStateException("bad time")
                if (!t.isFinite() || t < 0) throw IllegalStateException("bad time")
                val h = t.toInt()
                val m = ((t - h) * 60).toInt().coerceIn(0, 59)
                "%d:%02d".format(h, m)
            }.getOrNull()
            SectionCard("Trip cost") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(dist, { dist = it }, "Km") }
                    Box(Modifier.weight(1f)) { NumField(cons, { cons = it }, "L/100km") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(fuelPrice, { fuelPrice = it }, "Price/L") }
                    Box(Modifier.weight(1f)) { NumField(avg, { avg = it }, "Avg km/h") }
                }
                ToolErrorLine(avgErr)
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Fuel cost", if (avgErr != null) cost?.let { fmt(it, 2) } ?: "—" else cost?.let { fmt(it, 2) } ?: "—")
                ToolResultRow(Icons.Filled.AttachMoney, "Fuel needed (L)", liters?.let { fmt(it, 2) } ?: "—")
                ToolResultRow(Icons.Filled.DateRange, "Drive time (H:MM)", if (avgErr != null || timeHmm == null) "—" else timeHmm + " (${fmt(time ?: Double.NaN, 2)} h)")
            }
        }
        item {
            var sh1 by remember { mutableStateOf("10") }
            var pr1 by remember { mutableStateOf("100") }
            var sh2 by remember { mutableStateOf("10") }
            var pr2 by remember { mutableStateOf("80") }
            val res = runCatching { Finance.stockAverage(num(sh1), num(pr1), num(sh2), num(pr2)) }.getOrNull()
            SectionCard("Stock average") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(sh1, { sh1 = it }, "Shares 1") }
                    Box(Modifier.weight(1f)) { NumField(pr1, { pr1 = it }, "Price 1") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(sh2, { sh2 = it }, "Shares 2") }
                    Box(Modifier.weight(1f)) { NumField(pr2, { pr2 = it }, "Price 2") }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "Total shares", fmt(res?.first ?: Double.NaN, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Average price", fmt(res?.second ?: Double.NaN, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Total cost", fmt(res?.third ?: Double.NaN, 2))
            }
        }
        item {
            var sgM by remember { mutableStateOf("5000") }
            var sgR by remember { mutableStateOf("12") }
            var sgY by remember { mutableStateOf("10") }
            val fv = runCatching { Finance.savingsGoal(num(sgM), num(sgR), num(sgY)) }.getOrNull()
            SectionCard("Savings goal") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(sgM, { sgM = it }, "Monthly") }
                    Box(Modifier.weight(1f)) { NumField(sgR, { sgR = it }, "Annual %") }
                    Box(Modifier.weight(1f)) { NumField(sgY, { sgY = it }, "Years") }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Future value", fv?.let { fmt(it, 2) } ?: "—")
            }
        }
        item {
            var r72 by remember { mutableStateOf("8") }
            val dbl = runCatching { Finance.rule72(num(r72)) }.getOrNull()
            SectionCard("Rule of 72") {
                NumField(r72, { r72 = it }, "Rate %")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.DateRange, "Years to double", dbl?.let { fmt(it, 2) } ?: "—")
            }
        }
        item {
            var gpaScale by remember { mutableStateOf("4.0") }
            var gpaRows by remember { mutableStateOf(listOf("4" to "3", "3" to "3", "3" to "3")) }
            fun letterToPoint(letter: String): Double? = when (letter.trim().uppercase()) {
                "A+", "A" -> 4.0
                "A-" -> 3.7
                "B+" -> 3.3
                "B" -> 3.0
                "B-" -> 2.7
                "C+" -> 2.3
                "C" -> 2.0
                "C-" -> 1.7
                "D+" -> 1.3
                "D" -> 1.0
                "F" -> 0.0
                else -> null
            }
            fun gradeToPoint(raw: String): Double? {
                return when (gpaScale) {
                    "Letter" -> letterToPoint(raw)
                    "100" -> raw.toDoubleOrNull()?.let { (it / 100 * 4.0).coerceIn(0.0, 4.0) }
                    else -> raw.toDoubleOrNull()
                }
            }
            val gpaCreditsErr = if (gpaRows.any { (it.second.toDoubleOrNull() ?: -1.0) <= 0 }) "Each course needs credits greater than 0." else null
            val gpaGradeErr = if (gpaRows.any { gradeToPoint(it.first) == null }) "Check grade entries for the selected scale (4.0 number, 0..100, or A+..F)." else null
            val gpaRes = runCatching {
                if (gpaCreditsErr != null || gpaGradeErr != null) throw IllegalStateException("fix inputs")
                Finance.gpa(gpaRows.map { (gradeToPoint(it.first) ?: throw IllegalStateException("bad grade")) to num(it.second) })
            }
            val gpa = gpaRes.getOrNull()
            SectionCard("GPA") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("4.0", "100", "Letter").forEach { s ->
                        item {
                            FilterChip(selected = gpaScale == s, onClick = { gpaScale = s }, label = { Text(s) })
                        }
                    }
                }
                HelperCaption("Letter map: A+/A=4.0, A-=3.7, B+=3.3, B=3.0, B-=2.7, C+=2.3, C=2.0, C-=1.7, D+=1.3, D=1.0, F=0.0. 100-scale converts as score/100·4.")
                gpaRows.forEachIndexed { idx, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) {
                            if (gpaScale == "Letter") {
                                OutlinedTextField(value = row.first, onValueChange = { v ->
                                    gpaRows = gpaRows.toMutableList().also { it[idx] = v to row.second }
                                }, label = { Text("Grade ${idx + 1}") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            } else {
                                NumField(row.first, { v ->
                                    gpaRows = gpaRows.toMutableList().also { it[idx] = v to row.second }
                                }, "Grade ${idx + 1}")
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            NumField(row.second, { v ->
                                gpaRows = gpaRows.toMutableList().also { it[idx] = row.first to v }
                            }, "Credits ${idx + 1}")
                        }
                        TextButton(onClick = { gpaRows = gpaRows.filterIndexed { i, _ -> i != idx } }, enabled = gpaRows.size > 1) { Text("X") }
                    }
                }
                TextButton(onClick = { gpaRows = gpaRows + ("3" to "3") }) { Text("+ Add course") }
                ToolErrorLine(gpaCreditsErr)
                ToolErrorLine(gpaGradeErr ?: gpaRes.exceptionOrNull()?.message?.takeIf { gpaCreditsErr == null && gpaGradeErr == null })
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "GPA", if (gpaCreditsErr != null || gpaGradeErr != null) "—" else gpa?.let { fmt(it, 2) } ?: "—")
            }
        }
        item {
            var gnCur by remember { mutableStateOf("85") }
            var gnDone by remember { mutableStateOf("60") }
            var gnTarget by remember { mutableStateOf("90") }
            val gnDoneV = gnDone.toDoubleOrNull()
            val gnWeightErr = if (gnDoneV == null || gnDoneV < 0 || gnDoneV > 100) "Weight done must be 0..100 (use <100)." else null
            val neededRes = if (gnWeightErr != null) null else runCatching { Finance.gradeNeeded(num(gnCur), gnDoneV ?: 0.0, num(gnTarget)) }
            val needed = neededRes?.getOrNull()
            val gnState: String? = when {
                gnWeightErr != null -> null
                needed == null -> null
                needed > 100 -> "Impossible — needs over 100% on the remainder."
                needed <= 0 -> "Already met — no score needed on the remainder."
                else -> null
            }
            SectionCard("Grade needed") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(gnCur, { gnCur = it }, "Current %") }
                    Box(Modifier.weight(1f)) { NumField(gnDone, { gnDone = it }, "Weight done %") }
                    Box(Modifier.weight(1f)) { NumField(gnTarget, { gnTarget = it }, "Target %") }
                }
                HelperCaption("Weight done must be 0..100 and below 100; >100% needed is impossible, <=0% means already met.")
                ToolErrorLine(gnWeightErr)
                ToolErrorLine(neededRes?.exceptionOrNull()?.message)
                if (gnState != null) {
                    Text(gnState, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Percent, "Needed on remainder", if (gnWeightErr != null || needed == null) "—" else fmt(needed, 2) + " %")
            }
        }
        item {
            var pcRate by remember { mutableStateOf("20") }
            var pcHours by remember { mutableStateOf("40") }
            var pcTax by remember { mutableStateOf("20") }
            var pcOtHours by remember { mutableStateOf("0") }
            var pcOtMult by remember { mutableStateOf("1.5") }
            var pcFreq by remember { mutableStateOf("Monthly") }
            val pcBase = runCatching { Finance.paycheck(num(pcRate), num(pcHours), num(pcTax)) }
            val pcOtPay = runCatching { num(pcOtHours) * num(pcRate) * num(pcOtMult) * 52 / 12 }.getOrDefault(Double.NaN)
            val pcGrossM = (pcBase.getOrNull()?.first ?: Double.NaN) + if (pcOtPay.isFinite()) pcOtPay else 0.0
            val pcTaxM = if (pcGrossM.isFinite()) pcGrossM * num(pcTax) / 100 else Double.NaN
            val pcNetM = if (pcGrossM.isFinite() && pcTaxM.isFinite()) pcGrossM - pcTaxM else Double.NaN
            val pcDiv = when (pcFreq) {
                "Weekly" -> 52.0 / 12.0
                "Biweekly" -> 26.0 / 12.0
                else -> 1.0
            }
            fun perPeriod(m: Double) = if (!m.isFinite()) Double.NaN else m / pcDiv
            SectionCard("Paycheck") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(pcRate, { pcRate = it }, "Hourly rate") }
                    Box(Modifier.weight(1f)) { NumField(pcHours, { pcHours = it }, "Hours/week") }
                    Box(Modifier.weight(1f)) { NumField(pcTax, { pcTax = it }, "Tax %") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(pcOtHours, { pcOtHours = it }, "OT hrs/week") }
                    Box(Modifier.weight(1f)) { NumField(pcOtMult, { pcOtMult = it }, "OT multiplier") }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Weekly", "Biweekly", "Monthly").forEach { f ->
                        item {
                            FilterChip(selected = pcFreq == f, onClick = { pcFreq = f }, label = { Text(f) })
                        }
                    }
                }
                HelperCaption("Monthly assumes hourly × hours × 52 / 12; weekly = monthly ÷ (52/12), biweekly = monthly ÷ (26/12). Overtime adds OT-hrs × rate × multiplier × 52 / 12.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Gross / $pcFreq", fmt(perPeriod(pcGrossM), 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Tax / $pcFreq", fmt(perPeriod(pcTaxM), 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Net / $pcFreq", fmt(perPeriod(pcNetM), 2))
                ToolErrorLine(pcBase.exceptionOrNull()?.message)
            }
        }
        item {
            var poBal by remember { mutableStateOf("1000") }
            var poApr by remember { mutableStateOf("12") }
            var poPay by remember { mutableStateOf("100") }
            val poBalV = num(poBal)
            val poAprV = num(poApr)
            val poPayV = num(poPay)
            val poMonthlyInt = poBalV * poAprV / 1200
            val neverPays = poBalV > 0 && poAprV > 0 && poPayV.isFinite() && poPayV <= poMonthlyInt
            val res = if (neverPays) null else runCatching { Finance.creditPayoff(poBalV, poAprV, poPayV) }
            SectionCard("Credit payoff") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(poBal, { poBal = it }, "Balance") }
                    Box(Modifier.weight(1f)) { NumField(poApr, { poApr = it }, "APR %") }
                    Box(Modifier.weight(1f)) { NumField(poPay, { poPay = it }, "Monthly pay") }
                }
                if (neverPays) {
                    Text(
                        "Payment covers only interest (${fmt(poMonthlyInt, 2)}/mo) — balance never decreases. Increase the payment.",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.DateRange, "Months", if (neverPays) "—" else res?.getOrNull()?.first?.toString() ?: "—")
                ToolResultRow(Icons.Filled.AttachMoney, "Total interest", if (neverPays) "—" else res?.getOrNull()?.let { fmt(it.second, 2) } ?: "—")
                ToolResultRow(Icons.Filled.AttachMoney, "Total paid", if (neverPays) "—" else res?.getOrNull()?.let { fmt(it.third, 2) } ?: "—")
                ToolErrorLine(if (neverPays) null else res?.exceptionOrNull()?.message)
            }
        }
        item {
            var lcP by remember { mutableStateOf("10000") }
            var lcRa by remember { mutableStateOf("5") }
            var lcRb by remember { mutableStateOf("8") }
            var lcM by remember { mutableStateOf("24") }
            val res = runCatching {
                Finance.loanCompare(num(lcP), num(lcRa), num(lcRb), lcM.toIntOrNull() ?: 0)
            }
            val saving = res.getOrNull()?.third
            val savingLabel = when {
                saving == null || !saving.isFinite() -> "Difference total (A−B)"
                saving >= 0 -> "Savings total (A−B)"
                else -> "B costs ${fmt(-saving, 2)} more (A−B negative)"
            }
            SectionCard("Loan compare") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(lcP, { lcP = it }, "Principal") }
                    Box(Modifier.weight(1f)) { NumField(lcM, { lcM = it }, "Months", integer = true) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(lcRa, { lcRa = it }, "Rate A %") }
                    Box(Modifier.weight(1f)) { NumField(lcRb, { lcRb = it }, "Rate B %") }
                }
                HelperCaption("Positive = A saves vs B; negative = B costs more than A.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "EMI A", res.getOrNull()?.let { fmt(it.first, 2) } ?: "—")
                ToolResultRow(Icons.Filled.AttachMoney, "EMI B", res.getOrNull()?.let { fmt(it.second, 2) } ?: "—")
                ToolResultRow(Icons.Filled.AttachMoney, savingLabel, res.getOrNull()?.let { fmt(it.third, 2) } ?: "—")
                ToolErrorLine(res.exceptionOrNull()?.message)
            }
        }
        item {
            var mgCost by remember { mutableStateOf("50") }
            var mgPrice by remember { mutableStateOf("100") }
            val marginRes = runCatching { Finance.profitMargin(num(mgCost), num(mgPrice)) }
            val margin = marginRes.getOrNull()
            SectionCard("Profit margin") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(mgCost, { mgCost = it }, "Cost") }
                    Box(Modifier.weight(1f)) { NumField(mgPrice, { mgPrice = it }, "Price") }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Percent, "Margin", margin?.let { fmt(it, 2) + " %" } ?: "—")
                ToolErrorLine(marginRes.exceptionOrNull()?.message)
            }
        }
        item {
            val appCtx = LocalContext.current.applicationContext
            val repo = remember { calc.u.data.CryptoRepository(appCtx) }
            val prices by repo.prices.collectAsState(initial = emptyMap())
            val stale by repo.isStale.collectAsState(initial = true)
            val source by repo.source.collectAsState()
            var cryptoAmt by remember { mutableStateOf("100") }
            var cryptoQuery by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) { runCatching { repo.refresh() }.onFailure { } }
            val coins = listOf(
                "bitcoin", "ethereum", "tether", "bnb", "solana", "usd-coin", "xrp",
                "dogecoin", "toncoin", "cardano", "avalanche-2", "shiba-inu", "chainlink",
                "polkadot", "bitcoin-cash", "near", "litecoin", "uniswap", "dai", "tron"
            )
            val amtParsed = cryptoAmt.toDoubleOrNull()
            val amt = num(cryptoAmt)
            val cryptoErr = if (amtParsed == null || amtParsed <= 0) "Enter a USD amount greater than 0." else null
            val filtered = if (cryptoQuery.isBlank()) coins else coins.filter { it.contains(cryptoQuery.trim().lowercase()) }
            SectionCard("Crypto") {
                NumField(cryptoAmt, { cryptoAmt = it }, "USD amount")
                OutlinedTextField(value = cryptoQuery, onValueChange = { cryptoQuery = it }, label = { Text("Search coins") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                ToolErrorLine(cryptoErr)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "CoinGecko · " + if (stale && source.label == "live") "live · stale" else source.label,
                        style = MaterialTheme.typography.labelLarge
                    )
            Button(onClick = { scope.launch { runCatching { repo.refresh() } } }) { Text("Refresh") }
                }
                HorizontalDivider()
                if (filtered.isEmpty()) {
                    HelperCaption("No coins match your search.")
                }
                filtered.forEach { id ->
                    val coin = prices[id]
                    if (coin == null) {
                        ToolResultRow(Icons.Filled.AttachMoney, id, "—")
                    } else {
                        val change = coin.usd_24h_change
                        val changeTxt = if (change == null) "n/a" else fmt(change, 2) + " %"
                        val qty = if (cryptoErr != null || !coin.usd.isFinite() || coin.usd <= 0) Double.NaN else amt / coin.usd
                        ToolResultRow(Icons.Filled.AttachMoney, id, fmt(coin.usd, 2) + " USD (" + changeTxt + ") → " + fmt(qty, 6))
                    }
                }
            }
        }
        item {
            var zakAssets by remember { mutableStateOf("10000") }
            var zakDebts by remember { mutableStateOf("1000") }
            var zakNisab by remember { mutableStateOf("0") }
            val zakNisabV = zakNisab.toDoubleOrNull()
            val zakNet = num(zakAssets) - num(zakDebts)
            val belowNisab = zakNisabV != null && zakNisabV > 0 && zakNet < zakNisabV
            val zakRes = runCatching { Finance.zakat(num(zakAssets), num(zakDebts), zakNisabV ?: 0.0) }
            val due = zakRes.getOrNull()
            SectionCard("Zakat") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(zakAssets, { zakAssets = it }, "Assets") }
                    Box(Modifier.weight(1f)) { NumField(zakDebts, { zakDebts = it }, "Debts") }
                }
                NumField(zakNisab, { zakNisab = it }, "Nisab")
                HelperCaption("Nisab = minimum net assets before zakat is due (ask your school for the current value in your currency). Zakat = 2.5% of net assets at/above nisab.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Zakat due", if (belowNisab) "0 due (below nisab)" else due?.let { fmt(it, 2) } ?: "—")
                ToolErrorLine(zakRes.exceptionOrNull()?.message)
            }
        }
        item {
            var invAmt by remember { mutableStateOf("10000") }
            var stlAmt by remember { mutableStateOf("12000") }
            var startD by remember { mutableStateOf("2024-01-01") }
            var endD by remember { mutableStateOf("2025-01-01") }
            val startParsed = runCatching { LocalDate.parse(startD.trim()) }.getOrNull()
            val endParsed = runCatching { LocalDate.parse(endD.trim()) }.getOrNull()
            val dateOrderErr = if (startParsed != null && endParsed != null && endParsed.isBefore(startParsed)) "End date is before start date." else null
            val days = if (dateOrderErr != null) null else runCatching {
                ChronoUnit.DAYS.between(LocalDate.parse(startD.trim()), LocalDate.parse(endD.trim())).toInt()
            }.getOrNull()
            val roiRes = if (days == null || dateOrderErr != null) null else runCatching { Finance.investRoi(num(invAmt), num(stlAmt), days) }
            val roi = roiRes?.getOrNull()
            SectionCard("Investment ROI") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(invAmt, { invAmt = it }, "Invested") }
                    Box(Modifier.weight(1f)) { NumField(stlAmt, { stlAmt = it }, "Settled") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = startD,
                            onValueChange = { startD = it },
                            label = { Text("Start yyyy-MM-dd") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = endD,
                            onValueChange = { endD = it },
                            label = { Text("End yyyy-MM-dd") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                HelperCaption("Annualized = (Settled / Invested)^(365 / days) − 1, compounding daily-equivalent.")
                ToolErrorLine(dateOrderErr)
                HorizontalDivider()
                ToolResultRow(Icons.Filled.DateRange, "Days", if (dateOrderErr != null) "—" else days?.takeIf { it > 0 }?.toString() ?: "—")
                ToolResultRow(Icons.Filled.AttachMoney, "Profit", if (dateOrderErr != null) "—" else roi?.let { fmt(it.first, 2) } ?: "—")
                ToolResultRow(Icons.Filled.Percent, "Return", if (dateOrderErr != null) "—" else roi?.let { fmt(it.second, 2) + " %" } ?: "—")
                ToolResultRow(Icons.Filled.Percent, "Annualized", if (dateOrderErr != null) "—" else roi?.let { fmt(it.third, 2) + " %" } ?: "—")
                ToolErrorLine(roiRes?.exceptionOrNull()?.message)
            }
        }
        item {
            var gstNet by remember { mutableStateOf("100") }
            var gstGross by remember { mutableStateOf("118") }
            var gstRate by remember { mutableStateOf(18.0) }
            var gstCustom by remember { mutableStateOf("") }
            var gstIntra by remember { mutableStateOf(true) }
            var gstDir by remember { mutableStateOf("net") }
            val gstRates = listOf(5.0, 12.0, 18.0, 28.0)
            val gstEffRate = gstCustom.toDoubleOrNull()?.takeIf { it >= 0 } ?: gstRate
            val gstRes = runCatching {
                if (gstDir == "net") Finance.gstForward(num(gstNet), gstEffRate, gstIntra)
                else Finance.gstReverse(num(gstGross), gstEffRate)
            }.getOrNull()
            val gstTax = gstRes?.second ?: Double.NaN
            SectionCard("GST") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = gstDir == "net", onClick = { gstDir = "net" }, label = { Text("Net → Gross") })
                    }
                    item {
                        FilterChip(selected = gstDir == "gross", onClick = { gstDir = "gross" }, label = { Text("Gross → Net") })
                    }
                }
                if (gstDir == "net") {
                    NumField(gstNet, { gstNet = it }, "Net amount")
                } else {
                    NumField(gstGross, { gstGross = it }, "Gross amount")
                }
                HelperCaption(if (gstDir == "net") "Enter net only — gross is computed." else "Enter gross only — net is computed. Intra/inter split is honored in both directions.")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(gstRates) { r ->
                        FilterChip(
                            selected = gstCustom.toDoubleOrNull() == null && gstRate == r,
                            onClick = { gstRate = r; gstCustom = "" },
                            label = { Text("${fmt(r, 0)}%") }
                        )
                    }
                }
                NumField(gstCustom, { gstCustom = it }, "Custom rate % (optional)")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = gstIntra, onClick = { gstIntra = true }, label = { Text("Intra-state") })
                    }
                    item {
                        FilterChip(selected = !gstIntra, onClick = { gstIntra = false }, label = { Text("Inter-state") })
                    }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Net", gstRes?.let { fmt(it.first, 2) } ?: "—")
                if (gstIntra) {
                    ToolResultRow(Icons.Filled.AttachMoney, "CGST", runCatching { fmt(gstTax / 2, 2) }.getOrDefault("—"))
                    ToolResultRow(Icons.Filled.AttachMoney, "SGST", runCatching { fmt(gstTax / 2, 2) }.getOrDefault("—"))
                } else {
                    ToolResultRow(Icons.Filled.AttachMoney, "IGST", runCatching { fmt(gstTax, 2) }.getOrDefault("—"))
                }
                ToolResultRow(Icons.Filled.AttachMoney, "Gross", gstRes?.let { fmt(it.third, 2) } ?: "—")
            }
        }
        item {
            var lpP by remember { mutableStateOf("100000") }
            var lpR by remember { mutableStateOf("9") }
            var lpM by remember { mutableStateOf("60") }
            val lpMonths = (lpM.toIntOrNull() ?: 0).coerceIn(1, 360)
            val lpPrin = num(lpP)
            val lpEmi = runCatching { Finance.emi(lpPrin, num(lpR), lpMonths) }.getOrNull()
            val lpInt = if (lpEmi == null) Double.NaN else runCatching { lpEmi * lpMonths - lpPrin }.getOrDefault(Double.NaN)
            SectionCard("Loan breakdown") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(lpP, { lpP = it }, "Principal") }
                    Box(Modifier.weight(1f)) { NumField(lpR, { lpR = it }, "Annual %") }
                }
                NumField(lpM, { lpM = it }, "Months", integer = true)
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Monthly EMI", lpEmi?.let { fmt(it, 2) } ?: "—")
                ToolResultRow(Icons.Filled.AttachMoney, "Total interest", runCatching { fmt(lpInt, 2) }.getOrDefault("—"))
                PieChart(
                    listOf("Principal" to lpPrin.coerceAtLeast(0.0), "Interest" to lpInt.coerceAtLeast(0.0)),
                    "Monthly",
                    lpEmi?.let { fmt(it, 2) } ?: "—"
                )
            }
        }
        item {
            var bdP by remember { mutableStateOf("10000") }
            var bdR by remember { mutableStateOf("6") }
            var bdY by remember { mutableStateOf("5") }
            var bdMode by remember { mutableStateOf(1) }
            val bdLabels = listOf("Monthly", "Quarterly", "Half-yearly", "Yearly", "Lump sum")
            val bdFreqs = listOf(12, 4, 2, 1, 0)
            val bdFreq = bdFreqs.getOrNull(bdMode) ?: 4
            val bdRes = runCatching {
                if (bdFreq == 0) {
                    val (si, total) = Finance.simple(num(bdP), num(bdR), num(bdY))
                    Triple(num(bdP), si, total)
                } else {
                    Finance.fd(num(bdP), num(bdR), num(bdY), bdFreq)
                }
            }.getOrNull()
            SectionCard("Bank deposit") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(bdP, { bdP = it }, "Principal") }
                    Box(Modifier.weight(1f)) { NumField(bdR, { bdR = it }, "Rate %") }
                    Box(Modifier.weight(1f)) { NumField(bdY, { bdY = it }, "Years") }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bdLabels.size) { i ->
                        FilterChip(selected = bdMode == i, onClick = { bdMode = i }, label = { Text(bdLabels[i]) })
                    }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.AttachMoney, "Interest", bdRes?.let { fmt(it.second, 2) } ?: "—")
                ToolResultRow(Icons.Filled.AttachMoney, "Maturity", bdRes?.let { fmt(it.third, 2) } ?: "—")
                PieChart(
                    listOf(
                        "Principal" to (bdRes?.first ?: 0.0).coerceAtLeast(0.0),
                        "Interest" to (bdRes?.second ?: 0.0).coerceAtLeast(0.0)
                    ),
                    "Maturity",
                    bdRes?.let { fmt(it.third, 2) } ?: "—"
                )
            }
        }
        item {
            Box(Modifier.height(72.dp))
        }
    }
        BottomBackChevron(onBack = { onNavigate("tools") }, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
        JumpToCalcFab(onJump = { onNavigate("calc") }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))
    }
}

@Composable
fun MathScreen(onNavigate: (String) -> Unit = {}) {
    Box(Modifier.fillMaxSize()) {
    var tab by remember { mutableStateOf("numbers") }
    val tabs = listOf("numbers" to "Numbers", "geometry" to "Geometry", "health" to "Health")
    Column(
        Modifier.fillMaxSize().padding(vertical = 16.dp).padding(bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                "health" -> HealthScreen(onNavigate = onNavigate)
            }
        }
    }
        BottomBackChevron(onBack = { onNavigate("tools") }, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
        JumpToCalcFab(onJump = { onNavigate("calc") }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))
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
    var la by remember { mutableStateOf("1") }
    var lb by remember { mutableStateOf("-2") }
    var ohmV by remember { mutableStateOf("") }
    var ohmI by remember { mutableStateOf("") }
    var ohmR by remember { mutableStateOf("") }
    val av = a.toLongOrNull() ?: 0L
    val bv = b.toLongOrNull() ?: 0L
    val values = parseList(listInput)
    val sorted = values.sorted()
    val statValue: String = runCatching {
        when (stat) {
            "Median" -> if (sorted.isEmpty()) "—" else fmt(
                if (sorted.size % 2 == 1) sorted.getOrNull(sorted.size / 2) ?: return@runCatching "—"
                else ((sorted.getOrNull(sorted.size / 2 - 1) ?: return@runCatching "—") + (sorted.getOrNull(sorted.size / 2) ?: return@runCatching "—")) / 2
            )
            "Min" -> sorted.firstOrNull()?.let { fmt(it) } ?: "—"
            "Max" -> sorted.lastOrNull()?.let { fmt(it) } ?: "—"
            "Sum" -> if (values.isEmpty()) "—" else fmt(values.sum())
            "Count" -> "${values.size}"
            else -> if (values.isEmpty()) "—" else fmt(values.sum() / values.size.coerceAtLeast(1))
        }
    }.getOrDefault("—")
    val qav = num(qa)
    val qbv = num(qb)
    val qcv = num(qc)
    val roots = runCatching { Engine.solveQuadratic(qav, qbv, qcv) }.getOrDefault(emptyList())
    val lav = num(la)
    val lbv = num(lb)
    val linRes = when {
        !lav.isFinite() || !lbv.isFinite() -> "—"
        lav == 0.0 -> if (lbv == 0.0) "any x" else "no solution"
        else -> "x=" + fmt(-lbv / lav, 6)
    }
    val ohmVi = ohmV.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
    val ohmCi = ohmI.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
    val ohmRi = ohmR.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
    val (ohmVo, ohmCo, ohmRo) = runCatching { HealthDate.ohm(ohmVi, ohmCi, ohmRi) }
        .getOrDefault(Triple(ohmVi, ohmCi, ohmRi))
    val m1 = num(a1)
    val n1 = num(b1)
    val o1 = num(c1)
    val m2 = num(a2)
    val n2 = num(b2)
    val o2 = num(c2)
    val det = m1 * n2 - m2 * n1
    val sys = if (!det.isFinite() || det == 0.0) null else runCatching { Pair((o1 * n2 - o2 * n1) / det, (m1 * o2 - m2 * o1) / det) }.getOrNull()
    val fn = fracN.toLongOrNull()
    val fd = fracD.toLongOrNull()
    val frac: String = if (fn == null || fd == null || fd == 0L) "—" else runCatching {
        val g = Engine.gcd(fn, fd)
        if (g == 0L) return@runCatching "—"
        val rn = fn / g
        val rd = fd / g
        val sign = if (rd < 0) "-" else ""
        "$sign${kotlin.math.abs(rn)}/${kotlin.math.abs(rd)} = ${fmt(fn.toDouble() / fd.toDouble(), 6)}"
    }.getOrDefault("—")
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Number theory") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(a, { a = it }, "a", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(b, { b = it }, "b", integer = true) }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "GCD", runCatching { "${Engine.gcd(av, bv)}" }.getOrDefault("—"))
                ToolResultRow(Icons.Filled.Info, "LCM", "${runCatching { Engine.lcm(av, bv) }.getOrDefault(0)}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimePill("a", runCatching { Engine.isPrime(av) }.getOrDefault(false))
                    PrimePill("b", runCatching { Engine.isPrime(bv) }.getOrDefault(false))
                }
                ToolResultRow(Icons.Filled.Info, "nCr", "${runCatching { Engine.nCr(av, bv) }.getOrDefault(0)}")
                ToolResultRow(Icons.Filled.Info, "nPr", "${runCatching { Engine.nPr(av, bv) }.getOrDefault(0)}")
                ToolResultRow(Icons.Filled.Info, "a!", factorial(av)?.toString() ?: "too large")
            }
        }
        item {
            SectionCard("Percent and discount") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(pctP, { pctP = it }, "%") }
                    Box(Modifier.weight(1f)) { NumField(pctX, { pctX = it }, "of value") }
                }
                HelperCaption("Result = of value · % / 100.")
                ToolResultRow(Icons.Filled.Percent, "Result", fmt(num(pctX) * num(pctP) / 100))
                HorizontalDivider()
                var discMode by remember { mutableStateOf("Discount") }
                MethodDropdown(
                    selected = discMode,
                    options = listOf(
                        "Discount" to "final = price − save",
                        "Increase" to "final = price + extra"
                    ),
                    onSelect = { discMode = it },
                    label = "Method"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(discPrice, { discPrice = it }, "Price") }
                    Box(Modifier.weight(1f)) { NumField(discPct, { discPct = it }, if (discMode == "Discount") "Off %" else "Add %") }
                }
                HelperCaption(if (discMode == "Discount") "Save = price · % / 100." else "Extra = price · % / 100.")
                val save = num(discPrice) * num(discPct) / 100
                ToolResultRow(Icons.Filled.AttachMoney, if (discMode == "Discount") "You save" else "Extra", fmt(save, 2))
                ToolResultRow(Icons.Filled.AttachMoney, "Final price", fmt(if (discMode == "Discount") num(discPrice) - save else num(discPrice) + save, 2))
            }
        }
        item {
            var stdevMode by remember { mutableStateOf("Population") }
            val rawTokens = remember(listInput) {
                listInput.split(",", ";", " ", "\n").map { it.trim() }.filter { it.isNotEmpty() }
            }
            val parsedVals = remember(rawTokens) {
                rawTokens.mapNotNull { it.toDoubleOrNull() }
            }
            val badTokens = remember(rawTokens) {
                rawTokens.filter { it.toDoubleOrNull() == null }
            }
            val mVals = parsedVals
            val mSorted = mVals.sorted()
            val mMean = runCatching { Engine.mean(mVals) }.getOrNull()
            val mMedian = runCatching { Engine.statsMedian(mVals) }.getOrNull()
            val mMode = runCatching { Engine.statsMode(mVals) }.getOrNull()
            val mVarPop = runCatching { Engine.statsVariance(mVals) }.getOrNull()
            val mStdevPop = runCatching { Engine.statsStdev(mVals) }.getOrNull()
            val mVar = if (stdevMode == "Sample") {
                if (mVals.size > 1 && mMean != null) {
                    runCatching {
                        mVals.sumOf { (it - mMean) * (it - mMean) } / (mVals.size - 1)
                    }.getOrNull()
                } else null
            } else mVarPop
            val mStdev = if (stdevMode == "Sample") {
                mVar?.let { runCatching { sqrt(it) }.getOrNull() }
            } else mStdevPop
            SectionCard("Statistics") {
                NumField(listInput, { listInput = it }, "Values, comma separated")
                HelperCaption("Separate values with commas, spaces, or new lines.")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = stdevMode == "Population", onClick = { stdevMode = "Population" }, label = { Text("Population") })
                    }
                    item {
                        FilterChip(selected = stdevMode == "Sample", onClick = { stdevMode = "Sample" }, label = { Text("Sample") })
                    }
                }
                HelperCaption(if (stdevMode == "Population") "Stdev ÷ N." else "Stdev ÷ (N − 1). Needs ≥ 2 values.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "Mean", mMean?.let { fmt(it) } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Median", mMedian?.let { fmt(it) } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Mode", mMode?.let { fmt(it) } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Stdev ($stdevMode)", mStdev?.let { fmt(it) } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Variance ($stdevMode)", mVar?.let { fmt(it) } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Min", mSorted.firstOrNull()?.let { fmt(it) } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Max", mSorted.lastOrNull()?.let { fmt(it) } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Count", "${mVals.size}")
                if (badTokens.isNotEmpty()) {
                    ToolErrorLine("Invalid token(s): " + badTokens.joinToString(", "))
                } else if (mVals.isEmpty()) {
                    ToolErrorLine("Enter at least one number.")
                } else if (stdevMode == "Sample" && mVals.size < 2) {
                    ToolErrorLine("Sample stdev needs at least 2 values.")
                }
            }
        }
        item {
            SectionCard("Linear solver") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) { NumField(la, { la = it }, "a") }
                    Text("x +", style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.weight(1f)) { NumField(lb, { lb = it }, "b") }
                    Text("= 0", style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "Solution", linRes)
            }
        }
        item {
            SectionCard("Quadratic solver") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) { NumField(qa, { qa = it }, "a") }
                    Text("x² +", style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.weight(1f)) { NumField(qb, { qb = it }, "b") }
                    Text("x +", style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.weight(1f)) { NumField(qc, { qc = it }, "c") }
                    Text("= 0", style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "Roots", if (roots.isEmpty()) "—" else roots.joinToString())
            }
        }
        item {
            SectionCard("2x2 system solver") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) { NumField(a1, { a1 = it }, "a1") }
                    Text("x +", style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.weight(1f)) { NumField(b1, { b1 = it }, "b1") }
                    Text("y =", style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.weight(1f)) { NumField(c1, { c1 = it }, "c1") }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) { NumField(a2, { a2 = it }, "a2") }
                    Text("x +", style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.weight(1f)) { NumField(b2, { b2 = it }, "b2") }
                    Text("y =", style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.weight(1f)) { NumField(c2, { c2 = it }, "c2") }
                }
                HorizontalDivider()
                ToolResultRow(
                    Icons.Filled.Info,
                    "Solution",
                    if (sys == null) "no unique solution" else "x=${fmt(sys.first, 6)}, y=${fmt(sys.second, 6)}"
                )
            }
        }
        item {
            SectionCard("Ohm's law") {
                Text(
                    "Fill any two to find the third",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(ohmV, { ohmV = it }, "Volts") }
                    Box(Modifier.weight(1f)) { NumField(ohmI, { ohmI = it }, "Amps") }
                    Box(Modifier.weight(1f)) { NumField(ohmR, { ohmR = it }, "Ohms") }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "Voltage", ohmVo?.let { fmt(it, 4) + " V" } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Current", ohmCo?.let { fmt(it, 4) + " A" } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Resistance", ohmRo?.let { fmt(it, 4) + " Ω" } ?: "—")
            }
        }
        item {
            SectionCard("Fraction simplifier") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(fracN, { fracN = it }, "Numerator", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(fracD, { fracD = it }, "Denominator", integer = true) }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "Reduced", frac)
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
                ToolResultRow(Icons.Filled.Info, "det", m?.let { runCatching { fmt(it.determinant()) }.getOrDefault("—") } ?: "—")
                ToolResultRow(
                    Icons.Filled.Info,
                    "transpose",
                    runCatching {
                        m?.transpose()?.let { t -> "${fmt(t[0, 0])}, ${fmt(t[0, 1])} / ${fmt(t[1, 0])}, ${fmt(t[1, 1])}" } ?: "—"
                    }.getOrDefault("—")
                )
                ToolResultRow(
                    Icons.Filled.Info,
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
                ToolResultRow(Icons.Filled.Info, "Roots", if (roots.isEmpty()) "—" else roots.joinToString())
            }
        }
        item {
            var listMode by remember { mutableStateOf("Manual") }
            var statVals by remember { mutableStateOf(listOf("10", "20", "30")) }
            var pasteInput by remember { mutableStateOf("10\n20\n30") }
            val parsed = remember(statVals) { statVals.mapNotNull { it.trim().toDoubleOrNull() } }
            val pasteTokens = remember(pasteInput) {
                pasteInput.split(",", ";", " ", "\n").map { it.trim() }.filter { it.isNotEmpty() }
            }
            val pasteBad = remember(pasteTokens) { pasteTokens.filter { it.toDoubleOrNull() == null } }
            val pasteVals = remember(pasteTokens) { pasteTokens.mapNotNull { it.toDoubleOrNull() } }
            val activeVals = if (listMode == "Paste") pasteVals else parsed
            val manualBad = remember(statVals) {
                statVals.mapIndexedNotNull { idx, v ->
                    val t = v.trim()
                    if (t.isEmpty() || t.toDoubleOrNull() == null) "Row ${idx + 1} ('$v')" else null
                }
            }
            SectionCard("List statistics") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = listMode == "Manual", onClick = { listMode = "Manual" }, label = { Text("Manual") })
                    }
                    item {
                        FilterChip(selected = listMode == "Paste", onClick = { listMode = "Paste" }, label = { Text("Paste list") })
                    }
                }
                if (listMode == "Manual") {
                    statVals.forEachIndexed { i, v ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.weight(1f)) {
                                NumField(
                                    v,
                                    { nv -> statVals = statVals.toMutableList().also { it[i] = nv } },
                                    "Value ${i + 1}"
                                )
                            }
                            if (statVals.size > 1) {
                                TextButton(onClick = { statVals = statVals.filterIndexed { j, _ -> j != i } }) { Text("X") }
                            }
                        }
                    }
                    Button(onClick = { statVals = statVals + "" }) { Text("Add value") }
                } else {
                    OutlinedTextField(
                        value = pasteInput,
                        onValueChange = { pasteInput = it },
                        label = { Text("Paste values (multiline)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )
                    HelperCaption("Separators: commas, spaces, or new lines.")
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "Count", "${activeVals.size}")
                ToolResultRow(Icons.Filled.Info, "Mean", runCatching { fmt(Engine.mean(activeVals)) }.getOrDefault("—"))
                ToolResultRow(Icons.Filled.Info, "Median", runCatching { fmt(Engine.statsMedian(activeVals)) }.getOrDefault("—"))
                ToolResultRow(Icons.Filled.Info, "Stdev", runCatching { fmt(Engine.statsStdev(activeVals)) }.getOrDefault("—"))
                if (listMode == "Paste" && pasteBad.isNotEmpty()) {
                    ToolErrorLine("Invalid token(s): " + pasteBad.joinToString(", "))
                }
                if (listMode == "Manual" && manualBad.isNotEmpty()) {
                    ToolErrorLine("Invalid row(s): " + manualBad.joinToString(", "))
                }
                if (activeVals.isEmpty()) {
                    ToolErrorLine("Enter at least one valid number.")
                }
                val activeLongs = remember(activeVals) {
                    activeVals.map {
                        if (it == kotlin.math.floor(it) && it >= Long.MIN_VALUE.toDouble() && it <= Long.MAX_VALUE.toDouble()) it.toLong() else null
                    }
                }
                val activeIntegral = activeVals.isNotEmpty() && activeLongs.all { it != null }
                if (activeIntegral) {
                    val intVals = activeLongs.filterNotNull()
                    ToolResultRow(
                        Icons.Filled.Info,
                        "GCD",
                        runCatching { "${intVals.reduce { x, y -> Engine.gcd(x, y) }}" }.getOrDefault("—")
                    )
                    ToolResultRow(
                        Icons.Filled.Info,
                        "LCM",
                        runCatching { "${intVals.reduce { x, y -> Engine.lcm(x, y) }}" }.getOrDefault("—")
                    )
                }
            }
        }
        item {
            var cq by remember { mutableStateOf("") }
            val clipboard = LocalClipboardManager.current
            val hits = remember(cq) { runCatching { Constants.search(cq).take(30) }.getOrDefault(emptyList()) }
            SectionCard("Constants") {
                OutlinedTextField(
                    value = cq,
                    onValueChange = { cq = it },
                    label = { Text("Search constants") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                HelperCaption("Tap a row to copy its value · ${Constants.all.size} constants with units shown.")
                HorizontalDivider()
                hits.forEach { c ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            runCatching { clipboard.setText(AnnotatedString(c.value.toString())) }
                        }.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${c.symbol} · ${c.name}", style = MaterialTheme.typography.bodyMedium)
                            Text("tap to copy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(fmt(c.value, 6), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (hits.isEmpty()) ToolResultRow(Icons.Filled.Info, "No match", "—")
            }
        }
        item {
            var sw by remember { mutableStateOf("1920") }
            var sh by remember { mutableStateOf("1080") }
            var diag by remember { mutableStateOf("6.1") }
            var ppiIn by remember { mutableStateOf("400") }
            var screenMode by remember { mutableStateOf("PPI from diagonal") }
            val wInt = sw.trim().toIntOrNull()
            val hInt = sh.trim().toIntOrNull()
            val dVal = diag.trim().toDoubleOrNull()
            val ppiVal = ppiIn.trim().toDoubleOrNull()
            val screenErr: String? = when {
                wInt == null || hInt == null -> "Width and height must be whole numbers."
                wInt <= 0 || hInt <= 0 -> "Width and height must be > 0."
                screenMode == "PPI from diagonal" && (dVal ?: 0.0) <= 0.0 -> "Diagonal must be a positive number."
                screenMode == "Diagonal from PPI" && (ppiVal ?: 0.0) <= 0.0 -> "PPI must be a positive number."
                else -> null
            }
            val aspect = runCatching {
                if (wInt == null || hInt == null) throw IllegalArgumentException("Width and height must be whole numbers.")
                ScreenKit.aspectRatio(wInt, hInt)
            }
            val ppiRes = runCatching {
                if (wInt == null || hInt == null) throw IllegalArgumentException("Width and height must be whole numbers.")
                if (wInt <= 0 || hInt <= 0) throw IllegalArgumentException("Width and height must be > 0.")
                if (dVal == null) throw IllegalArgumentException("Diagonal must be a number.")
                if (dVal <= 0.0) throw IllegalArgumentException("Diagonal must be > 0.")
                ScreenKit.ppi(wInt, hInt, dVal)
            }
            val diagRes = runCatching {
                if (wInt == null || hInt == null) throw IllegalArgumentException("Width and height must be whole numbers.")
                if (wInt <= 0 || hInt <= 0) throw IllegalArgumentException("Width and height must be > 0.")
                if (ppiVal == null) throw IllegalArgumentException("PPI must be a number.")
                if (ppiVal <= 0.0) throw IllegalArgumentException("PPI must be > 0.")
                sqrt(wInt.toDouble() * wInt + hInt.toDouble() * hInt) / ppiVal
            }
            val shownPpi = if (screenMode == "PPI from diagonal") ppiRes.getOrNull() else ppiVal
            val dpiBucket = shownPpi?.let {
                when {
                    !it.isFinite() -> null
                    it < 120 -> "ldpi"
                    it < 160 -> "mdpi"
                    it < 240 -> "hdpi"
                    it < 320 -> "xhdpi"
                    it < 480 -> "xxhdpi"
                    else -> "xxxhdpi"
                }
            }
            SectionCard("Screen") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = screenMode == "PPI from diagonal", onClick = { screenMode = "PPI from diagonal" }, label = { Text("PPI from diagonal") })
                    }
                    item {
                        FilterChip(selected = screenMode == "Diagonal from PPI", onClick = { screenMode = "Diagonal from PPI" }, label = { Text("Diagonal from PPI") })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(sw, { sw = it }, "W px", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(sh, { sh = it }, "H px", integer = true) }
                    Box(Modifier.weight(1f)) {
                        if (screenMode == "PPI from diagonal") NumField(diag, { diag = it }, "Inch")
                        else NumField(ppiIn, { ppiIn = it }, "PPI")
                    }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "Aspect", aspect.getOrNull() ?: "—")
                if (screenMode == "PPI from diagonal") {
                    ToolResultRow(Icons.Filled.Info, "PPI", ppiRes.getOrNull()?.let { fmt(it, 1) } ?: "—")
                } else {
                    ToolResultRow(Icons.Filled.Info, "Diagonal", diagRes.getOrNull()?.let { fmt(it, 2) + " in" } ?: "—")
                }
                ToolResultRow(Icons.Filled.Info, "DPI bucket", dpiBucket ?: "—")
                ToolErrorLine(screenErr ?: aspect.exceptionOrNull()?.message ?: (if (screenMode == "PPI from diagonal") ppiRes.exceptionOrNull()?.message else diagRes.exceptionOrNull()?.message))
            }
        }
        item {
            var s11 by remember { mutableStateOf("2") }
            var s12 by remember { mutableStateOf("1") }
            var s13 by remember { mutableStateOf("-1") }
            var s21 by remember { mutableStateOf("-3") }
            var s22 by remember { mutableStateOf("-1") }
            var s23 by remember { mutableStateOf("2") }
            var s31 by remember { mutableStateOf("-2") }
            var s32 by remember { mutableStateOf("1") }
            var s33 by remember { mutableStateOf("2") }
            var sr1 by remember { mutableStateOf("8") }
            var sr2 by remember { mutableStateOf("-11") }
            var sr3 by remember { mutableStateOf("-3") }
            val grid = listOf(
                listOf(num(s11), num(s12), num(s13)),
                listOf(num(s21), num(s22), num(s23)),
                listOf(num(s31), num(s32), num(s33))
            )
            val rhs = listOf(num(sr1), num(sr2), num(sr3))
            val sol = runCatching { Engine.solve3x3(grid, rhs) }.getOrDefault(listOf("—"))
            SectionCard("3x3 system solver") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(s11, { s11 = it }, "a11") }
                    Box(Modifier.weight(1f)) { NumField(s12, { s12 = it }, "a12") }
                    Box(Modifier.weight(1f)) { NumField(s13, { s13 = it }, "a13") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(s21, { s21 = it }, "a21") }
                    Box(Modifier.weight(1f)) { NumField(s22, { s22 = it }, "a22") }
                    Box(Modifier.weight(1f)) { NumField(s23, { s23 = it }, "a23") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(s31, { s31 = it }, "a31") }
                    Box(Modifier.weight(1f)) { NumField(s32, { s32 = it }, "a32") }
                    Box(Modifier.weight(1f)) { NumField(s33, { s33 = it }, "a33") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(sr1, { sr1 = it }, "b1") }
                    Box(Modifier.weight(1f)) { NumField(sr2, { sr2 = it }, "b2") }
                    Box(Modifier.weight(1f)) { NumField(sr3, { sr3 = it }, "b3") }
                }
                HorizontalDivider()
                ToolResultRow(
                    Icons.Filled.Info,
                    "Solution",
                    if (sol.size == 3 && sol.getOrNull(0) != "no unique solution") "x=${sol.getOrNull(0)}, y=${sol.getOrNull(1)}, z=${sol.getOrNull(2)}"
                    else sol.joinToString()
                )
            }
        }
        item {
            var vax by remember { mutableStateOf("1") }
            var vay by remember { mutableStateOf("2") }
            var vaz by remember { mutableStateOf("3") }
            var vbx by remember { mutableStateOf("4") }
            var vby by remember { mutableStateOf("5") }
            var vbz by remember { mutableStateOf("6") }
            var vecDim by remember { mutableStateOf("3D") }
            val va = if (vecDim == "2D") listOf(num(vax), num(vay)) else listOf(num(vax), num(vay), num(vaz))
            val vb = if (vecDim == "2D") listOf(num(vbx), num(vby)) else listOf(num(vbx), num(vby), num(vbz))
            val magA = runCatching { VectorKit.magnitude(va) }.getOrNull()
            val magB = runCatching { VectorKit.magnitude(vb) }.getOrNull()
            val unitA = if (magA != null && magA.isFinite() && magA > 0) va.map { it / magA } else null
            val unitB = if (magB != null && magB.isFinite() && magB > 0) vb.map { it / magB } else null
            val proj = if (magB != null && magB.isFinite() && magB > 0) {
                runCatching {
                    val dot = VectorKit.dot(va, vb)
                    val scale = dot / (magB * magB)
                    vb.map { it * scale }
                }.getOrNull()
            } else null
            SectionCard("Vectors") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = vecDim == "2D", onClick = { vecDim = "2D" }, label = { Text("2D") })
                    }
                    item {
                        FilterChip(selected = vecDim == "3D", onClick = { vecDim = "3D" }, label = { Text("3D") })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(vax, { vax = it }, "ax") }
                    Box(Modifier.weight(1f)) { NumField(vay, { vay = it }, "ay") }
                    if (vecDim == "3D") Box(Modifier.weight(1f)) { NumField(vaz, { vaz = it }, "az") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(vbx, { vbx = it }, "bx") }
                    Box(Modifier.weight(1f)) { NumField(vby, { vby = it }, "by") }
                    if (vecDim == "3D") Box(Modifier.weight(1f)) { NumField(vbz, { vbz = it }, "bz") }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "Dot", runCatching { fmt(VectorKit.dot(va, vb)) }.getOrDefault("—"))
                if (vecDim == "3D") {
                    ToolResultRow(
                        Icons.Filled.Info,
                        "Cross",
                        runCatching { VectorKit.cross(va, vb).joinToString(prefix = "[", postfix = "]") { fmt(it) } }.getOrDefault("—")
                    )
                } else {
                    HelperCaption("Cross product is 3D only.")
                }
                ToolResultRow(Icons.Filled.Info, "Magnitude a", magA?.let { fmt(it) } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Magnitude b", magB?.let { fmt(it) } ?: "—")
                ToolResultRow(
                    Icons.Filled.Info,
                    "Unit a",
                    unitA?.joinToString(prefix = "[", postfix = "]") { fmt(it, 3) } ?: "—"
                )
                ToolResultRow(
                    Icons.Filled.Info,
                    "Unit b",
                    unitB?.joinToString(prefix = "[", postfix = "]") { fmt(it, 3) } ?: "—"
                )
                ToolResultRow(
                    Icons.Filled.Info,
                    "Proj a→b",
                    proj?.joinToString(prefix = "[", postfix = "]") { fmt(it, 3) } ?: "—"
                )
                ToolResultRow(
                    Icons.Filled.Info,
                    "Angle",
                    runCatching { fmt(VectorKit.angleDeg(va, vb), 2) + "°" }.getOrDefault("—")
                )
            }
        }
        item {
            var ch by remember { mutableStateOf("3") }
            var cmi by remember { mutableStateOf("15") }
            val hInt = ch.trim().toIntOrNull()
            val mInt = cmi.trim().toIntOrNull()
            val clockErr: String? = when {
                hInt == null -> "Hour must be a whole number 1..12."
                hInt !in 1..12 -> "Hour must be in 1..12 (got $hInt)."
                mInt == null -> "Minute must be a whole number 0..59."
                mInt !in 0..59 -> "Minute must be in 0..59 (got $mInt)."
                else -> null
            }
            val ang = runCatching {
                if (hInt == null || hInt !in 1..12) throw IllegalArgumentException("Hour must be in 1..12.")
                if (mInt == null || mInt !in 0..59) throw IllegalArgumentException("Minute must be in 0..59.")
                ClockAngle.angle(hInt, mInt)
            }
            SectionCard("Clock angle") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(ch, { ch = it }, "Hour 1..12", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(cmi, { cmi = it }, "Minute 0..59", integer = true) }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.DateRange, "Angle", ang.getOrNull()?.let { fmt(it, 2) + "°" } ?: "—")
                ToolErrorLine(clockErr ?: ang.exceptionOrNull()?.message)
            }
        }
        item {
            var ntN by remember { mutableStateOf("36") }
            var ntA by remember { mutableStateOf("7") }
            var ntM by remember { mutableStateOf("26") }
            var ntF by remember { mutableStateOf("20") }
            val nL = ntN.toLongOrNull()
            val fibInt = ntF.trim().toIntOrNull()
            val fibRes = runCatching {
                if (fibInt == null) throw IllegalArgumentException("fib n must be a whole number 0..92.")
                NumberTheory.fibonacci(fibInt)
            }
            val modRes = runCatching {
                val av = ntA.trim().toLongOrNull() ?: throw IllegalArgumentException("a must be a whole number.")
                val mv = ntM.trim().toLongOrNull() ?: throw IllegalArgumentException("m must be a whole number > 1.")
                NumberTheory.modInverse(av, mv)
            }
            SectionCard("Advanced number theory") {
                NumField(ntN, { ntN = it }, "n", integer = true)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(ntA, { ntA = it }, "a", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(ntM, { ntM = it }, "m", integer = true) }
                }
                NumField(ntF, { ntF = it }, "fib n (0..92)", integer = true)
                HorizontalDivider()
                ToolResultRow(
                    Icons.Filled.Info,
                    "Totient φ(n)",
                    nL?.let { runCatching { "${NumberTheory.totient(it)}" }.getOrDefault("—") } ?: "—"
                )
                ToolResultRow(
                    Icons.Filled.Info,
                    "Mod inverse a⁻¹ mod m",
                    modRes.getOrNull()?.toString() ?: "—"
                )
                if (modRes.isFailure) {
                    val msg = modRes.exceptionOrNull()?.message ?: ""
                    ToolErrorLine(if (msg.contains("no modular inverse", ignoreCase = true)) "no inverse (not coprime)" else msg.ifBlank { "no inverse (not coprime)" })
                }
                ToolResultRow(
                    Icons.Filled.Info,
                    "Prime factors",
                    nL?.let {
                        runCatching { NumberTheory.primeFactors(it).joinToString(" × ").ifEmpty { "—" } }.getOrDefault("—")
                    } ?: "—"
                )
                ToolResultRow(
                    Icons.Filled.Info,
                    "Fibonacci",
                    fibRes.getOrNull()?.toString() ?: "—"
                )
                ToolErrorLine(fibRes.exceptionOrNull()?.message?.let { "fib n must be in 0..92." })
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
    var cubeSolve by remember { mutableStateOf("Volume & Surface") }
    var sphereSolve by remember { mutableStateOf("Volume & Surface") }
    var cylSolve by remember { mutableStateOf("Volume & Surface") }
    var coneSolve by remember { mutableStateOf("Volume & Surface") }
    val shapes = listOf(
        "circle", "rectangle", "triangle", "sphere", "cylinder",
        "cone", "cube", "prism", "pyramid", "ellipse",
        "rightTriangle", "square", "rhombus", "pentagon", "hexagon", "arc",
        "pyramidFrustum", "conicalFrustum", "sphereCap", "sphereZone", "ellipsoid", "trapezoid"
    )
    val labels: List<String> = when (shape) {
        "circle" -> listOf("Radius")
        "sphere" -> if (sphereSolve == "Radius from Volume") listOf("Volume") else listOf("Radius")
        "rectangle" -> listOf("Width", "Height")
        "triangle" -> listOf("Base", "Height")
        "cylinder" -> if (cylSolve == "Height from Volume") listOf("Radius", "Volume") else listOf("Radius", "Height")
        "cone" -> if (coneSolve == "Height from Volume") listOf("Radius", "Volume") else listOf("Radius", "Height")
        "cube" -> when (cubeSolve) {
            "Side from Volume" -> listOf("Volume")
            "Side from Surface" -> listOf("Surface")
            else -> listOf("Side")
        }
        "prism" -> listOf("Width", "Height", "Depth")
        "pyramid" -> listOf("Base side", "Height")
        "ellipse" -> listOf("Semi-axis a", "Semi-axis b")
        "rightTriangle" -> listOf("Leg a", "Leg b")
        "square" -> listOf("Side")
        "rhombus" -> listOf("Diagonal 1", "Diagonal 2")
        "pentagon" -> listOf("Side")
        "hexagon" -> listOf("Side")
        "arc" -> listOf("Radius", "Degrees")
        "pyramidFrustum" -> listOf("Base a", "Top b", "Height")
        "conicalFrustum" -> listOf("Radius R", "Radius r", "Height")
        "sphereCap" -> listOf("Radius R", "Height")
        "sphereZone" -> listOf("Radius R", "Height")
        "ellipsoid" -> listOf("Axis a", "Axis b", "Axis c")
        "trapezoid" -> listOf("Base a", "Base b", "Height")
        else -> emptyList()
    }
    var geoUnit by remember { mutableStateOf("m") }
    var showGeoSteps by remember { mutableStateOf(false) }
    val toM = when (geoUnit) { "cm" -> 0.01; "inch" -> 0.0254; else -> 1.0 }
    val areaToM2 = toM * toM
    val volToM3 = toM * toM * toM
    val areaUnit = when (geoUnit) { "cm" -> "cm²"; "inch" -> "in²"; else -> "m²" }
    val volUnit = when (geoUnit) { "cm" -> "cm³"; "inch" -> "in³"; else -> "m³" }
    val dims = listOf(f1, f2, f3)
    val setters: List<(String) -> Unit> = listOf({ f1 = it }, { f2 = it }, { f3 = it })
    val xr = f1.trim().toDoubleOrNull()
    val yr = f2.trim().toDoubleOrNull()
    val zr = f3.trim().toDoubleOrNull()
    fun needPos(v: Double?, name: String): Double {
        if (v == null) throw IllegalArgumentException("$name must be a number > 0.")
        if (v <= 0.0) throw IllegalArgumentException("$name must be > 0.")
        return v
    }
    val geoErr = runCatching {
        when (shape) {
            "circle", "square", "pentagon", "hexagon" -> needPos(xr, labels.firstOrNull() ?: "Input")
            "sphere" -> if (sphereSolve == "Radius from Volume") {
                if (xr == null) throw IllegalArgumentException("Volume must be a number.")
                if (xr < 0.0) throw IllegalArgumentException("Volume must be >= 0.")
            } else needPos(xr, "Radius")
            "cube" -> when (cubeSolve) {
                "Side from Volume" -> { if (xr == null) throw IllegalArgumentException("Volume must be a number."); if (xr < 0.0) throw IllegalArgumentException("Volume must be >= 0.") }
                "Side from Surface" -> { if (xr == null) throw IllegalArgumentException("Surface must be a number."); if (xr < 0.0) throw IllegalArgumentException("Surface must be >= 0.") }
                else -> needPos(xr, "Side")
            }
            "arc" -> { needPos(xr, "Radius"); if (yr == null) throw IllegalArgumentException("Degrees must be a number.") }
            else -> {
                val n = labels.size
                if (n >= 1) needPos(xr, labels.getOrNull(0) ?: "Input 1")
                if (n >= 2 && shape != "arc") needPos(yr, labels.getOrNull(1) ?: "Input 2")
                if (n >= 3) needPos(zr, labels.getOrNull(2) ?: "Input 3")
                if (shape == "sphereCap" || shape == "sphereZone") {
                    val r = xr ?: 0.0; val hh = yr ?: 0.0
                    if (hh > 2 * r) throw IllegalArgumentException("Height must be ≤ 2R.")
                }
            }
        }
    }.exceptionOrNull()?.message
    val x = num(f1)
    val y = num(f2)
    val z = num(f3)
    val geoFormula: String = when (shape) {
        "circle" -> "Steps: A = πr², C = 2πr."
        "rectangle" -> "Steps: A = w·h, P = 2(w+h)."
        "triangle" -> "Steps: A = b·h/2. Perimeter needs all 3 sides — see Triangle SSS."
        "sphere" -> "Steps: V = 4/3πr³, S = 4πr²."
        "cylinder" -> "Steps: V = πr²h, S = 2πr(r+h)."
        "cone" -> "Steps: V = πr²h/3, S = πr(r+√(r²+h²))."
        "cube" -> "Steps: V = s³, S = 6s²."
        "prism" -> "Steps: V = w·h·d, S = 2(wh+wd+hd)."
        "pyramid" -> "Steps: V = b²h/3."
        "ellipse" -> "Steps: A = πab, P ≈ Ramanujan."
        "rightTriangle" -> "Steps: c = √(a²+b²), A = ab/2, P = a+b+c."
        "square" -> "Steps: A = s², P = 4s, d = s√2."
        "rhombus" -> "Steps: A = d1·d2/2, side = √((d1/2)²+(d2/2)²), P = 4·side."
        "pentagon" -> "Steps: A ≈ 1.721·s², P = 5s."
        "hexagon" -> "Steps: A = 3√3/2·s², P = 6s."
        "arc" -> "Steps: L = πr·deg/180, sector = πr²·deg/360."
        "trapezoid" -> "Steps: A = (a+b)/2·h. Perimeter needs all 4 sides."
        "ellipsoid" -> "Steps: V = 4/3πabc."
        else -> "Steps: see formula."
    }
    val outputs: List<Pair<String, String>> = runCatching {
        if (geoErr != null) throw IllegalArgumentException(geoErr)
        when (shape) {
            "circle" -> {
                val a = Geometry.circleArea(x); val c = Geometry.circleCirc(x)
                listOf("Area ($areaUnit)" to fmt(a, 2), "Area (m²)" to fmt(a * areaToM2, 2), "Circumference ($geoUnit)" to fmt(c, 2))
            }
            "rectangle" -> {
                val a = Geometry.rectArea(x, y)
                listOf("Area ($areaUnit)" to fmt(a, 2), "Area (m²)" to fmt(a * areaToM2, 2), "Perimeter ($geoUnit)" to fmt(2 * (x + y), 2))
            }
            "triangle" -> {
                val a = Geometry.triangleArea(x, y)
                listOf("Area ($areaUnit)" to fmt(a, 2), "Area (m²)" to fmt(a * areaToM2, 2))
            }
            "sphere" -> if (sphereSolve == "Radius from Volume") {
                val r = runCatching { Geometry.sphereRFromVol(x) }.getOrDefault(Double.NaN)
                val surf = runCatching { Geometry.sphereArea(r) }.getOrDefault(Double.NaN)
                listOf("Radius ($geoUnit)" to fmt(r, 2), "Surface ($areaUnit)" to fmt(surf, 2), "Surface (m²)" to fmt(surf * areaToM2, 2))
            } else {
                val v = Geometry.sphereVolume(x); val s = Geometry.sphereArea(x)
                listOf("Volume ($volUnit)" to fmt(v, 2), "Volume (m³)" to fmt(v * volToM3, 2), "Surface ($areaUnit)" to fmt(s, 2), "Surface (m²)" to fmt(s * areaToM2, 2))
            }
            "cylinder" -> if (cylSolve == "Height from Volume") {
                val h = runCatching {
                    require(x > 0) { "r must be > 0" }
                    y / (PI * x * x)
                }.getOrDefault(Double.NaN)
                listOf("Height ($geoUnit)" to fmt(h, 2))
            } else {
                val v = Geometry.cylinderVolume(x, y); val s = 2 * PI * x * (x + y)
                listOf("Volume ($volUnit)" to fmt(v, 2), "Volume (m³)" to fmt(v * volToM3, 2), "Surface ($areaUnit)" to fmt(s, 2), "Surface (m²)" to fmt(s * areaToM2, 2))
            }
            "cone" -> if (coneSolve == "Height from Volume") {
                listOf("Height ($geoUnit)" to fmt(runCatching { Geometry.coneHFromVol(x, y) }.getOrDefault(Double.NaN), 2))
            } else {
                val v = Geometry.coneVolume(x, y); val s = PI * x * (x + sqrt(x * x + y * y))
                listOf("Volume ($volUnit)" to fmt(v, 2), "Volume (m³)" to fmt(v * volToM3, 2), "Surface ($areaUnit)" to fmt(s, 2), "Surface (m²)" to fmt(s * areaToM2, 2))
            }
            "cube" -> when (cubeSolve) {
                "Side from Volume" -> listOf(
                    "Side ($geoUnit)" to fmt(runCatching { Geometry.cubeSideFromVol(x) }.getOrDefault(Double.NaN), 2)
                )
                "Side from Surface" -> listOf(
                    "Side ($geoUnit)" to fmt(runCatching {
                        require(x >= 0) { "area must be >= 0" }
                        sqrt(x / 6)
                    }.getOrDefault(Double.NaN), 2)
                )
                else -> {
                    val v = Geometry.cubeVolume(x); val s = 6 * x * x
                    listOf("Volume ($volUnit)" to fmt(v, 2), "Volume (m³)" to fmt(v * volToM3, 2), "Surface ($areaUnit)" to fmt(s, 2), "Surface (m²)" to fmt(s * areaToM2, 2))
                }
            }
            "prism" -> {
                val v = Geometry.prismVolume(x, y, z); val s = 2 * (x * y + x * z + y * z)
                listOf("Volume ($volUnit)" to fmt(v, 2), "Volume (m³)" to fmt(v * volToM3, 2), "Surface ($areaUnit)" to fmt(s, 2), "Surface (m²)" to fmt(s * areaToM2, 2))
            }
            "pyramid" -> {
                val v = Geometry.pyramidVolume(x, y); val s = x * x + 2 * x * sqrt((x / 2) * (x / 2) + y * y)
                listOf("Volume ($volUnit)" to fmt(v, 2), "Volume (m³)" to fmt(v * volToM3, 2), "Surface ($areaUnit)" to fmt(s, 2), "Surface (m²)" to fmt(s * areaToM2, 2))
            }
            "ellipse" -> {
                val a = fmt(Geometry.ellipseArea(x, y), 2)
                val p = fmt(runCatching { Geometry.ellipsePerim(x, y) }.getOrDefault(Double.NaN), 2)
                val aM2 = fmt(runCatching { Geometry.ellipseArea(x, y) }.getOrDefault(Double.NaN) * areaToM2, 2)
                listOf("Area ($areaUnit)" to a, "Area (m²)" to aM2, "Perimeter ($geoUnit)" to p)
            }
            "rightTriangle" -> runCatching {
                val (hyp, area, per) = Geometry.rightTriangle(x, y)
                listOf("Hypotenuse ($geoUnit)" to fmt(hyp, 2), "Area ($areaUnit)" to fmt(area, 2), "Area (m²)" to fmt(area * areaToM2, 2), "Perimeter ($geoUnit)" to fmt(per, 2))
            }.getOrDefault(listOf("Hypotenuse" to "—", "Area" to "—", "Perimeter" to "—"))
            "square" -> runCatching {
                val (area, per, diag) = Geometry.square(x)
                listOf("Area ($areaUnit)" to fmt(area, 2), "Area (m²)" to fmt(area * areaToM2, 2), "Perimeter ($geoUnit)" to fmt(per, 2), "Diagonal ($geoUnit)" to fmt(diag, 2))
            }.getOrDefault(listOf("Area" to "—", "Perimeter" to "—", "Diagonal" to "—"))
            "rhombus" -> {
                val area = runCatching { Geometry.rhombusAreaD(x, y) }.getOrDefault(Double.NaN)
                val side = runCatching { sqrt((x / 2) * (x / 2) + (y / 2) * (y / 2)) }.getOrDefault(Double.NaN)
                listOf("Area ($areaUnit)" to fmt(area, 2), "Area (m²)" to fmt(area * areaToM2, 2), "Side ($geoUnit)" to fmt(side, 2), "Perimeter ($geoUnit)" to fmt(side * 4, 2))
            }
            "pentagon" -> {
                val area = runCatching { Geometry.pentagonArea(x) }.getOrDefault(Double.NaN)
                listOf("Area ($areaUnit)" to fmt(area, 2), "Area (m²)" to fmt(area * areaToM2, 2), "Perimeter ($geoUnit)" to fmt(5 * x, 2))
            }
            "hexagon" -> {
                val area = runCatching { Geometry.hexagonArea(x) }.getOrDefault(Double.NaN)
                listOf("Area ($areaUnit)" to fmt(area, 2), "Area (m²)" to fmt(area * areaToM2, 2), "Perimeter ($geoUnit)" to fmt(6 * x, 2))
            }
            "arc" -> listOf(
                "Arc length" to fmt(runCatching { Geometry.arcLength(x, y) }.getOrDefault(Double.NaN), 2),
                "Sector area" to fmt(runCatching { Geometry.sectorArea(x, y) }.getOrDefault(Double.NaN), 2)
            )
            "pyramidFrustum" -> runCatching {
                val (slant, vol, lat) = Geometry.pyramidFrustum(x, y, z)
                listOf("Slant" to fmt(slant, 2), "Volume" to fmt(vol, 2), "Lateral area" to fmt(lat, 2))
            }.getOrDefault(listOf("Slant" to "—", "Volume" to "—", "Lateral area" to "—"))
            "conicalFrustum" -> runCatching {
                val (slant, vol, lat) = Geometry.conicalFrustum(x, y, z)
                listOf("Slant" to fmt(slant, 2), "Volume" to fmt(vol, 2), "Lateral area" to fmt(lat, 2))
            }.getOrDefault(listOf("Slant" to "—", "Volume" to "—", "Lateral area" to "—"))
            "sphereCap" -> runCatching {
                val (baseR, vol, curved) = Geometry.sphereCap(x, y)
                listOf("Base radius" to fmt(baseR, 2), "Volume" to fmt(vol, 2), "Curved area" to fmt(curved, 2))
            }.getOrDefault(listOf("Base radius" to "—", "Volume" to "—", "Curved area" to "—"))
            "sphereZone" -> listOf("Curved area ($areaUnit)" to fmt(runCatching { Geometry.sphereZone(x, y) }.getOrDefault(Double.NaN), 2), "Curved area (m²)" to fmt(runCatching { Geometry.sphereZone(x, y) }.getOrDefault(Double.NaN) * areaToM2, 2))
            "ellipsoid" -> {
                val v = runCatching { Geometry.ellipsoidVol(x, y, z) }.getOrDefault(Double.NaN)
                listOf(
                    "Volume ($volUnit)" to fmt(v, 2),
                    "Volume (m³)" to fmt(v * volToM3, 2),
                    "Surface ($areaUnit)" to fmt(runCatching { Geometry.ellipsoidSurf(x, y, z) }.getOrDefault(Double.NaN), 2)
                )
            }
            "trapezoid" -> listOf("Area ($areaUnit)" to fmt(Geometry.trapezoidArea(x, y, z), 2), "Area (m²)" to fmt(Geometry.trapezoidArea(x, y, z) * areaToM2, 2))
            else -> emptyList()
        }
    }.getOrDefault(listOf("Result" to "—"))
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                UnitDropdown(geoUnit, listOf("m", "cm", "inch"), { geoUnit = it }, "Unit")
                when (shape) {
                    "cube" -> UnitDropdown(cubeSolve, listOf("Volume & Surface", "Side from Volume", "Side from Surface"), { cubeSolve = it }, "Solve for")
                    "sphere" -> UnitDropdown(sphereSolve, listOf("Volume & Surface", "Radius from Volume"), { sphereSolve = it }, "Solve for")
                    "cylinder" -> UnitDropdown(cylSolve, listOf("Volume & Surface", "Height from Volume"), { cylSolve = it }, "Solve for")
                    "cone" -> UnitDropdown(coneSolve, listOf("Volume & Surface", "Height from Volume"), { coneSolve = it }, "Solve for")
                }
                labels.forEachIndexed { i, label ->
                    NumField(dims.getOrNull(i) ?: "", setters.getOrNull(i) ?: {}, "$label ($geoUnit)")
                }
                HelperCaption("Inputs in $geoUnit · areas in $areaUnit (m² also shown) · volumes in $volUnit (m³ also shown). All inputs must be > 0.")
                TextButton(onClick = { showGeoSteps = !showGeoSteps }) { Text(if (showGeoSteps) "Hide steps" else "Show steps") }
                if (showGeoSteps) HelperCaption(geoFormula)
                HorizontalDivider()
                outputs.forEach { (label, value) -> ToolResultRow(Icons.Filled.Info, label, value) }
                ToolErrorLine(geoErr)
            }
        }
        item {
            var sa by remember { mutableStateOf("3") }
            var sb by remember { mutableStateOf("4") }
            var sc by remember { mutableStateOf("5") }
            val triRes = runCatching {
                val a = sa.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Side a must be a number > 0.")
                val b = sb.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Side b must be a number > 0.")
                val c = sc.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Side c must be a number > 0.")
                Geometry.solveTriangleSSS(a, b, c)
            }
            val tri = triRes.getOrNull()
            SectionCard("Triangle SSS") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(sa, { sa = it }, "a ($geoUnit)") }
                    Box(Modifier.weight(1f)) { NumField(sb, { sb = it }, "b ($geoUnit)") }
                    Box(Modifier.weight(1f)) { NumField(sc, { sc = it }, "c ($geoUnit)") }
                }
                HelperCaption("All sides must be > 0 and satisfy the triangle inequality: a+b>c, a+c>b, b+c>a.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Info, "Angle A", tri?.get("angleA")?.let { fmt(it, 2) + "°" } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Angle B", tri?.get("angleB")?.let { fmt(it, 2) + "°" } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Angle C", tri?.get("angleC")?.let { fmt(it, 2) + "°" } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Perimeter", tri?.get("perimeter")?.let { fmt(it, 2) + " $geoUnit" } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Area", tri?.get("area")?.let { fmt(it, 2) + " $areaUnit" } ?: "—")
                ToolErrorLine(triRes.exceptionOrNull()?.message?.let {
                    if (it.contains("triangle inequality", ignoreCase = true)) "triangle inequality violated: each pair of sides must sum above the third."
                    else it
                })
            }
        }
    }
}

@Composable
fun HealthScreen(onNavigate: (String) -> Unit = {}) {
    var bmiWeight by remember { mutableStateOf("70") }
    var bmiHeightCm by remember { mutableStateOf("175") }
    var bfHeightCm by remember { mutableStateOf("175") }
    var tdeeWeight by remember { mutableStateOf("70") }
    var tdeeHeightCm by remember { mutableStateOf("175") }
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
    var bmiMetric by remember { mutableStateOf(true) }
    var wtLb by remember { mutableStateOf("154") }
    var htFt by remember { mutableStateOf("5") }
    var htIn by remember { mutableStateOf("9") }
    val fatRes = runCatching {
        HealthDate.bodyFatNavy(num(waist), num(neck), num(bfHeightCm), num(hips), male)
    }
    val tdeeRes = runCatching {
        val tw = tdeeWeight.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Weight must be a number > 0.")
        val th = tdeeHeightCm.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Height must be a number > 0.")
        val ta = age.trim().toIntOrNull() ?: throw IllegalArgumentException("Age must be a whole number.")
        HealthDate.tdee(tw, th, ta, tdeeMale, activity)
    }
    val ageRes = ageYMD(by.toIntOrNull() ?: 0, bm.toIntOrNull() ?: 0, bd.toIntOrNull() ?: 0)
    val activities = listOf(
        1.2 to "Sedentary",
        1.375 to "Light",
        1.55 to "Moderate",
        1.725 to "Active",
        1.9 to "Athlete"
    )
    LazyColumn(Modifier.fillMaxSize().padding(bottom = 72.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("BMI") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = bmiMetric, onClick = { bmiMetric = true }, label = { Text("Metric") })
                    }
                    item {
                        FilterChip(selected = !bmiMetric, onClick = { bmiMetric = false }, label = { Text("Imperial") })
                    }
                }
                if (bmiMetric) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { NumField(bmiWeight, { bmiWeight = it }, "Weight kg") }
                        Box(Modifier.weight(1f)) { NumField(bmiHeightCm, { bmiHeightCm = it }, "Height cm") }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { NumField(htFt, { htFt = it }, "Feet", integer = true) }
                        Box(Modifier.weight(1f)) { NumField(htIn, { htIn = it }, "Inches") }
                    }
                    NumField(wtLb, { wtLb = it }, "Weight lb")
                }
                val bw = if (bmiMetric) (bmiWeight.trim().toDoubleOrNull() ?: Double.NaN) else num(wtLb) * 0.45359237
                val bh = if (bmiMetric) (bmiHeightCm.trim().toDoubleOrNull() ?: Double.NaN) else num(htFt) * 30.48 + num(htIn) * 2.54
                val bmiRes = runCatching { HealthDate.bmi(bw, bh) }
                val bmiV = bmiRes.getOrDefault(Double.NaN)
                val bmiCatV = when {
                    !bmiV.isFinite() || (bw == 0.0 && bh == 0.0) -> "—"
                    bmiV < 18.5 -> "Underweight"
                    bmiV < 25 -> "Normal"
                    bmiV < 30 -> "Overweight"
                    else -> "Obese"
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Person, "BMI", if (bmiV.isFinite()) fmt(bmiV, 1) else "—")
                ToolResultRow(Icons.Filled.Favorite, "Category", bmiCatV)
                BmiBar(bmiV)
                HelperCaption("BMI = weight kg / (height m)². Underweight < 18.5 · Normal 18.5–24.9 · Overweight 25–29.9 · Obese ≥ 30.")
                Text(
                    bmiPlainLabel(bmiCatV, bmiV),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ToolErrorLine(bmiRes.exceptionOrNull()?.message)
                val bmiD = runCatching { HealthPlus.bmiDelta(bw, bh) }.getOrNull()
                ToolResultRow(
                    Icons.Filled.Info,
                    "To healthy range",
                    when {
                        bmiD == null || !bmiD.isFinite() -> "—"
                        bmiD == 0.0 -> "At healthy weight"
                        bmiD > 0 -> "Lose ${fmt(bmiD, 1)} kg to reach 24.9"
                        else -> "Gain ${fmt(-bmiD, 1)} kg to reach 18.5"
                    }
                )
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
                if (male) {
                    NumField(bfHeightCm, { bfHeightCm = it }, "Height cm")
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { NumField(bfHeightCm, { bfHeightCm = it }, "Height cm") }
                        Box(Modifier.weight(1f)) { NumField(hips, { hips = it }, "Hips cm") }
                    }
                }
                HelperCaption(if (male) "Male needs waist > neck." else "Female needs waist + hips > neck.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Person, "Body fat", fatRes.getOrNull()?.let { if (it.isFinite()) fmt(it, 1) + " %" else "—" } ?: "—")
                ToolErrorLine(fatRes.exceptionOrNull()?.message?.let {
                    if (it.contains("waistCm must exceed neckCm")) "Waist must exceed neck — measure waist at navel, neck below larynx."
                    else if (it.contains("waistCm + hipCm must exceed neckCm")) "Waist + hips must exceed neck — check tape placement."
                    else it
                })
            }
        }
        item {
            var tdeeGoal by remember { mutableStateOf("Maintain") }
            val goalAdj = when (tdeeGoal) { "Cut" -> 0.8; "Bulk" -> 1.1; else -> 1.0 }
            val tdeeBase = tdeeRes.getOrNull()
            val tdeeAdj = tdeeBase?.let { it * goalAdj }
            SectionCard("TDEE") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(tdeeWeight, { tdeeWeight = it }, "Weight kg") }
                    Box(Modifier.weight(1f)) { NumField(tdeeHeightCm, { tdeeHeightCm = it }, "Height cm") }
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = tdeeGoal == "Cut", onClick = { tdeeGoal = "Cut" }, label = { Text("Cut −20%") })
                    }
                    item {
                        FilterChip(selected = tdeeGoal == "Maintain", onClick = { tdeeGoal = "Maintain" }, label = { Text("Maintain") })
                    }
                    item {
                        FilterChip(selected = tdeeGoal == "Bulk", onClick = { tdeeGoal = "Bulk" }, label = { Text("Bulk +10%") })
                    }
                }
                HelperCaption("Mifflin-St Jeor: 10·kg + 6.25·cm − 5·age + 5 (male) / −161 (female), × activity.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Favorite, "Daily calories", tdeeAdj?.let { if (it.isFinite()) fmt(it, 0) + " kcal" else "—" } ?: "—")
                ToolResultRow(Icons.Filled.Info, "Goal ($tdeeGoal)", tdeeBase?.let { if (it.isFinite()) fmt(it * goalAdj, 0) + " kcal" else "—" } ?: "—")
                ToolErrorLine(tdeeRes.exceptionOrNull()?.message)
            }
        }
        item {
            val ageY = by.trim().toIntOrNull()
            val ageM = bm.trim().toIntOrNull()
            val ageD = bd.trim().toIntOrNull()
            val ageErr: String? = when {
                ageY == null -> "Year must be a whole number."
                ageM == null -> "Month must be a whole number 1..12."
                ageD == null -> "Day must be a whole number."
                ageM !in 1..12 -> "Month must be in 1..12 (got $ageM)."
                else -> runCatching {
                    java.time.LocalDate.of(ageY, ageM, ageD)
                    if (java.time.LocalDate.of(ageY, ageM, ageD).isAfter(java.time.LocalDate.now())) {
                        throw IllegalArgumentException("Birth date is in the future.")
                    }
                }.exceptionOrNull()?.message?.let {
                    if (it.contains("Invalid date", ignoreCase = true) || it.contains("MonthOfYear", ignoreCase = true) || it.contains("DayOfMonth", ignoreCase = true)) {
                        when {
                            ageM == 2 && ageD > 29 -> "Feb $ageY has at most 29 days (got $ageD)."
                            ageD > 31 -> "Day must be in 1..31 (got $ageD)."
                            else -> "Invalid date: $ageY-$ageM-$ageD does not exist."
                        }
                    } else it
                }
            }
            SectionCard("Age calculator") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(by, { by = it }, "Year", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(bm, { bm = it }, "Month", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(bd, { bd = it }, "Day", integer = true) }
                }
                HorizontalDivider()
                ToolResultRow(
                    Icons.Filled.DateRange,
                    "Age",
                    if (ageErr != null) "—" else if (ageRes == null) "—"
                    else "${ageRes.first}y ${ageRes.second}m ${ageRes.third}d"
                )
                ToolErrorLine(ageErr ?: if (ageRes == null) "Invalid date." else null)
            }
        }
        item {
            var cy by remember { mutableStateOf("2026") }
            var cm by remember { mutableStateOf("9") }
            var cd by remember { mutableStateOf("11") }
            var zone by remember { mutableStateOf("UTC") }
            val zones = listOf("UTC", "Europe/London", "Europe/Istanbul", "Europe/Berlin", "America/New_York", "America/Chicago", "America/Los_Angeles", "Asia/Dubai", "Asia/Karachi", "Asia/Kolkata", "Asia/Singapore", "Asia/Tokyo", "Australia/Sydney")
            val cyI = cy.trim().toIntOrNull()
            val cmI = cm.trim().toIntOrNull()
            val cdI = cd.trim().toIntOrNull()
            val dateErr: String? = when {
                cyI == null -> "Year must be a whole number."
                cmI == null || cmI !in 1..12 -> "Month must be in 1..12 (got ${cm.trim()})."
                cdI == null -> "Day must be a whole number."
                else -> runCatching { ClockKit.weekdayName(cyI, cmI, cdI) }.exceptionOrNull()?.message?.let {
                    if (cmI == 2 && cdI > 29) "Feb $cyI has at most 29 days (got $cdI)."
                    else "Invalid date: $cyI-$cmI-$cdI does not exist."
                }
            }
            val weekday = runCatching {
                if (dateErr != null) throw IllegalArgumentException(dateErr)
                ClockKit.weekdayName(cyI ?: 0, cmI ?: 0, cdI ?: 0)
            }
            val until = runCatching {
                if (dateErr != null) throw IllegalArgumentException(dateErr)
                ClockKit.daysUntil(cyI ?: 0, cmI ?: 0, cdI ?: 0)
            }
            val untilLabel = until.getOrNull()?.let {
                when {
                    it == 0L -> "today"
                    it > 0 -> "in $it day(s)"
                    else -> "${-it} day(s) ago"
                }
            }
            SectionCard("Date and world clock") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(cy, { cy = it }, "Year", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(cm, { cm = it }, "Month", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(cd, { cd = it }, "Day", integer = true) }
                }
                UnitDropdown(if (zone in zones) zone else zones.first(), zones, { zone = it }, "Timezone")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.DateRange, "Weekday", weekday.getOrNull() ?: "—")
                ToolResultRow(Icons.Filled.DateRange, "When", untilLabel ?: "—")
                ToolErrorLine(dateErr ?: weekday.exceptionOrNull()?.message ?: until.exceptionOrNull()?.message)
                val worldRes = runCatching { ClockKit.worldTime(zone) }
                ToolResultRow(Icons.Filled.DateRange, zone.ifBlank { "Zone" }, worldRes.getOrNull() ?: (worldRes.exceptionOrNull()?.message ?: "—"))
                ToolErrorLine(worldRes.exceptionOrNull()?.message?.let { "Unknown zone: $zone. Pick from the list." })
            }
        }
        item {
            var wWt by remember { mutableStateOf("70") }
            var wAct by remember { mutableStateOf("30") }
            var climate by remember { mutableStateOf("Temperate") }
            var waterLevel by remember { mutableStateOf("Moderate") }
            val climateExtra = when (climate) { "Hot" -> 500.0; "Humid" -> 250.0; else -> 0.0 }
            val levelExtra = when (waterLevel) { "Light" -> 0.0; "High" -> 500.0; "Very high" -> 1000.0; else -> 250.0 }
            val mlRes = runCatching {
                HealthPlus.waterIntakeMl(
                    wWt.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Weight must be a number > 0."),
                    wAct.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Active minutes must be a number ≥ 0.")
                ) + climateExtra + levelExtra
            }
            val ml = mlRes.getOrNull()
            SectionCard("Water intake") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(wWt, { wWt = it }, "Weight kg") }
                    Box(Modifier.weight(1f)) { NumField(wAct, { wAct = it }, "Active min") }
                }
                UnitDropdown(climate, listOf("Temperate", "Hot", "Humid"), { climate = it }, "Climate")
                UnitDropdown(waterLevel, listOf("Light", "Moderate", "High", "Very high"), { waterLevel = it }, "Activity level")
                HelperCaption("Formula: 35 mL × kg + 120 mL per 30 active min + climate/activity extra.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Favorite, "Daily water", ml?.let { if (it.isFinite()) "${fmt(it, 0)} mL (${fmt(it / 1000.0, 2)} L)" else "—" } ?: "—")
                ToolErrorLine(mlRes.exceptionOrNull()?.message)
            }
        }
        item {
            var pDist by remember { mutableStateOf("5") }
            var pTime by remember { mutableStateOf("30:00") }
            fun parseTimeToMin(s: String): Double {
                val t = s.trim()
                if (t.isEmpty()) throw IllegalArgumentException("Time is required (H:MM:SS or minutes).")
                if (t.contains(":")) {
                    val parts = t.split(":").map { it.trim() }
                    if (parts.size > 3 || parts.any { it.isEmpty() || it.toDoubleOrNull() == null }) {
                        throw IllegalArgumentException("Time must be H:MM:SS, MM:SS, or decimal minutes.")
                    }
                    val nums = parts.map { it.toDouble() }
                    return when (nums.size) {
                        3 -> nums[0] * 60 + nums[1] + nums[2] / 60.0
                        2 -> nums[0] + nums[1] / 60.0
                        else -> nums[0]
                    }
                }
                return t.toDoubleOrNull() ?: throw IllegalArgumentException("Time must be H:MM:SS or decimal minutes.")
            }
            fun fmtPace(minPerKm: Double): String {
                if (!minPerKm.isFinite() || minPerKm < 0) return "—"
                val m = minPerKm.toInt()
                val s = ((minPerKm - m) * 60).toInt().coerceIn(0, 59)
                return "%d:%02d min/km".format(m, s)
            }
            val paceRes = runCatching {
                val d = pDist.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Distance must be a number > 0.")
                if (d <= 0.0) throw IllegalArgumentException("Distance must be > 0.")
                val mins = parseTimeToMin(pTime)
                if (mins < 0) throw IllegalArgumentException("Time must be ≥ 0.")
                HealthPlus.runPace(d, mins)
            }
            val distVal = pDist.trim().toDoubleOrNull()
            val minsVal = runCatching { parseTimeToMin(pTime) }.getOrNull()
            val speedKmh = if (paceRes.isSuccess && minsVal != null && minsVal > 0 && distVal != null && distVal > 0) distVal / (minsVal / 60.0) else null
            SectionCard("Run pace") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(pDist, { pDist = it }, "Distance km") }
                    Box(Modifier.weight(1f)) {
                        OutlinedTextField(value = pTime, onValueChange = { pTime = it }, label = { Text("Time H:MM:SS/min") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                }
                HelperCaption("Time accepts H:MM:SS, MM:SS, or decimal minutes.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Favorite, "Pace", paceRes.getOrNull()?.let { fmt(it, 2) + " min/km (" + fmtPace(it) + ")" } ?: "—")
                ToolResultRow(Icons.Filled.Favorite, "Speed", speedKmh?.let { fmt(it, 2) + " km/h" } ?: "—")
                ToolResultRow(Icons.Filled.Favorite, "Time", minsVal?.let { fmt(it, 2) + " min" } ?: "—")
                ToolErrorLine(paceRes.exceptionOrNull()?.message)
            }
        }
        item {
            var ormW by remember { mutableStateOf("100") }
            var ormR by remember { mutableStateOf("5") }
            var ormFormula by remember { mutableStateOf("Epley") }
            val ormRes = runCatching {
                val wgt = ormW.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Weight must be a number > 0.")
                val reps = ormR.trim().toIntOrNull() ?: throw IllegalArgumentException("Reps must be a whole number 1..36.")
                if (reps !in 1..36) throw IllegalArgumentException("Reps must be in 1..36 (got $reps).")
                when (ormFormula) {
                    "Epley" -> HealthPlus.oneRepMax(wgt, reps, "epley")
                    "Brzycki" -> HealthPlus.oneRepMax(wgt, reps, "brzycki")
                    else -> {
                        if (wgt <= 0.0) throw IllegalArgumentException("Weight must be > 0.")
                        wgt * Math.pow(reps.toDouble(), 0.10)
                    }
                }
            }
            SectionCard("One-rep max") {
                MethodDropdown(
                    selected = ormFormula,
                    options = listOf(
                        "Epley" to "w × (1 + r/30)",
                        "Brzycki" to "w × 36/(37 − r)",
                        "Lombardi" to "w × r^0.10"
                    ),
                    onSelect = { ormFormula = it },
                    label = "Formula"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(ormW, { ormW = it }, "Weight kg") }
                    Box(Modifier.weight(1f)) { NumField(ormR, { ormR = it }, "Reps 1..36", integer = true) }
                }
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Person, "1RM ($ormFormula)", ormRes.getOrNull()?.let { fmt(it, 1) + " kg" } ?: "—")
                ToolErrorLine(ormRes.exceptionOrNull()?.message)
            }
        }
        item {
            var hrAge by remember { mutableStateOf("30") }
            var hrInt by remember { mutableStateOf("70") }
            var hrRest by remember { mutableStateOf("60") }
            var hrMethod by remember { mutableStateOf("220 − age") }
            val hrRes = runCatching {
                val a = hrAge.trim().toIntOrNull() ?: throw IllegalArgumentException("Age must be a whole number.")
                val inten = hrInt.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Intensity must be 0..100.")
                HealthPlus.targetHeartRate(a, inten)
            }
            val hrKarv = runCatching {
                val a = hrAge.trim().toIntOrNull() ?: throw IllegalArgumentException("Age must be a whole number.")
                val rest = hrRest.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Resting HR must be a number.")
                val inten = hrInt.trim().toDoubleOrNull() ?: throw IllegalArgumentException("Intensity must be 0..100.")
                if (a <= 0 || a >= 220) throw IllegalArgumentException("Age must be in 1..219.")
                if (rest <= 0) throw IllegalArgumentException("Resting HR must be > 0.")
                ((220 - a) - rest) * inten / 100.0 + rest
            }
            val hrShow = if (hrMethod == "Karvonen") hrKarv else hrRes
            fun zoneAt(pct: Double): String {
                val a = hrAge.trim().toIntOrNull() ?: return "—"
                val rest = hrRest.trim().toDoubleOrNull()
                return runCatching {
                    if (hrMethod == "Karvonen") {
                        if (rest == null || rest <= 0) throw IllegalArgumentException("x")
                        fmt(((220 - a) - rest) * pct / 100.0 + rest, 0) + " bpm"
                    } else {
                        fmt(HealthPlus.targetHeartRate(a, pct), 0) + " bpm"
                    }
                }.getOrDefault("—")
            }
            SectionCard("Target heart rate") {
                MethodDropdown(
                    selected = hrMethod,
                    options = listOf(
                        "220 − age" to "target = (220 − age) × intensity",
                        "Karvonen" to "target = ((220 − age) − rest) × intensity + rest"
                    ),
                    onSelect = { hrMethod = it },
                    label = "Method"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { NumField(hrAge, { hrAge = it }, "Age", integer = true) }
                    Box(Modifier.weight(1f)) { NumField(hrInt, { hrInt = it }, "Intensity %") }
                }
                if (hrMethod == "Karvonen") NumField(hrRest, { hrRest = it }, "Resting HR bpm", integer = true)
                HelperCaption(if (hrMethod == "Karvonen") "Karvonen uses resting HR." else "Standard method: 220 − age.")
                HorizontalDivider()
                ToolResultRow(Icons.Filled.Favorite, "Target HR", hrShow.getOrNull()?.let { if (it.isFinite()) fmt(it, 0) + " bpm" else "—" } ?: "—")
                ToolResultRow(Icons.Filled.Favorite, "Fat-burn (50–70%)", zoneAt(50.0) + " – " + zoneAt(70.0))
                ToolResultRow(Icons.Filled.Favorite, "Cardio (70–85%)", zoneAt(70.0) + " – " + zoneAt(85.0))
                ToolResultRow(Icons.Filled.Favorite, "Peak (85–95%)", zoneAt(85.0) + " – " + zoneAt(95.0))
                ToolErrorLine(hrShow.exceptionOrNull()?.message)
            }
        }
    }
}

@Composable
fun StepsScreen(onNavigate: (String) -> Unit = {}) {
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
    var unitCat by remember { mutableStateOf("length") }
    val tabs = listOf("quad" to "Quadratic", "emi" to "EMI", "gcd" to "GCD", "units" to "Length steps")
    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier.fillMaxSize().padding(vertical = 16.dp).padding(bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                    val r = annual / 1200.0
                                    val emi = runCatching { Finance.emi(p, annual, months) }.getOrDefault(Double.NaN)
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
                    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    val catMap = remember(unitCat) { mapFor(unitCat) }
                    val catIds = listOf("length", "mass", "volume", "area", "speed", "pressure", "energy", "power", "data")
                    val safeCf = if (catMap.containsKey(cf.trim())) cf.trim() else catMap.keys.sorted().firstOrNull() ?: cf.trim()
                    val safeCt = if (catMap.containsKey(ct.trim())) ct.trim() else catMap.keys.sorted().firstOrNull() ?: ct.trim()
                    val v = cv.trim().toDoubleOrNull()
                    val f = catMap[safeCf]
                    val t = catMap[safeCt]
                    val names = catMap.keys.sorted().joinToString(", ")
                    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        item {
                            SectionCard("Inputs") {
                                NumField(cv, { cv = it }, "Value")
                                UnitDropdown(unitCat, catIds, { unitCat = it; cf = ""; ct = "" }, "Category")
                                UnitDropdown(safeCf, catMap.keys.sorted(), { cf = it }, "From unit")
                                UnitDropdown(safeCt, catMap.keys.sorted(), { ct = it }, "To unit")
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
                                        "Unknown unit. Pick from the dropdowns.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    val base = v * f.toBase / 1.0
                                    val out = runCatching { Units.convert(v, f, t) }.getOrDefault(Double.NaN)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("1. Factors to base: 1 ${f.id} = ${fmt(f.toBase, 6)}, 1 ${t.id} = ${fmt(t.toBase, 6)}", style = MaterialTheme.typography.bodyMedium)
                                        Text("2. To base: $v × ${fmt(f.toBase, 6)} = ${fmt(base, 6)}", style = MaterialTheme.typography.bodyMedium)
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
                    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                    ResultLine("Roots", runCatching { Engine.solveQuadratic(a, b, c) }.getOrDefault(emptyList()).joinToString().ifBlank { "—" })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        BottomBackChevron(onBack = { onNavigate("tools") }, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
        JumpToCalcFab(onJump = { onNavigate("calc") }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                Text(
                    when {
                        aStr.trim().isEmpty() || bStr.trim().isEmpty() -> "Enter values for a and b."
                        av == null -> "Invalid a for base ${bases.firstOrNull { it.first == base }?.second ?: base}."
                        else -> "Invalid b for base ${bases.firstOrNull { it.first == base }?.second ?: base}."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                ResultLine("AND", runCatching { show(Engine.bitwiseAnd(av, bv)) }.getOrDefault("—"))
                ResultLine("OR", runCatching { show(Engine.bitwiseOr(av, bv)) }.getOrDefault("—"))
                ResultLine("XOR", runCatching { show(Engine.bitwiseXor(av, bv)) }.getOrDefault("—"))
                ResultLine("NOT a", runCatching { show(Engine.bitwiseNot(av)) }.getOrDefault("—"))
                ResultLine("a shl b", runCatching { show(Engine.shl(av, bv.toInt())) }.getOrDefault("—"))
                ResultLine("a shr b", runCatching { show(Engine.shr(av, bv.toInt())) }.getOrDefault("—"))
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
            val loRaw = minStr.trim().toIntOrNull()
            val hiRaw = maxStr.trim().toIntOrNull()
            val rngErr: String? = when {
                loRaw == null -> "Min must be a whole number."
                hiRaw == null -> "Max must be a whole number."
                loRaw > hiRaw -> "Min must not exceed Max (got $loRaw > $hiRaw)."
                else -> null
            }
            Button(onClick = {
                val lo = minStr.trim().toIntOrNull() ?: return@Button
                val hi = maxStr.trim().toIntOrNull() ?: return@Button
                if (lo > hi) return@Button
                runCatching { Engine.randomInt(lo, hi) }.onSuccess { rolls = (listOf(it) + rolls).take(20) }
            }) { Text("Generate") }
            HorizontalDivider()
            if (rngErr != null) {
                Text(rngErr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            if (rolls.isEmpty()) {
                ResultLine("Last roll", "—")
            } else {
                rolls.forEachIndexed { i, r -> ResultLine(if (i == 0) "Last roll" else "Roll ${i + 1}", "$r") }
            }
            HelperCaption("History keeps last 20 rolls.")
        }
    }
}
