package calc.u.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class TourPage(
    val title: String,
    val body: String,
    val icon: ImageVector,
    val isLayoutPicker: Boolean = false
)

private val TourPages = listOf(
    TourPage(
        title = "Calculator",
        body = "Full keypad with live result. Swipe up the history sheet to reuse past answers.",
        icon = Icons.Filled.Calculate
    ),
    TourPage(
        title = "Convert",
        body = "Type an expression once, then switch units with the picker. No retyping.",
        icon = Icons.Filled.SwapHoriz
    ),
    TourPage(
        title = "Tools",
        body = "One hub for Finance, Math, Time Lab and more. Use search to jump straight in.",
        icon = Icons.Filled.Apps
    ),
    TourPage(
        title = "Make it yours",
        body = "Tip: Settings follows your system theme. Turn on dynamic color to match your wallpaper.",
        icon = Icons.Filled.Palette
    ),
    TourPage(
        title = "Keypad layout",
        body = "Pick Simple, Classic or Modern. The preview updates live — you can change it later too.",
        icon = Icons.Filled.Keyboard,
        isLayoutPicker = true
    )
)

private val KeypadLayoutOptions = listOf("simple", "classic", "modern")

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TourScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    keypadLayout: String = "simple",
    onKeypadLayout: (String) -> Unit = {},
    showMemoryRow: Boolean = true,
    onMemoryRow: (Boolean) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { TourPages.size })
    val scope = rememberCoroutineScope()
    val last = pagerState.currentPage == TourPages.size - 1
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).widthIn(max = 560.dp)
            ) { index ->
                val page = TourPages[index]
                if (page.isLayoutPicker) {
                    KeypadLayoutTourPage(
                        keypadLayout = keypadLayout,
                        onKeypadLayout = onKeypadLayout,
                        showMemoryRow = showMemoryRow,
                        onMemoryRow = onMemoryRow
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            tonalElevation = 2.dp,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                page.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(24.dp).size(48.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            page.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.semantics { heading() }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            page.body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                repeat(TourPages.size) { i ->
                    Box(
                        modifier = Modifier.padding(horizontal = 4.dp).size(8.dp).clip(CircleShape)
                            .background(
                                if (i == pagerState.currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (last) {
                    Spacer(Modifier.weight(1f))
                } else {
                    TextButton(onClick = onDone) { Text("Skip") }
                }
                if (last) {
                    FilledTonalButton(onClick = onDone) { Text("Done") }
                } else {
                    FilledTonalButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
                    ) { Text("Next") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeypadLayoutTourPage(
    keypadLayout: String,
    onKeypadLayout: (String) -> Unit,
    showMemoryRow: Boolean,
    onMemoryRow: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = KeypadLayoutOptions.indexOf(keypadLayout.lowercase()).coerceAtLeast(0)
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Keypad layout",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Pick Simple, Classic or Modern. The preview updates live — you can change it later too.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    KeypadLayoutOptions.forEachIndexed { i, value ->
                        SegmentedButton(
                            selected = selectedIndex == i,
                            onClick = { onKeypadLayout(value) },
                            shape = SegmentedButtonDefaults.itemShape(i, KeypadLayoutOptions.size),
                            label = {
                                Text(value.replaceFirstChar { it.uppercase() })
                            }
                        )
                    }
                }
                KeypadMiniPreview(
                    layout = KeypadLayoutOptions[selectedIndex],
                    showMemoryRow = showMemoryRow,
                    modifier = Modifier.widthIn(max = 240.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Memory",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = showMemoryRow,
                        onCheckedChange = onMemoryRow
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadMiniPreview(
    layout: String,
    showMemoryRow: Boolean,
    modifier: Modifier = Modifier
) {
    val gap = 4.dp
    val digitColor = MaterialTheme.colorScheme.secondaryContainer
    val sciColor = MaterialTheme.colorScheme.tertiaryContainer
    val opColor = MaterialTheme.colorScheme.primaryContainer
    val eqColor = MaterialTheme.colorScheme.primary
    val wideCols = layout == "modern"
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            if (showMemoryRow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    repeat(4) {
                        MiniKey(color = sciColor, height = 8.dp, modifier = Modifier.weight(1f))
                    }
                }
            }
            if (layout == "classic") {
                Box(
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(sciColor),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        "Scientific ▾",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
            repeat(3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    repeat(4) {
                        MiniKey(color = sciColor, height = 10.dp, modifier = Modifier.weight(1f))
                    }
                }
            }
            repeat(4) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    val cols = if (wideCols) 5 else 4
                    repeat(cols) { col ->
                        val lastRow = row == 3
                        val lastCol = col == cols - 1
                        val color = when {
                            lastRow && lastCol -> eqColor
                            lastCol || (wideCols && col == cols - 2) -> opColor
                            else -> digitColor
                        }
                        MiniKey(color = color, height = 16.dp, modifier = Modifier.weight(1f))
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                MiniKey(color = sciColor, height = 12.dp, modifier = Modifier.weight(1f))
                MiniKey(color = digitColor, height = 12.dp, modifier = Modifier.weight(1f))
                MiniKey(color = eqColor, height = 12.dp, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniKey(
    color: androidx.compose.ui.graphics.Color,
    height: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(height).clip(MaterialTheme.shapes.extraSmall).background(color)
    )
}
