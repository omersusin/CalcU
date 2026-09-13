package calc.u.core

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import calc.u.ui.CalcUNumberBox
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import kotlin.math.roundToInt

@Composable
fun HealthLabContent(onCopy: (String) -> Unit = {}) {
    // Shared anthropometrics so every section keys off the same inputs.
    var weightStr by remember { mutableStateOf("70") }
    var heightStr by remember { mutableStateOf("175") }
    var ageStr by remember { mutableStateOf("30") }
    var male by remember { mutableStateOf(true) }
    var activityStr by remember { mutableStateOf("1.55") }
    var activeMinStr by remember { mutableStateOf("30") }
    var waistStr by remember { mutableStateOf("85") }
    var hipStr by remember { mutableStateOf("95") }
    var sysStr by remember { mutableStateOf("120") }
    var diasStr by remember { mutableStateOf("80") }
    var intensityStr by remember { mutableStateOf("70") }
    var liftStr by remember { mutableStateOf("80") }
    var repsStr by remember { mutableStateOf("8") }
    var distStr by remember { mutableStateOf("5") }
    var minsStr by remember { mutableStateOf("30") }

    val weight = (weightStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val height = (heightStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val age = (ageStr.toDoubleOrNull()?.toInt() ?: 0).coerceAtLeast(0)
    val activity = (activityStr.toDoubleOrNull() ?: 1.55).coerceAtLeast(1.0)
    val activeMin = (activeMinStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val waist = (waistStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val hip = (hipStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val sys = (sysStr.toDoubleOrNull()?.toInt() ?: 0).coerceIn(0, 320)
    val dias = (diasStr.toDoubleOrNull()?.toInt() ?: 0).coerceIn(0, 220)
    val intensity = (intensityStr.toDoubleOrNull() ?: 70.0).coerceIn(0.0, 100.0)
    val lift = (liftStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val reps = (repsStr.toDoubleOrNull()?.toInt() ?: 0).coerceAtLeast(0)
    val dist = (distStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val mins = (minsStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)

    // Body section.
    val bmi = remember(weight, height) { runCatching { HealthDate.bmi(weight, height) }.getOrNull() }
    val bodyFat = remember(weight, height, age, male) {
        runCatching { HealthPlus.bodyFatDeurenberg(weight, height, age, male) }.getOrNull()
    }
    val bmr = remember(weight, height, age, male) {
        runCatching { GapTools.bmrMifflin(weight, height, age, male) }.getOrNull()
    }
    val tdee = remember(bmr, activity) { bmr?.let { runCatching { it * activity }.getOrNull() } }
    val waterL = remember(weight, activeMin) {
        runCatching { HealthPlus.waterIntakeMl(weight, activeMin) / 1000.0 }.getOrNull()
    }

    // Health markers section.
    val bsa = remember(weight, height) { runCatching { GapTools.bsaDuBois(weight, height) }.getOrNull() }
    val whr = remember(waist, hip) { runCatching { GapTools.waistHipRatio(waist, hip) }.getOrNull() }
    val whrCategory = remember(whr, male) {
        whr?.let { runCatching { GapTools.waistHipCategory(it, male) }.getOrNull() }
    }
    val rfm = remember(height, waist, male) {
        runCatching { GapTools.rfm(height, waist, male) }.getOrNull()
    }
    val lbm = remember(weight, height, male) {
        runCatching { GapTools.lbmHume(weight, height, male) }.getOrNull()
    }
    val bpCategory = remember(sys, dias) {
        runCatching { GapTools.bloodPressureCategory(sys, dias) }.getOrNull()
    }

    // Fitness section (HealthPlus.targetHeartRate has no rest-HR arg; age + intensity%).
    val targetHr = remember(age, intensity) {
        runCatching { HealthPlus.targetHeartRate(age, intensity) }.getOrNull()
    }
    val orm = remember(lift, reps) { runCatching { HealthPlus.oneRepMax(lift, reps) }.getOrNull() }
    val pace = remember(dist, mins) { runCatching { HealthPlus.runPace(dist, mins) }.getOrNull() }
    val paceString = pace?.let {
        val totalSec = (it * 60.0).roundToInt()
        "${totalSec / 60}:${(totalSec % 60).toString().padStart(2, '0')} min/km"
    } ?: "–"

    val summary = buildString {
        appendLine("Health Lab")
        appendLine("BMI: ${fmtOrDash(bmi)}${bmi?.let { " (${bmiBand(it)})" } ?: ""}")
        appendLine("Body fat: ${fmtOrDash(bodyFat)} %")
        appendLine("BMR: ${fmtOrDash(bmr)} kcal/day")
        appendLine("TDEE: ${fmtOrDash(tdee)} kcal/day")
        appendLine("Water: ${fmtOrDash(waterL)} L")
        appendLine("BSA: ${fmtOrDash(bsa)} m²")
        appendLine("Waist-hip: ${fmtOrDash(whr)} (${whrCategory ?: "–"})")
        appendLine("RFM: ${fmtOrDash(rfm)} %")
        appendLine("Lean body mass: ${fmtOrDash(lbm)} kg")
        appendLine("Blood pressure: ${bpCategory ?: "–"}")
        appendLine("Target HR: ${fmtOrDash(targetHr)} bpm")
        appendLine("1RM: ${fmtOrDash(orm)} kg")
        append("Pace: $paceString")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SectionCard(title = "Body") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcUNumberBox(
                    weightStr, { weightStr = it }, "Weight (kg)",
                    modifier = Modifier.weight(1f), min = 0.0
                )
                CalcUNumberBox(
                    heightStr, { heightStr = it }, "Height (cm)",
                    modifier = Modifier.weight(1f), min = 0.0
                )
            }
            CalcUNumberBox(ageStr, { ageStr = it }, "Age (years)", min = 0.0, max = 150.0, integer = true)
            SexSelector(male = male, onMaleChange = { male = it })
            BmiBand(bmi = bmi ?: Double.NaN)
            ResultLine(label = "BMI", value = "${fmtOrDash(bmi)}${bmi?.let { " · ${bmiBand(it)}" } ?: ""}")
            ResultLine(label = "Body fat", value = "${fmtOrDash(bodyFat)} %")
            ResultLine(label = "BMR", value = "${fmtOrDash(bmr)} kcal/day")
            CalcUNumberBox(
                activityStr, { activityStr = it }, "Activity multiplier",
                min = 1.0, max = 2.0, smallChange = 0.125
            )
            ResultLine(label = "TDEE", value = "${fmtOrDash(tdee)} kcal/day")
            CalcUNumberBox(activeMinStr, { activeMinStr = it }, "Active time / day (min)", min = 0.0)
            ResultLine(label = "Water intake", value = "${fmtOrDash(waterL)} L")
        }

        SectionCard(title = "Health markers") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcUNumberBox(
                    waistStr, { waistStr = it }, "Waist (cm)",
                    modifier = Modifier.weight(1f), min = 0.0
                )
                CalcUNumberBox(
                    hipStr, { hipStr = it }, "Hip (cm)",
                    modifier = Modifier.weight(1f), min = 0.0
                )
            }
            ResultLine(label = "BSA", value = "${fmtOrDash(bsa)} m²")
            ResultLine(label = "Waist-hip ratio", value = "${fmtOrDash(whr)} (${whrCategory ?: "–"})")
            ResultLine(label = "Relative fat mass", value = "${fmtOrDash(rfm)} %")
            ResultLine(label = "Lean body mass", value = "${fmtOrDash(lbm)} kg")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcUNumberBox(
                    sysStr, { sysStr = it }, "Systolic (mmHg)",
                    modifier = Modifier.weight(1f), min = 40.0, max = 320.0, integer = true
                )
                CalcUNumberBox(
                    diasStr, { diasStr = it }, "Diastolic (mmHg)",
                    modifier = Modifier.weight(1f), min = 20.0, max = 220.0, integer = true
                )
            }
            ResultLine(label = "Blood pressure", value = capitalize(bpCategory ?: "–"))
        }

        SectionCard(title = "Fitness") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcUNumberBox(
                    ageStr, { ageStr = it }, "Age (years)",
                    modifier = Modifier.weight(1f), min = 0.0, max = 150.0, integer = true
                )
                CalcUNumberBox(
                    intensityStr, { intensityStr = it }, "Intensity (%)",
                    modifier = Modifier.weight(1f), min = 0.0, max = 100.0
                )
            }
            ResultLine(label = "Target heart rate", value = "${fmtOrDash(targetHr)} bpm")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcUNumberBox(
                    liftStr, { liftStr = it }, "Lifted (kg)",
                    modifier = Modifier.weight(1f), min = 0.0
                )
                CalcUNumberBox(
                    repsStr, { repsStr = it }, "Reps",
                    modifier = Modifier.weight(1f), min = 0.0, integer = true
                )
            }
            ResultLine(label = "One-rep max", value = "${fmtOrDash(orm)} kg")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcUNumberBox(
                    distStr, { distStr = it }, "Distance (km)",
                    modifier = Modifier.weight(1f), min = 0.0
                )
                CalcUNumberBox(
                    minsStr, { minsStr = it }, "Time (min)",
                    modifier = Modifier.weight(1f), min = 0.0
                )
            }
            ResultLine(label = "Run pace", value = paceString)
        }

        Button(onClick = { onCopy(summary) }, modifier = Modifier.fillMaxWidth()) {
            Text("Copy summary")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SexSelector(male: Boolean, onMaleChange: (Boolean) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = male,
            onClick = { onMaleChange(true) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
        ) { Text("Male") }
        SegmentedButton(
            selected = !male,
            onClick = { onMaleChange(false) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) { Text("Female") }
    }
}

@Composable
private fun BmiBand(bmi: Double, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier.fillMaxWidth().height(18.dp)) {
        val low = 14.0
        val high = 40.0
        val px: (Double) -> Float = { v -> ((v - low) / (high - low)).toFloat() * size.width }
        val band = (0.28f * size.height).coerceAtLeast(4f)
        val y = (size.height - band) / 2f
        val r = CornerRadius(band / 2f, band / 2f)
        drawRoundRect(
            colors.primaryContainer,
            topLeft = Offset(0f, y),
            size = Size(size.width, band),
            cornerRadius = r
        )
        val segments = listOf(
            Triple(14.0, 18.5, colors.tertiaryContainer),
            Triple(18.5, 25.0, colors.primary),
            Triple(25.0, 30.0, colors.primaryContainer),
            Triple(30.0, 35.0, colors.errorContainer),
            Triple(35.0, 40.0, colors.error)
        )
        segments.forEach { (from, to, color) ->
            val a = px(from).coerceIn(0f, size.width)
            val b = px(to).coerceIn(0f, size.width)
            if (b > a) {
                drawRoundRect(color, topLeft = Offset(a, y), size = Size(b - a, band), cornerRadius = r)
            }
        }
        if (bmi.isFinite()) {
            val markerX = px(bmi.coerceIn(low, high))
            val cy = y + band / 2f
            drawCircle(colors.onSurface, radius = band * 0.62f, center = Offset(markerX, cy))
            drawCircle(colors.surface, radius = band * 0.3f, center = Offset(markerX, cy))
        }
    }
}

private fun one(v: Double): String {
    val rounded = (v * 10.0).roundToInt() / 10.0
    val asLong = rounded.toLong()
    return if (rounded == asLong.toDouble()) asLong.toString() else rounded.toString()
}

private fun fmtOrDash(v: Double?): String = v?.let { one(it) } ?: "–"

private fun bmiBand(v: Double): String = when {
    v < 18.5 -> "underweight"
    v < 25.0 -> "healthy"
    v < 30.0 -> "overweight"
    v < 35.0 -> "obese class I"
    else -> "obese class II"
}

private fun capitalize(s: String): String =
    if (s.isEmpty()) s else s[0].uppercase() + s.substring(1)