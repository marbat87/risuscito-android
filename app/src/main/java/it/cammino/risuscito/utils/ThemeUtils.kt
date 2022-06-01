package it.cammino.risuscito.utils

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColorsOptions
import it.cammino.risuscito.Utility.DYNAMIC_COLORS
import it.cammino.risuscito.Utility.NIGHT_MODE

class ThemeUtils {

    companion object {

        private val TAG = ThemeUtils::class.java.canonicalName
        private const val LIGHT_MODE = "light"
        private const val DARK_MODE = "dark"
        private const val DEFAULT_MODE = "default"

        fun isDarkMode(context: Context): Boolean {
            Log.d(
                TAG,
                "isDarkMode: ${(context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES}"
            )
            return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }

        fun setDefaultNightMode(context: Context) {
            when (getPrefNightMode(context)) {
                LIGHT_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                DARK_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                DEFAULT_MODE -> AppCompatDelegate.setDefaultNightMode(if (OSUtils.hasP()) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }
        }

        private fun getPrefNightMode(context: Context): String {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(NIGHT_MODE, DEFAULT_MODE)
                ?: "default"
        }

        fun getDynamicColorOptions(ctx: Context): DynamicColorsOptions {
            return DynamicColorsOptions.Builder()
                .setPrecondition { _, _ ->
                    PreferenceManager.getDefaultSharedPreferences(ctx)
                        .getBoolean(DYNAMIC_COLORS, false)
                }
                .build()
        }

    }

}

