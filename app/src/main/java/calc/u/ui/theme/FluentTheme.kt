package calc.u.ui.theme

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
    secondaryContainer = Color(0xFFE8E8E8),
    surface = Color(0xFFF3F3F3),
    surfaceContainer = Color(0xFFEDEDED),
    surfaceContainerHigh = Color(0xFFE6E6E6),
    background = Color(0xFFF9F9F9)
)

private val FluentDark = darkColorScheme(
    primary = Color(0xFF60CDFF),
    onPrimary = Color(0xFF00344D),
    primaryContainer = Color(0xFF004578),
    secondaryContainer = Color(0xFF2D2D2D),
    surface = Color(0xFF202020),
    surfaceContainer = Color(0xFF2B2B2B),
    surfaceContainerHigh = Color(0xFF323232),
    background = Color(0xFF181818)
)

private val FluentAmoled = darkColorScheme(
    primary = Color(0xFF60CDFF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004578),
    secondaryContainer = Color(0xFF1A1A1A),
    surface = Color.Black,
    surfaceContainer = Color(0xFF0A0A0A),
    surfaceContainerHigh = Color(0xFF141414),
    background = Color.Black
)

val FluentShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun CalcUTheme(theme: String = "system", dark: Boolean = false, amoled: Boolean = false, content: @Composable () -> Unit) {
    val scheme = when {
        amoled && dark -> FluentAmoled
        theme == "light" -> FluentLight
        theme == "dark" -> FluentDark
        theme == "amoled" -> FluentAmoled
        dark -> FluentDark
        else -> FluentLight
    }
    MaterialTheme(colorScheme = scheme, shapes = FluentShapes, typography = Typography(), content = content)
}
