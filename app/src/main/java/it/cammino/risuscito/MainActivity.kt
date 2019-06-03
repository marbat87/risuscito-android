package it.cammino.risuscito

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import com.mikepenz.iconics.colorInt
import com.mikepenz.iconics.paddingDp
import com.mikepenz.iconics.sizeDp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.materialdrawer.*
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.mikepenz.materialize.util.UIUtils
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.dialogs.ProgressDialogFragment
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.ui.CrossfadeWrapper
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.common_bottom_bar.*
import kotlinx.android.synthetic.main.common_circle_progress.*
import kotlinx.android.synthetic.main.risuscito_toolbar_noelevation.*
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : ThemeableActivity(), SimpleDialogFragment.SimpleCallback {
    private lateinit var mViewModel: MainActivityViewModel
    private lateinit var profileIcon: IconicsDrawable
    private var mLUtils: LUtils? = null
    var drawer: Drawer? = null
        private set
    private var mMiniDrawer: MiniDrawer? = null
    var crossFader: Crossfader<*>? = null
        private set
    private lateinit var mAccountHeader: AccountHeader
    var isOnTablet: Boolean = false
        private set
    var hasThreeColumns: Boolean = false
        private set
    var isGridLayout: Boolean = false
        private set
    private var acct: GoogleSignInAccount? = null
    private var mSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private var mRegularFont: Typeface? = null
    private var mMediumFont: Typeface? = null

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

        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        mRegularFont = ResourcesCompat.getFont(this, R.font.googlesans_regular)
        mMediumFont = ResourcesCompat.getFont(this, R.font.googlesans_medium)

        val icon = IconicsDrawable(this)
                .icon(CommunityMaterial.Icon2.cmd_menu)
                .colorInt(Color.WHITE)
                .sizeDp(24)
                .paddingDp(2)

        profileIcon = IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_account_circle)
                .colorInt(themeUtils.primaryColor())
                .sizeDp(48)

        risuscito_toolbar?.setBackgroundColor(themeUtils.primaryColor())
        risuscito_toolbar?.navigationIcon = icon
        setSupportActionBar(risuscito_toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (intent.getBooleanExtra(Utility.DB_RESET, false)) {
            TranslationTask(this).execute()
        }

        mLUtils = LUtils.getInstance(this)
        isOnTablet = mLUtils?.isOnTablet ?: false
        Log.d(TAG, "onCreate: isOnTablet = $isOnTablet")
        hasThreeColumns = mLUtils?.hasThreeColumns ?: false
        Log.d(TAG, "onCreate: hasThreeComlumns = $hasThreeColumns")
        isGridLayout = mLUtils?.isGridLayout ?: false
        Log.d(TAG, "onCreate: isGridLayout = $isGridLayout")

        if (isOnTablet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.statusBarColor = themeUtils.primaryColorDark()

        if (isOnTablet)
            tabletToolbarBackground?.setBackgroundColor(themeUtils.primaryColor())
        else
            material_tabs?.setBackgroundColor(themeUtils.primaryColor())

        setupNavDrawer(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.transaction {
                replace(R.id.content_frame, Risuscito(), R.id.navigation_home.toString())
            }
        }
        if (!isOnTablet) toolbar_layout?.setExpanded(true, false)

        searchView.setBackIconColor(themeUtils.primaryColor())
        searchView.setBackgroundColor(themeUtils.primaryColor())

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
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

        // registra un receiver per ricevere la notifica di preparazione della registrazione
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(nextStepReceiver, IntentFilter(BROADCAST_NEXT_STEP))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(nextStepReceiver)
    }

    public override fun onStart() {
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
        hideProgressDialog()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        val mSavedInstanceState = drawer?.saveInstanceState(savedInstanceState)
        super.onSaveInstanceState(mSavedInstanceState)
    }

    private fun setupNavDrawer(savedInstanceState: Bundle?) {

        val profile = ProfileDrawerItem()
                .withName("")
                .withEmail("")
                .withIcon(profileIcon)
                .withIdentifier(PROF_ID)
                .withTypeface(mRegularFont)

        // Create the AccountHeader
        mAccountHeader = AccountHeaderBuilder().apply {
            withTranslucentStatusBar(!isOnTablet)
            withSelectionListEnabledForSingleProfile(false)
            withSavedInstance(savedInstanceState)
            addProfiles(profile)
            mRegularFont?.let {
                withNameTypeface(it)
                withEmailTypeface(it)
            }
            withOnAccountHeaderListener(object : AccountHeader.OnAccountHeaderListener {
                override fun onProfileChanged(view: View?, profile: IProfile<*>, current: Boolean): Boolean {
                    // sample usage of the onProfileChanged listener
                    // if the clicked item has the identifier 1 add a new profile ;)
                    if (profile is IDrawerItem<*>) {
                        when (profile.identifier) {
                            R.id.gdrive_backup.toLong() -> showAccountRelatedDialog(BACKUP_ASK)
                            R.id.gdrive_restore.toLong() -> showAccountRelatedDialog(RESTORE_ASK)
                            R.id.gplus_signout.toLong() -> showAccountRelatedDialog(SIGNOUT)
                            R.id.gplus_revoke.toLong() -> showAccountRelatedDialog(REVOKE)
                        }
                    }
                    // false if you have not consumed the event and it should close the drawer
                    return false
                }
            })
        }.withActivity(this).build()

        val selectedColor = themeUtils.primaryColor()

        val mDrawerBuilder = DrawerBuilder().apply {
            risuscito_toolbar?.let {
                withToolbar(it)
            }
            withHasStableIds(true)
            withAccountHeader(mAccountHeader)
            addDrawerItems(
                    PrimaryDrawerItem()
                            .withName(R.string.activity_homepage)
                            .withIcon(CommunityMaterial.Icon2.cmd_home)
                            .withIdentifier(R.id.navigation_home.toLong())
                            .withSelectedIconColor(selectedColor)
                            .withSelectedTextColor(selectedColor)
                            .withTypeface(mMediumFont),
                    PrimaryDrawerItem()
                            .withName(R.string.search_name_text)
                            .withIcon(CommunityMaterial.Icon2.cmd_magnify)
                            .withIdentifier(R.id.navigation_search.toLong())
                            .withSelectedIconColor(selectedColor)
                            .withSelectedTextColor(selectedColor)
                            .withTypeface(mMediumFont),
                    PrimaryDrawerItem()
                            .withName(R.string.title_activity_general_index)
                            .withIcon(CommunityMaterial.Icon2.cmd_view_list)
                            .withIdentifier(R.id.navigation_indexes.toLong())
                            .withSelectedIconColor(selectedColor)
                            .withSelectedTextColor(selectedColor)
                            .withTypeface(mMediumFont),
                    PrimaryDrawerItem()
                            .withName(R.string.title_activity_custom_lists)
                            .withIcon(CommunityMaterial.Icon2.cmd_view_carousel)
                            .withIdentifier(R.id.navitagion_lists.toLong())
                            .withSelectedIconColor(selectedColor)
                            .withSelectedTextColor(selectedColor)
                            .withTypeface(mMediumFont),
                    PrimaryDrawerItem()
                            .withName(R.string.action_favourites)
                            .withIcon(CommunityMaterial.Icon2.cmd_heart)
                            .withIdentifier(R.id.navigation_favorites.toLong())
                            .withSelectedIconColor(selectedColor)
                            .withSelectedTextColor(selectedColor)
                            .withTypeface(mMediumFont),
                    PrimaryDrawerItem()
                            .withName(R.string.title_activity_consegnati)
                            .withIcon(CommunityMaterial.Icon.cmd_clipboard_check)
                            .withIdentifier(R.id.navigation_consegnati.toLong())
                            .withSelectedIconColor(selectedColor)
                            .withSelectedTextColor(selectedColor)
                            .withTypeface(mMediumFont),
                    PrimaryDrawerItem()
                            .withName(R.string.title_activity_history)
                            .withIcon(CommunityMaterial.Icon2.cmd_history)
                            .withIdentifier(R.id.navigation_history.toLong())
                            .withSelectedIconColor(selectedColor)
                            .withSelectedTextColor(selectedColor)
                            .withTypeface(mMediumFont),
                    PrimaryDrawerItem()
                            .withName(R.string.title_activity_settings)
                            .withIcon(CommunityMaterial.Icon2.cmd_settings)
                            .withIdentifier(R.id.navigation_settings.toLong())
                            .withSelectedIconColor(selectedColor)
                            .withSelectedTextColor(selectedColor)
                            .withTypeface(mMediumFont),
                    DividerDrawerItem(),
                    PrimaryDrawerItem()
                            .withName(R.string.title_activity_about)
                            .withIcon(CommunityMaterial.Icon2.cmd_information_outline)
                            .withIdentifier(R.id.navigation_changelog.toLong())
                            .withSelectedIconColor(selectedColor)
                            .withSelectedTextColor(selectedColor)
                            .withTypeface(mMediumFont))
            withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                    // check if the drawerItem is set.
                    // there are different reasons for the drawerItem to be null
                    // --> click on the header
                    // --> click on the footer
                    // those items don't contain a drawerItem

                    val fragment: Fragment
                    when (drawerItem.identifier) {
                        R.id.navigation_home.toLong() -> {
                            fragment = Risuscito()
                            if (!isOnTablet)
                                toolbar_layout?.setExpanded(true, true)
                        }
                        R.id.navigation_search.toLong() -> fragment = SearchFragment()
                        R.id.navigation_indexes.toLong() -> fragment = GeneralIndex()
                        R.id.navitagion_lists.toLong() -> fragment = CustomLists()
                        R.id.navigation_favorites.toLong() -> fragment = FavoritesFragment()
                        R.id.navigation_settings.toLong() -> fragment = SettingsFragment()
                        R.id.navigation_changelog.toLong() -> fragment = AboutFragment()
                        R.id.navigation_consegnati.toLong() -> fragment = ConsegnatiFragment()
                        R.id.navigation_history.toLong() -> fragment = HistoryFragment()
                        else -> return true
                    }

                    // creo il nuovo fragment solo se non è lo stesso che sto già visualizzando
                    val myFragment = supportFragmentManager
                            .findFragmentByTag(drawerItem.identifier.toString())
                    if (myFragment == null || !myFragment.isVisible) {
                        supportFragmentManager.transaction {
                            if (!isOnTablet)
                                setCustomAnimations(
                                        R.anim.animate_slide_in_left, R.anim.animate_slide_out_right)
                            replace(R.id.content_frame, fragment, drawerItem.identifier.toString())
                        }
                    }

                    if (isOnTablet) mMiniDrawer?.setSelection(drawerItem.identifier)
                    return isOnTablet
                }
            })
            withGenerateMiniDrawer(isOnTablet)
            withSavedInstance(savedInstanceState)
            withTranslucentStatusBar(!isOnTablet)
        }.withActivity(this)

        if (isOnTablet) {
            drawer = mDrawerBuilder.buildView()
            // the MiniDrawer is managed by the Drawer and we just get it to hook it into the Crossfader
            mMiniDrawer = drawer?.miniDrawer?.withEnableSelectedMiniDrawerItemBackground(true)?.withIncludeSecondaryDrawerItems(true)

            // get the widths in px for the first and second panel
            val firstWidth = UIUtils.convertDpToPixel(302f, this).toInt()
            val secondWidth = UIUtils.convertDpToPixel(72f, this).toInt()

            // create and build our crossfader (see the MiniDrawer is also builded in here, as the build
            // method returns the view to be used in the crossfader)
            crossFader = Crossfader<CrossFadeSlidingPaneLayout>()
                    .withContent(main_frame)
                    .withFirst(drawer?.slider, firstWidth)
                    .withSecond(mMiniDrawer?.build(this), secondWidth)
                    .withSavedInstance(savedInstanceState)
                    .withGmailStyleSwiping()
                    .build()

            // define the crossfader to be used with the miniDrawer. This is required to be able to
            // automatically toggle open / close
            mMiniDrawer?.withCrossFader(CrossfadeWrapper(crossFader))

            // define a shadow (this is only for normal LTR layouts if you have a RTL app you need to
            // define the other one
            crossFader?.getCrossFadeSlidingPaneLayout()?.setShadowResourceLeft(R.drawable.material_drawer_shadow_left)
            crossFader?.getCrossFadeSlidingPaneLayout()?.setShadowResourceRight(R.drawable.material_drawer_shadow_right)
        } else {
            drawer = mDrawerBuilder.build()
            drawer?.drawerLayout?.setStatusBarBackgroundColor(themeUtils.primaryColorDark())
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")

        if (searchView.onBackPressed()) {
            return
        }

        if (fab_pager.isOpen) {
            fab_pager.close()
            return
        }

        if (isOnTablet) {
            if (crossFader?.isCrossFaded() == true) {
                crossFader?.crossFade()
                return
            }
        } else {
            if (drawer?.isDrawerOpen == true) {
                drawer?.closeDrawer()
                return
            }
        }

        backToHome(true)
    }

    // converte gli accordi salvati dalla lingua vecchia alla nuova
    private fun convertTabs() {
        val conversion = intent.getStringExtra(Utility.CHANGE_LANGUAGE)

        var accordi1 = CambioAccordi.accordi_it
        Log.d(TAG, "convertTabs - from: " + conversion.substring(0, 2))
        when (conversion.substring(0, 2)) {
            "uk" -> accordi1 = CambioAccordi.accordi_uk
            "en" -> accordi1 = CambioAccordi.accordi_en
        }

        var accordi2 = CambioAccordi.accordi_it
        Log.d(TAG, "convertTabs - to: " + conversion.substring(3, 5))
        when (conversion.substring(3, 5)) {
            "uk" -> accordi2 = CambioAccordi.accordi_uk
            "en" -> accordi2 = CambioAccordi.accordi_en
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
        Log.d(TAG, "convertiBarre - from: ${conversion.substring(0, 2)}")
        when (conversion.substring(0, 2)) {
            "uk" -> barre1 = CambioAccordi.barre_uk
            "en" -> barre1 = CambioAccordi.barre_en
        }

        var barre2 = CambioAccordi.barre_it
        Log.d(TAG, "convertiBarre - to: ${conversion.substring(3, 5)}")
        when (conversion.substring(3, 5)) {
            "uk" -> barre2 = CambioAccordi.barre_uk
            "en" -> barre2 = CambioAccordi.barre_en
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
        risuscito_toolbar?.title = getString(titleResId)
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
                params?.behavior = if (mLUtils?.isFabScrollingActive == true) SpeedDialView.ScrollingViewSnackbarBehavior() else SpeedDialView.NoBehavior()
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
        enableFab(true)
        fab_pager.clearActionItems()

        if (optionMenu) {
            fab_pager.expansionMode = if (mLUtils?.isFabScrollingActive == true) (if (mLUtils?.isLandscape == true) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP) else SpeedDialView.ExpansionMode.BOTTOM
            val iconColor = ContextCompat.getColor(this, R.color.text_color_secondary)
            val backgroundColor = ContextCompat.getColor(this, R.color.floating_background)

            fab_pager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_pulisci, IconicsDrawable(this)
                            .icon(CommunityMaterial.Icon.cmd_eraser_variant)
                            .colorInt(iconColor)
                            .sizeDp(24)
                            .paddingDp(4))
                            .setLabel(getString(R.string.dialog_reset_list_title))
                            .setFabBackgroundColor(backgroundColor)
                            .setLabelBackgroundColor(backgroundColor)
                            .setLabelColor(iconColor)
                            .create()
            )

            fab_pager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_add_lista, IconicsDrawable(this)
                            .icon(CommunityMaterial.Icon2.cmd_plus)
                            .colorInt(iconColor)
                            .sizeDp(24)
                            .paddingDp(4))
                            .setLabel(getString(R.string.action_add_list))
                            .setFabBackgroundColor(backgroundColor)
                            .setLabelBackgroundColor(backgroundColor)
                            .setLabelColor(iconColor)
                            .create()
            )

            fab_pager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_condividi, IconicsDrawable(this)
                            .icon(CommunityMaterial.Icon2.cmd_share_variant)
                            .colorInt(iconColor)
                            .sizeDp(24)
                            .paddingDp(4))
                            .setLabel(getString(R.string.action_share))
                            .setFabBackgroundColor(backgroundColor)
                            .setLabelBackgroundColor(backgroundColor)
                            .setLabelColor(iconColor)
                            .create()
            )

            if (customList) {
                fab_pager.addActionItem(
                        SpeedDialActionItem.Builder(R.id.fab_condividi_file, IconicsDrawable(this)
                                .icon(CommunityMaterial.Icon.cmd_attachment)
                                .colorInt(iconColor)
                                .sizeDp(24)
                                .paddingDp(4))
                                .setLabel(getString(R.string.action_share_file))
                                .setFabBackgroundColor(backgroundColor)
                                .setLabelBackgroundColor(backgroundColor)
                                .setLabelColor(iconColor)
                                .create()
                )

                fab_pager.addActionItem(
                        SpeedDialActionItem.Builder(R.id.fab_edit_lista, IconicsDrawable(this)
                                .icon(CommunityMaterial.Icon2.cmd_pencil)
                                .colorInt(iconColor)
                                .sizeDp(24)
                                .paddingDp(4))
                                .setLabel(getString(R.string.action_edit_list))
                                .setFabBackgroundColor(backgroundColor)
                                .setLabelBackgroundColor(backgroundColor)
                                .setLabelColor(iconColor)
                                .create()
                )

                fab_pager.addActionItem(
                        SpeedDialActionItem.Builder(R.id.fab_delete_lista, IconicsDrawable(this)
                                .icon(CommunityMaterial.Icon.cmd_delete)
                                .colorInt(iconColor)
                                .sizeDp(24)
                                .paddingDp(4))
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
        material_tabs.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun getMaterialTabs(): TabLayout {
        return material_tabs
    }

    fun enableBottombar(enabled: Boolean) {
        if (!isOnTablet) {
            Log.d(TAG, "enableBottombar - enabled: $enabled")
            if (enabled)
                mLUtils?.animateIn(bottom_bar)
            else
                mLUtils?.animateOut(bottom_bar)
        }
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
        when (requestCode) {
            RC_SIGN_IN -> handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
            else -> {
            }
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
                personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length - 2) + 400
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
            if (isOnTablet) mMiniDrawer?.onProfileClick()
        } else {
            val profile = ProfileDrawerItem()
                    .withName("")
                    .withEmail("")
                    .withIcon(profileIcon)
                    .withIdentifier(PROF_ID)
                    .withTypeface(mRegularFont)
            if ((mAccountHeader.profiles?.size ?: 0) > 1) {
                mAccountHeader.removeProfile(1)
                mAccountHeader.removeProfile(1)
                mAccountHeader.removeProfile(1)
                mAccountHeader.removeProfile(1)
            }
            mAccountHeader.updateProfile(profile)
            if (isOnTablet) mMiniDrawer?.onProfileClick()
        }
        hideProgressDialog()
    }

    private fun showProgressDialog() {
        loadingBar?.visibility = View.VISIBLE
    }

    private fun hideProgressDialog() {
        loadingBar?.visibility = View.GONE
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
        if (isOnTablet && item.itemId == android.R.id.home) {
            crossFader?.crossFade()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNegative(tag: String) {}

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

        if (isOnTablet)
            mMiniDrawer?.setSelection(R.id.navigation_home.toLong())
        else {
            toolbar_layout?.setExpanded(true, true)
        }
        drawer?.setSelection(R.id.navigation_home.toLong())
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
