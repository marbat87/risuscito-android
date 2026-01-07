package it.cammino.risuscito.utils.extension

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.annotation.RequiresApi
import it.cammino.risuscito.utils.OSUtils

fun String.capitalize(context: Context): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(context.systemLocale) else it.toString()
    }
}

fun CharSequence.capitalize(context: Context): String {
    return this.toString().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(context.systemLocale) else it.toString()
    }
}

@Suppress("DEPRECATION")
private val String.spannedFromHtmlLegacy: Spanned
    get() {
        return Html.fromHtml(this)
    }

private val String.spannedFromHtmlN: Spanned
    @RequiresApi(Build.VERSION_CODES.N)
    get() {
        return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    }

val String.spannedFromHtml: Spanned
    get() {
        return if (OSUtils.hasN())
            spannedFromHtmlN
        else
            spannedFromHtmlLegacy
    }