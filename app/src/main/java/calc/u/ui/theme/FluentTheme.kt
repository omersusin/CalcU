package calc.u.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calc.u.R
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
    // Verified 2026-09-13 against M3 1.4.0 stable sources (BOM 2025.09.01):
    // MotionScheme + MaterialTheme(motionScheme) + Typography *-Emphasized +
    // ExperimentalMaterial3ExpressiveApi are all `internal`, and Button(shapes=...),
    // ToggleButton, LoadingIndicator/wavy progress exist only in 1.5.0-alpha
    // (forbidden: no alpha/beta/rc). So no M3-expressive install here — Lato +
    // sizes + -0.25sp tracking below are kept as the expressive stand-ins.
    // Full expressive install waits on the M3 1.5 stable wave (needs user sign-off:
    // likely Kotlin >2.0.21 + compileSdk >34).
    val HeroCardShape get() = FluentShapes.extraLarge
    val GroupCardShape get() = FluentShapes.large
    // Group headers follow labelLarge spec (token-only, no behavior).
    val GroupHeaderFontSize = 12.sp
    val GroupHeaderLineHeight = 16.sp
    val GroupHeaderWeight = FontWeight.Bold
    val GroupHeaderLetterSpacing = 0.4.sp
}

private val Lato = FontFamily(Font(R.font.lato_regular))

private fun fluentType(): Typography {
    val f = FontFamily.Default
    fun s(size: Int, height: Int, w: FontWeight) = TextStyle(
        fontFamily = f, fontWeight = w, fontSize = size.sp, lineHeight = height.sp
    )
    fun l(size: Int, height: Int, w: FontWeight) = TextStyle(
        fontFamily = Lato, fontWeight = w, fontSize = size.sp, lineHeight = height.sp
    )
    val semi = FontWeight.SemiBold
    val reg = FontWeight.Normal
    val base = Typography(
        displayLarge = l(57, 64, semi),
        displayMedium = l(45, 52, semi),
        displaySmall = l(36, 44, semi),
        headlineLarge = l(36, 44, semi),
        headlineMedium = l(24, 32, semi),
        headlineSmall = l(20, 26, semi),
        titleLarge = l(20, 28, semi),
        titleMedium = l(16, 22, semi),
        titleSmall = l(16, 22, semi),
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

private val BotanicalLight = lightColorScheme(
    primary = Color(0xFF4F6632),
    onPrimary = Color(0xFFEFFFD4),
    primaryContainer = Color(0xFFD0ECAB),
    onPrimaryContainer = Color(0xFF425826),
    secondary = Color(0xFF58634A),
    onSecondary = Color(0xFFEFFFD4),
    secondaryContainer = Color(0xFFDCE7C7),
    onSecondaryContainer = Color(0xFF425826),
    tertiary = Color(0xFF6A5F27),
    onTertiary = Color(0xFFEFFFD4),
    tertiaryContainer = Color(0xFFF6E6A0),
    onTertiaryContainer = Color(0xFF425826),
    error = Color(0xFFA73B21),
    onError = Color.White,
    background = Color(0xFFFAFAF0),
    onBackground = Color(0xFF303429),
    surface = Color(0xFFFAFAF0),
    onSurface = Color(0xFF303429),
    surfaceVariant = Color(0xFFE1E4D4),
    onSurfaceVariant = Color(0xFF5C6154),
    surfaceDim = Color(0xFFD8DCCC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F5E9),
    surfaceContainer = Color(0xFFEDEFE2),
    surfaceContainerHigh = Color(0xFFE7EADB),
    surfaceContainerHighest = Color(0xFFE1E4D4),
    inverseSurface = Color(0xFF0D0F0A),
    inverseOnSurface = Color(0xFFE4E7D7),
    outline = Color(0xFF787C6F),
    outlineVariant = Color(0xFFB0B4A5),
    scrim = Color(0x52000000)
)

private val BotanicalDark = darkColorScheme(
    primary = Color(0xFFB9CE9B),
    onPrimary = Color(0xFF34451F),
    primaryContainer = Color(0xFF465830),
    onPrimaryContainer = Color(0xFFD5EBB6),
    secondary = Color(0xFFC0CBAD),
    onSecondary = Color(0xFF34451F),
    secondaryContainer = Color(0xFF353F28),
    onSecondaryContainer = Color(0xFFD5EBB6),
    tertiary = Color(0xFFFFF4CB),
    onTertiary = Color(0xFF34451F),
    tertiaryContainer = Color(0xFFF6E6A0),
    onTertiaryContainer = Color(0xFF465830),
    error = Color(0xFFF97758),
    onError = Color.Black,
    background = Color(0xFF0D0F0A),
    onBackground = Color(0xFFE4E7D7),
    surface = Color(0xFF0D0F0A),
    onSurface = Color(0xFFE4E7D7),
    surfaceVariant = Color(0xFF23271D),
    onSurfaceVariant = Color(0xFF919587),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF12140E),
    surfaceContainer = Color(0xFF181B13),
    surfaceContainerHigh = Color(0xFF1D2118),
    surfaceContainerHighest = Color(0xFF23271D),
    inverseSurface = Color(0xFFFAFAF0),
    inverseOnSurface = Color(0xFF303429),
    outline = Color(0xFF73776A),
    outlineVariant = Color(0xFF45493E),
    scrim = Color(0x52000000)
)

private val ObsidianLight = lightColorScheme(
    primary = Color(0xFF2F5D50),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC9E7D8),
    onPrimaryContainer = Color(0xFF0B211B),
    secondary = Color(0xFF4D6359),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E7DC),
    onSecondaryContainer = Color(0xFF0B211B),
    tertiary = Color(0xFF635548),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE0D4),
    onTertiaryContainer = Color(0xFF231A12),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    background = Color(0xFFF7F5F0),
    onBackground = Color(0xFF1A1E1D),
    surface = Color(0xFFF7F5F0),
    onSurface = Color(0xFF1A1E1D),
    surfaceVariant = Color(0xFFDFE5E0),
    onSurfaceVariant = Color(0xFF404944),
    surfaceDim = Color(0xFFD8DAD3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1EEE8),
    surfaceContainer = Color(0xFFECE9E3),
    surfaceContainerHigh = Color(0xFFE6E3DC),
    surfaceContainerHighest = Color(0xFFDFDCD4),
    inverseSurface = Color(0xFF2A322F),
    inverseOnSurface = Color(0xFFECF0EC),
    outline = Color(0xFF6F7874),
    outlineVariant = Color(0xFFBEC9C2),
    scrim = Color(0x52000000)
)

private val ObsidianDark = darkColorScheme(
    primary = Color(0xFF7BD8BE),
    onPrimary = Color(0xFF00382B),
    primaryContainer = Color(0xFF00513F),
    onPrimaryContainer = Color(0xFFA7F0D6),
    secondary = Color(0xFFB2CCC0),
    onSecondary = Color(0xFF1D352C),
    secondaryContainer = Color(0xFF33493F),
    onSecondaryContainer = Color(0xFFCEE8DC),
    tertiary = Color(0xFFD8C4A8),
    onTertiary = Color(0xFF392E22),
    tertiaryContainer = Color(0xFF504434),
    onTertiaryContainer = Color(0xFFF2DFC2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF0E1312),
    onBackground = Color(0xFFE0E6E2),
    surface = Color(0xFF0E1312),
    onSurface = Color(0xFFE0E6E2),
    surfaceVariant = Color(0xFF1A2B26),
    onSurfaceVariant = Color(0xFFAEB9B3),
    surfaceContainerLowest = Color(0xFF060A09),
    surfaceContainerLow = Color(0xFF0E1312),
    surfaceContainer = Color(0xFF1A2B26),
    surfaceContainerHigh = Color(0xFF20352E),
    surfaceContainerHighest = Color(0xFF273E36),
    inverseSurface = Color(0xFFF7F5F0),
    inverseOnSurface = Color(0xFF1A1E1D),
    outline = Color(0xFF88938E),
    outlineVariant = Color(0xFF32403A),
    scrim = Color(0x52000000)
)

// Companion roles (onSecondary/onTertiary/inverseOnSurface) reuse palette
// values above; no new hues introduced.

/**
 * Seed catalogue: single source of truth for the theme picker and [CalcUTheme].
 *
 * Seed ids are stable storage ids — never rename them: backups and restores
 * compare the raw id string. Display labels may change freely.
 *
 * Groups (picker order):
 * - Dynamic: pseudo-seed "system" (wallpaper color when dynamic is on, API 31+).
 * - Botanical: custom light/dark schemes (representative argb = light primary).
 * - Classics: Ocean/Forest/Sunset/Grape/Slate/Mono/Amber.
 * - Vivid: Nord/Dracula/Tokyo/Gruvbox/Catppuccin/Kanagawa/Rosé Pine.
 * - Custom: user HSL seed id "custom" ([CustomThemeId]); argb is NOT in
 *   [allById] — it comes from the `custom_seed_argb` pref via [seedScheme].
 *
 * Legacy ids "light", "dark", "amoled", "contrast" are NOT seeds: they stay
 * resolvable in [CalcUTheme] for backup compat but are hidden from the picker
 * (covered by Mode + AMOLED toggle instead). All seeds are free forever.
 */
object CalcUThemeSeeds {
    data class Seed(val id: String, val label: String, val argb: Int)

    val botanical = Seed("botanical", "Botanical", 0xFF4F6632.toInt())

    val classics = listOf(
        Seed("ocean", "Ocean", 0xFF0061A4.toInt()),
        Seed("forest", "Forest", 0xFF1B6B4A.toInt()),
        Seed("sunset", "Sunset", 0xFFB23C17.toInt()),
        Seed("grape", "Grape", 0xFF6B4DAB.toInt()),
        Seed("slate", "Slate", 0xFF78909C.toInt()),
        Seed("mono", "Mono", 0xFF9AA0A6.toInt()),
        Seed("amber", "Amber", 0xFFFF8F00.toInt()),
        Seed("obsidian", "Obsidian", 0xFF2F5D50.toInt())
    )

    val vivid = listOf(
        Seed("nord", "Nord", 0xFF5E81AC.toInt()),
        Seed("dracula", "Dracula", 0xFFBD93F9.toInt()),
        Seed("tokyo", "Tokyo", 0xFF7AA2F7.toInt()),
        Seed("gruvbox", "Gruvbox", 0xFFD79921.toInt()),
        Seed("catppuccin", "Catppuccin", 0xFFCBA6F7.toInt()),
        Seed("kanagawa", "Kanagawa", 0xFF7E9CD8.toInt()),
        Seed("rosepine", "Rosé Pine", 0xFFEBBCBA.toInt())
    )

    val allById: Map<String, Seed> =
        (listOf(botanical) + classics + vivid).associateBy { it.id }

    fun argbFor(id: String): Int? = allById[id]?.argb
}

/**
 * Stable id for the user-defined custom seed. Stored in the existing "theme"
 * pref like every other seed id (never renamed so backups keep working).
 * The argb itself lives in the additive `custom_seed_argb` pref and is passed
 * to [CalcUTheme] as `customSeedArgb`; see the "custom" branch there.
 */
const val CustomThemeId = "custom"

/**
 * Opaque-argb validation for the custom seed. Null/0 means "not set".
 * Any set value is forced opaque so tonal palettes stay well-behaved.
 */
fun validatedCustomSeedArgb(raw: Int?): Int? {
    if (raw == null || raw == 0) return null
    return raw or 0xFF000000.toInt()
}

/**
 * HSL <-> opaque argb helpers for the custom-seed bottom sheet.
 * Hue is degrees 0-360, saturation/lightness are 0-1; all inputs clamped.
 * Pure functions (no Android/Compose dependencies) so they stay unit-testable.
 */
fun hslToSeedArgb(hueDeg: Float, saturation: Float, lightness: Float): Int {
    val h = hueDeg.coerceIn(0f, 360f).mod(360f) / 360f
    val s = saturation.coerceIn(0f, 1f)
    val l = lightness.coerceIn(0f, 1f)
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val x = c * (1f - kotlin.math.abs((h * 6f).mod(2f) - 1f))
    val m = l - c / 2f
    val (r, g, b) = when ((h * 6f).toInt().coerceIn(0, 5)) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val ri = ((r + m) * 255f).toInt().coerceIn(0, 255)
    val gi = ((g + m) * 255f).toInt().coerceIn(0, 255)
    val bi = ((b + m) * 255f).toInt().coerceIn(0, 255)
    return 0xFF000000.toInt() or (ri shl 16) or (gi shl 8) or bi
}

fun seedArgbToHsl(argb: Int): Triple<Float, Float, Float> {
    val r = ((argb shr 16) and 0xFF) / 255f
    val g = ((argb shr 8) and 0xFF) / 255f
    val b = (argb and 0xFF) / 255f
    val cmax = maxOf(r, g, b)
    val cmin = minOf(r, g, b)
    val delta = cmax - cmin
    val l = (cmax + cmin) / 2f
    if (delta == 0f) return Triple(0f, 0f, l.coerceIn(0f, 1f))
    val s = (delta / (1f - kotlin.math.abs(2f * l - 1f))).coerceIn(0f, 1f)
    val h = when (cmax) {
        r -> (((g - b) / delta).mod(6f)) * 60f
        g -> (((b - r) / delta) + 2f) * 60f
        else -> (((r - g) / delta) + 4f) * 60f
    }
    return Triple(h.mod(360f).coerceIn(0f, 360f), s, l.coerceIn(0f, 1f))
}

/**
 * Keypad shape ids (stable storage ids, same contract as seed ids).
 * Personalization idea re-implemented from Calc-OS (MIT, see Settings
 * attributions): Calc-OS maps pill/9999px (default), 16px rounded-rect and
 * 4px square onto `--calc-btn-radius`. Here Circles ~ Calc-OS pill default,
 * Squircle ~ 16px rounded rect, Pill ~ full stadium capsule.
 */
object KeypadShapeIds {
    const val Circles = "circles"
    const val Squircle = "squircle"
    const val Pill = "pill"

    val All: Set<String> = setOf(Circles, Squircle, Pill)

    fun coerce(raw: String?): String = if (raw != null && All.contains(raw)) raw else Circles
}

/**
 * Documented pure helper for keypad key shape.
 *
 * Follow-up wiring (call sites live outside the owned files — do NOT change
 * them here):
 * - CalculatorScreens.kt: `CalcKey` + `BackKey` currently hardcode
 *   `.clip(CircleShape)` / `.background(..., CircleShape)` (lines ~1113-1114,
 *   ~1266-1267). Thread `shape: Shape = keyShape(shapeId)` through `Keypad` ->
 *   `SimpleKeypad`/`ClassicKeypad`/`ModernKeypad` -> `DigitKey`/`SciKey`/`CalcKey`
 *   and `BackKey`, replacing both the clip and background shapes.
 * - NumPadSheet.kt: `PadKey` currently hardcodes `val circle = CircleShape`
 *   (line ~77) for Button/FilledTonalButton/OutlinedButton `shape =`. Add a
 *   `shape: Shape` parameter defaulting to `keyShape(KeypadShapeIds.Circles)`
 *   and pass the collected pref down from the caller.
 * - Pref flow mirror: SettingsRepository.keypadShape +
 *   CalcViewModel.keypadLayout pattern (`settingsRepo.keypadShape.stateIn(...)`
 *   next to `keypadLayout`, default "circles").
 * Existing call sites keep compiling: this helper is purely additive.
 */
fun keyShape(shapeId: String): Shape = when (shapeId) {
    KeypadShapeIds.Pill -> RoundedCornerShape(percent = 50)
    KeypadShapeIds.Squircle -> RoundedCornerShape(16.dp)
    else -> CircleShape
}

private fun ColorScheme.withPureBlackBackground(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF141414),
    onBackground = Color(0xFFE6E6E6),
    onSurface = Color(0xFFE6E6E6),
    onSurfaceVariant = Color(0xFFC6C6C6),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0B0B0B),
    surfaceContainer = Color(0xFF141414),
    surfaceContainerHigh = Color(0xFF1E1E1E),
    surfaceContainerHighest = Color(0xFF2A2A2A),
    inverseSurface = Color(0xFFE6E6E6),
    inverseOnSurface = Color(0xFF121212),
    outlineVariant = Color(0xFF2A2A2A)
)

/**
 * Radii lock: extraSmall 4 / small 8 / medium 12 / large 16 / extraLarge 28.
 * Hero surfaces use extraLarge (28), grouped cards use large (16). Frozen —
 * do not change without a design review.
 */
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

private fun colorToArgb(color: Color): Int {
    val a = (color.alpha * 255f).toInt().coerceIn(0, 255)
    val r = (color.red * 255f).toInt().coerceIn(0, 255)
    val g = (color.green * 255f).toInt().coerceIn(0, 255)
    val b = (color.blue * 255f).toInt().coerceIn(0, 255)
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

private fun relativeLuminance(color: Color): Float {
    fun channel(v: Float): Float {
        val c = v.coerceIn(0f, 1f).toDouble()
        return if (c <= 0.04045) (c / 12.92).toFloat() else Math.pow((c + 0.055) / 1.055, 2.4).toFloat()
    }
    return 0.2126f * channel(color.red) + 0.7152f * channel(color.green) + 0.0722f * channel(color.blue)
}

private fun contrastRatio(fg: Color, bg: Color): Float {
    val l1 = relativeLuminance(fg)
    val l2 = relativeLuminance(bg)
    val hi = maxOf(l1, l2)
    val lo = minOf(l1, l2)
    return (hi + 0.05f) / (lo + 0.05f)
}

@SuppressLint("RestrictedApi")
@Composable
fun dynamicFixedScheme(context: Context, dark: Boolean): ColorScheme {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return if (dark) darkColorScheme() else lightColorScheme()
    }
    val base = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    val seedInt = runCatching { colorToArgb(base.primary) }.getOrNull() ?: return base
    val palette = runCatching { TonalPalette.fromInt(seedInt) }.getOrNull() ?: return base
    fun tone(tone: Int, fallback: Color): Color =
        runCatching { Color(palette.tone(tone)) }.getOrNull() ?: fallback
    val lowest = if (dark) tone(0, base.surfaceContainerLowest) else tone(100, base.surfaceContainerLowest)
    val low = if (dark) tone(10, base.surfaceContainerLow) else tone(96, base.surfaceContainerLow)
    val container = if (dark) tone(12, base.surfaceContainer) else tone(94, base.surfaceContainer)
    val high = if (dark) tone(17, base.surfaceContainerHigh) else tone(92, base.surfaceContainerHigh)
    val highest = if (dark) tone(22, base.surfaceContainerHighest) else tone(90, base.surfaceContainerHighest)
    val candidates = if (dark) listOf(60, 70, 80, 50, 40, 90) else listOf(50, 40, 30, 60, 70)
    var fixedVariant = base.outlineVariant
    if (contrastRatio(fixedVariant, container) < 3f) {
        for (t in candidates) {
            val c = runCatching { Color(palette.tone(t)) }.getOrNull()
            if (c != null && contrastRatio(c, container) >= 3f) {
                fixedVariant = c
                break
            }
        }
    }
    return base.copy(
        surfaceContainerLowest = lowest,
        surfaceContainerLow = low,
        surfaceContainer = container,
        surfaceContainerHigh = high,
        surfaceContainerHighest = highest,
        outlineVariant = fixedVariant
    )
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

/**
 * App theme entry point.
 *
 * Resolution order (highest precedence first):
 * 1. AMOLED toggle ([amoled]): forces pure-black background/surfaces on top of
 *    whatever scheme resolved below.
 * 2. Mode ([mode]): "light" forces the light variant, "dark" forces the dark
 *    variant, "system" (default) defers to the seed id / system setting.
 * 3. Seed id ([theme]): "system" follows the system dark setting, "light"
 *    pins light, "dark" pins dark (both flippable by [mode]), custom seeds
 *    resolve via [CalcUThemeSeeds] + [seedScheme]. Id "custom" ([CustomThemeId])
 *    resolves via [seedScheme] from [customSeedArgb] (validated, opaque);
 *    with no custom argb stored it falls back exactly like an unknown seed.
 *    Legacy ids "amoled" and
 *    "contrast" still resolve to [FluentAmoled]/[FluentContrast] for backup
 *    compat but are hidden from the picker.
 * 4. Dynamic wallpaper ([dynamic] on API 31+): "system"/"light"/"dark" bases
 *    use the wallpaper-derived scheme; seed schemes stay tonal by design.
 *
 * Seed id strings are never renamed so backups keep working. [mode], [amoled]
 * and [customSeedArgb] are additive with safe defaults, so existing call sites
 * such as `CalcUTheme(theme = theme) { ... }` compile unchanged.
 * NOTE (follow-up): MainActivity must collect `SettingsRepository.customSeedArgb`
 * and pass it as [customSeedArgb]; until then theme id "custom" renders the
 * dynamic/Fluent fallback.
 */
@Composable
fun CalcUTheme(
    theme: String = "system",
    dark: Boolean = isSystemInDarkTheme(),
    dynamic: Boolean = true,
    mode: String = "system",
    amoled: Boolean = false,
    customSeedArgb: Int? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val legacy = when (theme) {
        "amoled" -> FluentAmoled
        "contrast" -> FluentContrast
        else -> null
    }
    if (legacy != null) {
        val scheme = if (amoled) legacy.withPureBlackBackground() else legacy
        MaterialTheme(colorScheme = scheme, shapes = FluentShapes, typography = fluentType(), content = content)
        return
    }
    val normalizedMode = if (mode == "light" || mode == "dark") mode else "system"
    val seedDark = when (theme) {
        "light" -> false
        "dark" -> true
        else -> dark
    }
    val effectiveDark = when (normalizedMode) {
        "light" -> false
        "dark" -> true
        else -> seedDark
    }
    val useDynamic = dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val base = when (theme) {
        "light", "dark", "system" ->
            if (useDynamic) {
                dynamicFixedScheme(context, effectiveDark)
            } else {
                if (effectiveDark) FluentDark else FluentLight
            }
        "botanical" -> if (effectiveDark) BotanicalDark else BotanicalLight
        "obsidian" -> if (effectiveDark) ObsidianDark else ObsidianLight
        CustomThemeId -> {
            val custom = validatedCustomSeedArgb(customSeedArgb)
            if (custom != null) {
                seedScheme(custom, effectiveDark)
            } else if (useDynamic) {
                dynamicFixedScheme(context, effectiveDark)
            } else {
                if (effectiveDark) FluentDark else FluentLight
            }
        }
        else -> {
            val argb = CalcUThemeSeeds.argbFor(theme)
            if (argb != null) {
                seedScheme(argb, effectiveDark)
            } else if (useDynamic) {
                dynamicFixedScheme(context, effectiveDark)
            } else {
                if (effectiveDark) FluentDark else FluentLight
            }
        }
    }
    val scheme = if (amoled) base.withPureBlackBackground() else base
    MaterialTheme(colorScheme = scheme, shapes = FluentShapes, typography = fluentType(), content = content)
}
