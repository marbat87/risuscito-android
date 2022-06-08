package it.cammino.risuscito.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.elevation.SurfaceColors
import it.cammino.risuscito.Utility
import java.util.*

fun String.capitalize(res: Resources): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(res.systemLocale) else it.toString()
    }
}

fun CharSequence.capitalize(res: Resources): String {
    return this.toString().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(res.systemLocale) else it.toString()
    }
}

fun Context.getTypedValueResId(resId: Int): Int {
    val outTypedValue = TypedValue()
    theme.resolveAttribute(resId, outTypedValue, true)
    return outTypedValue.resourceId
}

@Suppress("DEPRECATION")
private fun Configuration.getSystemLocaleLegacy(): Locale {
    return locale
}

@TargetApi(Build.VERSION_CODES.N)
private fun Configuration.getSystemLocaleN(): Locale {
    return locales.get(0)
}

val Resources.systemLocale: Locale
    get() {
        return if (OSUtils.hasN())
            configuration.getSystemLocaleN()
        else
            configuration.getSystemLocaleLegacy()
    }

fun Context.setDefaultNightMode() {
    when (prefNightMode) {
        ThemeUtils.LIGHT_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ThemeUtils.DARK_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        ThemeUtils.DEFAULT_MODE -> AppCompatDelegate.setDefaultNightMode(if (OSUtils.hasP()) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
    }
}

private val Context.prefNightMode: String
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
            .getString(Utility.NIGHT_MODE, ThemeUtils.DEFAULT_MODE)
            ?: ThemeUtils.DEFAULT_MODE
    }

val Context.dynamicColorOptions: DynamicColorsOptions
    get() {
        return DynamicColorsOptions.Builder()
            .setPrecondition { _, _ ->
                PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(Utility.DYNAMIC_COLORS, false)
            }
            .build()
    }

val Context.isDarkMode: Boolean
    get() {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

fun Activity.setupNavBarColor() {
    if (OSUtils.hasO()) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (!isDarkMode) setLightNavigationBar()
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Activity.setLightNavigationBar() {
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).isAppearanceLightNavigationBars = true
}

fun Activity.setLigthStatusBar(light: Boolean) {
    WindowCompat.getInsetsController(
        window,
        window.decorView
    ).isAppearanceLightStatusBars = light
    setLighStatusBarFlag(light)
}

private fun Activity.setLighStatusBarFlag(light: Boolean) {
    if (OSUtils.hasM())
        setLighStatusBarFlagM(light)
}

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.M)
private fun Activity.setLighStatusBarFlagM(light: Boolean) {
    if (light)
        window
            .decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}