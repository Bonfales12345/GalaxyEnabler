package com.bonfales.galaxyenabler.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.bonfales.galaxyenabler.AppColorPalette
import com.bonfales.galaxyenabler.AppSettings
import com.bonfales.galaxyenabler.AppThemeMode

private val PixelBlueLight = expressiveLightColorScheme().copy(
    primary = Color(0xFF1A56D6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001945),
    secondary = Color(0xFF475D92),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDE2F9),
    onSecondaryContainer = Color(0xFF151B2C),
    tertiary = Color(0xFF006A6A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF9DF0F0),
    onTertiaryContainer = Color(0xFF002020),
)

private val PixelBlueDark = darkColorScheme(
    primary = Color(0xFFB1C5FF),
    onPrimary = Color(0xFF002D72),
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFFBFC6DC),
    onSecondary = Color(0xFF293042),
    secondaryContainer = Color(0xFF3F4759),
    onSecondaryContainer = Color(0xFFDBE2F9),
    tertiary = Color(0xFF80D5D4),
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF004F4F),
    onTertiaryContainer = Color(0xFF9DF0F0),
)

private val SageLight = expressiveLightColorScheme().copy(
    primary = Color(0xFF2B6B46),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFAEF3C4),
    onPrimaryContainer = Color(0xFF00210E),
    secondary = Color(0xFF4F6354),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1E8D5),
    onSecondaryContainer = Color(0xFF0C1F13),
    tertiary = Color(0xFF38656A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCEBF0),
    onTertiaryContainer = Color(0xFF002022),
)

private val SageDark = darkColorScheme(
    primary = Color(0xFF93D6A9),
    onPrimary = Color(0xFF00391D),
    primaryContainer = Color(0xFF0D5230),
    onPrimaryContainer = Color(0xFFAEF3C4),
    secondary = Color(0xFFB6CCB9),
    onSecondary = Color(0xFF213527),
    secondaryContainer = Color(0xFF384B3D),
    onSecondaryContainer = Color(0xFFD1E8D5),
    tertiary = Color(0xFFA0CFD4),
    onTertiary = Color(0xFF00363B),
    tertiaryContainer = Color(0xFF1F4D52),
    onTertiaryContainer = Color(0xFFBCEBF0),
)

private val CoralLight = expressiveLightColorScheme().copy(
    primary = Color(0xFFB33B24),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD2),
    onPrimaryContainer = Color(0xFF3D0700),
    secondary = Color(0xFF77574F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD2),
    onSecondaryContainer = Color(0xFF2C1510),
    tertiary = Color(0xFF6F5C2E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFAE0A6),
    onTertiaryContainer = Color(0xFF251A00),
)

private val CoralDark = darkColorScheme(
    primary = Color(0xFFFFB4A3),
    onPrimary = Color(0xFF631300),
    primaryContainer = Color(0xFF90240E),
    onPrimaryContainer = Color(0xFFFFDAD2),
    secondary = Color(0xFFE7BDB3),
    onSecondary = Color(0xFF442A23),
    secondaryContainer = Color(0xFF5D3F38),
    onSecondaryContainer = Color(0xFFFFDAD2),
    tertiary = Color(0xFFDDC48C),
    onTertiary = Color(0xFF3D2E04),
    tertiaryContainer = Color(0xFF554519),
    onTertiaryContainer = Color(0xFFFAE0A6),
)

private val IrisLight = expressiveLightColorScheme().copy(
    primary = Color(0xFF624FA9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE7DEFF),
    onPrimaryContainer = Color(0xFF1E0361),
    secondary = Color(0xFF605B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6DFF9),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7C5266),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E8),
    onTertiaryContainer = Color(0xFF301122),
)

private val IrisDark = darkColorScheme(
    primary = Color(0xFFCCBEFF),
    onPrimary = Color(0xFF331C78),
    primaryContainer = Color(0xFF4A368F),
    onPrimaryContainer = Color(0xFFE7DEFF),
    secondary = Color(0xFFCAC3DC),
    onSecondary = Color(0xFF322E41),
    secondaryContainer = Color(0xFF484459),
    onSecondaryContainer = Color(0xFFE6DFF9),
    tertiary = Color(0xFFEDB8CF),
    onTertiary = Color(0xFF492537),
    tertiaryContainer = Color(0xFF623B4E),
    onTertiaryContainer = Color(0xFFFFD8E8),
)

private val AmberLight = expressiveLightColorScheme().copy(
    primary = Color(0xFF8C5000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCBE),
    onPrimaryContainer = Color(0xFF2D1600),
    secondary = Color(0xFF725A42),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFEDDBE),
    onSecondaryContainer = Color(0xFF281806),
    tertiary = Color(0xFF57633B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDBE8B6),
    onTertiaryContainer = Color(0xFF151F01),
)

private val AmberDark = darkColorScheme(
    primary = Color(0xFFFFB86E),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF6A3C00),
    onPrimaryContainer = Color(0xFFFFDCBE),
    secondary = Color(0xFFE1C1A3),
    onSecondary = Color(0xFF402C18),
    secondaryContainer = Color(0xFF59422C),
    onSecondaryContainer = Color(0xFFFEDDBE),
    tertiary = Color(0xFFBFCB9B),
    onTertiary = Color(0xFF2A3411),
    tertiaryContainer = Color(0xFF404B25),
    onTertiaryContainer = Color(0xFFDBE8B6),
)

private val MonochromeLight = expressiveLightColorScheme().copy(
    primary = Color(0xFF202124),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE2E2E6),
    onPrimaryContainer = Color(0xFF121316),
    secondary = Color(0xFF5D5E62),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE4E4E8),
    onSecondaryContainer = Color(0xFF1A1B1E),
    tertiary = Color(0xFF46484C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDADCE0),
    onTertiaryContainer = Color(0xFF141518),
)

private val MonochromeDark = darkColorScheme(
    primary = Color(0xFFE3E3E8),
    onPrimary = Color(0xFF1A1B1E),
    primaryContainer = Color(0xFF3A3B40),
    onPrimaryContainer = Color(0xFFF1F1F4),
    secondary = Color(0xFFC5C6CA),
    onSecondary = Color(0xFF2E3034),
    secondaryContainer = Color(0xFF44464B),
    onSecondaryContainer = Color(0xFFE2E2E6),
    tertiary = Color(0xFFD0D2D6),
    onTertiary = Color(0xFF24262A),
    tertiaryContainer = Color(0xFF3E4045),
    onTertiaryContainer = Color(0xFFECEEF2),
)

private fun resolvePaletteScheme(
    palette: AppColorPalette,
    darkTheme: Boolean,
    dynamicScheme: ColorScheme,
): ColorScheme = when (palette) {
    AppColorPalette.DYNAMIC -> dynamicScheme
    AppColorPalette.PIXEL_BLUE -> if (darkTheme) PixelBlueDark else PixelBlueLight
    AppColorPalette.SAGE -> if (darkTheme) SageDark else SageLight
    AppColorPalette.CORAL -> if (darkTheme) CoralDark else CoralLight
    AppColorPalette.IRIS -> if (darkTheme) IrisDark else IrisLight
    AppColorPalette.AMBER -> if (darkTheme) AmberDark else AmberLight
    AppColorPalette.MONOCHROME -> if (darkTheme) MonochromeDark else MonochromeLight
}

fun resolveDynamicColorScheme(
    context: android.content.Context,
    darkTheme: Boolean,
): ColorScheme = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
} else {
    if (darkTheme) PixelBlueDark else PixelBlueLight
}

fun palettePreviewColors(
    palette: AppColorPalette,
    darkTheme: Boolean,
    dynamicScheme: ColorScheme,
): Triple<Color, Color, Color> {
    val scheme = resolvePaletteScheme(palette, darkTheme, dynamicScheme)
    return Triple(scheme.primary, scheme.primaryContainer, scheme.tertiary)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GalaxyEnablerTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val darkTheme = when (settings.themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val dynamicScheme = resolveDynamicColorScheme(context, darkTheme)
    val baseScheme = resolvePaletteScheme(settings.colorPalette, darkTheme, dynamicScheme)
    val colorScheme = if (darkTheme && settings.amoledBlack) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color(0xFF0A0A0D),
            surfaceContainer = Color(0xFF111116),
            surfaceContainerHigh = Color(0xFF18181F),
            surfaceContainerHighest = Color(0xFF21212B),
        )
    } else {
        baseScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = if (settings.expressiveMotion) MotionScheme.expressive() else MotionScheme.standard(),
        shapes = Shapes(),
        typography = Typography(),
        content = content,
    )
}
