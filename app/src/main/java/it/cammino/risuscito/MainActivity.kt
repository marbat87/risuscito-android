package it.cammino.risuscito

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.view.ActionMode
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat.START
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.color.MaterialColors
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.mikepenz.crossfader.Crossfader
import com.mikepenz.crossfader.util.UIUtils.convertDpToPixel
import com.mikepenz.crossfader.view.CrossFadeSlidingPaneLayout
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.setItems
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.mikepenz.materialdrawer.widget.MiniDrawerSliderView
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.databinding.ActivityMainBinding
import it.cammino.risuscito.dialogs.DialogState
import it.cammino.risuscito.dialogs.ProgressDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.ui.CrossfadeWrapper
import it.cammino.risuscito.ui.GmailDrawerItem
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_ENGLISH
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_POLISH
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_UKRAINIAN
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.utils.ThemeUtils.Companion.getStatusBarDefaultColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.max
import kotlin.math.min

class MainActivity : ThemeableActivity() {
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels()
    private lateinit var profileIcon: IconicsDrawable
    private lateinit var mAccountHeader: AccountHeaderView
    private lateinit var miniSliderView: MiniDrawerSliderView
    private lateinit var sliderView: MaterialDrawerSliderView
    private lateinit var crossFader: Crossfader<*>
    private var acct: GoogleSignInAccount? = null
    private var mSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private var mRegularFont: Typeface? = null
    private var mMediumFont: Typeface? = null
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
                        val sFragment = ProgressDialogFragment.findVisible(this@MainActivity, RESTORE_RUNNING)
                        sFragment?.setContent(R.string.restoring_settings)
                    } else {
                        val sFragment = ProgressDialogFragment.findVisible(this@MainActivity, BACKUP_RUNNING)
                        sFragment?.setContent(R.string.backup_settings)
                    }
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, e.localizedMessage, e)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        hasNavDrawer = true
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this) {
            when {
                binding.searchView.onBackPressed() -> {
                }
                binding.fabPager.isOpen -> binding.fabPager.close()
                mViewModel.isTabletWithNoFixedDrawer && crossFader.isCrossFaded() -> crossFader.crossFade()
                !mViewModel.isOnTablet && (binding.drawer as? DrawerLayout)?.isDrawerOpen(START) == true -> (binding.drawer as? DrawerLayout)?.closeDrawer(START)
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
        if (!LUtils.hasL() && ThemeUtils.isDarkMode(this)) {
            val elevatedSurfaceColor = ElevationOverlayProvider(this).compositeOverlayWithThemeSurfaceColorIfNeeded(resources.getDimension(R.dimen.design_appbar_elevation))
            binding.appBarLayout.background = ColorDrawable(elevatedSurfaceColor)
        }

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
                                ProgressDialogFragment.show(ProgressDialogFragment.Builder(this, BACKUP_RUNNING)
                                        .title(R.string.backup_running)
                                        .content(R.string.backup_database)
                                        .progressIndeterminate(true),
                                        supportFragmentManager)
                                backToHome(false)
                                lifecycleScope.launch { backupDbPrefs() }
                            }
                            RESTORE_ASK -> {
                                simpleDialogViewModel.handled = true
                                ProgressDialogFragment.show(ProgressDialogFragment.Builder(this, RESTORE_RUNNING)
                                        .title(R.string.restore_running)
                                        .content(R.string.restoring_database)
                                        .progressIndeterminate(true),
                                        supportFragmentManager)
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
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(nextStepReceiver, IntentFilter(BROADCAST_NEXT_STEP))
        hideProgressDialog()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "ONPAUSE")
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(nextStepReceiver)
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values which need to be saved from the drawer to the bundle
        binding.slider?.let { outState = it.saveInstanceState(outState) }
        //add the values, which need to be saved from the drawer to the bundle
        if (::sliderView.isInitialized) {
            outState = sliderView.saveInstanceState(outState)
        }
        //add the values, which need to be saved from the accountHeader to the bundle
        if (::mAccountHeader.isInitialized) {
            outState = mAccountHeader.saveInstanceState(outState)
        }
        //add the values, which need to be saved from the crossFader to the bundle
        if (::crossFader.isInitialized) {
            outState = crossFader.saveInstanceState(outState)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.fabPager.expansionMode = if (mViewModel.mLUtils.isFabExpansionLeft) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP
    }

    private fun setupNavDrawer(savedInstanceState: Bundle?) {

        val profile = ProfileDrawerItem().apply {
            nameText = ""
            descriptionText = ""
            icon = ImageHolder(profileIcon)
            identifier = PROF_ID
            typeface = mRegularFont
        }

        mAccountHeader = AccountHeaderView(this).apply {
            binding.slider?.let { attachToSliderView(it) }
            selectionListEnabledForSingleProfile = false
            profileImagesClickable = false
            mRegularFont?.let {
                nameTypeface = it
                emailTypeface = it
            }
            addProfiles(profile)
            onAccountHeaderListener = { _, profile, _ ->
                //sample usage of the onProfileChanged listener
                //if the clicked item has the identifier 1 add a new profile ;)
                if (profile is IDrawerItem<*>) {
                    when (profile.identifier) {
                        R.id.gdrive_backup.toLong() -> showAccountRelatedDialog(BACKUP_ASK)
                        R.id.gdrive_restore.toLong() -> showAccountRelatedDialog(RESTORE_ASK)
                        R.id.gplus_signout.toLong() -> showAccountRelatedDialog(SIGNOUT)
                        R.id.gplus_revoke.toLong() -> showAccountRelatedDialog(REVOKE)
                    }
                }
                //false if you have not consumed the event and it should close the drawer
                false
            }
            withSavedInstance(savedInstanceState)
        }

        val drawerArrowColor = if (ThemeUtils.isDarkMode(this)) MaterialColors.getColor(this, R.attr.colorControlNormal, TAG) else
            MaterialColors.getColor(this, R.attr.colorOnPrimarySurface, TAG)

        if (mViewModel.isOnTablet) {
            sliderView = MaterialDrawerSliderView(this).apply {
                accountHeader = mAccountHeader
                customWidth = MATCH_PARENT
                setItems(
                        GmailDrawerItem().apply {
                            nameRes = R.string.activity_homepage
                            iconicsIcon = CommunityMaterial.Icon2.cmd_home
                            identifier = R.id.navigation_home.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.search_name_text
                            iconicsIcon = CommunityMaterial.Icon2.cmd_magnify
                            identifier = R.id.navigation_search.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_general_index
                            iconicsIcon = CommunityMaterial.Icon2.cmd_view_list
                            identifier = R.id.navigation_indexes.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_custom_lists
                            iconicsIcon = CommunityMaterial.Icon2.cmd_view_carousel
                            identifier = R.id.navitagion_lists.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.action_favourites
                            iconicsIcon = CommunityMaterial.Icon2.cmd_star
                            identifier = R.id.navigation_favorites.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_consegnati
                            iconicsIcon = CommunityMaterial.Icon.cmd_clipboard_check
                            identifier = R.id.navigation_consegnati.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_history
                            iconicsIcon = CommunityMaterial.Icon2.cmd_history
                            identifier = R.id.navigation_history.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_settings
                            iconicsIcon = CommunityMaterial.Icon.cmd_cog
                            identifier = R.id.navigation_settings.toLong()
                            typeface = mMediumFont
                        },
                        DividerDrawerItem(),
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_about
                            iconicsIcon = CommunityMaterial.Icon2.cmd_information_outline
                            identifier = R.id.navigation_changelog.toLong()
                            typeface = mMediumFont
                        }
                )
                onDrawerItemClickListener = { _, drawerItem, position ->
                    onDrawerItemClick(drawerItem, position)
                }
                setSavedInstance(savedInstanceState)
            }

            if (savedInstanceState == null)
                sliderView.setSelectionAtPosition(1, false)

            if (mViewModel.isTabletWithFixedDrawer) {
                binding.fixedDrawerContent?.addView(sliderView)
            } else {
                miniSliderView = MiniDrawerSliderView(this).apply {
                    drawer = sliderView
                }
                //get the widths in px for the first and second panel
                val firstWidth = convertDpToPixel(302f, this).toInt()
                val secondWidth = convertDpToPixel(72f, this).toInt()
                //create and build our crossfader (see the MiniDrawer is also builded in here, as the build method returns the view to be used in the crossfader)
                //the crossfader library can be found here: https://github.com/mikepenz/Crossfader
                crossFader = Crossfader<CrossFadeSlidingPaneLayout>()
                        .withContent(binding.mainContent)
                        .withFirst(sliderView, firstWidth)
                        .withSecond(miniSliderView, secondWidth)
                        .withSavedInstance(savedInstanceState)
                        .withGmailStyleSwiping()
                        .withPanelSlideListener(object : SlidingPaneLayout.PanelSlideListener {
                            override fun onPanelSlide(panel: View, slideOffset: Float) {
                                (binding.risuscitoToolbar.navigationIcon as? DrawerArrowDrawable)?.progress = min(1f, max(0f, slideOffset))
                            }

                            override fun onPanelClosed(panel: View) {
                                (binding.risuscitoToolbar.navigationIcon as? DrawerArrowDrawable)?.setVerticalMirror(false)
                            }

                            override fun onPanelOpened(panel: View) {
                                (binding.risuscitoToolbar.navigationIcon as? DrawerArrowDrawable)?.setVerticalMirror(true)
                            }
                        })
                        .build()

                //define the crossfader to be used with the miniDrawer. This is required to be able to automatically toggle open / close
                miniSliderView.crossFader = CrossfadeWrapper(crossFader)
                //define a shadow (this is only for normal LTR layouts if you have a RTL app you need to define the other one
                crossFader.getCrossFadeSlidingPaneLayout().setShadowResourceLeft(R.drawable.material_drawer_shadow_left)
                binding.risuscitoToolbar.navigationIcon = DrawerArrowDrawable(this).apply { color = drawerArrowColor }
                binding.risuscitoToolbar.setNavigationOnClickListener { crossFader.crossFade() }
            }
        } else {
            mActionBarDrawerToggle = ActionBarDrawerToggle(this, binding.drawer as DrawerLayout, binding.risuscitoToolbar, R.string.material_drawer_open, R.string.material_drawer_close)
            mActionBarDrawerToggle.drawerArrowDrawable.color = drawerArrowColor
            mActionBarDrawerToggle.syncState()

            binding.slider?.apply {
                setItems(
                        GmailDrawerItem().apply {
                            nameRes = R.string.activity_homepage
                            iconicsIcon = CommunityMaterial.Icon2.cmd_home
                            identifier = R.id.navigation_home.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.search_name_text
                            iconicsIcon = CommunityMaterial.Icon2.cmd_magnify
                            identifier = R.id.navigation_search.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_general_index
                            iconicsIcon = CommunityMaterial.Icon2.cmd_view_list
                            identifier = R.id.navigation_indexes.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_custom_lists
                            iconicsIcon = CommunityMaterial.Icon2.cmd_view_carousel
                            identifier = R.id.navitagion_lists.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.action_favourites
                            iconicsIcon = CommunityMaterial.Icon2.cmd_star
                            identifier = R.id.navigation_favorites.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_consegnati
                            iconicsIcon = CommunityMaterial.Icon.cmd_clipboard_check
                            identifier = R.id.navigation_consegnati.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_history
                            iconicsIcon = CommunityMaterial.Icon2.cmd_history
                            identifier = R.id.navigation_history.toLong()
                            typeface = mMediumFont
                        },
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_settings
                            iconicsIcon = CommunityMaterial.Icon.cmd_cog
                            identifier = R.id.navigation_settings.toLong()
                            typeface = mMediumFont
                        },
                        DividerDrawerItem(),
                        GmailDrawerItem().apply {
                            nameRes = R.string.title_activity_about
                            iconicsIcon = CommunityMaterial.Icon2.cmd_information_outline
                            identifier = R.id.navigation_changelog.toLong()
                            typeface = mMediumFont
                        }
                )
                onDrawerItemClickListener = { _, drawerItem, position ->
                    onDrawerItemClick(drawerItem, position)
                }
                tintStatusBar = true
                setSavedInstance(savedInstanceState)
            }
            if (savedInstanceState == null)
                binding.slider?.setSelectionAtPosition(1, false)
            (binding.drawer as? DrawerLayout)?.setStatusBarBackgroundColor(getStatusBarDefaultColor(this))
            (binding.drawer as? DrawerLayout)?.addDrawerListener(mActionBarDrawerToggle)
        }
    }

    private fun onDrawerItemClick(drawerItem: IDrawerItem<*>, position: Int): Boolean {
        val fragment = when (drawerItem.identifier) {
            R.id.navigation_home.toLong() -> Risuscito()
            R.id.navigation_search.toLong() -> SearchFragment()
            R.id.navigation_indexes.toLong() -> GeneralIndex()
            R.id.navitagion_lists.toLong() -> CustomLists()
            R.id.navigation_favorites.toLong() -> FavoritesFragment()
            R.id.navigation_settings.toLong() -> SettingsFragment()
            R.id.navigation_changelog.toLong() -> AboutFragment()
            R.id.navigation_consegnati.toLong() -> ConsegnatiFragment()
            R.id.navigation_history.toLong() -> HistoryFragment()
            else -> Risuscito()
        }
        binding.appBarLayout.setExpanded(true, true)

        // creo il nuovo fragment solo se non è lo stesso che sto già visualizzando
        val myFragment = supportFragmentManager
                .findFragmentByTag(drawerItem.identifier.toString())
        if (myFragment == null || !myFragment.isVisible) {
            supportFragmentManager.commit {
                setCustomAnimations(
                        R.anim.animate_slide_in_left, R.anim.animate_slide_out_right)
                replace(R.id.content_frame, fragment, drawerItem.identifier.toString())
            }
        }

        //FIX perchè cliccando sul MiniDrawer non si deseleziona la voce del drawer
        //precedentemente selezionata
        if (mViewModel.isTabletWithNoFixedDrawer) {
            for (i in 0 until sliderView.adapter.itemCount) {
                if (i != position)
                    sliderView.selectExtension.deselect(i)
            }
        }

        return false
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
                                + mappa[canto.savedTab ?: ""])
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
                                + mappa[canto.savedBarre])
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

    fun initFab(optionMenu: Boolean, icon: Drawable, click: View.OnClickListener, action: SpeedDialView.OnActionSelectedListener?, customList: Boolean) {
        Log.d(TAG, "initFab()")
        enableFab(false)
        binding.fabPager.setMainFabClosedDrawable(icon)
        binding.fabPager.clearActionItems()
        binding.fabPager.expansionMode = if (mViewModel.mLUtils.isFabExpansionLeft) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP
        enableFab(true)
        Log.d(TAG, "initFab optionMenu: $optionMenu")

        if (optionMenu) {
            val iconColor = ContextCompat.getColor(this, R.color.text_color_secondary)
            val backgroundColor = ContextCompat.getColor(this, R.color.floating_background)

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
                            IconicsDrawable(this, CommunityMaterial.Icon2.cmd_plus).apply {
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
                            IconicsDrawable(this, CommunityMaterial.Icon2.cmd_share_variant).apply {
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
                                IconicsDrawable(this, CommunityMaterial.Icon2.cmd_pencil).apply {
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

    private val startSignInForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.data))
    }

    // [START signOut]
    private fun signOut() {
        PreferenceManager.getDefaultSharedPreferences(this).edit { putBoolean(Utility.SIGN_IN_REQUESTED, false) }
        FirebaseAuth.getInstance().signOut()
        mSignInClient?.signOut()?.addOnCompleteListener {
            updateUI(false)
            Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT)
                    .show()
        }
    }

    // [START revokeAccess]
    private fun revokeAccess() {
        PreferenceManager.getDefaultSharedPreferences(this).edit { putBoolean(Utility.SIGN_IN_REQUESTED, false) }
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
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Utility.SIGN_IN_REQUESTED, false))
                Toast.makeText(this, getString(
                        R.string.login_failed,
                        -1,
                        task.exception?.localizedMessage), Toast.LENGTH_SHORT)
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
                            Toast.makeText(this, getString(R.string.connected_as, acct?.displayName), Toast.LENGTH_SHORT)
                                    .show()
                            mViewModel.showSnackbar = false
                        }
                        updateUI(true)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Utility.SIGN_IN_REQUESTED, false))
                            Toast.makeText(this, getString(
                                    R.string.login_failed,
                                    -1,
                                    task.exception?.localizedMessage), Toast.LENGTH_SHORT)
                                    .show()
                    }
                }
    }

    private fun updateUI(signedIn: Boolean) {
        mViewModel.signedIn.value = signedIn
        PreferenceManager.getDefaultSharedPreferences(this).edit { putBoolean(Utility.SIGNED_IN, signedIn) }
        if (signedIn)
            PreferenceManager.getDefaultSharedPreferences(this).edit { putBoolean(Utility.SIGN_IN_REQUESTED, true) }
        if (signedIn) {
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
            } else {
                profile = ProfileDrawerItem().apply {
                    nameText = acct?.displayName as? CharSequence ?: ""
                    descriptionText = acct?.email as? CharSequence ?: ""
                    icon = ImageHolder(profileIcon)
                    identifier = PROF_ID
                    typeface = mRegularFont
                }
            }
            // Create the AccountHeader
            mAccountHeader.updateProfile(profile)
            if (mAccountHeader.profiles?.size == 1) {
                mAccountHeader.addProfiles(
                        ProfileSettingDrawerItem().apply {
                            nameRes = R.string.gdrive_backup
                            iconicsIcon = CommunityMaterial.Icon.cmd_cloud_upload
                            identifier = R.id.gdrive_backup.toLong()
                        },
                        ProfileSettingDrawerItem().apply {
                            nameRes = R.string.gdrive_restore
                            iconicsIcon = CommunityMaterial.Icon.cmd_cloud_download
                            identifier = R.id.gdrive_restore.toLong()
                        },
                        ProfileSettingDrawerItem().apply {
                            nameRes = R.string.gplus_signout
                            iconicsIcon = CommunityMaterial.Icon.cmd_account_remove
                            identifier = R.id.gplus_signout.toLong()
                        },
                        ProfileSettingDrawerItem().apply {
                            nameRes = R.string.gplus_revoke
                            iconicsIcon = CommunityMaterial.Icon.cmd_account_key
                            identifier = R.id.gplus_revoke.toLong()
                        }
                )
            }
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

        }
        if (mViewModel.isTabletWithNoFixedDrawer) {
            miniSliderView.onProfileClick()
            miniSliderView.itemAdapter.getAdapterItem(0).isEnabled = signedIn
        }
        hideProgressDialog()
    }

    private fun showProgressDialog() {
        binding.loadingBar.isVisible = true
    }

    private fun hideProgressDialog() {
        binding.loadingBar.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: " + item.itemId)
        if (mViewModel.isTabletWithNoFixedDrawer && item.itemId == android.R.id.home) {
            crossFader.crossFade()
            return true
        }
        return super.onOptionsItemSelected(item)
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
        if (mViewModel.isOnTablet) {
            if (mViewModel.isTabletWithNoFixedDrawer) miniSliderView.setSelection(R.id.navigation_home.toLong())
            sliderView.setSelectionAtPosition(1, true)
        }
        binding.appBarLayout.setExpanded(true, true)
        binding.slider?.setSelectionAtPosition(1, true)
    }

    private fun showAccountRelatedDialog(tag: String) {
        SimpleDialogFragment.show(SimpleDialogFragment.Builder(this, tag).apply {
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
                supportFragmentManager)
    }

    private suspend fun translate() {
        ProgressDialogFragment.show(ProgressDialogFragment.Builder(this, "TRANSLATION")
                .content(R.string.translation_running)
                .progressIndeterminate(true),
                supportFragmentManager)
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
                    Snackbar.LENGTH_LONG)
                    .show()
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.localizedMessage, e)
            Snackbar.make(binding.mainContent, "error: " + e.localizedMessage, Snackbar.LENGTH_SHORT).show()
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
            SimpleDialogFragment.show(SimpleDialogFragment.Builder(this, RESTART)
                    .title(R.string.general_message)
                    .content(R.string.gdrive_restore_success)
                    .positiveButton(R.string.ok),
                    supportFragmentManager)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.localizedMessage, e)
            Snackbar.make(binding.mainContent, "error: " + e.localizedMessage, Snackbar.LENGTH_LONG).show()
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
