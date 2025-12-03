package it.cammino.risuscito.ui.composable.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import it.cammino.risuscito.R

//val risuscito_medium_font = FontFamily(
//    Font(resId = R.font.googlesans_medium)
//)
//
//val risuscito_regular_font = FontFamily(
//    Font(resId = R.font.googlesans_regular)
//)

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontName = GoogleFont("Nunito")

//val bodyFontFamily = FontFamily(
//    Font(
//        googleFont = fontName,
//        fontProvider = provider,
//    ),
//    Font(
//        resId = R.font.googlesans_regular
//    ),
//)
//
//val displayFontFamily = FontFamily(
//    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Medium),
//    Font(
//        resId = R.font.googlesans_medium
//    )
//)

val regularFontFamily = FontFamily(
    Font(
        googleFont = fontName,
        fontProvider = provider,
    ),
    Font(
        resId = R.font.googlesans_regular
    ),
)

val mediumFontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(
        resId = R.font.googlesans_medium
    )
)