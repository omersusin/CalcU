package calc.u.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import calc.u.ui.theme.FluentSpace
import calc.u.ui.theme.infoSeverityColor

enum class SpinMode { Inline, Hidden }

const val INFO = 0
const val SUCCESS = 1
const val WARNING = 2
const val ERROR = 3

private val NumericInput = Regex("-?[0-9]*\\.?[0-9]*([eE][+-]?[0-9]*)?")

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
            shape = MaterialTheme.shapes.small,
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
                                indication = null
                            ) { spin(smallChange) }.semantics { contentDescription = "Increase" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null)
                        }
                        Box(
                            Modifier.size(40.dp).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
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
    val color = infoSeverityColor(severity)
    val icon = when (severity) {
        SUCCESS -> Icons.Filled.CheckCircle
        WARNING -> Icons.Filled.Warning
        ERROR -> Icons.Filled.Error
        else -> Icons.Filled.Info
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = null
    ) {
        Row(
            Modifier.padding(FluentSpace.X12),
            horizontalArrangement = Arrangement.spacedBy(FluentSpace.X12),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FluentSpace.X4)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
