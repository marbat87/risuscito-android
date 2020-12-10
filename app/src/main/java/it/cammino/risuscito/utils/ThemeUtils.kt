package it.cammino.risuscito.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.MaterialColors
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility.NIGHT_MODE
import it.cammino.risuscito.Utility.PRIMARY_COLOR
import it.cammino.risuscito.Utility.SECONDARY_COLOR

class ThemeUtils(context: Context) {

    private val mContext: Context = context

    val current: Int
        get() = LUtils.getResId("Risuscito_Theme_${getPrimaryThemeName()}_${getSecondaryThemeName()}", R.style::class.java)

    private fun getPrimaryThemeName(): String {
        return when (primaryColor()) {
            ContextCompat.getColor(mContext, R.color.md_amber_500) -> "pAmber"
            ContextCompat.getColor(mContext, R.color.md_blue_500) -> "pBlue"
            ContextCompat.getColor(mContext, R.color.md_brown_500) -> "pBrown"
            ContextCompat.getColor(mContext, R.color.md_cyan_500) -> "pCyan"
            ContextCompat.getColor(mContext, R.color.md_green_500) -> "pGreen"
            ContextCompat.getColor(mContext, R.color.md_grey_500) -> "pGrey"
            ContextCompat.getColor(mContext, R.color.md_indigo_500) -> "pIndigo"
            ContextCompat.getColor(mContext, R.color.md_lime_500) -> "pLime"
            ContextCompat.getColor(mContext, R.color.md_orange_500) -> "pOrange"
            ContextCompat.getColor(mContext, R.color.md_pink_500) -> "pPink"
            ContextCompat.getColor(mContext, R.color.md_purple_500) -> "pPurple"
            ContextCompat.getColor(mContext, R.color.md_teal_500) -> "pTeal"
            ContextCompat.getColor(mContext, R.color.md_red_500) -> "pRed"
            ContextCompat.getColor(mContext, R.color.md_deep_orange_500) -> "pDeepOrange"
            ContextCompat.getColor(mContext, R.color.md_deep_purple_500) -> "pDeepPurple"
            ContextCompat.getColor(mContext, R.color.md_light_blue_500) -> "pLightBlue"
            ContextCompat.getColor(mContext, R.color.md_blue_grey_500) -> "pBlueGrey"
            ContextCompat.getColor(mContext, R.color.md_light_green_500) -> "pLightGreen"
            ContextCompat.getColor(mContext, R.color.md_yellow_500) -> "pYellow"
            else -> "pIndigo"
        }
    }

    private fun getSecondaryThemeName(): String {
        return when (accentColor()) {
            ContextCompat.getColor(mContext, R.color.md_amber_a200) -> "Amber"
            ContextCompat.getColor(mContext, R.color.md_blue_a200) -> "Blue"
            ContextCompat.getColor(mContext, R.color.md_cyan_a200) -> "Cyan"
            ContextCompat.getColor(mContext, R.color.md_green_a200) -> "Green"
            ContextCompat.getColor(mContext, R.color.md_indigo_a200) -> "Indigo"
            ContextCompat.getColor(mContext, R.color.md_lime_a200) -> "Lime"
            ContextCompat.getColor(mContext, R.color.md_orange_a200) -> "Orange"
            ContextCompat.getColor(mContext, R.color.md_pink_a200) -> "Pink"
            ContextCompat.getColor(mContext, R.color.md_purple_a200) -> "Purple"
            ContextCompat.getColor(mContext, R.color.md_teal_a200) -> "Teal"
            ContextCompat.getColor(mContext, R.color.md_red_a200) -> "Red"
            ContextCompat.getColor(mContext, R.color.md_deep_orange_a200) -> "DeepOrange"
            ContextCompat.getColor(mContext, R.color.md_deep_purple_a200) -> "DeepPurple"
            ContextCompat.getColor(mContext, R.color.md_light_blue_a200) -> "LightBlue"
            ContextCompat.getColor(mContext, R.color.md_light_green_a200) -> "LightGreen"
            ContextCompat.getColor(mContext, R.color.md_yellow_a200) -> "Yellow"
            else -> "Orange"
        }
    }

    private fun primaryColor(): Int {
        val defaultColor = ContextCompat.getColor(mContext, R.color.theme_primary)
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(PRIMARY_COLOR, defaultColor)
    }

    private fun accentColor(): Int {
        val defaultColor = ContextCompat.getColor(mContext, R.color.theme_accent)
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(SECONDARY_COLOR, defaultColor)
    }

    companion object {

        private val TAG = ThemeUtils::class.java.canonicalName
        private const val LIGHT_MODE = "light"
        private const val DARK_MODE = "dark"
        private const val DEFAULT_MODE = "default"

        fun getStatusBarDefaultColor(context: Context): Int {
            return if (isDarkMode(context))
                Color.BLACK
            else MaterialColors.getColor(context, R.attr.colorPrimaryVariant, TAG)
        }

        fun isDarkMode(context: Context): Boolean {
            Log.d(TAG, "isDarkMode: ${(context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES}")
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
            return PreferenceManager.getDefaultSharedPreferences(context).getString(NIGHT_MODE, DEFAULT_MODE)
                    ?: "default"
        }

    }

}

