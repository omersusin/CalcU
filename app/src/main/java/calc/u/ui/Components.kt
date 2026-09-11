package calc.u.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import calc.u.ui.theme.FluentMotion
import calc.u.ui.theme.FluentSpace

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) +
            slideInVertically(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) { it / 8 },
        modifier = modifier
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(FluentSpace.X16),
                verticalArrangement = Arrangement.spacedBy(FluentSpace.X12)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                content()
            }
        }
    }
}

@Composable
fun ResultLine(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

enum class FluentKeyKind { Digit, Operator, Equals, Sci }

private val ExpressionOperators = setOf('+', '-', '−', '×', '÷', '/', '*', '%', '^', '√', '(', ')', '!')

fun tintExpression(input: String, number: Color, operator: Color): AnnotatedString =
    buildAnnotatedString {
        input.forEach { c ->
            if (c in ExpressionOperators) withStyle(SpanStyle(color = operator)) { append(c) }
            else withStyle(SpanStyle(color = number)) { append(c) }
        }
    }

@Composable
fun FluentCalcKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: FluentKeyKind = FluentKeyKind.Digit,
    keyHeight: Dp = 60.dp
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.95f else 1f,
        animationSpec = tween(FluentMotion.Short, easing = FluentMotion.Standard),
        label = "fluent-press"
    )
    val pressModifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    val shape = MaterialTheme.shapes.small
    when (kind) {
        FluentKeyKind.Equals -> Button(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = shape,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp)
        ) { Text(label, style = MaterialTheme.typography.titleMedium) }
        FluentKeyKind.Operator -> FilledTonalButton(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = shape
        ) { Text(label, style = MaterialTheme.typography.titleMedium) }
        FluentKeyKind.Digit -> Button(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp)
        ) { Text(label, style = MaterialTheme.typography.titleLarge) }
        FluentKeyKind.Sci -> OutlinedButton(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = shape
        ) { Text(label, style = MaterialTheme.typography.titleSmall) }
    }
}
