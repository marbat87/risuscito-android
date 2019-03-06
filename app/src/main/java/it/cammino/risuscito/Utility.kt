package it.cammino.risuscito

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

object Utility {

    // Costanti per le impostazioni
    const val SCREEN_ON = "sempre_acceso"
    const val SYSTEM_LANGUAGE = "lingua_sistema"
    const val DB_RESET = "db_reset"
    const val CHANGE_LANGUAGE = "changed"
    const val CLICK_DELAY: Long = 500
    internal const val SHOW_SECONDA = "mostra_seconda_lettura"
    internal const val SHOW_PACE = "mostra_canto_pace"
    internal const val SAVE_LOCATION = "memoria_salvataggio_scelta"
    internal const val DEFAULT_INDEX = "indice_predefinito"
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

    internal fun isOnline(activity: Activity): Boolean {
        val cm = ContextCompat.getSystemService(activity as Context, ConnectivityManager::class.java) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

    /* Filtra il link di input per tenere solo il nome del file */
    internal fun filterMediaLinkNew(link: String): String {
        return if (link.isEmpty())
            link
        else {
            return when {
                link.indexOf(".com") > 0 -> {
                    val start = link.indexOf(".com/")
                    link.substring(start + 5).replace("%20".toRegex(), "_")
                }
                link.indexOf("ITALIANO/") > 0 -> {
                    val start = link.indexOf("ITALIANO/")
                    link.substring(start + 9).replace("%20".toRegex(), "_")
                }
                else -> link
            }
        }
    }

    /* Filtra il link di input per tenere solo il nome del file */
    internal fun filterMediaLink(link: String): String {
        return if (link.isEmpty())
            link
        else {
            when {
                link.indexOf(".com") > 0 -> {
                    val start = link.indexOf(".com/")
                    link.substring(start + 5)
                }
                link.indexOf("ITALIANO/") > 0 -> {
                    val start = link.indexOf("ITALIANO/")
                    link.substring(start + 9)
                }
                else -> link
            }
        }
    }

    fun retrieveMediaFileLink(activity: Context, link: String, cercaEsterno: Boolean): String {

        if (link.isEmpty()) return ""

        if (isExternalStorageReadable && cercaEsterno) {
            Log.v("Utility.java", "retrieveMediaFileLink: " + filterMediaLinkNew(link))
            // cerca file esterno con nuovi path e nome
            var fileExt = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "/Risuscitò/" + filterMediaLinkNew(link))
            if (fileExt.exists()) {
                Log.d("Utility.java", "FILE esterno: " + fileExt.absolutePath)
                return fileExt.absolutePath
            } else {
                // cerca file esterno con vecchi path e nome
                val fileArray = ContextCompat.getExternalFilesDirs(activity, null)
                fileExt = File(fileArray[0], filterMediaLink(link))
                if (fileExt.exists()) {
                    Log.d("Utility.java", "FILE esterno: " + fileExt.absolutePath)
                    return fileExt.absolutePath
                } else
                    Log.v("Utility.java", "FILE ESTERNO NON TROVATO")
            }
        } else {
            Log.v("Utility.java", "isExternalStorageReadable: FALSE")
        }

        val fileInt = File(activity.filesDir, filterMediaLink(link))
        if (fileInt.exists()) {
            Log.d("Utility.java", "FILE interno: " + fileInt.absolutePath)
            return fileInt.absolutePath
        } else
            Log.v("Utility.java", "FILE INTERNO NON TROVATO")
        //		Log.i("FILE INTERNO:", "NON TROVATO");
        return ""
    }

    @SuppressLint("NewApi")
    fun setupTransparentTints(context: Activity, color: Int, hasNavDrawer: Boolean) {

        if (!hasNavDrawer && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            context.window.statusBarColor = color
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
}
