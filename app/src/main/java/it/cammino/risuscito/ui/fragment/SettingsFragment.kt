package it.cammino.risuscito.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
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
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.jakewharton.processphoenix.ProcessPhoenix
import com.michaelflisar.composepreferences.core.PreferenceScreen
import com.michaelflisar.composepreferences.core.PreferenceSection
import com.michaelflisar.composepreferences.core.classes.PreferenceSettingsDefaults
import com.michaelflisar.composepreferences.core.styles.ModernStyle
import com.michaelflisar.composepreferences.screen.bool.PreferenceBool
import com.michaelflisar.composepreferences.screen.list.PreferenceList
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.RisuscitoApplication
import it.cammino.risuscito.ui.activity.ThemeableActivity
import it.cammino.risuscito.ui.composable.hasNavigationBar
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.utils.CambioAccordi
import it.cammino.risuscito.utils.LocaleManager
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.Utility.DEFAULT_INDEX
import it.cammino.risuscito.utils.Utility.DEFAULT_SEARCH
import it.cammino.risuscito.utils.Utility.DYNAMIC_COLORS
import it.cammino.risuscito.utils.Utility.NIGHT_MODE
import it.cammino.risuscito.utils.Utility.SAVE_LOCATION
import it.cammino.risuscito.utils.Utility.SCREEN_ON
import it.cammino.risuscito.utils.Utility.SHOW_AUDIO
import it.cammino.risuscito.utils.Utility.SHOW_EUCARESTIA_PACE
import it.cammino.risuscito.utils.Utility.SHOW_OFFERTORIO
import it.cammino.risuscito.utils.Utility.SHOW_PACE
import it.cammino.risuscito.utils.Utility.SHOW_SANTO
import it.cammino.risuscito.utils.Utility.SHOW_SECONDA
import it.cammino.risuscito.utils.extension.checkScreenAwake
import it.cammino.risuscito.utils.extension.hasStorageAccess
import it.cammino.risuscito.utils.extension.prefNightMode
import it.cammino.risuscito.utils.extension.setDefaultNightMode
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.ProgressDialogManagerViewModel
import it.cammino.risuscito.viewmodels.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SettingsFragment : Fragment() {

    private val mSettingsViewModel: SettingsViewModel by viewModels()

    private val progressDialogViewModel: ProgressDialogManagerViewModel by activityViewModels()

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
                    progressDialogViewModel.showProgressDialog.value = false
                    mMainActivity?.showSnackBar("Module install failed with ${state.errorCode()}")
                }

                REQUIRES_USER_CONFIRMATION -> {
                    splitInstallManager.startConfirmationDialogForResult(
                        state, requireActivity(), CONFIRMATION_REQUEST_CODE
                    )
                }

                DOWNLOADING -> {
                    val totalBytes = state.totalBytesToDownload()
                    val progress = state.bytesDownloaded()
                    Log.i(TAG, "DOWNLOADING LANGUAGE - progress: $progress su $totalBytes")
                    // Update progress bar.
                    if (totalBytes > 0) progressDialogViewModel.progress.value =
                        (progress / totalBytes).toFloat()
                }

                INSTALLED -> {
                    val newLanguage = mSettingsViewModel.persistingLanguage
                    mSettingsViewModel.persistingLanguage = StringUtils.EMPTY
                    mMainActivity
                    progressDialogViewModel.showProgressDialog.value = false
                    Log.i(TAG, "Module installed")
                    if (state.languages().isNotEmpty()) {
                        val currentLang = systemLocale.language
                        Log.i(TAG, "Module installed: language $currentLang")
                        Log.i(TAG, "Module installed: newLanguage $newLanguage")
                        lifecycleScope.launch { translate(currentLang, newLanguage) }
                    } else {
                        Log.e(TAG, "Module install failed: empyt language list")
                        mMainActivity?.showSnackBar("Module install failed: no language installed!")
                    }
                }

                else -> Log.i(TAG, "Status: ${state.status()}")
            }
        }
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

        val preferenManager = PreferenceManager.getDefaultSharedPreferences(requireContext())

        loadStorageList(activity?.hasStorageAccess == true)

        mMainActivity?.setTabVisible(false)
        mMainActivity?.initFab(enable = false)

        return ComposeView(requireContext()).apply {

            setContent {

                val hasNavigationBar = hasNavigationBar()

                val themeLablesArray = stringArrayResource(R.array.themeListArray)
                val themeValueArray = stringArrayResource(R.array.themeEntryArray)
                val themeMapList = themeValueArray.zip(themeLablesArray)

                val defaultIndexesLabelsArray =
                    stringArrayResource(R.array.pref_default_index_entries)
                val defaultIndexesValuesArray =
                    stringArrayResource(R.array.pref_default_index_values)
                val defaultIndexesMap = defaultIndexesValuesArray.zip(defaultIndexesLabelsArray)

                val defaultSearchLabelsArray =
                    stringArrayResource(R.array.pref_default_search_entries)
                val defaultSearchValuesArray =
                    stringArrayResource(R.array.pref_default_search_values)
                val defaultSearchMap = defaultSearchValuesArray.zip(defaultSearchLabelsArray)

                val storageMapList = mEntryValues.zip(mEntries)

                val defaultLanguageLabelsArray = stringArrayResource(R.array.pref_languages_entries)
                val defaultLanguageValuesArray = stringArrayResource(R.array.pref_languages_values)
                val defaultLanguageMap = defaultLanguageValuesArray.zip(defaultLanguageLabelsArray)

                // select a style for your preferences
                val modernStyle = ModernStyle.create(
                    sectionBackgroundColor = Color.Transparent,
                    sectionGroupItemBackgroundColor = MaterialTheme.colorScheme.surface,
                )

                val settings = PreferenceSettingsDefaults.settings(
                    style = modernStyle
                )

                val nightMode = remember { mutableStateOf(requireContext().prefNightMode) }

                var dynamicColors by remember {
                    mutableStateOf(
                        preferenManager.getBoolean(
                            DYNAMIC_COLORS, false
                        )
                    )
                }

                var showSecondaLettura by remember {
                    mutableStateOf(
                        preferenManager.getBoolean(
                            SHOW_SECONDA, false
                        )
                    )
                }

                var showOffertorio by remember {
                    mutableStateOf(
                        preferenManager.getBoolean(
                            SHOW_OFFERTORIO, false
                        )
                    )
                }

                var showEucaristiaPace by remember {
                    mutableStateOf(
                        preferenManager.getBoolean(
                            SHOW_EUCARESTIA_PACE, true
                        )
                    )
                }

                var showSanto by remember {
                    mutableStateOf(
                        preferenManager.getBoolean(
                            SHOW_SANTO, false
                        )
                    )
                }

                var showParolaPace by remember {
                    mutableStateOf(
                        preferenManager.getBoolean(
                            SHOW_PACE, false
                        )
                    )
                }

                val defaultIndex = remember {
                    mutableStateOf(
                        preferenManager.getString(
                            DEFAULT_INDEX, "0"
                        )
                    )
                }

                val defaultSearch = remember {
                    mutableStateOf(
                        preferenManager.getString(
                            DEFAULT_SEARCH, "0"
                        )
                    )
                }

                var alwaysOnDisplay by remember {
                    mutableStateOf(
                        preferenManager.getBoolean(
                            SCREEN_ON, false
                        )
                    )
                }

                var showAudio by remember {
                    mutableStateOf(
                        preferenManager.getBoolean(
                            SHOW_AUDIO, true
                        )
                    )
                }

                val defaultSaveLocation = remember {
                    mutableStateOf(
                        preferenManager.getString(
                            SAVE_LOCATION, "0"
                        )
                    )
                }

                val defaultLanguage = remember {
                    mutableStateOf(
                        RisuscitoApplication.localeManager.getLanguage(requireContext())
                    )
                }

                RisuscitoTheme {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {

                        PreferenceScreen(
                            settings = settings,
                        ) {
                            PreferenceSection(
                                title = stringResource(R.string.theme)
                            ) {
                                PreferenceList(
                                    style = PreferenceList.Style.Spinner,
                                    value = themeMapList.find { it.first == nightMode.value },
                                    onValueChange = {
                                        Log.d(TAG, "setDefaultNightMode: ${it?.first}")
                                        val newValue = it?.first.orEmpty()
                                        nightMode.value = newValue
                                        preferenManager.edit {
                                            putString(NIGHT_MODE, newValue)
                                        }
                                        context?.setDefaultNightMode()
                                    },
                                    items = themeMapList,
                                    itemTextProvider = { it?.second.orEmpty() },
                                    title = stringResource(R.string.night_theme_title),
                                )
                                if (OSUtils.hasS()) {
                                    PreferenceBool(
                                        value = dynamicColors,
                                        onValueChange = {
                                            @Suppress("AssignedValueIsNeverRead")
                                            dynamicColors = it
                                            saveBooleanPreference(
                                                preferenceKey = DYNAMIC_COLORS,
                                                preferenceValue = it,
                                                restart = true,
                                                hasNavigationBar = hasNavigationBar
                                            )
                                        },
                                        title = stringResource(R.string.dynamic_colors_title),
                                        subtitle = stringResource(R.string.dynamic_colors_summary)
                                    )
                                }
                            }

                            PreferenceSection(
                                title = stringResource(R.string.lists_pref)
                            ) {
                                PreferenceBool(
                                    value = showSecondaLettura,
                                    onValueChange = {
                                        @Suppress("AssignedValueIsNeverRead")
                                        showSecondaLettura = it
                                        saveBooleanPreference(
                                            preferenceKey = SHOW_SECONDA,
                                            preferenceValue = it
                                        )
                                    },
                                    title = stringResource(R.string.show_seconda_title),
                                    subtitle = stringResource(R.string.show_seconda_summary)
                                )
                                PreferenceBool(
                                    value = showOffertorio,
                                    onValueChange = {
                                        @Suppress("AssignedValueIsNeverRead")
                                        showOffertorio = it
                                        saveBooleanPreference(
                                            preferenceKey = SHOW_OFFERTORIO,
                                            preferenceValue = it,
                                        )
                                    },
                                    title = stringResource(R.string.show_offertorio_title),
                                    subtitle = stringResource(R.string.show_offertorio_summary)
                                )
                                PreferenceBool(
                                    value = showEucaristiaPace,
                                    onValueChange = {
                                        @Suppress("AssignedValueIsNeverRead")
                                        showEucaristiaPace = it
                                        saveBooleanPreference(
                                            preferenceKey = SHOW_EUCARESTIA_PACE,
                                            preferenceValue = it,
                                        )
                                    },
                                    title = stringResource(R.string.show_eucarestia_pace_title),
                                    subtitle = stringResource(R.string.show_eucarestia_pace_summary)
                                )
                                PreferenceBool(
                                    value = showSanto,
                                    onValueChange = {
                                        @Suppress("AssignedValueIsNeverRead")
                                        showSanto = it
                                        saveBooleanPreference(
                                            preferenceKey = SHOW_SANTO,
                                            preferenceValue = it,
                                        )
                                    },
                                    title = stringResource(R.string.show_santo_title),
                                    subtitle = stringResource(R.string.show_santo_summary)
                                )
                                PreferenceBool(
                                    value = showParolaPace,
                                    onValueChange = {
                                        @Suppress("AssignedValueIsNeverRead")
                                        showParolaPace = it
                                        saveBooleanPreference(
                                            preferenceKey = SHOW_PACE,
                                            preferenceValue = it,
                                        )
                                    },
                                    title = stringResource(R.string.show_pace_title),
                                    subtitle = stringResource(R.string.show_pace_summary)
                                )
                                PreferenceList(
                                    style = PreferenceList.Style.Spinner,
                                    value = defaultIndexesMap.find { it.first == defaultIndex.value },
                                    onValueChange = {
                                        Log.d(TAG, "setDefaultIndex: ${it?.first}")
                                        val newValue = it?.first.orEmpty()
                                        defaultIndex.value = newValue
                                        preferenManager.edit {
                                            putString(DEFAULT_INDEX, newValue)
                                        }
                                    },
                                    items = defaultIndexesMap,
                                    itemTextProvider = { it?.second.orEmpty() },
                                    title = stringResource(R.string.default_index_title),
                                    subtitle = stringResource(R.string.default_index_summary)
                                )
                                PreferenceList(
                                    style = PreferenceList.Style.Spinner,
                                    value = defaultSearchMap.find { it.first == defaultSearch.value },
                                    onValueChange = {
                                        Log.d(TAG, "setDefaultSearch: ${it?.first}")
                                        val newValue = it?.first.orEmpty()
                                        defaultSearch.value = newValue
                                        Log.d(
                                            TAG, "saveStringPreference: $DEFAULT_SEARCH / $newValue"
                                        )
                                        val preferenManager =
                                            PreferenceManager.getDefaultSharedPreferences(
                                                requireContext()
                                            )
                                        val actualValue =
                                            preferenManager.getString(DEFAULT_SEARCH, "0")
                                        if (actualValue != newValue) {
                                            Log.d(
                                                TAG,
                                                "saveStringPreference SAVE: $DEFAULT_SEARCH / $newValue"
                                            )
                                            preferenManager.edit {
                                                putString(DEFAULT_SEARCH, newValue)
                                            }
                                            recreateActivity(hasNavigationBar)
                                        }
                                    },
                                    items = defaultSearchMap,
                                    itemTextProvider = { it?.second.orEmpty() },
                                    title = stringResource(R.string.default_search_title),
                                    subtitle = stringResource(R.string.default_search_summary)
                                )
                            }

                            PreferenceSection(
                                title = stringResource(R.string.system)
                            ) {
                                PreferenceBool(
                                    value = alwaysOnDisplay,
                                    onValueChange = {
                                        @Suppress("AssignedValueIsNeverRead")
                                        alwaysOnDisplay = it
                                        saveBooleanPreference(
                                            preferenceKey = SCREEN_ON,
                                            preferenceValue = it,
                                        )
                                        activity?.checkScreenAwake()
                                    },
                                    title = stringResource(R.string.always_on),
                                    subtitle = stringResource(R.string.always_on_summary)
                                )
                                PreferenceBool(
                                    value = showAudio,
                                    onValueChange = {
                                        @Suppress("AssignedValueIsNeverRead")
                                        showAudio = it
                                        saveBooleanPreference(
                                            preferenceKey = SHOW_AUDIO,
                                            preferenceValue = it,
                                        )
                                    },
                                    title = stringResource(R.string.show_audio_title),
                                    subtitle = stringResource(R.string.show_audio_summary)
                                )
                                PreferenceList(
                                    style = PreferenceList.Style.Spinner,
                                    value = storageMapList.find { it.first == defaultSaveLocation.value },
                                    onValueChange = {
                                        Log.d(TAG, "setDefaultSaveLocation: ${it?.first}")
                                        val newValue = it?.first.orEmpty()
                                        defaultSaveLocation.value = newValue
                                        preferenManager.edit {
                                            putString(SAVE_LOCATION, newValue)
                                        }
                                    },
                                    items = storageMapList,
                                    itemTextProvider = { it?.second.orEmpty() },
                                    title = stringResource(R.string.save_location_title),
                                    subtitle = stringResource(R.string.save_location_summary)
                                )
                                PreferenceList(
                                    style = PreferenceList.Style.Spinner,
                                    value = defaultLanguageMap.find { it.first == defaultLanguage.value },
                                    onValueChange = {
                                        Log.d(TAG, "setDefaultLanguage: ${it?.first}")
                                        val currentLang =
                                            RisuscitoApplication.localeManager.getLanguage(
                                                requireContext()
                                            )
                                        val newLanguage = it?.first ?: currentLang
                                        defaultLanguage.value = newLanguage
                                        Log.i(
                                            TAG,
                                            "OnPreferenceChangeListener - oldValue: $currentLang"
                                        )
                                        Log.i(
                                            TAG,
                                            "OnPreferenceChangeListener - newValue: $newLanguage"
                                        )
                                        if (!currentLang.equals(newLanguage, ignoreCase = true)) {
                                            progressDialogViewModel.indeterminate = false
                                            progressDialogViewModel.dialogTitleRes = 0
                                            progressDialogViewModel.dialogIconRes =
                                                R.drawable.file_download_24px
                                            progressDialogViewModel.messageRes.value =
                                                R.string.download_running
                                            progressDialogViewModel.buttonTextRes = 0
                                            progressDialogViewModel.progress.value = 0f
                                            progressDialogViewModel.showProgressDialog.value = true
                                            // Creates a request to download and install additional language resources.
                                            val request =
                                                SplitInstallRequest.newBuilder().addLanguage(
                                                    if (newLanguage == LocaleManager.LANGUAGE_ENGLISH_PHILIPPINES) Locale.Builder()
                                                        .setLanguage(LocaleManager.LANGUAGE_ENGLISH)
                                                        .setRegion(LocaleManager.COUNTRY_PHILIPPINES) // Usa setRegion per il paese
                                                        .build()
                                                    else Locale.Builder().setLanguage(newLanguage)
                                                        .build()
                                                ).build()

                                            // Submits the request to install the additional language resources.
                                            mSettingsViewModel.persistingLanguage = newLanguage
                                            splitInstallManager.startInstall(request)
                                                // You should also add the following listener to handle any errors
                                                // processing the request.
                                                ?.addOnFailureListener { exception ->
                                                    Log.e(TAG, "language download error", exception)
                                                    progressDialogViewModel.showProgressDialog.value =
                                                        false
                                                    mMainActivity?.showSnackBar("error downloading language: ${(exception as? SplitInstallException)?.errorCode}")
                                                }
                                                // When the platform accepts your request to download
                                                // an on demand module, it binds it to the following session ID.
                                                // You use this ID to track further status updates for the request.
                                                ?.addOnSuccessListener { id -> sessionId = id }
                                        }
                                    },
                                    items = defaultLanguageMap,
                                    itemTextProvider = { it?.second.orEmpty() },
                                    title = stringResource(R.string.language_title),
                                    subtitle = stringResource(R.string.language_summary)
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        splitInstallManager.registerListener(listener)
    }

    override fun onStop() {
        super.onStop()
        try {
            splitInstallManager.unregisterListener(listener)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "unregister error", e)
        }
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
                progressDialogViewModel.showProgressDialog.value = false
                mMainActivity?.showSnackBar("download cancelled by user")
            }
        }
    }

    private fun saveBooleanPreference(
        preferenceKey: String,
        preferenceValue: Boolean,
        restart: Boolean = false,
        hasNavigationBar: Boolean = false
    ) {
        Log.d(TAG, "saveBooleanPreference: $preferenceKey / $preferenceValue")
        val preferenManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val actualValue = preferenManager.getBoolean(preferenceKey, false)
        if (actualValue != preferenceValue) {
            Log.d(TAG, "saveBooleanPreference SAVE: $preferenceKey / $preferenceValue")
            preferenManager.edit {
                putBoolean(preferenceKey, preferenceValue)
            }
            if (restart) {
                recreateActivity(hasNavigationBar)
            }
        }
    }

    private fun recreateActivity(hasNavigationBar: Boolean = false) {
        mMainActivity?.let {
            if (hasNavigationBar) {
                it.recreate()
            } else ProcessPhoenix.triggerRebirth(
                it.applicationContext
            )
        }
    }

    private fun loadStorageList(external: Boolean) {
        Log.d(
            TAG,
            "loadStorageList: WRITE_EXTERNAL_STORAGE " + Utility.isExternalStorageWritable + " / " + external
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
        progressDialogViewModel.dialogTitleRes = 0
        progressDialogViewModel.dialogIconRes = R.drawable.translate_24px
        progressDialogViewModel.buttonTextRes = 0
        progressDialogViewModel.indeterminate = true
        progressDialogViewModel.messageRes.value = R.string.translation_running
        progressDialogViewModel.showProgressDialog.value = true
        val cambioAccordi = CambioAccordi(requireContext())
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            cambioAccordi.convertTabs(oldLanguage, newLanguage)
            cambioAccordi.convertiBarre(oldLanguage, newLanguage)
        }
        try {
            progressDialogViewModel.showProgressDialog.value = false
            RisuscitoApplication.localeManager.updateLanguage(
                requireContext(), newLanguage
            )
        } catch (e: IllegalArgumentException) {
            Log.e(javaClass.name, e.localizedMessage, e)
        }
    }

    companion object {
        private const val CONFIRMATION_REQUEST_CODE = 1
        private val TAG = SettingsFragment::class.java.canonicalName
    }

}
