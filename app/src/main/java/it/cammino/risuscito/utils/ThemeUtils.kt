package it.cammino.risuscito.utils

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.Utility.NIGHT_MODE

class ThemeUtils {

    companion object {

        private val TAG = ThemeUtils::class.java.canonicalName
        private const val LIGHT_MODE = "light"
        private const val DARK_MODE = "dark"
        private const val DEFAULT_MODE = "default"

//        fun getStatusBarDefaultColor(context: Context): Int {
//            return if (isDarkMode(context))
//                Color.BLACK
//            else MaterialColors.getColor(context, R.attr.colorPrimaryVariant, TAG)
//        }

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
                DEFAULT_MODE -> AppCompatDelegate.setDefaultNightMode(if (LUtils.hasP()) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }
        }

//        fun getNightModeText(context: Context): String {
//            return when (getPrefNightMode(context)) {
//                LIGHT_MODE -> context.getString(R.string.night_mode_light)
//                DARK_MODE -> context.getString(R.string.night_mode_dark)
//                else -> context.getString(if (LUtils.hasP()) R.string.night_mode_auto_system else R.string.night_mode_auto_battery)
//            }
//        }

        private fun getPrefNightMode(context: Context): String {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(NIGHT_MODE, DEFAULT_MODE)
                ?: "default"
        }

    }

}

