package calc.u.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val FluentLight = lightColorScheme(
    primary = Color(0xFF0067C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6EAFD),
    onPrimaryContainer = Color(0xFF001D33),
    secondary = Color(0xFF535F70),
    tertiary = Color(0xFF6B4DAB),
    error = Color(0xFFBA1A1A),
    surface = Color(0xFFF9F9F9),
    onSurface = Color(0xFF1A1C1E),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF3F3F3),
    surfaceContainer = Color(0xFFEDEDED),
    surfaceContainerHigh = Color(0xFFE6E6E6),
    surfaceContainerHighest = Color(0xFFDFDFDF),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C7CF)
)

private val FluentDark = darkColorScheme(
    primary = Color(0xFF60CDFF),
    onPrimary = Color(0xFF00344D),
    primaryContainer = Color(0xFF004578),
    onPrimaryContainer = Color(0xFFD6EAFD),
    secondary = Color(0xFFBBC7DC),
    tertiary = Color(0xFFD0BCFF),
    error = Color(0xFFFFB4AB),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceContainerLowest = Color(0xFF0F1113),
    surfaceContainerLow = Color(0xFF202020),
    surfaceContainer = Color(0xFF2B2B2B),
    surfaceContainerHigh = Color(0xFF323232),
    surfaceContainerHighest = Color(0xFF3A3A3A),
    outline = Color(0xFF8E9199),
    outlineVariant = Color(0xFF43474E)
)

private val FluentAmoled = darkColorScheme(
    primary = Color(0xFF60CDFF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004578),
    onPrimaryContainer = Color(0xFFD6EAFD),
    secondary = Color(0xFFBBC7DC),
    tertiary = Color(0xFFD0BCFF),
    error = Color(0xFFFFB4AB),
    surface = Color.Black,
    onSurface = Color(0xFFE6E6E6),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF111111),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF242424),
    outline = Color(0xFF8E9199),
    outlineVariant = Color(0xFF2A2A2A)
)

val FluentShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

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
        else -> if (dark) FluentDark else FluentLight
    }
    MaterialTheme(colorScheme = scheme, shapes = FluentShapes, typography = Typography(), content = content)
}
