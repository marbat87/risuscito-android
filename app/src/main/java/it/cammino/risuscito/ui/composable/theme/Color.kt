package it.cammino.risuscito.ui.composable.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

object RisuscitoColor {
    val md_theme_light_primary = Color(0xFF335BB1)
    val md_theme_light_onPrimary = Color(0xFFFFFFFF)
    val md_theme_light_primaryContainer = Color(0xFFD9E2FF)

    val md_theme_light_onPrimaryContainer = Color(0xFF001849)
// ..
// ..

    val md_theme_dark_primary = Color(0xFFACD370)
    val md_theme_dark_onPrimary = Color(0xFF213600)
    val md_theme_dark_primaryContainer = Color(0xFF324F00)
}

val darkColorPalette = darkColorScheme(
    primary = RisuscitoColor.md_theme_dark_primary,
    onPrimary = RisuscitoColor.md_theme_dark_onPrimary,
    primaryContainer = RisuscitoColor.md_theme_dark_primaryContainer,
//    onPrimaryContainer = RisuscitoColor.md_theme_dark_onPrimaryContainer,
//    secondary = RisuscitoColor.md_theme_dark_secondary,
//    onSecondary = RisuscitoColor.md_theme_dark_onSecondary
)