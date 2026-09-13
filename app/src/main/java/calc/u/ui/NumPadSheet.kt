package calc.u.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

class NumPadState {
    var text by mutableStateOf("")
    var show by mutableStateOf(false)
    var onCommit: (String) -> Unit by mutableStateOf({})

    fun open(current: String, onCommit: (String) -> Unit) {
        text = current
        this.onCommit = onCommit
        show = true
    }
}

@Composable
fun rememberNumPadState(): NumPadState = remember { NumPadState() }

private fun smartParen(text: String): String {
    val open = text.count { it == '(' }
    val close = text.count { it == ')' }
    val last = text.lastOrNull()
    return if (open > close && (last?.isDigit() == true || last == ')' || last == '%')) {
        "$text)"
    } else {
        "$text("
    }
}

private fun isPadOperator(k: String): Boolean =
    k == "÷" || k == "×" || k == "−" || k == "+" || k == "%" || k == "()"

@Composable
private fun RowScope.PadKey(
    label: String,
    tonal: Boolean,
    onClick: () -> Unit
) {
    val modifier = Modifier.weight(1f).height(56.dp)
    val circle = CircleShape
    if (label == "AC") {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = circle,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Text(label, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        }
        return
    }
    if (isPadOperator(label)) {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            shape = circle,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(label, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        }
        return
    }
    if (tonal) {
        FilledTonalButton(onClick = onClick, modifier = modifier, shape = circle) {
            Text(label, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier, shape = circle) {
            Text(label, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumPadSheet(state: NumPadState, title: String) {
    if (!state.show) return
    ModalBottomSheet(
        onDismissRequest = { state.show = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).semantics { heading() },
                    maxLines = 1
                )
                IconButton(onClick = { state.text = "" }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear input")
                }
                FilledIconButton(
                    onClick = {
                        state.onCommit(state.text)
                        state.show = false
                    }
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Done")
                }
            }
            OutlinedTextField(
                value = state.text,
                onValueChange = { state.text = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
            val rows = listOf(
                listOf("AC", "()", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "−"),
                listOf("1", "2", "3", "+"),
                listOf("0", "00", ".", "⌫")
            )
            fun onKey(k: String) {
                when (k) {
                    "AC" -> state.text = ""
                    "⌫" -> state.text = state.text.dropLast(1)
                    "()" -> state.text = smartParen(state.text)
                    else -> state.text += k
                }
            }
            fun isTonal(k: String): Boolean =
                k == "." || k == "00" || (k.length == 1 && k[0].isDigit())
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { k ->
                        PadKey(label = k, tonal = isTonal(k), onClick = { onKey(k) })
                    }
                }
            }
        }
    }
}
