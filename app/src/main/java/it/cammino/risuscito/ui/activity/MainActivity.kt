package it.cammino.risuscito.ui.activity

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
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
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.messaging.messaging
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.CredendialObject
import it.cammino.risuscito.ui.CredentialCacheManager
import it.cammino.risuscito.ui.RisuscitoApplication
import it.cammino.risuscito.ui.composable.dialogs.ProfileDialog
import it.cammino.risuscito.ui.composable.dialogs.ProfileMenuItem
import it.cammino.risuscito.ui.composable.dialogs.ProgressDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleDialogTag
import it.cammino.risuscito.ui.composable.hasNavigationBar
import it.cammino.risuscito.ui.composable.main.ActionModeItem
import it.cammino.risuscito.ui.composable.main.Destination
import it.cammino.risuscito.ui.composable.main.MainScreen
import it.cammino.risuscito.ui.composable.main.NavigationScreen
import it.cammino.risuscito.ui.composable.main.OptionMenuItem
import it.cammino.risuscito.ui.composable.main.RisuscitoSnackBar
import it.cammino.risuscito.ui.composable.main.StatusBarProtection
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.interfaces.ActionModeFragment
import it.cammino.risuscito.ui.interfaces.FabActionsFragment
import it.cammino.risuscito.ui.interfaces.OptionMenuFragment
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
import it.cammino.risuscito.utils.extension.queryIntentActivities
import it.cammino.risuscito.utils.extension.startActivityWithTransition
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import it.cammino.risuscito.viewmodels.MainActivityViewModel.ProfileAction
import it.cammino.risuscito.viewmodels.SharedBottomSheetViewModel
import it.cammino.risuscito.viewmodels.SharedProfileViewModel
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.SharedSearchViewModel
import it.cammino.risuscito.viewmodels.SharedTabViewModel
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.Collator


class MainActivity : ThemeableActivity() {
    private val cantiViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(application, Bundle().apply { putInt(Utility.TIPO_LISTA, 0) })
    }
    private val scrollViewModel: SharedScrollViewModel by viewModels()
    private val sharedSearchViewModel: SharedSearchViewModel by viewModels()
    private val sharedTabViewModel: SharedTabViewModel by viewModels()
    private val sharedBottomSheetViewModel: SharedBottomSheetViewModel by viewModels()

    private val sharedProfileViewModel: SharedProfileViewModel by viewModels()

    private lateinit var mCredentialCacheManager: CredentialCacheManager

    private lateinit var mCredentialManager: CredentialManager
    private var mCredentialRequest: GetCredentialRequest? = null
    private lateinit var auth: FirebaseAuth

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
    private var onActionModeClickItem: (ActionModeItem) -> Unit = {}
    private var navHostController = mutableStateOf(NavHostController(this))
    private val tabsDestinationList = MutableLiveData(ArrayList<Destination>())
    private var optionMenuFragment: OptionMenuFragment? = null
    private val optionMenuList = MutableLiveData(ArrayList<OptionMenuItem>())
    private var fabActionsFragment: FabActionsFragment? = null
    private val signedId = mutableStateOf(false)

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

                val localTabsList by tabsDestinationList.observeAsState()

                val localFabActionsList by fabActionList.observeAsState()

                val localOptionMenu = optionMenuList.observeAsState()

                val snackbarHostState = remember { SnackbarHostState() }

                val navControllerInstance = rememberNavController() // Create NavController here
                navHostController.value = navControllerInstance

                val showAlertDialog by mViewModel.showAlertDialog.observeAsState()

                val showProgressDialog by progressDialogViewModel.showProgressDialog.observeAsState()

                val hasNavigationBar = hasNavigationBar()

                MainScreen(
                    sharedScrollViewModel = sharedScrollVM,
                    navController = navHostController.value,
                    isActionMode = isActionMode.value,
                    actionModeMenu = actionModeMenuList,
                    hideNavigation = hideNavigationIcon.value,
                    onActionModeClick = onActionModeClickItem,
                    contextualTitle = actionModeTitle.value,
                    showLoadingBar = showProgressBar.value,
                    showTabs = tabsVisible.value,
                    tabsList = localTabsList,
                    resetTab = sharedTabViewModel.resetTab,
                    selectedTabIndex = sharedTabViewModel.tabsSelectedIndex,
                    snackbarHostState = snackbarHostState,
                    showFab = showFab.value,
                    sharedSearchViewModel = sharedSearchViewModel,
                    optionMenu = localOptionMenu.value,
                    onOptionMenuClick = { optionMenuFragment?.onItemClick(it) },
                    fabIconRes = fabIconRes.intValue,
                    onFabClick = { fabFragment?.onFabClick(it) },
                    fabActions = localFabActionsList,
                    fabExpanded = fabExpanded,
                    loggedIn = signedId.value,
                    profilePhotoUrl = sharedProfileViewModel.profilePhotoUrl,
                    onProfileItemClick = {
                        if (it || hasNavigationBar)
                            sharedProfileViewModel.showProfileDialog.value = true
                        else
                            signIn(false)
                    },
                    bottomSheetViewModel = sharedBottomSheetViewModel,
                    bottomSheetOnItemClick = { startExternalActivity(it) },
                    pm = packageManager,
                    cantoData = mViewModel.cantoData.value,
                    navigateBack = mViewModel.navigateBack,
                    searchBarState = searchBarState
                )

                RisuscitoSnackBar(
                    snackbarHostState = snackbarHostState,
                    callBack = snackBarFragment,
                    message = sharedSnackBarViewModel.snackbarMessage.value,
                    actionLabel = sharedSnackBarViewModel.actionLabel.value,
                    showSnackBar = sharedSnackBarViewModel.showSnackBar
                )

                if (showAlertDialog == true) {
                    SimpleAlertDialog(
                        onDismissRequest = {
                            mViewModel.showAlertDialog.postValue(false)
                        },
                        onConfirmation = { tag ->
                            mViewModel.showAlertDialog.postValue(false)
                            when (tag) {
                                SimpleDialogTag.BACKUP_ASK -> {
                                    backToHome()
                                    mViewModel.profileAction = ProfileAction.BACKUP
                                    mCredentialCacheManager.validateToken()
                                }

                                SimpleDialogTag.RESTORE_ASK -> {
                                    backToHome()
                                    mViewModel.profileAction = ProfileAction.RESTORE
                                    mCredentialCacheManager.validateToken()
                                }

                                SimpleDialogTag.SIGNOUT -> {
                                    signOut()
                                }

                                SimpleDialogTag.REVOKE -> {
                                    revokeAccess()
                                }

                                SimpleDialogTag.RESTORE_DONE -> {
                                    if (PreferenceManager.getDefaultSharedPreferences(this)
                                            .getString(Utility.SYSTEM_LANGUAGE, StringUtils.EMPTY)
                                            .isNullOrEmpty()
                                    ) RisuscitoApplication.localeManager.setDefaultSystemLanguage(
                                        this
                                    )
                                    else RisuscitoApplication.localeManager.updateLanguage(this)

                                }

                                SimpleDialogTag.PERMISSION_ASK -> {
                                    run {
                                        requestPermissionLauncher.launch(
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        )
                                    }
                                }

                                else -> {}
                            }
                        },
                        dialogTitle = mViewModel.dialogTitle.value.orEmpty(),
                        dialogText = mViewModel.content.value.orEmpty(),
                        iconRes = mViewModel.iconRes.value ?: 0,
                        confirmButtonText = mViewModel.positiveButton.value.orEmpty(),
                        dismissButtonText = mViewModel.negativeButton.value.orEmpty(),
                        dialogTag = mViewModel.dialogTag
                    )
                }

                if (showProgressDialog == true) {
                    ProgressDialog(
                        dialogTitleRes = progressDialogViewModel.dialogTitleRes,
                        messageRes = progressDialogViewModel.messageRes.value ?: 0,
                        onDismissRequest = {
                            progressDialogViewModel.showProgressDialog.value = false
                        },
                        buttonTextRes = progressDialogViewModel.buttonTextRes,
                        indeterminate = progressDialogViewModel.indeterminate
                    )
                }

                ProfileDialog(
                    viewModel = sharedProfileViewModel,
                    loggedIn = signedId.value
                ) { item ->
                    when (item) {
                        ProfileMenuItem.GDRIVE_BACKUP -> showAccountRelatedDialog(SimpleDialogTag.BACKUP_ASK)

                        ProfileMenuItem.GDRIVE_RESTORE -> showAccountRelatedDialog(SimpleDialogTag.RESTORE_ASK)

                        ProfileMenuItem.GDRIVE_REFRESH -> {
                            mViewModel.profileAction = ProfileAction.NONE
                            signIn(lastAccount = true, forceRefresh = true)
                        }

                        ProfileMenuItem.GOOGLE_SIGNOUT -> showAccountRelatedDialog(SimpleDialogTag.SIGNOUT)

                        ProfileMenuItem.GOOGLE_REVOKE -> showAccountRelatedDialog(SimpleDialogTag.REVOKE)

                        ProfileMenuItem.SETTINGS -> onSettingsItemClick(item)

                        ProfileMenuItem.ABOUT -> onSettingsItemClick(item)

                        ProfileMenuItem.SIGN_IN -> signIn(false)
                    }
                }

                // After drawing main content, draw status bar protection
                StatusBarProtection(if (isActionMode.value) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer)

                LaunchedEffect(closeSearch.value) {
                    snapshotFlow { closeSearch.value }
                        .distinctUntilChanged()
                        .collect { close ->
                            if (close) {
                                scope.launch { searchBarState.animateToCollapsed() }
                                closeSearch.value = false
                            }
                        }
                }

            }

        }

        mCredentialManager = CredentialManager.create(this)

        mCredentialCacheManager = CredentialCacheManager(mViewModel, this, mCredentialManager)

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

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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
                mViewModel.dialogTag = SimpleDialogTag.PERMISSION_ASK
                mViewModel.dialogTitle.value = "Permission"
                mViewModel.iconRes.value = R.drawable.storage_24px
                mViewModel.content.value = getString(R.string.external_storage_pref_rationale)
                mViewModel.positiveButton.value = getString(R.string.ok)
                mViewModel.negativeButton.value = getString(android.R.string.cancel)
                mViewModel.showAlertDialog.value = true
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
        mViewModel.backupRestoreState.observe(this) { state ->
            state?.let {
                when (it) {
                    MainActivityViewModel.BakupRestoreState.RESTORE_STARTED -> {
                        progressDialogViewModel.indeterminate = true
                        progressDialogViewModel.dialogTitleRes = R.string.restore_running
                        progressDialogViewModel.dialogIconRes = R.drawable.cloud_download_24px
                        progressDialogViewModel.messageRes.value = R.string.restoring_database
                        progressDialogViewModel.buttonTextRes = 0
                        progressDialogViewModel.showProgressDialog.value = true
                    }

                    MainActivityViewModel.BakupRestoreState.RESTORE_STEP_2 -> progressDialogViewModel.messageRes.value =
                        R.string.restoring_settings

                    MainActivityViewModel.BakupRestoreState.RESTORE_COMPLETED -> progressDialogViewModel.showProgressDialog.value =
                        false

                    MainActivityViewModel.BakupRestoreState.BACKUP_STARTED -> {
                        progressDialogViewModel.indeterminate = true
                        progressDialogViewModel.dialogTitleRes = R.string.backup_running
                        progressDialogViewModel.dialogIconRes = R.drawable.cloud_upload_24px
                        progressDialogViewModel.messageRes.value = R.string.backup_database
                        progressDialogViewModel.buttonTextRes = 0
                        progressDialogViewModel.showProgressDialog.value = true
                    }

                    MainActivityViewModel.BakupRestoreState.BACKUP_STEP_2 -> progressDialogViewModel.messageRes.value =
                        R.string.backup_settings

                    MainActivityViewModel.BakupRestoreState.BACKUP_COMPLETED -> progressDialogViewModel.showProgressDialog.value =
                        false

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
            sharedSearchViewModel.titoli.value = canti.sortedWith(
                compareBy(
                    Collator.getInstance(systemLocale)
                ) { getString(it.titleRes) })
        }

    }

    private fun onSettingsItemClick(item: ProfileMenuItem) {
        expandToolbar()

        val activityClass = when (item) {
            ProfileMenuItem.SETTINGS -> SettingsActivity::class.java
            else -> AboutActivity::class.java
        }

        val intent = Intent(this, activityClass)
        startActivityWithTransition(intent, MaterialSharedAxis.Y)
    }

    fun getFabExpanded(): Boolean {
        return fabExpanded.value
    }

    fun setFabExpanded(expanded: Boolean) {
        fabExpanded.value = expanded
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun expandToolbar() {
        scrollViewModel.scrollBehavior.value?.scrollOffset = 0F
    }

    fun setupMaterialTab(tabsList: List<Destination>, selectedIndex: Int = 0) {
        Log.d(TAG, "setupMaterialTab() - selectedIndex:$selectedIndex")
        val newList = ArrayList(tabsList) // Crea una nuova lista
        tabsDestinationList.value = newList
        sharedTabViewModel.tabsSelectedIndex.intValue = selectedIndex
    }

    // [START signIn]
    private fun signIn(
        lastAccount: Boolean, forceRefresh: Boolean = false
    ) {
        Log.d(TAG, "signIn -> lastAccount: $lastAccount / forceRefresh: $forceRefresh")
        // [START build_client]
        buildCredentialRequest(
            if (lastAccount) buildLastAccountCredentialOption() else {
                showProgressDialog()
                buildGoogleCredentialOption()
            }
        )
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
        mCredentialRequest =
            GetCredentialRequest.Builder().addCredentialOption(credOption).build()
    }

    private fun buildLastAccountCredentialOption(): CredentialOption {
        return GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()
    }

    private fun buildGoogleCredentialOption(): CredentialOption {
        return GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id))
            .build()
    }

    // [START signOut]
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        lifecycleScope.launch {
            logout()
        }
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

        signedId.value = signedIn
        hideProgressDialog()
    }

    /**
     * Updates the profile information (name, email, photo URL) based on the user's account.
     *
     * @param account The user's account information.
     */
    private fun updateProfileInfo(account: CredendialObject?) { // Replace YourAccountType with the actual type
        sharedProfileViewModel.profileNameStr.value = account?.getDisplayName().orEmpty()
        Log.d(TAG, "LOGIN profileName: ${sharedProfileViewModel.profileNameStr.value}")

        sharedProfileViewModel.profileEmailStr.value = account?.getAccountId().orEmpty()
        Log.d(TAG, "LOGIN profileEmail: ${sharedProfileViewModel.profileNameStr.value}")

        sharedProfileViewModel.profilePhotoUrl = account?.getProfilePictureUri()?.let { uri ->
            Log.d(TAG, "personPhotoUrl BEFORE: $uri")
            val modifiedUrl = uri.replace(OLD_PHOTO_RES, NEW_PHOTO_RES)
            Log.d(TAG, "personPhotoUrl AFTER: $modifiedUrl")
            modifiedUrl
        }.orEmpty()
    }

    /**
     * Clears the profile information (name, email, photo URL).
     */
    private fun clearProfileInfo() {
        sharedProfileViewModel.profileNameStr.value = StringUtils.EMPTY
        sharedProfileViewModel.profileEmailStr.value = StringUtils.EMPTY
        sharedProfileViewModel.profilePhotoUrl = StringUtils.EMPTY
    }

    fun showProgressDialog() {
        showProgressBar.value = true
    }

    fun hideProgressDialog() {
        showProgressBar.value = false
    }

    private fun backToHome() {
        Log.d(TAG, "backToHome")
        if (navHostController.value.currentDestination?.route == NavigationScreen.GeneralIndex.route) {
            return
        }
        navHostController.value.navigate(NavigationScreen.GeneralIndex.route) {
            popUpTo(navHostController.value.graph.startDestinationId)
            launchSingleTop = true
        }
        expandToolbar()
    }

    private fun showAccountRelatedDialog(tag: SimpleDialogTag) {
        mViewModel.dialogTag = tag
        when (tag) {
            SimpleDialogTag.BACKUP_ASK -> {
                mViewModel.dialogTitle.value = getString(R.string.gdrive_backup)
                mViewModel.iconRes.value = R.drawable.cloud_upload_24px
                mViewModel.content.value = getString(R.string.gdrive_backup_content)
                mViewModel.positiveButton.value = getString(R.string.backup_confirm)
            }

            SimpleDialogTag.RESTORE_ASK -> {
                mViewModel.dialogTitle.value = getString(R.string.gdrive_restore)
                mViewModel.iconRes.value = R.drawable.cloud_download_24px
                mViewModel.content.value = getString(R.string.gdrive_restore_content)
                mViewModel.positiveButton.value = getString(R.string.restore_confirm)
            }

            SimpleDialogTag.SIGNOUT -> {
                mViewModel.dialogTitle.value = getString(R.string.gplus_signout)
                mViewModel.iconRes.value = R.drawable.person_remove_24px
                mViewModel.content.value = getString(R.string.dialog_acc_disconn_text)
                mViewModel.positiveButton.value = getString(R.string.disconnect_confirm)
            }

            SimpleDialogTag.REVOKE -> {
                mViewModel.dialogTitle.value = getString(R.string.gplus_revoke)
                mViewModel.iconRes.value = R.drawable.person_off_24px
                mViewModel.content.value = getString(R.string.dialog_acc_revoke_text)
                mViewModel.positiveButton.value = getString(R.string.disconnect_confirm)
            }

            else -> {}
        }
        mViewModel.negativeButton.value = getString(android.R.string.cancel)
        mViewModel.showAlertDialog.value = true
    }

    private suspend fun translate() {
        Log.d(TAG, "translate")
        progressDialogViewModel.indeterminate = true
        progressDialogViewModel.dialogIconRes = R.drawable.translate_24px
        progressDialogViewModel.messageRes.value = R.string.translation_running
        progressDialogViewModel.buttonTextRes = 0
        progressDialogViewModel.dialogTitleRes = 0
        progressDialogViewModel.showProgressDialog.value = true
        intent.removeExtra(CHANGE_LANGUAGE)
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            convertTabs()
            convertiBarre()
        }
        intent.removeExtra(OLD_LANGUAGE)
        intent.removeExtra(NEW_LANGUAGE)
        progressDialogViewModel.showProgressDialog.value = false
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
                    mViewModel.sub,
                    mCredentialCacheManager.getCachedCredential()?.getAccountId()
                )
            }

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.BACKUP_COMPLETED
            mViewModel.dialogTag = SimpleDialogTag.BACKUP_DONE
            mViewModel.dialogTitle.value = getString(R.string.general_message)
            mViewModel.iconRes.value = R.drawable.cloud_done_24px
            mViewModel.content.value = getString(R.string.gdrive_backup_success)
            mViewModel.positiveButton.value = getString(R.string.ok)
            mViewModel.showAlertDialog.value = true
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
            mViewModel.dialogTag = SimpleDialogTag.RESTORE_DONE
            mViewModel.dialogTitle.value = getString(R.string.general_message)
            mViewModel.iconRes.value = R.drawable.cloud_done_24px
            mViewModel.content.value = getString(R.string.gdrive_restore_success)
            mViewModel.positiveButton.value = getString(R.string.ok)
            mViewModel.showAlertDialog.value = true
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
        onActionModeClick: (ActionModeItem) -> Unit = {}
    ) {
        hideNavigationIcon.value = hideNavigation
        actionModeFragment = fragment
        actionModeMenuList.clear()
        actionModeMenuList.addAll(actionModeMenu)
        onActionModeClickItem = onActionModeClick
        expandToolbar()
        isActionMode.value = true
    }

    fun destroyActionMode() {
        isActionMode.value = false
        actionModeFragment?.destroyActionMode()
    }

    fun createOptionsMenu(
        optionMenu: List<OptionMenuItem>,
        fragment: OptionMenuFragment?
    ) {
        Log.d(TAG, "createOptionsMenu")
        val newList = ArrayList(optionMenu) // Crea una nuova lista
        optionMenuList.value = newList
        optionMenuFragment = fragment
    }

    fun setFabActionsFragment(fragment: FabActionsFragment) {
        fabActionsFragment = fragment
    }

    fun getFabActionsFragment(): FabActionsFragment? {
        return fabActionsFragment
    }

    fun updateActionModeTitle(title: String) {
        actionModeTitle.value = title
    }

    fun showBottomSheet(titleRes: Int = 0, intent: Intent?) {
        intent?.let { mIntent ->
            val list = packageManager.queryIntentActivities(mIntent)

            val lastApp = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(Utility.ULTIMA_APP_USATA, StringUtils.EMPTY)
            val lastAppInfo: ResolveInfo? = list.indices
                .firstOrNull { list[it].activityInfo.applicationInfo.packageName == lastApp }
                ?.let { list.removeAt(it) }

            lastAppInfo?.let { list.add(it) }

            sharedBottomSheetViewModel.appList.clear()
            sharedBottomSheetViewModel.appList.addAll(list)

            sharedBottomSheetViewModel.titleTextRes.intValue = titleRes
            sharedBottomSheetViewModel.intent.value = intent

            sharedBottomSheetViewModel.showBottomSheet.value = true

        }

    }

    private fun startExternalActivity(selectedProduct: ResolveInfo) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit {
                putString(
                    Utility.ULTIMA_APP_USATA,
                    selectedProduct.activityInfo?.packageName
                )
            }

        val name = ComponentName(
            selectedProduct.activityInfo?.packageName.orEmpty(),
            selectedProduct.activityInfo?.name.orEmpty()
        )

        val newIntent = sharedBottomSheetViewModel.intent.value?.clone() as? Intent
        newIntent?.component = name
        startActivity(newIntent)
    }

    companion object {
        private const val OLD_PHOTO_RES = "s96-c"
        private const val NEW_PHOTO_RES = "s400-c"
        private val TAG = MainActivity::class.java.canonicalName

    }

}
