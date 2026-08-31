package it.cammino.risuscito.utils.extension

import android.content.Context

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