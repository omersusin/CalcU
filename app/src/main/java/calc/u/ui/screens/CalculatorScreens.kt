package calc.u.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calc.u.ui.CalcViewModel

@Composable
fun CalculatorScreen(vm: CalcViewModel = hiltViewModel()) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = st.input, onValueChange = {}, readOnly = true, label = { Text("Input") }, modifier = Modifier.fillMaxWidth())
        Text(st.result, style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = st.angleDeg, onClick = { vm.onToggleAngle() }, label = { Text(if (st.angleDeg) "DEG" else "RAD") })
            AssistChip(onClick = { vm.onMemClear() }, label = { Text("MC") })
            AssistChip(onClick = { vm.onMemRecall() }, label = { Text("MR") })
            AssistChip(onClick = { vm.onMemPlus() }, label = { Text("M+") })
            AssistChip(onClick = { vm.onMemMinus() }, label = { Text("M-") })
        }
        val rows = listOf(
            listOf("7","8","9","÷"),
            listOf("4","5","6","×"),
            listOf("1","2","3","−"),
            listOf("0",".","+","="),
            listOf("(",")","^","√"),
            listOf("sin(","cos(","tan(","π")
        )
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    Button(onClick = { if (k == "=") vm.onEquals() else vm.onInput(k) }, modifier = Modifier.weight(1f)) { Text(k) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.onClear() }) { Text("C") }
            OutlinedButton(onClick = { vm.onBackspace() }) { Text("⌫") }
        }
        Text("History", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(st.history.take(30)) { h -> Text(h, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun GraphScreen() {
    var expr by remember { mutableStateOf("sin(x)") }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(expr, { expr = it }, label = { Text("f(x), e.g. sin(x)") }, modifier = Modifier.fillMaxWidth())
        Card(Modifier.fillMaxWidth().height(280.dp)) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                val w = size.width; val h = size.height
                drawLine(androidx.compose.ui.graphics.Color.Gray, start = androidx.compose.ui.geometry.Offset(0f, h / 2), end = androidx.compose.ui.geometry.Offset(w, h / 2))
                drawLine(androidx.compose.ui.graphics.Color.Gray, start = androidx.compose.ui.geometry.Offset(w / 2, 0f), end = androidx.compose.ui.geometry.Offset(w / 2, h))
                var prev: androidx.compose.ui.geometry.Offset? = null
                var x = -10.0
                while (x <= 10.0) {
                    val y = try {
                        calc.u.core.Engine.eval(expr.replace("x", "($x)"), true).getOrNull()?.toDouble() ?: Double.NaN
                    } catch (e: Exception) { Double.NaN }
                    if (y.isFinite()) {
                        val px = (w / 2 + x / 10 * w / 2).toFloat()
                        val py = (h / 2 - y.toFloat() / 10 * h / 2)
                        val p = androidx.compose.ui.geometry.Offset(px, py)
                        prev?.let { drawLine(androidx.compose.ui.graphics.Color(0xFF0067C0), it, p, strokeWidth = 4f) }
                        prev = p
                    } else prev = null
                    x += 0.1
                }
            }
        }
        Text("Plots f(x) for x in [-10,10]. Uses the same EvalEx engine as the calculator.")
    }
}
