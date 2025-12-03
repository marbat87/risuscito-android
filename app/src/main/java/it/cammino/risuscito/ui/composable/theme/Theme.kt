package it.cammino.risuscito.ui.composable.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.preference.PreferenceManager
import it.cammino.risuscito.utils.Utility

private val lightScheme = lightColorScheme(
    primary = RisuscitoColor.primaryLight,
    onPrimary = RisuscitoColor.onPrimaryLight,
    primaryContainer = RisuscitoColor.primaryContainerLight,
    onPrimaryContainer = RisuscitoColor.onPrimaryContainerLight,
    secondary = RisuscitoColor.secondaryLight,
    onSecondary = RisuscitoColor.onSecondaryLight,
    secondaryContainer = RisuscitoColor.secondaryContainerLight,
    onSecondaryContainer = RisuscitoColor.onSecondaryContainerLight,
    tertiary = RisuscitoColor.tertiaryLight,
    onTertiary = RisuscitoColor.onTertiaryLight,
    tertiaryContainer = RisuscitoColor.tertiaryContainerLight,
    onTertiaryContainer = RisuscitoColor.onTertiaryContainerLight,
    error = RisuscitoColor.errorLight,
    onError = RisuscitoColor.onErrorLight,
    errorContainer = RisuscitoColor.errorContainerLight,
    onErrorContainer = RisuscitoColor.onErrorContainerLight,
    background = RisuscitoColor.backgroundLight,
    onBackground = RisuscitoColor.onBackgroundLight,
    surface = RisuscitoColor.surfaceLight,
    onSurface = RisuscitoColor.onSurfaceLight,
    surfaceVariant = RisuscitoColor.surfaceVariantLight,
    onSurfaceVariant = RisuscitoColor.onSurfaceVariantLight,
    outline = RisuscitoColor.outlineLight,
    outlineVariant = RisuscitoColor.outlineVariantLight,
    scrim = RisuscitoColor.scrimLight,
    inverseSurface = RisuscitoColor.inverseSurfaceLight,
    inverseOnSurface = RisuscitoColor.inverseOnSurfaceLight,
    inversePrimary = RisuscitoColor.inversePrimaryLight,
    surfaceDim = RisuscitoColor.surfaceDimLight,
    surfaceBright = RisuscitoColor.surfaceBrightLight,
    surfaceContainerLowest = RisuscitoColor.surfaceContainerLowestLight,
    surfaceContainerLow = RisuscitoColor.surfaceContainerLowLight,
    surfaceContainer = RisuscitoColor.surfaceContainerLight,
    surfaceContainerHigh = RisuscitoColor.surfaceContainerHighLight,
    surfaceContainerHighest = RisuscitoColor.surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = RisuscitoColor.primaryDark,
    onPrimary = RisuscitoColor.onPrimaryDark,
    primaryContainer = RisuscitoColor.primaryContainerDark,
    onPrimaryContainer = RisuscitoColor.onPrimaryContainerDark,
    secondary = RisuscitoColor.secondaryDark,
    onSecondary = RisuscitoColor.onSecondaryDark,
    secondaryContainer = RisuscitoColor.secondaryContainerDark,
    onSecondaryContainer = RisuscitoColor.onSecondaryContainerDark,
    tertiary = RisuscitoColor.tertiaryDark,
    onTertiary = RisuscitoColor.onTertiaryDark,
    tertiaryContainer = RisuscitoColor.tertiaryContainerDark,
    onTertiaryContainer = RisuscitoColor.onTertiaryContainerDark,
    error = RisuscitoColor.errorDark,
    onError = RisuscitoColor.onErrorDark,
    errorContainer = RisuscitoColor.errorContainerDark,
    onErrorContainer = RisuscitoColor.onErrorContainerDark,
    background = RisuscitoColor.backgroundDark,
    onBackground = RisuscitoColor.onBackgroundDark,
    surface = RisuscitoColor.surfaceDark,
    onSurface = RisuscitoColor.onSurfaceDark,
    surfaceVariant = RisuscitoColor.surfaceVariantDark,
    onSurfaceVariant = RisuscitoColor.onSurfaceVariantDark,
    outline = RisuscitoColor.outlineDark,
    outlineVariant = RisuscitoColor.outlineVariantDark,
    scrim = RisuscitoColor.scrimDark,
    inverseSurface = RisuscitoColor.inverseSurfaceDark,
    inverseOnSurface = RisuscitoColor.inverseOnSurfaceDark,
    inversePrimary = RisuscitoColor.inversePrimaryDark,
    surfaceDim = RisuscitoColor.surfaceDimDark,
    surfaceBright = RisuscitoColor.surfaceBrightDark,
    surfaceContainerLowest = RisuscitoColor.surfaceContainerLowestDark,
    surfaceContainerLow = RisuscitoColor.surfaceContainerLowDark,
    surfaceContainer = RisuscitoColor.surfaceContainerDark,
    surfaceContainerHigh = RisuscitoColor.surfaceContainerHighDark,
    surfaceContainerHighest = RisuscitoColor.surfaceContainerHighestDark,
)

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = RisuscitoColor.primaryLightMediumContrast,
    onPrimary = RisuscitoColor.onPrimaryLightMediumContrast,
    primaryContainer = RisuscitoColor.primaryContainerLightMediumContrast,
    onPrimaryContainer = RisuscitoColor.onPrimaryContainerLightMediumContrast,
    secondary = RisuscitoColor.secondaryLightMediumContrast,
    onSecondary = RisuscitoColor.onSecondaryLightMediumContrast,
    secondaryContainer = RisuscitoColor.secondaryContainerLightMediumContrast,
    onSecondaryContainer = RisuscitoColor.onSecondaryContainerLightMediumContrast,
    tertiary = RisuscitoColor.tertiaryLightMediumContrast,
    onTertiary = RisuscitoColor.onTertiaryLightMediumContrast,
    tertiaryContainer = RisuscitoColor.tertiaryContainerLightMediumContrast,
    onTertiaryContainer = RisuscitoColor.onTertiaryContainerLightMediumContrast,
    error = RisuscitoColor.errorLightMediumContrast,
    onError = RisuscitoColor.onErrorLightMediumContrast,
    errorContainer = RisuscitoColor.errorContainerLightMediumContrast,
    onErrorContainer = RisuscitoColor.onErrorContainerLightMediumContrast,
    background = RisuscitoColor.backgroundLightMediumContrast,
    onBackground = RisuscitoColor.onBackgroundLightMediumContrast,
    surface = RisuscitoColor.surfaceLightMediumContrast,
    onSurface = RisuscitoColor.onSurfaceLightMediumContrast,
    surfaceVariant = RisuscitoColor.surfaceVariantLightMediumContrast,
    onSurfaceVariant = RisuscitoColor.onSurfaceVariantLightMediumContrast,
    outline = RisuscitoColor.outlineLightMediumContrast,
    outlineVariant = RisuscitoColor.outlineVariantLightMediumContrast,
    scrim = RisuscitoColor.scrimLightMediumContrast,
    inverseSurface = RisuscitoColor.inverseSurfaceLightMediumContrast,
    inverseOnSurface = RisuscitoColor.inverseOnSurfaceLightMediumContrast,
    inversePrimary = RisuscitoColor.inversePrimaryLightMediumContrast,
    surfaceDim = RisuscitoColor.surfaceDimLightMediumContrast,
    surfaceBright = RisuscitoColor.surfaceBrightLightMediumContrast,
    surfaceContainerLowest = RisuscitoColor.surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = RisuscitoColor.surfaceContainerLowLightMediumContrast,
    surfaceContainer = RisuscitoColor.surfaceContainerLightMediumContrast,
    surfaceContainerHigh = RisuscitoColor.surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = RisuscitoColor.surfaceContainerHighestLightMediumContrast,
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = RisuscitoColor.primaryLightHighContrast,
    onPrimary = RisuscitoColor.onPrimaryLightHighContrast,
    primaryContainer = RisuscitoColor.primaryContainerLightHighContrast,
    onPrimaryContainer = RisuscitoColor.onPrimaryContainerLightHighContrast,
    secondary = RisuscitoColor.secondaryLightHighContrast,
    onSecondary = RisuscitoColor.onSecondaryLightHighContrast,
    secondaryContainer = RisuscitoColor.secondaryContainerLightHighContrast,
    onSecondaryContainer = RisuscitoColor.onSecondaryContainerLightHighContrast,
    tertiary = RisuscitoColor.tertiaryLightHighContrast,
    onTertiary = RisuscitoColor.onTertiaryLightHighContrast,
    tertiaryContainer = RisuscitoColor.tertiaryContainerLightHighContrast,
    onTertiaryContainer = RisuscitoColor.onTertiaryContainerLightHighContrast,
    error = RisuscitoColor.errorLightHighContrast,
    onError = RisuscitoColor.onErrorLightHighContrast,
    errorContainer = RisuscitoColor.errorContainerLightHighContrast,
    onErrorContainer = RisuscitoColor.onErrorContainerLightHighContrast,
    background = RisuscitoColor.backgroundLightHighContrast,
    onBackground = RisuscitoColor.onBackgroundLightHighContrast,
    surface = RisuscitoColor.surfaceLightHighContrast,
    onSurface = RisuscitoColor.onSurfaceLightHighContrast,
    surfaceVariant = RisuscitoColor.surfaceVariantLightHighContrast,
    onSurfaceVariant = RisuscitoColor.onSurfaceVariantLightHighContrast,
    outline = RisuscitoColor.outlineLightHighContrast,
    outlineVariant = RisuscitoColor.outlineVariantLightHighContrast,
    scrim = RisuscitoColor.scrimLightHighContrast,
    inverseSurface = RisuscitoColor.inverseSurfaceLightHighContrast,
    inverseOnSurface = RisuscitoColor.inverseOnSurfaceLightHighContrast,
    inversePrimary = RisuscitoColor.inversePrimaryLightHighContrast,
    surfaceDim = RisuscitoColor.surfaceDimLightHighContrast,
    surfaceBright = RisuscitoColor.surfaceBrightLightHighContrast,
    surfaceContainerLowest = RisuscitoColor.surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = RisuscitoColor.surfaceContainerLowLightHighContrast,
    surfaceContainer = RisuscitoColor.surfaceContainerLightHighContrast,
    surfaceContainerHigh = RisuscitoColor.surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = RisuscitoColor.surfaceContainerHighestLightHighContrast,
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = RisuscitoColor.primaryDarkMediumContrast,
    onPrimary = RisuscitoColor.onPrimaryDarkMediumContrast,
    primaryContainer = RisuscitoColor.primaryContainerDarkMediumContrast,
    onPrimaryContainer = RisuscitoColor.onPrimaryContainerDarkMediumContrast,
    secondary = RisuscitoColor.secondaryDarkMediumContrast,
    onSecondary = RisuscitoColor.onSecondaryDarkMediumContrast,
    secondaryContainer = RisuscitoColor.secondaryContainerDarkMediumContrast,
    onSecondaryContainer = RisuscitoColor.onSecondaryContainerDarkMediumContrast,
    tertiary = RisuscitoColor.tertiaryDarkMediumContrast,
    onTertiary = RisuscitoColor.onTertiaryDarkMediumContrast,
    tertiaryContainer = RisuscitoColor.tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = RisuscitoColor.onTertiaryContainerDarkMediumContrast,
    error = RisuscitoColor.errorDarkMediumContrast,
    onError = RisuscitoColor.onErrorDarkMediumContrast,
    errorContainer = RisuscitoColor.errorContainerDarkMediumContrast,
    onErrorContainer = RisuscitoColor.onErrorContainerDarkMediumContrast,
    background = RisuscitoColor.backgroundDarkMediumContrast,
    onBackground = RisuscitoColor.onBackgroundDarkMediumContrast,
    surface = RisuscitoColor.surfaceDarkMediumContrast,
    onSurface = RisuscitoColor.onSurfaceDarkMediumContrast,
    surfaceVariant = RisuscitoColor.surfaceVariantDarkMediumContrast,
    onSurfaceVariant = RisuscitoColor.onSurfaceVariantDarkMediumContrast,
    outline = RisuscitoColor.outlineDarkMediumContrast,
    outlineVariant = RisuscitoColor.outlineVariantDarkMediumContrast,
    scrim = RisuscitoColor.scrimDarkMediumContrast,
    inverseSurface = RisuscitoColor.inverseSurfaceDarkMediumContrast,
    inverseOnSurface = RisuscitoColor.inverseOnSurfaceDarkMediumContrast,
    inversePrimary = RisuscitoColor.inversePrimaryDarkMediumContrast,
    surfaceDim = RisuscitoColor.surfaceDimDarkMediumContrast,
    surfaceBright = RisuscitoColor.surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = RisuscitoColor.surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = RisuscitoColor.surfaceContainerLowDarkMediumContrast,
    surfaceContainer = RisuscitoColor.surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = RisuscitoColor.surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = RisuscitoColor.surfaceContainerHighestDarkMediumContrast,
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = RisuscitoColor.primaryDarkHighContrast,
    onPrimary = RisuscitoColor.onPrimaryDarkHighContrast,
    primaryContainer = RisuscitoColor.primaryContainerDarkHighContrast,
    onPrimaryContainer = RisuscitoColor.onPrimaryContainerDarkHighContrast,
    secondary = RisuscitoColor.secondaryDarkHighContrast,
    onSecondary = RisuscitoColor.onSecondaryDarkHighContrast,
    secondaryContainer = RisuscitoColor.secondaryContainerDarkHighContrast,
    onSecondaryContainer = RisuscitoColor.onSecondaryContainerDarkHighContrast,
    tertiary = RisuscitoColor.tertiaryDarkHighContrast,
    onTertiary = RisuscitoColor.onTertiaryDarkHighContrast,
    tertiaryContainer = RisuscitoColor.tertiaryContainerDarkHighContrast,
    onTertiaryContainer = RisuscitoColor.onTertiaryContainerDarkHighContrast,
    error = RisuscitoColor.errorDarkHighContrast,
    onError = RisuscitoColor.onErrorDarkHighContrast,
    errorContainer = RisuscitoColor.errorContainerDarkHighContrast,
    onErrorContainer = RisuscitoColor.onErrorContainerDarkHighContrast,
    background = RisuscitoColor.backgroundDarkHighContrast,
    onBackground = RisuscitoColor.onBackgroundDarkHighContrast,
    surface = RisuscitoColor.surfaceDarkHighContrast,
    onSurface = RisuscitoColor.onSurfaceDarkHighContrast,
    surfaceVariant = RisuscitoColor.surfaceVariantDarkHighContrast,
    onSurfaceVariant = RisuscitoColor.onSurfaceVariantDarkHighContrast,
    outline = RisuscitoColor.outlineDarkHighContrast,
    outlineVariant = RisuscitoColor.outlineVariantDarkHighContrast,
    scrim = RisuscitoColor.scrimDarkHighContrast,
    inverseSurface = RisuscitoColor.inverseSurfaceDarkHighContrast,
    inverseOnSurface = RisuscitoColor.inverseOnSurfaceDarkHighContrast,
    inversePrimary = RisuscitoColor.inversePrimaryDarkHighContrast,
    surfaceDim = RisuscitoColor.surfaceDimDarkHighContrast,
    surfaceBright = RisuscitoColor.surfaceBrightDarkHighContrast,
    surfaceContainerLowest = RisuscitoColor.surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = RisuscitoColor.surfaceContainerLowDarkHighContrast,
    surfaceContainer = RisuscitoColor.surfaceContainerDarkHighContrast,
    surfaceContainerHigh = RisuscitoColor.surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = RisuscitoColor.surfaceContainerHighestDarkHighContrast,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RisuscitoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    val context = LocalContext.current

    val dynamicColorSetting = PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean(Utility.DYNAMIC_COLORS, false)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dynamicColorSetting -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }



//    val dynamicColor = RisuscitoColor.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
//    val colors = RisuscitoColor.when {
//        dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
//        dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
//        darkTheme -> darkScheme
//        else -> expressiveLightColorScheme()
//    }

    val baseline = MaterialTheme.typography

    val risuscitoTypography = Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = regularFontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = regularFontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = regularFontFamily),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = regularFontFamily),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = regularFontFamily),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = regularFontFamily),
        titleLarge = baseline.titleLarge.copy(fontFamily = regularFontFamily),
        titleMedium = baseline.titleMedium.copy(fontFamily = mediumFontFamily),
        titleSmall = baseline.titleSmall.copy(fontFamily = mediumFontFamily),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = regularFontFamily),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = regularFontFamily),
        bodySmall = baseline.bodySmall.copy(fontFamily = regularFontFamily),
        labelLarge = baseline.labelLarge.copy(fontFamily = mediumFontFamily),
        labelMedium = baseline.labelMedium.copy(fontFamily = mediumFontFamily),
        labelSmall = baseline.labelSmall.copy(fontFamily = mediumFontFamily),
    )

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = risuscitoTypography,
        content = content
    )


//    val risuscitoTypography = Typography(
//        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = RisuscitoColor.risuscito_regular_font),
//        displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = RisuscitoColor.risuscito_regular_font),
//        displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = RisuscitoColor.risuscito_regular_font),
//
//        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = RisuscitoColor.risuscito_regular_font),
//        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = RisuscitoColor.risuscito_regular_font),
//        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = RisuscitoColor.risuscito_regular_font),
//
//        titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = RisuscitoColor.risuscito_regular_font),
//        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = RisuscitoColor.risuscito_medium_font),
//        titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = RisuscitoColor.risuscito_medium_font),
//
//        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = RisuscitoColor.risuscito_regular_font),
//        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = RisuscitoColor.risuscito_regular_font),
//        bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = RisuscitoColor.risuscito_regular_font),
//
//        labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = RisuscitoColor.risuscito_medium_font),
//        labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = RisuscitoColor.risuscito_medium_font),
//        labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = RisuscitoColor.risuscito_medium_font)
//    )
//
//    MaterialExpressiveTheme(
//        colorScheme = colors,
//        content = content,
//        typography = risuscitoTypography
//    )
}