package calc.u.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calc.u.core.Engine
import calc.u.ui.CalcViewModel
import calc.u.ui.SectionCard

@Composable
fun CalculatorScreen(vm: CalcViewModel = hiltViewModel()) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp).animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        st.input.ifBlank { "0" },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            st.result.ifBlank { "" },
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (st.result.isNotBlank()) {
                            IconButton(onClick = { clipboard.setText(AnnotatedString(st.result)) }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy result")
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = st.angleDeg,
                    onClick = { vm.onToggleAngle() },
                    label = { Text(if (st.angleDeg) "DEG" else "RAD") }
                )
                AssistChip(onClick = { vm.onMemClear() }, label = { Text("MC") })
                AssistChip(onClick = { vm.onMemRecall() }, label = { Text("MR") })
                AssistChip(onClick = { vm.onMemPlus() }, label = { Text("M+") })
                AssistChip(onClick = { vm.onMemMinus() }, label = { Text("M-") })
            }
        }
        item {
            Keypad(
                onKey = { k -> if (k == "=") vm.onEquals() else vm.onInput(k) },
                onClear = { vm.onClear() },
                onBack = { vm.onBackspace() }
            )
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("History", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                if (st.history.isNotEmpty()) {
                    TextButton(onClick = { vm.onClearHistory() }) { Text("Clear") }
                }
            }
        }
        if (st.history.isEmpty()) {
            item {
                Text(
                    "No calculations yet. Results you evaluate will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(st.history.take(30)) { h ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Text(
                        h,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Keypad(onKey: (String) -> Unit, onClear: () -> Unit, onBack: () -> Unit) {
    val digitRows = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "−"),
        listOf("0", ".", "+", "=")
    )
    val sciRows = listOf(
        listOf("(", ")", "^", "√"),
        listOf("sin(", "cos(", "tan(", "π")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sciRows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    OutlinedButton(
                        onClick = { onKey(k) },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text(k, style = MaterialTheme.typography.titleSmall) }
                }
            }
        }
        digitRows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    val isOp = k in setOf("÷", "×", "−", "+")
                    when {
                        k == "=" -> Button(
                            onClick = { onKey(k) },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) { Text(k, style = MaterialTheme.typography.titleMedium) }
                        isOp -> FilledTonalButton(
                            onClick = { onKey(k) },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) { Text(k, style = MaterialTheme.typography.titleMedium) }
                        else -> FilledTonalButton(
                            onClick = { onKey(k) },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) { Text(k, style = MaterialTheme.typography.titleLarge) }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f).height(48.dp)) {
                Text("C", style = MaterialTheme.typography.titleSmall)
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(48.dp)) {
                Icon(Icons.Filled.Backspace, contentDescription = "Backspace")
            }
        }
    }
}

@Composable
fun GraphScreen() {
    var expr by remember { mutableStateOf("sin(x)") }
    val grid = MaterialTheme.colorScheme.outlineVariant
    val axes = MaterialTheme.colorScheme.outline
    val line = MaterialTheme.colorScheme.primary
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard("Function") {
                OutlinedTextField(
                    value = expr,
                    onValueChange = { expr = it },
                    label = { Text("f(x), e.g. sin(x)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Plots x in [-10, 10] with the same EvalEx engine as the calculator.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            ElevatedCard {
                Canvas(Modifier.fillMaxWidth().height(300.dp).padding(8.dp)) {
                    val w = size.width
                    val h = size.height
                    var gx = 0f
                    while (gx <= w) {
                        drawLine(grid, Offset(gx, 0f), Offset(gx, h))
                        gx += w / 20
                    }
                    var gy = 0f
                    while (gy <= h) {
                        drawLine(grid, Offset(0f, gy), Offset(w, gy))
                        gy += h / 12
                    }
                    drawLine(axes, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth = 3f)
                    drawLine(axes, Offset(w / 2, 0f), Offset(w / 2, h), strokeWidth = 3f)
                    var prev: Offset? = null
                    var x = -10.0
                    while (x <= 10.0) {
                        val y = try {
                            Engine.eval(expr.replace("x", "($x)"), true).getOrNull()?.toDouble() ?: Double.NaN
                        } catch (e: Exception) {
                            Double.NaN
                        }
                        if (y.isFinite()) {
                            val px = (w / 2 + x / 10 * w / 2).toFloat()
                            val py = (h / 2 - y.toFloat() / 10 * h / 2).toFloat()
                            val p = Offset(px, py)
                            prev?.let { drawLine(line, it, p, strokeWidth = 5f) }
                            prev = p
                        } else {
                            prev = null
                        }
                        x += 0.1
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderBox() {
    Card(Modifier.fillMaxWidth().heightIn(min = 0.dp)) { }
}
