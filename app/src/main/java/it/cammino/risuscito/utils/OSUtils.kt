package it.cammino.risuscito.utils

import android.os.Build
import java.util.*

@Suppress("unused")
object OSUtils {
    private const val LGE = "lge"
    private const val SAMSUNG = "samsung"
    private const val MEIZU = "meizu"

    /** Returns true if the device manufacturer is Meizu.  */
    fun isMeizuDevice(): Boolean {
        return Build.MANUFACTURER.lowercase(Locale.ENGLISH) == MEIZU
    }

    /** Returns true if the device manufacturer is LG.  */
    private fun isLGEDevice(): Boolean {
        return Build.MANUFACTURER.lowercase(Locale.ENGLISH) == LGE
    }

    /** Returns true if the device manufacturer is Samsung.  */
    private fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER.lowercase(Locale.ENGLISH) == SAMSUNG
    }

    /**
     * Returns true if the date input keyboard is potentially missing separator characters such as /.
     */
    fun isDateInputKeyboardMissingSeparatorCharacters(): Boolean {
        return isLGEDevice() || isSamsungDevice()
    }

    fun hasO(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun hasQ(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun hasM(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun hasN(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    fun hasP(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    fun hasS(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    fun isObySamsung(): Boolean {
        return (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) && isSamsungDevice()
    }

}