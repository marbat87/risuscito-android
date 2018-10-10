package it.cammino.risuscito.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import it.cammino.risuscito.R

class ThemeUtils(context: Activity) {

    private val mContext: Context
    private var mPrimaryDarkMap: HashMap<Int, Int>? = null

    val current: Int
        get() {
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_amber_A200))
                return R.style.RisuscitoTheme_Amber
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_blue_A200))
                return R.style.RisuscitoTheme_Blue
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_cyan_A200))
                return R.style.RisuscitoTheme_Cyan
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_green_A200))
                return R.style.RisuscitoTheme_Green
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_indigo_A200))
                return R.style.RisuscitoTheme_Indigo
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_lime_A200))
                return R.style.RisuscitoTheme_Lime
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_orange_A200))
                return R.style.RisuscitoTheme_Orange
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_pink_A200))
                return R.style.RisuscitoTheme_Pink
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_purple_A200))
                return R.style.RisuscitoTheme_Purple
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_teal_A200))
                return R.style.RisuscitoTheme_Teal
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_red_A200))
                return R.style.RisuscitoTheme_Red
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_deep_orange_A200))
                return R.style.RisuscitoTheme_DeepOrange
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_deep_purple_A200))
                return R.style.RisuscitoTheme_DeepPurple
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_light_blue_A200))
                return R.style.RisuscitoTheme_LightBlue
            if (accentColor() == ContextCompat.getColor(mContext, R.color.md_light_green_A200))
                return R.style.RisuscitoTheme_LightGreen
            return if (accentColor() == ContextCompat.getColor(mContext, R.color.md_yellow_A200))
                R.style.RisuscitoTheme_Yellow
            else
                R.style.RisuscitoTheme
        }

    val isLightTheme: Boolean
        get() {
            val color = primaryColor()
            val a = 1 - (Color.red(color) * 0.299 + Color.green(color) * 0.587 + Color.blue(color) * 0.114) / 255
            return a < 0.5
        }

    init {
        mContext = context

        mPrimaryDarkMap = hashMapOf(ContextCompat.getColor(mContext, R.color.md_amber_500) to ContextCompat.getColor(mContext, R.color.md_amber_700)
                , ContextCompat.getColor(mContext, R.color.md_blue_500) to ContextCompat.getColor(mContext, R.color.md_blue_700)
                , ContextCompat.getColor(mContext, R.color.md_brown_500) to ContextCompat.getColor(mContext, R.color.md_brown_700)
                , ContextCompat.getColor(mContext, R.color.md_cyan_500) to ContextCompat.getColor(mContext, R.color.md_cyan_700)
                , ContextCompat.getColor(mContext, R.color.md_green_500) to ContextCompat.getColor(mContext, R.color.md_green_700)
                , ContextCompat.getColor(mContext, R.color.md_grey_500) to ContextCompat.getColor(mContext, R.color.md_grey_700)
                , ContextCompat.getColor(mContext, R.color.md_indigo_500) to ContextCompat.getColor(mContext, R.color.md_indigo_700)
                , ContextCompat.getColor(mContext, R.color.md_lime_500) to ContextCompat.getColor(mContext, R.color.md_lime_700)
                , ContextCompat.getColor(mContext, R.color.md_orange_500) to ContextCompat.getColor(mContext, R.color.md_orange_700)
                , ContextCompat.getColor(mContext, R.color.md_pink_500) to ContextCompat.getColor(mContext, R.color.md_pink_700)
                , ContextCompat.getColor(mContext, R.color.md_purple_500) to ContextCompat.getColor(mContext, R.color.md_purple_700)
                , ContextCompat.getColor(mContext, R.color.md_teal_500) to ContextCompat.getColor(mContext, R.color.md_teal_700)
                , ContextCompat.getColor(mContext, R.color.md_red_500) to ContextCompat.getColor(mContext, R.color.md_red_700)
                , ContextCompat.getColor(mContext, R.color.md_deep_orange_500) to ContextCompat.getColor(mContext, R.color.md_deep_orange_700)
                , ContextCompat.getColor(mContext, R.color.md_deep_purple_500) to ContextCompat.getColor(mContext, R.color.md_deep_purple_700)
                , ContextCompat.getColor(mContext, R.color.md_light_blue_500) to ContextCompat.getColor(mContext, R.color.md_light_blue_700)
                , ContextCompat.getColor(mContext, R.color.md_blue_grey_500) to ContextCompat.getColor(mContext, R.color.md_blue_grey_700)
                , ContextCompat.getColor(mContext, R.color.md_light_green_500) to ContextCompat.getColor(mContext, R.color.md_light_green_700)
                , ContextCompat.getColor(mContext, R.color.md_yellow_500) to ContextCompat.getColor(mContext, R.color.md_yellow_700)
        )
    }

    fun primaryColor(): Int {
        val defaultColor = ContextCompat.getColor(mContext, R.color.theme_primary)
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt("new_primary_color", defaultColor)
    }

    fun primaryColorDark(): Int {
        return mPrimaryDarkMap!![primaryColor()]!!
    }

    fun accentColor(): Int {
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

