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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import calc.u.core.AutocompleteIndex
import calc.u.core.Engine
import calc.u.core.Suggestion
import calc.u.data.TapeHolder
import calc.u.ui.CalcEffect
import calc.u.ui.CalcViewModel
import calc.u.ui.QuickOverlay
import calc.u.ui.QuickSettings
import calc.u.ui.FluentCalcKey
import calc.u.ui.FluentInfoBar
import calc.u.ui.FluentKeyKind
import calc.u.ui.FluentStagger
import calc.u.ui.FluentTeachingTip
import calc.u.ui.SectionCard
import calc.u.ui.WARNING
import calc.u.ui.theme.FluentMotion
import calc.u.ui.tintExpression
import kotlinx.coroutines.delay
import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Checkbox
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val XSubst = Regex("(?<![A-Za-z])x(?![A-Za-z])")

private fun historyBody(entry: String): String {
    val after = entry.substringAfter("|", entry)
    return if (entry.count { it == '|' } >= 2) after.substringBeforeLast("|") else after
}

private fun historyNote(entry: String): String =
    if (entry.count { it == '|' } >= 2) entry.substringAfterLast("|") else ""

private val MiniGraphHint = Regex("(sin|cos|tan|asin|acos|atan|log|ln|sqrt|\\^|/|\\*|\\(|\\d)")

@Composable
private fun DisplayMiniGraph(input: String, modifier: Modifier = Modifier) {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return
    if (!XSubst.containsMatchIn(trimmed)) return
    if (!MiniGraphHint.containsMatchIn(trimmed)) return
    if (trimmed.filter { !it.isWhitespace() }.length < 2) return
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axesColor = MaterialTheme.colorScheme.outline
    val lineColor = MaterialTheme.colorScheme.primary
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(12.dp)
        ) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas
            val ppu = 40f
            var gx = -w / 2
            while (gx <= w / 2) {
                drawLine(gridColor, Offset(w / 2 + gx, 0f), Offset(w / 2 + gx, h))
                gx += ppu
            }
            var gy = -h / 2
            while (gy <= h / 2) {
                drawLine(gridColor, Offset(0f, h / 2 + gy), Offset(w, h / 2 + gy))
                gy += ppu
            }
            drawLine(axesColor, Offset(w / 2, 0f), Offset(w / 2, h), strokeWidth = 3f)
            drawLine(axesColor, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth = 3f)
            var prev: Offset? = null
            var px = 0f
            while (px <= w) {
                val mx = ((px - w / 2) / ppu).toDouble()
                val y = evalGraphAt(trimmed, mx)
                if (y.isFinite()) {
                    val py = (h / 2 - y * ppu).toFloat()
                    val p = Offset(px, py)
                    if (prev != null) drawLine(lineColor, prev, p, strokeWidth = 5f)
                    prev = p
                } else {
                    prev = null
                }
                px += 2f
            }
        }
    }
}

@Composable
private fun InlineTape(
    onRecall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tape by TapeHolder.entries.collectAsStateWithLifecycle()
    if (tape.isEmpty()) return
    val listState = rememberLazyListState()
    val lastId = tape.lastOrNull()?.id
    LaunchedEffect(lastId) {
        runCatching {
            if (tape.isNotEmpty()) listState.scrollToItem(tape.lastIndex)
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth().heightIn(max = 120.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(tape, key = { it.id }) { e ->
            Text(
                e.expression + " = " + e.result,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
                    .clickable { onRecall(e.result) }
                    .padding(vertical = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalculatorScreen(vm: CalcViewModel = hiltViewModel()) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val vibration by vm.vibration.collectAsStateWithLifecycle()
    val fractions by vm.fractions.collectAsStateWithLifecycle()
    val memoryRow by vm.memoryRow.collectAsStateWithLifecycle()
    val numberFormat by vm.numberFormat.collectAsStateWithLifecycle()
    val keepScreenOn by vm.keepScreenOn.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    fun tapFeedback() {
        if (vibration) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        runCatching {
            vm.effects.collect { e ->
                when (e) {
                    is CalcEffect.Copy -> {
                        runCatching { clipboard.setText(AnnotatedString(e.text)) }
                        runCatching { snackbar.showSnackbar("Copied") }
                    }
                }
            }
        }
    }
    var noteIndex by remember { mutableStateOf<Int?>(null) }
    var noteDraft by remember { mutableStateOf("") }
    var historyOpen by remember { mutableStateOf(false) }
    var quickOpen by remember { mutableStateOf(false) }
    var inverse by remember { mutableStateOf(false) }
    var percentMode by rememberSaveable { mutableStateOf("off") }
    remember {
        AutocompleteIndex.build(
            listOf("sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt")
                .map { Suggestion.function(it) },
            listOf("m", "km", "ft", "mi", "kg", "lb", "N", "J", "W", "Pa", "s", "min")
                .map { Suggestion.unit(it) }
        )
    }
    val completion = remember(st.input) { AutocompleteIndex.query(st.input, st.input.length) }
    fun displayResult(target: String): String {
        if (percentMode == "off" || target.isBlank()) return target
        val v = runCatching {
            java.text.NumberFormat.getInstance().parse(target.trim())?.toDouble()
        }.getOrNull() ?: return target
        if (!v.isFinite()) return target
        return runCatching { Engine.formatPercentMode(v, percentMode) }.getOrDefault(target)
    }
    if (historyOpen) {
        ModalBottomSheet(
            onDismissRequest = { historyOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            HistorySheetContent(
                history = st.history,
                query = st.query,
                onQuery = { vm.onQueryChange(it) },
                onClear = { vm.onClearHistory() },
                onTap = { h -> vm.onHistoryTap(h); historyOpen = false },
                onNote = { i, n -> noteIndex = i; noteDraft = n },
                onDelete = { vm.onDeleteHistoryAt(it) }
            )
        }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InlineTape(onRecall = { vm.onTapeRecall(it) })
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                runCatching { haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
                                quickOpen = true
                            }
                        )
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp).animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            tintExpression(
                                st.input.ifBlank { "0" },
                                MaterialTheme.colorScheme.onSurface,
                                MaterialTheme.colorScheme.onSurface
                            ),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (completion.items.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(completion.items, key = { it.name }) { s ->
                                    AssistChip(
                                        onClick = {
                                            tapFeedback()
                                            val count = completion.end - completion.start
                                            repeat(count) { vm.onBackspace() }
                                            vm.onInput(s.insertBefore + s.insertAfter)
                                        },
                                        label = { Text(s.name) }
                                    )
                                }
                            }
                            Text(
                                completion.relevantText + " → " +
                                    completion.items.first().title.ifBlank {
                                        completion.items.first().name
                                    },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        AnimatedContent(
                            targetState = st.result,
                            transitionSpec = {
                                (slideInVertically(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) { it / 5 } + fadeIn()) togetherWith
                                    (slideOutVertically(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) { -it / 5 } + fadeOut())
                            },
                            label = "result"
                        ) { target ->
                            Text(
                                displayResult(target),
                                style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = { historyOpen = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.History, contentDescription = "Open history")
                            }
                            if (st.result.isNotBlank()) {
                                FilledTonalIconButton(
                                    onClick = { vm.onCopyResult() },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy result")
                                }
                            }
                        }
                    }
                }
                DisplayMiniGraph(input = st.input)
                if (quickOpen) {
                    QuickOverlay(
                        expanded = true,
                        onDismiss = { quickOpen = false },
                        settings = QuickSettings(
                            vibration = vibration,
                            fractions = fractions,
                            memoryRow = memoryRow,
                            keepScreenOn = keepScreenOn,
                            onVibration = { vm.onVibration(it) },
                            onFractions = { vm.onFractions(it) },
                            onMemoryRow = { vm.onMemoryRow(it) },
                            onKeepScreenOn = { vm.onKeepScreenOn(it) }
                        ),
                        numberFormat = numberFormat
                    )
                }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = st.angleMode == "DEG",
                        onClick = { vm.onToggleAngle() },
                        label = {
                            val mode = when (st.angleMode) {
                                "RAD" -> "RAD"
                                "GRA" -> "GRA"
                                else -> "DEG"
                            }
                            Text(
                                mode,
                                fontWeight = if (mode == "DEG") FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    )
                    FilterChip(
                        selected = inverse,
                        onClick = { inverse = !inverse },
                        label = {
                            Text(
                                "INV",
                                fontWeight = if (inverse) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    )
                    if (st.showMemoryRow) {
                        AssistChip(onClick = { vm.onMemClear() }, label = { Text("MC") })
                        AssistChip(onClick = { vm.onMemRecall() }, label = { Text("MR") })
                        AssistChip(onClick = { vm.onMemPlus() }, label = { Text("M+") })
                        AssistChip(onClick = { vm.onMemMinus() }, label = { Text("M-") })
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = percentMode == "percent",
                        onClick = {
                            tapFeedback()
                            percentMode = if (percentMode == "percent") "off" else "percent"
                        },
                        label = { Text("%") }
                    )
                    FilterChip(
                        selected = percentMode == "permille",
                        onClick = {
                            tapFeedback()
                            percentMode = if (percentMode == "permille") "off" else "permille"
                        },
                        label = { Text("‰") }
                    )
                }
            }
            item {
                val keypadLayout by vm.keypadLayout.collectAsStateWithLifecycle()
                Keypad(
                    onKey = { k ->
                        tapFeedback()
                        when (k) {
                            "=" -> vm.onEquals()
                            "ANS" -> vm.onAns()
                            "x²" -> vm.onInput("^2")
                            else -> vm.onInput(k)
                        }
                    },
                    onClear = { tapFeedback(); vm.onClear() },
                    onBack = { tapFeedback(); vm.onBackspace() },
                    onBackLong = {
                        if (vibration) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.onClear()
                    },
                    inverse = inverse,
                    layout = keypadLayout
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistorySheetContent(
    history: List<String>,
    query: String,
    onQuery: (String) -> Unit,
    onClear: () -> Unit,
    onTap: (String) -> Unit,
    onNote: (Int, String) -> Unit,
    onDelete: (Int) -> Unit,
    activity: Map<Long, Int> = emptyMap()
) {
    var selected by remember { mutableStateOf(setOf<Int>()) }
    val inSelection = selected.isNotEmpty()
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(listState) {
        var prev = runCatching { listState.firstVisibleItemIndex }.getOrDefault(0)
        runCatching {
            snapshotFlow { listState.firstVisibleItemIndex }.collect {
                if (it != prev) {
                    prev = it
                    runCatching { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                }
            }
        }
    }
    val indexed = history.mapIndexed { i, h -> i to h }.filter { (_, h) ->
        query.isBlank() || h.contains(query, ignoreCase = true)
    }.take(50)
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val today = System.currentTimeMillis() / 86400000L
                val days = (0..13).map { today - 13 + it }
                val max = days.maxOfOrNull { activity[it] ?: 0 } ?: 0
                days.forEach { d ->
                    val c = activity[d] ?: 0
                    val alpha = when {
                        c <= 0 -> 0.12f
                        max <= 1 -> 1f
                        else -> 0.25f + 0.75f * (c.toFloat() / max.toFloat())
                    }
                    Box(
                        Modifier.size(20.dp).clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                    )
                }
            }
            Text(
                "last 14 days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("History", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
            if (history.isNotEmpty()) {
                TextButton(onClick = onClear) { Text("Clear") }
            }
        }
        AnimatedVisibility(
            visible = inSelection,
            enter = slideInVertically(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) + fadeIn(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.large
            ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${selected.size} selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
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
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            label = { Text("Search history") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
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
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = MaterialTheme.shapes.large
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

private val InverseLongPress = mapOf(
    "sin(" to "asin(",
    "cos(" to "acos(",
    "tan(" to "atan(",
    "asin(" to "sin(",
    "acos(" to "cos(",
    "atan(" to "tan(",
    "log(" to "10^(",
    "ln(" to "e^(",
    "10^(" to "log(",
    "e^(" to "ln("
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SciKey(
    label: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (onLongClick == null) {
        FluentCalcKey(
            label = label,
            onClick = onClick,
            modifier = modifier,
            kind = FluentKeyKind.Sci,
            keyHeight = 56.dp
        )
        return
    }
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = modifier.height(56.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    runCatching { haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
                    onLongClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Keypad(
    onKey: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onBackLong: () -> Unit,
    inverse: Boolean = false,
    layout: String = "simple"
) {
    when (layout) {
        "classic" -> ClassicKeypad(onKey, onClear, onBack, onBackLong, inverse)
        "modern" -> ModernKeypad(onKey, onClear, onBack, onBackLong, inverse)
        else -> SimpleKeypad(onKey, onClear, onBack, onBackLong, inverse)
    }
}

private fun keypadRowEnter(delay: Int) =
    fadeIn(tween(FluentMotion.Medium, delayMillis = delay, easing = FluentMotion.Standard)) +
        slideInVertically(tween(FluentMotion.Medium, delayMillis = delay, easing = FluentMotion.Standard)) { it / 10 }

private val DigitRows = listOf(
    listOf("7", "8", "9", "÷"),
    listOf("4", "5", "6", "×"),
    listOf("1", "2", "3", "−"),
    listOf("0", ".", "+", "=")
)

private fun sciRowsFor(inverse: Boolean) = if (!inverse) listOf(
    listOf("(", ")", "^", "√"),
    listOf("sin(", "cos(", "tan(", "π"),
    listOf("log(", "ln(", "x²", "1/(")
) else listOf(
    listOf("(", ")", "^", "x²"),
    listOf("asin(", "acos(", "atan(", "π"),
    listOf("10^(", "e^(", "√", "1/(")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DigitKey(
    label: String,
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Long-press on a digit inserts plain "^n" text (never superscript
    // characters); Engine evaluates "^" as power, guaranteed.
    if (label.length != 1 || label[0] !in '0'..'9') {
        FluentCalcKey(
            label = label,
            onClick = { onKey(label) },
            modifier = modifier,
            kind = FluentKeyKind.Digit,
            keyHeight = 64.dp
        )
        return
    }
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = modifier.height(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .combinedClickable(
                onClick = { onKey(label) },
                onLongClick = {
                    runCatching { haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
                    onKey("^$label")
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BackKey(
    onBack: () -> Unit,
    onBackLong: () -> Unit,
    modifier: Modifier = Modifier,
    tall: Boolean = false
) {
    val backInteractions = remember { MutableInteractionSource() }
    val backPressed by backInteractions.collectIsPressedAsState()
    val backScale by animateFloatAsState(
        if (backPressed) 0.96f else 1f,
        animationSpec = tween(FluentMotion.Short, easing = FluentMotion.Standard),
        label = "back-press"
    )
    Box(
        modifier = modifier.height(if (tall) 64.dp else 56.dp)
            .graphicsLayer(scaleX = backScale, scaleY = backScale)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.inverseSurface)
            .combinedClickable(
                interactionSource = backInteractions,
                indication = LocalIndication.current,
                onClick = onBack,
                onLongClick = onBackLong
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Backspace,
            contentDescription = "Backspace, long-press to clear",
            tint = MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}

@Composable
private fun SciRowsGrid(
    onKey: (String) -> Unit,
    inverse: Boolean,
    staggerBase: Int = 0
) {
    val sciRows = sciRowsFor(inverse)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sciRows.forEachIndexed { i, row ->
            AnimatedVisibility(visible = true, enter = keypadRowEnter((i + staggerBase) * 32)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { k ->
                        val alt = InverseLongPress[k]
                        SciKey(
                            label = k,
                            onClick = { onKey(if (k == "x²") "^2" else k) },
                            onLongClick = alt?.let { a -> { onKey(if (a == "x²") "^2" else a) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DigitRowsGrid(
    onKey: (String) -> Unit,
    staggerBase: Int = 3
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DigitRows.forEachIndexed { j, row ->
            AnimatedVisibility(visible = true, enter = keypadRowEnter((j + staggerBase) * 32)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { k ->
                        if (k.length == 1 && k[0] in '0'..'9') {
                            DigitKey(label = k, onKey = onKey, modifier = Modifier.weight(1f))
                        } else {
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
                                keyHeight = 64.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClearBackRow(
    onClear: () -> Unit,
    onBack: () -> Unit,
    onBackLong: () -> Unit,
    staggerDelay: Int = 224
) {
    AnimatedVisibility(visible = true, enter = keypadRowEnter(staggerDelay)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FluentCalcKey(
                label = "C",
                onClick = onClear,
                modifier = Modifier.weight(1f),
                kind = FluentKeyKind.Sci,
                keyHeight = 56.dp
            )
            BackKey(onBack = onBack, onBackLong = onBackLong, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SimpleKeypad(
    onKey: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onBackLong: () -> Unit,
    inverse: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SciRowsGrid(onKey = onKey, inverse = inverse, staggerBase = 0)
        DigitRowsGrid(onKey = onKey, staggerBase = 3)
        ClearBackRow(onClear = onClear, onBack = onBack, onBackLong = onBackLong)
    }
}

@Composable
private fun ClassicKeypad(
    onKey: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onBackLong: () -> Unit,
    inverse: Boolean
) {
    var sciOpen by rememberSaveable { mutableStateOf(true) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { sciOpen = !sciOpen }) {
                Text(if (sciOpen) "Scientific ▾" else "Scientific ▸")
            }
        }
        AnimatedVisibility(
            visible = sciOpen,
            enter = keypadRowEnter(0),
            exit = fadeOut(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) +
                slideOutVertically(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) { it / 10 }
        ) {
            SciRowsGrid(onKey = onKey, inverse = inverse, staggerBase = 0)
        }
        DigitRowsGrid(onKey = onKey, staggerBase = 0)
        ClearBackRow(
            onClear = onClear,
            onBack = onBack,
            onBackLong = onBackLong,
            staggerDelay = 128
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModernKeypad(
    onKey: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onBackLong: () -> Unit,
    inverse: Boolean
) {
    val mergedRows = listOf(
        listOf("7", "8", "9", "(", "÷"),
        listOf("4", "5", "6", ")", "×"),
        listOf("1", "2", "3", "^", "−"),
        listOf("0", ".", "ANS", "π", "+")
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SciRowsGrid(onKey = onKey, inverse = inverse, staggerBase = 0)
        mergedRows.forEachIndexed { j, row ->
            AnimatedVisibility(visible = true, enter = keypadRowEnter((j + 3) * 32)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { k ->
                        when {
                            k == "." || (k.length == 1 && k[0] in '0'..'9') ->
                                DigitKey(label = k, onKey = onKey, modifier = Modifier.weight(1f))
                            k in setOf("÷", "×", "−", "+") ->
                                FluentCalcKey(
                                    label = k,
                                    onClick = { onKey(k) },
                                    modifier = Modifier.weight(1f),
                                    kind = FluentKeyKind.Operator,
                                    keyHeight = 64.dp
                                )
                            else ->
                                FluentCalcKey(
                                    label = k,
                                    onClick = { onKey(k) },
                                    modifier = Modifier.weight(1f),
                                    kind = FluentKeyKind.Sci,
                                    keyHeight = 64.dp
                                )
                        }
                    }
                }
            }
        }
        AnimatedVisibility(visible = true, enter = keypadRowEnter(7 * 32)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FluentCalcKey(
                    label = "C",
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    kind = FluentKeyKind.Sci,
                    keyHeight = 64.dp
                )
                BackKey(onBack = onBack, onBackLong = onBackLong, modifier = Modifier.weight(1f), tall = true)
                FluentCalcKey(
                    label = "x²",
                    onClick = { onKey("^2") },
                    modifier = Modifier.weight(1f),
                    kind = FluentKeyKind.Sci,
                    keyHeight = 64.dp
                )
                FluentCalcKey(
                    label = "√",
                    onClick = { onKey("√") },
                    modifier = Modifier.weight(1f),
                    kind = FluentKeyKind.Sci,
                    keyHeight = 64.dp
                )
                FluentCalcKey(
                    label = "=",
                    onClick = { onKey("=") },
                    modifier = Modifier.weight(1f),
                    kind = FluentKeyKind.Equals,
                    keyHeight = 64.dp
                )
            }
        }
    }
}

private data class GraphView(val centerX: Double = 0.0, val centerY: Double = 0.0, val scale: Float = 40f)

private fun niceGraphStep(pxPerUnit: Float): Double {
    val raw = 80.0 / pxPerUnit.coerceAtLeast(1f).toDouble()
    val mag = 10.0.pow(floor(log10(raw.coerceAtLeast(1e-9))))
    val n = raw / mag
    val nice = when {
        n < 1.5 -> 1.0
        n < 3.5 -> 2.0
        n < 7.5 -> 5.0
        else -> 10.0
    }
    return nice * mag
}

private fun evalGraphAt(expr: String, x: Double): Double {
    if (expr.isBlank()) return Double.NaN
    return try {
        Engine.eval(XSubst.replace(expr, "($x)"), true).getOrNull()?.toDouble() ?: Double.NaN
    } catch (e: Exception) {
        Double.NaN
    }
}

@Composable
fun GraphScreen() {
    var fExpr by remember { mutableStateOf("sin(x)") }
    var gExpr by remember { mutableStateOf("x^2/10-2") }
    var fOn by remember { mutableStateOf(true) }
    var gOn by remember { mutableStateOf(true) }
    val defaultScale = 40f
    var view by remember { mutableStateOf(GraphView(scale = defaultScale)) }
    var canvasPx by remember { mutableStateOf(IntSize.Zero) }
    var readout by remember { mutableStateOf<String?>(null) }
    var shareError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(readout) {
        if (readout != null) {
            try {
                delay(3000)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                readout = null
                return@LaunchedEffect
            }
            readout = null
        }
    }
    LaunchedEffect(shareError) {
        if (shareError != null) {
            try {
                delay(3000)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                shareError = null
                return@LaunchedEffect
            }
            shareError = null
        }
    }
    val context = LocalContext.current
    val grid = MaterialTheme.colorScheme.outlineVariant
    val axes = MaterialTheme.colorScheme.outline
    val fColor = MaterialTheme.colorScheme.primary
    val gColor = MaterialTheme.colorScheme.tertiary
    val span = 400.0 / view.scale.coerceAtLeast(1f).toDouble()
    fun hasValid(expr: String, enabled: Boolean): Boolean {
        if (!enabled) return false
        var i = 0
        while (i <= 120) {
            val x = view.centerX - span + 2 * span * i / 120.0
            if (evalGraphAt(expr, x).isFinite()) return true
            i++
        }
        return false
    }
    val fValid = remember(fExpr, fOn, view) { hasValid(fExpr, fOn) }
    val gValid = remember(gExpr, gOn, view) { hasValid(gExpr, gOn) }
    val noneValid = (fOn || gOn) && !fValid && !gValid
    LazyColumn(
        Modifier.fillMaxSize().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard("Functions") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.size(12.dp).background(fColor, CircleShape))
                    OutlinedTextField(
                        value = fExpr,
                        onValueChange = { fExpr = it },
                        label = { Text("f(x)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(checked = fOn, onCheckedChange = { fOn = it })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.size(12.dp).background(gColor, CircleShape))
                    OutlinedTextField(
                        value = gExpr,
                        onValueChange = { gExpr = it },
                        label = { Text("g(x)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(checked = gOn, onCheckedChange = { gOn = it })
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box {
                        Canvas(
                            Modifier.fillMaxWidth().height(300.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { off ->
                                            runCatching {
                                                val w = canvasPx.width
                                                val h = canvasPx.height
                                                if (w > 0 && h > 0) {
                                                    val scale = view.scale.coerceIn(5f, 500f)
                                                    val mx = view.centerX + (off.x - w / 2.0) / scale
                                                    val my = view.centerY - (off.y - h / 2.0) / scale
                                                    fun fmt(v: Double) = if (!v.isFinite()) "—" else runCatching { "%.2f".format(v) }.getOrDefault("—")
                                                    val parts = buildList {
                                                        add("x=" + fmt(mx))
                                                        if (fOn) add("f=" + fmt(evalGraphAt(fExpr, mx)))
                                                        if (gOn) add("g=" + fmt(evalGraphAt(gExpr, mx)))
                                                    }
                                                    readout = parts.joinToString(", ") + "  y=" + fmt(my)
                                                }
                                            }
                                        },
                                        onDoubleTap = { view = GraphView(scale = defaultScale) }
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        runCatching {
                                            val cur = view.scale.coerceIn(5f, 500f)
                                            val s = (view.scale * zoom.coerceIn(0.1f, 10f)).coerceIn(5f, 500f)
                                            view = view.copy(
                                                centerX = view.centerX - pan.x / cur,
                                                centerY = view.centerY + pan.y / cur,
                                                scale = s
                                            )
                                        }
                                    }
                                }
                                .onSizeChanged { canvasPx = it }
                        ) {
                            val w = size.width
                            val h = size.height
                            if (w > 0f && h > 0f) {
                                val ppu = view.scale.coerceAtLeast(1f)
                                val step = niceGraphStep(ppu)
                                val xMin = view.centerX - (w / 2) / ppu.toDouble()
                                val xMax = view.centerX + (w / 2) / ppu.toDouble()
                                val yMin = view.centerY - (h / 2) / ppu.toDouble()
                                val yMax = view.centerY + (h / 2) / ppu.toDouble()
                                var gx = floor(xMin / step) * step
                                while (gx <= xMax) {
                                    val px = (w / 2 + (gx - view.centerX) * ppu).toFloat()
                                    drawLine(grid, Offset(px, 0f), Offset(px, h))
                                    gx += step
                                    if (gx > xMin + 1000 * step) break
                                }
                                var gy = floor(yMin / step) * step
                                while (gy <= yMax) {
                                    val py = (h / 2 - (gy - view.centerY) * ppu).toFloat()
                                    drawLine(grid, Offset(0f, py), Offset(w, py))
                                    gy += step
                                    if (gy > yMin + 1000 * step) break
                                }
                                if (xMin <= 0.0 && 0.0 <= xMax) {
                                    val px = (w / 2 + (0.0 - view.centerX) * ppu).toFloat()
                                    drawLine(axes, Offset(px, 0f), Offset(px, h), strokeWidth = 3f)
                                }
                                if (yMin <= 0.0 && 0.0 <= yMax) {
                                    val py = (h / 2 - (0.0 - view.centerY) * ppu).toFloat()
                                    drawLine(axes, Offset(0f, py), Offset(w, py), strokeWidth = 3f)
                                }
                                fun plot(expr: String, enabled: Boolean, color: Color) {
                                    if (!enabled) return
                                    var prev: Offset? = null
                                    var px = 0f
                                    while (px <= w) {
                                        val mx = view.centerX + (px - w / 2) / ppu
                                        val y = evalGraphAt(expr, mx)
                                        if (y.isFinite()) {
                                            val py = (h / 2 - (y - view.centerY) * ppu).toFloat()
                                            val p = Offset(px, py)
                                            if (prev != null) drawLine(color, prev, p, strokeWidth = 5f)
                                            prev = p
                                        } else {
                                            prev = null
                                        }
                                        px += 2f
                                    }
                                }
                                plot(fExpr, fOn, fColor)
                                plot(gExpr, gOn, gColor)
                            }
                        }
                        val chip = readout
                        if (chip != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
                            ) {
                                Text(
                                    chip,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Row(
                        Modifier.background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            CircleShape
                        ).padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { view = view.copy(scale = (view.scale * 1.25f).coerceIn(5f, 500f)) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom in")
                        }
                        IconButton(
                            onClick = { view = view.copy(scale = (view.scale / 1.25f).coerceIn(5f, 500f)) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom out")
                        }
                        IconButton(
                            onClick = { view = GraphView(scale = defaultScale) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.MyLocation, contentDescription = "Reset view")
                        }
                        IconButton(
                            onClick = {
                                runCatching {
                                    val text = buildString {
                                        if (fOn) append("f(x)=" + fExpr)
                                        if (fOn && gOn) append("; ")
                                        if (gOn) append("g(x)=" + gExpr)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_SEND).setType("text/plain")
                                                .putExtra(Intent.EXTRA_TEXT, text.ifBlank { "f(x)=" + fExpr }),
                                            null
                                        )
                                    )
                                }.onFailure { shareError = "Could not share expressions" }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share expressions")
                        }
                    }
                    }
                    if (shareError != null) {
                        FluentInfoBar(
                            severity = WARNING,
                            title = "Share failed",
                            message = shareError ?: "Could not share expressions",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    if (noneValid) {
                        FluentInfoBar(
                            severity = WARNING,
                            title = "Cannot plot",
                            message = "No valid points for the enabled functions in this view.",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
