package it.cammino.risuscito.ui.composable.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import it.cammino.risuscito.ui.composable.risuscito_medium_font
import it.cammino.risuscito.ui.composable.risuscito_regular_font

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RisuscitoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colors = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> darkColorPalette
        else -> expressiveLightColorScheme()
    }

    val risuscitoTypography = Typography(
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = risuscito_regular_font),
        displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = risuscito_regular_font),
        displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = risuscito_regular_font),

        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = risuscito_regular_font),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = risuscito_regular_font),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = risuscito_regular_font),

        titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = risuscito_regular_font),
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = risuscito_medium_font),
        titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = risuscito_medium_font),

        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = risuscito_regular_font),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = risuscito_regular_font),
        bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = risuscito_regular_font),

        labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = risuscito_medium_font),
        labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = risuscito_medium_font),
        labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = risuscito_medium_font)
    )

    MaterialExpressiveTheme(
        colorScheme = colors,
        content = content,
        typography = risuscitoTypography
    )
}