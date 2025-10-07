package it.cammino.risuscito.utils.extension

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColorsOptions
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.RisuscitoApplication
import it.cammino.risuscito.utils.LocaleManager
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.Utility
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale


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

fun Resources.readTextFromResource(resourceID: String): String {
    val inputStream =
        openRawResource(Utility.getResId(resourceID, R.raw::class.java))
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
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

val Context.isOnTablet: Boolean
    get() = resources.getBoolean(R.bool.is_tablet)

val Context.isOnPhone: Boolean
    get() = resources.getBoolean(R.bool.is_phone_view)

val Context.isGridLayout: Boolean
    get() = resources.getBoolean(R.bool.is_grid_layout)

val Context.isLandscape: Boolean
    get() = resources.getBoolean(R.bool.landscape)

val Context.isFabExpansionLeft: Boolean
    get() = resources.getBoolean(R.bool.fab_orientation_left)

fun PackageManager.queryIntentActivities(intent: Intent): MutableList<ResolveInfo> {
    return if (OSUtils.hasT())
        queryIntentActivitiesTiramisu(intent)
    else
        queryIntentActivitiesLegacy(intent)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun PackageManager.queryIntentActivitiesTiramisu(intent: Intent): MutableList<ResolveInfo> {
    return queryIntentActivities(
        intent,
        PackageManager.ResolveInfoFlags.of(0)
    )
}

fun PackageManager.queryIntentActivitiesLegacy(intent: Intent): MutableList<ResolveInfo> {
    return queryIntentActivities(intent, 0)
}

fun PackageManager.getPackageInfo(packageName: String): PackageInfo {
    return if (OSUtils.hasT())
        getPackageInfoTiramisu(packageName)
    else
        getPackageInfoLegacy(packageName)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun PackageManager.getPackageInfoTiramisu(packageName: String): PackageInfo {
    return getPackageInfo(
        packageName,
        PackageManager.PackageInfoFlags.of(0)
    )
}

fun PackageManager.getPackageInfoLegacy(packageName: String): PackageInfo {
    return getPackageInfo(packageName, 0)
}

fun Context.shareThisApp(subject: String?): Intent {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.uri_play_store_app_website, packageName))
    return intent
}

val Context.systemLocale: Locale
    get() {
        return AppCompatDelegate.getApplicationLocales()[0] ?: run {
            return if (RisuscitoApplication.localeManager.getLanguage(this) == LocaleManager.LANGUAGE_ENGLISH_PHILIPPINES)
                Locale.Builder()
                    .setLanguage(LocaleManager.LANGUAGE_ENGLISH)
                    .setRegion(LocaleManager.COUNTRY_PHILIPPINES) // Usa setRegion per il paese
                    .build()
            else
                Locale.Builder()
                    .setLanguage(RisuscitoApplication.localeManager.getLanguage(this))
                    .build()
        }
    }

val Fragment.systemLocale: Locale
    get() {
        return requireContext().systemLocale
    }