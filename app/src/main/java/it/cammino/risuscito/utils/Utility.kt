package it.cammino.risuscito.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.util.Utils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import java.io.File
import java.text.Normalizer
import java.util.Random
import java.util.regex.Pattern

object Utility {

    // Costanti per le impostazioni
    private val TAG = Utility::class.java.canonicalName
//    const val VECCHIO_INDICE = "vecchio_indice"
    const val VECCHIO_INDICE = "vecchio_indice_new"
    const val SCREEN_ON = "sempre_acceso"
    const val SYSTEM_LANGUAGE = "lingua_sistema_new_new"
    const val CHANGE_LANGUAGE = "changed_language"
    const val OLD_LANGUAGE = "old_language"
    const val NEW_LANGUAGE = "new_language"
    const val CLICK_DELAY: Long = 2000
    internal const val SHOW_SECONDA = "mostra_seconda_lettura"
    internal const val SHOW_PACE = "mostra_canto_pace"
    internal const val SAVE_LOCATION = "memoria_salvataggio_scelta"
    internal const val DEFAULT_INDEX = "indice_predefinito_new"
    internal const val DEFAULT_SEARCH = "ricerca_predefinita"
    internal const val SHOW_SANTO = "mostra_santo"
    internal const val SHOW_AUDIO = "mostra_audio"
    internal const val SIGNED_IN = "signed_id"
    internal const val SIGN_IN_REQUESTED = "sign_id_requested"
    internal const val SHOW_OFFERTORIO = "mostra_canto_offertorio"
    internal const val SHOW_EUCARESTIA_PACE = "mostra_eucarestia_pace"
    internal const val PREFERITI_OPEN = "preferiti_open"
    internal const val HISTORY_OPEN = "history_open"
    internal const val INTRO_CONSEGNATI = "intro_consegnati_test"
    internal const val INTRO_CONSEGNATI_2 = "intro_consegnati_2_test"
    internal const val INTRO_PAGINARENDER = "intro_paginarender_test"
    internal const val INTRO_CREALISTA = "intro_crealista_test"
    internal const val INTRO_CREALISTA_2 = "intro_crealista_2_test"
    internal const val INTRO_CUSTOMLISTS = "intro_customlists_test_2"
    internal const val NIGHT_MODE = "night_mode"
    internal const val DYNAMIC_COLORS = "dynamic_colors"
    internal const val OLD_PAGE_SUFFIX = "_old"
    internal const val SHARED_AXIS = "shared_axis"
//    private const val TOKEN_VALIDATION_PATH = "https://oauth2.googleapis.com/tokeninfo?id_token="

    //    internal const val PRIMARY_COLOR = "new_primary_color"
//    internal const val SECONDARY_COLOR = "new_accent_color"
    internal const val ULTIMA_APP_USATA = "ULTIMA_APP_USATA"
    internal const val CLICK_DELAY_SELECTION: Long = 300

    // Costanti per il passaggio dati alla pagina di visualizzazione canto in fullscreen
    internal const val HTML_CONTENT = "htmlContent"
    internal const val SPEED_VALUE = "speedValue"
    internal const val SCROLL_PLAYING = "scrollPlaying"
    internal const val TIPO_LISTA = "tipoLista"


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

    /* Filtra il link di input per tenere solo il nome del file */
    private fun filterMediaLinkNew(link: String?): String {
        link?.let {
            return if (it.isEmpty())
                it
            else {
                it.substring(it.lastIndexOf("/") + 1).replace("%20".toRegex(), "_")
            }
        } ?: return StringUtils.EMPTY
    }

    /* Filtra il link di input per tenere solo il nome del file */
    internal fun filterMediaLink(link: String?): String {
        link?.let {
            return if (it.isEmpty())
                it
            else {
                it.substring(it.lastIndexOf("/") + 1).replace("%20".toRegex(), "_")
            }
        } ?: return StringUtils.EMPTY
    }

    fun retrieveMediaFileLink(activity: Context, link: String?, cercaEsterno: Boolean): String {

        if (link.isNullOrEmpty()) return StringUtils.EMPTY

        return if (OSUtils.hasQ())
            retrieveMediaFileLinkQ(activity, link, cercaEsterno)
        else
            retrieveMediaFileLinkLegacy(activity, link, cercaEsterno)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    internal fun getExternalMediaIdByName(context: Context, link: String): Long {
        val projection = arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media._ID)
        val collection = MediaStore.Audio.Media
            .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        context.contentResolver.query(
            collection,
            projection,
            "${MediaStore.Audio.Media.DISPLAY_NAME} = ?",
            arrayOf(getExternalLink(link)),
            null
        ).use { cursor ->
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

    private fun retrieveMediaFileLinkLegacy(
        activity: Context,
        link: String,
        cercaEsterno: Boolean
    ): String {

        if (isExternalStorageReadable && cercaEsterno) {
//            Log.d(TAG, "retrieveMediaFileLinkLegacy: " + filterMediaLinkNew(link))
            // cerca file esterno con nuovi path e nome
            var fileExt = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "/Risuscitò/" + filterMediaLinkNew(link)
            )
            if (fileExt.exists()) {
//                Log.d(TAG, "retrieveMediaFileLinkLegacy FILE esterno1: " + fileExt.absolutePath)
                return fileExt.absolutePath
            } else {
                // cerca file esterno con vecchi path e nome
                val fileArray = activity.getExternalFilesDirs(null)
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
        return StringUtils.EMPTY
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
        return pattern.matcher(normalized).replaceAll(StringUtils.EMPTY)
    }

    fun createNotificationChannelWrapper(
        applicationContext: Context,
        channelId: String,
        name: String,
        description: String
    ) {
        if (OSUtils.hasO()) createNotificationChannel(
            applicationContext,
            channelId,
            name,
            description
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        applicationContext: Context,
        channelId: String,
        name: String,
        description: String
    ) {
        val mNotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

    fun helperSetColor(t: String?): Int =
        if (t.isNullOrEmpty())
            Color.WHITE
        else
            t.toColorInt()

    fun getExternalLink(link: String): String {
        return if (OSUtils.hasQ())
            getExternalLinkQ(link)
        else
            getExternalLinkLegacy(link)
    }

    private fun getExternalLinkQ(link: String): String {
        return filterMediaLinkNew(link)
    }

    private fun getExternalLinkLegacy(link: String): String {
        if (File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "Risuscitò"
            )
                .mkdirs()
        )
            Log.d(TAG, "CARTELLA RISUSCITO CREATA")
        else
            Log.d(TAG, "CARTELLA RISUSCITO ESISTENTE")
        return (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            .absolutePath
                + "/Risuscitò/"
                + filterMediaLinkNew(link))
    }

    fun mediaScan(context: Context, link: String) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    .absolutePath
                        + "/Risuscitò/"
                        + filterMediaLinkNew(link)
            ), null, null
        )
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
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

    private val STROKE_LETTERS: Map<String, String> = mapOf(
        Pair("Ł", "L"),
        Pair("ł", "l")
    )

    fun getResId(resName: String?, c: Class<*>): Int {
        resName?.let {
            return try {
                val idField = c.getDeclaredField(it)
                idField.getInt(idField)
            } catch (e: Exception) {
                Log.e(TAG, "getResId: $resName" + e.localizedMessage, e)
                -1
            }
        }
        Log.e(TAG, "resName NULL")
        return -1
    }

    fun validateToken(
        idToken: String,
        clientId: String
    ): String {
        Log.d(TAG, "IDTOKEN: $idToken")
        if (idToken.isEmpty())
            return StringUtils.EMPTY

        try {
            val verifier = GoogleIdTokenVerifier.Builder(
                Utils.getDefaultTransport(),
                Utils.getDefaultJsonFactory()
            ) // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(listOf(clientId))
                .build()
            val googleIdToken: GoogleIdToken = verifier.verify(idToken)
            Log.d(TAG, "IDTOKEN SUBJECT: ${googleIdToken.payload.subject}")
            return googleIdToken.payload.subject
        }
        catch (e: Exception) {
            Log.e(TAG, "validateToken exception", e)
            return StringUtils.EMPTY
        }

//        val client = OkHttpClient()
//        val request = Request.Builder()
//            .url(TOKEN_VALIDATION_PATH + idToken)
//            .build()
//
//        try {
//            val response = client.newCall(request).execute()
//            Log.d(TAG, "validateToken statusCode: ${response.code}")
//            val res = response.body?.string()
//            Log.d(TAG, "validateToken response: $res")
//            if (response.code == 200 && res?.isNotEmpty() == true) {
//                val tokenInfo: TokenInfo = GsonBuilder().create().fromJson(
//                    res, object : TypeToken<TokenInfo>() {}.type
//                )
//                Log.d(TAG, "validateToken response sub: ${tokenInfo.sub}")
//                return tokenInfo.sub
//            } else {
//                return StringUtils.EMPTY
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "validateToken exception", e)
//            return StringUtils.EMPTY
//        }

    }

    fun fixSystemBarPadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(
            view
        ) { v, insets ->
            val innerPadding = insets.getInsets(
                // Notice we're using systemBars, not statusBar
                WindowInsetsCompat.Type.systemBars()
                        // Notice we're also accounting for the display cutouts
                        or WindowInsetsCompat.Type.displayCutout()
                // If using EditText, also add
                // "or WindowInsetsCompat.Type.ime()"
                // to maintain focus when opening the IME
            )
            v.setPadding(
                innerPadding.left,
                0,
                innerPadding.right,
                innerPadding.bottom)
            insets
        }
    }

}
