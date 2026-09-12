package calc.u.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class QuickSettings(
    val vibration: Boolean,
    val fractions: Boolean,
    val memoryRow: Boolean,
    val keepScreenOn: Boolean,
    val onVibration: (Boolean) -> Unit,
    val onFractions: (Boolean) -> Unit,
    val onMemoryRow: (Boolean) -> Unit,
    val onKeepScreenOn: (Boolean) -> Unit
)

@Composable
private fun QuickSwitchRow(
    title: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
fun QuickOverlay(
    expanded: Boolean,
    onDismiss: () -> Unit,
    settings: QuickSettings,
    numberFormat: String,
    modifier: Modifier = Modifier
) {
    if (!expanded) return
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var shown by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { shown = true }
        Box(
            modifier = modifier.fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = shown,
                enter = scaleIn(
                    spring(stiffness = Spring.StiffnessMediumLow),
                    initialScale = 0.92f
                ) + fadeIn(),
                modifier = Modifier.padding(24.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Quick settings",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        QuickSwitchRow(
                            title = "Vibration",
                            checked = settings.vibration,
                            onChecked = settings.onVibration
                        )
                        QuickSwitchRow(
                            title = "Fractions",
                            checked = settings.fractions,
                            onChecked = settings.onFractions
                        )
                        QuickSwitchRow(
                            title = "Memory row",
                            checked = settings.memoryRow,
                            onChecked = settings.onMemoryRow
                        )
                        Text(
                            "Theme in Settings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        var advanced by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { advanced = !advanced }
                                )
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Advanced",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                if (advanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (advanced) "Collapse advanced" else "Expand advanced",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AnimatedVisibility(visible = advanced) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                QuickSwitchRow(
                                    title = "Keep screen on",
                                    checked = settings.keepScreenOn,
                                    onChecked = settings.onKeepScreenOn
                                )
                                Text(
                                    "Number format: $numberFormat",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
