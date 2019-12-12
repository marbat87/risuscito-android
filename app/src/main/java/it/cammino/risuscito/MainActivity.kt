package it.cammino.risuscito

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat.START
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.mikepenz.crossfader.Crossfader
import com.mikepenz.crossfader.view.CrossFadeSlidingPaneLayout
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.dsl.iconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.mikepenz.materialdrawer.widget.MiniDrawerSliderView
import com.mikepenz.materialize.util.UIUtils
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.dialogs.ProgressDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.ui.CrossfadeWrapper
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_ENGLISH
import it.cammino.risuscito.ui.LocaleManager.Companion.LANGUAGE_UKRAINIAN
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils.Companion.getStatusBarDefaultColor
import it.cammino.risuscito.utils.themeColor
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.common_bottom_bar.*
import kotlinx.android.synthetic.main.common_circle_progress.*
import kotlinx.android.synthetic.main.common_top_toolbar.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max
import kotlin.math.min

class MainActivity : ThemeableActivity(), SimpleDialogFragment.SimpleCallback {
    private val mViewModel: MainActivityViewModel by viewModels()
    private lateinit var profileIcon: IconicsDrawable
    private lateinit var mLUtils: LUtils
    private lateinit var mAccountHeader: AccountHeaderView
    private lateinit var miniSliderView: MiniDrawerSliderView
    private lateinit var sliderView: MaterialDrawerSliderView
    private lateinit var crossFader: Crossfader<*>
    var hasThreeColumns: Boolean = false
        private set
    var isGridLayout: Boolean = false
        private set
    private var isLandscape: Boolean = false
    private var isTabletWithFixedDrawer: Boolean = false
    private var isTabletWithNoFixedDrawer: Boolean = false
    private var acct: GoogleSignInAccount? = null
    private var mSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private var mRegularFont: Typeface? = null
    private var mMediumFont: Typeface? = null
    private lateinit var mActionBarDrawerToggle: ActionBarDrawerToggle

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
        super.hasNavDrawer = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onBackPressedDispatcher.addCallback(this) {
            when {
                searchView.onBackPressed() -> {
                }
                fab_pager.isOpen -> fab_pager.close()
                isTabletWithNoFixedDrawer && crossFader.isCrossFaded() -> crossFader.crossFade()
                !isOnTablet && root?.isDrawerOpen(START) == true -> root?.closeDrawer(START)
                else -> backToHome(true)
            }
        }

        mLUtils = LUtils.getInstance(this)

        mRegularFont = ResourcesCompat.getFont(this, R.font.googlesans_regular)
        mMediumFont = ResourcesCompat.getFont(this, R.font.googlesans_medium)

        profileIcon = iconicsDrawable(CommunityMaterial.Icon.cmd_account_circle) {
            color = colorInt(themeColor(R.attr.colorPrimary))
            size = sizeDp(56)
        }

        setSupportActionBar(risuscito_toolbar)

        if (intent.getBooleanExtra(Utility.DB_RESET, false)) {
            TranslationTask(this).execute()
        }

        hasThreeColumns = mLUtils.hasThreeColumns
        Log.d(TAG, "onCreate: hasThreeColumns = $hasThreeColumns")
        isGridLayout = mLUtils.isGridLayout
        Log.d(TAG, "onCreate: isGridLayout = $isGridLayout")
        isLandscape = mLUtils.isLandscape
        Log.d(TAG, "onCreate: isLandscape = $isLandscape")
        isTabletWithFixedDrawer = isOnTablet && isLandscape
        Log.d(TAG, "onCreate: hasFixedDrawer = $isTabletWithFixedDrawer")
        isTabletWithNoFixedDrawer = isOnTablet && !isLandscape
        Log.d(TAG, "onCreate: hasFixedDrawer = $isTabletWithNoFixedDrawer")

        setupNavDrawer(savedInstanceState)

        toolbar_layout?.setExpanded(true, false)

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

        setDialogCallback(BACKUP_ASK)
        setDialogCallback(RESTORE_ASK)
        setDialogCallback(SIGNOUT)
        setDialogCallback(REVOKE)
        setDialogCallback(RESTART)

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
        slider?.let { outState = it.saveInstanceState(outState) }
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
        fab_pager.expansionMode = if (mLUtils.isFabExpansionLeft) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP
    }

    private fun setupNavDrawer(savedInstanceState: Bundle?) {

        val profile = ProfileDrawerItem()
                .withName("")
                .withEmail("")
                .withIcon(profileIcon)
                .withIdentifier(PROF_ID)
                .withTypeface(mRegularFont)

        mAccountHeader = AccountHeaderView(this).apply {
            slider?.let { attachToSliderView(it) }
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

        if (isOnTablet) {
            sliderView = MaterialDrawerSliderView(this).apply {
                accountHeader = mAccountHeader
                customWidth = MATCH_PARENT
                itemAdapter.add(
                        PrimaryDrawerItem()
                                .withName(R.string.activity_homepage)
                                .withIcon(CommunityMaterial.Icon2.cmd_home)
                                .withIdentifier(R.id.navigation_home.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.search_name_text)
                                .withIcon(CommunityMaterial.Icon2.cmd_magnify)
                                .withIdentifier(R.id.navigation_search.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_general_index)
                                .withIcon(CommunityMaterial.Icon2.cmd_view_list)
                                .withIdentifier(R.id.navigation_indexes.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_custom_lists)
                                .withIcon(CommunityMaterial.Icon2.cmd_view_carousel)
                                .withIdentifier(R.id.navitagion_lists.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.action_favourites)
                                .withIcon(CommunityMaterial.Icon2.cmd_star)
                                .withIdentifier(R.id.navigation_favorites.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_consegnati)
                                .withIcon(CommunityMaterial.Icon.cmd_clipboard_check)
                                .withIdentifier(R.id.navigation_consegnati.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_history)
                                .withIcon(CommunityMaterial.Icon2.cmd_history)
                                .withIdentifier(R.id.navigation_history.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_settings)
                                .withIcon(CommunityMaterial.Icon2.cmd_settings)
                                .withIdentifier(R.id.navigation_settings.toLong())
                                .withTypeface(mMediumFont),
                        DividerDrawerItem(),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_about)
                                .withIcon(CommunityMaterial.Icon2.cmd_information_outline)
                                .withIdentifier(R.id.navigation_changelog.toLong())
                                .withTypeface(mMediumFont)
                )
                onDrawerItemClickListener = { _, drawerItem, _ ->
                    onDrawerItemClick(drawerItem)
                }
                withSavedInstance(savedInstanceState)
            }

            if (savedInstanceState == null)
                sliderView.setSelectionAtPosition(1, false)

            if (isTabletWithFixedDrawer) {
                fixed_drawer_content?.addView(sliderView)
            } else {
                miniSliderView = MiniDrawerSliderView(this).apply {
                    drawer = sliderView
                }
                //get the widths in px for the first and second panel
                val firstWidth = UIUtils.convertDpToPixel(302f, this).toInt()
                val secondWidth = UIUtils.convertDpToPixel(72f, this).toInt()
                //create and build our crossfader (see the MiniDrawer is also builded in here, as the build method returns the view to be used in the crossfader)
                //the crossfader library can be found here: https://github.com/mikepenz/Crossfader
                crossFader = Crossfader<CrossFadeSlidingPaneLayout>()
                        .withContent(main_content)
                        .withFirst(sliderView, firstWidth)
                        .withSecond(miniSliderView, secondWidth)
                        .withSavedInstance(savedInstanceState)
                        .withGmailStyleSwiping()
                        .withPanelSlideListener(object : SlidingPaneLayout.PanelSlideListener {
                            override fun onPanelSlide(panel: View, slideOffset: Float) {
                                (risuscito_toolbar.navigationIcon as? DrawerArrowDrawable)?.progress = min(1f, max(0f, slideOffset))
                            }
                            override fun onPanelClosed(panel: View) {
                                (risuscito_toolbar.navigationIcon as? DrawerArrowDrawable)?.setVerticalMirror(false)
                            }
                            override fun onPanelOpened(panel: View) {
                                (risuscito_toolbar.navigationIcon as? DrawerArrowDrawable)?.setVerticalMirror(true)
                            }
                        })
                        .build()

                //define the crossfader to be used with the miniDrawer. This is required to be able to automatically toggle open / close
                miniSliderView.crossFader = CrossfadeWrapper(crossFader)
                //define a shadow (this is only for normal LTR layouts if you have a RTL app you need to define the other one
                crossFader.getCrossFadeSlidingPaneLayout().setShadowResourceLeft(R.drawable.material_drawer_shadow_left)
                risuscito_toolbar.navigationIcon = DrawerArrowDrawable(this)
                risuscito_toolbar.setNavigationOnClickListener { crossFader.crossFade() }
            }
        } else {
            mActionBarDrawerToggle = ActionBarDrawerToggle(this, root, risuscito_toolbar, R.string.material_drawer_open, R.string.material_drawer_close)
            mActionBarDrawerToggle.syncState()

            slider?.apply {
                itemAdapter.add(
                        PrimaryDrawerItem()
                                .withName(R.string.activity_homepage)
                                .withIcon(CommunityMaterial.Icon2.cmd_home)
                                .withIdentifier(R.id.navigation_home.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.search_name_text)
                                .withIcon(CommunityMaterial.Icon2.cmd_magnify)
                                .withIdentifier(R.id.navigation_search.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_general_index)
                                .withIcon(CommunityMaterial.Icon2.cmd_view_list)
                                .withIdentifier(R.id.navigation_indexes.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_custom_lists)
                                .withIcon(CommunityMaterial.Icon2.cmd_view_carousel)
                                .withIdentifier(R.id.navitagion_lists.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.action_favourites)
                                .withIcon(CommunityMaterial.Icon2.cmd_star)
                                .withIdentifier(R.id.navigation_favorites.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_consegnati)
                                .withIcon(CommunityMaterial.Icon.cmd_clipboard_check)
                                .withIdentifier(R.id.navigation_consegnati.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_history)
                                .withIcon(CommunityMaterial.Icon2.cmd_history)
                                .withIdentifier(R.id.navigation_history.toLong())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_settings)
                                .withIcon(CommunityMaterial.Icon2.cmd_settings)
                                .withIdentifier(R.id.navigation_settings.toLong())
                                .withTypeface(mMediumFont),
                        DividerDrawerItem(),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_about)
                                .withIcon(CommunityMaterial.Icon2.cmd_information_outline)
                                .withIdentifier(R.id.navigation_changelog.toLong())
                                .withTypeface(mMediumFont)
                )
                onDrawerItemClickListener = { _, drawerItem, _ ->
                    onDrawerItemClick(drawerItem)
                }
                tintStatusBar = true
                hasStableIds = true
                withSavedInstance(savedInstanceState)
            }
            if (savedInstanceState == null)
                slider?.setSelectionAtPosition(1, false)
            root?.setStatusBarBackgroundColor(getStatusBarDefaultColor(this))
        }
    }

    private fun onDrawerItemClick(drawerItem: IDrawerItem<*>): Boolean {
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
        toolbar_layout?.setExpanded(true, true)

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

        if (isTabletWithNoFixedDrawer) miniSliderView.setSelection(drawerItem.identifier)
        return isTabletWithNoFixedDrawer
    }

    // converte gli accordi salvati dalla lingua vecchia alla nuova
    private fun convertTabs() {
        val conversion = intent.getStringExtra(Utility.CHANGE_LANGUAGE)

        var accordi1 = CambioAccordi.accordi_it
        Log.d(TAG, "convertTabs - from: ${conversion?.substring(0, 2)}")
        when (conversion?.substring(0, 2)) {
            LANGUAGE_UKRAINIAN -> accordi1 = CambioAccordi.accordi_uk
            LANGUAGE_ENGLISH -> accordi1 = CambioAccordi.accordi_en
        }

        var accordi2 = CambioAccordi.accordi_it
        Log.d(TAG, "convertTabs - to: ${conversion?.substring(3, 5)}")
        when (conversion?.substring(3, 5)) {
            LANGUAGE_UKRAINIAN -> accordi2 = CambioAccordi.accordi_uk
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
            LANGUAGE_UKRAINIAN -> barre1 = CambioAccordi.barre_uk
            LANGUAGE_ENGLISH -> barre1 = CambioAccordi.barre_en
        }

        var barre2 = CambioAccordi.barre_it
        Log.d(TAG, "convertiBarre - to: ${conversion?.substring(3, 5)}")
        when (conversion?.substring(3, 5)) {
            LANGUAGE_UKRAINIAN -> barre2 = CambioAccordi.barre_uk
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
        if (fab_pager?.isOpen == true)
            fab_pager?.close()
    }

    fun toggleFabMenu() {
        fab_pager?.toggle()
    }

    fun enableFab(enable: Boolean) {
        Log.d(TAG, "enableFab: $enable")
        if (enable) {
            if (fab_pager?.isOpen == true)
                fab_pager?.close()
            else {
                val params = fab_pager?.layoutParams as? CoordinatorLayout.LayoutParams
                params?.behavior = SpeedDialView.ScrollingViewSnackbarBehavior()
                fab_pager.requestLayout()
                fab_pager.show()
            }
        } else {
            if (fab_pager?.isOpen == true)
                fab_pager?.close()
            fab_pager.hide()
            val params = fab_pager?.layoutParams as? CoordinatorLayout.LayoutParams
            params?.behavior = SpeedDialView.NoBehavior()
            fab_pager.requestLayout()
        }
    }

    fun initFab(optionMenu: Boolean, icon: Drawable, click: View.OnClickListener, action: SpeedDialView.OnActionSelectedListener?, customList: Boolean) {
        Log.d(TAG, "initFab()")
        enableFab(false)
        fab_pager.setMainFabClosedDrawable(icon)
        fab_pager.clearActionItems()
        fab_pager.expansionMode = if (mLUtils.isFabExpansionLeft) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP
        enableFab(true)
        Log.d(TAG, "initFab optionMenu: $optionMenu")

        if (optionMenu) {
            val iconColor = ContextCompat.getColor(this, R.color.text_color_secondary)
            val backgroundColor = ContextCompat.getColor(this, R.color.floating_background)

            fab_pager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_pulisci,
                            iconicsDrawable(CommunityMaterial.Icon.cmd_eraser_variant) {
                                size = sizeDp(24)
                                padding = sizeDp(4)
                            }
                    )
                            .setTheme(R.style.Risuscito_SpeedDialActionItem)
                            .setLabel(getString(R.string.dialog_reset_list_title))
                            .setFabBackgroundColor(backgroundColor)
                            .setLabelBackgroundColor(backgroundColor)
                            .setLabelColor(iconColor)
                            .create()
            )

            fab_pager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_add_lista,
                            iconicsDrawable(CommunityMaterial.Icon2.cmd_plus) {
                                size = sizeDp(24)
                                padding = sizeDp(4)
                            }
                    )
                            .setTheme(R.style.Risuscito_SpeedDialActionItem)
                            .setLabel(getString(R.string.action_add_list))
                            .setFabBackgroundColor(backgroundColor)
                            .setLabelBackgroundColor(backgroundColor)
                            .setLabelColor(iconColor)
                            .create()
            )

            fab_pager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_condividi,
                            iconicsDrawable(CommunityMaterial.Icon2.cmd_share_variant) {
                                size = sizeDp(24)
                                padding = sizeDp(4)
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
                fab_pager.addActionItem(
                        SpeedDialActionItem.Builder(R.id.fab_condividi_file,
                                iconicsDrawable(CommunityMaterial.Icon.cmd_attachment) {
                                    size = sizeDp(24)
                                    padding = sizeDp(4)
                                }
                        )
                                .setTheme(R.style.Risuscito_SpeedDialActionItem)
                                .setLabel(getString(R.string.action_share_file))
                                .setFabBackgroundColor(backgroundColor)
                                .setLabelBackgroundColor(backgroundColor)
                                .setLabelColor(iconColor)
                                .create()
                )

                fab_pager.addActionItem(
                        SpeedDialActionItem.Builder(R.id.fab_edit_lista,
                                iconicsDrawable(CommunityMaterial.Icon2.cmd_pencil) {
                                    size = sizeDp(24)
                                    padding = sizeDp(4)
                                }
                        )
                                .setTheme(R.style.Risuscito_SpeedDialActionItem)
                                .setLabel(getString(R.string.action_edit_list))
                                .setFabBackgroundColor(backgroundColor)
                                .setLabelBackgroundColor(backgroundColor)
                                .setLabelColor(iconColor)
                                .create()
                )

                fab_pager.addActionItem(
                        SpeedDialActionItem.Builder(R.id.fab_delete_lista,
                                iconicsDrawable(CommunityMaterial.Icon.cmd_delete) {
                                    size = sizeDp(24)
                                    padding = sizeDp(4)
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
            fab_pager.setOnActionSelectedListener(action)

        }
        fab_pager.mainFab.setOnClickListener(click)
    }

    fun getFab(): FloatingActionButton {
        return fab_pager.mainFab
    }

    fun setTabVisible(visible: Boolean) {
        material_tabs.isVisible = visible
    }

    fun expandToolbar() {
        toolbar_layout?.setExpanded(true, true)
    }

    fun getMaterialTabs(): TabLayout {
        return material_tabs
    }

    fun enableBottombar(enabled: Boolean) {
        Log.d(TAG, "enableBottombar - enabled: $enabled")
        if (enabled)
            mLUtils.animateIn(bottom_bar)
        else
            mLUtils.animateOut(bottom_bar)
    }

    // [START signIn]
    fun signIn() {
        val signInIntent = mSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // [START signOut]
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        mSignInClient?.signOut()?.addOnCompleteListener {
            updateUI(false)
            PreferenceManager.getDefaultSharedPreferences(this).edit { putBoolean(Utility.SIGNED_IN, false) }
            Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT)
                    .show()
        }
    }

    // [START revokeAccess]
    private fun revokeAccess() {
        FirebaseAuth.getInstance().signOut()
        mSignInClient?.revokeAccess()?.addOnCompleteListener {
            updateUI(false)
            PreferenceManager.getDefaultSharedPreferences(this).edit { putBoolean(Utility.SIGNED_IN, false) }
            Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT)
                    .show()
        }
    }

    // [START onActivityResult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "requestCode: $requestCode")
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
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
                        PreferenceManager.getDefaultSharedPreferences(this).edit { putBoolean(Utility.SIGNED_IN, true) }
                        if (mViewModel.showSnackbar) {
                            Toast.makeText(this, getString(R.string.connected_as, acct?.displayName), Toast.LENGTH_SHORT)
                                    .show()
                            mViewModel.showSnackbar = false
                        }
                        updateUI(true)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(this, getString(
                                R.string.login_failed,
                                -1,
                                task.exception?.localizedMessage), Toast.LENGTH_SHORT)
                                .show()
                    }
                }
    }

    private fun updateUI(signedIn: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this).edit { putBoolean(Utility.SIGNED_IN, signedIn) }
        val intentBroadcast = Intent(Risuscito.BROADCAST_SIGNIN_VISIBLE)
        Log.d(TAG, "updateUI: DATA_VISIBLE " + !signedIn)
        intentBroadcast.putExtra(Risuscito.DATA_VISIBLE, !signedIn)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
        if (signedIn) {
            val profile: IProfile<*>
            val profilePhoto = acct?.photoUrl
            if (profilePhoto != null) {
                var personPhotoUrl = profilePhoto.toString()
                Log.d(TAG, "personPhotoUrl BEFORE $personPhotoUrl")
//                personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length - 2) + 400
                personPhotoUrl = personPhotoUrl.replace(OLD_PHOTO_RES, NEW_PHOTO_RES)
                Log.d(TAG, "personPhotoUrl AFTER $personPhotoUrl")
                profile = ProfileDrawerItem()
                        .withName(acct?.displayName)
                        .withEmail(acct?.email)
                        .withIcon(personPhotoUrl)
                        .withIdentifier(PROF_ID)
                        .withTypeface(mRegularFont)
            } else {
                profile = ProfileDrawerItem()
                        .withName(acct?.displayName)
                        .withEmail(acct?.email)
                        .withIcon(profileIcon)
                        .withIdentifier(PROF_ID)
                        .withTypeface(mRegularFont)
            }
            // Create the AccountHeader
            mAccountHeader.updateProfile(profile)
            if (mAccountHeader.profiles?.size == 1) {
                mAccountHeader.addProfiles(
                        ProfileSettingDrawerItem()
                                .withName(getString(R.string.gdrive_backup))
                                .withIcon(CommunityMaterial.Icon.cmd_cloud_upload)
                                .withIdentifier(R.id.gdrive_backup.toLong()),
                        ProfileSettingDrawerItem()
                                .withName(getString(R.string.gdrive_restore))
                                .withIcon(CommunityMaterial.Icon.cmd_cloud_download)
                                .withIdentifier(R.id.gdrive_restore.toLong()),
                        ProfileSettingDrawerItem()
                                .withName(getString(R.string.gplus_signout))
                                .withIcon(CommunityMaterial.Icon.cmd_account_remove)
                                .withIdentifier(R.id.gplus_signout.toLong()),
                        ProfileSettingDrawerItem()
                                .withName(getString(R.string.gplus_revoke))
                                .withIcon(CommunityMaterial.Icon.cmd_account_key)
                                .withIdentifier(R.id.gplus_revoke.toLong()))
            }
            if (isTabletWithNoFixedDrawer) miniSliderView.onProfileClick()
        } else {
            val profile = ProfileDrawerItem()
                    .withName("")
                    .withEmail("")
                    .withIcon(profileIcon)
                    .withIdentifier(PROF_ID)
                    .withTypeface(mRegularFont)
            if ((mAccountHeader.profiles?.size ?: 0) > 1) {
                mAccountHeader.removeAllViews()
            }
            mAccountHeader.updateProfile(profile)
            if (isTabletWithNoFixedDrawer) miniSliderView.onProfileClick()
        }
        hideProgressDialog()
    }

    private fun showProgressDialog() {
        loadingBar?.isVisible = true
    }

    private fun hideProgressDialog() {
        loadingBar?.isVisible = false
    }

    fun setShowSnackbar() {
        this.mViewModel.showSnackbar = true
    }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: TAG $tag")
        when (tag) {
            BACKUP_ASK -> {
                ProgressDialogFragment.Builder(this, null, BACKUP_RUNNING)
                        .title(R.string.backup_running)
                        .content(R.string.backup_database)
                        .progressIndeterminate(true)
                        .show()
                backToHome(false)
                BackupTask(this).execute()
            }
            RESTORE_ASK -> {
                ProgressDialogFragment.Builder(this, null, RESTORE_RUNNING)
                        .title(R.string.restore_running)
                        .content(R.string.restoring_database)
                        .progressIndeterminate(true)
                        .show()
                backToHome(false)
                RestoreTask(this).execute()
            }
            SIGNOUT -> signOut()
            REVOKE -> revokeAccess()
            RESTART -> {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: " + item.itemId)
        if (isTabletWithNoFixedDrawer && item.itemId == android.R.id.home) {
            crossFader.crossFade()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNegative(tag: String) {
        // no-op
    }

    private fun dismissProgressDialog(tag: String) {
        val sFragment = ProgressDialogFragment.findVisible(this, tag)
        sFragment?.dismiss()
    }

    private fun setDialogCallback(tag: String) {
        val sFragment = SimpleDialogFragment.findVisible(this, tag)
        sFragment?.setmCallback(this)
    }

    private fun backToHome(exitAlso: Boolean) {
        val myFragment = supportFragmentManager.findFragmentByTag(R.id.navigation_home.toString())
        if (myFragment != null && myFragment.isVisible) {
            if (exitAlso)
                finish()
            return
        }
        if (isTabletWithNoFixedDrawer) {
            miniSliderView.setSelection(R.id.navigation_home.toLong())
            sliderView.setSelection(R.id.navigation_home.toLong())
        }
        toolbar_layout?.setExpanded(true, true)
        slider?.setSelection(R.id.navigation_home.toLong())
    }

    private fun showAccountRelatedDialog(tag: String) {
        SimpleDialogFragment.Builder(this, this, tag).apply {
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
            negativeButton(android.R.string.no)
        }.show()
    }

    companion object {
        /* Request code used to invoke sign in user interactions. */
        private const val RC_SIGN_IN = 9001
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

        private class TranslationTask(activity: MainActivity) : AsyncTask<Void, Void, Void>() {

            private val activityWeakReference: WeakReference<MainActivity> = WeakReference(activity)

            override fun doInBackground(vararg sUrl: Void): Void? {
                activityWeakReference.get()?.let {
                    it.intent.removeExtra(Utility.DB_RESET)
                    it.convertTabs()
                    it.convertiBarre()
                }
                return null
            }

            override fun onPreExecute() {
                super.onPreExecute()
                activityWeakReference.get()?.let {
                    ProgressDialogFragment.Builder(it, null, "TRANSLATION")
                            .content(R.string.translation_running)
                            .progressIndeterminate(true)
                            .show()
                }
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                activityWeakReference.get()?.let {
                    it.intent.removeExtra(Utility.CHANGE_LANGUAGE)
                    try {
                        it.dismissProgressDialog("TRANSLATION")
                    } catch (e: IllegalArgumentException) {
                        Log.e(javaClass.name, e.localizedMessage, e)
                    }
                }
            }
        }

        private class BackupTask(activity: MainActivity) : AsyncTask<Void, Void, String>() {

            private val activityReference: WeakReference<MainActivity> = WeakReference(activity)

            override fun doInBackground(vararg sUrl: Void): String {
                activityReference.get()?.let {
                    return try {
                        it.backupDatabase(it.acct?.id)
                        val intentBroadcast = Intent(BROADCAST_NEXT_STEP)
                        intentBroadcast.putExtra(WHICH, "BACKUP")
                        LocalBroadcastManager.getInstance(it).sendBroadcast(intentBroadcast)
                        it.backupSharedPreferences(it.acct?.id, it.acct?.email)
                        ""
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception: " + e.localizedMessage, e)
                        "error: " + e.localizedMessage
                    }
                }
                Log.e(TAG, "activityReference.get() is null")
                return "error: activityReference.get() is null"
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                activityReference.get()?.let {
                    it.dismissProgressDialog(BACKUP_RUNNING)
                    if (result.isEmpty())
                        Snackbar.make(
                                it.main_content,
                                R.string.gdrive_backup_success,
                                Snackbar.LENGTH_LONG)
                                .show()
                    else
                        Snackbar.make(it.main_content, result, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        private class RestoreTask(activity: MainActivity) : AsyncTask<Void, Void, String>() {

            private val activityReference: WeakReference<MainActivity> = WeakReference(activity)

            override fun doInBackground(vararg sUrl: Void): String {
                activityReference.get()?.let {
                    return try {
                        it.restoreDatabase(it.acct?.id)
                        val intentBroadcast = Intent(BROADCAST_NEXT_STEP)
                        intentBroadcast.putExtra(WHICH, RESTORE)
                        LocalBroadcastManager.getInstance(it).sendBroadcast(intentBroadcast)
                        it.restoreSharedPreferences(it.acct?.id)
                        ""
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception: " + e.localizedMessage, e)
                        "error: " + e.localizedMessage
                    }
                }
                Log.e(TAG, "activityReference.get() is null")
                return "error: activityReference.get() is null"
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                activityReference.get()?.let {
                    it.dismissProgressDialog(RESTORE_RUNNING)
                    if (result.isEmpty())
                        SimpleDialogFragment.Builder(it, it, RESTART)
                                .title(R.string.general_message)
                                .content(R.string.gdrive_restore_success)
                                .positiveButton(R.string.ok)
                                .show()
                    else
                        Snackbar.make(it.main_content, result, Snackbar.LENGTH_LONG).show()
                }
            }

        }
    }
}
