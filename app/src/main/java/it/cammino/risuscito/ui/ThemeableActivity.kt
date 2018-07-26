package it.cammino.risuscito.ui

import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.view.LayoutInflaterCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import com.google.android.gms.tasks.Tasks
import com.mikepenz.iconics.context.IconicsLayoutInflater2
import it.cammino.risuscito.DatabaseCanti
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.utils.ThemeUtils
import java.io.*
import java.util.*
import java.util.concurrent.ExecutionException

abstract class ThemeableActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    protected var hasNavDrawer = false
    var themeUtils: ThemeUtils? = null
        private set

    private val newDbPath: File?
        get() {
            Log.d(javaClass.name, "dbpath:" + getDatabasePath(RisuscitoDatabase.dbName))
            return getDatabasePath(RisuscitoDatabase.dbName)
        }

    private val oldDbPath: File
        get() {
            Log.d(javaClass.name, "dbpath:" + getDatabasePath(DatabaseCanti.getDbName()))
            return getDatabasePath(DatabaseCanti.getDbName())
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        Log.d(TAG, "onSharedPreferenceChanged: $s")
        if (s.equals("new_primary_color", ignoreCase = true)) {
            Log.d(TAG, "onSharedPreferenceChanged: new_primary_color" + sharedPreferences.getInt(s, 0))
            recreate()
        }
        if (s.equals("new_accent_color", ignoreCase = true)) {
            Log.d(TAG, "onSharedPreferenceChanged: new_accent_color" + sharedPreferences.getInt(s, 0))
            recreate()
        }
        if (s == Utility.SYSTEM_LANGUAGE) {
            Log.d(
                    TAG,
                    "onSharedPreferenceChanged: cur lang " + ThemeableActivity.getSystemLocalWrapper(resources.configuration)
                            .language)
            Log.d(TAG, "onSharedPreferenceChanged: cur set " + sharedPreferences.getString(s, "")!!)
            if (!ThemeableActivity.getSystemLocalWrapper(resources.configuration)
                            .language
                            .equals(sharedPreferences.getString(s, "it")!!, ignoreCase = true)) {
                val i = baseContext
                        .packageManager
                        .getLaunchIntentForPackage(baseContext.packageName)
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i.putExtra(Utility.DB_RESET, true)
                    val currentLang = ThemeableActivity.getSystemLocalWrapper(resources.configuration)
                            .language
                    i.putExtra(
                            Utility.CHANGE_LANGUAGE,
                            currentLang + "-" + sharedPreferences.getString(s, ""))
                }
                startActivity(i)
            }
        }
        if (s == Utility.SCREEN_ON) this@ThemeableActivity.checkScreenAwake()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        if (isMenuWorkaroundRequired) {
            forceOverflowMenu()
        }
        themeUtils = ThemeUtils(this)
        val mLUtils = LUtils.getInstance(this)
        mLUtils.convertIntPreferences()
        setTheme(themeUtils!!.current)
        AppCompatDelegate.setDefaultNightMode(
                if (ThemeUtils.isDarkMode(this))
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO)

        // setta il colore della barra di stato, solo su KITKAT
        Utility.setupTransparentTints(
                this@ThemeableActivity, themeUtils!!.primaryColorDark(), hasNavDrawer)

//        if (LUtils.hasL()) {
//            // Since our app icon has the same color as colorPrimary, our entry in the Recent Apps
//            // list gets weird. We need to change either the icon or the color
//            // of the TaskDescription.
//            val taskDesc = ActivityManager.TaskDescription(null, null, themeUtils!!.primaryColor())
//            setTaskDescription(taskDesc)
//        }
        setTaskDescriptionWrapper(themeUtils!!)

        // Iconic
        LayoutInflaterCompat.setFactory2(
                layoutInflater, IconicsLayoutInflater2(delegate))
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        checkScreenAwake()

        PreferenceManager.getDefaultSharedPreferences(this@ThemeableActivity)
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(this@ThemeableActivity)
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU && isMenuWorkaroundRequired) {
            openOptionsMenu()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return keyCode == KeyEvent.KEYCODE_MENU && isMenuWorkaroundRequired || super.onKeyDown(keyCode, event)
    }

    // controlla se l'app deve mantenere lo schermo acceso
    private fun checkScreenAwake() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val screenOn = pref.getBoolean(Utility.SCREEN_ON, false)
        if (screenOn)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun forceOverflowMenu() {
        try {
            val config = ViewConfiguration.get(this)
            val menuKeyField = ViewConfiguration::class.java.getDeclaredField("sHasPermanentMenuKey")
            if (menuKeyField != null) {
                menuKeyField.isAccessible = true
                menuKeyField.setBoolean(config, false)
            }
        } catch (e: IllegalAccessException) {
            Log.w(javaClass.toString(), "IllegalAccessException - Failed to force overflow menu.", e)
        } catch (e: NoSuchFieldException) {
            Log.w(javaClass.toString(), "NoSuchFieldException - Failed to force overflow menu.", e)
        }

    }

    override fun attachBaseContext(newBase: Context) {
        var mNewBase = newBase

        val config = Configuration()

        // lingua
        val sp = PreferenceManager.getDefaultSharedPreferences(mNewBase)
        val language = sp.getString(Utility.SYSTEM_LANGUAGE, "")
        Log.d(TAG, "attachBaseContext - language: " + language!!)
        // ho settato almeno una volta la lingua --> imposto quella
        if (language.isNotEmpty() && language != "en") {
            val locale = Locale(language)
            Locale.setDefault(locale)
            ThemeableActivity.setSystemLocalWrapper(config, locale)
        } else {
            val mEditor = sp.edit()
            val mLanguage = when (getSystemLocalWrapper(mNewBase.resources.configuration).language) {
                "uk" -> "uk"
//                "en" -> "en"
                else -> "it"
            }
            Log.d(TAG, "attachBaseContext - language setted: $mLanguage")
            mEditor.putString(Utility.SYSTEM_LANGUAGE, mLanguage)
            mEditor.apply()
            val locale = Locale(mLanguage)
            Locale.setDefault(locale)
            ThemeableActivity.setSystemLocalWrapper(config, locale)
        }// non è ancora stata impostata nessuna lingua nelle impostazioni --> setto una lingua
        // selezionabile oppure IT se non presente

        // fond dimension
        try {
            val actualScale = mNewBase.resources.configuration.fontScale
            Log.d(TAG, "actualScale: $actualScale")
            val systemScale = Settings.System.getFloat(contentResolver, Settings.System.FONT_SCALE)
            Log.d(TAG, "systemScale: $systemScale")
            if (actualScale != systemScale) {
                config.fontScale = systemScale
            }
        } catch (e: Settings.SettingNotFoundException) {
            Log.e(
                    TAG,
                    "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: " + e.localizedMessage)
        } catch (e: NullPointerException) {
            Log.e(
                    TAG,
                    "NullPointerException - FUNZIONE RESIZE TESTO NON SUPPORTATA: " + e.localizedMessage)
        }

        if (LUtils.hasJB()) {
            mNewBase = mNewBase.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            mNewBase.resources.updateConfiguration(config, mNewBase.resources.displayMetrics)
        }

        // Calligraphy
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(mNewBase))
//        super.attachBaseContext(ViewPumpContextWrapper.wrap(mNewBase))
        super.attachBaseContext(mNewBase)
    }

    private fun saveSharedPreferencesToFile(out: OutputStream): Boolean {
        var res = false
        var output: ObjectOutputStream? = null
        try {
            output = ObjectOutputStream(out)
            val pref = PreferenceManager.getDefaultSharedPreferences(this@ThemeableActivity)
            output.writeObject(pref.all)

        } catch (e: IOException) {
            val error = "saveSharedPreferencesToFile - IOException: " + e.localizedMessage
            Log.e(javaClass.name, error, e)
            Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show()
        } finally {
            try {
                if (output != null) {
                    output.flush()
                    output.close()
                    res = true
                }
            } catch (e: IOException) {
                val error = "saveSharedPreferencesToFile - IOException: " + e.localizedMessage
                Log.e(javaClass.name, error, e)
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show()
            }

        }
        return res
    }

    private fun loadSharedPreferencesFromFile(`in`: InputStream) {
        //        boolean res = false;
        var input: ObjectInputStream? = null
        try {
            input = ObjectInputStream(`in`)
            val prefEdit = PreferenceManager.getDefaultSharedPreferences(this@ThemeableActivity).edit()
            prefEdit.clear()
            val entries = input.readObject() as Map<*, *>
            for ((key, v) in entries) {

                when (v) {
                    is Boolean -> prefEdit.putBoolean(key as String, v)
                    is Float -> prefEdit.putFloat(key as String, v)
                    is Int -> prefEdit.putInt(key as String, v)
                    is Long -> prefEdit.putLong(key as String, v)
                    is String -> prefEdit.putString(key as String, v)
                }
            }
            prefEdit.apply()
        } catch (e: ClassNotFoundException) {
            val error = "loadSharedPreferencesFromFile - ClassNotFoundException: " + e.localizedMessage
            Log.e(javaClass.name, error, e)
            Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show()
        } catch (e: IOException) {
            val error = "loadSharedPreferencesFromFile - IOException: " + e.localizedMessage
            Log.e(javaClass.name, error, e)
            Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show()
        } finally {
            try {
                input?.close()
            } catch (e: IOException) {
                val error = "loadSharedPreferencesFromFile - IOException: " + e.localizedMessage
                Log.e(javaClass.name, error, e)
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    /**
     * **************************************************************** controlla se il file è già
     * esistente; se esiste lo cancella e poi lo ricrea
     *
     * @param titl file name
     * @param mime file mime type (application/x-sqlite3)
     */
    @Throws(IOException::class, ExecutionException::class, InterruptedException::class, NoPermissioneException::class)
    fun checkDuplTosave(titl: String?, mime: String, dataBase: Boolean) {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        // Synchronously check for necessary permissions
        checkDrivePermissions(account)

        val client = Drive.getDriveResourceClient(this, account!!)
        // task di recupero la cartella applicativa
        val folder = Tasks.await(client.appFolder)

        val file = newDbPath
        if (folder != null && titl != null && (!dataBase || file != null)) {
            // create content from file
            Log.d(javaClass.name, "saveCheckDupl - dataBase? $dataBase")
            Log.d(javaClass.name, "saveCheckDupl - title: $titl")
            val query = Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, titl)).build()

            // task di recupero metadata del file se già presente
            val metadataBuffer = Tasks.await(client.query(query))

            val count = metadataBuffer.count
            Log.d(javaClass.name, "saveCheckDupl - Count files old: $count")
            if (count > 0) {
                val mDriveId = metadataBuffer.get(count - 1).driveId
                Log.d(javaClass.name, "saveCheckDupl - driveIdRetrieved: $mDriveId")
                Log.d(
                        javaClass.name,
                        "saveCheckDupl - filesize in cloud " + metadataBuffer.get(0).fileSize)
                metadataBuffer.release()

                val mFile = mDriveId.asDriveFile()
                // task di cancellazione file eventualmente già presente
                Tasks.await(client.delete(mFile))
                Log.d(javaClass.name, "saveCheckDupl - deleted")

                saveToDrive(folder, titl, mime, file, dataBase)
            } else {
                metadataBuffer.release()
                saveToDrive(folder, titl, mime, file, dataBase)
            }
        }
    }

    /**
     * **************************************************************** create file in GOODrive
     *
     * @param titl file name
     * @param mime file mime type (application/x-sqlite3)
     * @param file file (with content) to create
     */
    @Throws(ExecutionException::class, InterruptedException::class, IOException::class, NoPermissioneException::class)
    private fun saveToDrive(
            driveFolder: DriveFolder?,
            titl: String?,
            mime: String?,
            file: File?,
            dataBase: Boolean) {
        Log.d(javaClass.name, "saveToDrive - title: $titl / database? $dataBase")

        val account = GoogleSignIn.getLastSignedInAccount(this)
        // Synchronously check for necessary permissions
        checkDrivePermissions(account)

        val client = Drive.getDriveResourceClient(this, account!!)
        // task di recupero la cartella applicativa
        if (driveFolder != null && titl != null && mime != null && (!dataBase || file != null)) {
            // task di creazione content da file
            val driveContents = Tasks.await(client.createContents())
            if (dataBase) {
                driveContents.outputStream?.use { mOoS ->
                    val `is` = FileInputStream(file!!)
                    val buf = ByteArray(4096)
                    var c = `is`.read(buf, 0, buf.size)
                    while (c > 0) {
                        mOoS.write(buf, 0, c)
                        mOoS.flush()
                        c = `is`.read(buf, 0, buf.size)
                    }
                }
            } else {
                if (!saveSharedPreferencesToFile(driveContents.outputStream)) {
                    return
                }
            }

            // content's COOL, create metadata
            val meta = MetadataChangeSet.Builder().setTitle(titl).setMimeType(mime).build()

            // task di creazione file in Google Drive
            val driveFile = Tasks.await(client.createFile(driveFolder, meta, driveContents))
            if (driveFile != null) {
                val metadata = Tasks.await(client.getMetadata(driveFile))
                val mDriveId = metadata.driveId
                Log.d(javaClass.name, "driveIdSaved: $mDriveId")
                val error = "saveToDrive - FILE CARICATO"
                Log.d(javaClass.name, error)
            }
        }
    }

    /**
     * **************************************************************** controlla se il file è già
     * esistente; se esiste lo cancella e poi lo ricrea
     *
     * @param titl file name
     */
    @Throws(IOException::class, ExecutionException::class, InterruptedException::class, NoPermissioneException::class)
    fun checkDupl(titl: String?): Boolean {
        var fileFound = false
        val account = GoogleSignIn.getLastSignedInAccount(this)
        // Synchronously check for necessary permissions
        checkDrivePermissions(account)

        val client = Drive.getDriveResourceClient(this, account!!)
        // task di recupero la cartella applicativa
        val folder = Tasks.await(client.appFolder)
        if (folder != null && titl != null) {
            // create content from file
            Log.d(javaClass.name, "checkDupl - title: $titl")
            val query = Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, titl)).build()

            // task di recupero metadata del file se già presente
            val metadataBuffer = Tasks.await(client.query(query))

            val count = metadataBuffer.count
            metadataBuffer.release()
            Log.d(javaClass.name, "checkDupl - Count files old: $count")
            fileFound = count > 0
        }
        return fileFound
    }

    @Throws(ExecutionException::class, InterruptedException::class, NoPermissioneException::class, IOException::class, NoBackupException::class)
    fun restoreNewDbBackup() {
        Log.d(
                javaClass.name, "restoreNewDriveBackup - Db name: " + RisuscitoDatabase.dbName)
        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, RisuscitoDatabase.dbName))
                .build()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        // Synchronously check for necessary permissions
        checkDrivePermissions(account)

        val client = Drive.getDriveResourceClient(this, account!!)

        val metadataBuffer = Tasks.await(client.query(query))

        val count = metadataBuffer.count
        Log.d(javaClass.name, "restoreNewDriveBackup - Count files backup: $count")
        if (count > 0) {
            val mDriveId = metadataBuffer.get(count - 1).driveId
            Log.d(javaClass.name, "restoreNewDriveBackup - driveIdRetrieved: $mDriveId")
            Log.d(
                    javaClass.name,
                    "restoreNewDriveBackup - filesize in cloud " + metadataBuffer.get(0).fileSize)
            metadataBuffer.release()

            RisuscitoDatabase.getInstance(this).close()

            val mFile = mDriveId.asDriveFile()

            val driveContents = Tasks.await(client.openFile(mFile, DriveFile.MODE_READ_ONLY))

            var dbFile = newDbPath
            val path = dbFile!!.path

            if (!dbFile.exists())
                dbFile.delete()

            dbFile = File(path)
            try {
                val fos = FileOutputStream(dbFile)
                val mOutput = BufferedOutputStream(fos)
//                val mInput = BufferedInputStream(driveContents.inputStream)
                val parcelFileDescriptor = driveContents.parcelFileDescriptor
                val mInput = FileInputStream(parcelFileDescriptor.fileDescriptor)

                val buffer = ByteArray(1024)
                var length = mInput.read(buffer)
                while (length > 0) {
                    mOutput.write(buffer, 0, length)
//                    mOutput.flush()
                    length = mInput.read(buffer)
                }
                mOutput.flush()
                mOutput.close()
                mInput.close()
            } catch (e: FileNotFoundException) {
                client.discardContents(driveContents)
                throw e
            } catch (e: IOException) {
                client.discardContents(driveContents)
                throw e
            }

            RisuscitoDatabase.resetInstance()
            RisuscitoDatabase.getInstance(this).recreateDB()
        } else {
            metadataBuffer.release()
            throw NoBackupException()
        }
    }

    @Throws(NoPermissioneException::class, ExecutionException::class, InterruptedException::class, IOException::class, NoBackupException::class)
    fun restoreOldDriveBackup() {
        Log.d(javaClass.name, "restoreOldDriveBackup - Db name: " + DatabaseCanti.getDbName())
        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, DatabaseCanti.getDbName()))
                .build()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        // Synchronously check for necessary permissions
        checkDrivePermissions(account)

        val client = Drive.getDriveResourceClient(this, account!!)

        // esevuzione task per metadata da query
        val metadataBuffer = Tasks.await(client.query(query))

        val count = metadataBuffer.count
        Log.d(javaClass.name, "restoreOldDriveBackup - Count files backup: $count")
        if (count > 0) {
            val mDriveId = metadataBuffer.get(count - 1).driveId
            Log.d(javaClass.name, "restoreOldDriveBackup - driveIdRetrieved: $mDriveId")
            Log.d(
                    javaClass.name,
                    "restoreOldDriveBackup - filesize in cloud " + metadataBuffer.get(0).fileSize)
            metadataBuffer.release()

            val mFile = mDriveId.asDriveFile()
            // esecuzione task per recupero driveContent
            val driveContents = Tasks.await(client.openFile(mFile, DriveFile.MODE_READ_ONLY))

            var listaCanti = DatabaseCanti(this)
            listaCanti.close()

            var dbFile = oldDbPath
            val path = dbFile.path

            if (!dbFile.exists())

                dbFile.delete()

            dbFile = File(path)
            try {
                val fos = FileOutputStream(dbFile)
                val bos = BufferedOutputStream(fos)
                val `in` = BufferedInputStream(driveContents.inputStream)

                val buffer = ByteArray(1024)
                var n = `in`.read(buffer)
                while (n > 0) {
                    bos.write(buffer, 0, n)
                    bos.flush()
                    n = `in`.read(buffer)
                }
                bos.close()
            } catch (e: FileNotFoundException) {
                client.discardContents(driveContents)
                throw e
            } catch (e: IOException) {
                client.discardContents(driveContents)
                throw e
            }

            listaCanti = DatabaseCanti(this)
            listaCanti.close()
            client.discardContents(driveContents)

            RisuscitoDatabase.getInstance(this).importFromOldDB(this)

            checkDuplTosave(RisuscitoDatabase.dbName, "application/x-sqlite3", true)
        } else {
            metadataBuffer.release()
            throw NoBackupException()
        }
    }

    @Throws(NoPermissioneException::class, ExecutionException::class, InterruptedException::class, NoBackupException::class)
    fun restoreDrivePrefBackup(title: String) {
        Log.d(javaClass.name, "restoreDrivePrefBackup - pref title: $title")
        val query = Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, title)).build()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        // Synchronously check for necessary permissions
        checkDrivePermissions(account)

        val client = Drive.getDriveResourceClient(this, account!!)

        val metadataBuffer = Tasks.await(client.query(query))

        val count = metadataBuffer.count
        Log.d(javaClass.name, "restoreDrivePrefBackup - Count files backup: $count")
        if (count > 0) {
            val mDriveId = metadataBuffer.get(count - 1).driveId
            Log.d(javaClass.name, "restoreDrivePrefBackup - driveIdRetrieved: $mDriveId")
            Log.d(
                    javaClass.name,
                    "restoreDrivePrefBackup - filesize in cloud " + metadataBuffer.get(0).fileSize)
            metadataBuffer.release()

            val mFile = mDriveId.asDriveFile()
            val driveContents = Tasks.await(client.openFile(mFile, DriveFile.MODE_READ_ONLY))

            loadSharedPreferencesFromFile(driveContents.inputStream)
            client.discardContents(driveContents)
        } else {
            throw NoBackupException()
        }
    }

    @Throws(NoPermissioneException::class)
    private fun checkDrivePermissions(account: GoogleSignInAccount?) {
        if (!GoogleSignIn.hasPermissions(account, Drive.SCOPE_FILE) || !GoogleSignIn.hasPermissions(account, Drive.SCOPE_APPFOLDER)) {
            // Note: this launches a sign-in flow, however the code to detect
            // the result of the sign-in flow and retry the API call is not
            // shown here.
            GoogleSignIn.requestPermissions(this, 9002, account, Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
            throw NoPermissioneException()
        }
    }

    inner class NoPermissioneException internal constructor() : Exception("no permission for drive SCOPE_FILE or SCOPE_APPFOLDER")

    inner class NoBackupException internal constructor() : Exception(resources.getString(R.string.no_restore_found))

    private fun setTaskDescriptionWrapper(themeUtils: ThemeUtils) {
        if (LUtils.hasP())
            setTaskDescriptionP(themeUtils)
        else if (LUtils.hasL())
            setTaskDescriptionL(themeUtils)
    }

    @Suppress("DEPRECATION")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setTaskDescriptionL(themeUtils: ThemeUtils) {
        val taskDesc = ActivityManager.TaskDescription(null, null, themeUtils.primaryColor())
        setTaskDescription(taskDesc)
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun setTaskDescriptionP(themeUtils: ThemeUtils) {
        val taskDesc = ActivityManager.TaskDescription(null, R.mipmap.ic_launcher, themeUtils.primaryColor())
        setTaskDescription(taskDesc)
    }

    companion object {
        internal val TAG = ThemeableActivity::class.java.canonicalName
        val isMenuWorkaroundRequired: Boolean
            get() = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT && ("LGE".equals(Build.MANUFACTURER, ignoreCase = true) || "E6710".equals(Build.DEVICE, ignoreCase = true))

        @Suppress("DEPRECATION")
        private fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun getSystemLocale(config: Configuration): Locale {
            return config.locales.get(0)
        }

        fun getSystemLocalWrapper(config: Configuration): Locale {
            return if (LUtils.hasN())
                ThemeableActivity.getSystemLocale(config)
            else
                ThemeableActivity.getSystemLocaleLegacy(config)
        }

        @Suppress("DEPRECATION")
        private fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun setSystemLocale(config: Configuration, locale: Locale) {
            config.setLocale(locale)
        }

        fun setSystemLocalWrapper(config: Configuration, locale: Locale) {
            if (LUtils.hasN())
                ThemeableActivity.setSystemLocale(config, locale)
            else
                ThemeableActivity.setSystemLocaleLegacy(config, locale)
        }
    }
}
