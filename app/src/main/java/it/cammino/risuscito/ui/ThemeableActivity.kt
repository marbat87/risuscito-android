package it.cammino.risuscito.ui

import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.ViewConfiguration
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Tasks
import com.google.android.material.color.MaterialColors
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.dao.Backup
import it.cammino.risuscito.database.entities.*
import it.cammino.risuscito.database.serializer.DateTimeDeserializer
import it.cammino.risuscito.database.serializer.DateTimeSerializer
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.utils.ThemeUtils.Companion.getStatusBarDefaultColor
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import java.io.*
import java.sql.Date
import java.util.*
import java.util.concurrent.ExecutionException

abstract class ThemeableActivity : AppCompatActivity() {

    protected var hasNavDrawer = false

    protected val mViewModel: MainActivityViewModel by viewModels()

    public override fun onCreate(savedInstanceState: Bundle?) {
        if (isMenuWorkaroundRequired)
            forceOverflowMenu()

        Log.d(TAG, "Configuration.UI_MODE_NIGHT_NO: ${Configuration.UI_MODE_NIGHT_NO}")
        Log.d(TAG, "Configuration.UI_MODE_NIGHT_YES: ${Configuration.UI_MODE_NIGHT_YES}")
        Log.d(TAG, "getResources().getConfiguration().uiMode: ${resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK}")

        val themeUtils = ThemeUtils(this)
        Log.d(TAG, "ThemeUtils.isDarkMode(this): ${ThemeUtils.isDarkMode(this)}")
        mViewModel.mLUtils = LUtils.getInstance(this)
        mViewModel.mLUtils.convertIntPreferences()
        setTheme(themeUtils.current)

        mViewModel.isOnTablet = mViewModel.mLUtils.isOnTablet
        Log.d(TAG, "onCreate: isOnTablet = ${mViewModel.isOnTablet}")
        mViewModel.hasThreeColumns = mViewModel.mLUtils.hasThreeColumns
        Log.d(TAG, "onCreate: hasThreeColumns = ${mViewModel.hasThreeColumns}")
        mViewModel.isGridLayout = mViewModel.mLUtils.isGridLayout
        Log.d(TAG, "onCreate: isGridLayout = ${mViewModel.isGridLayout}")
        mViewModel.isLandscape = mViewModel.mLUtils.isLandscape
        Log.d(TAG, "onCreate: isLandscape = ${mViewModel.isLandscape}")
        mViewModel.isTabletWithFixedDrawer = mViewModel.isOnTablet && mViewModel.isLandscape
        Log.d(TAG, "onCreate: hasFixedDrawer = ${mViewModel.isTabletWithFixedDrawer}")
        mViewModel.isTabletWithNoFixedDrawer = mViewModel.isOnTablet && !mViewModel.isLandscape
        Log.d(TAG, "onCreate: hasFixedDrawer = ${mViewModel.isTabletWithNoFixedDrawer}")

        // setta il colore della barra di stato, solo su KITKAT
        Utility.setupTransparentTints(this, getStatusBarDefaultColor(this), hasNavDrawer, mViewModel.isOnTablet)
        Utility.setupNavBarColor(this)

        setTaskDescription()

        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        LUtils.getInstance(this).checkScreenAwake()
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

    private fun forceOverflowMenu() {
        try {
            val config = ViewConfiguration.get(this)
            val menuKeyField = ViewConfiguration::class.java.getDeclaredField("sHasPermanentMenuKey")
            @Suppress("SENSELESS_COMPARISON")
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
        Log.d(TAG, "attachBaseContext")
        super.attachBaseContext(RisuscitoApplication.localeManager.useCustomConfig(newBase))
        RisuscitoApplication.localeManager.useCustomConfig(this)
        SplitCompat.install(this)
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        Log.d(TAG, "applyOverrideConfiguration")
        super.applyOverrideConfiguration(RisuscitoApplication.localeManager.updateConfigurationIfSupported(this, overrideConfiguration))
    }

    class NoBackupException internal constructor(val resources: Resources) : Exception(resources.getString(R.string.no_restore_found))

    class NoIdException internal constructor() : Exception("no ID linked to this Account")

    private fun setTaskDescription() {
        if (LUtils.hasP())
            setTaskDescriptionP()
        else if (LUtils.hasL())
            setTaskDescriptionL()
    }

    fun backupSharedPreferences(userId: String?, userEmail: String?) {
        Log.d(TAG, "backupSharedPreferences $userId")

        if (userId == null)
            throw NoIdException()

        val db = Firebase.firestore

        // Create a query against the collection.
        val query = db.collection(FIREBASE_COLLECTION_IMPOSTAZIONI).whereEqualTo(FIREBASE_FIELD_USER_ID, userId)

        val querySnapshot = Tasks.await(query.get())

        Log.d(TAG, "querySnapshot.documents.size ${querySnapshot.documents.size}")

        val usersPreferences = HashMap<String, Any>()
        usersPreferences[FIREBASE_FIELD_USER_ID] = userId
        usersPreferences[FIREBASE_FIELD_EMAIL] = userEmail ?: ""
        usersPreferences[FIREBASE_FIELD_TIMESTAMP] = Date(System.currentTimeMillis())
        usersPreferences[FIREBASE_FIELD_PREFERENCE] = PreferenceManager.getDefaultSharedPreferences(this).all

        if (querySnapshot.documents.size > 0) {
            Tasks.await(db.collection(FIREBASE_COLLECTION_IMPOSTAZIONI).document(querySnapshot.documents[0].id).delete())
            Log.d(TAG, "existing deleted")
        }

        // Add a new document with a generated ID
        val documentReference = Tasks.await(db.collection(FIREBASE_COLLECTION_IMPOSTAZIONI).add(usersPreferences))
        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id)

    }

    fun restoreSharedPreferences(userId: String?) {
        Log.d(TAG, "backupSharedPreferences $userId")

        if (userId == null)
            throw NoIdException()

        val db = Firebase.firestore

        // Create a query against the collection.
        val query = db.collection(FIREBASE_COLLECTION_IMPOSTAZIONI).whereEqualTo(FIREBASE_FIELD_USER_ID, userId)

        val querySnapshot = Tasks.await(query.get())

        Log.d(TAG, "querySnapshot.documents.size ${querySnapshot.documents.size}")

        if (querySnapshot.documents.size == 0)
            throw NoBackupException(resources)

        val prefEdit = PreferenceManager.getDefaultSharedPreferences(this).edit()
        prefEdit.clear()

        val entries = querySnapshot.documents[0].get(FIREBASE_FIELD_PREFERENCE) as? HashMap<*, *>
        entries?.let {
            for ((key, v) in it) {
                Log.d(TAG, "preference : $key / $v")
                when (v) {
                    is Boolean -> prefEdit.putBoolean(key as? String, v)
                    is Float -> prefEdit.putFloat(key as? String, v)
                    is Int -> prefEdit.putInt(key as? String, v)
                    is Long -> prefEdit.putInt(key as? String, v.toInt())
                    is String -> prefEdit.putString(key as? String, v)
                }
            }
        }
        prefEdit.apply()
    }

    fun backupDatabase(userId: String?) {
        Log.d(TAG, "backupDatabase $userId")

        if (userId == null)
            throw NoIdException()

        val storage = Firebase.storage
        val storageRef = storage.reference
        val risuscitoDb = RisuscitoDatabase.getInstance(this)

        //BACKUP CANTI
        val cantoRef = deleteExistingFile(storageRef, CANTO_FILE_NAME, userId)
        val backupList = risuscitoDb.cantoDao().backup
        Log.d(TAG, "canto backup size ${backupList.size}")
        putFileToFirebase(cantoRef, backupList, CANTO_FILE_NAME)

        //BACKUP CUSTOM LISTS
        val customListRef = deleteExistingFile(storageRef, CUSTOM_LIST_FILE_NAME, userId)
        val customLists = risuscitoDb.customListDao().all
        Log.d(TAG, "custom list size ${customLists.size}")
        putFileToFirebase(customListRef, customLists, CUSTOM_LIST_FILE_NAME)

        //BACKUP LISTE PERS
        val listePersRef = deleteExistingFile(storageRef, LISTA_PERS_FILE_NAME, userId)
        val listePers = risuscitoDb.listePersDao().all
        Log.d(TAG, "listePers size ${listePers.size}")
        putFileToFirebase(listePersRef, listePers, LISTA_PERS_FILE_NAME)

        //BACKUP LOCAL LINK
        val localLinkRef = deleteExistingFile(storageRef, LOCAL_LINK_FILE_NAME, userId)
        val localLink = risuscitoDb.localLinksDao().all
        Log.d(TAG, "localLink size ${localLink.size}")
        putFileToFirebase(localLinkRef, localLink, LOCAL_LINK_FILE_NAME)

        //BACKUP CONSEGNATI
        val consegnatiRef = deleteExistingFile(storageRef, CONSEGNATO_FILE_NAME, userId)
        val consegnati = risuscitoDb.consegnatiDao().all
        Log.d(TAG, "consegnati size ${consegnati.size}")
        putFileToFirebase(consegnatiRef, consegnati, CONSEGNATO_FILE_NAME)

        //BACKUP CRONOLOGIA
        val cronologiaRef = deleteExistingFile(storageRef, CRONOLOGIA_FILE_NAME, userId)
        val cronologia = risuscitoDb.cronologiaDao().all
        Log.d(TAG, "cronologia size ${cronologia.size}")
        putFileToFirebase(cronologiaRef, cronologia, CRONOLOGIA_FILE_NAME)

        Log.d(TAG, "BACKUP DB COMPLETATO")
    }

    private fun deleteExistingFile(storageRef: StorageReference, fileName: String, userId: String): StorageReference {
        val fileRef = storageRef.child("database_$userId/$fileName.json")

        try {
            Tasks.await(fileRef.delete())
            Log.d(TAG, "Backup esistente cancellato!")
        } catch (e: ExecutionException) {
            if (e.cause is StorageException && (e.cause as? StorageException)?.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND)
                Log.d(TAG, "Backup non trovato!")
            else
                throw e
        }
        return fileRef
    }

    private fun putFileToFirebase(fileRef: StorageReference, jsonObject: Any, fileName: String) {
        val gson = GsonBuilder().registerTypeAdapter(Date::class.java, DateTimeSerializer()).create()
        Log.d(TAG, "=== List to JSON ===")
        val jsonList: String = gson.toJson(jsonObject)
        Log.d(TAG, jsonList)

        val exportFile = File("${cacheDir.absolutePath}/$fileName.json")
        Log.d(TAG, "listToXML: exportFile = " + exportFile.absolutePath)
        val output = BufferedWriter(FileWriter(exportFile))
        output.write(jsonList)
        output.close()

        val saveFile = Tasks.await(fileRef.putFile(exportFile.toUri()))
        Log.d(TAG, "DocumentSnapshot added with path: ${saveFile.metadata?.path}")
    }

    fun restoreDatabase(userId: String?) {
        Log.d(TAG, "backupDatabase $userId")

        if (userId == null)
            throw NoIdException()

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val gson = GsonBuilder().registerTypeAdapter(Date::class.java, DateTimeDeserializer()).create()
        val risuscitoDb = RisuscitoDatabase.getInstance(this)

        //RESTORE CANTI
        val backupCanti: List<Backup> = gson.fromJson(InputStreamReader(getFileFromFirebase(storageRef, CANTO_FILE_NAME, userId)), object : TypeToken<List<Backup>>() {}.type)

        val cantoDao = risuscitoDb.cantoDao()
        cantoDao.truncateTable()
        Log.d(TAG, "Canto Truncated!")
        cantoDao.insertCanto(Canto.defaultCantoData())
        Log.d(TAG, "Canto default data inserted")
        for (backup in backupCanti) {
            Log.d(TAG, "backupCanto.id + ${backup.id} / backupCanto.savedTab ${backup.savedTab} / backupCanto.savedBarre ${backup.savedBarre}")
            cantoDao.setBackup(
                    backup.id,
                    backup.zoom,
                    backup.scrollX,
                    backup.scrollY,
                    backup.favorite,
                    backup.savedTab,
                    backup.savedBarre,
                    backup.savedSpeed)
        }


        //RESTORE CUSTOM LIST
        val backupCustomList: List<CustomList> = gson.fromJson(InputStreamReader(getFileFromFirebase(storageRef, CUSTOM_LIST_FILE_NAME, userId)), object : TypeToken<List<CustomList>>() {}.type)

        val customListDao = risuscitoDb.customListDao()
        customListDao.truncateTable()
        Log.d(TAG, "Custom List Truncated!")
        for (backup in backupCustomList) {
            Log.d(TAG, "backupCustomList.id + ${backup.id} / backupCustomList.position ${backup.position} / backupCustomList.idCanto ${backup.idCanto} / backupCustomList.timestamp ${backup.timestamp}")
            customListDao.insertPosition(backup)
        }


        //RESTORE LISTA PERS
        val backupListePers: List<ListaPers> = gson.fromJson(InputStreamReader(getFileFromFirebase(storageRef, LISTA_PERS_FILE_NAME, userId)), object : TypeToken<List<ListaPers>>() {}.type)

        val listePersDao = risuscitoDb.listePersDao()
        listePersDao.truncateTable()
        Log.d(TAG, "Liste Pers Truncated!")
        for (backup in backupListePers) {
            Log.d(TAG, "backupListePers.id + ${backup.id} / backupCustomList.position ${backup.titolo}")
            listePersDao.insertLista(backup)
        }


        //RESTORE LOCAL LINK
        val backupLocalLink: List<LocalLink> = gson.fromJson(InputStreamReader(getFileFromFirebase(storageRef, LOCAL_LINK_FILE_NAME, userId)), object : TypeToken<List<LocalLink>>() {}.type)

        val localLinkDao = risuscitoDb.localLinksDao()
        localLinkDao.truncateTable()
        Log.d(TAG, "Liste Pers Truncated!")
        for (backup in backupLocalLink) {
            Log.d(TAG, "backupLocalLink.idCanto + ${backup.idCanto} / backupLocalLink.localPath ${backup.localPath}")
            localLinkDao.insertLocalLink(backup)
        }


        //RESTORE CONSEGNATI
        val backupConsegnati: List<Consegnato> = gson.fromJson(InputStreamReader(getFileFromFirebase(storageRef, CONSEGNATO_FILE_NAME, userId)), object : TypeToken<List<Consegnato>>() {}.type)

        val consegnatiDao = risuscitoDb.consegnatiDao()
        consegnatiDao.truncateTable()
        Log.d(TAG, "Liste Pers Truncated!")
        for (backup in backupConsegnati) {
            Log.d(TAG, "backupConsegnati.idConsegnato + ${backup.idConsegnato} / backupConsegnati.idCanto ${backup.idCanto}")
            consegnatiDao.insertConsegnati(backup)
        }


        //RESTORE CRONOLOGIA
        val backupCronologia: List<Cronologia> = gson.fromJson(InputStreamReader(getFileFromFirebase(storageRef, CRONOLOGIA_FILE_NAME, userId)), object : TypeToken<List<Cronologia>>() {}.type)

        val cronologiaDao = risuscitoDb.cronologiaDao()
        cronologiaDao.truncateTable()
        Log.d(TAG, "Liste Pers Truncated!")
        for (backup in backupCronologia) {
            Log.d(TAG, "backupCronologia.idCanto + ${backup.idCanto} / backupCronologia.ultimaVisita ${backup.ultimaVisita}")
            cronologiaDao.insertCronologia(backup)
        }

        Log.d(TAG, "RESTORE DB COMPLETATO")
    }

    private fun getFileFromFirebase(storageRef: StorageReference, fileName: String, userId: String): InputStream {
        try {

            val cantoRef = storageRef.child("database_$userId/$fileName.json")
            val fileStream = Tasks.await(cantoRef.stream)
            return fileStream.stream

        } catch (e: ExecutionException) {
            Log.e(TAG, e.localizedMessage, e)
            if (e.cause is StorageException && (e.cause as? StorageException)?.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND)
                throw NoBackupException(resources)
            else
                throw e
        }
    }

    @Suppress("DEPRECATION")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setTaskDescriptionL() {
        val taskDesc = ActivityManager.TaskDescription(null, null, MaterialColors.getColor(this, R.attr.colorPrimary, TAG))
        setTaskDescription(taskDesc)
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun setTaskDescriptionP() {
        val taskDesc = ActivityManager.TaskDescription(null, R.mipmap.ic_launcher, MaterialColors.getColor(this, R.attr.colorPrimary, TAG))
        setTaskDescription(taskDesc)
    }

    companion object {
        internal val TAG = ThemeableActivity::class.java.canonicalName

        internal const val FIREBASE_FIELD_USER_ID = "userId"
        internal const val FIREBASE_FIELD_PREFERENCE = "userPreferences"
        internal const val FIREBASE_FIELD_EMAIL = "userEmail"
        internal const val FIREBASE_FIELD_TIMESTAMP = "timestamp"
        internal const val FIREBASE_COLLECTION_IMPOSTAZIONI = "Impostazioni"
        internal const val CANTO_FILE_NAME = "Canto"
        internal const val CUSTOM_LIST_FILE_NAME = "CustomList"
        internal const val LISTA_PERS_FILE_NAME = "ListaPers"
        internal const val LOCAL_LINK_FILE_NAME = "LocalLink"
        internal const val CONSEGNATO_FILE_NAME = "Consegnato"
        internal const val CRONOLOGIA_FILE_NAME = "Cronologia"

        val isMenuWorkaroundRequired: Boolean
            get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && ("LGE".equals(Build.MANUFACTURER, ignoreCase = true) || "E6710".equals(Build.DEVICE, ignoreCase = true))

    }
}
