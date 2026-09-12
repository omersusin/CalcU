package calc.u.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import calc.u.ui.theme.FluentElevation
import calc.u.ui.theme.FluentMotion

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) +
            slideInVertically(tween(FluentMotion.Medium, easing = FluentMotion.Standard)) { it / 10 },
        modifier = modifier
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp).semantics { heading() }
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ResultLine(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
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
fun FluentStagger(index: Int, content: @Composable () -> Unit) {
    val delay = minOf(index * 40, 240)
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(FluentMotion.Medium, delayMillis = delay, easing = FluentMotion.Standard)) +
            slideInVertically(tween(FluentMotion.Medium, delayMillis = delay, easing = FluentMotion.Standard)) { it / 8 }
    ) { content() }
}

@Composable
fun FluentCalcKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: FluentKeyKind = FluentKeyKind.Digit,
    keyHeight: Dp = 64.dp
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.96f else 1f,
        animationSpec = tween(FluentMotion.Short, easing = FluentMotion.Standard),
        label = "fluent-press"
    )
    val pressSpec = spring<Float>(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy)
    val equalsScale by animateFloatAsState(
        if (pressed) 0.93f else 1f,
        animationSpec = pressSpec,
        label = "fluent-equals-press"
    )
    val equalsCorner by animateDpAsState(
        if (pressed) 16.dp else 28.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fluent-equals-morph"
    )
    val pressModifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    val shape = MaterialTheme.shapes.large
    when (kind) {
        FluentKeyKind.Equals -> Button(
            onClick = onClick,
            modifier = modifier.graphicsLayer(scaleX = equalsScale, scaleY = equalsScale).height(keyHeight),
            interactionSource = interactions,
            shape = RoundedCornerShape(equalsCorner),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = FluentElevation.AccentKey,
                pressedElevation = FluentElevation.KeyPressed
            )
        ) { Text(label, style = MaterialTheme.typography.headlineSmall) }
        FluentKeyKind.Operator -> FilledTonalButton(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = shape
        ) { Text(label, style = MaterialTheme.typography.titleLarge) }
        FluentKeyKind.Digit -> Button(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = FluentElevation.Key,
                pressedElevation = FluentElevation.KeyPressed
            )
        ) { Text(label, style = MaterialTheme.typography.titleLarge) }
        FluentKeyKind.Sci -> OutlinedButton(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = shape
        ) { Text(label, style = MaterialTheme.typography.titleSmall) }
    }
}
