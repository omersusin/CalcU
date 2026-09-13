package calc.u.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import calc.u.ui.theme.FluentSpace

enum class SpinMode { Inline, Hidden }

const val INFO = 0
const val SUCCESS = 1
const val WARNING = 2
const val ERROR = 3

private val NumericInput = Regex("-?[0-9]*\\.?[0-9]*([eE][+-]?[0-9]*)?")
private val FieldGroupShape = RoundedCornerShape(16.dp)

@Composable
fun CalcUNumberBox(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    min: Double? = null,
    max: Double? = null,
    smallChange: Double = 1.0,
    spinMode: SpinMode = SpinMode.Hidden,
    isError: Boolean = false,
    supportingText: String? = null,
    integer: Boolean = false
) {
    var lastValid by remember { mutableStateOf(value) }
    fun spin(delta: Double) {
        val base = value.toDoubleOrNull() ?: min ?: 0.0
        var nv = base + delta
        if (min != null) nv = maxOf(min, nv)
        if (max != null) nv = minOf(max, nv)
        val text = if (integer || nv % 1.0 == 0.0) nv.toLong().toString() else nv.toString()
        lastValid = text
        onValueChange(text)
    }
    Column(modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = { next ->
                if (next.isEmpty() || next.matches(NumericInput)) {
                    lastValid = next
                    onValueChange(next)
                } else {
                    onValueChange(lastValid)
                }
            },
            placeholder = { if (placeholder != null) Text(placeholder) },
            singleLine = true,
            isError = isError,
            shape = FieldGroupShape,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (integer) KeyboardType.Number else KeyboardType.Decimal
            ),
            keyboardActions = KeyboardActions(onDone = {
                if (value.toDoubleOrNull() == null && value.isNotEmpty()) onValueChange(lastValid)
            }),
            trailingIcon = {
                if (spinMode == SpinMode.Inline) {
                    Row {
                        Box(
                            Modifier.size(40.dp).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) { spin(smallChange) }.semantics { contentDescription = "Increase" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null)
                        }
                        Box(
                            Modifier.size(40.dp).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) { spin(-smallChange) }.semantics { contentDescription = "Decrease" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (supportingText != null) {
            Text(
                supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = FluentSpace.X4)
            )
        }
    }
}

@Composable
fun FluentInfoBar(
    severity: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val container = when (severity) {
        SUCCESS -> scheme.tertiaryContainer
        WARNING -> scheme.secondaryContainer
        ERROR -> scheme.errorContainer
        else -> scheme.primaryContainer
    }
    val onContainer = when (severity) {
        SUCCESS -> scheme.onTertiaryContainer
        WARNING -> scheme.onSecondaryContainer
        ERROR -> scheme.onErrorContainer
        else -> scheme.onPrimaryContainer
    }
    val icon = when (severity) {
        SUCCESS -> Icons.Filled.CheckCircle
        WARNING -> Icons.Filled.Warning
        ERROR -> Icons.Filled.Error
        else -> Icons.Filled.Info
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
        border = null
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(FluentSpace.X12),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = onContainer)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FluentSpace.X4)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = onContainer)
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer
                )
            }
            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Dismiss")
                }
            }
        }
    }
}

enum class ExpandDirection { Down, Up }

@Composable
fun FluentExpander(
    header: String,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    expandDirection: ExpandDirection = ExpandDirection.Down,
    content: @Composable () -> Unit
) {
    var internal by rememberSaveable { mutableStateOf(false) }
    val isExpanded = expanded ?: internal
    fun toggle() {
        if (expanded == null) internal = !internal
    }
    val headerInteractions = remember { MutableInteractionSource() }
    val headerPressed by headerInteractions.collectIsPressedAsState()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth()
                    .pressBounce(headerPressed)
                    .clickable(
                        interactionSource = headerInteractions,
                        indication = LocalIndication.current
                    ) { toggle() }
                    .padding(FluentSpace.X16),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    header,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(if (isExpanded) 90f else 0f)
                )
            }
            if (expandDirection == ExpandDirection.Up && isExpanded) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    Box(Modifier.padding(horizontal = FluentSpace.X16)) { content() }
                }
            }
            AnimatedVisibility(
                visible = expandDirection == ExpandDirection.Down && isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    Modifier.padding(
                        start = FluentSpace.X16,
                        end = FluentSpace.X16,
                        bottom = FluentSpace.X16
                    )
                ) { content() }
            }
        }
    }
}

@Composable
fun FluentTeachingTip(
    title: String,
    subtitle: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    lightDismiss: Boolean = false
) {
    val card = @Composable {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                Modifier.padding(FluentSpace.X16),
                horizontalArrangement = Arrangement.spacedBy(FluentSpace.X12),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(FluentSpace.X4)
                ) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (actionLabel != null && onAction != null) {
                        Button(onClick = onAction) { Text(actionLabel) }
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close tip")
                }
            }
        }
    }
    if (lightDismiss) {
        Box(
            Modifier.fillMaxSize().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { onClose() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier.padding(FluentSpace.X16).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    enabled = true,
                    onClick = {}
                )
            ) { card() }
        }
    } else {
        Box(modifier) { card() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluentSegmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        options.forEachIndexed { i, label ->
            SegmentedButton(
                selected = selected == i,
                onClick = { onSelect(i) },
                shape = SegmentedButtonDefaults.itemShape(i, options.size),
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun FluentSearchPill(
    query: String,
    onQuery: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            hint,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            placeholder = { Text(hint) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQuery("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = FieldGroupShape,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions.Default
        )
    }
}

@Composable
fun CountPill(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(50.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}
