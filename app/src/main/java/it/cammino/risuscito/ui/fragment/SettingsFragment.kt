package it.cammino.risuscito.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.DOWNLOADING
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.FAILED
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.INSTALLED
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.ui.RisuscitoApplication
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.activity.ThemeableActivity
import it.cammino.risuscito.ui.dialog.ProgressDialogFragment
import it.cammino.risuscito.utils.CambioAccordi
import it.cammino.risuscito.utils.LocaleManager
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.Utility.DEFAULT_INDEX
import it.cammino.risuscito.utils.Utility.DEFAULT_SEARCH
import it.cammino.risuscito.utils.Utility.DYNAMIC_COLORS
import it.cammino.risuscito.utils.Utility.NIGHT_MODE
import it.cammino.risuscito.utils.Utility.SAVE_LOCATION
import it.cammino.risuscito.utils.Utility.SCREEN_ON
import it.cammino.risuscito.utils.Utility.SYSTEM_LANGUAGE
import it.cammino.risuscito.utils.extension.checkScreenAwake
import it.cammino.risuscito.utils.extension.hasStorageAccess
import it.cammino.risuscito.utils.extension.setDefaultNightMode
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.collections.set

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val mSettingsViewModel: SettingsViewModel by viewModels()

    private lateinit var mEntries: Array<String>
    private lateinit var mEntryValues: Array<String>
    internal var mMainActivity: ThemeableActivity? = null

    private lateinit var splitInstallManager: SplitInstallManager
    private var sessionId = 0

    private val listener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == sessionId) {
            when (state.status()) {
                FAILED -> {
                    Log.e(TAG, "Module install failed with ${state.errorCode()}")
                    ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.dismiss()
                    mMainActivity?.let {
                        Snackbar.make(
                            it.findViewById(R.id.main_content),
                            "Module install failed with ${state.errorCode()}",
                            Snackbar.LENGTH_SHORT
                        )
                            .show()
                    }

                }

                REQUIRES_USER_CONFIRMATION -> {
                    splitInstallManager.startConfirmationDialogForResult(
                        state,
                        requireActivity(),
                        CONFIRMATION_REQUEST_CODE
                    )
                }

                DOWNLOADING -> {
                    val totalBytes = state.totalBytesToDownload()
                    val progress = state.bytesDownloaded()
                    Log.i(TAG, "DOWNLOADING LANGUAGE - progress: $progress su $totalBytes")
                    // Update progress bar.
                    if (totalBytes > 0)
                        ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)
                            ?.setProgress((100 * progress / totalBytes).toInt())
                }

                INSTALLED -> {
                    val newLanguage = mSettingsViewModel.persistingLanguage
                    mSettingsViewModel.persistingLanguage = StringUtils.EMPTY
                    ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.dismiss()
                    if (state.languages().isNotEmpty()) {
                        val currentLang = systemLocale.language
                        Log.i(TAG, "Module installed: language $currentLang")
                        Log.i(TAG, "Module installed: newLanguage $newLanguage")
                        lifecycleScope.launch { translate(currentLang, newLanguage) }
                    } else {
                        Log.e(TAG, "Module install failed: empyt language list")
                        mMainActivity?.let {
                            Snackbar.make(
                                it.findViewById(R.id.main_content),
                                "Module install failed: no language installed!",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }

                else -> Log.i(TAG, "Status: ${state.status()}")
            }
        }
    }

    private val changeListener = Preference.OnPreferenceChangeListener { _, newValue ->
        val currentLang = RisuscitoApplication.localeManager.getLanguage(requireContext())
        val newLanguage = newValue as? String ?: currentLang
        Log.i(TAG, "OnPreferenceChangeListener - oldValue: $currentLang")
        Log.i(TAG, "OnPreferenceChangeListener - newValue: $newLanguage")
        if (!currentLang.equals(newLanguage, ignoreCase = true)) {
            mMainActivity?.let { activity ->
                ProgressDialogFragment.show(
                    ProgressDialogFragment.Builder(
                        DOWNLOAD_LANGUAGE
                    ).apply {
                        content = R.string.download_running
                        icon = R.drawable.file_download_24px
                        progressIndeterminate = false
                        progressMax = 100
                    },
                    activity.supportFragmentManager
                )
            }
            // Creates a request to download and install additional language resources.
            val request = SplitInstallRequest.newBuilder()
                .addLanguage(
                    if (newLanguage == LocaleManager.LANGUAGE_ENGLISH_PHILIPPINES)
                        Locale(
                            LocaleManager.LANGUAGE_ENGLISH,
                            LocaleManager.COUNTRY_PHILIPPINES
                        )
                    else Locale(newLanguage)
                )
                .build()

            // Submits the request to install the additional language resources.
            mSettingsViewModel.persistingLanguage = newLanguage
            splitInstallManager.startInstall(request)
                // You should also add the following listener to handle any errors
                // processing the request.
                ?.addOnFailureListener { exception ->
                    Log.e(TAG, "language download error", exception)
                    ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)
                        ?.dismiss()
                    mMainActivity?.let {
                        Snackbar.make(
                            it.findViewById(R.id.main_content),
                            "error downloading language: ${(exception as? SplitInstallException)?.errorCode}",
                            Snackbar.LENGTH_SHORT
                        )
                            .show()
                    }
                }
                // When the platform accepts your request to download
                // an on demand module, it binds it to the following session ID.
                // You use this ID to track further status updates for the request.
                ?.addOnSuccessListener { id -> sessionId = id }
        }
        false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "Fragment: ${this::class.java.canonicalName}")
        Firebase.crashlytics.log("Fragment: ${this::class.java}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mMainActivity = activity as? ThemeableActivity

        splitInstallManager = SplitInstallManagerFactory.create(requireContext())

        //usato solo in tablet
        (mMainActivity as? MainActivity)?.let {
            it.setupToolbarTitle(R.string.title_activity_settings)
            it.setTabVisible(false)
            it.enableFab(false)
            it.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    it.updateProfileImage()
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return false
                }
            }, viewLifecycleOwner)
        }

        val listPreference = findPreference("memoria_salvataggio_scelta") as? DropDownPreference

        loadStorageList(activity?.hasStorageAccess == true)
        listPreference?.entries = mEntries
        listPreference?.entryValues = mEntryValues

        var pref = findPreference(SYSTEM_LANGUAGE) as? ListPreference
        pref?.onPreferenceChangeListener = changeListener
        pref?.summaryProvider = Preference.SummaryProvider<ListPreference> {
            composeSummaryListPreference(it)
        }

        pref = findPreference(DEFAULT_INDEX) as? DropDownPreference
        pref?.summaryProvider = Preference.SummaryProvider<DropDownPreference> {
            composeSummary(
                R.string.default_index_summary,
                it
            )
        }

        pref = findPreference(DEFAULT_SEARCH) as? DropDownPreference
        pref?.summaryProvider = Preference.SummaryProvider<DropDownPreference> {
            composeSummary(
                R.string.default_search_summary,
                it
            )
        }

        pref = findPreference(SAVE_LOCATION) as? DropDownPreference
        pref?.summaryProvider = Preference.SummaryProvider<DropDownPreference> {
            composeSummary(
                R.string.save_location_summary,
                it
            )
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        splitInstallManager.registerListener(listener)
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        try {
            splitInstallManager.unregisterListener(listener)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "unregister error", e)
        }
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    /** This is needed to handle the result of the manager.startConfirmationDialogForResult
    request that can be made from SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION
    in the listener above. */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CONFIRMATION_REQUEST_CODE) {
            // Handle the user's decision. For example, if the user selects "Cancel",
            // you may want to disable certain functionality that depends on the module.
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.e(TAG, "download cancelled by user")
                ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.dismiss()
                mMainActivity?.let {
                    Snackbar.make(
                        it.findViewById(R.id.main_content),
                        "download cancelled by user",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(TAG, "onSharedPreferenceChanged: $key")
        if (key == NIGHT_MODE) {
            Log.d(
                TAG,
                "onSharedPreferenceChanged: dark_mode: ${
                    sharedPreferences?.getString(
                        key,
                        "2"
                    ) ?: ""
                }"
            )
            context?.setDefaultNightMode()
        }
        if (key == SCREEN_ON) activity?.checkScreenAwake()
        if (key == DYNAMIC_COLORS) activity?.recreate()
    }

    private fun composeSummary(@StringRes id: Int, pref: DropDownPreference): String {
        val text = pref.entry
        return "${getString(id)}${System.getProperty("line.separator")}$text"
    }

    private fun composeSummaryListPreference(pref: ListPreference): String {
        val text = pref.entry
        return "${getString(R.string.language_summary)}${System.getProperty("line.separator")}$text"
    }

    private fun loadStorageList(external: Boolean) {
        Log.d(
            TAG,
            "loadStorageList: WRITE_EXTERNAL_STORAGE "
                    + Utility.isExternalStorageWritable
                    + " / "
                    + external
        )
        if (Utility.isExternalStorageWritable && external) {
            mEntries = resources.getStringArray(R.array.save_location_sd_entries)
            mEntryValues = resources.getStringArray(R.array.save_location_sd_values)
        } else {
            mEntries = resources.getStringArray(R.array.save_location_nosd_entries)
            mEntryValues = resources.getStringArray(R.array.save_location_nosd_values)
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit { putString(SAVE_LOCATION, "0") }
        }
    }

    private suspend fun translate(oldLanguage: String, newLanguage: String) {
        Log.d(TAG, "translate")
        mMainActivity?.let { activity ->
            ProgressDialogFragment.show(
                ProgressDialogFragment.Builder(TRANSLATION).apply {
                    content = R.string.translation_running
                    progressIndeterminate = true
                },
                activity.supportFragmentManager
            )
        }
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            convertTabs(oldLanguage, newLanguage)
            convertiBarre(oldLanguage, newLanguage)
        }
        try {
            ProgressDialogFragment.findVisible(mMainActivity, TRANSLATION)?.dismiss()
            RisuscitoApplication.localeManager.updateLanguage(
                requireContext(),
                newLanguage
            )
        } catch (e: IllegalArgumentException) {
            Log.e(javaClass.name, e.localizedMessage, e)
        }
    }

    // converte gli accordi salvati dalla lingua vecchia alla nuova
    private fun convertTabs(oldLanguage: String, newLanguage: String) {
        var accordi1 = CambioAccordi.accordi_it
        Log.d(TAG, "convertTabs - from: $oldLanguage")
        when (oldLanguage) {
            LocaleManager.LANGUAGE_UKRAINIAN -> accordi1 = CambioAccordi.accordi_uk
            LocaleManager.LANGUAGE_POLISH -> accordi1 = CambioAccordi.accordi_pl
            LocaleManager.LANGUAGE_ENGLISH -> accordi1 = CambioAccordi.accordi_en
            LocaleManager.LANGUAGE_ENGLISH_PHILIPPINES -> accordi1 = CambioAccordi.accordi_en
        }

        var accordi2 = CambioAccordi.accordi_it
        Log.d(TAG, "convertTabs - to: $newLanguage")
        when (newLanguage) {
            LocaleManager.LANGUAGE_UKRAINIAN -> accordi2 = CambioAccordi.accordi_uk
            LocaleManager.LANGUAGE_POLISH -> accordi2 = CambioAccordi.accordi_pl
            LocaleManager.LANGUAGE_ENGLISH -> accordi2 = CambioAccordi.accordi_en
            LocaleManager.LANGUAGE_ENGLISH_PHILIPPINES -> accordi2 = CambioAccordi.accordi_en
        }

        val mappa = HashMap<String, String>()
        for (i in CambioAccordi.accordi_it.indices) mappa[accordi1[i]] = accordi2[i]

        val mDao = RisuscitoDatabase.getInstance(requireContext()).cantoDao()
        val canti = mDao.allByName()
        for (canto in canti) {
            if (!canto.savedTab.isNullOrEmpty()) {
                Log.d(
                    TAG,
                    "convertTabs: "
                            + "ID "
                            + canto.id
                            + " -> CONVERTO DA "
                            + canto.savedTab
                            + " A "
                            + mappa[canto.savedTab.orEmpty()]
                )
                canto.savedTab = mappa[canto.savedTab.orEmpty()]
                mDao.updateCanto(canto)
            }
        }
    }

    // converte gli accordi salvati dalla lingua vecchia alla nuova
    private fun convertiBarre(oldLanguage: String, newLanguage: String) {
        var barre1 = CambioAccordi.barre_it
        Log.d(TAG, "convertiBarre - from: $oldLanguage")
        when (oldLanguage) {
            LocaleManager.LANGUAGE_ENGLISH -> barre1 = CambioAccordi.barre_en
        }

        var barre2 = CambioAccordi.barre_it
        Log.d(TAG, "convertiBarre - to: $newLanguage")
        when (newLanguage) {
            LocaleManager.LANGUAGE_ENGLISH -> barre2 = CambioAccordi.barre_en
        }

        val mappa = HashMap<String, String>()
        for (i in CambioAccordi.barre_it.indices) mappa[barre1[i]] = barre2[i]

        val mDao = RisuscitoDatabase.getInstance(requireContext()).cantoDao()
        val canti = mDao.allByName()
        for (canto in canti) {
            if (!canto.savedTab.isNullOrEmpty()) {
                Log.d(
                    TAG,
                    "convertiBarre: "
                            + "ID "
                            + canto.id
                            + " -> CONVERTO DA "
                            + canto.savedBarre
                            + " A "
                            + mappa[canto.savedBarre]
                )
                canto.savedBarre = mappa[canto.savedBarre]
                mDao.updateCanto(canto)
            }
        }
    }

    companion object {
        private const val DOWNLOAD_LANGUAGE = "download_language"
        private const val CONFIRMATION_REQUEST_CODE = 1
        private const val TRANSLATION = "TRANSLATION"
        private val TAG = SettingsFragment::class.java.canonicalName
    }

}
