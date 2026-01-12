package it.cammino.risuscito.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ShareCompat
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.fragment.AboutFragment.Companion.TAG
import it.cammino.risuscito.utils.extension.shareThisApp
import java.util.Locale

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

    fun hasN(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    fun hasP(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    fun hasS(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

//    fun isObySamsung(): Boolean {
//        return (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1 || Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) && isSamsungDevice()
//    }

    fun hasT(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    fun hasU(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    fun hasV(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
    }

    fun getVersionCode(context: Context): Long {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (hasP())
            getVersionCodeP(packageInfo)
        else getVersionCodeLegacy(packageInfo)
    }

    @Suppress("DEPRECATION")
    private fun getVersionCodeLegacy(packageInfo: PackageInfo): Long {
        return packageInfo.versionCode.toLong()
    }

    private fun getVersionCodeP(packageInfo: PackageInfo): Long {
        return packageInfo.longVersionCode
    }

    fun rateOnClickAction(c: Context) {
        val uri = Uri.parse("market://details?id=" + c.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        } else {
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        }

        try {
            c.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            c.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + c.packageName)
                )
            )
        }
    }

    fun shareAppOnClickAction(context: Context) {
        context.startActivity(
            context.shareThisApp(context.getString(R.string.app_name))
        )
    }

    fun sendMailOnClickAction(context: Context) {
        try {
            context.startActivity(
                ShareCompat.IntentBuilder(context).setType("text/html")

                    .setSubject(context.getString(R.string.app_name))
                    .addEmailTo("marbat87@outlook.it").intent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error:", e)
            // No activity to handle intent
            Toast.makeText(
                context,
                R.string.mal_activity_exception,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun createWebsiteOnClickAction(c: Context, websiteUrl: Uri?) {
        val i = Intent(Intent.ACTION_VIEW)
        i.setData(websiteUrl)
        try {
            c.startActivity(i)
        } catch (e: java.lang.Exception) {
            // No activity to handle intent
            Toast.makeText(
                c,
                R.string.mal_activity_exception,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}