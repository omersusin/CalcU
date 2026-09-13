package calc.u.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calc.u.ui.SectionCard
import kotlin.math.floor

@Composable
fun RulerScreen() {
    val ctx = LocalContext.current
    val densityDpi = remember(ctx) { ctx.resources.displayMetrics.densityDpi }
    val pxPerMm = densityDpi / 25.4f
    var metric by remember { mutableStateOf(true) }
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 12.sp,
        color = textColor,
        textAlign = TextAlign.Center
    )
    fun labelLayout(text: String) = measurer.measure(text, labelStyle)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionCard("Ruler") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = metric,
                    onClick = { metric = true },
                    label = { Text("cm") }
                )
                FilterChip(
                    selected = !metric,
                    onClick = { metric = false },
                    label = { Text("inch") }
                )
            }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val density = LocalDensity.current
                val widthPx = with(density) { maxWidth.toPx() }
                Canvas(Modifier.fillMaxWidth().height(140.dp)) {
                    drawLine(tickColor, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 4f)
                    if (metric) {
                        val totalMm = floor(widthPx / pxPerMm).toInt()
                        for (mm in 0..totalMm) {
                            val x = mm * pxPerMm
                            val len = when {
                                mm % 10 == 0 -> 56f
                                mm % 5 == 0 -> 40f
                                else -> 24f
                            }
                            val wide = if (mm % 5 == 0) 3f else 2f
                            drawLine(tickColor, Offset(x, 0f), Offset(x, len), strokeWidth = wide)
                            if (mm % 10 == 0 && mm > 0) {
                                val layout = labelLayout("${mm / 10}")
                                drawText(
                                    layout,
                                    topLeft = Offset(x - layout.size.width / 2f, len + 8f)
                                )
                            }
                        }
                    } else {
                        val pxPerInch = pxPerMm * 25.4f
                        val totalSixteenths = floor(widthPx / pxPerInch * 16).toInt()
                        for (s in 0..totalSixteenths) {
                            val x = s * pxPerInch / 16f
                            val len = when {
                                s % 16 == 0 -> 56f
                                s % 8 == 0 -> 48f
                                s % 4 == 0 -> 40f
                                s % 2 == 0 -> 32f
                                else -> 24f
                            }
                            val wide = if (s % 2 == 0) 3f else 2f
                            drawLine(tickColor, Offset(x, 0f), Offset(x, len), strokeWidth = wide)
                            if (s % 16 == 0 && s > 0) {
                                val layout = labelLayout("${s / 16}")
                                drawText(
                                    layout,
                                    topLeft = Offset(x - layout.size.width / 2f, len + 8f)
                                )
                            }
                        }
                    }
                }
            }
            Text(
                "Screen rulers are approximate — check against a real ruler.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
