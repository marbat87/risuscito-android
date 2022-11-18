package it.cammino.risuscito.ui.fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.preference.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.*
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.RisuscitoApplication
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.dialog.ProgressDialogFragment
import it.cammino.risuscito.utils.LocaleManager
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.Utility.CHANGE_LANGUAGE
import it.cammino.risuscito.utils.Utility.DEFAULT_INDEX
import it.cammino.risuscito.utils.Utility.DEFAULT_SEARCH
import it.cammino.risuscito.utils.Utility.DYNAMIC_COLORS
import it.cammino.risuscito.utils.Utility.NEW_LANGUAGE
import it.cammino.risuscito.utils.Utility.NIGHT_MODE
import it.cammino.risuscito.utils.Utility.OLD_LANGUAGE
import it.cammino.risuscito.utils.Utility.SAVE_LOCATION
import it.cammino.risuscito.utils.Utility.SCREEN_ON
import it.cammino.risuscito.utils.Utility.SYSTEM_LANGUAGE
import it.cammino.risuscito.utils.extension.checkScreenAwake
import it.cammino.risuscito.utils.extension.hasStorageAccess
import it.cammino.risuscito.utils.extension.setDefaultNightMode
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.SettingsViewModel
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val mSettingsViewModel: SettingsViewModel by viewModels()

    private lateinit var mEntries: Array<String>
    private lateinit var mEntryValues: Array<String>
    internal var mMainActivity: MainActivity? = null

    private lateinit var splitInstallManager: SplitInstallManager
    private var sessionId = 0

    private val listener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == sessionId) {
            val newLanguage = mSettingsViewModel.persistingLanguage
            mSettingsViewModel.persistingLanguage = StringUtils.EMPTY
            when (state.status()) {
                FAILED -> {
                    Log.e(TAG, "Module install failed with ${state.errorCode()}")
                    ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.dismiss()
                    mMainActivity?.let {
                        Snackbar.make(
                            it.activityMainContent,
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
                    ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)
                        ?.setProgress((100 * progress / totalBytes).toInt())
                }
                INSTALLED -> {
                    ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.dismiss()
                    if (state.languages().isNotEmpty()) {
                        val currentLang = resources.systemLocale.language
                        Log.i(TAG, "Module installed: language $newLanguage")
                        Log.i(TAG, "Module installed: newLanguage $newLanguage")
                        RisuscitoApplication.localeManager.persistLanguage(
                            requireContext(),
                            newLanguage
                        )
                        val mIntent =
                            activity?.baseContext?.packageManager?.getLaunchIntentForPackage(
                                requireActivity().baseContext.packageName
                            )
                        mIntent?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            it.putExtra(CHANGE_LANGUAGE, true)
                            it.putExtra(OLD_LANGUAGE, currentLang)
                            it.putExtra(NEW_LANGUAGE, newLanguage)
                            startActivity(it)
                        }
                    } else {
                        Log.e(TAG, "Module install failed: empyt language list")
                        mMainActivity?.let {
                            Snackbar.make(
                                it.activityMainContent,
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
                        Locale(LocaleManager.LANGUAGE_ENGLISH, LocaleManager.COUNTRY_PHILIPPINES)
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
                    ProgressDialogFragment.findVisible(mMainActivity, DOWNLOAD_LANGUAGE)?.dismiss()
                    mMainActivity?.let {
                        Snackbar.make(
                            it.activityMainContent,
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mMainActivity = activity as? MainActivity

        splitInstallManager = SplitInstallManagerFactory.create(requireContext())

        mMainActivity?.setupToolbarTitle(R.string.title_activity_settings)

        mMainActivity?.setTabVisible(false)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)

        val listPreference = findPreference("memoria_salvataggio_scelta") as? DropDownPreference

        loadStorageList(activity?.hasStorageAccess == true)
        listPreference?.entries = mEntries
        listPreference?.entryValues = mEntryValues

        var pref = findPreference(SYSTEM_LANGUAGE) as? ListPreference
        pref?.onPreferenceChangeListener = changeListener
        pref?.summaryProvider = Preference.SummaryProvider<ListPreference> {
            composeSummary(
                R.string.language_summary,
                it
            )
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

        mMainActivity?.let {
            it.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    it.updateProfileImage()
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return false
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
                        it.activityMainContent,
                        "download cancelled by user",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        Log.d(TAG, "onSharedPreferenceChanged: $s")
        if (s == NIGHT_MODE) {
            Log.d(TAG, "onSharedPreferenceChanged: dark_mode" + sharedPreferences.getString(s, "0"))
            context?.setDefaultNightMode()
        }
        if (s == SCREEN_ON) activity?.checkScreenAwake()
        if (s == DYNAMIC_COLORS) activity?.recreate()
    }

    private fun composeSummary(@StringRes id: Int, pref: DropDownPreference): String {
        val text = pref.entry
        return "${getString(id)}${System.getProperty("line.separator")}$text"
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


    companion object {
        private const val DOWNLOAD_LANGUAGE = "download_language"
        private const val CONFIRMATION_REQUEST_CODE = 1
        private val TAG = SettingsFragment::class.java.canonicalName
    }

}
