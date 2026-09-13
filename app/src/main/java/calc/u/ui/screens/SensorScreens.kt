package calc.u.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import calc.u.ui.ResultLine
import calc.u.ui.SectionCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.log10
import kotlin.math.sqrt

@Composable
fun SensorScreen() {
    var tab by remember { mutableStateOf("compass") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("compass" to "Compass", "level" to "Level", "sound" to "Sound")) { (id, label) ->
                FilterChip(selected = tab == id, onClick = { tab = id }, label = { Text(label) })
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                "level" -> LevelScreen()
                "sound" -> SoundScreen()
                else -> CompassScreen()
            }
        }
    }
}

private val Compass16 = listOf(
    "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
    "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
)

private fun cardinalLabel(deg: Float): String {
    if (!deg.isFinite()) return "—"
    val idx = runCatching {
        (((((deg + 11.25f) / 22.5f).toInt() % 16) + 16) % 16)
    }.getOrDefault(0)
    return runCatching { Compass16.getOrNull(idx) }.getOrNull() ?: "—"
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
    var accuracy by remember { mutableStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var lowAccuracy by remember { mutableStateOf(false) }
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
            override fun onAccuracyChanged(sensor: Sensor?, acc: Int) {
                runCatching {
                    accuracy = acc
                    lowAccuracy = acc <= SensorManager.SENSOR_STATUS_ACCURACY_LOW
                }
            }
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
        if (lowAccuracy && hasReading) {
            Text(
                "Low accuracy — move phone in a figure-8 to calibrate.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else if (!hasReading) {
            Text(
                "Waiting for sensor… if the heading drifts, move phone in a figure-8.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(
                Modifier.size(240.dp)
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.minDimension / 2f - 2.dp.toPx()
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
        ResultLine("Heading", if (hasReading) "${azimuth.toInt()}° ${cardinalLabel(azimuth)} (16-pt)" else "Waiting for sensor…")
        ResultLine(
            "Accuracy",
            when (accuracy) {
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low — calibrate (figure-8)"
                else -> "Unreliable — calibrate (figure-8)"
            }
        )
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
    var pitchZero by rememberSaveable { mutableStateOf(0f) }
    var rollZero by rememberSaveable { mutableStateOf(0f) }
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
    val shownPitch = runCatching { pitch - pitchZero }.getOrDefault(pitch)
    val shownRoll = runCatching { roll - rollZero }.getOrDefault(roll)
    SectionCard("Spirit level") {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(
                Modifier.size(200.dp)
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.minDimension / 2f - 2.dp.toPx()
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
                val maxOffset = radius - 32.dp.toPx()
                val bx = cx + (shownRoll / 45f).coerceIn(-1f, 1f) * maxOffset
                val by = cy + (shownPitch / 45f).coerceIn(-1f, 1f) * maxOffset
                drawCircle(color = primary, radius = 18.dp.toPx(), center = Offset(bx, by))
                drawCircle(color = primary, radius = 30.dp.toPx(), center = Offset(bx, by), style = Stroke(width = 1.5.dp.toPx()))
            }
        }
        fun oneDec(v: Float): String = runCatching { "%.1f°".format(v) }.getOrDefault("—")
        ResultLine("Pitch", if (hasReading) oneDec(shownPitch) else "Waiting for sensor…")
        ResultLine("Roll", if (hasReading) oneDec(shownRoll) else "Waiting for sensor…")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                Button(
                    onClick = {
                        runCatching {
                            if (hasReading && pitch.isFinite() && roll.isFinite()) {
                                pitchZero = pitch
                                rollZero = roll
                            }
                        }
                    },
                    enabled = hasReading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Zero / calibrate") }
            }
            Box(Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { runCatching { pitchZero = 0f; rollZero = 0f } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reset zero") }
            }
        }
        if (pitchZero != 0f || rollZero != 0f) {
            Text(
                "Zeroed at ${oneDec(pitchZero)} / ${oneDec(rollZero)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SoundScreen() {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            runCatching {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)
        )
    }
    var running by remember { mutableStateOf(false) }
    var levelDb by remember { mutableStateOf<Double?>(null) }
    var minDb by remember { mutableStateOf<Double?>(null) }
    var maxDb by remember { mutableStateOf<Double?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        granted = isGranted
        error = null
        if (isGranted) running = true else running = false
    }

    LaunchedEffect(granted, running) {
        if (!granted || !running) return@LaunchedEffect
        error = null
        runCatching {
            withContext(Dispatchers.IO) {
                val sampleRate = 44100
                val minBuf = runCatching {
                    AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                }.getOrNull() ?: 0
                val bufSize = if (minBuf <= 0) sampleRate * 2 else minBuf * 2
                val record = runCatching {
                    AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufSize
                    )
                }.getOrNull()
                if (record == null) {
                    error = "Microphone unavailable on this device."
                    return@withContext
                }
                try {
                    val started = runCatching { record.startRecording() }
                    if (started.isFailure) {
                        val cause = started.exceptionOrNull()
                        error = if (cause is SecurityException) {
                            "Microphone permission denied. Grant it to use the sound meter."
                        } else {
                            "Microphone failed to start."
                        }
                        return@withContext
                    }
                    val buf = ShortArray((bufSize / 2).coerceAtLeast(1024))
                    var smoothed: Double? = null
                    while (isActive) {
                        val read = runCatching { record.read(buf, 0, buf.size) }.getOrNull() ?: 0
                        if (read <= 0) {
                            runCatching { kotlinx.coroutines.delay(50) }
                            continue
                        }
                        var sum = 0.0
                        for (i in 0 until read) {
                            val s = buf[i].toDouble()
                            if (s.isFinite()) sum += s * s
                        }
                        if (!sum.isFinite()) continue
                        val rms = sqrt(sum / read.toDouble())
                        val db = if (rms <= 0.0 || !rms.isFinite()) {
                            -60.0
                        } else {
                            (20.0 * log10(rms / 32768.0)).coerceIn(-60.0, 0.0)
                        }
                        if (!db.isFinite()) continue
                        val prev = smoothed
                        smoothed = if (prev == null) db else 0.7 * prev + 0.3 * db
                        val shown = (smoothed ?: db).coerceIn(-60.0, 0.0)
                        levelDb = shown
                        val curMin = minDb
                        val curMax = maxDb
                        if (curMin == null || shown < curMin) minDb = shown
                        if (curMax == null || shown > curMax) maxDb = shown
                    }
                } finally {
                    runCatching { record.stop() }
                    runCatching { record.release() }
                }
            }
        }.onFailure { cause ->
            error = if (cause is SecurityException) {
                "Microphone permission denied. Grant it to use the sound meter."
            } else {
                "Microphone error: ${cause.message ?: "unknown"}"
            }
            running = false
        }
    }

    SectionCard("Sound meter") {
        if (!granted) {
            Text(
                "Microphone access is needed to measure sound. Audio is only used for the live level and never stored.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = { runCatching { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) } }) {
                Text("Grant microphone permission")
            }
            error?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            return@SectionCard
        }
        fun fmtFs(v: Double?): String = if (v == null) "—" else runCatching { "${v.toInt()} dBFS" }.getOrDefault("—")
        Text(
            if (levelDb == null) "—" else fmtFs(levelDb),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        val fraction = (((levelDb ?: -60.0) + 60.0) / 60.0).toFloat().coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = fraction,
            modifier = Modifier.fillMaxWidth().height(12.dp)
        )
        ResultLine("Level", if (levelDb == null) "Tap Start…" else fmtFs(levelDb))
        ResultLine("Min", if (minDb == null) "—" else fmtFs(minDb))
        ResultLine("Max", if (maxDb == null) "—" else fmtFs(maxDb))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                error = null
                if (running) {
                    running = false
                } else {
                    val ok = runCatching {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    }.getOrDefault(false)
                    granted = ok
                    if (ok) running = true else runCatching {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }) {
                Text(if (running) "Stop" else "Start")
            }
            Button(onClick = {
                minDb = null
                maxDb = null
            }) {
                Text("Reset min/max")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "0 dBFS is full scale, −60 dBFS is silence. Smoothed live average.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "dBFS is relative to digital full scale, not calibrated SPL — not a legal sound-level meter. Mic sensitivity varies by device; compare against a calibrated meter before trusting absolute values.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            error?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
