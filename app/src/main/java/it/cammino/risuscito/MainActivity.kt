package it.cammino.risuscito

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.view.ActionMode
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.databinding.ActivityMainBinding
import it.cammino.risuscito.dialogs.DialogState
import it.cammino.risuscito.dialogs.ProgressDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_ENGLISH
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_POLISH
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_UKRAINIAN
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : ThemeableActivity() {
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels()
    private lateinit var profileIcon: IconicsDrawable
    private lateinit var mAccountHeader: AccountHeaderView
    private var acct: GoogleSignInAccount? = null
    private var mSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private var mRegularFont: Typeface? = null
    private var mMediumFont: Typeface? = null
    private var accountMenuExpanded: Boolean = false
    private lateinit var mActionBarDrawerToggle: ActionBarDrawerToggle

    private lateinit var binding: ActivityMainBinding

    var actionMode: ActionMode? = null
        private set

    private val nextStepReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Implement UI change code here once notification is received
            try {
                Log.v(TAG, BROADCAST_NEXT_STEP)
                if (intent.getStringExtra(WHICH) != null) {
                    val which = intent.getStringExtra(WHICH)
                    Log.v(TAG, "$BROADCAST_NEXT_STEP: $which")
                    if (which.equals(RESTORE, ignoreCase = true)) {
                        val sFragment =
                            ProgressDialogFragment.findVisible(this@MainActivity, RESTORE_RUNNING)
                        sFragment?.setContent(R.string.restoring_settings)
                    } else {
                        val sFragment =
                            ProgressDialogFragment.findVisible(this@MainActivity, BACKUP_RUNNING)
                        sFragment?.setContent(R.string.backup_settings)
                    }
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, e.localizedMessage, e)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Attach a callback used to capture the shared elements from this Activity to be used
        // by the container transform transition
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        // Keep system bars (status bar, navigation bar) persistent throughout the transition.
        window.sharedElementsUseOverlay = false

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.content_frame, Risuscito(), R.id.navigation_home.toString())
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            when {
                binding.searchView.onBackPressed() -> {
                }
                binding.fabPager.isOpen -> binding.fabPager.close()
                !mViewModel.isOnTablet && (binding.drawer as? DrawerLayout)?.isOpen == true -> (binding.drawer as? DrawerLayout)?.close()
                else -> backToHome(true)
            }
        }

        mRegularFont = ResourcesCompat.getFont(this, R.font.googlesans_regular)
        mMediumFont = ResourcesCompat.getFont(this, R.font.googlesans_medium)

        profileIcon = IconicsDrawable(this).apply {
            colorInt = MaterialColors.getColor(this@MainActivity, R.attr.colorPrimary, TAG)
            icon = CommunityMaterial.Icon.cmd_account_circle
            sizeDp = 56
        }

        setSupportActionBar(binding.risuscitoToolbar)

        if (intent.getBooleanExtra(Utility.DB_RESET, false)) {
            lifecycleScope.launch { translate() }
        }

        setupNavDrawer(savedInstanceState)

        binding.appBarLayout.setExpanded(true, false)

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // [END configure_signin]

        // [START build_client]
        mSignInClient = GoogleSignIn.getClient(this, gso)
        // [END build_client]

        FirebaseAnalytics.getInstance(this)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        simpleDialogViewModel.state.observe(this) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            BACKUP_ASK -> {
                                simpleDialogViewModel.handled = true
                                ProgressDialogFragment.show(
                                    ProgressDialogFragment.Builder(this, BACKUP_RUNNING)
                                        .title(R.string.backup_running)
                                        .content(R.string.backup_database)
                                        .progressIndeterminate(true),
                                    supportFragmentManager
                                )
                                backToHome(false)
                                lifecycleScope.launch { backupDbPrefs() }
                            }
                            RESTORE_ASK -> {
                                simpleDialogViewModel.handled = true
                                ProgressDialogFragment.show(
                                    ProgressDialogFragment.Builder(this, RESTORE_RUNNING)
                                        .title(R.string.restore_running)
                                        .content(R.string.restoring_database)
                                        .progressIndeterminate(true),
                                    supportFragmentManager
                                )
                                backToHome(false)
                                lifecycleScope.launch { restoreDbPrefs() }
                            }
                            SIGNOUT -> {
                                simpleDialogViewModel.handled = true
                                signOut()
                            }
                            REVOKE -> {
                                simpleDialogViewModel.handled = true
                                revokeAccess()
                            }
                            RESTART -> {
                                simpleDialogViewModel.handled = true
                                val i = baseContext
                                    .packageManager
                                    .getLaunchIntentForPackage(baseContext.packageName)
                                i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(i)
                                finish()
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        simpleDialogViewModel.handled = true
                    }
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val task = mSignInClient?.silentSignIn()
        task?.let {
            if (it.isSuccessful) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in")
                handleSignInResult(task)
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog()

                task.addOnCompleteListener { mTask: Task<GoogleSignInAccount> ->
                    Log.d(TAG, "Reconnected")
                    handleSignInResult(mTask)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "ONRESUME")
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(nextStepReceiver, IntentFilter(BROADCAST_NEXT_STEP))
        hideProgressDialog()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "ONPAUSE")
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(nextStepReceiver)
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values, which need to be saved from the accountHeader to the bundle
        if (::mAccountHeader.isInitialized) {
            outState = mAccountHeader.saveInstanceState(outState)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.fabPager.expansionMode =
            if (mViewModel.mLUtils.isFabExpansionLeft) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP
    }

    private fun setupNavDrawer(savedInstanceState: Bundle?) {

        val profile = ProfileDrawerItem().apply {
            nameText = ""
            descriptionText = ""
            icon = ImageHolder(profileIcon)
            identifier = PROF_ID
            typeface = mRegularFont
        }
        updateHeaderImage(ImageHolder(profileIcon), null)

        mAccountHeader = AccountHeaderView(this).apply {
            if (!mViewModel.isTabletWithNoFixedDrawer)
                binding.navigationView.addHeaderView(this)
            selectionListEnabledForSingleProfile = false
            profileImagesClickable = false
            mRegularFont?.let {
                nameTypeface = it
                emailTypeface = it
            }
            addProfiles(profile)
            withSavedInstance(savedInstanceState)
        }

        binding.navigationView.setNavigationItemSelectedListener {
            onDrawerItemClick(it)
        }

//        syncDrawerRailSelectedItem(mViewModel.selectedMenuItemId)
        binding.navigationView.menu.findItem(mViewModel.selectedMenuItemId)?.isChecked = true

        if (!mViewModel.isOnTablet) {
            mActionBarDrawerToggle = ActionBarDrawerToggle(
                this,
                binding.drawer as DrawerLayout,
                binding.risuscitoToolbar,
                R.string.material_drawer_open,
                R.string.material_drawer_close
            )
            mActionBarDrawerToggle.syncState()
            (binding.drawer as? DrawerLayout)?.addDrawerListener(mActionBarDrawerToggle)
        }
    }

    private fun onDrawerItemClick(menuItem: MenuItem): Boolean {
        expandToolbar()

        val fragment = when (menuItem.itemId) {
            R.id.navigation_home -> Risuscito()
            R.id.navigation_search -> SearchFragment()
            R.id.navigation_indexes -> GeneralIndex()
            R.id.navigation_lists -> CustomLists()
            R.id.navigation_favorites -> FavoritesFragment()
            R.id.navigation_settings -> SettingsFragment()
            R.id.navigation_changelog -> AboutFragment()
            R.id.navigation_consegnati -> ConsegnatiFragment()
            R.id.navigation_history -> HistoryFragment()
            R.id.gdrive_backup -> {
                showAccountRelatedDialog(BACKUP_ASK)
                return false
            }
            R.id.gdrive_restore -> {
                showAccountRelatedDialog(RESTORE_ASK)
                return false
            }
            R.id.gplus_signout -> {
                showAccountRelatedDialog(SIGNOUT)
                return false
            }
            R.id.gplus_revoke -> {
                showAccountRelatedDialog(REVOKE)
                return false
            }
            else -> Risuscito()
        }

        mViewModel.selectedMenuItemId = menuItem.itemId
        menuItem.isChecked = true

        (binding.drawer as? DrawerLayout)?.close()

        // creo il nuovo fragment solo se non è lo stesso che sto già visualizzando
        val myFragment = supportFragmentManager
            .findFragmentByTag(menuItem.itemId.toString())
        if (myFragment == null || !myFragment.isVisible) {
            Timer("SettingUp", false).schedule(if (mViewModel.isOnTablet) 0 else 300) {
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.animate_slide_in_left, R.anim.animate_slide_out_right
                    )
                    replace(R.id.content_frame, fragment, menuItem.itemId.toString())
                }
            }
        }



        return true
    }

    // converte gli accordi salvati dalla lingua vecchia alla nuova
    private fun convertTabs() {
        val conversion = intent.getStringExtra(Utility.CHANGE_LANGUAGE)

        var accordi1 = CambioAccordi.accordi_it
        Log.d(TAG, "convertTabs - from: ${conversion?.substring(0, 2)}")
        when (conversion?.substring(0, 2)) {
            LANGUAGE_UKRAINIAN -> accordi1 = CambioAccordi.accordi_uk
            LANGUAGE_POLISH -> accordi1 = CambioAccordi.accordi_pl
            LANGUAGE_ENGLISH -> accordi1 = CambioAccordi.accordi_en
        }

        var accordi2 = CambioAccordi.accordi_it
        Log.d(TAG, "convertTabs - to: ${conversion?.substring(3, 5)}")
        when (conversion?.substring(3, 5)) {
            LANGUAGE_UKRAINIAN -> accordi2 = CambioAccordi.accordi_uk
            LANGUAGE_POLISH -> accordi2 = CambioAccordi.accordi_pl
            LANGUAGE_ENGLISH -> accordi2 = CambioAccordi.accordi_en
        }

        val mappa = HashMap<String, String>()
        for (i in CambioAccordi.accordi_it.indices) mappa[accordi1[i]] = accordi2[i]

        val mDao = RisuscitoDatabase.getInstance(this).cantoDao()
        val canti = mDao.allByName
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
                            + mappa[canto.savedTab ?: ""]
                )
                canto.savedTab = mappa[canto.savedTab ?: ""]
                mDao.updateCanto(canto)
            }
        }
    }

    // converte gli accordi salvati dalla lingua vecchia alla nuova
    private fun convertiBarre() {
        val conversion = intent.getStringExtra(Utility.CHANGE_LANGUAGE)

        var barre1 = CambioAccordi.barre_it
        Log.d(TAG, "convertiBarre - from: ${conversion?.substring(0, 2)}")
        when (conversion?.substring(0, 2)) {
            LANGUAGE_ENGLISH -> barre1 = CambioAccordi.barre_en
        }

        var barre2 = CambioAccordi.barre_it
        Log.d(TAG, "convertiBarre - to: ${conversion?.substring(3, 5)}")
        when (conversion?.substring(3, 5)) {
            LANGUAGE_ENGLISH -> barre2 = CambioAccordi.barre_en
        }

        val mappa = HashMap<String, String>()
        for (i in CambioAccordi.barre_it.indices) mappa[barre1[i]] = barre2[i]

        val mDao = RisuscitoDatabase.getInstance(this).cantoDao()
        val canti = mDao.allByName
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

    fun setupToolbarTitle(titleResId: Int) {
        supportActionBar?.setTitle(titleResId)
    }

    fun closeFabMenu() {
        if (binding.fabPager.isOpen)
            binding.fabPager.close()
    }

    fun toggleFabMenu() {
        binding.fabPager.toggle()
    }

    fun enableFab(enable: Boolean) {
        Log.d(TAG, "enableFab: $enable")
        if (enable) {
            if (binding.fabPager.isOpen)
                binding.fabPager.close()
            else {
                val params = binding.fabPager.layoutParams as? CoordinatorLayout.LayoutParams
                params?.behavior = SpeedDialView.ScrollingViewSnackbarBehavior()
                binding.fabPager.requestLayout()
                binding.fabPager.show()
            }
        } else {
            if (binding.fabPager.isOpen)
                binding.fabPager.close()
            binding.fabPager.hide()
            val params = binding.fabPager.layoutParams as? CoordinatorLayout.LayoutParams
            params?.behavior = SpeedDialView.NoBehavior()
            binding.fabPager.requestLayout()
        }
    }

    fun initFab(
        optionMenu: Boolean,
        icon: Drawable,
        click: View.OnClickListener,
        action: SpeedDialView.OnActionSelectedListener?,
        customList: Boolean
    ) {
        Log.d(TAG, "initFab()")
        enableFab(false)
        binding.fabPager.setMainFabClosedDrawable(icon)
        val colorPrimaryContainer = MaterialColors.getColor(this, R.attr.colorPrimaryContainer, TAG)
        binding.fabPager.mainFabOpenedBackgroundColor = colorPrimaryContainer
        binding.fabPager.mainFabClosedBackgroundColor = colorPrimaryContainer
        binding.fabPager.clearActionItems()
        binding.fabPager.expansionMode =
            if (mViewModel.mLUtils.isFabExpansionLeft) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP
        enableFab(true)
        Log.d(TAG, "initFab optionMenu: $optionMenu")

        if (optionMenu) {
            val iconColor = MaterialColors.getColor(this, R.attr.colorOnSecondaryContainer, TAG)
            val backgroundColor = MaterialColors.getColor(this, R.attr.colorSecondaryContainer, TAG)

            binding.fabPager.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_pulisci,
                    IconicsDrawable(this, CommunityMaterial.Icon.cmd_eraser_variant).apply {
                        sizeDp = 24
                        paddingDp = 4
                    }
                )
                    .setTheme(R.style.Risuscito_SpeedDialActionItem)
                    .setLabel(getString(R.string.dialog_reset_list_title))
                    .setFabBackgroundColor(backgroundColor)
                    .setLabelBackgroundColor(backgroundColor)
                    .setLabelColor(iconColor)
                    .create()
            )

            binding.fabPager.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_add_lista,
                    IconicsDrawable(this, CommunityMaterial.Icon3.cmd_plus).apply {
                        sizeDp = 24
                        paddingDp = 4
                    }
                )
                    .setTheme(R.style.Risuscito_SpeedDialActionItem)
                    .setLabel(getString(R.string.action_add_list))
                    .setFabBackgroundColor(backgroundColor)
                    .setLabelBackgroundColor(backgroundColor)
                    .setLabelColor(iconColor)
                    .create()
            )

            binding.fabPager.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_condividi,
                    IconicsDrawable(this, CommunityMaterial.Icon3.cmd_share_variant).apply {
                        sizeDp = 24
                        paddingDp = 4
                    }
                )
                    .setTheme(R.style.Risuscito_SpeedDialActionItem)
                    .setLabel(getString(R.string.action_share))
                    .setFabBackgroundColor(backgroundColor)
                    .setLabelBackgroundColor(backgroundColor)
                    .setLabelColor(iconColor)
                    .create()
            )

            if (customList) {
                binding.fabPager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_condividi_file,
                        IconicsDrawable(this, CommunityMaterial.Icon.cmd_attachment).apply {
                            sizeDp = 24
                            paddingDp = 4
                        }
                    )
                        .setTheme(R.style.Risuscito_SpeedDialActionItem)
                        .setLabel(getString(R.string.action_share_file))
                        .setFabBackgroundColor(backgroundColor)
                        .setLabelBackgroundColor(backgroundColor)
                        .setLabelColor(iconColor)
                        .create()
                )

                binding.fabPager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_edit_lista,
                        IconicsDrawable(this, CommunityMaterial.Icon3.cmd_pencil).apply {
                            sizeDp = 24
                            paddingDp = 4
                        }
                    )
                        .setTheme(R.style.Risuscito_SpeedDialActionItem)
                        .setLabel(getString(R.string.action_edit_list))
                        .setFabBackgroundColor(backgroundColor)
                        .setLabelBackgroundColor(backgroundColor)
                        .setLabelColor(iconColor)
                        .create()
                )

                binding.fabPager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_delete_lista,
                        IconicsDrawable(this, CommunityMaterial.Icon.cmd_delete).apply {
                            sizeDp = 24
                            paddingDp = 4
                        }
                    )
                        .setTheme(R.style.Risuscito_SpeedDialActionItem)
                        .setLabel(getString(R.string.action_remove_list))
                        .setFabBackgroundColor(backgroundColor)
                        .setLabelBackgroundColor(backgroundColor)
                        .setLabelColor(iconColor)
                        .create()
                )
            }
            binding.fabPager.setOnActionSelectedListener(action)

        }
        binding.fabPager.mainFab.setOnClickListener(click)
    }

    fun getFab(): FloatingActionButton {
        return binding.fabPager.mainFab
    }

    fun setTabVisible(visible: Boolean) {
        binding.materialTabs.isVisible = visible
    }

    fun expandToolbar() {
        binding.appBarLayout.setExpanded(true, true)
    }

    fun getMaterialTabs(): TabLayout {
        return binding.materialTabs
    }

    val activityDrawer: DrawerLayout?
        get() = binding.drawer as? DrawerLayout

    val activityBottomBar: BottomAppBar
        get() = binding.bottomBar

    val activitySearchView: SimpleSearchView
        get() = binding.searchView

    val activityMainContent: View
        get() = binding.mainContent

    val activityToolbar: MaterialToolbar
        get() = binding.risuscitoToolbar

    fun enableBottombar(enabled: Boolean) {
        Log.d(TAG, "enableBottombar - enabled: $enabled")
        if (enabled)
            mViewModel.mLUtils.animateIn(binding.bottomBar)
        else
            mViewModel.mLUtils.animateOut(binding.bottomBar)
    }

    // [START signIn]
    fun signIn() {
        val signInIntent = mSignInClient?.signInIntent
        startSignInForResult.launch(signInIntent)
    }

    private val startSignInForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.data))
        }

    // [START signOut]
    private fun signOut() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit { putBoolean(Utility.SIGN_IN_REQUESTED, false) }
        FirebaseAuth.getInstance().signOut()
        mSignInClient?.signOut()?.addOnCompleteListener {
            updateUI(false)
            Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT)
                .show()
        }
    }

    // [START revokeAccess]
    private fun revokeAccess() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit { putBoolean(Utility.SIGN_IN_REQUESTED, false) }
        FirebaseAuth.getInstance().signOut()
        mSignInClient?.revokeAccess()?.addOnCompleteListener {
            updateUI(false)
            Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT)
                .show()
        }
    }

    // [START handleSignInResult]
    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        //    Log.d(getClass().getName(), "handleSignInResult:" + result.isSuccess());
        Log.d(TAG, "handleSignInResult:" + task.isSuccessful)
        if (task.isSuccessful) {
            // Signed in successfully, show authenticated UI.
            acct = GoogleSignIn.getLastSignedInAccount(this)
            firebaseAuthWithGoogle()
        } else {
            // Sign in failed, handle failure and update UI
            Log.w(TAG, "handleSignInResult:failure", task.exception)
            if (PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(Utility.SIGN_IN_REQUESTED, false)
            )
                Toast.makeText(
                    this, getString(
                        R.string.login_failed,
                        -1,
                        task.exception?.localizedMessage
                    ), Toast.LENGTH_SHORT
                )
                    .show()
            acct = null
            updateUI(false)
        }
    }

    private fun firebaseAuthWithGoogle() {
        Log.d(TAG, "firebaseAuthWithGoogle: ${acct?.idToken}")

        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "firebaseAuthWithGoogle:success")
                    if (mViewModel.showSnackbar) {
                        Toast.makeText(
                            this,
                            getString(R.string.connected_as, acct?.displayName),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        mViewModel.showSnackbar = false
                    }
                    updateUI(true)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (PreferenceManager.getDefaultSharedPreferences(this)
                            .getBoolean(Utility.SIGN_IN_REQUESTED, false)
                    )
                        Toast.makeText(
                            this, getString(
                                R.string.login_failed,
                                -1,
                                task.exception?.localizedMessage
                            ), Toast.LENGTH_SHORT
                        )
                            .show()
                }
            }
    }

    private fun updateUI(signedIn: Boolean) {
        mViewModel.signedIn.value = signedIn
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit { putBoolean(Utility.SIGNED_IN, signedIn) }
        if (signedIn)
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit { putBoolean(Utility.SIGN_IN_REQUESTED, true) }
        if (signedIn) {

            val listener = View.OnClickListener {
                if (accountMenuExpanded)
                    resetNavigationDrawerContent()
                else
                    setAccountOptionsContent()
            }

            val profile: IProfile
            val profilePhoto = acct?.photoUrl
            if (profilePhoto != null) {
                var personPhotoUrl = profilePhoto.toString()
                Log.d(TAG, "personPhotoUrl BEFORE $personPhotoUrl")
                personPhotoUrl = personPhotoUrl.replace(OLD_PHOTO_RES, NEW_PHOTO_RES)
                Log.d(TAG, "personPhotoUrl AFTER $personPhotoUrl")
                profile = ProfileDrawerItem().apply {
                    nameText = acct?.displayName as? CharSequence ?: ""
                    descriptionText = acct?.email as? CharSequence ?: ""
                    icon = ImageHolder(personPhotoUrl)
                    identifier = PROF_ID
                    typeface = mRegularFont
                }
                updateHeaderImage(ImageHolder(personPhotoUrl), listener)
            } else {
                profile = ProfileDrawerItem().apply {
                    nameText = acct?.displayName as? CharSequence ?: ""
                    descriptionText = acct?.email as? CharSequence ?: ""
                    icon = ImageHolder(profileIcon)
                    identifier = PROF_ID
                    typeface = mRegularFont
                }
                updateHeaderImage(ImageHolder(profileIcon), listener)
            }
            // Create the AccountHeader
            mAccountHeader.updateProfile(profile)
            if (mAccountHeader.profiles?.size == 1) {
                mAccountHeader.addProfiles(
                    //fake item to make the arrow appear
                    ProfileSettingDrawerItem()
                )
            }



            mAccountHeader.setOnClickListener(listener)

        } else {
            val profile = ProfileDrawerItem().apply {
                nameText = ""
                descriptionText = ""
                icon = ImageHolder(profileIcon)
                identifier = PROF_ID
                typeface = mRegularFont
            }
            mAccountHeader.clear()
            mAccountHeader.addProfiles(profile)
            mAccountHeader.setOnClickListener(null)
            updateHeaderImage(ImageHolder(profileIcon), null)
        }
        hideProgressDialog()
    }

    private fun updateHeaderImage(icon: ImageHolder, listener: View.OnClickListener?) {
        ImageHolder.applyToOrSetInvisible(
            icon,
            findViewById(R.id.header_profileIcon),
            DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name
        )
        findViewById<ImageView>(R.id.header_profileIcon)?.setOnClickListener(listener)
    }

    private fun setAccountOptionsContent() {
        accountMenuExpanded = true
        binding.navigationView.let {
            it.menu.clear()
            it.inflateMenu(R.menu.account_options)
            it.checkedItem?.isChecked = false
        }
        mAccountHeader.accountSwitcherArrow.clearAnimation()
        ViewCompat.animate(mAccountHeader.accountSwitcherArrow).rotation(180f).start()
    }

    private fun resetNavigationDrawerContent() {
        accountMenuExpanded = false
        binding.navigationView.menu.clear()
        binding.navigationView.inflateMenu(R.menu.navigation_drawer)
//        syncDrawerRailSelectedItem(mViewModel.selectedMenuItemId)
        binding.navigationView.menu.findItem(mViewModel.selectedMenuItemId)?.isChecked = true
        mAccountHeader.accountSwitcherArrow.clearAnimation()
        ViewCompat.animate(mAccountHeader.accountSwitcherArrow).rotation(0f).start()
    }

    fun showProgressDialog() {
        binding.loadingBar.isVisible = true
    }

    fun hideProgressDialog() {
        binding.loadingBar.isVisible = false
    }

    private fun dismissProgressDialog(tag: String) {
        val sFragment = ProgressDialogFragment.findVisible(this, tag)
        sFragment?.dismiss()
    }

    private fun backToHome(exitAlso: Boolean) {
        val myFragment = supportFragmentManager.findFragmentByTag(R.id.navigation_home.toString())
        if (myFragment != null && myFragment.isVisible) {
            if (exitAlso)
                finish()
            return
        }

        binding.navigationView.menu.findItem(R.id.navigation_home)?.isChecked = true

        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.animate_slide_in_left, R.anim.animate_slide_out_right
            )
            replace(R.id.content_frame, Risuscito(), R.id.navigation_home.toString())
        }

        expandToolbar()
    }

    private fun showAccountRelatedDialog(tag: String) {
        SimpleDialogFragment.show(
            SimpleDialogFragment.Builder(this, tag).apply {
                when (tag) {
                    BACKUP_ASK -> {
                        title(R.string.gdrive_backup)
                        content(R.string.gdrive_backup_content)
                        positiveButton(R.string.backup_confirm)
                    }
                    RESTORE_ASK -> {
                        title(R.string.gdrive_restore)
                        content(R.string.gdrive_restore_content)
                        positiveButton(R.string.restore_confirm)
                    }
                    SIGNOUT -> {
                        title(R.string.gplus_signout)
                        content(R.string.dialog_acc_disconn_text)
                        positiveButton(R.string.disconnect_confirm)
                    }
                    REVOKE -> {
                        title(R.string.gplus_revoke)
                        content(R.string.dialog_acc_revoke_text)
                        positiveButton(R.string.disconnect_confirm)
                    }
                }
                negativeButton(android.R.string.cancel)
            },
            supportFragmentManager
        )
        resetNavigationDrawerContent()
        (binding.drawer as? DrawerLayout)?.close()
    }

    private suspend fun translate() {
        ProgressDialogFragment.show(
            ProgressDialogFragment.Builder(this, "TRANSLATION")
                .content(R.string.translation_running)
                .progressIndeterminate(true),
            supportFragmentManager
        )
        intent.removeExtra(Utility.DB_RESET)
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            convertTabs()
            convertiBarre()
        }
        intent.removeExtra(Utility.CHANGE_LANGUAGE)
        try {
            dismissProgressDialog("TRANSLATION")
        } catch (e: IllegalArgumentException) {
            Log.e(javaClass.name, e.localizedMessage, e)
        }
    }

    private suspend fun backupDbPrefs() {
        try {
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                backupDatabase(acct?.id)
            }

            val intentBroadcast = Intent(BROADCAST_NEXT_STEP)
            intentBroadcast.putExtra(WHICH, "BACKUP")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                backupSharedPreferences(acct?.id, acct?.email)
            }

            dismissProgressDialog(BACKUP_RUNNING)
            Snackbar.make(
                binding.mainContent,
                R.string.gdrive_backup_success,
                Snackbar.LENGTH_LONG
            )
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.localizedMessage, e)
            Snackbar.make(
                binding.mainContent,
                "error: " + e.localizedMessage,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private suspend fun restoreDbPrefs() {
        try {
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                restoreDatabase(acct?.id)
            }

            val intentBroadcast = Intent(BROADCAST_NEXT_STEP)
            intentBroadcast.putExtra(WHICH, RESTORE)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                restoreSharedPreferences(acct?.id)
            }

            dismissProgressDialog(RESTORE_RUNNING)
            SimpleDialogFragment.show(
                SimpleDialogFragment.Builder(this, RESTART)
                    .title(R.string.general_message)
                    .content(R.string.gdrive_restore_success)
                    .positiveButton(R.string.ok),
                supportFragmentManager
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.localizedMessage, e)
            Snackbar.make(binding.mainContent, "error: " + e.localizedMessage, Snackbar.LENGTH_LONG)
                .show()
        }
    }

    fun createActionMode(callback: ActionMode.Callback) {
        actionMode?.finish()
        actionMode = startSupportActionMode(callback)
    }

    fun destroyActionMode() {
        actionMode = null
    }

    fun updateActionModeTitle(title: String) {
        actionMode?.title = title
    }

    companion object {
        /* Request code used to invoke sign in user interactions. */
        private const val PROF_ID = 5428471L
        private const val BROADCAST_NEXT_STEP = "BROADCAST_NEXT_STEP"
        private const val WHICH = "WHICH"
        private const val RESTORE_RUNNING = "RESTORE_RUNNING"
        private const val BACKUP_RUNNING = "BACKUP_RUNNING"
        private const val BACKUP_ASK = "BACKUP_ASK"
        private const val RESTORE_ASK = "RESTORE_ASK"
        private const val SIGNOUT = "SIGNOUT"
        private const val REVOKE = "REVOKE"
        private const val RESTART = "RESTART"
        private const val RESTORE = "RESTORE"
        private const val OLD_PHOTO_RES = "s96-c"
        private const val NEW_PHOTO_RES = "s400-c"
        private val TAG = MainActivity::class.java.canonicalName

    }
}
