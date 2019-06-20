package it.cammino.risuscito.utils

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R

class ThemeUtils(context: Activity) {

    private val mContext: Context

    val current: Int
        get() {
            return LUtils.getResId("RisuscitoTheme_${getPrimaryThemeName()}_${getSecondaryThemeName()}", R.style::class.java)
        }

    init {
        mContext = context
    }

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
            ContextCompat.getColor(mContext, R.color.md_deep_orange_500) -> "pLightBlue"
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
            ContextCompat.getColor(mContext, R.color.md_amber_A200) -> "Amber"
            ContextCompat.getColor(mContext, R.color.md_blue_A200) -> "Blue"
            ContextCompat.getColor(mContext, R.color.md_cyan_A200) -> "Cyan"
            ContextCompat.getColor(mContext, R.color.md_green_A200) -> "Green"
            ContextCompat.getColor(mContext, R.color.md_indigo_A200) -> "Indigo"
            ContextCompat.getColor(mContext, R.color.md_lime_A200) -> "Lime"
            ContextCompat.getColor(mContext, R.color.md_orange_A200) -> "Orange"
            ContextCompat.getColor(mContext, R.color.md_pink_A200) -> "Pink"
            ContextCompat.getColor(mContext, R.color.md_purple_A200) -> "Purple"
            ContextCompat.getColor(mContext, R.color.md_teal_A200) -> "Teal"
            ContextCompat.getColor(mContext, R.color.md_red_A200) -> "Red"
            ContextCompat.getColor(mContext, R.color.md_deep_orange_A200) -> "DeepOrange"
            ContextCompat.getColor(mContext, R.color.md_deep_purple_A200) -> "DeepPurple"
            ContextCompat.getColor(mContext, R.color.md_light_blue_A200) -> "LightBlue"
            ContextCompat.getColor(mContext, R.color.md_light_green_A200) -> "LightGreen"
            ContextCompat.getColor(mContext, R.color.md_yellow_A200) -> "Yellow"
            else -> "Orange"
        }
    }

    private fun primaryColor(): Int {
        val defaultColor = ContextCompat.getColor(mContext, R.color.theme_primary)
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt("new_primary_color", defaultColor)
    }

    private fun accentColor(): Int {
        val defaultColor = ContextCompat.getColor(mContext, R.color.theme_accent)
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt("new_accent_color", defaultColor)
    }

    companion object {

        fun isDarkMode(context: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getBoolean("dark_mode", false)
        }

    }

}

