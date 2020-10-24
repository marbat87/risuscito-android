package it.cammino.risuscito

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.materialdrawer.holder.ColorHolder
import it.cammino.risuscito.LUtils.Companion.hasQ
import it.cammino.risuscito.utils.ThemeUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

object Utility {

    // Costanti per le impostazioni
    private val TAG = Utility::class.java.canonicalName
    const val SCREEN_ON = "sempre_acceso"
    const val SYSTEM_LANGUAGE = "lingua_sistema_new"
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
    internal const val SIGN_IN_REQUESTED = "sign_id_requested"
    internal const val SHOW_OFFERTORIO = "mostra_canto_offertorio"
    internal const val PREFERITI_OPEN = "preferiti_open"
    internal const val HISTORY_OPEN = "history_open"
    internal const val INTRO_CONSEGNATI = "intro_consegnati_test"
    internal const val INTRO_CONSEGNATI_2 = "intro_consegnati_2_test"
    internal const val INTRO_PAGINARENDER = "intro_paginarender_test"
    internal const val INTRO_CREALISTA = "intro_crealista_test"
    internal const val INTRO_CREALISTA_2 = "intro_crealista_2_test"
    internal const val INTRO_CUSTOMLISTS = "intro_customlists_test_2"
    internal const val NIGHT_MODE = "night_mode"
    internal const val PRIMARY_COLOR = "new_primary_color"
    internal const val SECONDARY_COLOR = "new_accent_color"
    internal const val ULTIMA_APP_USATA = "ULTIMA_APP_USATA"
    internal const val CLICK_DELAY_SELECTION: Long = 300

    // Costanti per il passaggio dati alla pagina di visualizzazione canto in fullscreen
    internal const val HTML_CONTENT = "htmlContent"
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
    internal val isExternalStorageReadable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

    internal fun isDefaultLocationPublic(context: Context): Boolean {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(SAVE_LOCATION, "0")
                ?: "0") == 1
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
    private fun filterMediaLinkNew(link: String?): String {
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

        return if (hasQ())
            retrieveMediaFileLinkQ(activity, link, cercaEsterno)
        else
            retrieveMediaFileLinkLegacy(activity, link, cercaEsterno)
    }

    @TargetApi(Build.VERSION_CODES.Q)
    fun retrieveMediaFileLinkQ(activity: Context, link: String, cercaEsterno: Boolean): String {

        if (isExternalStorageReadable && cercaEsterno) {
//            Log.d(TAG, "retrieveMediaFileLinkQ: " + getExternalLink(link))
            val externalId = getExternalMediaIdByName(activity, link)
            if (externalId >= 0) {
//                Log.d(TAG, "retrieveMediaFileLinkQ: FILE ESTERNO TROVATO")
                return getExternalLink(link)
            }
//            else
//                Log.d(TAG, "retrieveMediaFileLinkQ: FILE ESTERNO NON TROVATO")
        }
//        else
//            Log.d(TAG, "retrieveMediaFileLinkQ isExternalStorageReadable: FALSE")

        return retrieveInternalLink(activity, link)
    }

    @TargetApi(Build.VERSION_CODES.Q)
    internal fun getExternalMediaIdByName(context: Context, link: String): Long {
        val projection = arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media._ID)
        val resolver = context.contentResolver
        val collection = MediaStore.Audio.Media
                .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        resolver.query(collection, projection, "${MediaStore.Audio.Media.DISPLAY_NAME} = ?", arrayOf(getExternalLink(link)), null).use { cursor ->
            cursor?.let {
                if (it.moveToFirst()) {
                    val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                    Log.d(TAG, "retrieveMediaFileLinkQ DISPLAY_NAME: ${it.getString(nameColumn)}")
                    val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    Log.d(TAG, "retrieveMediaFileLinkQ _ID: ${it.getInt(idColumn)}")
                    return it.getLong(idColumn)
                }
            }
        }
        return -1
    }

    @Suppress("DEPRECATION")
    fun retrieveMediaFileLinkLegacy(activity: Context, link: String, cercaEsterno: Boolean): String {

        if (isExternalStorageReadable && cercaEsterno) {
//            Log.d(TAG, "retrieveMediaFileLinkLegacy: " + filterMediaLinkNew(link))
            // cerca file esterno con nuovi path e nome
            var fileExt = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "/Risuscitò/" + filterMediaLinkNew(link))
            if (fileExt.exists()) {
//                Log.d(TAG, "retrieveMediaFileLinkLegacy FILE esterno1: " + fileExt.absolutePath)
                return fileExt.absolutePath
            } else {
                // cerca file esterno con vecchi path e nome
                val fileArray = ContextCompat.getExternalFilesDirs(activity, null)
                fileExt = File(fileArray[0], filterMediaLink(link))
                if (fileExt.exists()) {
//                    Log.d(TAG, "retrieveMediaFileLinkLegacy FILE esterno2: " + fileExt.absolutePath)
                    return fileExt.absolutePath
                }
//                else
//                    Log.d(TAG, "retrieveMediaFileLinkLegacy FILE ESTERNO NON TROVATO")
            }
        }
//        else
//            Log.d(TAG, "retrieveMediaFileLinkLegacy isExternalStorageReadable: FALSE")

        return retrieveInternalLink(activity, link)
    }

    private fun retrieveInternalLink(activity: Context, link: String?): String {
        val fileInt = File(activity.filesDir, filterMediaLink(link))
        if (fileInt.exists()) {
//            Log.d(TAG, "FILE interno: " + fileInt.absolutePath)
            return fileInt.absolutePath
        }
//        else
//            Log.v(TAG, "FILE INTERNO NON TROVATO")
        //		Log.i("FILE INTERNO:", "NON TROVATO");
        return ""
    }

    @SuppressLint("NewApi")
    fun setupTransparentTints(context: Activity, color: Int, hasNavDrawer: Boolean, isOnTablet: Boolean) {
        if ((!hasNavDrawer || isOnTablet) && LUtils.hasL())
            context.window.statusBarColor = color
    }

    @SuppressLint("NewApi")
    fun setupNavBarColor(context: Activity) {
        context.window.decorView.setBackgroundColor(ContextCompat.getColor(context, if (ThemeUtils.isDarkMode(context)) R.color.design_dark_default_color_background else R.color.design_default_color_background))
        if (LUtils.hasO()) {
//            context.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            context.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (!ThemeUtils.isDarkMode(context)) setLightNavigationBar(context)
//            context.window.decorView.setBackgroundColor(ContextCompat.getColor(context, if (ThemeUtils.isDarkMode(context)) R.color.design_dark_default_color_background else R.color.design_default_color_background))
            context.window.navigationBarColor = Color.TRANSPARENT
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setLightNavigationBar(context: Activity) {
        if (LUtils.hasR())
            setLightNavigationBarR(context)
        else
            setLightNavigationBarLegacy(context)
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.O)
    fun setLightNavigationBarLegacy(context: Activity) {
        context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun setLightNavigationBarR(context: Activity) {
        context.window.insetsController?.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
    }

    internal fun random(start: Int, end: Int): Int {
        return Random().nextInt(end - start + 1) + start
    }

    internal fun isLowerCase(ch: Char): Boolean {
        return ch in 'a'..'z'
    }

    internal fun removeAccents(value: String): String {

        var normalized = value

        for ((mapKey, mapValue) in STROKE_LETTERS)
            normalized = normalized.replace(mapKey, mapValue)

        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
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

    fun getExternalLink(link: String): String {
        return if (hasQ())
            getExternalLinkQ(link)
        else
            getExternalLinkLegacy(link)
    }

    private fun getExternalLinkQ(link: String): String {
        return filterMediaLinkNew(link)
    }

    @Suppress("DEPRECATION")
    private fun getExternalLinkLegacy(link: String): String {
        if (File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                        "Risuscitò")
                        .mkdirs())
            Log.d(TAG, "CARTELLA RISUSCITO CREATA")
        else
            Log.d(TAG, "CARTELLA RISUSCITO ESISTENTE")
        return (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                .absolutePath
                + "/Risuscitò/"
                + filterMediaLinkNew(link))
    }

    @Suppress("DEPRECATION")
    fun mediaScan(context: Context, link: String) {
        MediaScannerConnection.scanFile(
                context,
                arrayOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                        .absolutePath
                        + "/Risuscitò/"
                        + filterMediaLinkNew(link)), null, null)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    internal fun decodeSampledBitmapFromResource(
            res: Resources,
            resId: Int,
            reqWidth: Int,
            reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeResource(res, resId, this)
        }
    }

    internal fun readTextFromResource(ctx: Context, resourceID: String): String {
        val inputStream = ctx.resources.openRawResource(LUtils.getResId(resourceID, R.raw::class.java))
        val br = BufferedReader(InputStreamReader(inputStream, ECONDING_UTF8))
        var line: String? = br.readLine()
        val cantoTrasportato = StringBuffer()

        while (line != null) {
//            Log.d(TAG, "line: $line")
            cantoTrasportato.append(line)
            cantoTrasportato.append("\n")
            line = br.readLine()
        }
        br.close()
        Log.d(TAG, "readTextFromResource cantoTrasportato: $cantoTrasportato")
        return cantoTrasportato.toString()
    }

    private const val ECONDING_UTF8 = "utf-8"

    private val STROKE_LETTERS: Map<String, String> = mapOf(
            Pair("Ł", "L"),
            Pair("ł", "l"))

}
