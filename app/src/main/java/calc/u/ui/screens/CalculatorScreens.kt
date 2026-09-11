package calc.u.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calc.u.core.Engine
import calc.u.ui.CalcEffect
import calc.u.ui.CalcViewModel
import calc.u.ui.FluentCalcKey
import calc.u.ui.FluentInfoBar
import calc.u.ui.FluentKeyKind
import calc.u.ui.FluentStagger
import calc.u.ui.FluentTeachingTip
import calc.u.ui.SectionCard
import calc.u.ui.WARNING
import calc.u.ui.theme.FluentElevation
import calc.u.ui.theme.FluentMotion
import calc.u.ui.theme.FluentStroke
import calc.u.ui.tintExpression
import com.microsoft.fluentui.tokenized.bottomsheet.BottomSheet
import com.microsoft.fluentui.tokenized.bottomsheet.BottomSheetValue
import com.microsoft.fluentui.tokenized.bottomsheet.rememberBottomSheetState
import kotlinx.coroutines.launch

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
    val vibration by vm.vibration.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    fun tapFeedback() {
        if (vibration) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
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
    var noteIndex by remember { mutableStateOf<Int?>(null) }
    var noteDraft by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val historySheet = rememberBottomSheetState(BottomSheetValue.Hidden)
    BottomSheet(
        sheetContent = {
            HistorySheetContent(
                history = st.history,
                query = st.query,
                onQuery = { vm.onQueryChange(it) },
                onClear = { vm.onClearHistory() },
                onTap = { h -> vm.onHistoryTap(h); scope.launch { historySheet.hide() } },
                onNote = { i, n -> noteIndex = i; noteDraft = n },
                onDelete = { vm.onDeleteHistoryAt(it) }
            )
        },
        sheetState = historySheet,
        expandable = true,
        peekHeight = 480.dp,
        scrimVisible = true,
        enableSwipeDismiss = true,
        onDismiss = {}
    ) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val stroke = if (isSystemInDarkTheme()) FluentStroke.CardDark else FluentStroke.CardLight
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = FluentElevation.Display),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(
                        Modifier.border(BorderStroke(1.dp, stroke), MaterialTheme.shapes.medium)
                    ) {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp).animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            tintExpression(
                                st.input.ifBlank { "0" },
                                MaterialTheme.colorScheme.onSurface,
                                MaterialTheme.colorScheme.primary
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AnimatedContent(
                            targetState = st.result,
                            transitionSpec = {
                                (slideInVertically(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) { it / 4 } + fadeIn()) togetherWith
                                    (slideOutVertically(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) { -it / 4 } + fadeOut())
                            },
                            label = "result"
                        ) { target ->
                            Text(
                                target.ifBlank { "" },
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { scope.launch { historySheet.show() } },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.History, contentDescription = "Open history")
                            }
                            if (st.result.isNotBlank()) {
                                IconButton(
                                    onClick = { vm.onCopyResult() },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy result")
                                }
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
                    onKey = { k -> tapFeedback(); if (k == "=") vm.onEquals() else vm.onInput(k) },
                    onClear = { tapFeedback(); vm.onClear() },
                    onBack = { tapFeedback(); vm.onBackspace() },
                    onBackLong = {
                        if (vibration) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.onClear()
                    }
                )
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
        if (st.showGraphTip) {
            FluentTeachingTip(
                title = "Graph anything",
                subtitle = "Type sin(x), then open the chart tab to plot it.",
                onClose = { vm.onDismissGraphTip() },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            )
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistorySheetContent(
    history: List<String>,
    query: String,
    onQuery: (String) -> Unit,
    onClear: () -> Unit,
    onTap: (String) -> Unit,
    onNote: (Int, String) -> Unit,
    onDelete: (Int) -> Unit
) {
    var selected by remember { mutableStateOf(setOf<Int>()) }
    val inSelection = selected.isNotEmpty()
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(listState) {
        var prev = listState.firstVisibleItemIndex
        snapshotFlow { listState.firstVisibleItemIndex }.collect {
            if (it != prev) {
                prev = it
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }
    val indexed = history.mapIndexed { i, h -> i to h }.filter { (_, h) ->
        query.isBlank() || h.contains(query, ignoreCase = true)
    }.take(50)
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("History", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            if (history.isNotEmpty()) {
                TextButton(onClick = onClear) { Text("Clear") }
            }
        }
        AnimatedVisibility(
            visible = inSelection,
            enter = slideInVertically(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) + fadeIn(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${selected.size} selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            selected.sortedDescending().forEach { onDelete(it) }
                            selected = emptySet()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete selected")
                    }
                    IconButton(
                        onClick = { selected = emptySet() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close selection")
                    }
                }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            label = { Text("Search history") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(50.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "${indexed.size} entries",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth().height(360.dp)) {
            if (history.isEmpty()) {
                item {
                    Text(
                        "No history yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (indexed.isEmpty()) {
                item {
                    Text(
                        "No matches.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                itemsIndexed(indexed, key = { _, p -> p.first }) { pos, (realIndex, h) ->
                    val note = historyNote(h)
                    val isSelected = realIndex in selected
                    FluentStagger(pos) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = LocalIndication.current,
                                    onClick = {
                                        if (inSelection) {
                                            selected = if (isSelected) selected - realIndex else selected + realIndex
                                        } else {
                                            onTap(h)
                                        }
                                    },
                                    onLongClick = { selected = selected + realIndex }
                                ),
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
                                onClick = { onNote(realIndex, note) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    if (note.isBlank()) Icons.Filled.NoteAdd else Icons.Filled.Edit,
                                    contentDescription = if (note.isBlank()) "Add note" else "Edit note"
                                )
                            }
                            IconButton(
                                onClick = { onDelete(realIndex) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete entry")
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Keypad(
    onKey: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onBackLong: () -> Unit
) {
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
    fun rowEnter(delay: Int) =
        fadeIn(tween(FluentMotion.Medium, delayMillis = delay, easing = FluentMotion.Standard)) +
            slideInVertically(tween(FluentMotion.Medium, delayMillis = delay, easing = FluentMotion.Standard)) { it / 8 }
    val backInteractions = remember { MutableInteractionSource() }
    val backPressed by backInteractions.collectIsPressedAsState()
    val backScale by animateFloatAsState(
        if (backPressed) 0.95f else 1f,
        animationSpec = tween(FluentMotion.Short, easing = FluentMotion.Standard),
        label = "back-press"
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sciRows.forEachIndexed { i, row ->
            AnimatedVisibility(visible = true, enter = rowEnter(i * 40)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { k ->
                        FluentCalcKey(
                            label = k,
                            onClick = { onKey(k) },
                            modifier = Modifier.weight(1f),
                            kind = FluentKeyKind.Sci,
                            keyHeight = 48.dp
                        )
                    }
                }
            }
        }
        digitRows.forEachIndexed { j, row ->
            AnimatedVisibility(visible = true, enter = rowEnter((j + sciRows.size) * 40)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { k ->
                        val isOp = k in setOf("÷", "×", "−", "+")
                        FluentCalcKey(
                            label = k,
                            onClick = { onKey(k) },
                            modifier = Modifier.weight(1f),
                            kind = when {
                                k == "=" -> FluentKeyKind.Equals
                                isOp -> FluentKeyKind.Operator
                                else -> FluentKeyKind.Digit
                            },
                            keyHeight = 60.dp
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = true,
            enter = rowEnter((sciRows.size + digitRows.size) * 40)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FluentCalcKey(
                    label = "C",
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    kind = FluentKeyKind.Sci,
                    keyHeight = 48.dp
                )
                Box(
                    modifier = Modifier.weight(1f).height(48.dp)
                        .graphicsLayer(scaleX = backScale, scaleY = backScale)
                        .clip(ButtonDefaults.outlinedShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, ButtonDefaults.outlinedShape)
                        .combinedClickable(
                            interactionSource = backInteractions,
                            indication = LocalIndication.current,
                            onClick = onBack,
                            onLongClick = onBackLong
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Backspace, contentDescription = "Backspace, long-press to clear")
                }
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
                    FluentInfoBar(
                        severity = WARNING,
                        title = "Cannot plot",
                        message = "No valid points for this expression in range.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
