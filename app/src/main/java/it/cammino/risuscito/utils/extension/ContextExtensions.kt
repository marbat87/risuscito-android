package it.cammino.risuscito.utils.extension

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColorsOptions
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.Utility
import java.io.BufferedReader
import java.io.InputStreamReader


fun Context.getTypedValueResId(resId: Int): Int {
    val outTypedValue = TypedValue()
    theme.resolveAttribute(resId, outTypedValue, true)
    return outTypedValue.resourceId
}

fun Context.setDefaultNightMode() {
    when (prefNightMode) {
        LIGHT_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        DARK_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        DEFAULT_MODE -> AppCompatDelegate.setDefaultNightMode(if (OSUtils.hasP()) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
    }
}

private val Context.prefNightMode: String
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
            .getString(Utility.NIGHT_MODE, DEFAULT_MODE)
            ?: DEFAULT_MODE
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

private const val LIGHT_MODE = "light"
private const val DARK_MODE = "dark"
private const val DEFAULT_MODE = "default"

fun Context.readTextFromResource(resourceID: String): String {
    val inputStream =
        resources.openRawResource(Utility.getResId(resourceID, R.raw::class.java))
    val br = BufferedReader(InputStreamReader(inputStream, "utf-8"))
    var line: String? = br.readLine()
    val cantoTrasportato = StringBuffer()

    while (line != null) {
//            Log.d(TAG, "line: $line")
        cantoTrasportato.append(line)
        cantoTrasportato.append("\n")
        line = br.readLine()
    }
    br.close()
    return cantoTrasportato.toString()
}

val Context.isDefaultLocationPublic: Boolean
    get() {
        return Integer.parseInt(
            PreferenceManager.getDefaultSharedPreferences(this)
                .getString(Utility.SAVE_LOCATION, "0")
                ?: "0"
        ) == 1
    }

val Context.isOnline: Boolean
    get() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return if (OSUtils.hasM())
            connectivityManager?.isOnlineM == true
        else connectivityManager?.isOnlineLegacy == true
    }

private val ConnectivityManager.isOnlineM: Boolean
    get() {
        val network = activeNetwork
        val capabilities = getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

@Suppress("DEPRECATION")
private val ConnectivityManager.isOnlineLegacy: Boolean
    get() {
        return activeNetworkInfo?.isConnected == true
    }

val Context.isOnTablet: Boolean
    get() = resources.getBoolean(R.bool.is_tablet)

val Context.hasThreeColumns: Boolean
    get() = resources.getBoolean(R.bool.has_three_columns)

val Context.isGridLayout: Boolean
    get() = resources.getBoolean(R.bool.is_grid_layout)

val Context.isLandscape: Boolean
    get() = resources.getBoolean(R.bool.landscape)

val Context.isFabExpansionLeft: Boolean
    get() = resources.getBoolean(R.bool.fab_orientation_left)