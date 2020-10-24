package it.cammino.risuscito

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.*
import com.takisoft.preferencex.PreferenceFragmentCompat
import com.takisoft.preferencex.SimpleMenuPreference
import it.cammino.risuscito.Utility.CHANGE_LANGUAGE
import it.cammino.risuscito.Utility.DB_RESET
import it.cammino.risuscito.Utility.DEFAULT_INDEX
import it.cammino.risuscito.Utility.DEFAULT_SEARCH
import it.cammino.risuscito.Utility.NIGHT_MODE
import it.cammino.risuscito.Utility.PRIMARY_COLOR
import it.cammino.risuscito.Utility.SAVE_LOCATION
import it.cammino.risuscito.Utility.SCREEN_ON
import it.cammino.risuscito.Utility.SECONDARY_COLOR
import it.cammino.risuscito.Utility.SYSTEM_LANGUAGE
import it.cammino.risuscito.dialogs.ProgressDialogFragment
import it.cammino.risuscito.ui.LocaleManager
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.ui.RisuscitoApplication
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.SettingsViewModel
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val mSettingsViewModel: SettingsViewModel by viewModels()

    private lateinit var mEntries: Array<String>
    private lateinit var mEntryValues: Array<String>
    internal var mMainActivity: MainActivity? = null

    private lateinit var splitInstallManager: SplitInstallManager
    private var sessionId = 0

    private val listener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == sessionId) {
            val newLanguage = mSettingsViewModel.persistingLanguage
            mSettingsViewModel.persistingLanguage = ""
            when (state.status()) {
                FAILED -> {
                    Log.e(TAG, "Module install failed with ${state.errorCode()}")
                    ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.dismiss()
                    mMainActivity?.let {
                        Snackbar.make(
                                it.activityMainContent,
                                "Module install failed with ${state.errorCode()}",
                                Snackbar.LENGTH_SHORT)
                                .show()
                    }

                }
                REQUIRES_USER_CONFIRMATION -> {
                    splitInstallManager.startConfirmationDialogForResult(state, requireActivity(), CONFIRMATION_REQUEST_CODE)
                }
                DOWNLOADING -> {
                    val totalBytes = state.totalBytesToDownload()
                    val progress = state.bytesDownloaded()
                    Log.i(TAG, "DOWNLOADING LANGUAGE - progress: $progress su $totalBytes")
                    // Update progress bar.
                    ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.setProgress((100 * progress / totalBytes).toInt())
                }
                INSTALLED -> {
                    ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.dismiss()
                    if (state.languages().isNotEmpty()) {
//                        val newLanguage = state.languages().first()
                        Log.i(TAG, "Module installed: language $newLanguage")
                        Log.i(TAG, "Module installed: newLanguage $newLanguage")
                        RisuscitoApplication.localeManager.persistLanguage(requireContext(), newLanguage)
                        val mIntent = activity?.baseContext?.packageManager?.getLaunchIntentForPackage(requireActivity().baseContext.packageName)
                        mIntent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            it.putExtra(DB_RESET, true)
                            val currentLang = getSystemLocale(resources).language
                            it.putExtra(
                                    CHANGE_LANGUAGE,
                                    "$currentLang-$newLanguage")
                            startActivity(it)
                        }
                    } else {
                        Log.e(TAG, "Module install failed: empyt language list")
                        mMainActivity?.let {
                            Snackbar.make(
                                    it.activityMainContent,
                                    "Module install failed: no language installed!",
                                    Snackbar.LENGTH_SHORT)
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
        Log.i(TAG, "OnPreferenceChangeListener - oldValue: ${RisuscitoApplication.localeManager.getLanguage(requireContext())}")
        Log.i(TAG, "OnPreferenceChangeListener - newValue: $newValue")
        if (!currentLang.equals(newValue as? String ?: currentLang, ignoreCase = true)) {
            if (LUtils.hasL()) {
                mMainActivity?.let { activity ->
                    ProgressDialogFragment.show(ProgressDialogFragment.Builder(
                            activity, DOWNLOAD_LANGUAGE)
                            .content(R.string.download_running)
                            .progressIndeterminate(false)
                            .progressMax(100),
                            activity.supportFragmentManager)
                }
                // Creates a request to download and install additional language resources.
                val request = SplitInstallRequest.newBuilder()
                        .addLanguage(if ((newValue as? String
                                        ?: "") == LocaleManager.LANGUAGE_ENGLISH_PHILIPPINES)
                            Locale(LocaleManager.LANGUAGE_ENGLISH, LocaleManager.COUNTRY_PHILIPPINES)
                        else Locale(newValue as? String ?: ""))
                        .build()

                // Submits the request to install the additional language resources.
                mSettingsViewModel.persistingLanguage = newValue as? String ?: ""
                splitInstallManager.startInstall(request)
                        // You should also add the following listener to handle any errors
                        // processing the request.
                        ?.addOnFailureListener { exception ->
                            Log.e(TAG, "language download error", exception)
                            ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.dismiss()
                            mMainActivity?.let {
                                Snackbar.make(
                                        it.activityMainContent,
                                        "error downloading language: ${(exception as? SplitInstallException)?.errorCode}",
                                        Snackbar.LENGTH_SHORT)
                                        .show()
                            }
                        }
                        // When the platform accepts your request to download
                        // an on demand module, it binds it to the following session ID.
                        // You use this ID to track further status updates for the request.
                        ?.addOnSuccessListener { id -> sessionId = id }
            } else {
                RisuscitoApplication.localeManager.persistLanguage(requireContext(), newValue as? String
                        ?: currentLang)
                val mIntent = activity?.baseContext?.packageManager?.getLaunchIntentForPackage(requireActivity().baseContext.packageName)
                mIntent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    it.putExtra(DB_RESET, true)
                    it.putExtra(
                            CHANGE_LANGUAGE,
                            "$currentLang-${newValue as? String ?: currentLang}")
                    startActivity(it)
                }
            }
        }
        false
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mMainActivity = activity as? MainActivity

        splitInstallManager = SplitInstallManagerFactory.create(requireContext())

        mMainActivity?.setupToolbarTitle(R.string.title_activity_settings)

        mMainActivity?.setTabVisible(false)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)

        val listPreference = findPreference("memoria_salvataggio_scelta") as? ListPreference

        loadStorageList(
                EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))

        listPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val listPref = it as? ListPreference
            listPref?.entries = mEntries
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val saveLocation = pref.getString(SAVE_LOCATION, "0")
            listPref?.setDefaultValue(saveLocation)
            listPref?.entryValues = mEntryValues
            false
        }

        var pref = findPreference(SYSTEM_LANGUAGE) as? SimpleMenuPreference
        pref?.onPreferenceChangeListener = changeListener
        pref?.summaryProvider = Preference.SummaryProvider<ListPreference> { composeSummary(R.string.language_summary, it) }

        pref = findPreference(DEFAULT_INDEX) as? SimpleMenuPreference
        pref?.summaryProvider = Preference.SummaryProvider<ListPreference> { composeSummary(R.string.default_index_summary, it) }

        pref = findPreference(DEFAULT_SEARCH) as? SimpleMenuPreference
        pref?.summaryProvider = Preference.SummaryProvider<ListPreference> { composeSummary(R.string.default_search_summary, it) }

        pref = findPreference(SAVE_LOCATION) as? SimpleMenuPreference
        pref?.summaryProvider = Preference.SummaryProvider<ListPreference> { composeSummary(R.string.save_location_summary, it) }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        splitInstallManager.registerListener(listener)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        try {
            splitInstallManager.unregisterListener(listener)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "unregister error", e)
        }
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /** This is needed to handle the result of the manager.startConfirmationDialogForResult
    request that can be made from SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION
    in the listener above. */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CONFIRMATION_REQUEST_CODE) {
            // Handle the user's decision. For example, if the user selects "Cancel",
            // you may want to disable certain functionality that depends on the module.
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.e(TAG, "download cancelled by user")
                ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.dismiss()
                mMainActivity?.let {
                    Snackbar.make(
                            it.activityMainContent,
                            "download cancelled by user",
                            Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        Log.d(TAG, "onSharedPreferenceChanged: $s")
        if (s == PRIMARY_COLOR) {
            Log.d(TAG, "onSharedPreferenceChanged: PRIMARY_COLOR" + sharedPreferences.getInt(s, 0))
            activity?.recreate()
        }
        if (s == SECONDARY_COLOR) {
            Log.d(TAG, "onSharedPreferenceChanged: SECONDARY_COLOR" + sharedPreferences.getInt(s, 0))
            activity?.recreate()
        }
        if (s == NIGHT_MODE) {
            Log.d(TAG, "onSharedPreferenceChanged: dark_mode" + sharedPreferences.getString(s, "0"))
            ThemeUtils.setDefaultNightMode(requireContext())
        }
        if (s == SCREEN_ON) LUtils.getInstance(requireActivity()).checkScreenAwake()
    }

    private fun composeSummary(@StringRes id: Int, pref: ListPreference): String {
        val text = pref.entry
        return "${getString(id)}${System.getProperty("line.separator")}$text"
    }

    private fun loadStorageList(external: Boolean) {
        Log.d(
                TAG,
                "loadStorageList: WRITE_EXTERNAL_STORAGE "
                        + Utility.isExternalStorageWritable
                        + " / "
                        + external)
        if (Utility.isExternalStorageWritable && external) {
            mEntries = resources.getStringArray(R.array.save_location_sd_entries)
            mEntryValues = resources.getStringArray(R.array.save_location_sd_values)
        } else {
            mEntries = resources.getStringArray(R.array.save_location_nosd_entries)
            mEntryValues = resources.getStringArray(R.array.save_location_nosd_values)
            PreferenceManager.getDefaultSharedPreferences(context).edit { putString(SAVE_LOCATION, "0") }
        }
    }


    companion object {
        private const val DOWNLOAD_LANGUAGE = "download_language"
        private const val CONFIRMATION_REQUEST_CODE = 1
        private val TAG = SettingsFragment::class.java.canonicalName
    }

}
