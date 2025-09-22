package it.cammino.risuscito.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.messaging.messaging
import com.leinardi.android.speeddial.SpeedDialView
import com.michaelflisar.changelog.ChangelogBuilder
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.CredendialObject
import it.cammino.risuscito.ui.CredentialCacheManager
import it.cammino.risuscito.ui.ProfileUiManager
import it.cammino.risuscito.ui.RisuscitoApplication
import it.cammino.risuscito.ui.composable.main.ActionModeItem
import it.cammino.risuscito.ui.composable.main.Destination
import it.cammino.risuscito.ui.composable.main.Drawer
import it.cammino.risuscito.ui.composable.main.MainScreen
import it.cammino.risuscito.ui.composable.main.NavigationScreen
import it.cammino.risuscito.ui.composable.main.RisuscitoSnackBar
import it.cammino.risuscito.ui.composable.main.StatusBarProtection
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.dialog.DialogState
import it.cammino.risuscito.ui.dialog.ProfileDialogFragment
import it.cammino.risuscito.ui.dialog.ProgressDialogFragment
import it.cammino.risuscito.ui.dialog.SimpleDialogFragment
import it.cammino.risuscito.ui.interfaces.ActionModeFragment
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.CantiXmlParser
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.Utility.CHANGE_LANGUAGE
import it.cammino.risuscito.utils.Utility.NEW_LANGUAGE
import it.cammino.risuscito.utils.Utility.OLD_LANGUAGE
import it.cammino.risuscito.utils.extension.convertTabs
import it.cammino.risuscito.utils.extension.convertiBarre
import it.cammino.risuscito.utils.extension.dynamicColorOptions
import it.cammino.risuscito.utils.extension.getVersionCode
import it.cammino.risuscito.utils.extension.isDarkMode
import it.cammino.risuscito.utils.extension.isOnTablet
import it.cammino.risuscito.utils.extension.startActivityWithTransition
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import it.cammino.risuscito.viewmodels.MainActivityViewModel.ProfileAction
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.SharedSearchViewModel
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.Collator


class MainActivity : ThemeableActivity() {
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels()
    private val profileDialogViewModel: ProfileDialogFragment.DialogViewModel by viewModels()
    private val cantiViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(application, Bundle().apply { putInt(Utility.TIPO_LISTA, 0) })
    }

    private val scrollViewModel: SharedScrollViewModel by viewModels()

    private val sharedSearchViewModel: SharedSearchViewModel by viewModels()

    private lateinit var mCredentialCacheManager: CredentialCacheManager

    private lateinit var mCredentialManager: CredentialManager
    private var mCredentialRequest: GetCredentialRequest? = null
    private lateinit var auth: FirebaseAuth
    private var profileItem: MenuItem? = null
    private var profileUiManager: ProfileUiManager? = null
    private var profilePhotoUrl: String = StringUtils.EMPTY
    private var profileNameStr: String = StringUtils.EMPTY
    private var profileEmailStr: String = StringUtils.EMPTY

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showSnackBar(getString(R.string.permission_ok))
        } else {
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit { putString(Utility.SAVE_LOCATION, "0") }
            showSnackBar(getString(R.string.external_storage_denied))
        }
    }

    var showProgressBar = mutableStateOf(false)
    var isActionMode = mutableStateOf(false)
    private var actionModeFragment: ActionModeFragment? = null
    private var actionModeTitle = mutableStateOf("")
    private var hideNavigationIcon = mutableStateOf(false)
    private var actionModeMenuList = mutableListOf<ActionModeItem>()
    private var onActionModeClickItem: (String) -> Unit = {}
    private var navHostController = mutableStateOf(NavHostController(this))
    private val drawerState = mutableStateOf(DrawerState(initialValue = DrawerValue.Closed))
    private val tabsDestinationList = MutableLiveData(ArrayList<Destination>())
    private val tabsSelectedIndex = mutableIntStateOf(0)
    private val tabsVisible = mutableStateOf(false)
    private val showSnackbar = mutableStateOf(false)
    private val snackbarMessage = mutableStateOf("")
    private val actionLabel = mutableStateOf("")
    private var snackBarFragment: SnackBarFragment? = null

    private val showFab = mutableStateOf(false)

    private lateinit var pagerState: PagerState

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        DynamicColors.applyToActivityIfAvailable(this, dynamicColorOptions)
        super.onCreate(savedInstanceState)

        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.fileout)
            sharedSearchViewModel.aTexts = CantiXmlParser().parse(inputStream)
            inputStream.close()
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Error:", e)
            Firebase.crashlytics.recordException(e)
        } catch (e: IOException) {
            Log.e(TAG, "Error:", e)
            Firebase.crashlytics.recordException(e)
        }

        // enableEdgeToEdge sets window.isNavigationBarContrastEnforced = true
        // which is used to add a translucent scrim to three-button navigation
        enableEdgeToEdge()

        setContent {

            val scope = rememberCoroutineScope()
            val searchBarState = rememberSearchBarState()

            RisuscitoTheme {

                val sharedScrollVM: SharedScrollViewModel = viewModel()

                val localTabsList = tabsDestinationList.observeAsState()

                pagerState = rememberPagerState(pageCount = {
                    localTabsList.value?.size ?: 0
                })

                val snackbarHostState = remember { SnackbarHostState() }

                val navControllerInstance = rememberNavController() // Create NavController here
                navHostController.value = navControllerInstance

                MainScreen(
                    sharedScrollViewModel = sharedScrollVM,
                    navController = navHostController.value,
                    drawerState = drawerState.value,
                    onDrawerItemClick = { onMobileDrawerItemClick(it) },
                    isActionMode = isActionMode.value,
                    actionModeMenu = actionModeMenuList,
                    hideNavigation = hideNavigationIcon.value,
                    onActionModeClick = onActionModeClickItem,
                    contextualTitle = actionModeTitle.value,
                    searchBarState = searchBarState,
                    showLoadingBar = showProgressBar.value,
                    showTabs = tabsVisible.value,
                    tabsList = localTabsList.value,
                    selectedTabIndex = tabsSelectedIndex,
                    pagerState = pagerState,
                    snackbarHostState = snackbarHostState,
                    showFab = showFab.value,
                    sharedSearchViewModel = sharedSearchViewModel
                )

                RisuscitoSnackBar(
                    snackbarHostState = snackbarHostState,
                    callBack = snackBarFragment,
                    message = snackbarMessage.value,
                    actionLabel = actionLabel.value,
                    showSnackBar = showSnackbar
                )

                // After drawing main content, draw status bar protection
                StatusBarProtection()
            }

            onBackPressedDispatcher.addCallback(this) {
                when {
//                binding.fabPager.isOpen -> binding.fabPager.close()
                    !isOnTablet && drawerState.value.isOpen -> scope.launch { drawerState.value.close() }
                    isActionMode.value -> destroyActionMode()
                    searchBarState.currentValue == SearchBarValue.Expanded -> scope.launch { searchBarState.animateToCollapsed() }
                    else -> backToHome(true)
                }
            }
        }

        mCredentialManager = CredentialManager.create(this)

        mCredentialCacheManager = CredentialCacheManager(mViewModel, this, mCredentialManager)

        //TODO
        val outValue = TypedValue()
        resources.getValue(R.dimen.horizontal_percentage_half_divider, outValue, true)
        val percentage = outValue.float

//        binding.halfGuideline?.setGuidelinePercent(percentage)

        Log.d(TAG, "getVersionCode(): ${getVersionCode()}")

        ChangelogBuilder().withUseBulletList(true) // true if you want to show bullets before each changelog row, false otherwise
            .withMinVersionToShow(getVersionCode())     // provide a number and the log will only show changelog rows for versions equal or higher than this number
            .withManagedShowOnStart(
                getSharedPreferences(
                    "com.michaelflisar.changelog", 0
                ).getInt("changelogVersion", -1) != -1
            )  // library will take care to show activity/dialog only if the changelog has new infos and will only show this new infos
            .withTitle(getString(R.string.dialog_change_title)) // provide a custom title if desired, default one is "Changelog <VERSION>"
            .withOkButtonLabel(getString(R.string.ok)) // provide a custom ok button text if desired, default one is "OK"
            .buildAndShowDialog(
                this, isDarkMode
            ) // second parameter defines, if the dialog has a dark or light theme

        if (!OSUtils.hasQ()) checkPermission()

        if (savedInstanceState == null) {
            val currentItem = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(Utility.DEFAULT_SEARCH, "0") ?: "0"
            )
            sharedSearchViewModel.advancedSearchFilter.value = currentItem != 0
        }

        if (intent.getBooleanExtra(CHANGE_LANGUAGE, false)) {
            lifecycleScope.launch { translate() }
        }

        FirebaseAnalytics.getInstance(this)

        subscribeUiChanges()

        Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, "token ok $token")
        })

    }


    private suspend fun logout() {
        mCredentialManager.clearCredentialState(ClearCredentialStateRequest())
        mCredentialCacheManager.clearCache()
        updateUI(false)
        Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Utility.SIGNED_IN, false)
        ) {
            signIn(lastAccount = true)
        }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "permission granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                MaterialAlertDialogBuilder(this).setMessage(R.string.external_storage_pref_rationale)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        run {
                            dialog.cancel()
                            requestPermissionLauncher.launch(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        }
                    }.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                    .show()
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun subscribeUiChanges() {
        simpleDialogViewModel.state.observe(this) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            BACKUP_ASK -> {
                                simpleDialogViewModel.handled = true
                                backToHome(false)
                                mViewModel.profileAction = ProfileAction.BACKUP
                                mCredentialCacheManager.validateToken()
                            }

                            RESTORE_ASK -> {
                                simpleDialogViewModel.handled = true
                                backToHome(false)
                                mViewModel.profileAction = ProfileAction.RESTORE
                                mCredentialCacheManager.validateToken()
                            }

                            SIGNOUT -> {
                                simpleDialogViewModel.handled = true
                                signOut()
                            }

                            REVOKE -> {
                                simpleDialogViewModel.handled = true
                                revokeAccess()
                            }

                            RESTORE_DONE -> {
                                simpleDialogViewModel.handled = true
                                if (PreferenceManager.getDefaultSharedPreferences(this)
                                        .getString(Utility.SYSTEM_LANGUAGE, StringUtils.EMPTY)
                                        .isNullOrEmpty()
                                ) RisuscitoApplication.localeManager.setDefaultSystemLanguage(this)
                                else RisuscitoApplication.localeManager.updateLanguage(this)

                            }
                        }
                    }

                    is DialogState.Negative -> {
                        simpleDialogViewModel.handled = true
                    }
                }
            }
        }

        profileDialogViewModel.state.observe(this) {
            Log.d(TAG, "profileDialogViewModel state $it")
            if (!profileDialogViewModel.handled) {
                if (it is DialogState.Positive) {
                    when (profileDialogViewModel.menuItemId) {
                        R.id.gdrive_backup -> {
                            showAccountRelatedDialog(BACKUP_ASK)
                        }

                        R.id.gdrive_restore -> {
                            showAccountRelatedDialog(RESTORE_ASK)
                        }

                        R.id.gdrive_refresh -> {
                            mViewModel.profileAction = ProfileAction.NONE
                            signIn(lastAccount = true, forceRefresh = true)
                        }

                        R.id.gplus_signout -> {
                            showAccountRelatedDialog(SIGNOUT)
                        }

                        R.id.gplus_revoke -> {
                            showAccountRelatedDialog(REVOKE)
                        }
                    }
                    profileDialogViewModel.handled = true
                }
            }
        }

        mViewModel.backupRestoreState.observe(this) { state ->
            state?.let {
                when (it) {
                    MainActivityViewModel.BakupRestoreState.RESTORE_STARTED -> ProgressDialogFragment.show(
                        ProgressDialogFragment.Builder(RESTORE_RUNNING).apply {
                            title = R.string.restore_running
                            icon = R.drawable.cloud_download_24px
                            content = R.string.restoring_database
                            progressIndeterminate = true
                        }, supportFragmentManager
                    )

                    MainActivityViewModel.BakupRestoreState.RESTORE_STEP_2 -> {
                        val sFragment =
                            ProgressDialogFragment.findVisible(this@MainActivity, RESTORE_RUNNING)
                        sFragment?.setContent(R.string.restoring_settings)
                    }

                    MainActivityViewModel.BakupRestoreState.RESTORE_COMPLETED -> dismissProgressDialog(
                        RESTORE_RUNNING
                    )

                    MainActivityViewModel.BakupRestoreState.BACKUP_STARTED -> ProgressDialogFragment.show(
                        ProgressDialogFragment.Builder(BACKUP_RUNNING).apply {
                            title = R.string.backup_running
                            icon = R.drawable.cloud_upload_24px
                            content = R.string.backup_database
                            progressIndeterminate = true
                        }, supportFragmentManager
                    )

                    MainActivityViewModel.BakupRestoreState.BACKUP_STEP_2 -> {
                        val sFragment =
                            ProgressDialogFragment.findVisible(this@MainActivity, BACKUP_RUNNING)
                        sFragment?.setContent(R.string.backup_settings)
                    }

                    MainActivityViewModel.BakupRestoreState.BACKUP_COMPLETED -> dismissProgressDialog(
                        BACKUP_RUNNING
                    )

                    MainActivityViewModel.BakupRestoreState.NONE -> {}
                }
            }
        }

        mViewModel.httpRequestState.observe(this) { state ->
            Log.d(TAG, "httpRequestState -> state:$state")
            state?.let {
                when (it) {
                    MainActivityViewModel.ClientState.COMPLETED -> {
                        Log.d(TAG, "httpRequestState -> mViewModel.sub:${mViewModel.sub}")
                        if (mViewModel.sub.isNotEmpty()) {
                            firebaseAuthWithGoogle()
                        } else {
                            signIn(lastAccount = true, forceRefresh = true)
                        }
                    }

                    MainActivityViewModel.ClientState.STARTED -> {}
                }
            }
        }

        mViewModel.loginState.observe(this) { state ->
            state?.let {
                when (it) {
                    MainActivityViewModel.LOGIN_STATE_STARTED -> {}
                    MainActivityViewModel.LOGIN_STATE_OK_SILENT -> {
                        updateUI(true)
                    }

                    MainActivityViewModel.LOGIN_STATE_OK -> {
                        handleSignInResult()
                    }

                    else -> handleErrorResult(it)
                }
            }
        }

        cantiViewModel.itemsResult?.observe(this) { canti ->
            sharedSearchViewModel.titoli = canti.sortedWith(
                compareBy(
                    Collator.getInstance(systemLocale)
                ) { getString(it.titleRes) })
        }

    }

    private fun onMobileDrawerItemClick(itemRoute: String) {
        expandToolbar()

        val activityClass = when (itemRoute) {
            Drawer.SETTINGS.route -> SettingsActivity::class.java
            Drawer.INFO.route -> AboutActivity::class.java
            else -> SettingsActivity::class.java
        }

        val intent = Intent(this, activityClass)
        startActivityWithTransition(intent, MaterialSharedAxis.Y)
    }

    fun setupToolbarTitle(titleResId: Int) {
        supportActionBar?.setTitle(titleResId)
    }

    fun closeFabMenu() {
        //TODO
//        if (binding.fabPager.isOpen) binding.fabPager.close()
    }

    fun toggleFabMenu() {
        //TODO
//        binding.fabPager.toggle()
    }

    fun enableFab(enable: Boolean, autoHide: Boolean = true) {
        Log.d(TAG, "enableFab: $enable")
        //TODO
//        if (enable) {
//            if (binding.fabPager.isOpen) binding.fabPager.close()
//            else {
//                val params = binding.fabPager.layoutParams as? CoordinatorLayout.LayoutParams
//                params?.behavior =
//                    if (autoHide) SpeedDialView.ScrollingViewSnackbarBehavior() else SpeedDialView.NoBehavior()
//                binding.fabPager.requestLayout()
//                binding.fabPager.show()
//            }
//        } else {
//            if (binding.fabPager.isOpen) binding.fabPager.close()
//            binding.fabPager.hide()
//            val params = binding.fabPager.layoutParams as? CoordinatorLayout.LayoutParams
//            params?.behavior = SpeedDialView.NoBehavior()
//            binding.fabPager.requestLayout()
//        }
        showFab.value = enable
    }

    fun initFab(
        optionMenu: Boolean,
        icon: Drawable,
        click: View.OnClickListener,
        action: SpeedDialView.OnActionSelectedListener?,
        customList: Boolean
    ) {
        Log.d(TAG, "initFab()")
        //TODO
//        enableFab(false)
//        binding.fabPager.setMainFabClosedDrawable(icon)
//        binding.fabPager.mainFab.rippleColor =
//            ContextCompat.getColor(this, android.R.color.transparent)
//        binding.fabPager.clearActionItems()
//        binding.fabPager.expansionMode =
//            if (isFabExpansionLeft) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP
//        enableFab(true)
//        Log.d(TAG, "initFab optionMenu: $optionMenu")
//
//        if (optionMenu) {
//            val iconColor = MaterialColors.getColor(
//                this, com.google.android.material.R.attr.colorOnPrimaryContainer, TAG
//            )
//            val backgroundColor = MaterialColors.getColor(
//                this, com.google.android.material.R.attr.colorPrimaryContainer, TAG
//            )
//
//            binding.fabPager.addActionItem(
//                SpeedDialActionItem.Builder(
//                    R.id.fab_pulisci,
//                    AppCompatResources.getDrawable(this, R.drawable.cleaning_services_24px)
//                ).setTheme(R.style.Risuscito_SpeedDialActionItem)
//                    .setLabel(getString(R.string.dialog_reset_list_title))
//                    .setFabBackgroundColor(backgroundColor).setLabelBackgroundColor(backgroundColor)
//                    .setLabelColor(iconColor).create()
//            )
//
//            binding.fabPager.addActionItem(
//                SpeedDialActionItem.Builder(
//                    R.id.fab_add_lista, AppCompatResources.getDrawable(this, R.drawable.add_24px)
//                ).setTheme(R.style.Risuscito_SpeedDialActionItem)
//                    .setLabel(getString(R.string.action_add_list))
//                    .setFabBackgroundColor(backgroundColor).setLabelBackgroundColor(backgroundColor)
//                    .setLabelColor(iconColor).create()
//            )
//
//            binding.fabPager.addActionItem(
//                SpeedDialActionItem.Builder(
//                    R.id.fab_condividi, AppCompatResources.getDrawable(this, R.drawable.share_24px)
//                ).setTheme(R.style.Risuscito_SpeedDialActionItem)
//                    .setLabel(getString(R.string.action_share))
//                    .setFabBackgroundColor(backgroundColor).setLabelBackgroundColor(backgroundColor)
//                    .setLabelColor(iconColor).create()
//            )
//
//            if (customList) {
//                binding.fabPager.addActionItem(
//                    SpeedDialActionItem.Builder(
//                        R.id.fab_condividi_file,
//                        AppCompatResources.getDrawable(this, R.drawable.attachment_24px)
//                    ).setTheme(R.style.Risuscito_SpeedDialActionItem)
//                        .setLabel(getString(R.string.action_share_file))
//                        .setFabBackgroundColor(backgroundColor)
//                        .setLabelBackgroundColor(backgroundColor).setLabelColor(iconColor).create()
//                )
//
//                binding.fabPager.addActionItem(
//                    SpeedDialActionItem.Builder(
//                        R.id.fab_edit_lista,
//                        AppCompatResources.getDrawable(this, R.drawable.edit_24px)
//                    ).setTheme(R.style.Risuscito_SpeedDialActionItem)
//                        .setLabel(getString(R.string.action_edit_list))
//                        .setFabBackgroundColor(backgroundColor)
//                        .setLabelBackgroundColor(backgroundColor).setLabelColor(iconColor).create()
//                )
//
//                binding.fabPager.addActionItem(
//                    SpeedDialActionItem.Builder(
//                        R.id.fab_delete_lista,
//                        AppCompatResources.getDrawable(this, R.drawable.delete_24px)
//                    ).setTheme(R.style.Risuscito_SpeedDialActionItem)
//                        .setLabel(getString(R.string.action_remove_list))
//                        .setFabBackgroundColor(backgroundColor)
//                        .setLabelBackgroundColor(backgroundColor).setLabelColor(iconColor).create()
//                )
//            }
//            binding.fabPager.setOnActionSelectedListener(action)
//
//        }
//        binding.fabPager.mainFab.setOnClickListener(click)
    }

//    fun getFab(): FloatingActionButton {
//        return binding.fabPager.mainFab
//    }

    fun getPagerState(): PagerState {
        return pagerState
    }

    fun setTabVisible(visible: Boolean) {
        tabsVisible.value = visible
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun expandToolbar() {
        scrollViewModel.scrollBehavior.value?.state?.heightOffset = 0F
    }

    fun setupMaterialTab(tabsList: List<Destination>) {
        val newList = ArrayList(tabsList) // Crea una nuova lista
        tabsDestinationList.value = newList
        tabsSelectedIndex.intValue = 0
    }

    fun changeMaterialTabPage(selectedIndex: Int) {
        tabsSelectedIndex.intValue = selectedIndex
    }

    val activityMainContent: View
        get() = window.decorView.findViewById(android.R.id.content)

    // [START signIn]
    private fun signIn(
        lastAccount: Boolean, forceRefresh: Boolean = false
    ) {
        Log.d(TAG, "signIn -> lastAccount: $lastAccount / forceRefresh: $forceRefresh")
        // [START build_client]
        buildCredentialRequest(if (lastAccount) buildLastAccountCredentialOption() else buildGoogleCredentialOption())
        // [END build_client]

        mCredentialRequest?.let {
            lifecycleScope.launch {
                mCredentialRequest?.let {
                    mCredentialCacheManager.getCredential(it, forceRefresh)
                }
            }
        }
    }

    private fun buildCredentialRequest(credOption: CredentialOption) {
        mCredentialRequest = GetCredentialRequest.Builder().addCredentialOption(credOption).build()
    }

    private fun buildLastAccountCredentialOption(): CredentialOption {
        return GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.default_web_client_id)).setAutoSelectEnabled(true)
            .build()
    }

    private fun buildGoogleCredentialOption(): CredentialOption {
        return GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id)).build()
    }

    // [START signOut]
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        lifecycleScope.launch {
            logout()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        profileItem = menu.findItem(R.id.account_manager)
        profileUiManager = ProfileUiManager(
            this, supportFragmentManager, profileItem?.actionView, ::signIn
        )
        return super.onCreateOptionsMenu(menu)
    }

    // [START revokeAccess]
    private fun revokeAccess() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun handleSignInResult() {
        // Handle the successfully returned credential.
        Toast.makeText(
            this, getString(
                R.string.connected_as,
                mCredentialCacheManager.getCachedCredential()?.getDisplayName()
            ), Toast.LENGTH_SHORT
        ).show()
        mCredentialCacheManager.validateToken()
    }

    private fun handleErrorResult(message: String) {
        Toast.makeText(
            this, getString(
                R.string.login_failed, -1, message
            ), Toast.LENGTH_SHORT
        ).show()
        mViewModel.sub = ""
        updateUI(false)
    }

    private fun firebaseAuthWithGoogle() {
        Log.d(
            TAG, "firebaseAuthWithGoogle: ${
                mCredentialCacheManager.getCachedCredential()?.getAccountIdToken()
            }"
        )
        mCredentialCacheManager.getCachedCredential()?.let { account ->
            val credential = GoogleAuthProvider.getCredential(account.getAccountIdToken(), null)
            auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "firebaseAuthWithGoogle:success")
                    updateUI(true)
                    when (mViewModel.profileAction) {
                        ProfileAction.BACKUP -> {
                            lifecycleScope.launch { backupDbPrefs() }
                            mViewModel.profileAction = ProfileAction.NONE
                        }

                        ProfileAction.RESTORE -> {
                            lifecycleScope.launch { restoreDbPrefs() }
                            mViewModel.profileAction = ProfileAction.NONE
                        }

                        ProfileAction.NONE -> {}
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(false)
                    Toast.makeText(
                        this, getString(
                            R.string.login_failed, -1, task.exception?.localizedMessage
                        ), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } ?: {
            Log.w(TAG, "signInWithCredential:failure")
            updateUI(false)
            Toast.makeText(
                this, getString(
                    R.string.login_failed, -1, "null account"
                ), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateUI(signedIn: Boolean) {
        Log.d(TAG, "updateUI:signedIn = $signedIn")
        // Use a more descriptive name for the shared preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Update sign-in status in shared preferences
        sharedPreferences.edit {
            putBoolean(Utility.SIGNED_IN, signedIn)
        }
        if (signedIn) {
            updateProfileInfo(mCredentialCacheManager.getCachedCredential())
        } else {
            clearProfileInfo()
        }

        updateProfileImage()
//        hideProgressDialog()
    }

    fun updateProfileImage() {
        profileUiManager?.updateProfileUi(profilePhotoUrl, profileNameStr, profileEmailStr)
    }

    /**
     * Updates the profile information (name, email, photo URL) based on the user's account.
     *
     * @param account The user's account information.
     */
    private fun updateProfileInfo(account: CredendialObject?) { // Replace YourAccountType with the actual type
        profileNameStr = account?.getDisplayName().orEmpty()
        Log.d(TAG, "LOGIN profileName: $profileNameStr")

        profileEmailStr = account?.getAccountId().orEmpty()
        Log.d(TAG, "LOGIN profileEmail: $profileEmailStr")

        profilePhotoUrl = account?.getProfilePictureUri()?.let { uri ->
            Log.d(TAG, "personPhotoUrl BEFORE: $uri")
            val modifiedUrl = uri.replace(OLD_PHOTO_RES, NEW_PHOTO_RES)
            Log.d(TAG, "personPhotoUrl AFTER: $modifiedUrl")
            modifiedUrl
        } ?: StringUtils.EMPTY
    }

    /**
     * Clears the profile information (name, email, photo URL).
     */
    private fun clearProfileInfo() {
        profileNameStr = StringUtils.EMPTY
        profileEmailStr = StringUtils.EMPTY
        profilePhotoUrl = StringUtils.EMPTY
    }

    fun showProgressDialog() {
        showProgressBar.value = true
    }

    fun hideProgressDialog() {
        showProgressBar.value = false
    }

    private fun dismissProgressDialog(tag: String) {
        val sFragment = ProgressDialogFragment.findVisible(this, tag)
        sFragment?.dismiss()
    }

    private fun backToHome(exitAlso: Boolean) {
        val myFragment =
            supportFragmentManager.findFragmentByTag(R.id.navigation_indexes.toString())
        if (myFragment != null && myFragment.isVisible) {
            if (exitAlso) finish()
            return
        }

        navHostController.value.navigate(NavigationScreen.GeneralIndex.route)

        expandToolbar()
    }

    private fun showAccountRelatedDialog(tag: String) {
        SimpleDialogFragment.show(
            SimpleDialogFragment.Builder(tag).apply {
                when (tag) {
                    BACKUP_ASK -> {
                        title(R.string.gdrive_backup)
                        icon(R.drawable.cloud_upload_24px)
                        content(R.string.gdrive_backup_content)
                        positiveButton(R.string.backup_confirm)
                    }

                    RESTORE_ASK -> {
                        title(R.string.gdrive_restore)
                        icon(R.drawable.cloud_download_24px)
                        content(R.string.gdrive_restore_content)
                        positiveButton(R.string.restore_confirm)
                    }

                    SIGNOUT -> {
                        title(R.string.gplus_signout)
                        icon(R.drawable.person_remove_24px)
                        content(R.string.dialog_acc_disconn_text)
                        positiveButton(R.string.disconnect_confirm)
                    }

                    REVOKE -> {
                        title(R.string.gplus_revoke)
                        icon(R.drawable.person_off_24px)
                        content(R.string.dialog_acc_revoke_text)
                        positiveButton(R.string.disconnect_confirm)
                    }
                }
                negativeButton(android.R.string.cancel)
            }, supportFragmentManager
        )
        lifecycleScope.launch { drawerState.value.close() }
    }

    private suspend fun translate() {
        Log.d(TAG, "translate")
        ProgressDialogFragment.show(
            ProgressDialogFragment.Builder(TRANSLATION).apply {
                content = R.string.translation_running
                progressIndeterminate = true
            }, supportFragmentManager
        )
        intent.removeExtra(CHANGE_LANGUAGE)
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            convertTabs()
            convertiBarre()
        }
        intent.removeExtra(OLD_LANGUAGE)
        intent.removeExtra(NEW_LANGUAGE)
        try {
            dismissProgressDialog(TRANSLATION)
        } catch (e: IllegalArgumentException) {
            Log.e(javaClass.name, e.localizedMessage, e)
        }
    }

    private suspend fun backupDbPrefs() {
        try {

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.BACKUP_STARTED

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                backupDatabase(mViewModel.sub)
            }

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.BACKUP_STEP_2

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                backupSharedPreferences(
                    mViewModel.sub, mCredentialCacheManager.getCachedCredential()?.getAccountId()
                )
            }

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.BACKUP_COMPLETED
            SimpleDialogFragment.show(
                SimpleDialogFragment.Builder(BACKUP_DONE).title(R.string.general_message)
                    .icon(R.drawable.cloud_done_24px).content(R.string.gdrive_backup_success)
                    .positiveButton(R.string.ok), supportFragmentManager
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.localizedMessage, e)
            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.BACKUP_COMPLETED
            showSnackBar("error: " + e.localizedMessage)
        }
    }

    private suspend fun restoreDbPrefs() {
        try {

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.RESTORE_STARTED

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                restoreDatabase(mViewModel.sub)
            }

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.RESTORE_STEP_2

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                restoreSharedPreferences(mViewModel.sub)
            }

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.RESTORE_COMPLETED
            SimpleDialogFragment.show(
                SimpleDialogFragment.Builder(RESTORE_DONE).title(R.string.general_message)
                    .icon(R.drawable.cloud_done_24px).content(R.string.gdrive_restore_success)
                    .positiveButton(R.string.ok), supportFragmentManager
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.localizedMessage, e)
            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.RESTORE_COMPLETED
            showSnackBar("error: " + e.localizedMessage)
        }
    }

    fun createActionMode(
        actionModeMenu: List<ActionModeItem>,
        fragment: ActionModeFragment,
        hideNavigation: Boolean = false,
        onActionModeClick: (String) -> Unit = {}
    ) {
        hideNavigationIcon.value = hideNavigation
        actionModeFragment = fragment
        actionModeMenuList.clear()
        actionModeMenuList.addAll(actionModeMenu)
        onActionModeClickItem = onActionModeClick
        setTransparentStatusBar(false)
//        binding.risuscitoToolbar.expand(binding.contextualToolbarContainer, binding.appBarLayout)
        expandToolbar()
        isActionMode.value = true
    }

    fun destroyActionMode(): Boolean {
        isActionMode.value = false
        setTransparentStatusBar(true)
        actionModeFragment?.destroyActionMode()
//        return binding.risuscitoToolbar.collapse(
//            binding.contextualToolbarContainer, binding.appBarLayout
//        )
        return true
    }

    fun showSnackBar(message: String, callback: SnackBarFragment? = null, label: String? = null) {
        snackBarFragment = callback
        snackbarMessage.value = message
        actionLabel.value = label ?: StringUtils.EMPTY
        showSnackbar.value = true
    }

    fun updateActionModeTitle(title: String) {
        actionModeTitle.value = title
    }

    companion object {
        private const val RESTORE_RUNNING = "RESTORE_RUNNING"
        private const val BACKUP_RUNNING = "BACKUP_RUNNING"
        private const val TRANSLATION = "TRANSLATION"
        private const val BACKUP_ASK = "BACKUP_ASK"
        private const val RESTORE_ASK = "RESTORE_ASK"
        private const val SIGNOUT = "SIGNOUT"
        private const val REVOKE = "REVOKE"
        private const val BACKUP_DONE = "BACKUP_DONE"
        private const val RESTORE_DONE = "RESTORE_DONE"
        private const val OLD_PHOTO_RES = "s96-c"
        private const val NEW_PHOTO_RES = "s400-c"
        private val TAG = MainActivity::class.java.canonicalName

    }

}
