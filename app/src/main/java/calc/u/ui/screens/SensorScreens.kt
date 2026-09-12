package calc.u.ui.screens

import android.content.Context
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun SensorScreen() {
    var tab by remember { mutableStateOf("compass") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("compass" to "Compass", "level" to "Level")) { (id, label) ->
                FilterChip(selected = tab == id, onClick = { tab = id }, label = { Text(label) })
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                "level" -> LevelScreen()
                else -> CompassScreen()
            }
        }
    }
}

private fun cardinalLabel(deg: Float): String {
    val names = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    if (!deg.isFinite()) return "—"
    return names.getOrNull((((((deg + 22.5f) / 45f).toInt() % 8 + 8) % 8))) ?: "—"
}

@Composable
fun CompassScreen() {
    val context = LocalContext.current
    val sensorManager = remember {
        runCatching { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }.getOrNull()
    }
    if (sensorManager == null) {
        SectionCard("Compass") {
            Text(
                "Compass is unavailable on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }
    val accelerometer = remember(sensorManager) { runCatching { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }.getOrNull() }
    val magnetometer = remember(sensorManager) { runCatching { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }.getOrNull() }
    var azimuth by remember { mutableFloatStateOf(0f) }
    var hasReading by remember { mutableStateOf(false) }
    if (accelerometer == null || magnetometer == null) {
        SectionCard("Compass") {
            Text(
                "Compass needs an accelerometer and magnetometer, which this device lacks.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }
    DisposableEffect(Unit) {
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasMag = false
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                runCatching {
                    val vals = event.values
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        if (vals.size < 3) return@runCatching
                        System.arraycopy(vals, 0, gravity, 0, 3)
                        hasGravity = true
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        if (vals.size < 3) return@runCatching
                        System.arraycopy(vals, 0, geomagnetic, 0, 3)
                        hasMag = true
                    }
                    if (hasGravity && hasMag) {
                        val r = FloatArray(9)
                        val i = FloatArray(9)
                        if (runCatching { SensorManager.getRotationMatrix(r, i, gravity, geomagnetic) }.getOrDefault(false)) {
                            val orientation = FloatArray(3)
                            runCatching { SensorManager.getOrientation(r, orientation) }
                            val deg = orientation.getOrNull(0)?.toDouble()?.let { Math.toDegrees(it) }?.toFloat() ?: return@runCatching
                            if (!deg.isFinite()) return@runCatching
                            azimuth = ((deg + 360f) % 360f)
                            hasReading = true
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        runCatching { sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI) }
        runCatching { sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI) }
        onDispose { runCatching { sensorManager.unregisterListener(listener) } }
    }
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val northRed = MaterialTheme.colorScheme.error
    SectionCard("Compass") {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(
                Modifier.size(240.dp)
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.minDimension / 2f
                drawCircle(color = track, radius = radius, style = Stroke(width = 2.dp.toPx()))
                for (deg in 0 until 360 step 15) {
                    val cardinal = deg % 90 == 0
                    val rad = Math.toRadians(deg.toDouble())
                    val outer = radius - 4.dp.toPx()
                    val inner = radius - (if (cardinal) 20.dp.toPx() else 12.dp.toPx())
                    drawLine(
                        color = if (cardinal) onSurface else onSurfaceVariant,
                        start = Offset(cx + inner * kotlin.math.sin(rad).toFloat(), cy - inner * kotlin.math.cos(rad).toFloat()),
                        end = Offset(cx + outer * kotlin.math.sin(rad).toFloat(), cy - outer * kotlin.math.cos(rad).toFloat()),
                        strokeWidth = if (cardinal) 3.dp.toPx() else 1.5.dp.toPx()
                    )
                }
                drawContext.canvas.nativeCanvas.apply {
                    val paint = Paint().apply {
                        isAntiAlias = true
                        textAlign = Paint.Align.CENTER
                        textSize = 16.dp.toPx()
                        color = onSurface.toArgb()
                    }
                    val labelR = radius - 34.dp.toPx()
                    val labels = listOf("N" to 0, "E" to 90, "S" to 180, "W" to 270)
                    for ((text, deg) in labels) {
                        val rad = Math.toRadians(deg.toDouble())
                        drawText(
                            text,
                            cx + labelR * kotlin.math.sin(rad).toFloat(),
                            cy - labelR * kotlin.math.cos(rad).toFloat() + 16.dp.toPx() / 3f,
                            paint
                        )
                    }
                }
                val needle = Math.toRadians((-azimuth).toDouble())
                val nx = kotlin.math.sin(needle).toFloat()
                val ny = -kotlin.math.cos(needle).toFloat()
                drawLine(
                    color = northRed,
                    start = Offset(cx - nx * 8.dp.toPx(), cy - ny * 8.dp.toPx()),
                    end = Offset(cx + nx * (radius - 44.dp.toPx()), cy + ny * (radius - 44.dp.toPx())),
                    strokeWidth = 6.dp.toPx()
                )
                drawLine(
                    color = onSurfaceVariant,
                    start = Offset(cx + nx * 8.dp.toPx(), cy + ny * 8.dp.toPx()),
                    end = Offset(cx - nx * (radius - 44.dp.toPx()), cy - ny * (radius - 44.dp.toPx())),
                    strokeWidth = 6.dp.toPx()
                )
                drawCircle(color = primary, radius = 6.dp.toPx())
            }
        }
        Text(
            if (hasReading) "${azimuth.toInt()}° ${cardinalLabel(azimuth)}" else "—",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        ResultLine("Heading", if (hasReading) "${azimuth.toInt()}°" else "Waiting for sensor…")
    }
}

@Composable
fun LevelScreen() {
    val context = LocalContext.current
    val sensorManager = remember {
        runCatching { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }.getOrNull()
    }
    val accelerometer = remember(sensorManager) {
        val mgr = sensorManager ?: return@remember null
        runCatching { mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }.getOrNull()
    }
    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }
    var hasReading by remember { mutableStateOf(false) }
    if (sensorManager == null || accelerometer == null) {
        SectionCard("Spirit level") {
            Text(
                "Spirit level needs an accelerometer, which this device lacks.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }
    DisposableEffect(Unit) {
        val mgr = sensorManager
        val sensor = accelerometer
        if (mgr == null || sensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    runCatching {
                        val ax = event.values.getOrNull(0) ?: return@runCatching
                        val ay = event.values.getOrNull(1) ?: return@runCatching
                        val az = event.values.getOrNull(2) ?: return@runCatching
                        if (!ax.isFinite() || !ay.isFinite() || !az.isFinite()) return@runCatching
                        pitch = Math.toDegrees(atan2(-ax.toDouble(), sqrt((ay * ay + az * az).toDouble()))).toFloat()
                        roll = Math.toDegrees(atan2(ay.toDouble(), az.toDouble())).toFloat()
                        if (pitch.isFinite() && roll.isFinite()) hasReading = true
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            runCatching { mgr.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI) }
            onDispose { runCatching { mgr.unregisterListener(listener) } }
        }
    }
    val primary = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    SectionCard("Spirit level") {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(
                Modifier.size(200.dp)
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.minDimension / 2f
                drawCircle(color = track, radius = radius, style = Stroke(width = 2.dp.toPx()))
                drawLine(
                    color = onSurfaceVariant,
                    start = Offset(cx - radius, cy),
                    end = Offset(cx + radius, cy),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = onSurfaceVariant,
                    start = Offset(cx, cy - radius),
                    end = Offset(cx, cy + radius),
                    strokeWidth = 1.dp.toPx()
                )
                drawCircle(color = onSurfaceVariant, radius = 4.dp.toPx(), center = Offset(cx, cy))
                val maxOffset = radius - 24.dp.toPx()
                val bx = cx + (roll / 45f).coerceIn(-1f, 1f) * maxOffset
                val by = cy + (pitch / 45f).coerceIn(-1f, 1f) * maxOffset
                drawCircle(color = primary, radius = 18.dp.toPx(), center = Offset(bx, by))
                drawCircle(color = primary, radius = 30.dp.toPx(), center = Offset(bx, by), style = Stroke(width = 1.5.dp.toPx()))
            }
        }
        ResultLine("Pitch", if (hasReading) "${pitch.toInt()}°" else "Waiting for sensor…")
        ResultLine("Roll", if (hasReading) "${roll.toInt()}°" else "Waiting for sensor…")
    }
}
