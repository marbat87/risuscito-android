package it.cammino.risuscito

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.holder.StringHolder
import it.cammino.risuscito.utils.ThemeUtils
import java.io.File
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

object Utility {

    // Costanti per le impostazioni
    private val TAG = Utility::class.java.canonicalName
    const val SCREEN_ON = "sempre_acceso"
    const val SYSTEM_LANGUAGE = "lingua_sistema"
    const val DB_RESET = "db_reset"
    const val CHANGE_LANGUAGE = "changed"
    const val CLICK_DELAY: Long = 500
    internal const val SHOW_SECONDA = "mostra_seconda_lettura"
    internal const val SHOW_PACE = "mostra_canto_pace"
    internal const val SAVE_LOCATION = "memoria_salvataggio_scelta"
    internal const val DEFAULT_INDEX = "indice_predefinito"
    internal const val DEFAULT_SEARCH = "ricerca_predefinita"
    internal const val SHOW_SANTO = "mostra_santo"
    internal const val SHOW_AUDIO = "mostra_audio"
    internal const val SIGNED_IN = "signed_id"
    internal const val SHOW_OFFERTORIO = "mostra_canto_offertorio"
    internal const val PREFERITI_OPEN = "preferiti_open"
    internal const val HISTORY_OPEN = "history_open"
    internal const val INTRO_CONSEGNATI = "intro_consegnati_test"
    internal const val INTRO_CONSEGNATI_2 = "intro_consegnati_2_test"
    internal const val INTRO_PAGINARENDER = "intro_paginarender_test"
    internal const val INTRO_CREALISTA = "intro_crealista_test"
    internal const val INTRO_CREALISTA_2 = "intro_crealista_2_test"
    internal const val INTRO_CUSTOMLISTS = "intro_customlists_test_2"
    internal const val ULTIMA_APP_USATA = "ULTIMA_APP_USATA"
    internal const val CLICK_DELAY_SELECTION: Long = 300
    // Costanti per il passaggio dati alla pagina di visualizzazione canto in fullscreen
    internal const val URL_CANTO = "urlCanto"
    internal const val SPEED_VALUE = "speedValue"
    internal const val SCROLL_PLAYING = "scrollPlaying"
    internal const val ID_CANTO = "idCanto"
    internal const val PAGINA = "pagina"
    internal const val TIPO_LISTA = "tipoLista"
    internal const val WRITE_STORAGE_RC = 123


    /* Checks if external storage is available for read and write */
    internal val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    /* Checks if external storage is available to at least read */
    private val isExternalStorageReadable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

    internal fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return if (LUtils.hasM())
            isOnlineM(connectivityManager)
        else isOnlineLegacy(connectivityManager)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun isOnlineM(connectivityManager: ConnectivityManager?): Boolean {
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    @Suppress("DEPRECATION")
    private fun isOnlineLegacy(connectivityManager: ConnectivityManager?): Boolean {
        return connectivityManager?.activeNetworkInfo?.isConnected == true
    }

    /* Filtra il link di input per tenere solo il nome del file */
    internal fun filterMediaLinkNew(link: String?): String {
        link?.let {
            return if (it.isEmpty())
                it
            else {
                return when {
                    it.indexOf("resuscicanti") > 0 -> {
                        val start = it.indexOf(".com/")
                        it.substring(start + 5).replace("%20".toRegex(), "_")
                    }
                    it.indexOf("marbat87") > 0 -> {
                        val start = it.indexOf("audio/")
                        it.substring(start + 6).replace("%20".toRegex(), "_")
                    }
                    else -> it
                }
            }
        } ?: return ""
    }

    /* Filtra il link di input per tenere solo il nome del file */
    internal fun filterMediaLink(link: String?): String {
        link?.let {
            return if (it.isEmpty())
                it
            else {
                when {
                    it.indexOf("resuscicanti") > 0 -> {
                        val start = it.indexOf(".com/")
                        it.substring(start + 5)
                    }
                    it.indexOf("marbat87") > 0 -> {
                        val start = it.indexOf("audio/")
                        it.substring(start + 6)
                    }
                    else -> it
                }
            }
        } ?: return ""
    }

    fun retrieveMediaFileLink(activity: Context, link: String?, cercaEsterno: Boolean): String {

        if (link.isNullOrEmpty()) return ""

        if (isExternalStorageReadable && cercaEsterno) {
            Log.d(TAG, "retrieveMediaFileLink: " + filterMediaLinkNew(link))
            // cerca file esterno con nuovi path e nome
            var fileExt = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "/Risuscitò/" + filterMediaLinkNew(link))
            if (fileExt.exists()) {
                Log.d(TAG, "FILE esterno1: " + fileExt.absolutePath)
                return fileExt.absolutePath
            } else {
                // cerca file esterno con vecchi path e nome
                val fileArray = ContextCompat.getExternalFilesDirs(activity, null)
                fileExt = File(fileArray[0], filterMediaLink(link))
                if (fileExt.exists()) {
                    Log.d(TAG, "FILE esterno2: " + fileExt.absolutePath)
                    return fileExt.absolutePath
                } else
                    Log.d(TAG, "FILE ESTERNO NON TROVATO")
            }
//            val values = ContentValues()
//            values.put(MediaStore.Audio.Media.TITLE, filterMediaLinkNew(link))
//            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "/Risuscitò/")
//            val fileUri = activity.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
//            fileUri?.let {
//                Log.d(TAG, "FILE esterno1: ${it.path}")
//                return it.path ?: ""
//            }
//            val fileArray = ContextCompat.getExternalFilesDirs(activity, null)
//            val fileExt = File(fileArray[0], filterMediaLink(link))
//            if (fileExt.exists()) {
//                Log.d(TAG, "FILE esterno: " + fileExt.absolutePath)
//                return fileExt.absolutePath
//            } else
//                Log.d(TAG, "FILE ESTERNO NON TROVATO")
        } else {
            Log.d(TAG, "isExternalStorageReadable: FALSE")
        }

        val fileInt = File(activity.filesDir, filterMediaLink(link))
        if (fileInt.exists()) {
            Log.d(TAG, "FILE interno: " + fileInt.absolutePath)
            return fileInt.absolutePath
        } else
            Log.v(TAG, "FILE INTERNO NON TROVATO")
        //		Log.i("FILE INTERNO:", "NON TROVATO");
        return ""
    }

    @SuppressLint("NewApi")
    fun setupTransparentTints(context: Activity, color: Int, hasNavDrawer: Boolean) {
        if (!hasNavDrawer && LUtils.hasL())
            context.window.statusBarColor = color
    }

    @SuppressLint("NewApi")
    fun setupNavBarColor(context: Activity) {
        if (LUtils.hasO()) {
            context.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            context.window.decorView.setBackgroundColor(ContextCompat.getColor(context, if (ThemeUtils.isDarkMode(context)) R.color.design_dark_default_color_background else R.color.design_default_color_background))
            context.window.navigationBarColor = Color.TRANSPARENT
        }
    }

    internal fun random(start: Int, end: Int): Int {
        return Random().nextInt(end - start + 1) + start
    }

    internal fun isLowerCase(ch: Char): Boolean {
        return ch in 'a'..'z'
    }

    internal fun removeAccents(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(normalized).replaceAll("")
    }

    fun createNotificationChannelWrapper(applicationContext: Context, channelId: String, name: String, description: String) {
        if (LUtils.hasO()) createNotificationChannel(applicationContext, channelId, name, description)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(applicationContext: Context, channelId: String, name: String, description: String) {
        val mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mChannel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW)
        // Configure the notification channel.
        mChannel.description = description
        mChannel.setShowBadge(false)
        mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        mNotificationManager.createNotificationChannel(mChannel)
    }

    fun <T> helperSetString(t: T): StringHolder = when (t) {
        is String -> StringHolder(t)
        is Int -> StringHolder(t)
        else -> throw IllegalArgumentException()
    }

    fun <T> helperSetColor(t: T): ColorHolder = when (t) {
        is String -> ColorHolder.fromColor(Color.parseColor(t))
        is @ColorInt Int -> ColorHolder.fromColor(t)
        else -> throw IllegalArgumentException()
    }

}
