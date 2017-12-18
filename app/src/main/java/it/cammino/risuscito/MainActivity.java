package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.CantoDao;
import it.cammino.risuscito.database.entities.Canto;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.ui.CrossfadeWrapper;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.viewmodels.MainActivityViewModel;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MainActivity extends ThemeableActivity
    implements ColorChooserDialog.ColorCallback, SimpleDialogFragment.SimpleCallback {

  /* Request code used to invoke sign in user interactions. */
  private static final int RC_SIGN_IN = 9001;
  private static final String PREF_DRIVE_FILE_NAME = "preferences_backup";
  private final String TAG = getClass().getCanonicalName();
  private final long PROF_ID = 5428471L;

  @BindView(R.id.risuscito_toolbar)
  Toolbar mToolbar;

  @BindView(R.id.loadingBar)
  MaterialProgressBar mCircleProgressBar;

  @BindView(R.id.toolbar_layout)
  @Nullable
  AppBarLayout appBarLayout;

  @BindView(R.id.material_tabs)
  TabLayout mTabLayout;

  @BindView(R.id.tabletToolbarBackground)
  @Nullable
  View mTabletBG;

  private MainActivityViewModel mViewModel;
  private LUtils mLUtils;
  private MaterialCab materialCab;
  private Drawer mDrawer;
  private MiniDrawer mMiniDrawer;
  private Crossfader crossFader;
  private AccountHeader mAccountHeader;
  private boolean isOnTablet;
  private GoogleSignInAccount acct;
  private GoogleSignInClient mSignInClient;
  private Typeface mRegularFont;
  private Typeface mMediumFont;

  private BroadcastReceiver nextStepReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          try {
            Log.v(TAG, "BROADCAST_NEXT_STEP");
            if (intent.getStringExtra("WHICH") != null) {
              String which = intent.getStringExtra("WHICH");
              Log.v(TAG, "NEXT_STEP: " + which);
              if (which.equalsIgnoreCase("RESTORE")) {
                SimpleDialogFragment sFragment =
                    SimpleDialogFragment.findVisible(MainActivity.this, "RESTORE_RUNNING");
                if (sFragment != null) sFragment.setContent(R.string.restoring_settings);
              } else {
                SimpleDialogFragment sFragment =
                    SimpleDialogFragment.findVisible(MainActivity.this, "BACKUP_RUNNING");
                if (sFragment != null) sFragment.setContent(R.string.backup_settings);
              }
            }
          } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
          }
        }
      };

  private BroadcastReceiver lastStepReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          try {
            Log.v(TAG, "BROADCAST_LAST_STEP");
            if (intent.getStringExtra("WHICH") != null) {
              String which = intent.getStringExtra("WHICH");
              Log.v(TAG, "NEXT_STEP: " + which);
              if (which.equalsIgnoreCase("RESTORE")) {
                dismissDialog("RESTORE_RUNNING");
                new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "RESTART")
                    .title(R.string.general_message)
                    .content(R.string.gdrive_restore_success)
                    .positiveButton(android.R.string.ok)
                    .show();
              } else {
                dismissDialog("BACKUP_RUNNING");
                Snackbar.make(
                        findViewById(R.id.main_content),
                        R.string.gdrive_backup_success,
                        Snackbar.LENGTH_LONG)
                    .show();
              }
            }
          } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
          }
        }
      };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.hasNavDrawer = true;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

    mRegularFont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
    mMediumFont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");

    IconicsDrawable icon =
        new IconicsDrawable(this)
            .icon(CommunityMaterial.Icon.cmd_menu)
            .color(Color.WHITE)
            .sizeDp(24)
            .paddingDp(2);

    mToolbar.setBackgroundColor(getThemeUtils().primaryColor());
    mToolbar.setNavigationIcon(icon);
    setSupportActionBar(mToolbar);
    //noinspection ConstantConditions
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    if (getIntent().getBooleanExtra(Utility.DB_RESET, false)) {
      (new TranslationTask(MainActivity.this)).execute();
    }

    mLUtils = LUtils.getInstance(MainActivity.this);
    isOnTablet = mLUtils.isOnTablet();
    Log.d(TAG, "onCreate: isOnTablet = " + isOnTablet);

    if (isOnTablet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
      getWindow().setStatusBarColor(getThemeUtils().primaryColorDark());

    if (isOnTablet() && mTabletBG != null)
      mTabletBG.setBackgroundColor(getThemeUtils().primaryColor());
    else mTabLayout.setBackgroundColor(getThemeUtils().primaryColor());

    setupNavDrawer(savedInstanceState);

    materialCab =
        new MaterialCab(this, R.id.cab_stub)
            .setBackgroundColor(getThemeUtils().primaryColorDark())
            .setPopupMenuTheme(R.style.ThemeOverlay_AppCompat_Light)
            .setContentInsetStartRes(R.dimen.mcab_default_content_inset);

    //    mViewModel.showSnackbar = savedInstanceState == null ||
    // savedInstanceState.getBoolean(SHOW_SNACKBAR, true);

    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.content_frame, new Risuscito(), String.valueOf(R.id.navigation_home))
          .commit();
    }
    if (!isOnTablet && appBarLayout != null) appBarLayout.setExpanded(true, false);

    // [START configure_signin]
    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    GoogleSignInOptions gso =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            //                .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
            .build();
    // [END configure_signin]

    // [START build_client]
    mSignInClient = GoogleSignIn.getClient(this, gso);
    // [END build_client]

    FirebaseAnalytics.getInstance(this);

    setDialogCallback("BACKUP_ASK");
    setDialogCallback("RESTORE_ASK");
    setDialogCallback("SIGNOUT");
    setDialogCallback("REVOKE");
    setDialogCallback("RESTART");

    // registra un receiver per ricevere la notifica di preparazione della registrazione
    registerReceiver(nextStepReceiver, new IntentFilter("BROADCAST_NEXT_STEP"));
    registerReceiver(lastStepReceiver, new IntentFilter("BROADCAST_LAST_STEP"));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(nextStepReceiver);
    unregisterReceiver(lastStepReceiver);
  }

  @Override
  public void onStart() {
    super.onStart();
    Task<GoogleSignInAccount> task = mSignInClient.silentSignIn();
    if (task.isSuccessful()) {
      // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
      // and the GoogleSignInResult will be available instantly.
      Log.d(getClass().getName(), "Got cached sign-in");
      handleSignInResult(task);
    } else {
      // If the user has not previously signed in on this device or the sign-in has expired,
      // this asynchronous branch will attempt to sign in the user silently.  Cross-device
      // single sign-on will occur in this branch.
      showProgressDialog();
      //noinspection unchecked
      task.addOnCompleteListener(
          new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
              Log.d(getClass().getName(), "Reconnected");
              //noinspection unchecked
              handleSignInResult(task);
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
    savedInstanceState = mDrawer.saveInstanceState(savedInstanceState);
    super.onSaveInstanceState(savedInstanceState);
  }

  private void setupNavDrawer(@Nullable Bundle savedInstanceState) {

    IProfile profile =
        new ProfileDrawerItem()
            .withName("")
            .withEmail("")
            .withIcon(R.mipmap.profile_picture)
            .withIdentifier(PROF_ID)
            .withTypeface(mRegularFont);

    // Create the AccountHeader
    mAccountHeader =
        new AccountHeaderBuilder()
            .withActivity(MainActivity.this)
            .withTranslucentStatusBar(!isOnTablet)
            .withSelectionListEnabledForSingleProfile(false)
            .withHeaderBackground(
                isOnTablet
                    ? new ColorDrawable(ContextCompat.getColor(this, R.color.floating_background))
                    : new ColorDrawable(getThemeUtils().primaryColor()))
            .withSavedInstance(savedInstanceState)
            .addProfiles(profile)
            .withNameTypeface(mMediumFont)
            .withEmailTypeface(mRegularFont)
            .withOnAccountHeaderListener(
                new AccountHeader.OnAccountHeaderListener() {
                  @Override
                  public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                    // sample usage of the onProfileChanged listener
                    // if the clicked item has the identifier 1 add a new profile ;)
                    if (profile instanceof IDrawerItem
                        && profile.getIdentifier() == R.id.gdrive_backup) {
                      new SimpleDialogFragment.Builder(
                              MainActivity.this, MainActivity.this, "BACKUP_ASK")
                          .title(R.string.gdrive_backup)
                          .content(R.string.gdrive_backup_content)
                          .positiveButton(android.R.string.yes)
                          .negativeButton(android.R.string.no)
                          .show();
                    } else if (profile instanceof IDrawerItem
                        && profile.getIdentifier() == R.id.gdrive_restore) {
                      new SimpleDialogFragment.Builder(
                              MainActivity.this, MainActivity.this, "RESTORE_ASK")
                          .title(R.string.gdrive_restore)
                          .content(R.string.gdrive_restore_content)
                          .positiveButton(android.R.string.yes)
                          .negativeButton(android.R.string.no)
                          .show();
                    } else if (profile instanceof IDrawerItem
                        && profile.getIdentifier() == R.id.gplus_signout) {
                      new SimpleDialogFragment.Builder(
                              MainActivity.this, MainActivity.this, "SIGNOUT")
                          .title(R.string.gplus_signout)
                          .content(R.string.dialog_acc_disconn_text)
                          .positiveButton(android.R.string.yes)
                          .negativeButton(android.R.string.no)
                          .show();
                    } else if (profile instanceof IDrawerItem
                        && profile.getIdentifier() == R.id.gplus_revoke) {
                      new SimpleDialogFragment.Builder(
                              MainActivity.this, MainActivity.this, "REVOKE")
                          .title(R.string.gplus_revoke)
                          .content(R.string.dialog_acc_revoke_text)
                          .positiveButton(android.R.string.yes)
                          .negativeButton(android.R.string.no)
                          .show();
                    }

                    // false if you have not consumed the event and it should close the drawer
                    return false;
                  }
                })
            .build();

    DrawerBuilder mDrawerBuilder =
        new DrawerBuilder()
            .withActivity(this)
            .withToolbar(mToolbar)
            .withHasStableIds(true)
            .withAccountHeader(mAccountHeader)
            .addDrawerItems(
                new PrimaryDrawerItem()
                    .withName(R.string.activity_homepage)
                    .withIcon(CommunityMaterial.Icon.cmd_home)
                    .withIdentifier(R.id.navigation_home)
                    .withSelectedIconColor(getThemeUtils().primaryColor())
                    .withSelectedTextColor(getThemeUtils().primaryColor())
                    .withTypeface(mMediumFont),
                new PrimaryDrawerItem()
                    .withName(R.string.search_name_text)
                    .withIcon(CommunityMaterial.Icon.cmd_magnify)
                    .withIdentifier(R.id.navigation_search)
                    .withSelectedIconColor(getThemeUtils().primaryColor())
                    .withSelectedTextColor(getThemeUtils().primaryColor())
                    .withTypeface(mMediumFont),
                new PrimaryDrawerItem()
                    .withName(R.string.title_activity_general_index)
                    .withIcon(CommunityMaterial.Icon.cmd_view_list)
                    .withIdentifier(R.id.navigation_indexes)
                    .withSelectedIconColor(getThemeUtils().primaryColor())
                    .withSelectedTextColor(getThemeUtils().primaryColor())
                    .withTypeface(mMediumFont),
                new PrimaryDrawerItem()
                    .withName(R.string.title_activity_custom_lists)
                    .withIcon(CommunityMaterial.Icon.cmd_view_carousel)
                    .withIdentifier(R.id.navitagion_lists)
                    .withSelectedIconColor(getThemeUtils().primaryColor())
                    .withSelectedTextColor(getThemeUtils().primaryColor())
                    .withTypeface(mMediumFont),
                new PrimaryDrawerItem()
                    .withName(R.string.action_favourites)
                    .withIcon(CommunityMaterial.Icon.cmd_heart)
                    .withIdentifier(R.id.navigation_favorites)
                    .withSelectedIconColor(getThemeUtils().primaryColor())
                    .withSelectedTextColor(getThemeUtils().primaryColor())
                    .withTypeface(mMediumFont),
                new PrimaryDrawerItem()
                    .withName(R.string.title_activity_consegnati)
                    .withIcon(CommunityMaterial.Icon.cmd_clipboard_check)
                    .withIdentifier(R.id.navigation_consegnati)
                    .withSelectedIconColor(getThemeUtils().primaryColor())
                    .withSelectedTextColor(getThemeUtils().primaryColor())
                    .withTypeface(mMediumFont),
                new PrimaryDrawerItem()
                    .withName(R.string.title_activity_history)
                    .withIcon(CommunityMaterial.Icon.cmd_history)
                    .withIdentifier(R.id.navigation_history)
                    .withSelectedIconColor(getThemeUtils().primaryColor())
                    .withSelectedTextColor(getThemeUtils().primaryColor())
                    .withTypeface(mMediumFont),
                new PrimaryDrawerItem()
                    .withName(R.string.title_activity_settings)
                    .withIcon(CommunityMaterial.Icon.cmd_settings)
                    .withIdentifier(R.id.navigation_settings)
                    .withSelectedIconColor(getThemeUtils().primaryColor())
                    .withSelectedTextColor(getThemeUtils().primaryColor())
                    .withTypeface(mMediumFont),
                new DividerDrawerItem(),
                new PrimaryDrawerItem()
                    .withName(R.string.title_activity_about)
                    .withIcon(CommunityMaterial.Icon.cmd_information_outline)
                    .withIdentifier(R.id.navigation_changelog)
                    .withSelectedIconColor(getThemeUtils().primaryColor())
                    .withSelectedTextColor(getThemeUtils().primaryColor())
                    .withTypeface(mMediumFont))
            .withOnDrawerItemClickListener(
                new Drawer.OnDrawerItemClickListener() {
                  @Override
                  public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    // check if the drawerItem is set.
                    // there are different reasons for the drawerItem to be null
                    // --> click on the header
                    // --> click on the footer
                    // those items don't contain a drawerItem

                    if (drawerItem != null) {
                      Fragment fragment;
                      if (drawerItem.getIdentifier() == R.id.navigation_home) {
                        fragment = new Risuscito();
                        if (!isOnTablet && appBarLayout != null)
                          appBarLayout.setExpanded(true, true);
                      } else if (drawerItem.getIdentifier() == R.id.navigation_search) {
                        fragment = new GeneralSearch();
                      } else if (drawerItem.getIdentifier() == R.id.navigation_indexes) {
                        fragment = new GeneralIndex();
                      } else if (drawerItem.getIdentifier() == R.id.navitagion_lists) {
                        fragment = new CustomLists();
                      } else if (drawerItem.getIdentifier() == R.id.navigation_favorites) {
                        fragment = new FavouritesActivity();
                      } else if (drawerItem.getIdentifier() == R.id.navigation_settings) {
                        fragment = new SettingsFragment();
                      } else if (drawerItem.getIdentifier() == R.id.navigation_changelog) {
                        fragment = new AboutFragment();
                      } else if (drawerItem.getIdentifier() == R.id.navigation_consegnati) {
                        //                                if (LUtils.hasL())
                        //
                        // mToolbar.setElevation(getResources().getDimension(R.dimen.design_appbar_elevation));
                        fragment = new ConsegnatiFragment();
                      } else if (drawerItem.getIdentifier() == R.id.navigation_history) {
                        fragment = new HistoryFragment();
                      } else return true;

                      // creo il nuovo fragment solo se non è lo stesso che sto già visualizzando
                      Fragment myFragment =
                          getSupportFragmentManager()
                              .findFragmentByTag(String.valueOf(drawerItem.getIdentifier()));
                      if (myFragment == null || !myFragment.isVisible()) {
                        FragmentTransaction transaction =
                            getSupportFragmentManager().beginTransaction();
                        if (!isOnTablet)
                          transaction.setCustomAnimations(
                              R.anim.slide_in_right, R.anim.slide_out_left);
                        transaction
                            .replace(
                                R.id.content_frame,
                                fragment,
                                String.valueOf(drawerItem.getIdentifier()))
                            .commit();
                      }

                      if (isOnTablet) mMiniDrawer.setSelection(drawerItem.getIdentifier());
                    }
                    return isOnTablet;
                  }
                })
            .withGenerateMiniDrawer(isOnTablet)
            .withSavedInstance(savedInstanceState)
            .withTranslucentStatusBar(!isOnTablet);

    if (isOnTablet) {
      mDrawer = mDrawerBuilder.buildView();
      // the MiniDrawer is managed by the Drawer and we just get it to hook it into the Crossfader
      mMiniDrawer =
          mDrawer
              .getMiniDrawer()
              .withEnableSelectedMiniDrawerItemBackground(true)
              .withIncludeSecondaryDrawerItems(true);

      // get the widths in px for the first and second panel
      int firstWidth = (int) UIUtils.convertDpToPixel(302, this);
      int secondWidth = (int) UIUtils.convertDpToPixel(72, this);

      // create and build our crossfader (see the MiniDrawer is also builded in here, as the build
      // method returns the view to be used in the crossfader)
      crossFader =
          new Crossfader()
              .withContent(findViewById(R.id.main_frame))
              .withFirst(mDrawer.getSlider(), firstWidth)
              .withSecond(mMiniDrawer.build(this), secondWidth)
              .withSavedInstance(savedInstanceState)
              .withGmailStyleSwiping()
              .build();

      // define the crossfader to be used with the miniDrawer. This is required to be able to
      // automatically toggle open / close
      mMiniDrawer.withCrossFader(new CrossfadeWrapper(crossFader));

      // define a shadow (this is only for normal LTR layouts if you have a RTL app you need to
      // define the other one
      crossFader
          .getCrossFadeSlidingPaneLayout()
          .setShadowResourceLeft(R.drawable.material_drawer_shadow_left);
      crossFader
          .getCrossFadeSlidingPaneLayout()
          .setShadowResourceRight(R.drawable.material_drawer_shadow_right);
    } else {
      mDrawer = mDrawerBuilder.build();
      mDrawer.getDrawerLayout().setStatusBarBackgroundColor(getThemeUtils().primaryColorDark());
    }
  }

  @Override
  public void onBackPressed() {
    Log.d(TAG, "onBackPressed: ");
    if (isOnTablet) {
      if (crossFader != null && crossFader.isCrossFaded()) {
        crossFader.crossFade();
        return;
      }
    } else {
      if (mDrawer != null && mDrawer.isDrawerOpen()) {
        mDrawer.closeDrawer();
        return;
      }
    }

    Fragment myFragment =
        getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.navigation_home));
    if (myFragment != null && myFragment.isVisible()) {
      finish();
      return;
    }

    if (isOnTablet) mMiniDrawer.setSelection(R.id.navigation_home);
    else {
      if (appBarLayout != null) appBarLayout.setExpanded(true, true);
    }
    mDrawer.setSelection(R.id.navigation_home);
  }

  @Override
  public void onColorSelection(
      @NonNull ColorChooserDialog colorChooserDialog, @ColorInt int color) {
    if (colorChooserDialog.isAccentMode()) getThemeUtils().accentColor(color);
    else getThemeUtils().primaryColor(color);

    recreate();
  }

  @Override
  public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {}

  // converte gli accordi salvati dalla lingua vecchia alla nuova
  //  private void convertTabs(SQLiteDatabase db, String conversion) {
  //  private void convertTabs(String conversion) {
  private void convertTabs() {
    String conversion = getIntent().getStringExtra(Utility.CHANGE_LANGUAGE);

    String[] accordi1 = CambioAccordi.accordi_it;
    Log.d(TAG, "convertTabs - from: " + conversion.substring(0, 2));
    switch (conversion.substring(0, 2)) {
      case "uk":
        accordi1 = CambioAccordi.accordi_uk;
        break;
      case "en":
        accordi1 = CambioAccordi.accordi_en;
        break;
    }

    String[] accordi2 = CambioAccordi.accordi_it;
    Log.d(TAG, "convertTabs - to: " + conversion.substring(3, 5));
    switch (conversion.substring(3, 5)) {
      case "uk":
        accordi2 = CambioAccordi.accordi_uk;
        break;
      case "en":
        accordi2 = CambioAccordi.accordi_en;
        break;
    }

    HashMap<String, String> mappa = new HashMap<>();
    for (int i = 0; i < CambioAccordi.accordi_it.length; i++) mappa.put(accordi1[i], accordi2[i]);

    CantoDao mDao = RisuscitoDatabase.getInstance(MainActivity.this).cantoDao();
    List<Canto> canti = mDao.getAllByName();
    for (Canto canto : canti) {
      if (canto.savedTab != null && !canto.savedTab.isEmpty()) {
        Log.d(
            TAG,
            "convertTabs: "
                + "ID "
                + canto.id
                + " -> CONVERTO DA "
                + canto.savedTab
                + " A "
                + mappa.get(canto.savedTab));
        canto.savedTab = mappa.get(canto.savedTab);
        mDao.updateCanto(canto);
      }
    }
  }

  // converte gli accordi salvati dalla lingua vecchia alla nuova
  //  private void convertiBarre(SQLiteDatabase db, String conversion) {
  //  private void convertiBarre(String conversion) {
  private void convertiBarre() {
    String conversion = getIntent().getStringExtra(Utility.CHANGE_LANGUAGE);

    String[] barre1 = CambioAccordi.barre_it;
    Log.d(TAG, "convertiBarre - from: " + conversion.substring(0, 2));
    switch (conversion.substring(0, 2)) {
      case "uk":
        barre1 = CambioAccordi.barre_uk;
        break;
      case "en":
        barre1 = CambioAccordi.barre_en;
        break;
    }

    String[] barre2 = CambioAccordi.barre_it;
    Log.d(TAG, "convertiBarre - to: " + conversion.substring(3, 5));
    switch (conversion.substring(3, 5)) {
      case "uk":
        barre2 = CambioAccordi.barre_uk;
        break;
      case "en":
        barre2 = CambioAccordi.barre_en;
        break;
    }

    HashMap<String, String> mappa = new HashMap<>();
    for (int i = 0; i < CambioAccordi.barre_it.length; i++) mappa.put(barre1[i], barre2[i]);

    CantoDao mDao = RisuscitoDatabase.getInstance(MainActivity.this).cantoDao();
    List<Canto> canti = mDao.getAllByName();
    for (Canto canto : canti) {
      if (canto.savedTab != null && !canto.savedTab.isEmpty()) {
        Log.d(
            TAG,
            "convertiBarre: "
                + "ID "
                + canto.id
                + " -> CONVERTO DA "
                + canto.savedBarre
                + " A "
                + mappa.get(canto.savedBarre));
        canto.savedBarre = mappa.get(canto.savedBarre);
        mDao.updateCanto(canto);
      }
    }
  }

  public void setupToolbarTitle(int titleResId) {
    ((TextView) mToolbar.findViewById(R.id.main_toolbarTitle)).setText(titleResId);
  }

  public void enableFab(boolean enable) {
    Log.d(TAG, "enableFab: " + enable);
    FloatingActionButton mFab = findViewById(R.id.fab_pager);
    if (enable) mFab.show();
    else mFab.hide();
  }

  public void enableBottombar(boolean enabled) {
    Log.d(TAG, "enableBottombar - enabled: " + enabled);
    View mBottomBar = findViewById(R.id.bottom_bar);
    if (enabled) mLUtils.animateIn(mBottomBar);
    else mBottomBar.setVisibility(View.GONE);
    //            mBottomBar.setVisibility(enabled ? View.VISIBLE : View.GONE);
  }
  // [END signIn]

  // [START signIn]
  public void signIn() {
    //    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    Intent signInIntent = mSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }
  // [END signOut]

  // [START signOut]
  private void signOut() {
    mSignInClient
        .signOut()
        .addOnCompleteListener(
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                updateUI(false);
                SharedPreferences.Editor editor =
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putBoolean(Utility.SIGNED_IN, false);
                editor.apply();
                Snackbar.make(
                        findViewById(R.id.main_content),
                        R.string.disconnected,
                        Snackbar.LENGTH_SHORT)
                    .show();
              }
            });
  }
  // [END revokeAccess]

  // [START revokeAccess]
  private void revokeAccess() {
    mSignInClient
        .revokeAccess()
        .addOnCompleteListener(
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                updateUI(false);
                SharedPreferences.Editor editor =
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putBoolean(Utility.SIGNED_IN, false);
                editor.apply();
                Snackbar.make(
                        findViewById(R.id.main_content),
                        R.string.disconnected,
                        Snackbar.LENGTH_SHORT)
                    .show();
              }
            });
  }

  // [START onActivityResult]
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Log.d(getClass().getName(), "requestCode: " + requestCode);
    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    switch (requestCode) {
      case RC_SIGN_IN:
        handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data));
        break;
      default:
        break;
    }
  }
  // [END handleSignInResult]

  // [START handleSignInResult]
  private void handleSignInResult(Task<GoogleSignInAccount> task) {
    //    Log.d(getClass().getName(), "handleSignInResult:" + result.isSuccess());
    Log.d(getClass().getName(), "handleSignInResult:" + task.isSuccessful());
    if (task.isSuccessful()) {
      // Signed in successfully, show authenticated UI.
      acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
      SharedPreferences.Editor editor =
          PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
      editor.putBoolean(Utility.SIGNED_IN, true);
      editor.apply();
      if (mViewModel.showSnackbar) {
        Snackbar.make(
                findViewById(R.id.main_content),
                getString(R.string.connected_as, acct.getDisplayName()),
                Snackbar.LENGTH_SHORT)
            .show();
        mViewModel.showSnackbar = false;
      }
      updateUI(true);
    } else {
      // Signed out, show unauthenticated UI.
      acct = null;
      updateUI(false);
    }
  }

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
        personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() - 2) + 400;
        profile =
            new ProfileDrawerItem()
                .withName(acct.getDisplayName())
                .withEmail(acct.getEmail())
                .withIcon(personPhotoUrl)
                .withIdentifier(PROF_ID)
                .withTypeface(mRegularFont);
      } else {
        profile =
            new ProfileDrawerItem()
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
            new ProfileSettingDrawerItem()
                .withName(getString(R.string.gdrive_backup))
                .withIcon(CommunityMaterial.Icon.cmd_cloud_upload)
                .withIdentifier(R.id.gdrive_backup),
            new ProfileSettingDrawerItem()
                .withName(getString(R.string.gdrive_restore))
                .withIcon(CommunityMaterial.Icon.cmd_cloud_download)
                .withIdentifier(R.id.gdrive_restore),
            new ProfileSettingDrawerItem()
                .withName(getString(R.string.gplus_signout))
                .withIcon(CommunityMaterial.Icon.cmd_account_remove)
                .withIdentifier(R.id.gplus_signout),
            new ProfileSettingDrawerItem()
                .withName(getString(R.string.gplus_revoke))
                .withIcon(CommunityMaterial.Icon.cmd_account_key)
                .withIdentifier(R.id.gplus_revoke));
      }
      if (isOnTablet) mMiniDrawer.onProfileClick();
    } else {
      IProfile profile =
          new ProfileDrawerItem()
              .withName("")
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
      if (isOnTablet) mMiniDrawer.onProfileClick();
    }
    hideProgressDialog();
  }

  private void showProgressDialog() {
    mCircleProgressBar.setVisibility(View.VISIBLE);
  }

  private void hideProgressDialog() {
    mCircleProgressBar.setVisibility(View.GONE);
  }

  public void setShowSnackbar() {
    this.mViewModel.showSnackbar = true;
  }

  @Override
  public void onPositive(@NonNull String tag) {
    Log.d(getClass().getName(), "onPositive: TAG " + tag);
    switch (tag) {
      case "BACKUP_ASK":
        new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "BACKUP_RUNNING")
            .title(R.string.backup_running)
            .content(R.string.backup_database)
            .showProgress()
            .progressIndeterminate(true)
            .progressMax(0)
            .show();
        new BackupTask().execute();
        break;
      case "RESTORE_ASK":
        new SimpleDialogFragment.Builder(MainActivity.this, MainActivity.this, "RESTORE_RUNNING")
            .title(R.string.restore_running)
            .content(R.string.restoring_database)
            .showProgress()
            .progressIndeterminate(true)
            .progressMax(0)
            .show();
        new RestoreTask().execute();
        break;
      case "SIGNOUT":
        signOut();
        break;
      case "REVOKE":
        revokeAccess();
        break;
      case "RESTART":
        Intent i =
            getBaseContext()
                .getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (i != null) i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
    if (sFragment != null) sFragment.dismiss();
  }

  private void setDialogCallback(String tag) {
    SimpleDialogFragment sFragment = SimpleDialogFragment.findVisible(MainActivity.this, tag);
    if (sFragment != null) sFragment.setmCallback(MainActivity.this);
  }

  private static class TranslationTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<MainActivity> activityWeakReference;

    TranslationTask(MainActivity activity) {
      this.activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected Void doInBackground(Void... sUrl) {
      activityWeakReference.get().getIntent().removeExtra(Utility.DB_RESET);
      DatabaseCanti listaCanti = new DatabaseCanti(activityWeakReference.get());
      SQLiteDatabase db = listaCanti.getReadableDatabase();
      listaCanti.reCreateDatabse(db);
      db.close();
      listaCanti.close();
      RisuscitoDatabase.getInstance(activityWeakReference.get())
          .recreateDB(activityWeakReference.get());
      activityWeakReference.get().convertTabs();
      activityWeakReference.get().convertiBarre();
      return null;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      new SimpleDialogFragment.Builder(
              activityWeakReference.get(), activityWeakReference.get(), "TRANSLATION")
          .content(R.string.translation_running)
          .showProgress()
          .progressIndeterminate(true)
          .progressMax(0)
          .show();
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      activityWeakReference.get().getIntent().removeExtra(Utility.CHANGE_LANGUAGE);
      try {
        activityWeakReference.get().dismissDialog("TRANSLATION");
      } catch (IllegalArgumentException e) {
        Log.e(getClass().getName(), e.getLocalizedMessage(), e);
      }
    }
  }

  @SuppressLint("StaticFieldLeak")
  private class BackupTask extends AsyncTask<Void, Void, Void> {

    BackupTask() {}

    @Override
    protected Void doInBackground(Void... sUrl) {
      try {
        checkDuplTosave(RisuscitoDatabase.getDbName(), "application/x-sqlite3", true);
        Intent intentBroadcast = new Intent("BROADCAST_NEXT_STEP");
        intentBroadcast.putExtra("WHICH", "BACKUP");
        sendBroadcast(intentBroadcast);
        checkDuplTosave(PREF_DRIVE_FILE_NAME, "application/json", false);
      } catch (Exception e) {
        Log.e(getClass().getName(), "Exception: " + e.getLocalizedMessage(), e);
        String error = "error: " + e.getLocalizedMessage();
        Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
        cancel(true);
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      if (isCancelled()) return;
      Intent intentBroadcast = new Intent("BROADCAST_LAST_STEP");
      intentBroadcast.putExtra("WHICH", "BACKUP");
      sendBroadcast(intentBroadcast);
    }
  }

  @SuppressLint("StaticFieldLeak")
  private class RestoreTask extends AsyncTask<Void, Void, Void> {

    RestoreTask() {}

    @Override
    protected Void doInBackground(Void... sUrl) {
      try {
        if (checkDupl(RisuscitoDatabase.getDbName())) restoreNewDbBackup();
        else restoreOldDriveBackup();
        //        if (checkDupl(DatabaseCanti.getDbName())) restoreOldDriveBackup();
        //        else restoreNewDbBackup();
        Intent intentBroadcast = new Intent("BROADCAST_NEXT_STEP");
        intentBroadcast.putExtra("WHICH", "RESTORE");
        sendBroadcast(intentBroadcast);
        restoreDrivePrefBackup(PREF_DRIVE_FILE_NAME);
      } catch (Exception e) {
        Log.e(getClass().getName(), "Exception: " + e.getLocalizedMessage(), e);
        String error = "error: " + e.getLocalizedMessage();
        Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT).show();
        cancel(true);
      }

      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      if (isCancelled()) return;
      Intent intentBroadcast = new Intent("BROADCAST_LAST_STEP");
      intentBroadcast.putExtra("WHICH", "RESTORE");
      sendBroadcast(intentBroadcast);
    }
  }
}
