package calc.u.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import calc.u.ui.theme.FluentElevation
import calc.u.ui.theme.FluentMotion

private val CascadeEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
private const val CascadeDuration = 350

fun staggerDelayHeader(): Int = 0

fun staggerDelayControl(): Int = 60

fun staggerDelayCard(order: Int): Int = minOf(110 + 45 * order, 260)

fun staggerDelayItem(index: Int): Int = minOf(80 + 45 * index, 360)

private fun cascadeEnter(delayMillis: Int) =
    fadeIn(tween(CascadeDuration, delayMillis = delayMillis, easing = CascadeEasing)) +
        slideInVertically(tween(CascadeDuration, delayMillis = delayMillis, easing = CascadeEasing)) { density ->
            with(density) { 18.dp.toPx().roundToInt() }
        }

@Composable
fun FluentStaggerHeader(content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = cascadeEnter(staggerDelayHeader())
    ) { content() }
}

@Composable
fun FluentStaggerControl(content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = cascadeEnter(staggerDelayControl())
    ) { content() }
}

@Composable
fun FluentStaggerCard(order: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = cascadeEnter(staggerDelayCard(order))
    ) { content() }
}

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.4.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp).semantics { heading() }
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        ) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ResultCore(
    label: String,
    value: String,
    icon: ImageVector?,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp)
                .background(tint.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            } else {
                Text(
                    label.trim().firstOrNull()?.uppercase() ?: "–",
                    style = MaterialTheme.typography.labelLarge,
                    color = tint,
                    maxLines = 1
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ResultLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    ResultCore(label = label, value = value, icon = icon, tint = tint, modifier = modifier)
}

@Composable
fun ToolResultRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    ResultCore(label = label, value = value, icon = icon, tint = tint, modifier = modifier)
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
    val delay = staggerDelayItem(index)
    AnimatedVisibility(
        visible = true,
        enter = cascadeEnter(delay)
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
    val circle = CircleShape
    // Simple-style AC: pale tint, always circular (dynamic-safe, never hardcoded).
    if (kind == FluentKeyKind.Sci && (label == "AC" || label == "C")) {
        Button(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = circle,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = FluentElevation.Key,
                pressedElevation = FluentElevation.KeyPressed
            )
        ) { Text(label, style = MaterialTheme.typography.titleLarge) }
        return
    }
    when (kind) {
        // Simple-style equals: dark primary circle, largest key on the pad.
        FluentKeyKind.Equals -> Button(
            onClick = onClick,
            modifier = modifier.graphicsLayer(scaleX = equalsScale, scaleY = equalsScale).height(keyHeight * 1.3f),
            interactionSource = interactions,
            shape = circle,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = FluentElevation.AccentKey,
                pressedElevation = FluentElevation.KeyPressed
            )
        ) { Text(label, style = MaterialTheme.typography.headlineSmall) }
        FluentKeyKind.Operator -> FilledTonalButton(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = circle,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) { Text(label, style = MaterialTheme.typography.titleLarge) }
        FluentKeyKind.Digit -> FilledTonalButton(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = circle
        ) { Text(label, style = MaterialTheme.typography.titleLarge) }
        FluentKeyKind.Sci -> OutlinedButton(
            onClick = onClick,
            modifier = pressModifier.height(keyHeight),
            interactionSource = interactions,
            shape = circle
        ) { Text(label, style = MaterialTheme.typography.titleSmall) }
    }
}

@Composable
fun Modifier.pressScale(pressed: Boolean, pressedScale: Float = 0.96f): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(FluentMotion.Short, easing = FluentMotion.Standard),
        label = "press-scale"
    )
    return this.graphicsLayer(scaleX = scale, scaleY = scale)
}

@Composable
fun Modifier.pressBounce(pressed: Boolean, pressedScale: Float = 0.96f): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "press-bounce"
    )
    return this.graphicsLayer(scaleX = scale, scaleY = scale)
}

@Composable
fun AnimatedSection(
    title: String,
    modifier: Modifier = Modifier,
    index: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = cascadeEnter(staggerDelayCard(index))
    ) {
        SectionCard(title = title, modifier = modifier, content = content)
    }
}

@Composable
fun JumpToCalcFab(onJump: () -> Unit, modifier: Modifier = Modifier) {
    SmallFloatingActionButton(
        onClick = onJump,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Icon(Icons.Filled.Calculate, contentDescription = "Back to calculator")
    }
}

@Composable
fun BottomBackChevron(onBack: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onBack, modifier = modifier) {
        Icon(Icons.Filled.ChevronLeft, contentDescription = null)
        Text("Back")
    }
}
