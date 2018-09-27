package it.cammino.risuscito

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
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
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.crossfader.Crossfader
import com.mikepenz.crossfader.view.ICrossFadeSlidingPaneLayout
import com.mikepenz.iconics.IconicsDrawable
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
import kotlinx.android.synthetic.main.common_circle_progress.*
import kotlinx.android.synthetic.main.risuscito_toolbar_noelevation.*
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : ThemeableActivity(), SimpleDialogFragment.SimpleCallback {
    private var mViewModel: MainActivityViewModel? = null
    private var mLUtils: LUtils? = null
    var drawer: Drawer? = null
        private set
    private var mMiniDrawer: MiniDrawer? = null
    private var crossFader: Crossfader<*>? = null
    private var mAccountHeader: AccountHeader? = null
    var isOnTablet: Boolean = false
        private set
    var hasThreeColumns: Boolean = false
        private set
    var isGridLayout: Boolean = false
        private set
    private var acct: GoogleSignInAccount? = null
    private var mSignInClient: GoogleSignInClient? = null
    private var mRegularFont: Typeface? = null
    private var mMediumFont: Typeface? = null

    private val nextStepReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Implement UI change code here once notification is received
            try {
                Log.v(TAG, "BROADCAST_NEXT_STEP")
                if (intent.getStringExtra("WHICH") != null) {
                    val which = intent.getStringExtra("WHICH")
                    Log.v(TAG, "NEXT_STEP: $which")
                    if (which.equals("RESTORE", ignoreCase = true)) {
                        val sFragment = ProgressDialogFragment.findVisible(this@MainActivity, "RESTORE_RUNNING")
                        sFragment?.setContent(R.string.restoring_settings)
                    } else {
                        val sFragment = ProgressDialogFragment.findVisible(this@MainActivity, "BACKUP_RUNNING")
                        sFragment?.setContent(R.string.backup_settings)
                    }
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, e.localizedMessage, e)
            }

        }
    }

    private val lastStepReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Implement UI change code here once notification is received
            try {
                Log.v(TAG, "BROADCAST_LAST_STEP")
                if (intent.getStringExtra("WHICH") != null) {
                    val which = intent.getStringExtra("WHICH")
                    Log.v(TAG, "NEXT_STEP: $which")
                    if (which.equals("RESTORE", ignoreCase = true)) {
                        dismissProgressDialog("RESTORE_RUNNING")
                        if (intent.getBooleanExtra("RESULT", false))
                            SimpleDialogFragment.Builder(this@MainActivity, this@MainActivity, "RESTART")
                                    .title(R.string.general_message)
                                    .content(R.string.gdrive_restore_success)
                                    .positiveButton(android.R.string.ok)
                                    .show()
                    } else {
                        dismissProgressDialog("BACKUP_RUNNING")
                        if (intent.getBooleanExtra("RESULT", false))
                            Snackbar.make(
                                    findViewById(R.id.main_content),
                                    R.string.gdrive_backup_success,
                                    Snackbar.LENGTH_LONG)
                                    .show()
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

        mRegularFont = ResourcesCompat.getFont(this@MainActivity, R.font.googlesans_regular)
        mMediumFont = ResourcesCompat.getFont(this@MainActivity, R.font.googlesans_medium)

        val icon = IconicsDrawable(this)
                .icon(CommunityMaterial.Icon2.cmd_menu)
                .color(Color.WHITE)
                .sizeDp(24)
                .paddingDp(2)

        risuscito_toolbar!!.setBackgroundColor(themeUtils!!.primaryColor())
        risuscito_toolbar!!.navigationIcon = icon
        setSupportActionBar(risuscito_toolbar)

        supportActionBar!!.setDisplayShowTitleEnabled(false)

        if (intent.getBooleanExtra(Utility.DB_RESET, false)) {
            TranslationTask(this@MainActivity).execute()
        }

        mLUtils = LUtils.getInstance(this@MainActivity)
        isOnTablet = mLUtils!!.isOnTablet
        Log.d(TAG, "onCreate: isOnTablet = $isOnTablet")
        hasThreeColumns = mLUtils!!.hasThreeColumns
        Log.d(TAG, "onCreate: hasThreeComlumns = $hasThreeColumns")
        isGridLayout = mLUtils!!.isGridLayout
        Log.d(TAG, "onCreate: isGridLayout = $isGridLayout")

        if (isOnTablet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.statusBarColor = themeUtils!!.primaryColorDark()

        if (isOnTablet)
            tabletToolbarBackground!!.setBackgroundColor(themeUtils!!.primaryColor())
        else
            material_tabs!!.setBackgroundColor(themeUtils!!.primaryColor())

        setupNavDrawer(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_frame, Risuscito(), R.id.navigation_home.toString())
                    .commit()
        }
        if (!isOnTablet) toolbar_layout!!.setExpanded(true, false)

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(Scope(Scopes.DRIVE_FILE), Scope(Scopes.DRIVE_FILE))
                .build()
        // [END configure_signin]

        // [START build_client]
        mSignInClient = GoogleSignIn.getClient(this, gso)
        // [END build_client]

        FirebaseAnalytics.getInstance(this)

        setDialogCallback("BACKUP_ASK")
        setDialogCallback("RESTORE_ASK")
        setDialogCallback("SIGNOUT")
        setDialogCallback("REVOKE")
        setDialogCallback("RESTART")

        // registra un receiver per ricevere la notifica di preparazione della registrazione
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(nextStepReceiver, IntentFilter("BROADCAST_NEXT_STEP"))
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(lastStepReceiver, IntentFilter("BROADCAST_LAST_STEP"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(nextStepReceiver)
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(lastStepReceiver)
    }

    public override fun onStart() {
        super.onStart()
        val task = mSignInClient!!.silentSignIn()
        if (task.isSuccessful) {
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

    override fun onResume() {
        super.onResume()
        hideProgressDialog()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        val mSavedInstanceState = drawer!!.saveInstanceState(savedInstanceState)
        super.onSaveInstanceState(mSavedInstanceState)
    }

    private fun setupNavDrawer(savedInstanceState: Bundle?) {

        val profile = ProfileDrawerItem()
                .withName("")
                .withEmail("")
                .withIcon(R.mipmap.profile_picture)
                .withIdentifier(PROF_ID)
                .withTypeface(mRegularFont)

        // Create the AccountHeader
        mAccountHeader = AccountHeaderBuilder()
                .withActivity(this@MainActivity)
                .withTranslucentStatusBar(!isOnTablet)
                .withSelectionListEnabledForSingleProfile(false)
                .withHeaderBackground(
                        if (isOnTablet)
                            ColorDrawable(ContextCompat.getColor(this, R.color.floating_background))
                        else
                            ColorDrawable(themeUtils!!.primaryColor()))
                .withSavedInstance(savedInstanceState)
                .addProfiles(profile)
                .withNameTypeface(mMediumFont!!)
                .withEmailTypeface(mRegularFont!!)
                .withOnAccountHeaderListener { _, mProfile, _ ->
                    // sample usage of the onProfileChanged listener
                    // if the clicked item has the identifier 1 add a new profile ;)
                    if (mProfile is IDrawerItem<*, *> && mProfile.getIdentifier() == R.id.gdrive_backup.toLong()) {
                        SimpleDialogFragment.Builder(
                                this@MainActivity, this@MainActivity, "BACKUP_ASK")
                                .title(R.string.gdrive_backup)
                                .content(R.string.gdrive_backup_content)
                                .positiveButton(android.R.string.yes)
                                .negativeButton(android.R.string.no)
                                .show()
                    } else if (mProfile is IDrawerItem<*, *> && mProfile.getIdentifier() == R.id.gdrive_restore.toLong()) {
                        SimpleDialogFragment.Builder(
                                this@MainActivity, this@MainActivity, "RESTORE_ASK")
                                .title(R.string.gdrive_restore)
                                .content(R.string.gdrive_restore_content)
                                .positiveButton(android.R.string.yes)
                                .negativeButton(android.R.string.no)
                                .show()
                    } else if (mProfile is IDrawerItem<*, *> && mProfile.getIdentifier() == R.id.gplus_signout.toLong()) {
                        SimpleDialogFragment.Builder(
                                this@MainActivity, this@MainActivity, "SIGNOUT")
                                .title(R.string.gplus_signout)
                                .content(R.string.dialog_acc_disconn_text)
                                .positiveButton(android.R.string.yes)
                                .negativeButton(android.R.string.no)
                                .show()
                    } else if (mProfile is IDrawerItem<*, *> && mProfile.getIdentifier() == R.id.gplus_revoke.toLong()) {
                        SimpleDialogFragment.Builder(
                                this@MainActivity, this@MainActivity, "REVOKE")
                                .title(R.string.gplus_revoke)
                                .content(R.string.dialog_acc_revoke_text)
                                .positiveButton(android.R.string.yes)
                                .negativeButton(android.R.string.no)
                                .show()
                    }

                    // false if you have not consumed the event and it should close the drawer
                    false
                }
                .build()

        val mDrawerBuilder = DrawerBuilder()
                .withActivity(this)
                .withToolbar(risuscito_toolbar!!)
                .withHasStableIds(true)
                .withAccountHeader(mAccountHeader!!)
                .addDrawerItems(
                        PrimaryDrawerItem()
                                .withName(R.string.activity_homepage)
                                .withIcon(CommunityMaterial.Icon2.cmd_home)
                                .withIdentifier(R.id.navigation_home.toLong())
                                .withSelectedIconColor(themeUtils!!.primaryColor())
                                .withSelectedTextColor(themeUtils!!.primaryColor())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.search_name_text)
                                .withIcon(CommunityMaterial.Icon2.cmd_magnify)
                                .withIdentifier(R.id.navigation_search.toLong())
                                .withSelectedIconColor(themeUtils!!.primaryColor())
                                .withSelectedTextColor(themeUtils!!.primaryColor())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_general_index)
                                .withIcon(CommunityMaterial.Icon2.cmd_view_list)
                                .withIdentifier(R.id.navigation_indexes.toLong())
                                .withSelectedIconColor(themeUtils!!.primaryColor())
                                .withSelectedTextColor(themeUtils!!.primaryColor())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_custom_lists)
                                .withIcon(CommunityMaterial.Icon2.cmd_view_carousel)
                                .withIdentifier(R.id.navitagion_lists.toLong())
                                .withSelectedIconColor(themeUtils!!.primaryColor())
                                .withSelectedTextColor(themeUtils!!.primaryColor())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.action_favourites)
                                .withIcon(CommunityMaterial.Icon2.cmd_heart)
                                .withIdentifier(R.id.navigation_favorites.toLong())
                                .withSelectedIconColor(themeUtils!!.primaryColor())
                                .withSelectedTextColor(themeUtils!!.primaryColor())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_consegnati)
                                .withIcon(CommunityMaterial.Icon.cmd_clipboard_check)
                                .withIdentifier(R.id.navigation_consegnati.toLong())
                                .withSelectedIconColor(themeUtils!!.primaryColor())
                                .withSelectedTextColor(themeUtils!!.primaryColor())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_history)
                                .withIcon(CommunityMaterial.Icon2.cmd_history)
                                .withIdentifier(R.id.navigation_history.toLong())
                                .withSelectedIconColor(themeUtils!!.primaryColor())
                                .withSelectedTextColor(themeUtils!!.primaryColor())
                                .withTypeface(mMediumFont),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_settings)
                                .withIcon(CommunityMaterial.Icon2.cmd_settings)
                                .withIdentifier(R.id.navigation_settings.toLong())
                                .withSelectedIconColor(themeUtils!!.primaryColor())
                                .withSelectedTextColor(themeUtils!!.primaryColor())
                                .withTypeface(mMediumFont),
                        DividerDrawerItem(),
                        PrimaryDrawerItem()
                                .withName(R.string.title_activity_about)
                                .withIcon(CommunityMaterial.Icon2.cmd_information_outline)
                                .withIdentifier(R.id.navigation_changelog.toLong())
                                .withSelectedIconColor(themeUtils!!.primaryColor())
                                .withSelectedTextColor(themeUtils!!.primaryColor())
                                .withTypeface(mMediumFont))
                .withOnDrawerItemClickListener(
                        Drawer.OnDrawerItemClickListener { _, _, drawerItem ->
                            // check if the drawerItem is set.
                            // there are different reasons for the drawerItem to be null
                            // --> click on the header
                            // --> click on the footer
                            // those items don't contain a drawerItem

                            if (drawerItem != null) {
                                val fragment: Fragment
                                if (drawerItem.identifier == R.id.navigation_home.toLong()) {
                                    fragment = Risuscito()
                                    if (!isOnTablet)
                                        toolbar_layout!!.setExpanded(true, true)
                                } else if (drawerItem.identifier == R.id.navigation_search.toLong()) {
                                    fragment = GeneralSearch()
                                } else if (drawerItem.identifier == R.id.navigation_indexes.toLong()) {
                                    fragment = GeneralIndex()
                                } else if (drawerItem.identifier == R.id.navitagion_lists.toLong()) {
                                    fragment = CustomLists()
                                } else if (drawerItem.identifier == R.id.navigation_favorites.toLong()) {
                                    fragment = FavouritesActivity()
                                } else if (drawerItem.identifier == R.id.navigation_settings.toLong()) {
                                    fragment = SettingsFragment()
                                } else if (drawerItem.identifier == R.id.navigation_changelog.toLong()) {
                                    fragment = AboutFragment()
                                } else if (drawerItem.identifier == R.id.navigation_consegnati.toLong()) {
                                    fragment = ConsegnatiFragment()
                                } else if (drawerItem.identifier == R.id.navigation_history.toLong()) {
                                    fragment = HistoryFragment()
                                } else
                                    return@OnDrawerItemClickListener true

                                // creo il nuovo fragment solo se non è lo stesso che sto già visualizzando
                                val myFragment = supportFragmentManager
                                        .findFragmentByTag(drawerItem.identifier.toString())
                                if (myFragment == null || !myFragment.isVisible) {
                                    val transaction = supportFragmentManager.beginTransaction()
                                    if (!isOnTablet)
                                        transaction.setCustomAnimations(
                                                R.anim.slide_in_right, R.anim.slide_out_left)
                                    transaction
                                            .replace(
                                                    R.id.content_frame,
                                                    fragment,
                                                    drawerItem.identifier.toString())
                                            .commit()
                                }

                                if (isOnTablet) mMiniDrawer!!.setSelection(drawerItem.identifier)
                            }
                            isOnTablet
                        })
                .withGenerateMiniDrawer(isOnTablet)
                .withSavedInstance(savedInstanceState)
                .withTranslucentStatusBar(!isOnTablet)

        if (isOnTablet) {
            drawer = mDrawerBuilder.buildView()
            // the MiniDrawer is managed by the Drawer and we just get it to hook it into the Crossfader
            mMiniDrawer = drawer!!
                    .miniDrawer
                    .withEnableSelectedMiniDrawerItemBackground(true)
                    .withIncludeSecondaryDrawerItems(true)

            // get the widths in px for the first and second panel
            val firstWidth = UIUtils.convertDpToPixel(302f, this).toInt()
            val secondWidth = UIUtils.convertDpToPixel(72f, this).toInt()

            // create and build our crossfader (see the MiniDrawer is also builded in here, as the build
            // method returns the view to be used in the crossfader)
            crossFader = Crossfader<MyCrossfaderClass>()
                    .withContent(findViewById<View>(R.id.main_frame))
                    .withFirst(drawer!!.slider, firstWidth)
                    .withSecond(mMiniDrawer!!.build(this), secondWidth)
                    .withSavedInstance(savedInstanceState)
                    .withGmailStyleSwiping()
                    .build()

            // define the crossfader to be used with the miniDrawer. This is required to be able to
            // automatically toggle open / close
            mMiniDrawer!!.withCrossFader(CrossfadeWrapper(crossFader as Crossfader<*>))

            // define a shadow (this is only for normal LTR layouts if you have a RTL app you need to
            // define the other one
            crossFader!!
                    .getCrossFadeSlidingPaneLayout()
                    .setShadowResourceLeft(R.drawable.material_drawer_shadow_left)
            crossFader!!
                    .getCrossFadeSlidingPaneLayout()
                    .setShadowResourceRight(R.drawable.material_drawer_shadow_right)
        } else {
            drawer = mDrawerBuilder.build()
            drawer!!.drawerLayout.setStatusBarBackgroundColor(themeUtils!!.primaryColorDark())
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")

        if (fab_pager.isOpen) {
            fab_pager.close()
            return
        }

        if (isOnTablet) {
            if (crossFader != null && crossFader!!.isCrossFaded()) {
                crossFader!!.crossFade()
                return
            }
        } else {
            if (drawer != null && drawer!!.isDrawerOpen) {
                drawer!!.closeDrawer()
                return
            }
        }

        backToHome()
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

        val mDao = RisuscitoDatabase.getInstance(this@MainActivity).cantoDao()
        val canti = mDao.allByName
        for (canto in canti) {
            if (canto.savedTab != null && !canto.savedTab!!.isEmpty()) {
                Log.d(
                        TAG,
                        "convertTabs: "
                                + "ID "
                                + canto.id
                                + " -> CONVERTO DA "
                                + canto.savedTab
                                + " A "
                                + mappa[canto.savedTab!!])
                canto.savedTab = mappa[canto.savedTab!!]
                mDao.updateCanto(canto)
            }
        }
    }

    // converte gli accordi salvati dalla lingua vecchia alla nuova
    private fun convertiBarre() {
        val conversion = intent.getStringExtra(Utility.CHANGE_LANGUAGE)

        var barre1 = CambioAccordi.barre_it
        Log.d(TAG, "convertiBarre - from: " + conversion.substring(0, 2))
        when (conversion.substring(0, 2)) {
            "uk" -> barre1 = CambioAccordi.barre_uk
            "en" -> barre1 = CambioAccordi.barre_en
        }

        var barre2 = CambioAccordi.barre_it
        Log.d(TAG, "convertiBarre - to: " + conversion.substring(3, 5))
        when (conversion.substring(3, 5)) {
            "uk" -> barre2 = CambioAccordi.barre_uk
            "en" -> barre2 = CambioAccordi.barre_en
        }

        val mappa = HashMap<String, String>()
        for (i in CambioAccordi.barre_it.indices) mappa[barre1[i]] = barre2[i]

        val mDao = RisuscitoDatabase.getInstance(this@MainActivity).cantoDao()
        val canti = mDao.allByName
        for (canto in canti) {
            if (canto.savedTab != null && !canto.savedTab!!.isEmpty()) {
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
        risuscito_toolbar!!.title = getString(titleResId)
    }

    fun closeFabMenu() {
        if (fab_pager!!.isOpen)
            fab_pager!!.close()
    }

    fun toggleFabMenu() {
        fab_pager!!.toggle()
    }

    fun enableFab(enable: Boolean) {
        Log.d(TAG, "enableFab: $enable")
        if (enable) {
            if (fab_pager!!.isOpen)
                fab_pager!!.close()
            else {
                val params = fab_pager!!.layoutParams as CoordinatorLayout.LayoutParams
                params.behavior = if (mLUtils!!.isFabScrollingActive) SpeedDialView.ScrollingViewSnackbarBehavior() else SpeedDialView.NoBehavior()
                fab_pager.requestLayout()
                fab_pager.show()
                // This is a workaround for support library bug, call this instead of show() above.
//                fab_pager.show(object : FloatingActionButton.OnVisibilityChangedListener() {
//                    override fun onShown(fab: FloatingActionButton) {
//                        val impl = fab::class.java.getDeclaredField("impl").let {
//                            it.isAccessible = true
//                            it.get(fab)
//                        }
//
//                        val implClass = if (Build.VERSION.SDK_INT >= 21) {
//                            impl::class.java.superclass
//                        } else {
//                            impl::class.java
//                        }
//
//                        val scale =
//                                implClass.getDeclaredMethod("setImageMatrixScale", Float::class.java)
//                        scale.isAccessible = true
//                        scale.invoke(impl, 1.0F)
//                    }
//                })
            }
        } else {
            if (fab_pager!!.isOpen)
                fab_pager!!.close()
            fab_pager.hide()
            val params = fab_pager!!.layoutParams as CoordinatorLayout.LayoutParams
            params.behavior = SpeedDialView.NoBehavior()
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
            fab_pager.expansionMode = if (mLUtils!!.isFabScrollingActive) (if (mLUtils!!.isLandscape) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP) else SpeedDialView.ExpansionMode.BOTTOM
            val iconColor = ContextCompat.getColor(this@MainActivity, R.color.text_color_secondary)
            val backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.floating_background)

            fab_pager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_pulisci, IconicsDrawable(this@MainActivity)
                            .icon(CommunityMaterial.Icon.cmd_eraser_variant)
                            .color(iconColor)
                            .sizeDp(24)
                            .paddingDp(4))
                            .setLabel(getString(R.string.button_clean_list))
                            .setFabBackgroundColor(backgroundColor)
                            .setLabelBackgroundColor(backgroundColor)
                            .setLabelColor(iconColor)
                            .create()
            )

            fab_pager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_add_lista, IconicsDrawable(this@MainActivity)
                            .icon(CommunityMaterial.Icon2.cmd_plus)
                            .color(iconColor)
                            .sizeDp(24)
                            .paddingDp(4))
                            .setLabel(getString(R.string.action_add_list))
                            .setFabBackgroundColor(backgroundColor)
                            .setLabelBackgroundColor(backgroundColor)
                            .setLabelColor(iconColor)
                            .create()
            )

            fab_pager.addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_condividi, IconicsDrawable(this@MainActivity)
                            .icon(CommunityMaterial.Icon2.cmd_share_variant)
                            .color(iconColor)
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
                        SpeedDialActionItem.Builder(R.id.fab_condividi_file, IconicsDrawable(this@MainActivity)
                                .icon(CommunityMaterial.Icon.cmd_attachment)
                                .color(iconColor)
                                .sizeDp(24)
                                .paddingDp(4))
                                .setLabel(getString(R.string.action_share_file))
                                .setFabBackgroundColor(backgroundColor)
                                .setLabelBackgroundColor(backgroundColor)
                                .setLabelColor(iconColor)
                                .create()
                )

                fab_pager.addActionItem(
                        SpeedDialActionItem.Builder(R.id.fab_edit_lista, IconicsDrawable(this@MainActivity)
                                .icon(CommunityMaterial.Icon2.cmd_pencil)
                                .color(iconColor)
                                .sizeDp(24)
                                .paddingDp(4))
                                .setLabel(getString(R.string.action_edit_list))
                                .setFabBackgroundColor(backgroundColor)
                                .setLabelBackgroundColor(backgroundColor)
                                .setLabelColor(iconColor)
                                .create()
                )

                fab_pager.addActionItem(
                        SpeedDialActionItem.Builder(R.id.fab_delete_lista, IconicsDrawable(this@MainActivity)
                                .icon(CommunityMaterial.Icon.cmd_delete)
                                .color(iconColor)
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
            val mBottomBar = findViewById<View>(R.id.bottom_bar)
            if (enabled)
                mLUtils!!.animateIn(mBottomBar)
            else
                mBottomBar.visibility = View.GONE
        }
    }

    // [START signIn]
    fun signIn() {
        //    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        val signInIntent = mSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // [START signOut]
    private fun signOut() {
        mSignInClient!!
                .signOut()
                .addOnCompleteListener {
                    updateUI(false)
                    PreferenceManager.getDefaultSharedPreferences(this@MainActivity).edit { putBoolean(Utility.SIGNED_IN, false) }
                    Toast.makeText(this@MainActivity, R.string.disconnected, Toast.LENGTH_SHORT)
                            .show()
                }
    }

    // [START revokeAccess]
    private fun revokeAccess() {
        mSignInClient!!
                .revokeAccess()
                .addOnCompleteListener {
                    updateUI(false)
                    PreferenceManager.getDefaultSharedPreferences(this@MainActivity).edit { putBoolean(Utility.SIGNED_IN, false) }
                    Toast.makeText(this@MainActivity, R.string.disconnected, Toast.LENGTH_SHORT)
                            .show()
                }
    }

    // [START onActivityResult]
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
            acct = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
            PreferenceManager.getDefaultSharedPreferences(this@MainActivity).edit { putBoolean(Utility.SIGNED_IN, true) }
            if (mViewModel!!.showSnackbar) {
                Toast.makeText(this@MainActivity, getString(R.string.connected_as, acct!!.displayName), Toast.LENGTH_SHORT)
                        .show()
                mViewModel!!.showSnackbar = false
            }
            updateUI(true)
        } else {
            // Sign in failed, handle failure and update UI
            Toast.makeText(this@MainActivity, getString(
                    R.string.login_failed,
                    -1,
                    task.exception!!.localizedMessage), Toast.LENGTH_SHORT)
                    .show()
            acct = null
            updateUI(false)
        }
    }

    private fun updateUI(signedIn: Boolean) {
        val intentBroadcast = Intent(Risuscito.BROADCAST_SIGNIN_VISIBLE)
        Log.d(TAG, "updateUI: DATA_VISIBLE " + !signedIn)
        intentBroadcast.putExtra(Risuscito.DATA_VISIBLE, !signedIn)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
        if (signedIn) {
            val profile: IProfile<*>
            val profilePhoto = acct!!.photoUrl
            if (profilePhoto != null) {
                var personPhotoUrl = profilePhoto.toString()
                personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length - 2) + 400
                profile = ProfileDrawerItem()
                        .withName(acct!!.displayName)
                        .withEmail(acct!!.email)
                        .withIcon(personPhotoUrl)
                        .withIdentifier(PROF_ID)
                        .withTypeface(mRegularFont)
            } else {
                profile = ProfileDrawerItem()
                        .withName(acct!!.displayName)
                        .withEmail(acct!!.email)
                        .withIcon(R.mipmap.profile_picture)
                        .withIdentifier(PROF_ID)
                        .withTypeface(mRegularFont)
            }
            // Create the AccountHeader
            mAccountHeader!!.updateProfile(profile)
            if (mAccountHeader!!.profiles.size == 1) {
                mAccountHeader!!.addProfiles(
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
            if (isOnTablet) mMiniDrawer!!.onProfileClick()
        } else {
            val profile = ProfileDrawerItem()
                    .withName("")
                    .withEmail("")
                    .withIcon(R.mipmap.profile_picture)
                    .withIdentifier(PROF_ID)
                    .withTypeface(mRegularFont)
            if (mAccountHeader!!.profiles.size > 1) {
                mAccountHeader!!.removeProfile(1)
                mAccountHeader!!.removeProfile(1)
                mAccountHeader!!.removeProfile(1)
                mAccountHeader!!.removeProfile(1)
            }
            mAccountHeader!!.updateProfile(profile)
            if (isOnTablet) mMiniDrawer!!.onProfileClick()
        }
        hideProgressDialog()
    }

    private fun showProgressDialog() {
        loadingBar!!.visibility = View.VISIBLE
    }

    private fun hideProgressDialog() {
        loadingBar!!.visibility = View.GONE
    }

    fun setShowSnackbar() {
        this.mViewModel!!.showSnackbar = true
    }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: TAG $tag")
        when (tag) {
            "BACKUP_ASK" -> {
//                SimpleDialogFragment.Builder(this@MainActivity, this@MainActivity, "BACKUP_RUNNING")
//                        .title(R.string.backup_running)
//                        .content(R.string.backup_database)
//                        .showProgress()
//                        .progressIndeterminate(true)
//                        .progressMax(0)
//                        .show()
                ProgressDialogFragment.Builder(this@MainActivity, null, "BACKUP_RUNNING")
                        .title(R.string.backup_running)
                        .content(R.string.backup_database)
                        .progressIndeterminate(true)
                        .show()
                backToHome()
                BackupTask().execute()
            }
            "RESTORE_ASK" -> {
//                SimpleDialogFragment.Builder(this@MainActivity, this@MainActivity, "RESTORE_RUNNING")
//                        .title(R.string.restore_running)
//                        .content(R.string.restoring_database)
//                        .showProgress()
//                        .progressIndeterminate(true)
//                        .progressMax(0)
//                        .show()
                ProgressDialogFragment.Builder(this@MainActivity, null, "RESTORE_RUNNING")
                        .title(R.string.restore_running)
                        .content(R.string.restoring_database)
                        .progressIndeterminate(true)
                        .show()
                backToHome()
                RestoreTask().execute()
            }
            "SIGNOUT" -> signOut()
            "REVOKE" -> revokeAccess()
            "RESTART" -> {
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
            crossFader!!.crossFade()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNegative(tag: String) {}

    override fun onNeutral(tag: String) {}

//    private fun dismissDialog(tag: String) {
//        val sFragment = SimpleDialogFragment.findVisible(this@MainActivity, tag)
//        sFragment?.dismiss()
//    }

    private fun dismissProgressDialog(tag: String) {
        val sFragment = ProgressDialogFragment.findVisible(this@MainActivity, tag)
        sFragment?.dismiss()
    }

    private fun setDialogCallback(tag: String) {
        val sFragment = SimpleDialogFragment.findVisible(this@MainActivity, tag)
        sFragment?.setmCallback(this@MainActivity)
    }

    private class TranslationTask internal constructor(activity: MainActivity) : AsyncTask<Void, Void, Void>() {

        private val activityWeakReference: WeakReference<MainActivity> = WeakReference(activity)

        override fun doInBackground(vararg sUrl: Void): Void? {
            activityWeakReference.get()!!.intent.removeExtra(Utility.DB_RESET)
            activityWeakReference.get()!!.convertTabs()
            activityWeakReference.get()!!.convertiBarre()
            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
//            SimpleDialogFragment.Builder(
//                    activityWeakReference.get()!!, activityWeakReference.get()!!, "TRANSLATION")
//                    .content(R.string.translation_running)
//                    .showProgress()
//                    .progressIndeterminate(true)
//                    .progressMax(0)
//                    .show()
            ProgressDialogFragment.Builder(activityWeakReference.get()!!, null, "TRANSLATION")
                    .content(R.string.translation_running)
                    .progressIndeterminate(true)
                    .show()
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            activityWeakReference.get()!!.intent.removeExtra(Utility.CHANGE_LANGUAGE)
            try {
                activityWeakReference.get()!!.dismissProgressDialog("TRANSLATION")
            } catch (e: IllegalArgumentException) {
                Log.e(javaClass.name, e.localizedMessage, e)
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class BackupTask : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg sUrl: Void): Boolean {
            return try {
                checkDuplTosave(RisuscitoDatabase.dbName, "application/x-sqlite3", true)
                val intentBroadcast = Intent("BROADCAST_NEXT_STEP")
                intentBroadcast.putExtra("WHICH", "BACKUP")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                checkDuplTosave(PREF_DRIVE_FILE_NAME, "application/json", false)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.localizedMessage, e)
                val error = "error: " + e.localizedMessage
                Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show()
                false
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            val intentBroadcast = Intent("BROADCAST_LAST_STEP")
            intentBroadcast.putExtra("WHICH", "BACKUP")
            intentBroadcast.putExtra("RESULT", result)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class RestoreTask : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg sUrl: Void): Boolean {
            try {
                if (checkDupl(RisuscitoDatabase.dbName))
                    restoreNewDbBackup()
                else
                    restoreOldDriveBackup()
                val intentBroadcast = Intent("BROADCAST_NEXT_STEP")
                intentBroadcast.putExtra("WHICH", "RESTORE")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                restoreDrivePrefBackup(PREF_DRIVE_FILE_NAME)
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.localizedMessage, e)
                val error = "error: " + e.localizedMessage
                Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show()
                return false
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            val intentBroadcast = Intent("BROADCAST_LAST_STEP")
            intentBroadcast.putExtra("WHICH", "RESTORE")
            intentBroadcast.putExtra("RESULT", result)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
        }
    }

    private inner class MyCrossfaderClass(context: Context) : SlidingPaneLayout(context), ICrossFadeSlidingPaneLayout {
        override fun setCanSlide(canSlide: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun setOffset(slideOffset: Float) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    private fun backToHome() {
        val myFragment = supportFragmentManager.findFragmentByTag(R.id.navigation_home.toString())
        if (myFragment != null && myFragment.isVisible) {
            finish()
            return
        }

        if (isOnTablet)
            mMiniDrawer!!.setSelection(R.id.navigation_home.toLong())
        else {
            toolbar_layout!!.setExpanded(true, true)
        }
        drawer!!.setSelection(R.id.navigation_home.toLong())
    }

    companion object {
        /* Request code used to invoke sign in user interactions. */
        private const val RC_SIGN_IN = 9001
        private const val PREF_DRIVE_FILE_NAME = "preferences_backup"
        private const val PROF_ID = 5428471L
        private val TAG = MainActivity::class.java.canonicalName
    }
}
