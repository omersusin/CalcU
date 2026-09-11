package calc.u.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import calc.u.ui.CalcEffect
import calc.u.ui.CalcViewModel
import calc.u.ui.SectionCard

private val XSubst = Regex("(?<![A-Za-z])x(?![A-Za-z])")

private fun historyBody(entry: String): String {
    val after = entry.substringAfter("|", entry)
    return if (entry.count { it == '|' } >= 2) after.substringBeforeLast("|") else after
}

private fun historyNote(entry: String): String =
    if (entry.count { it == '|' } >= 2) entry.substringAfterLast("|") else ""

@Composable
fun CalculatorScreen(vm: CalcViewModel = hiltViewModel()) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.effects.collect { e ->
            when (e) {
                is CalcEffect.Copy -> {
                    clipboard.setText(AnnotatedString(e.text))
                    snackbar.showSnackbar("Copied")
                }
            }
        }
    }
    val filtered = if (st.query.isBlank()) st.history else st.history.filter { it.contains(st.query, ignoreCase = true) }
    val indexed = st.history.mapIndexed { i, h -> i to h }.filter { (_, h) ->
        st.query.isBlank() || h.contains(st.query, ignoreCase = true)
    }.take(30)
    var noteIndex by remember { mutableStateOf<Int?>(null) }
    var noteDraft by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize()) {
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
                                IconButton(onClick = { vm.onCopyResult() }) {
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
            item {
                OutlinedTextField(
                    value = st.query,
                    onValueChange = { vm.onQueryChange(it) },
                    label = { Text("Search history") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (st.history.isEmpty()) {
                item {
                    Text(
                        "No history yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (filtered.isEmpty()) {
                item {
                    Text(
                        "No matches.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(indexed) { (realIndex, h) ->
                    val note = historyNote(h)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    historyBody(h),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                if (note.isNotBlank()) {
                                    Text(
                                        note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { noteIndex = realIndex; noteDraft = note },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    if (note.isBlank()) Icons.Filled.NoteAdd else Icons.Filled.Edit,
                                    contentDescription = if (note.isBlank()) "Add note" else "Edit note"
                                )
                            }
                        }
                    }
                }
            }
        }
        val editIndex = noteIndex
        if (editIndex != null) {
            AlertDialog(
                onDismissRequest = { noteIndex = null },
                title = { Text("History note") },
                text = {
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        label = { Text("Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { vm.onSetHistoryNote(editIndex, noteDraft.trim()); noteIndex = null }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noteIndex = null }) { Text("Cancel") }
                }
            )
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
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
    var range by remember { mutableStateOf(10) }
    val grid = MaterialTheme.colorScheme.outlineVariant
    val axes = MaterialTheme.colorScheme.outline
    val line = MaterialTheme.colorScheme.primary
    val step = range / 100.0
    val ys = remember(expr, range) {
        var x = -range.toDouble()
        buildList {
            while (x <= range.toDouble()) {
                val y = try {
                    Engine.eval(XSubst.replace(expr, "($x)"), true).getOrNull()?.toDouble() ?: Double.NaN
                } catch (e: Exception) {
                    Double.NaN
                }
                add(y)
                x += step
            }
        }
    }
    val allFailed = ys.all { !it.isFinite() }
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 20, 50).forEach { r ->
                        FilterChip(
                            selected = range == r,
                            onClick = { range = r },
                            label = { Text(r.toString()) }
                        )
                    }
                }
                Text(
                    "Plots x in [-$range, $range] with the same EvalEx engine as the calculator.",
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
                    ys.forEachIndexed { i, y ->
                        val x = -range.toDouble() + i * step
                        if (y.isFinite()) {
                            val px = (w / 2 + x / range * w / 2).toFloat()
                            val py = (h / 2 - y / range * h / 2).toFloat()
                            val p = Offset(px, py)
                            prev?.let { drawLine(line, it, p, strokeWidth = 5f) }
                            prev = p
                        } else {
                            prev = null
                        }
                    }
                }
                if (allFailed) {
                    Text(
                        "Error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
