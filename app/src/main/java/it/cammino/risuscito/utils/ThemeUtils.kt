package it.cammino.risuscito.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import androidx.content.edit
import it.cammino.risuscito.R

@Suppress("unused")
class ThemeUtils(context: Activity) {

    private val mContext: Context

    val current: Int
        get() {
            if (accentColor() == Color.parseColor("#FF8A80"))
                return R.style.RisuscitoTheme_Red1
            if (accentColor() == Color.parseColor("#FF5252"))
                return R.style.RisuscitoTheme_Red2
            if (accentColor() == Color.parseColor("#FF1744"))
                return R.style.RisuscitoTheme_Red3
            if (accentColor() == Color.parseColor("#D50000"))
                return R.style.RisuscitoTheme_Red4

            if (accentColor() == Color.parseColor("#FF80AB"))
                return R.style.RisuscitoTheme_Pink1
            if (accentColor() == Color.parseColor("#FF4081"))
                return R.style.RisuscitoTheme_Pink2
            if (accentColor() == Color.parseColor("#F50057"))
                return R.style.RisuscitoTheme_Pink3
            if (accentColor() == Color.parseColor("#C51162"))
                return R.style.RisuscitoTheme_Pink4

            if (accentColor() == Color.parseColor("#EA80FC"))
                return R.style.RisuscitoTheme_Purple1
            if (accentColor() == Color.parseColor("#E040FB"))
                return R.style.RisuscitoTheme_Purple2
            if (accentColor() == Color.parseColor("#D500F9"))
                return R.style.RisuscitoTheme_Purple3
            if (accentColor() == Color.parseColor("#AA00FF"))
                return R.style.RisuscitoTheme_Purple4

            if (accentColor() == Color.parseColor("#B388FF"))
                return R.style.RisuscitoTheme_Violet1
            if (accentColor() == Color.parseColor("#7C4DFF"))
                return R.style.RisuscitoTheme_Violet2
            if (accentColor() == Color.parseColor("#651FFF"))
                return R.style.RisuscitoTheme_Violet3
            if (accentColor() == Color.parseColor("#6200EA"))
                return R.style.RisuscitoTheme_Violet4

            if (accentColor() == Color.parseColor("#8C9EFF"))
                return R.style.RisuscitoTheme_Blue1
            if (accentColor() == Color.parseColor("#536DFE"))
                return R.style.RisuscitoTheme_Blue2
            if (accentColor() == Color.parseColor("#3D5AFE"))
                return R.style.RisuscitoTheme_Blue3
            if (accentColor() == Color.parseColor("#304FFE"))
                return R.style.RisuscitoTheme_Blue4

            if (accentColor() == Color.parseColor("#82B1FF"))
                return R.style.RisuscitoTheme_Azure1
            if (accentColor() == Color.parseColor("#448AFF"))
                return R.style.RisuscitoTheme_Azure2
            if (accentColor() == Color.parseColor("#2979FF"))
                return R.style.RisuscitoTheme_Azure3
            if (accentColor() == Color.parseColor("#2962FF"))
                return R.style.RisuscitoTheme_Azure4

            if (accentColor() == Color.parseColor("#80D8FF"))
                return R.style.RisuscitoTheme_Turqouise1
            if (accentColor() == Color.parseColor("#40C4FF"))
                return R.style.RisuscitoTheme_Turqouise2
            if (accentColor() == Color.parseColor("#00B0FF"))
                return R.style.RisuscitoTheme_Turqouise3
            if (accentColor() == Color.parseColor("#0091EA"))
                return R.style.RisuscitoTheme_Turqouise4

            if (accentColor() == Color.parseColor("#84FFFF"))
                return R.style.RisuscitoTheme_BlueLight1
            if (accentColor() == Color.parseColor("#18FFFF"))
                return R.style.RisuscitoTheme_BlueLight2
            if (accentColor() == Color.parseColor("#00E5FF"))
                return R.style.RisuscitoTheme_BlueLight3
            if (accentColor() == Color.parseColor("#00B8D4"))
                return R.style.RisuscitoTheme_BlueLight4

            if (accentColor() == Color.parseColor("#A7FFEB"))
                return R.style.RisuscitoTheme_GreenWater1
            if (accentColor() == Color.parseColor("#64FFDA"))
                return R.style.RisuscitoTheme_GreenWater2
            if (accentColor() == Color.parseColor("#1DE9B6"))
                return R.style.RisuscitoTheme_GreenWater3
            if (accentColor() == Color.parseColor("#00BFA5"))
                return R.style.RisuscitoTheme_GreenWater4

            if (accentColor() == Color.parseColor("#B9F6CA"))
                return R.style.RisuscitoTheme_Green1
            if (accentColor() == Color.parseColor("#69F0AE"))
                return R.style.RisuscitoTheme_Green2
            if (accentColor() == Color.parseColor("#00E676"))
                return R.style.RisuscitoTheme_Green3
            if (accentColor() == Color.parseColor("#00C853"))
                return R.style.RisuscitoTheme_Green4

            if (accentColor() == Color.parseColor("#CCFF90"))
                return R.style.RisuscitoTheme_GreenLight1
            if (accentColor() == Color.parseColor("#B2FF59"))
                return R.style.RisuscitoTheme_GreenLight2
            if (accentColor() == Color.parseColor("#76FF03"))
                return R.style.RisuscitoTheme_GreenLight3
            if (accentColor() == Color.parseColor("#64DD17"))
                return R.style.RisuscitoTheme_GreenLight4

            if (accentColor() == Color.parseColor("#F4FF81"))
                return R.style.RisuscitoTheme_Lime1
            if (accentColor() == Color.parseColor("#EEFF41"))
                return R.style.RisuscitoTheme_Lime2
            if (accentColor() == Color.parseColor("#C6FF00"))
                return R.style.RisuscitoTheme_Lime3
            if (accentColor() == Color.parseColor("#AEEA00"))
                return R.style.RisuscitoTheme_Lime4

            if (accentColor() == Color.parseColor("#FFFF8D"))
                return R.style.RisuscitoTheme_Yellow1
            if (accentColor() == Color.parseColor("#FFFF00"))
                return R.style.RisuscitoTheme_Yellow2
            if (accentColor() == Color.parseColor("#FFEA00"))
                return R.style.RisuscitoTheme_Yellow3
            if (accentColor() == Color.parseColor("#FFD600"))
                return R.style.RisuscitoTheme_Yellow4

            if (accentColor() == Color.parseColor("#FFE57F"))
                return R.style.RisuscitoTheme_OrangeLight1
            if (accentColor() == Color.parseColor("#FFD740"))
                return R.style.RisuscitoTheme_OrangeLight2
            if (accentColor() == Color.parseColor("#FFC400"))
                return R.style.RisuscitoTheme_OrangeLight3
            if (accentColor() == Color.parseColor("#FFAB00"))
                return R.style.RisuscitoTheme_OrangeLight4

            if (accentColor() == Color.parseColor("#FFD180"))
                return R.style.RisuscitoTheme_Orange1
            if (accentColor() == Color.parseColor("#FFAB40"))
                return R.style.RisuscitoTheme_Orange2
            if (accentColor() == Color.parseColor("#FF9100"))
                return R.style.RisuscitoTheme_Orange3
            if (accentColor() == Color.parseColor("#FF6D00"))
                return R.style.RisuscitoTheme_Orange4

            if (accentColor() == Color.parseColor("#FF9E80"))
                return R.style.RisuscitoTheme_OrangeDark1
            if (accentColor() == Color.parseColor("#FF6E40"))
                return R.style.RisuscitoTheme_OrangeDark2
            if (accentColor() == Color.parseColor("#FF3D00"))
                return R.style.RisuscitoTheme_OrangeDark3
            return if (accentColor() == Color.parseColor("#DD2C00"))
                R.style.RisuscitoTheme_OrangeDark4
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
    }

    fun primaryColor(): Int {
        val defaultColor = ContextCompat.getColor(mContext, R.color.theme_primary)
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt("primary_color", defaultColor)
    }

    fun primaryColor(newColor: Int) {
//        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("primary_color", newColor).apply()
        PreferenceManager.getDefaultSharedPreferences(mContext).edit { putInt("primary_color", newColor) }
    }

    fun primaryColorDark(): Int {
        return shiftColorDown(primaryColor())
    }

    fun primaryColorLight(): Int {
        return lighter(primaryColor(), 0.5f)

    }

    fun accentColor(): Int {
        val defaultColor = ContextCompat.getColor(mContext, R.color.theme_accent)
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt("accent_color", defaultColor)
    }

    fun accentColorLight(): Int {
        return lighter(accentColor(), 0.5f)

    }

    fun accentColorDark(): Int {
        return shiftColorDown(accentColor())
    }

    fun accentColor(newColor: Int) {
//        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("accent_color", newColor).apply()
        PreferenceManager.getDefaultSharedPreferences(mContext).edit { putInt("accent_color", newColor) }
    }

    companion object {

        fun isDarkMode(context: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getBoolean("dark_mode", false)
        }

        private fun shiftColorDown(color: Int): Int {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[2] *= 0.8f // value component
            return Color.HSVToColor(hsv)
        }

        /**
         * Lightens a color by a given factor.
         *
         * @param color
         * The color to lighten
         * @param factor
         * The factor to lighten the color. 0 will make the color unchanged. 1 will make the
         * color white.
         * @return lighter version of the specified color.
         */
        private fun lighter(color: Int, factor: Float): Int {
            val red = ((Color.red(color) * (1 - factor) / 255 + factor) * 255).toInt()
            val green = ((Color.green(color) * (1 - factor) / 255 + factor) * 255).toInt()
            val blue = ((Color.blue(color) * (1 - factor) / 255 + factor) * 255).toInt()
            return Color.argb(Color.alpha(color), red, green, blue)
        }
    }

}
