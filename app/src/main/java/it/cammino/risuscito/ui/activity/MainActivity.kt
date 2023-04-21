package it.cammino.risuscito.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.search.SearchView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.jakewharton.processphoenix.ProcessPhoenix
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.michaelflisar.changelog.ChangelogBuilder
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.squareup.picasso.Picasso
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.databinding.ActivityMainBinding
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.dialog.DialogState
import it.cammino.risuscito.ui.dialog.ProfileDialogFragment
import it.cammino.risuscito.ui.dialog.ProgressDialogFragment
import it.cammino.risuscito.ui.dialog.SimpleDialogFragment
import it.cammino.risuscito.ui.fragment.AboutFragment
import it.cammino.risuscito.ui.fragment.ConsegnatiFragment
import it.cammino.risuscito.ui.fragment.CustomListsFragment
import it.cammino.risuscito.ui.fragment.FavoritesFragment
import it.cammino.risuscito.ui.fragment.GeneralIndexFragment
import it.cammino.risuscito.ui.fragment.HistoryFragment
import it.cammino.risuscito.ui.fragment.SettingsFragment
import it.cammino.risuscito.ui.interfaces.ActionModeFragment
import it.cammino.risuscito.utils.CambioAccordi
import it.cammino.risuscito.utils.CantiXmlParser
import it.cammino.risuscito.utils.LocaleManager.Companion.LANGUAGE_ENGLISH
import it.cammino.risuscito.utils.LocaleManager.Companion.LANGUAGE_ENGLISH_PHILIPPINES
import it.cammino.risuscito.utils.LocaleManager.Companion.LANGUAGE_POLISH
import it.cammino.risuscito.utils.LocaleManager.Companion.LANGUAGE_UKRAINIAN
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.Utility.CHANGE_LANGUAGE
import it.cammino.risuscito.utils.Utility.NEW_LANGUAGE
import it.cammino.risuscito.utils.Utility.OLD_LANGUAGE
import it.cammino.risuscito.utils.extension.dynamicColorOptions
import it.cammino.risuscito.utils.extension.getTypedValueResId
import it.cammino.risuscito.utils.extension.getVersionCode
import it.cammino.risuscito.utils.extension.isDarkMode
import it.cammino.risuscito.utils.extension.isFabExpansionLeft
import it.cammino.risuscito.utils.extension.isGridLayout
import it.cammino.risuscito.utils.extension.isOnTablet
import it.cammino.risuscito.utils.extension.openCanto
import it.cammino.risuscito.utils.extension.startActivityWithTransition
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.utils.extension.updateListaPersonalizzata
import it.cammino.risuscito.utils.extension.updatePosizione
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
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
    private var acct: GoogleSignInAccount? = null
    private var mSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private var profileItem: MenuItem? = null
    private var profilePhotoUrl: String = StringUtils.EMPTY
    private var profileNameStr: String = StringUtils.EMPTY
    private var profileEmailStr: String = StringUtils.EMPTY

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Snackbar.make(
                binding.mainContent,
                getString(R.string.permission_ok),
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit { putString(Utility.SAVE_LOCATION, "0") }
            Snackbar.make(
                binding.mainContent,
                getString(R.string.external_storage_denied),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    //search proprerties
    private var job: Job = Job()
    private val cantoAdapter: FastItemAdapter<SimpleItem> = FastItemAdapter()
    private var listePersonalizzate: List<ListaPers>? = null
    private var mLastClickTime: Long = 0

    private lateinit var binding: ActivityMainBinding

    //    var actionMode: ActionMode? = null
//        private set
    var isActionMode: Boolean = false
        private set
    private var actionModeFragment: ActionModeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        DynamicColors.applyToActivityIfAvailable(this, dynamicColorOptions)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val outValue = TypedValue()
        resources.getValue(R.dimen.horizontal_percentage_half_divider, outValue, true)
        val percentage = outValue.float

        binding.halfGuideline?.setGuidelinePercent(percentage)

        binding.contextualToolbar.setNavigationOnClickListener { destroyActionMode() }

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.contextualToolbarContainer
        ) { insetsView: View, insets: WindowInsetsCompat ->
            val systemInsetTop =
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            insetsView.setPadding(0, systemInsetTop, 0, 0)
            insets
        }

        lifecycleScope.launch(Dispatchers.IO) {
            listePersonalizzate =
                RisuscitoDatabase.getInstance(this@MainActivity).listePersDao().all
        }

        Log.d(TAG, "getVersionCode(): ${getVersionCode()}")

        ChangelogBuilder()
            .withUseBulletList(true) // true if you want to show bullets before each changelog row, false otherwise
            .withMinVersionToShow(getVersionCode())     // provide a number and the log will only show changelog rows for versions equal or higher than this number
            .withManagedShowOnStart(
                getSharedPreferences(
                    "com.michaelflisar.changelog",
                    0
                ).getInt("changelogVersion", -1) != -1
            )  // library will take care to show activity/dialog only if the changelog has new infos and will only show this new infos
            .withTitle(getString(R.string.dialog_change_title)) // provide a custom title if desired, default one is "Changelog <VERSION>"
            .withOkButtonLabel(getString(R.string.ok)) // provide a custom ok button text if desired, default one is "OK"
            .buildAndShowDialog(
                this,
                isDarkMode
            ) // second parameter defines, if the dialog has a dark or light theme

        if (!OSUtils.hasQ())
            checkPermission()

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(
                    R.id.content_frame,
                    GeneralIndexFragment(),
                    R.id.navigation_indexes.toString()
                )
            }

            val currentItem = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(Utility.DEFAULT_SEARCH, "0") ?: "0"
            )
            cantiViewModel.advancedSearch = currentItem != 0
        }

        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.fileout)
            cantiViewModel.aTexts = CantiXmlParser().parse(inputStream)
            inputStream.close()
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Error:", e)
            Firebase.crashlytics.recordException(e)
        } catch (e: IOException) {
            Log.e(TAG, "Error:", e)
            Firebase.crashlytics.recordException(e)
        }

        onBackPressedDispatcher.addCallback(this) {
            when {
                binding.fabPager.isOpen -> binding.fabPager.close()
                !isOnTablet && (binding.drawer as? DrawerLayout)?.isOpen == true
                -> (binding.drawer as? DrawerLayout)?.close()
                isActionMode -> destroyActionMode()
                binding.searchViewLayout.searchViewContainer.currentTransitionState == SearchView.TransitionState.SHOWN -> binding.searchViewLayout.searchViewContainer.hide()
                else -> backToHome(true)
            }
        }

        setSupportActionBar(binding.risuscitoToolbar)

        if (intent.getBooleanExtra(CHANGE_LANGUAGE, false)) {
            lifecycleScope.launch { translate() }
        }

        setupNavDrawer()

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

        cantoAdapter.onClickListener =
            { _: View?, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
                var consume = false
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    openCanto(
                        TAG,
                        item.id,
                        item.source?.getText(this),
                        false
                    )
                    consume = true
                }
                consume
            }

        cantoAdapter.onLongClickListener =
            { v: View, _: IAdapter<SimpleItem>, item: SimpleItem, _: Int ->
                cantiViewModel.idDaAgg = item.id
                cantiViewModel.popupMenu(
                    this, v,
                    SEARCH_REPLACE,
                    SEARCH_REPLACE_2, listePersonalizzate
                )
                true
            }

        cantoAdapter.setHasStableIds(true)

        binding.searchViewLayout.matchedList.adapter = cantoAdapter
        val llm = if (isGridLayout)
            GridLayoutManager(this, 2)
        else
            LinearLayoutManager(this)
        binding.searchViewLayout.matchedList.layoutManager = llm

        binding.searchViewLayout.searchViewContainer
            .editText
            .setOnEditorActionListener { _, actionId, _ ->
                var returnValue = false
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // to hide soft keyboard
                    ContextCompat.getSystemService(this, InputMethodManager::class.java)
                        ?.hideSoftInputFromWindow(
                            binding.searchViewLayout.searchViewContainer
                                .editText.windowToken, 0
                        )
                    returnValue = true
                }
                returnValue
            }

        binding.searchViewLayout.searchViewContainer
            .editText.doOnTextChanged { text, _, _, _ ->
                job.cancel()
                ricercaStringa(text.toString())
            }

        binding.searchViewLayout.advancedSearchChip.isChecked = cantiViewModel.advancedSearch
        binding.searchViewLayout.advancedSearchChip.setOnCheckedChangeListener { _, checked ->
            cantiViewModel.advancedSearch = checked
            job.cancel()
            ricercaStringa(binding.searchViewLayout.searchViewContainer.text.toString())
        }

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
//            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        })

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
        hideProgressDialog()

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.fabPager.expansionMode =
            if (isFabExpansionLeft) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP
    }

    private fun ricercaStringa(s: String) {
        job = lifecycleScope.launch {
            // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
            if (s.trim { it <= ' ' }.length >= 3) {
                binding.searchViewLayout.searchNoResults.isVisible = false
                binding.searchViewLayout.searchProgress.isVisible = true
                val titoliResult = ArrayList<SimpleItem>()

                Firebase.crashlytics.log("function: search_text - search_string: $s - advanced: ${cantiViewModel.advancedSearch}")

                Log.d(TAG, "performSearch STRINGA: $s")
                Log.d(TAG, "performSearch ADVANCED: ${cantiViewModel.advancedSearch}")
                if (cantiViewModel.advancedSearch) {
                    val words =
                        s.split("\\W".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    for (aText in cantiViewModel.aTexts) {
                        if (!isActive) return@launch

                        if (aText[0] == null || aText[0].isNullOrEmpty()) break

                        var found = true
                        for (word in words) {
                            if (!isActive) return@launch

                            if (word.trim { it <= ' ' }.length > 1) {
                                var text = word.trim { it <= ' ' }
                                text = text.lowercase(resources.systemLocale)
                                text = Utility.removeAccents(text)

                                if (aText[1]?.contains(text) != true) found = false
                            }
                        }

                        if (found) {
                            Log.d(TAG, "aText[0]: ${aText[0]}")
                            cantiViewModel.titoli
                                .filter { (aText[0].orEmpty()) == it.undecodedSource }
                                .forEach {
                                    if (!isActive) return@launch
                                    titoliResult.add(it.apply { filter = StringUtils.EMPTY })
                                }
                        }
                    }
                } else {
                    val stringa = Utility.removeAccents(s).lowercase(resources.systemLocale)
                    Log.d(TAG, "performSearch onTextChanged: stringa $stringa")
                    cantiViewModel.titoli
                        .filter {
                            Utility.removeAccents(
                                it.title?.getText(this@MainActivity).orEmpty()
                            ).lowercase(resources.systemLocale).contains(stringa)
                        }
                        .forEach {
                            if (!isActive) return@launch
                            titoliResult.add(it.apply { filter = stringa })
                        }
                }
                if (isActive) {
                    cantoAdapter.set(
                        titoliResult.sortedWith(
                            compareBy(
                                Collator.getInstance(resources.systemLocale)
                            ) { it.title?.getText(this@MainActivity) })
                    )
                    binding.searchViewLayout.searchProgress.isVisible = false
                    binding.searchViewLayout.searchNoResults.isVisible =
                        cantoAdapter.adapterItemCount == 0
                    binding.searchViewLayout.matchedList.isGone = cantoAdapter.adapterItemCount == 0
                }
            } else {
                if (s.isEmpty()) {
                    binding.searchViewLayout.searchNoResults.isVisible = false
                    binding.searchViewLayout.matchedList.isVisible = false
                    cantoAdapter.clear()
                    binding.searchViewLayout.searchProgress.isVisible = false
                    expandToolbar()
                }
            }
        }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "permission granted")
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.external_storage_pref_rationale)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        run {
                            dialog.cancel()
                            requestPermissionLauncher.launch(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
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
                                lifecycleScope.launch { backupDbPrefs() }
                            }
                            RESTORE_ASK -> {
                                simpleDialogViewModel.handled = true
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
                            RESTORE_DONE -> {
                                simpleDialogViewModel.handled = true
                                ProcessPhoenix.triggerRebirth(this)
                            }
                            SEARCH_REPLACE -> {
                                simpleDialogViewModel.handled = true
                                listePersonalizzate?.let { lista ->
                                    lista[cantiViewModel.idListaClick]
                                        .lista?.addCanto(
                                            cantiViewModel.idDaAgg.toString(),
                                            cantiViewModel.idPosizioneClick
                                        )
                                    updateListaPersonalizzata(lista[cantiViewModel.idListaClick])
                                }
                            }
                            SEARCH_REPLACE_2 -> {
                                simpleDialogViewModel.handled = true
                                updatePosizione(
                                    cantiViewModel.idDaAgg,
                                    cantiViewModel.idListaDaAgg,
                                    cantiViewModel.posizioneDaAgg
                                )
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
                    MainActivityViewModel.BakupRestoreState.RESTORE_STARTED ->
                        ProgressDialogFragment.show(
                            ProgressDialogFragment.Builder(RESTORE_RUNNING).apply {
                                title = R.string.restore_running
                                icon = R.drawable.cloud_download_24px
                                content = R.string.restoring_database
                                progressIndeterminate = true
                            },
                            supportFragmentManager
                        )
                    MainActivityViewModel.BakupRestoreState.RESTORE_STEP_2 -> {
                        val sFragment =
                            ProgressDialogFragment.findVisible(this@MainActivity, RESTORE_RUNNING)
                        sFragment?.setContent(R.string.restoring_settings)
                    }
                    MainActivityViewModel.BakupRestoreState.RESTORE_COMPLETED ->
                        dismissProgressDialog(RESTORE_RUNNING)
                    MainActivityViewModel.BakupRestoreState.BACKUP_STARTED ->
                        ProgressDialogFragment.show(
                            ProgressDialogFragment.Builder(BACKUP_RUNNING).apply {
                                title = R.string.backup_running
                                icon = R.drawable.cloud_upload_24px
                                content = R.string.backup_database
                                progressIndeterminate = true
                            },
                            supportFragmentManager
                        )
                    MainActivityViewModel.BakupRestoreState.BACKUP_STEP_2 -> {
                        val sFragment =
                            ProgressDialogFragment.findVisible(this@MainActivity, BACKUP_RUNNING)
                        sFragment?.setContent(R.string.backup_settings)
                    }
                    MainActivityViewModel.BakupRestoreState.BACKUP_COMPLETED ->
                        dismissProgressDialog(BACKUP_RUNNING)
                    MainActivityViewModel.BakupRestoreState.NONE -> {}
                }
            }
        }

        cantiViewModel.itemsResult?.observe(this) { canti ->
            cantiViewModel.titoli =
                canti.sortedWith(compareBy(
                    Collator.getInstance(resources.systemLocale)
                ) {
                    it.title?.getText(this)
                })
        }

    }

    private fun setupNavDrawer() {

        val listener = NavigationBarView.OnItemSelectedListener { item ->
            onDrawerItemClick(item)
        }

        binding.bottomNavigation?.setOnItemSelectedListener(listener)
        binding.navigationView?.setNavigationItemSelectedListener {
            onMobileDrawerItemClick(it)
        }
        binding.navigationRail?.setOnItemSelectedListener(listener)

        binding.bottomNavigation?.menu?.findItem(mViewModel.selectedMenuItemId)?.isChecked = true
        binding.navigationRail?.menu?.findItem(mViewModel.selectedMenuItemId)?.isChecked = true

        if (!isOnTablet) {
            val mActionBarDrawerToggle = ActionBarDrawerToggle(
                this,
                binding.drawer as DrawerLayout,
                binding.risuscitoToolbar,
                R.string.material_drawer_open,
                R.string.material_drawer_close
            )
            mActionBarDrawerToggle.syncState()
            (binding.drawer as? DrawerLayout)?.addDrawerListener(mActionBarDrawerToggle)
        }

        binding.navigationView?.getHeaderView(0)?.findViewById<TextView>(R.id.drawer_header_title)
            ?.setTextColor(
                MaterialColors.harmonizeWithPrimary(
                    this,
                    ContextCompat.getColor(this, R.color.ic_launcher_background)
                )
            )

    }

    private fun onDrawerItemClick(menuItem: MenuItem): Boolean {
        expandToolbar()

        val fragment = when (menuItem.itemId) {
            R.id.navigation_indexes -> GeneralIndexFragment()
            R.id.navigation_lists -> CustomListsFragment()
            R.id.navigation_favorites -> FavoritesFragment()
            R.id.navigation_settings -> SettingsFragment()
            R.id.navigation_changelog -> AboutFragment()
            R.id.navigation_consegnati -> ConsegnatiFragment()
            R.id.navigation_history -> HistoryFragment()
            else -> GeneralIndexFragment()
        }

        mViewModel.selectedMenuItemId = menuItem.itemId
        menuItem.isChecked = true

        (binding.drawer as? DrawerLayout)?.close()

        // creo il nuovo fragment solo se non è lo stesso che sto già visualizzando
        val myFragment = supportFragmentManager
            .findFragmentByTag(menuItem.itemId.toString())
        if (myFragment == null || !myFragment.isVisible) {
            supportFragmentManager.commit {
                replace(R.id.content_frame, fragment, menuItem.itemId.toString())
            }
        }

        return true
    }

    private fun onMobileDrawerItemClick(menuItem: MenuItem): Boolean {
        expandToolbar()
        (binding.drawer as? DrawerLayout)?.close()

        val activityClass = when (menuItem.itemId) {
            R.id.navigation_settings -> SettingsActivity::class.java
            R.id.navigation_changelog -> AboutActivity::class.java
            else -> SettingsActivity::class.java
        }

        val intent = Intent(this, activityClass)
        startActivityWithTransition(intent, MaterialSharedAxis.Y)

        return true
    }

    // converte gli accordi salvati dalla lingua vecchia alla nuova
    private fun convertTabs() {
        val oldLanguage = intent.getStringExtra(OLD_LANGUAGE)
        val newLanguage = intent.getStringExtra(NEW_LANGUAGE)

        var accordi1 = CambioAccordi.accordi_it
        Log.d(TAG, "convertTabs - from: $oldLanguage")
        when (oldLanguage) {
            LANGUAGE_UKRAINIAN -> accordi1 = CambioAccordi.accordi_uk
            LANGUAGE_POLISH -> accordi1 = CambioAccordi.accordi_pl
            LANGUAGE_ENGLISH -> accordi1 = CambioAccordi.accordi_en
            LANGUAGE_ENGLISH_PHILIPPINES -> accordi1 = CambioAccordi.accordi_en
        }

        var accordi2 = CambioAccordi.accordi_it
        Log.d(TAG, "convertTabs - to: $newLanguage")
        when (newLanguage) {
            LANGUAGE_UKRAINIAN -> accordi2 = CambioAccordi.accordi_uk
            LANGUAGE_POLISH -> accordi2 = CambioAccordi.accordi_pl
            LANGUAGE_ENGLISH -> accordi2 = CambioAccordi.accordi_en
            LANGUAGE_ENGLISH_PHILIPPINES -> accordi2 = CambioAccordi.accordi_en
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
                            + mappa[canto.savedTab.orEmpty()]
                )
                canto.savedTab = mappa[canto.savedTab.orEmpty()]
                mDao.updateCanto(canto)
            }
        }
    }

    // converte gli accordi salvati dalla lingua vecchia alla nuova
    private fun convertiBarre() {
        val oldLanguage = intent.getStringExtra(OLD_LANGUAGE)
        val newLanguage = intent.getStringExtra(NEW_LANGUAGE)

        var barre1 = CambioAccordi.barre_it
        Log.d(TAG, "convertiBarre - from: $oldLanguage")
        when (oldLanguage) {
            LANGUAGE_ENGLISH -> barre1 = CambioAccordi.barre_en
        }

        var barre2 = CambioAccordi.barre_it
        Log.d(TAG, "convertiBarre - to: $newLanguage")
        when (newLanguage) {
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

    fun enableFab(enable: Boolean, autoHide: Boolean = true) {
        Log.d(TAG, "enableFab: $enable")
        if (enable) {
            if (binding.fabPager.isOpen)
                binding.fabPager.close()
            else {
                val params = binding.fabPager.layoutParams as? CoordinatorLayout.LayoutParams
                params?.behavior =
                    if (autoHide) SpeedDialView.ScrollingViewSnackbarBehavior() else SpeedDialView.NoBehavior()
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
        binding.fabPager.mainFab.rippleColor =
            ContextCompat.getColor(this, android.R.color.transparent)
        binding.fabPager.clearActionItems()
        binding.fabPager.expansionMode =
            if (isFabExpansionLeft) SpeedDialView.ExpansionMode.LEFT else SpeedDialView.ExpansionMode.TOP
        enableFab(true)
        Log.d(TAG, "initFab optionMenu: $optionMenu")

        if (optionMenu) {
            val iconColor = MaterialColors.getColor(this, R.attr.colorOnPrimaryContainer, TAG)
            val backgroundColor = MaterialColors.getColor(this, R.attr.colorPrimaryContainer, TAG)

            binding.fabPager.addActionItem(
                SpeedDialActionItem.Builder(
                    R.id.fab_pulisci,
                    AppCompatResources.getDrawable(this, R.drawable.cleaning_services_24px)
                )
                    .setTheme(R.style.Risuscito_SpeedDialActionItem)
                    .setLabel(getString(R.string.dialog_reset_list_title))
                    .setFabBackgroundColor(backgroundColor)
                    .setLabelBackgroundColor(backgroundColor)
                    .setLabelColor(iconColor)
                    .create()
            )

            binding.fabPager.addActionItem(
                SpeedDialActionItem.Builder(
                    R.id.fab_add_lista,
                    AppCompatResources.getDrawable(this, R.drawable.add_24px)
                )
                    .setTheme(R.style.Risuscito_SpeedDialActionItem)
                    .setLabel(getString(R.string.action_add_list))
                    .setFabBackgroundColor(backgroundColor)
                    .setLabelBackgroundColor(backgroundColor)
                    .setLabelColor(iconColor)
                    .create()
            )

            binding.fabPager.addActionItem(
                SpeedDialActionItem.Builder(
                    R.id.fab_condividi,
                    AppCompatResources.getDrawable(this, R.drawable.share_24px)
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
                    SpeedDialActionItem.Builder(
                        R.id.fab_condividi_file,
                        AppCompatResources.getDrawable(this, R.drawable.attachment_24px)
                    )
                        .setTheme(R.style.Risuscito_SpeedDialActionItem)
                        .setLabel(getString(R.string.action_share_file))
                        .setFabBackgroundColor(backgroundColor)
                        .setLabelBackgroundColor(backgroundColor)
                        .setLabelColor(iconColor)
                        .create()
                )

                binding.fabPager.addActionItem(
                    SpeedDialActionItem.Builder(
                        R.id.fab_edit_lista,
                        AppCompatResources.getDrawable(this, R.drawable.edit_24px)
                    )
                        .setTheme(R.style.Risuscito_SpeedDialActionItem)
                        .setLabel(getString(R.string.action_edit_list))
                        .setFabBackgroundColor(backgroundColor)
                        .setLabelBackgroundColor(backgroundColor)
                        .setLabelColor(iconColor)
                        .create()
                )

                binding.fabPager.addActionItem(
                    SpeedDialActionItem.Builder(
                        R.id.fab_delete_lista,
                        AppCompatResources.getDrawable(this, R.drawable.delete_24px)
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

    val activityContextualToolbar: MaterialToolbar
        get() = binding.contextualToolbar

    val activityMainContent: View
        get() = binding.mainContent

    // [START signIn]
    private fun signIn() {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        profileItem = menu.findItem(R.id.account_manager)
        return super.onCreateOptionsMenu(menu)
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
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit { putBoolean(Utility.SIGNED_IN, signedIn) }
        if (signedIn)
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit { putBoolean(Utility.SIGN_IN_REQUESTED, true) }
        if (signedIn) {
            profileNameStr = acct?.displayName.orEmpty()
            profileEmailStr = acct?.email.orEmpty()
            val profilePhoto = acct?.photoUrl
            if (profilePhoto != null) {
                var personPhotoUrl = profilePhoto.toString()
                Log.d(TAG, "personPhotoUrl BEFORE $personPhotoUrl")
                personPhotoUrl = personPhotoUrl.replace(OLD_PHOTO_RES, NEW_PHOTO_RES)
                Log.d(TAG, "personPhotoUrl AFTER $personPhotoUrl")
                profilePhotoUrl = personPhotoUrl
            } else {
                profilePhotoUrl = StringUtils.EMPTY
            }
        } else {
            profileNameStr = StringUtils.EMPTY
            profileEmailStr = StringUtils.EMPTY
            profilePhotoUrl = StringUtils.EMPTY
        }
        updateProfileImage()
        hideProgressDialog()
    }

    fun updateProfileImage() {

        val profileImage =
            profileItem?.actionView?.findViewById<ShapeableImageView>(R.id.profile_icon)
        val signInButton =
            profileItem?.actionView?.findViewById<Button>(R.id.sign_in_button)

        if (profilePhotoUrl.isEmpty()) {
            profileImage?.setImageResource(R.drawable.account_circle_56px)
            profileImage?.background =
                null
        } else {
            AppCompatResources.getDrawable(this, R.drawable.account_circle_56px)?.let {
                Picasso.get().load(profilePhotoUrl)
                    .placeholder(it)
                    .into(profileItem?.actionView?.findViewById<ShapeableImageView>(R.id.profile_icon))
            }
            AppCompatResources.getDrawable(
                this,
                getTypedValueResId(R.attr.selectableItemBackgroundBorderless)
            )?.let {
                profileImage?.background =
                    it
            }
        }

        profileItem?.actionView?.findViewById<ShapeableImageView>(R.id.profile_icon)
            ?.setOnClickListener {
                ProfileDialogFragment.show(
                    ProfileDialogFragment.Builder(
                        PROFILE_DIALOG
                    ).apply {
                        profileName = profileNameStr
                        profileEmail = profileEmailStr
                        profileImageSrc = profilePhotoUrl
                    },
                    supportFragmentManager
                )
            }

        signInButton?.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit { putBoolean(Utility.SIGN_IN_REQUESTED, true) }
            mViewModel.showSnackbar = true
            signIn()
        }

        profileImage?.isVisible = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(Utility.SIGNED_IN, false)
        signInButton?.isGone =
            PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Utility.SIGNED_IN, false)

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
        val myFragment =
            supportFragmentManager.findFragmentByTag(R.id.navigation_indexes.toString())
        if (myFragment != null && myFragment.isVisible) {
            if (exitAlso)
                finish()
            return
        }

        binding.bottomNavigation?.menu?.findItem(R.id.navigation_indexes)?.isChecked = true
        binding.navigationRail?.menu?.findItem(R.id.navigation_indexes)?.isChecked = true

        supportFragmentManager.commit {
            replace(R.id.content_frame, GeneralIndexFragment(), R.id.navigation_indexes.toString())
        }

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
            },
            supportFragmentManager
        )
        (binding.drawer as? DrawerLayout)?.close()
    }

    private suspend fun translate() {
        Log.d(TAG, "translate")
        ProgressDialogFragment.show(
            ProgressDialogFragment.Builder(TRANSLATION).apply {
                content = R.string.translation_running
                progressIndeterminate = true
            },
            supportFragmentManager
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
                backupDatabase(acct?.id)
            }

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.BACKUP_STEP_2

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                backupSharedPreferences(acct?.id, acct?.email)
            }

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.BACKUP_COMPLETED
            SimpleDialogFragment.show(
                SimpleDialogFragment.Builder(BACKUP_DONE)
                    .title(R.string.general_message)
                    .icon(R.drawable.cloud_done_24px)
                    .content(R.string.gdrive_backup_success)
                    .positiveButton(R.string.ok),
                supportFragmentManager
            )
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

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.RESTORE_STARTED

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                restoreDatabase(acct?.id)
            }

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.RESTORE_STEP_2

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                restoreSharedPreferences(acct?.id)
            }

            mViewModel.backupRestoreState.value =
                MainActivityViewModel.BakupRestoreState.RESTORE_COMPLETED
            SimpleDialogFragment.show(
                SimpleDialogFragment.Builder(RESTORE_DONE)
                    .title(R.string.general_message)
                    .icon(R.drawable.cloud_done_24px)
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

    fun createActionMode(
        resId: Int,
        fragment: ActionModeFragment,
        clickListener: Toolbar.OnMenuItemClickListener
    ) {
        isActionMode = true
        actionModeFragment = fragment
        binding.contextualToolbar.menu.clear()
        binding.contextualToolbar.inflateMenu(resId)
        binding.contextualToolbar.setOnMenuItemClickListener(clickListener)
        setTransparentStatusBar(false)
        binding.risuscitoToolbar.expand(binding.contextualToolbarContainer, binding.appBarLayout)
    }

    fun destroyActionMode(): Boolean {
        isActionMode = false
        setTransparentStatusBar(true)
        actionModeFragment?.destroyActionMode()
        return binding.risuscitoToolbar.collapse(
            binding.contextualToolbarContainer,
            binding.appBarLayout
        )
    }

    fun updateActionModeTitle(title: String) {
        binding.contextualToolbar.title = title
    }

    companion object {
        private const val PROFILE_DIALOG = "PROFILE_DIALOG"
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
        private const val SEARCH_REPLACE = "SEARCH_REPLACE"
        private const val SEARCH_REPLACE_2 = "SEARCH_REPLACE_2"
        private val TAG = MainActivity::class.java.canonicalName

    }
}
