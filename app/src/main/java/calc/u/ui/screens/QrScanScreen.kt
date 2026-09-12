package calc.u.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import calc.u.ui.SectionCard
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private class PackedYuv(val data: ByteArray, val width: Int, val height: Int)

private fun packYPlane(image: ImageProxy): PackedYuv? {
    return runCatching {
        val w = image.width
        val h = image.height
        if (w <= 0 || h <= 0) return@runCatching null
        val yPlane = image.planes.getOrNull(0) ?: return@runCatching null
        val buffer = yPlane.buffer
        val rowStride = yPlane.rowStride
        val pixelStride = yPlane.pixelStride
        if (rowStride <= 0) return@runCatching null
        val out = ByteArray(w * h)
        runCatching { buffer.rewind() }.getOrNull() ?: return@runCatching null
        if (pixelStride == 1 && rowStride == w) {
            runCatching { buffer.get(out, 0, w * h) }.getOrNull() ?: return@runCatching null
        } else {
            val rowTmp = ByteArray(rowStride)
            var destPos = 0
            for (r in 0 until h) {
                runCatching { buffer.get(rowTmp, 0, rowStride) }.getOrNull() ?: return@runCatching null
                if (pixelStride == 1) {
                    runCatching { System.arraycopy(rowTmp, 0, out, destPos, w) }
                } else {
                    var src = 0
                    for (c in 0 until w) {
                        val idx = src
                        if (idx < rowTmp.size && destPos + c < out.size) {
                            out[destPos + c] = rowTmp[idx]
                        }
                        src += pixelStride
                    }
                }
                destPos += w
            }
        }
        PackedYuv(out, w, h)
    }.getOrNull()
}

private class QrCodeAnalyzer(
    private val isActive: () -> Boolean,
    private val onDecoded: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val reader = MultiFormatReader()

    override fun analyze(image: ImageProxy) {
        try {
            if (!isActive()) return
            runCatching {
                val packed = packYPlane(image) ?: return@runCatching
                val source = PlanarYUVLuminanceSource(
                    packed.data, packed.width, packed.height,
                    0, 0, packed.width, packed.height, false
                )
                val bitmap = BinaryBitmap(HybridBinarizer(source))
                val decoded = try {
                    reader.decode(bitmap)
                } catch (e: NotFoundException) {
                    null
                }
                runCatching { reader.reset() }
                val text = decoded?.text
                if (!text.isNullOrEmpty()) onDecoded(text)
            }
        } finally {
            runCatching { image.close() }
        }
    }
}

@Composable
fun QrScanScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboard = LocalClipboardManager.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    var hasPermission by remember {
        mutableStateOf(
            runCatching {
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)
        )
    }
    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scanningEnabled = remember { AtomicBoolean(true) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember(context) {
        runCatching { PreviewView(context) }.getOrNull()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) error = "Camera permission is needed to scan codes."
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            runCatching { permissionLauncher.launch(Manifest.permission.CAMERA) }
        }
    }

    val analyzer = remember {
        QrCodeAnalyzer(
            isActive = { scanningEnabled.get() },
            onDecoded = { text ->
                if (scanningEnabled.compareAndSet(true, false)) {
                    runCatching { mainExecutor.execute { result = text } }
                }
            }
        )
    }

    LaunchedEffect(previewView, hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        val pv = previewView ?: return@LaunchedEffect
        runCatching {
            val provider = runCatching { ProcessCameraProvider.getInstance(context).get() }.getOrNull()
            if (provider == null) {
                mainExecutor.execute { error = "Camera is unavailable on this device." }
                return@LaunchedEffect
            }
            val preview = Preview.Builder().build()
            runCatching { preview.setSurfaceProvider(pv.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            runCatching { analysis.setAnalyzer(executor, analyzer) }
            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            runCatching { provider.unbindAll() }
            runCatching {
                provider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
            }.onFailure { e ->
                mainExecutor.execute { error = e.message ?: "Could not start camera." }
            }
        }.onFailure { e ->
            mainExecutor.execute { error = e.message ?: "Could not start camera." }
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            runCatching {
                val future = runCatching { ProcessCameraProvider.getInstance(context) }.getOrNull()
                if (future != null && future.isDone) {
                    runCatching { future.get()?.unbindAll() }
                }
            }
            runCatching { executor.shutdown() }
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (!hasPermission) {
            SectionCard("Camera permission") {
                Text(
                    "Scanning needs camera access. Grant it to preview and decode QR and barcodes on-device.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = { runCatching { permissionLauncher.launch(Manifest.permission.CAMERA) } }) {
                    Text("Grant camera access")
                }
                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            return@Column
        }
        if (previewView == null) {
            SectionCard("Scanner unavailable") {
                Text(
                    "Preview view could not be created on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            return@Column
        }
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(MaterialTheme.shapes.large)
        )
        error?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        val current = result
        if (current == null) {
            SectionCard("Point at a code") {
                Text(
                    "Point the camera at a QR code or barcode. It decodes on-device.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            SectionCard("Scan result") {
                Text(current, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { runCatching { clipboard.setText(AnnotatedString(current)) } }) {
                        Text("Copy")
                    }
                    OutlinedButton(onClick = {
                        runCatching {
                            result = null
                            error = null
                            scanningEnabled.set(true)
                        }
                    }) {
                        Text("Rescan")
                    }
                }
            }
        }
    }
}
