package calc.u.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FluentAccent {
    val LightDefault = Color(0xFF005FB8)
    val LightSecondary = Color(0xE6005FB8)
    val LightTertiary = Color(0xCC005FB8)
    val LightDisabled = Color(0x37000000)
    val LightTextPrimary = Color(0xFF005FB8)
    val DarkDefault = Color(0xFF0078D4)
    val DarkSecondary = Color(0xE60078D4)
    val DarkTertiary = Color(0xCC0078D4)
    val DarkDisabled = Color(0x28FFFFFF)
    val DarkTextPrimary = Color(0xFF99EBFF)
}

object FluentText {
    val LightPrimary = Color(0xE4000000)
    val LightSecondary = Color(0x9E000000)
    val LightTertiary = Color(0x61000000)
    val LightDisabled = Color(0x5C000000)
    val DarkPrimary = Color(0xFFFFFFFF)
    val DarkSecondary = Color(0xC6FFFFFF)
    val DarkTertiary = Color(0x87FFFFFF)
    val DarkDisabled = Color(0x5DFFFFFF)
}

object FluentSeverity {
    val InfoLight = Color(0xFF005FB8)
    val InfoDark = Color(0xFF60CDFF)
    val SuccessLight = Color(0xFF0F7B0F)
    val SuccessDark = Color(0xFF6CCB5F)
    val WarningLight = Color(0xFF9A6B00)
    val WarningDark = Color(0xFFFFC53D)
    val ErrorLight = Color(0xFFBA1A1A)
    val ErrorDark = Color(0xFFFFB4AB)
}

object FluentSpace {
    val X4 = 4.dp
    val X8 = 8.dp
    val X12 = 12.dp
    val X16 = 16.dp
    val X20 = 20.dp
    val X24 = 24.dp
    val X32 = 32.dp
    val Page = 16.dp
}

object FluentStroke {
    val CardLight = Color(0x0F000000)
    val CardDark = Color(0x0FFFFFFF)
    val FocusLightOuter = Color(0xFF000000)
    val FocusDarkOuter = Color(0xFFFFFFFF)
}

object FluentMotion {
    val Short: Int = 150
    val Medium: Int = 300
    val Long: Int = 500
    val Standard: Easing = CubicBezierEasing(0.1f, 0.9f, 0.2f, 1.0f)
}

private fun fluentType(): Typography {
    val f = FontFamily.Default
    fun s(size: Int, height: Int, w: FontWeight) = TextStyle(
        fontFamily = f, fontWeight = w, fontSize = size.sp, lineHeight = height.sp
    )
    val semi = FontWeight.SemiBold
    val reg = FontWeight.Normal
    return Typography(
        displayLarge = s(68, 92, semi),
        displayMedium = s(40, 52, semi),
        displaySmall = s(28, 36, semi),
        headlineLarge = s(28, 36, semi),
        headlineMedium = s(20, 28, semi),
        headlineSmall = s(18, 24, reg),
        titleLarge = s(20, 28, semi),
        titleMedium = s(16, 22, semi),
        titleSmall = s(14, 20, semi),
        bodyLarge = s(18, 24, reg),
        bodyMedium = s(14, 20, reg),
        bodySmall = s(12, 16, reg),
        labelLarge = s(14, 20, semi),
        labelMedium = s(12, 16, semi),
        labelSmall = s(12, 16, reg)
    )
}

private val FluentLight = lightColorScheme(
    primary = FluentAccent.LightDefault,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F1FB),
    onPrimaryContainer = Color(0xFF001D33),
    secondary = Color(0xFF535F70),
    onSecondaryContainer = FluentText.LightPrimary,
    tertiary = Color(0xFF6B4DAB),
    error = FluentSeverity.ErrorLight,
    background = Color(0xFFF3F3F3),
    onBackground = FluentText.LightPrimary,
    surface = Color(0xFFF9F9F9),
    onSurface = FluentText.LightPrimary,
    surfaceVariant = Color(0xFFEDEDED),
    onSurfaceVariant = FluentText.LightSecondary,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF9F9F9),
    surfaceContainer = Color(0xFFF3F3F3),
    surfaceContainerHigh = Color(0xFFEDEDED),
    surfaceContainerHighest = Color(0xFFE6E6E6),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C7CF),
    scrim = Color(0x52000000)
)

private val FluentDark = darkColorScheme(
    primary = Color(0xFF60CDFF),
    onPrimary = Color(0xFF00344D),
    primaryContainer = Color(0xFF004578),
    onPrimaryContainer = Color(0xFFD6EAFD),
    secondary = Color(0xFFBBC7DC),
    onSecondaryContainer = FluentText.DarkPrimary,
    tertiary = Color(0xFFD0BCFF),
    error = FluentSeverity.ErrorDark,
    background = Color(0xFF202020),
    onBackground = FluentText.DarkPrimary,
    surface = Color(0xFF2B2B2B),
    onSurface = FluentText.DarkPrimary,
    surfaceVariant = Color(0xFF323232),
    onSurfaceVariant = FluentText.DarkSecondary,
    surfaceContainerLowest = Color(0xFF101010),
    surfaceContainerLow = Color(0xFF2B2B2B),
    surfaceContainer = Color(0xFF323232),
    surfaceContainerHigh = Color(0xFF3A3A3A),
    surfaceContainerHighest = Color(0xFF434343),
    outline = Color(0xFF8E9199),
    outlineVariant = Color(0xFF43474E),
    scrim = Color(0x52000000)
)

private val FluentAmoled = darkColorScheme(
    primary = Color(0xFF60CDFF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004578),
    onPrimaryContainer = Color(0xFFD6EAFD),
    secondary = Color(0xFFBBC7DC),
    tertiary = Color(0xFFD0BCFF),
    error = FluentSeverity.ErrorDark,
    background = Color.Black,
    onBackground = Color(0xFFE6E6E6),
    surface = Color.Black,
    onSurface = Color(0xFFE6E6E6),
    onSurfaceVariant = Color(0xFFC6C6C6),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF111111),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF242424),
    outline = Color(0xFF8E9199),
    outlineVariant = Color(0xFF2A2A2A)
)

private val FluentContrast = darkColorScheme(
    primary = Color(0xFF99EBFF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0078D4),
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    tertiary = Color(0xFFFFC53D),
    error = Color(0xFFFF7A70),
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    onSurfaceVariant = Color.White,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF2B2B2B),
    surfaceContainerHighest = Color(0xFF3A3A3A),
    outline = Color.White,
    outlineVariant = Color.White
)

val FluentShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
@ReadOnlyComposable
fun infoSeverityColor(kind: Int): Color {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    return when (kind) {
        1 -> if (dark) FluentSeverity.SuccessDark else FluentSeverity.SuccessLight
        2 -> if (dark) FluentSeverity.WarningDark else FluentSeverity.WarningLight
        3 -> if (dark) FluentSeverity.ErrorDark else FluentSeverity.ErrorLight
        else -> if (dark) FluentSeverity.InfoDark else FluentSeverity.InfoLight
    }
}

@Composable
fun CalcUTheme(
    theme: String = "system",
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = when (theme) {
        "light" -> FluentLight
        "dark" -> FluentDark
        "amoled" -> FluentAmoled
        "contrast" -> FluentContrast
        else -> if (dark) FluentDark else FluentLight
    }
    MaterialTheme(colorScheme = scheme, shapes = FluentShapes, typography = fluentType(), content = content)
}
