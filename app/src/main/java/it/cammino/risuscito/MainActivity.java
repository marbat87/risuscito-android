package it.cammino.risuscito;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.crossfader.Crossfader;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.MiniDrawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialize.util.UIUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.ui.CrossfadeWrapper;
import it.cammino.risuscito.ui.ThemeableActivity;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MainActivity extends ThemeableActivity
        implements ColorChooserDialog.ColorCallback
        , GoogleApiClient.OnConnectionFailedListener
        , SimpleDialogFragment.SimpleCallback {

    private final String TAG = getClass().getCanonicalName();
    private final long PROF_ID = 5428471L;

    private LUtils mLUtils;

    private MaterialCab materialCab;
    private Drawer mDrawer;
    private MiniDrawer mMiniDrawer;
    private Crossfader crossFader;
    private AccountHeader mAccountHeader;
    @BindView(R.id.risuscito_toolbar) Toolbar mToolbar;
    @BindView(R.id.loadingBar) MaterialProgressBar mCircleProgressBar;
    @BindView(R.id.toolbar_layout) @Nullable AppBarLayout appBarLayout;
    @BindView(R.id.material_tabs) TabLayout mTabLayout;
    @BindView(R.id.tabletToolbarBackground) @Nullable View mTabletBG;
    private boolean isOnTablet;
    private static final String SHOW_SNACKBAR = "mostra_snackbar";
    private static final String DB_RESTORE_RUNNING = "db_restore_running";
    private static final String PREF_RESTORE_RUNNING = "pref_restore_running";
    private static final String DB_BACKUP_RUNNING = "db_backup_running";
    private static final String PREF_BACKUP_RUNNING = "pref_backup_running";

    private boolean showSnackbar;
    private GoogleSignInAccount acct;

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
//    private FirebaseAnalytics mFirebaseAnalytics;

    private static final String PREF_DRIVE_FILE_NAME = "preferences_backup";

    private boolean dbRestoreRunning;
    private boolean prefRestoreRunning;
    private boolean dbBackupRunning;
    private boolean prefBackupRunning;

    private Typeface mRegularFont;
    private Typeface mMediumFont;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.hasNavDrawer = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        mSavedInstance = savedInstanceState;

        mRegularFont = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Regular.ttf");
        mMediumFont = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf");

        IconicsDrawable icon = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_menu)
                .color(Color.WHITE)
                .sizeDp(24)
                .paddingDp(2);

//        mToolbar = (Toolbar) findViewById(R.id.risuscito_toolbar);
        mToolbar.setBackgroundColor(getThemeUtils().primaryColor());
        mToolbar.setNavigationIcon(icon);
        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (getIntent().getBooleanExtra(Utility.DB_RESET, false)) {
            (new TranslationTask()).execute();
        }

        mLUtils = LUtils.getInstance(MainActivity.this);
        isOnTablet = mLUtils.isOnTablet();
        Log.d(TAG, "onCreate: isOnTablet = " + isOnTablet);

        if (isOnTablet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getThemeUtils().primaryColorDark());

        if (isOnTablet() && mTabletBG != null)
            mTabletBG.setBackgroundColor(getThemeUtils().primaryColor());
        else
            mTabLayout.setBackgroundColor(getThemeUtils().primaryColor());

        setupNavDrawer(savedInstanceState);

        materialCab = new MaterialCab(this, R.id.cab_stub)
//                .setMenu(R.menu.menu_delete)
                .setBackgroundColor(getThemeUtils().primaryColorDark())
                .setPopupMenuTheme(R.style.ThemeOverlay_AppCompat_Light)
                .setContentInsetStartRes(R.dimen.mcab_default_content_inset);
//                .setCloseDrawableRes(android.R.drawable.ic_menu_close_clear_cancel);

        showSnackbar = savedInstanceState == null
                || savedInstanceState.getBoolean(SHOW_SNACKBAR, true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new Risuscito(), String.valueOf(R.id.navigation_home)).commit();
//            AppBarLayout appBarLayout = (AppBarLayout)findViewById(R.id.toolbar_layout);
            if (!isOnTablet && appBarLayout != null)
                appBarLayout.setExpanded(true, true);
        }

        if (savedInstanceState != null) {
            dbRestoreRunning = savedInstanceState.getBoolean(DB_RESTORE_RUNNING);
            dbBackupRunning = savedInstanceState.getBoolean(DB_BACKUP_RUNNING);
            prefRestoreRunning = savedInstanceState.getBoolean(PREF_RESTORE_RUNNING);
            prefBackupRunning = savedInstanceState.getBoolean(PREF_BACKUP_RUNNING);
        }

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Drive.API)
                .build();
        // [END build_client]

        // Initialize FirebaseAuth
//        mFirebaseAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Measurement.
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        FirebaseAnalytics.getInstance(this);

//        SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_ASK");
//        if (sFragment != null)
//            sFragment.setmCallback(MainActivity.this);
//        sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_ASK");
//        if (sFragment != null)
//            sFragment.setmCallback(MainActivity.this);
//        sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "SIGNOUT");
//        if (sFragment != null)
//            sFragment.setmCallback(MainActivity.this);
//        sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "REVOKE");
//        if (sFragment != null)
//            sFragment.setmCallback(MainActivity.this);
//        sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "RESTART");
//        if (sFragment != null)
//            sFragment.setmCallback(MainActivity.this);
        setDialogCallback("BACKUP_ASK");
        setDialogCallback("RESTORE_ASK");
        setDialogCallback("SIGNOUT");
        setDialogCallback("REVOKE");
        setDialogCallback("RESTART");
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(getClass().getName(), "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
            Log.d(getClass().getName(), "dbRestoreRunning: " + dbRestoreRunning);
            Log.d(getClass().getName(), "prefRestoreRunning: " + prefRestoreRunning);
            Log.d(getClass().getName(), "dbBackupRunning: " + dbBackupRunning);
            Log.d(getClass().getName(), "prefBackupRunning: " + prefBackupRunning);
            if (dbRestoreRunning) {
                new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "RESTORE_RUNNING")
                        .title(R.string.restore_running)
                        .content(R.string.restoring_database)
                        .showProgress()
                        .progressIndeterminate(true)
                        .progressMax(0)
                        .show();
                restoreDriveBackup();
            }
            if (prefRestoreRunning) {
                new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "RESTORE_RUNNING")
                        .title(R.string.restore_running)
                        .content(R.string.restoring_settings)
                        .showProgress()
                        .progressIndeterminate(true)
                        .progressMax(0)
                        .show();
                restoreDrivePrefBackup(PREF_DRIVE_FILE_NAME);
            }
            if (dbBackupRunning) {
                new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "BACKUP_RUNNING")
                        .title(R.string.backup_running)
                        .content(R.string.backup_database)
                        .showProgress()
                        .progressIndeterminate(true)
                        .progressMax(0)
                        .show();
                saveCheckDupl(
                        Drive.DriveApi.getAppFolder(mGoogleApiClient)
                        , DatabaseCanti.getDbName()
                        , "application/x-sqlite3"
                        , getDbPath()
                        , true
                );
            }
            if (prefBackupRunning) {
                new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "BACKUP_RUNNING")
                        .title(R.string.backup_running)
                        .content(R.string.backup_settings)
                        .showProgress()
                        .progressIndeterminate(true)
                        .progressMax(0)
                        .show();
                saveCheckDupl(
                        Drive.DriveApi.getAppFolder(mGoogleApiClient)
                        , PREF_DRIVE_FILE_NAME
                        , "application/json"
                        , null
                        , false
                );
            }
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    Log.d(getClass().getName(), "Reconnected");
//                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                    Log.d(getClass().getName(), "dbRestoreRunning: " + dbRestoreRunning);
                    Log.d(getClass().getName(), "prefRestoreRunning: " + prefRestoreRunning);
                    Log.d(getClass().getName(), "dbBackupRunning: " + dbBackupRunning);
                    Log.d(getClass().getName(), "prefBackupRunning: " + prefBackupRunning);
                    if (dbRestoreRunning) {
                        new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "RESTORE_RUNNING")
                                .title(R.string.restore_running)
                                .content(R.string.restoring_database)
                                .showProgress()
                                .progressIndeterminate(true)
                                .progressMax(0)
                                .show();
                        restoreDriveBackup();
                    }
                    if (prefRestoreRunning) {
                        new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "RESTORE_RUNNING")
                                .title(R.string.restore_running)
                                .content(R.string.restoring_settings)
                                .showProgress()
                                .progressIndeterminate(true)
                                .progressMax(0)
                                .show();
                        restoreDrivePrefBackup(PREF_DRIVE_FILE_NAME);
                    }
                    if (dbBackupRunning) {
                        new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "BACKUP_RUNNING")
                                .title(R.string.backup_running)
                                .content(R.string.backup_database)
                                .showProgress()
                                .progressIndeterminate(true)
                                .progressMax(0)
                                .show();
                        saveCheckDupl(
                                Drive.DriveApi.getAppFolder(mGoogleApiClient)
                                , DatabaseCanti.getDbName()
                                , "application/x-sqlite3"
                                , getDbPath()
                                , true
                        );
                    }
                    if (prefBackupRunning) {
                        new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "BACKUP_RUNNING")
                                .title(R.string.backup_running)
                                .content(R.string.backup_settings)
                                .showProgress()
                                .progressIndeterminate(true)
                                .progressMax(0)
                                .show();
                        saveCheckDupl(
                                Drive.DriveApi.getAppFolder(mGoogleApiClient)
                                , PREF_DRIVE_FILE_NAME
                                , "application/json"
                                , null
                                , false
                        );
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideProgressDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putInt(SELECTED_ITEM, selectedItemIndex);
        //add the values which need to be saved from the drawer to the bundle
        savedInstanceState = mDrawer.saveInstanceState(savedInstanceState);
        //questo pezzo salva l'elenco dei titoli checkati del fragment ConsegnatiFragment, quando si ruota lo schermo
        ConsegnatiFragment consegnatiFragment = (ConsegnatiFragment) getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.navigation_consegnati));
        if (consegnatiFragment != null && consegnatiFragment.isVisible() && consegnatiFragment.getTitoliChoose() != null) {
            ConsegnatiFragment.RetainedFragment dataFragment = new ConsegnatiFragment.RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(dataFragment, ConsegnatiFragment.TITOLI_CHOOSE).commit();
            dataFragment.setData(consegnatiFragment.getTitoliChoose());
        }

        savedInstanceState.putBoolean(SHOW_SNACKBAR, showSnackbar);
        savedInstanceState.putBoolean(DB_RESTORE_RUNNING, dbRestoreRunning);
        savedInstanceState.putBoolean(PREF_RESTORE_RUNNING, prefRestoreRunning);
        savedInstanceState.putBoolean(DB_BACKUP_RUNNING, dbBackupRunning);
        savedInstanceState.putBoolean(PREF_BACKUP_RUNNING, prefBackupRunning);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupNavDrawer(@Nullable Bundle savedInstanceState) {

        IProfile profile = new ProfileDrawerItem().withName("")
                .withEmail("")
                .withIcon(R.mipmap.profile_picture)
                .withIdentifier(PROF_ID)
                .withTypeface(mRegularFont);

        // Create the AccountHeader
        mAccountHeader = new AccountHeaderBuilder()
                .withActivity(MainActivity.this)
                .withTranslucentStatusBar(!isOnTablet)
                .withSelectionListEnabledForSingleProfile(false)
                .withHeaderBackground(isOnTablet ? new ColorDrawable(Color.WHITE) : new ColorDrawable(getThemeUtils().primaryColor()))
                .withSavedInstance(savedInstanceState)
                .addProfiles(profile)
//                .withNameTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf"))
//                .withEmailTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Regular.ttf"))
                .withNameTypeface(mMediumFont)
                .withEmailTypeface(mRegularFont)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        //sample usage of the onProfileChanged listener
                        //if the clicked item has the identifier 1 add a new profile ;)
                        if (profile instanceof IDrawerItem && profile.getIdentifier() == R.id.gdrive_backup) {
                            new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "BACKUP_ASK")
                                    .title(R.string.gdrive_backup)
                                    .content(R.string.gdrive_backup_content)
                                    .positiveButton(android.R.string.yes)
                                    .negativeButton(android.R.string.no)
                                    .show();
//                            return true;
                        }
                        else if (profile instanceof IDrawerItem && profile.getIdentifier() == R.id.gdrive_restore) {
                            new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "RESTORE_ASK")
                                    .title(R.string.gdrive_restore)
                                    .content(R.string.gdrive_restore_content)
                                    .positiveButton(android.R.string.yes)
                                    .negativeButton(android.R.string.no)
                                    .show();
//                            return true;
                        }
                        else if (profile instanceof IDrawerItem && profile.getIdentifier() == R.id.gplus_signout) {
                            new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "SIGNOUT")
                                    .title(R.string.gplus_signout)
                                    .content(R.string.dialog_acc_disconn_text)
                                    .positiveButton(android.R.string.yes)
                                    .negativeButton(android.R.string.no)
                                    .show();
//                            return true;
                        }
                        else if (profile instanceof IDrawerItem && profile.getIdentifier() == R.id.gplus_revoke) {
                            new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "REVOKE")
                                    .title(R.string.gplus_revoke)
                                    .content(R.string.dialog_acc_revoke_text)
                                    .positiveButton(android.R.string.yes)
                                    .negativeButton(android.R.string.no)
                                    .show();
//                            return true;
                        }

                        //false if you have not consumed the event and it should close the drawer
                        return false;
                    }
                })
                .build();

        DrawerBuilder mDrawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withHasStableIds(true)
                .withAccountHeader(mAccountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.activity_homepage).withIcon(CommunityMaterial.Icon.cmd_home).withIdentifier(R.id.navigation_home)
                                .withSelectedIconColor(getThemeUtils().primaryColor())
                                .withSelectedTextColor(getThemeUtils().primaryColor())
//                                .withTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf")),
                                .withTypeface(mMediumFont),
                        new PrimaryDrawerItem().withName(R.string.search_name_text).withIcon(CommunityMaterial.Icon.cmd_magnify).withIdentifier(R.id.navigation_search)
                                .withSelectedIconColor(getThemeUtils().primaryColor())
                                .withSelectedTextColor(getThemeUtils().primaryColor())
//                                .withTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf")),
                                .withTypeface(mMediumFont),
                        new PrimaryDrawerItem().withName(R.string.title_activity_general_index).withIcon(CommunityMaterial.Icon.cmd_view_list).withIdentifier(R.id.navigation_indexes)
                                .withSelectedIconColor(getThemeUtils().primaryColor())
                                .withSelectedTextColor(getThemeUtils().primaryColor())
//                                .withTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf")),
                                .withTypeface(mMediumFont),
                        new PrimaryDrawerItem().withName(R.string.title_activity_custom_lists).withIcon(CommunityMaterial.Icon.cmd_view_carousel).withIdentifier(R.id.navitagion_lists)
                                .withSelectedIconColor(getThemeUtils().primaryColor())
                                .withSelectedTextColor(getThemeUtils().primaryColor())
//                                .withTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf")),
                                .withTypeface(mMediumFont),
                        new PrimaryDrawerItem().withName(R.string.action_favourites).withIcon(CommunityMaterial.Icon.cmd_heart).withIdentifier(R.id.navigation_favorites)
                                .withSelectedIconColor(getThemeUtils().primaryColor())
                                .withSelectedTextColor(getThemeUtils().primaryColor())
//                                .withTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf")),
                                .withTypeface(mMediumFont),
                        new PrimaryDrawerItem().withName(R.string.title_activity_consegnati).withIcon(CommunityMaterial.Icon.cmd_clipboard_check).withIdentifier(R.id.navigation_consegnati)
                                .withSelectedIconColor(getThemeUtils().primaryColor())
                                .withSelectedTextColor(getThemeUtils().primaryColor())
//                                .withTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf")),
                                .withTypeface(mMediumFont),
                        new PrimaryDrawerItem().withName(R.string.title_activity_history).withIcon(CommunityMaterial.Icon.cmd_history).withIdentifier(R.id.navigation_history)
                                .withSelectedIconColor(getThemeUtils().primaryColor())
                                .withSelectedTextColor(getThemeUtils().primaryColor())
//                                .withTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf")),
                                .withTypeface(mMediumFont),
                        new PrimaryDrawerItem().withName(R.string.title_activity_settings).withIcon(CommunityMaterial.Icon.cmd_settings).withIdentifier(R.id.navigation_settings)
                                .withSelectedIconColor(getThemeUtils().primaryColor())
                                .withSelectedTextColor(getThemeUtils().primaryColor())
//                                .withTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf")),
                                .withTypeface(mMediumFont),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.title_activity_about).withIcon(CommunityMaterial.Icon.cmd_information_outline).withIdentifier(R.id.navigation_changelog)
                                .withSelectedIconColor(getThemeUtils().primaryColor())
                                .withSelectedTextColor(getThemeUtils().primaryColor())
//                                .withTypeface(Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf"))
                                .withTypeface(mMediumFont)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if (drawerItem != null) {
                            Fragment fragment;
                            if (drawerItem.getIdentifier() == R.id.navigation_home) {
                                fragment = new Risuscito();
                                if (!isOnTablet && appBarLayout != null)
                                    appBarLayout.setExpanded(true, true);
//                                if (LUtils.hasL()) {
//                                    mToolbar.setElevation(getResources().getDimension(R.dimen.design_appbar_elevation));
//                                }
                            }
                            else if (drawerItem.getIdentifier() == R.id.navigation_search) {
//                                if (LUtils.hasL())
//                                    mToolbar.setElevation(0);
                                fragment = new GeneralSearch();
                            }
                            else if (drawerItem.getIdentifier() == R.id.navigation_indexes) {
//                                if (LUtils.hasL())
//                                    mToolbar.setElevation(0);
                                fragment = new GeneralIndex();
                            }
                            else if (drawerItem.getIdentifier() ==  R.id.navitagion_lists) {
//                                if (LUtils.hasL())
//                                    mToolbar.setElevation(0);
                                fragment = new CustomLists();
                            }
                            else if (drawerItem.getIdentifier() ==  R.id.navigation_favorites) {
//                                if (LUtils.hasL())
//                                    mToolbar.setElevation(getResources().getDimension(R.dimen.design_appbar_elevation));
                                fragment = new FavouritesActivity();
                            }
                            else if (drawerItem.getIdentifier() ==  R.id.navigation_settings) {
//                                if (LUtils.hasL())
//                                    mToolbar.setElevation(getResources().getDimension(R.dimen.design_appbar_elevation));
                                fragment = new SettingsFragment();
                            }
                            else if (drawerItem.getIdentifier() ==  R.id.navigation_changelog) {
//                                mLUtils.startActivityWithTransition(new Intent(MainActivity.this, ChangelogActivity.class));
//                                return true;
//                                if (LUtils.hasL())
//                                    mToolbar.setElevation(getResources().getDimension(R.dimen.design_appbar_elevation));
                                fragment = new AboutFragment();
                            }
                            else if (drawerItem.getIdentifier() ==  R.id.navigation_consegnati) {
//                                if (LUtils.hasL())
//                                    mToolbar.setElevation(getResources().getDimension(R.dimen.design_appbar_elevation));
                                fragment = new ConsegnatiFragment();
                            }
                            else if (drawerItem.getIdentifier() ==  R.id.navigation_history) {
//                                if (LUtils.hasL())
//                                    mToolbar.setElevation(getResources().getDimension(R.dimen.design_appbar_elevation));
                                fragment = new HistoryFragment();
                            }
                            else return true;

                            //creo il nuovo fragment solo se non è lo stesso che sto già visualizzando
                            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(drawerItem.getIdentifier()));
                            if (myFragment == null || !myFragment.isVisible()) {
                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                if (!isOnTablet)
                                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                                transaction.replace(R.id.content_frame, fragment, String.valueOf(drawerItem.getIdentifier())).commit();
                            }

                            if (isOnTablet)
                                mMiniDrawer.setSelection(drawerItem.getIdentifier());
                        }
                        return isOnTablet;

                    }
                })
                .withGenerateMiniDrawer(isOnTablet)
                .withSavedInstance(savedInstanceState)
                .withTranslucentStatusBar(!isOnTablet);

        if (isOnTablet) {
            mDrawer = mDrawerBuilder.buildView();
            //the MiniDrawer is managed by the Drawer and we just get it to hook it into the Crossfader
            mMiniDrawer = mDrawer.getMiniDrawer()
                    .withEnableSelectedMiniDrawerItemBackground(true)
                    .withIncludeSecondaryDrawerItems(true);

            //get the widths in px for the first and second panel
            int firstWidth = (int) UIUtils.convertDpToPixel(302, this);
            int secondWidth = (int) UIUtils.convertDpToPixel(72, this);

            //create and build our crossfader (see the MiniDrawer is also builded in here, as the build method returns the view to be used in the crossfader)
            crossFader = new Crossfader()
                    .withContent(findViewById(R.id.main_frame))
                    .withFirst(mDrawer.getSlider(), firstWidth)
                    .withSecond(mMiniDrawer.build(this), secondWidth)
                    .withSavedInstance(savedInstanceState)
                    .withGmailStyleSwiping()
                    .build();

            //define the crossfader to be used with the miniDrawer. This is required to be able to automatically toggle open / close
            mMiniDrawer.withCrossFader(new CrossfadeWrapper(crossFader));

            //define a shadow (this is only for normal LTR layouts if you have a RTL app you need to define the other one
            crossFader.getCrossFadeSlidingPaneLayout().setShadowResourceLeft(R.drawable.material_drawer_shadow_left);
            crossFader.getCrossFadeSlidingPaneLayout().setShadowResourceRight(R.drawable.material_drawer_shadow_right);
        }
        else {
            mDrawer = mDrawerBuilder.build();
            mDrawer.getDrawerLayout().setStatusBarBackgroundColor(getThemeUtils().primaryColorDark());
        }

    }

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (isOnTablet) {
//                if (crossFader != null && crossFader.isCrossFaded()) {
//                    crossFader.crossFade();
//                    return true;
//                }
//            }
//            else {
//                if (mDrawer != null && mDrawer.isDrawerOpen()) {
//                    mDrawer.closeDrawer();
//                    return true;
//                }
//            }
//
//            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.navigation_home));
//            if (myFragment != null && myFragment.isVisible()) {
//                finish();
//                return true;
//            }
//
//            if (isOnTablet)
//                mMiniDrawer.setSelection(R.id.navigation_home);
//            mDrawer.setSelection(R.id.navigation_home);
//            appBarLayout.setExpanded(true, true);
//            return true;
//        }
//        return super.onKeyUp(keyCode, event);
//    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        if (isOnTablet) {
            if (crossFader != null && crossFader.isCrossFaded()) {
                crossFader.crossFade();
                return;
            }
        }
        else {
            if (mDrawer != null && mDrawer.isDrawerOpen()) {
                mDrawer.closeDrawer();
                return;
            }
        }

        Fragment myFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.navigation_home));
        if (myFragment != null && myFragment.isVisible()) {
            finish();
            return;
        }

        if (isOnTablet)
            mMiniDrawer.setSelection(R.id.navigation_home);
        else {
            if (appBarLayout != null)
                appBarLayout.setExpanded(true, true);
        }
        mDrawer.setSelection(R.id.navigation_home);
//        appBarLayout.setExpanded(true, true);
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int color) {
        if (colorChooserDialog.isAccentMode())
            getThemeUtils().accentColor(color);
        else
            getThemeUtils().primaryColor(color);

        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {}

    //converte gli accordi salvati dalla lingua vecchia alla nuova
    private void convertTabs(SQLiteDatabase db, String conversion) {
//        Log.i(getClass().toString(), "CONVERSION: " + conversion);
//        HashMap<String, String> mappa = null;
//        if (conversion.equalsIgnoreCase("it-uk")) {
//            mappa = new HashMap<>();
//            for (int i = 0; i < CambioAccordi.accordi_it.length; i++)
//                mappa.put(CambioAccordi.accordi_it[i], CambioAccordi.accordi_uk[i]);
//        }
//        if (conversion.equalsIgnoreCase("uk-it")) {
//            mappa = new HashMap<>();
//            for (int i = 0; i < CambioAccordi.accordi_it.length; i++)
//                mappa.put(CambioAccordi.accordi_uk[i], CambioAccordi.accordi_it[i]);
//        }
        String[] accordi1 = CambioAccordi.accordi_it;
        Log.d(TAG, "convertTabs - from: " + conversion.substring(0,2));
        switch (conversion.substring(0,2)) {
            case "uk":
                accordi1 = CambioAccordi.accordi_uk;
                break;
            case "en":
                accordi1 = CambioAccordi.accordi_en;
                break;
        }

        String[] accordi2 = CambioAccordi.accordi_it;
        Log.d(TAG, "convertTabs - to: " + conversion.substring(3,5));
        switch (conversion.substring(3,5)) {
            case "uk":
                accordi2 = CambioAccordi.accordi_uk;
                break;
            case "en":
                accordi2 = CambioAccordi.accordi_en;
                break;
        }

        HashMap<String, String>mappa = new HashMap<>();
        for (int i = 0; i < CambioAccordi.accordi_it.length; i++)
            mappa.put(accordi1[i], accordi2[i]);

//        if (mappa != null) {
        String query = "SELECT _id, saved_tab" +
                "  FROM ELENCO";
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getString(1) != null && !cursor.getString(1).equals("")) {
                Log.d(TAG, "convertTabs: " + "ID " + cursor.getInt(0) +  " -> CONVERTO DA " + cursor.getString(1) + " A " + mappa.get(cursor.getString(1)));
                query = "UPDATE ELENCO" +
                        "  SET saved_tab = \'" + mappa.get(cursor.getString(1)) + "\' " +
                        "  WHERE _id =  " + cursor.getInt(0);
                db.execSQL(query);
            }
            cursor.moveToNext();
        }
        cursor.close();
//        }
    }

    //converte gli accordi salvati dalla lingua vecchia alla nuova
    private void convertiBarre(SQLiteDatabase db, String conversion) {
        String[] barre1 = CambioAccordi.barre_it;
        Log.d(TAG, "convertiBarre - from: " + conversion.substring(0,2));
        switch (conversion.substring(0,2)) {
            case "uk":
                barre1 = CambioAccordi.barre_uk;
                break;
            case "en":
                barre1 = CambioAccordi.barre_en;
                break;
        }

        String[] barre2 = CambioAccordi.barre_it;
        Log.d(TAG, "convertiBarre - to: " + conversion.substring(3,5));
        switch (conversion.substring(3,5)) {
            case "uk":
                barre2 = CambioAccordi.barre_uk;
                break;
            case "en":
                barre2 = CambioAccordi.barre_en;
                break;
        }

        HashMap<String, String>mappa = new HashMap<>();
        for (int i = 0; i < CambioAccordi.barre_it.length; i++)
            mappa.put(barre1[i], barre2[i]);

        String query = "SELECT _id, saved_barre" +
                "  FROM ELENCO";
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getString(1) != null && !cursor.getString(1).equals("")) {
                Log.d(TAG, "convertiBarre: " + "ID " + cursor.getInt(0) +  " -> CONVERTO DA " + cursor.getString(1) + " A " + mappa.get(cursor.getString(1)));
                query = "UPDATE ELENCO" +
                        "  SET saved_barre = \'" + mappa.get(cursor.getString(1)) + "\' " +
                        "  WHERE _id =  " + cursor.getInt(0);
                db.execSQL(query);
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

    private class TranslationTask extends AsyncTask<String, Void, Integer> {

        TranslationTask() {
        }

        @Override
        protected Integer doInBackground(String... sUrl) {
            getIntent().removeExtra(Utility.DB_RESET);
            DatabaseCanti listaCanti = new DatabaseCanti(MainActivity.this);
            SQLiteDatabase db = listaCanti.getReadableDatabase();
            DatabaseCanti.Backup[] backup = listaCanti.backupTables(db.getVersion(), db);
            DatabaseCanti.BackupLocalLink[] backupLink = listaCanti.backupLocalLink(db.getVersion(), db);
            listaCanti.reCreateDatabse(db);
            listaCanti.repopulateDB(db.getVersion(), db.getVersion(), db, backup, backupLink);
            convertTabs(db, getIntent().getStringExtra(Utility.CHANGE_LANGUAGE));
            convertiBarre(db, getIntent().getStringExtra(Utility.CHANGE_LANGUAGE));
            db.close();
            listaCanti.close();
            return 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "TRANSLATION")
                    .content(R.string.translation_running)
                    .showProgress()
                    .progressIndeterminate(true)
                    .progressMax(0)
                    .show();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            getIntent().removeExtra(Utility.CHANGE_LANGUAGE);
            try {
                dismissDialog("TRANSLATION");
            } catch (IllegalArgumentException e) {
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
        }
    }

    public void setupToolbarTitle(int titleResId) {
        ((TextView) mToolbar.findViewById(R.id.main_toolbarTitle)).setText(titleResId);
    }

    public void enableFab(boolean enable) {
        Log.d(TAG, "enableFab: " + enable);
        FloatingActionButton mFab = findViewById(R.id.fab_pager);
//        View mScrolling = findViewById(R.id.content_frame);
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mScrolling.getLayoutParams();
//        params.setBehavior(enable? new FABAwareScrollingViewBehavior() : new AppBarLayout.ScrollingViewBehavior());
//        mFab.requestLayout();
        if (enable)
            mFab.show();
        else
            mFab.hide();
    }

    public void enableBottombar(boolean enabled) {
        Log.d(TAG, "enableBottombar - enabled: "+ enabled);
        View mBottomBar = findViewById(R.id.bottom_bar);
        if (enabled)
            mLUtils.animateIn(mBottomBar);
        else
            mBottomBar.setVisibility(View.GONE);
//            mBottomBar.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    // [START signIn]
    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(MainActivity.this)
                                .edit();
                        editor.putBoolean(Utility.SIGNED_IN, false);
                        editor.apply();
                        Snackbar.make(findViewById(R.id.main_content), R.string.disconnected, Snackbar.LENGTH_SHORT).show();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(MainActivity.this)
                                .edit();
                        editor.putBoolean(Utility.SIGNED_IN, false);
                        editor.apply();
                        Snackbar.make(findViewById(R.id.main_content), R.string.disconnected, Snackbar.LENGTH_SHORT).show();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        hideProgressDialog();
        Snackbar.make(findViewById(R.id.main_content), getString(R.string.login_failed, connectionResult.getErrorCode(), connectionResult.getErrorMessage()), Snackbar.LENGTH_SHORT).show();
        Log.d(getClass().getName(), "onConnectionFailed:" + connectionResult);
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(getClass().getName(), "requestCode: " + requestCode);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (null != result)
                handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(getClass().getName(), "handleSignInResult:" + result.isSuccess());
        Log.d(getClass().getName(), "signin result: " + result.getStatus().getStatusCode() + " - " + result.getStatus().getStatusMessage());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(MainActivity.this)
                    .edit();
            editor.putBoolean(Utility.SIGNED_IN, true);
            editor.apply();
            if (showSnackbar) {
                Snackbar.make(findViewById(R.id.main_content), getString(R.string.connected_as, acct.getDisplayName()), Snackbar.LENGTH_SHORT).show();
                showSnackbar = false;
            }
            updateUI(true);
//            firebaseAuthWithGoogle();
        } else {
            // Signed out, show unauthenticated UI.
            acct = null;
            updateUI(false);
        }
    }
    // [END handleSignInResult]

    @SuppressWarnings("deprecation")
    private void updateUI(boolean signedIn) {
//        AccountHeader headerResult;
        Intent intentBroadcast = new Intent(Risuscito.BROADCAST_SIGNIN_VISIBLE);
        Log.d(TAG, "updateUI: DATA_VISIBLE " + !signedIn);
        intentBroadcast.putExtra(Risuscito.DATA_VISIBLE, !signedIn);
        sendBroadcast(intentBroadcast);
        if (signedIn) {
            IProfile profile;
            Uri profilePhoto = acct.getPhotoUrl();
            if (profilePhoto != null) {
                String personPhotoUrl = profilePhoto.toString();
                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + 400;
                profile = new ProfileDrawerItem()
                        .withName(acct.getDisplayName())
                        .withEmail(acct.getEmail())
                        .withIcon(personPhotoUrl)
                        .withIdentifier(PROF_ID)
                        .withTypeface(mRegularFont);
            }
            else {
                profile = new ProfileDrawerItem()
                        .withName(acct.getDisplayName())
                        .withEmail(acct.getEmail())
                        .withIcon(R.mipmap.profile_picture)
                        .withIdentifier(PROF_ID)
                        .withTypeface(mRegularFont);
            }
            // Create the AccountHeader
            mAccountHeader.updateProfile(profile);
            if (mAccountHeader.getProfiles().size() == 1) {
                mAccountHeader.addProfiles(
                        new ProfileSettingDrawerItem().withName(getString(R.string.gdrive_backup)).withIcon(CommunityMaterial.Icon.cmd_cloud_upload).withIdentifier(R.id.gdrive_backup),
                        new ProfileSettingDrawerItem().withName(getString(R.string.gdrive_restore)).withIcon(CommunityMaterial.Icon.cmd_cloud_download).withIdentifier(R.id.gdrive_restore),
                        new ProfileSettingDrawerItem().withName(getString(R.string.gplus_signout)).withIcon(CommunityMaterial.Icon.cmd_account_remove).withIdentifier(R.id.gplus_signout),
                        new ProfileSettingDrawerItem().withName(getString(R.string.gplus_revoke)).withIcon(CommunityMaterial.Icon.cmd_account_key).withIdentifier(R.id.gplus_revoke));
            }
            if (isOnTablet)
                mMiniDrawer.onProfileClick();
        }
        else {
            IProfile profile = new ProfileDrawerItem().withName("")
                    .withEmail("")
                    .withIcon(R.mipmap.profile_picture)
                    .withIdentifier(PROF_ID)
                    .withTypeface(mRegularFont);
            if (mAccountHeader.getProfiles().size() > 1) {
                mAccountHeader.removeProfile(1);
                mAccountHeader.removeProfile(1);
                mAccountHeader.removeProfile(1);
                mAccountHeader.removeProfile(1);
            }
            mAccountHeader.updateProfile(profile);
            if (isOnTablet)
                mMiniDrawer.onProfileClick();

        }
        hideProgressDialog();

    }

//    private void firebaseAuthWithGoogle() {
//        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        mFirebaseAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
//
//                        // If sign in fails, display a message to the uel1user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//                        if (task.isSuccessful())
//                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);
//                        else {
//                            Bundle bundle = new Bundle();
//                            bundle.putString(FirebaseAnalytics.Param.VALUE, task.getException().toString());
//                            mFirebaseAnalytics.logEvent("login_fallito", bundle);
//                        }
//                    }
//                });
//    }

    private void showProgressDialog() {
        mCircleProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressDialog() {
        mCircleProgressBar.setVisibility(View.GONE);
    }

    public void setShowSnackbar() {
        this.showSnackbar = true;
    }

//    public GoogleApiClient getmGoogleApiClient() {
//        return mGoogleApiClient;
//    }

    /******************************************************************
     * controlla se il file è già esistente; se esiste lo cancella e poi lo ricrea
     *
     * @param pFldr parent's ID
     * @param titl  file name
     * @param mime  file mime type  (application/x-sqlite3)
     * @param file  file (with content) to create
     */
    void saveCheckDupl(final DriveFolder pFldr, final String titl,
                       final String mime, final File file, final boolean dataBase) {
        if (dataBase)
            dbBackupRunning = true;
        else
            prefBackupRunning = true;
        if (mGoogleApiClient != null && pFldr != null && titl != null && mime != null && (!dataBase || file != null))
            try {
                // create content from file
                Log.d(getClass().getName(), "saveCheckDupl - dataBase? " + dataBase);
                Log.d(getClass().getName(), "saveCheckDupl - title: " + titl);
                Query query = new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, titl))
                        .build();

                Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {

                        int count = metadataBufferResult.getMetadataBuffer().getCount();
                        Log.d(getClass().getName(), "saveCheckDupl - Count files old: " + count);
                        if (count > 0) {
                            DriveId mDriveId = metadataBufferResult.getMetadataBuffer().get(count - 1).getDriveId();
                            Log.d(getClass().getName(), "saveCheckDupl - driveIdRetrieved: " + mDriveId);
                            Log.d(getClass().getName(), "saveCheckDupl - filesize in cloud " + metadataBufferResult.getMetadataBuffer().get(0).getFileSize());
                            metadataBufferResult.getMetadataBuffer().release();

                            DriveFile mFile = mDriveId.asDriveFile();
                            mFile.delete(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    if (status.isSuccess()) {
                                        Log.d(getClass().getName(), "saveCheckDupl - CANCELLAZIONE OK: " + status.getStatusCode());
                                        if (dataBase)
                                            saveToDrive(pFldr, titl, mime, file, true);
                                        else
                                            saveToDrive(pFldr, titl, mime, file, false);
                                    } else {
                                        String errore = "ERRORE CANCELLAZIONE: " + status.getStatusCode() + "-" + status.getStatusMessage();
                                        Log.e(getClass().getName(), "saveCheckDupl - " + errore);
                                        if (dataBase)
                                            dbBackupRunning = false;
                                        else
                                            prefBackupRunning = false;
//                                        SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING");
//                                        if (sFragment != null)
//                                            sFragment.dismiss();
                                        dismissDialog("BACKUP_RUNNING");
                                        Snackbar.make(findViewById(R.id.main_content), errore, Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else if (dataBase)
                            saveToDrive(pFldr, titl, mime, file, true);
                        else
                            saveToDrive(pFldr, titl, mime, file, false);
                    }
                });
            } catch (Exception e) {
                Log.e(getClass().getName(), "saveCheckDupl - ExceptionD: " + e.getLocalizedMessage(), e);
                if (dataBase)
                    dbBackupRunning = false;
                else
                    prefBackupRunning = false;
//                SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING");
//                if (sFragment != null)
//                    sFragment.dismiss();
                dismissDialog("BACKUP_RUNNING");
                String error = "error: " + e.getLocalizedMessage();
                Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
            }
    }

    /******************************************************************
     * create file in GOODrive
     *
     * @param pFldr parent's ID
     * @param titl  file name
     * @param mime  file mime type  (application/x-sqlite3)
     * @param file  file (with content) to create
     */
    void saveToDrive(final DriveFolder pFldr, final String titl,
                     final String mime, final File file, final boolean dataBase) {
        Log.d(getClass().getName(), "saveToDrive - database? " + dataBase);
        if (mGoogleApiClient != null && pFldr != null && titl != null && mime != null && (!dataBase || file != null))
            try {
                // create content from file
                Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                        // write file to content, chunk by chunk
                        if (driveContentsResult.getStatus().isSuccess()) {
                            DriveContents cont = driveContentsResult.getDriveContents();
                            if (dataBase) {
                                try {
                                    OutputStream oos = cont.getOutputStream();
                                    if (oos != null) try {
                                        InputStream is = new FileInputStream(file);
                                        byte[] buf = new byte[4096];
                                        int c;
                                        while ((c = is.read(buf, 0, buf.length)) > 0) {
                                            oos.write(buf, 0, c);
                                            oos.flush();
                                        }
                                    } finally {
                                        oos.close();
                                    }
                                } catch (Exception e) {
                                    Log.e(getClass().getName(), "saveToDrive - Exception1: " + e.getLocalizedMessage(), e);
                                    dbBackupRunning = false;
//                                    SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING");
//                                    if (sFragment != null)
//                                        sFragment.dismiss();
                                    dismissDialog("BACKUP_RUNNING");
                                    String error = "error: " + e.getLocalizedMessage();
                                    Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                if (!saveSharedPreferencesToFile(cont.getOutputStream())) {
                                    prefBackupRunning = false;
//                                    SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING");
//                                    if (sFragment != null)
//                                        sFragment.dismiss();
                                    dismissDialog("BACKUP_RUNNING");
                                    return;
                                }
                            }

                            // content's COOL, create metadata
                            MetadataChangeSet meta = new MetadataChangeSet.Builder().setTitle(titl).setMimeType(mime).build();

                            // now create file on GooDrive
                            pFldr.createFile(mGoogleApiClient, meta, cont).setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                @Override
                                public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                    if (driveFileResult.getStatus().isSuccess()) {
                                        DriveFile dFil = driveFileResult.getDriveFile();
                                        if (dFil != null) {
                                            // BINGO , file uploaded
                                            dFil.getMetadata(mGoogleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                                                @Override
                                                public void onResult(@NonNull DriveResource.MetadataResult metadataResult) {
                                                    if (metadataResult.getStatus().isSuccess()) {
                                                        DriveId mDriveId = metadataResult.getMetadata().getDriveId();
                                                        Log.d(getClass().getName(), "driveIdSaved: " + mDriveId);
                                                        String error = "saveToDrive - FILE CARICATO - CODE: " + metadataResult.getStatus().getStatusCode();
                                                        Log.d(getClass().getName(), error);
                                                        if (dataBase) {
                                                            dbBackupRunning = false;
                                                            SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING");
                                                            if (sFragment != null)
                                                                sFragment.setContent(R.string.backup_settings);
                                                            saveCheckDupl(
                                                                    Drive.DriveApi.getAppFolder(mGoogleApiClient)
                                                                    , PREF_DRIVE_FILE_NAME
                                                                    , "application/json"
                                                                    , null
                                                                    , false
                                                            );
                                                        } else {
                                                            prefBackupRunning = false;
//                                                            SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING");
//                                                            if (sFragment!= null)
//                                                                sFragment.dismiss();
                                                            dismissDialog("BACKUP_RUNNING");
                                                            Snackbar.make(findViewById(R.id.main_content), R.string.gdrive_backup_success, Snackbar.LENGTH_LONG).show();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                 /* report error */
                                        if (dataBase)
                                            dbBackupRunning = false;
                                        else
                                            prefBackupRunning = false;
//                                        SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING");
//                                        if (sFragment != null)
//                                            sFragment.dismiss();
                                        dismissDialog("BACKUP_RUNNING");
                                        String error = "saveToDrive - driveFile error: " + driveFileResult.getStatus().getStatusCode() + " - " + driveFileResult.getStatus().getStatusMessage();
                                        Log.e(getClass().getName(), error);
                                        Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                        /* report error */
                            if (dataBase)
                                dbBackupRunning = false;
                            else
                                prefBackupRunning = false;
//                            if (SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING") != null)
//                                SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING").dismiss();
                            dismissDialog("BACKUP_RUNNING");
                            String error = "saveToDrive - driveFile error: " + driveContentsResult.getStatus().getStatusCode() + " - " + driveContentsResult.getStatus().getStatusMessage();
                            Log.e(getClass().getName(), error);
                            Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(getClass().getName(), "Exception2: " + e.getLocalizedMessage(), e);
                if (dataBase)
                    dbBackupRunning = false;
                else
                    prefBackupRunning = false;
//                if (SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING") != null)
//                    SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING").dismiss();
                dismissDialog("BACKUP_RUNNING");
                String error = "error: " + e.getLocalizedMessage();
                Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
            }
    }

    private File getDbPath() {
        Log.d(getClass().getName(), "dbpath:" + getDatabasePath(DatabaseCanti.getDbName()));
        return getDatabasePath(DatabaseCanti.getDbName());
    }

    void restoreDriveBackup() {
        dbRestoreRunning = true;
        Log.d(getClass().getName(), "restoreDriveBackup - Db name: " + DatabaseCanti.getDbName());
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, DatabaseCanti.getDbName()))
                .build();

        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                int count = metadataBufferResult.getMetadataBuffer().getCount();
                Log.d(getClass().getName(), "restoreDriveBackup - Count files backup: " + count);
                if (count > 0) {
                    DriveId mDriveId = metadataBufferResult.getMetadataBuffer().get(count - 1).getDriveId();
                    Log.d(getClass().getName(), "restoreDriveBackup - driveIdRetrieved: " + mDriveId);
                    Log.d(getClass().getName(), "restoreDriveBackup - filesize in cloud " + metadataBufferResult.getMetadataBuffer().get(0).getFileSize());
                    metadataBufferResult.getMetadataBuffer().release();

                    DriveFile mFile = mDriveId.asDriveFile();
                    mFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, new DriveFile.DownloadProgressListener() {
                        @Override
                        public void onProgress(long bytesDown, long bytesExpected) {
                        }
                    })
                            .setResultCallback(restoreContentsCallback);
                } else {
                    dbRestoreRunning = false;
//                    if (SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING") != null)
//                        SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING").dismiss();
                    dismissDialog("RESTORE_RUNNING");
                    Snackbar.make(findViewById(R.id.main_content), R.string.no_restore_found, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    final private ResultCallback<DriveApi.DriveContentsResult> restoreContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        String error = "restoreContentsCallback - restore error: " + result.getStatus().getStatusCode() + " - " + result.getStatus().getStatusMessage();
                        Log.e(getClass().getName(), error);
                        dbRestoreRunning = false;
//                        if (SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING") != null)
//                            SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING").dismiss();
                        dismissDialog("RESTORE_RUNNING");
                        Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    DatabaseCanti listaCanti = new DatabaseCanti(MainActivity.this);
                    listaCanti.close();

                    File db_file = getDbPath();
                    String path = db_file.getPath();

                    if (!db_file.exists())
                        //noinspection ResultOfMethodCallIgnored
                        db_file.delete();

                    db_file = new File(path);

                    DriveContents contents = result.getDriveContents();

                    try {
                        FileOutputStream fos = new FileOutputStream(db_file);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        BufferedInputStream in = new BufferedInputStream(contents.getInputStream());

                        byte[] buffer = new byte[1024];
                        int n;
//                        cnt = 0;


                        //debug("before read " + in.available());

                        while ((n = in.read(buffer)) > 0) {
                            bos.write(buffer, 0, n);
//                            cnt += n;
//                            debug("buffer: " + buffer[0]);
//                            debug("buffer: " + buffer[1]);
//                            debug("buffer: " + buffer[2]);
//                            debug("buffer: " + buffer[3]);
                            bos.flush();
                        }

                        //debug(" read done: " + cnt);

                        bos.close();

                    } catch (FileNotFoundException e) {
                        Log.e(getClass().getName(), "restoreContentsCallback - Exception3: " + e.getLocalizedMessage(), e);
                        contents.discard(mGoogleApiClient);
                        dbRestoreRunning = false;
//                        if (SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING") != null)
//                            SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING").dismiss();
                        dismissDialog("RESTORE_RUNNING");
                        String error = "error: " + e.getLocalizedMessage();
                        Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
                        return;
                    } catch (IOException e) {
                        Log.e(getClass().getName(), "restoreContentsCallback - Exception4: " + e.getLocalizedMessage(), e);
                        contents.discard(mGoogleApiClient);
                        dbRestoreRunning = false;
//                        if (SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING") != null)
//                            SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING").dismiss();
                        dismissDialog("RESTORE_RUNNING");
                        String error = "error: " + e.getLocalizedMessage();
                        Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    listaCanti = new DatabaseCanti(MainActivity.this);
                    SQLiteDatabase db = listaCanti.getReadableDatabase();
                    db.close();
                    listaCanti.close();
                    contents.discard(mGoogleApiClient);

                    dbRestoreRunning = false;
                    SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING");
                    if (sFragment != null)
                        sFragment.setContent(R.string.restoring_settings);
                    restoreDrivePrefBackup(PREF_DRIVE_FILE_NAME);

                }
            };

    void restoreDrivePrefBackup(String title) {
        prefRestoreRunning = true;
        Log.d(getClass().getName(), "restoreDrivePrefBackup - pref title: " + title);
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, title))
                .build();

        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {

                int count = metadataBufferResult.getMetadataBuffer().getCount();
                Log.d(getClass().getName(), "restoreDrivePrefBackup - Count files backup: " + count);
                if (count > 0) {
                    DriveId mDriveId = metadataBufferResult.getMetadataBuffer().get(count - 1).getDriveId();
                    Log.d(getClass().getName(), "restoreDrivePrefBackup - driveIdRetrieved: " + mDriveId);
                    Log.d(getClass().getName(), "restoreDrivePrefBackup - filesize in cloud " + metadataBufferResult.getMetadataBuffer().get(0).getFileSize());
                    metadataBufferResult.getMetadataBuffer().release();

                    DriveFile mFile = mDriveId.asDriveFile();
                    mFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, new DriveFile.DownloadProgressListener() {
                        @Override
                        public void onProgress(long bytesDown, long bytesExpected) {
                        }
                    })
                            .setResultCallback(restoreContentsPrefCallback);
                } else {
                    prefRestoreRunning = false;
//                    if (SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING") != null)
//                        SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING").dismiss();
                    dismissDialog("RESTORE_RUNNING");
                    Snackbar.make(findViewById(R.id.main_content), R.string.no_restore_found, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    final private ResultCallback<DriveApi.DriveContentsResult> restoreContentsPrefCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        String error = "restoreContentsPrefCallback - restore error: " + result.getStatus().getStatusCode() + " - " + result.getStatus().getStatusMessage();
                        Log.e(getClass().getName(), error);
                        prefRestoreRunning = false;
//                        if (SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING") != null)
//                            SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING").dismiss();
                        dismissDialog("RESTORE_RUNNING");
                        Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    DriveContents contents = result.getDriveContents();

                    loadSharedPreferencesFromFile(contents.getInputStream());
                    contents.discard(mGoogleApiClient);
                    prefRestoreRunning = false;
//                    if (SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING") != null)
//                        SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING").dismiss();
                    dismissDialog("RESTORE_RUNNING");
                    new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "RESTART")
                            .title(R.string.general_message)
                            .content(R.string.gdrive_restore_success)
                            .positiveButton(android.R.string.ok)
                            .show();
                }
            };

    @Override
    public void onPositive(@NonNull String tag) {
        Log.d(getClass().getName(), "onPositive: TAG " + tag);
        switch (tag) {
            case "BACKUP_ASK":
                if (mGoogleApiClient.isConnected()) {
                    new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "BACKUP_RUNNING")
                            .title(R.string.backup_running)
                            .content(R.string.backup_database)
                            .showProgress()
                            .progressIndeterminate(true)
                            .progressMax(0)
                            .show();
                    saveCheckDupl(
                            Drive.DriveApi.getAppFolder(mGoogleApiClient)
                            , DatabaseCanti.getDbName()
                            , "application/x-sqlite3"
                            , getDbPath()
                            , true
                    );
                }
                else {
                    new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "NO_CONNECTION_ERROR")
                            .content(R.string.no_connection)
                            .positiveButton(android.R.string.ok)
                            .show();
                }
                break;
            case "RESTORE_ASK":
                if (mGoogleApiClient.isConnected()) {
                    new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "RESTORE_RUNNING")
                            .title(R.string.restore_running)
                            .content(R.string.restoring_database)
                            .showProgress()
                            .progressIndeterminate(true)
                            .progressMax(0)
                            .show();
                    restoreDriveBackup();
                }
                else {
                    new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "NO_CONNECTION_ERROR")
                            .content(R.string.no_connection)
                            .positiveButton(android.R.string.ok)
                            .show();
                }
                break;
            case "SIGNOUT":
                if (mGoogleApiClient.isConnected())
                    signOut();
                else {
                    // [START_EXCLUDE]
                    updateUI(false);
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this)
                            .edit();
                    editor.putBoolean(Utility.SIGNED_IN, false);
                    editor.apply();
                    Snackbar.make(findViewById(R.id.main_content), R.string.disconnected, Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }
                break;
            case "REVOKE":
                if (mGoogleApiClient.isConnected())
                    revokeAccess();
                else {
                    // [START_EXCLUDE]
                    updateUI(false);
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this)
                            .edit();
                    editor.putBoolean(Utility.SIGNED_IN, false);
                    editor.apply();
                    Snackbar.make(findViewById(R.id.main_content), R.string.disconnected, Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }
                break;
            case "RESTART":
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                if (i != null)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: " + item.getItemId());
        if (isOnTablet && item.getItemId() == android.R.id.home) {
            crossFader.crossFade();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}

    public Drawer getDrawer() {
        return mDrawer;
    }

    public boolean isOnTablet() {
        return isOnTablet;
    }

    public MaterialCab getMaterialCab() {
        return materialCab;
    }

    @Nullable
    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    private void dismissDialog(String tag) {
        SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, tag);
        if (sFragment != null)
            sFragment.dismiss();
    }

    private void setDialogCallback(String tag) {
        SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, tag);
        if (sFragment != null)
            sFragment.setmCallback(MainActivity.this);
    }

}
