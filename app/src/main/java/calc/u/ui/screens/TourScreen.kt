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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class TourPage(val title: String, val body: String, val icon: ImageVector)

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
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TourScreen(onDone: () -> Unit, modifier: Modifier = Modifier) {
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
