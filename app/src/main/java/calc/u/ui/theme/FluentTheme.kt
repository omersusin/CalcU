package calc.u.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.color.utilities.TonalPalette

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
    val Short: Int = 100
    val Medium: Int = 300
    val Long: Int = 500
    // Verified: M3 emphasized standard (0.05, 0.7, 0.1, 1.0) — keep.
    val Standard: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val Emphasized: Easing = Standard
}

object FluentElevation {
    val Display = 1.dp
    val Key = 1.dp
    val KeyPressed = 0.dp
    val AccentKey = 2.dp
}

object FluentExpressive {
    val HeroCardShape = RoundedCornerShape(28.dp)
    val GroupCardShape = RoundedCornerShape(16.dp)
    // Group headers follow labelLarge spec (token-only, no behavior).
    val GroupHeaderFontSize = 12.sp
    val GroupHeaderLineHeight = 16.sp
    val GroupHeaderWeight = FontWeight.Bold
    val GroupHeaderLetterSpacing = 0.4.sp
}

private fun fluentType(): Typography {
    val f = FontFamily.Default
    fun s(size: Int, height: Int, w: FontWeight) = TextStyle(
        fontFamily = f, fontWeight = w, fontSize = size.sp, lineHeight = height.sp
    )
    val semi = FontWeight.SemiBold
    val reg = FontWeight.Normal
    val base = Typography(
        displayLarge = s(64, 68, semi),
        displayMedium = s(52, 58, semi),
        displaySmall = s(40, 46, semi),
        headlineLarge = s(36, 44, semi),
        headlineMedium = s(24, 32, semi),
        headlineSmall = s(20, 26, semi),
        titleLarge = s(20, 28, semi),
        titleMedium = s(16, 22, semi),
        titleSmall = s(16, 22, semi),
        bodyLarge = s(18, 24, reg),
        bodyMedium = s(14, 20, reg),
        bodySmall = s(12, 16, reg),
        labelLarge = s(14, 20, semi),
        labelMedium = s(12, 16, semi),
        labelSmall = s(12, 16, reg)
    )
    // M3 Expressive: larger display/headline with tighter tracking.
    return base.copy(
        displayLarge = base.displayLarge.copy(letterSpacing = (-0.25).sp),
        displayMedium = base.displayMedium.copy(letterSpacing = (-0.25).sp),
        displaySmall = base.displaySmall.copy(letterSpacing = (-0.25).sp),
        headlineLarge = base.headlineLarge.copy(letterSpacing = (-0.25).sp),
        headlineMedium = base.headlineMedium.copy(letterSpacing = (-0.25).sp)
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
    background = Color(0xFFFBF8F9),
    onBackground = FluentText.LightPrimary,
    surface = Color(0xFFFBF8F9),
    onSurface = FluentText.LightPrimary,
    surfaceVariant = Color(0xFFEDEDED),
    onSurfaceVariant = FluentText.LightSecondary,
    surfaceContainerLowest = Color(0xFFFBF8F9),
    surfaceContainerLow = Color(0xFFF6F6F6),
    surfaceContainer = Color(0xFFEFEFEF),
    surfaceContainerHigh = Color(0xFFE8E8E8),
    surfaceContainerHighest = Color(0xFFE0E0E0),
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
    surfaceContainerLowest = Color(0xFF0C0C0C),
    surfaceContainerLow = Color(0xFF272727),
    surfaceContainer = Color(0xFF303030),
    surfaceContainerHigh = Color(0xFF3B3B3B),
    surfaceContainerHighest = Color(0xFF474747),
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
    surfaceContainerLow = Color(0xFF0B0B0B),
    surfaceContainer = Color(0xFF141414),
    surfaceContainerHigh = Color(0xFF1E1E1E),
    surfaceContainerHighest = Color(0xFF2A2A2A),
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
    surfaceContainer = Color(0xFF1C1C1C),
    surfaceContainerHigh = Color(0xFF2E2E2E),
    surfaceContainerHighest = Color(0xFF404040),
    outline = Color.White,
    outlineVariant = Color.White
)

val FluentShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
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

@SuppressLint("RestrictedApi")
@Composable
fun seedScheme(seedArgb: Int, dark: Boolean): ColorScheme {
    val p = TonalPalette.fromInt(seedArgb)
    fun c(tone: Int) = Color(p.tone(tone))
    return if (dark) {
        darkColorScheme(
            primary = c(80),
            onPrimary = c(20),
            primaryContainer = c(30),
            onPrimaryContainer = c(90),
            secondary = c(80),
            onSecondary = c(20),
            secondaryContainer = c(30),
            onSecondaryContainer = c(90),
            tertiary = c(80),
            onTertiary = c(20),
            tertiaryContainer = c(30),
            onTertiaryContainer = c(90),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            background = c(6),
            onBackground = c(90),
            surface = c(6),
            onSurface = c(90),
            surfaceVariant = c(30),
            onSurfaceVariant = c(80),
            surfaceContainerLowest = c(0),
            surfaceContainerLow = c(10),
            surfaceContainer = c(12),
            surfaceContainerHigh = c(17),
            surfaceContainerHighest = c(22),
            outline = c(60),
            outlineVariant = c(30)
        )
    } else {
        lightColorScheme(
            primary = c(40),
            onPrimary = c(100),
            primaryContainer = c(90),
            onPrimaryContainer = c(10),
            secondary = c(40),
            onSecondary = c(100),
            secondaryContainer = c(90),
            onSecondaryContainer = c(10),
            tertiary = c(40),
            onTertiary = c(100),
            tertiaryContainer = c(90),
            onTertiaryContainer = c(10),
            error = Color(0xFFBA1A1A),
            onError = Color.White,
            background = c(95),
            onBackground = c(10),
            surface = c(93),
            onSurface = c(10),
            surfaceVariant = c(90),
            onSurfaceVariant = c(30),
            surfaceContainerLowest = c(100),
            surfaceContainerLow = c(96),
            surfaceContainer = c(94),
            surfaceContainerHigh = c(92),
            surfaceContainerHighest = c(90),
            outline = c(50),
            outlineVariant = c(80)
        )
    }
}

@Composable
fun CalcUTheme(
    theme: String = "system",
    dark: Boolean = isSystemInDarkTheme(),
    dynamic: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val scheme = if (dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        when (theme) {
            "light" -> dynamicLightColorScheme(context)
            "dark" -> dynamicDarkColorScheme(context)
            "amoled" -> FluentAmoled
            "contrast" -> FluentContrast
            "ocean" -> seedScheme(0xFF0061A4.toInt(), dark)
            "forest" -> seedScheme(0xFF1B6B4A.toInt(), dark)
            "sunset" -> seedScheme(0xFFB23C17.toInt(), dark)
            "grape" -> seedScheme(0xFF6B4DAB.toInt(), dark)
            "nord" -> seedScheme(0xFF5E81AC.toInt(), dark)
            "dracula" -> seedScheme(0xFFBD93F9.toInt(), dark)
            "tokyo" -> seedScheme(0xFF7AA2F7.toInt(), dark)
            "gruvbox" -> seedScheme(0xFFD79921.toInt(), dark)
            "catppuccin" -> seedScheme(0xFFCBA6F7.toInt(), dark)
            "kanagawa" -> seedScheme(0xFF7E9CD8.toInt(), dark)
            "rosepine" -> seedScheme(0xFFEBBCBA.toInt(), dark)
            "mono" -> seedScheme(0xFF9AA0A6.toInt(), dark)
            "amber" -> seedScheme(0xFFFF8F00.toInt(), dark)
            "slate" -> seedScheme(0xFF78909C.toInt(), dark)
            else -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
    } else when (theme) {
        "light" -> FluentLight
        "dark" -> FluentDark
        "amoled" -> FluentAmoled
        "contrast" -> FluentContrast
        "ocean" -> seedScheme(0xFF0061A4.toInt(), dark)
        "forest" -> seedScheme(0xFF1B6B4A.toInt(), dark)
        "sunset" -> seedScheme(0xFFB23C17.toInt(), dark)
        "grape" -> seedScheme(0xFF6B4DAB.toInt(), dark)
        "nord" -> seedScheme(0xFF5E81AC.toInt(), dark)
        "dracula" -> seedScheme(0xFFBD93F9.toInt(), dark)
        "tokyo" -> seedScheme(0xFF7AA2F7.toInt(), dark)
        "gruvbox" -> seedScheme(0xFFD79921.toInt(), dark)
        "catppuccin" -> seedScheme(0xFFCBA6F7.toInt(), dark)
        "kanagawa" -> seedScheme(0xFF7E9CD8.toInt(), dark)
        "rosepine" -> seedScheme(0xFFEBBCBA.toInt(), dark)
        "mono" -> seedScheme(0xFF9AA0A6.toInt(), dark)
        "amber" -> seedScheme(0xFFFF8F00.toInt(), dark)
        "slate" -> seedScheme(0xFF78909C.toInt(), dark)
        else -> if (dark) FluentDark else FluentLight
    }
    MaterialTheme(colorScheme = scheme, shapes = FluentShapes, typography = fluentType(), content = content)
}
