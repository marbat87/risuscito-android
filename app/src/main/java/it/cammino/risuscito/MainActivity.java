package it.cammino.risuscito;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import it.cammino.risuscito.ui.ThemeableActivity;

public class MainActivity extends ThemeableActivity
        implements ColorChooserDialog.ColorCallback, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener{

    public DrawerLayout mDrawerLayout;
    protected static final String SELECTED_ITEM = "oggetto_selezionato";
    private static final String SHOW_SNACKBAR = "mostra_snackbar";

    private int prevOrientation;

    private NavigationView mNavigationView;

//    private static final int TALBLET_DP = 600;
//    private static final int WIDTH_320 = 320;
//    private static final int WIDTH_400 = 400;

    MaterialDialog mProgressDialog;

    private boolean showSnackbar;
    private GoogleSignInAccount acct;
    private ImageView profileImage;
    private ImageView profileBackground;
    private View copertinaAccount;
    private ImageView accountMenu;
    private TextView usernameTextView;
    private TextView emailTextView;

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 9001;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.hasNavDrawer = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().getBooleanExtra(Utility.DB_RESET, false)) {
            (new TranslationTask()).execute();
        }

        setupNavDrawer();

        View header = mNavigationView.getHeaderView(0);

        profileImage = (ImageView) header.findViewById(R.id.profile_image);
        profileImage.setVisibility(View.INVISIBLE);
        usernameTextView = (TextView) header.findViewById(R.id.username);
        usernameTextView.setVisibility(View.INVISIBLE);
        emailTextView = (TextView) header.findViewById(R.id.email);
        emailTextView.setVisibility(View.INVISIBLE);
        profileBackground = (ImageView) header.findViewById(R.id.copertina);
        copertinaAccount = header.findViewById(R.id.copertina_account);
        accountMenu = (ImageView) header.findViewById(R.id.account_menu);
        Drawable drawable = DrawableCompat.wrap(accountMenu.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(MainActivity.this, android.R.color.white));
        accountMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountMenu.setSelected(!accountMenu.isSelected());
                mNavigationView.getMenu().clear();
                mNavigationView.inflateMenu(accountMenu.isSelected()
                        ? R.menu.drawer_account_menu : R.menu.drawer_menu);
            }
        });

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        showSnackbar = savedInstanceState == null
                || savedInstanceState.getBoolean(SHOW_SNACKBAR, true);

        if (findViewById(R.id.content_frame) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                mNavigationView.getMenu().getItem(savedInstanceState.getInt(SELECTED_ITEM)).setChecked(true);
            }
            else
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new Risuscito(), String.valueOf(R.id.navigation_home)).commit();
        }

        mProgressDialog = new MaterialDialog.Builder(this)
                .title(R.string.connection_running)
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .build();

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

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
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        for (int i = 0; mNavigationView.getMenu().getItem(i) != null; i++) {
            if (mNavigationView.getMenu().getItem(i).isChecked()) {
                savedInstanceState.putInt(SELECTED_ITEM, i);
                break;
            }
        }
        //questo pezzo salva l'elenco dei titoli checkati del fragment ConsegnatiFragment, quando si ruota lo schermo
        ConsegnatiFragment consegnatiFragment = (ConsegnatiFragment)getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.navigation_consegnati));
        if (consegnatiFragment != null && consegnatiFragment.isVisible() && consegnatiFragment.getTitoliChoose() != null) {
            ConsegnatiFragment.RetainedFragment dataFragment = new ConsegnatiFragment.RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(dataFragment, ConsegnatiFragment.TITOLI_CHOOSE).commit();
            dataFragment.setData(consegnatiFragment.getTitoliChoose());
        }

        savedInstanceState.putBoolean(SHOW_SNACKBAR, showSnackbar);
        savedInstanceState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupNavDrawer() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.my_drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        mDrawerLayout.setStatusBarBackgroundColor(getThemeUtils().primaryColorDark());
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        ColorStateList mIconStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked}, //1
                        new int[]{} //2
                },
                new int[] {
                        getThemeUtils().primaryColor(), //1
                        ContextCompat.getColor(MainActivity.this, R.color.navdrawer_icon_tint) // 2
                }
        );

        ColorStateList mTextStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked}, //1
                        new int[]{} //2
                },
                new int[] {
                        getThemeUtils().primaryColor(), //1
                        ContextCompat.getColor(MainActivity.this, R.color.navdrawer_text_color) //2
                }
        );

        mNavigationView.setItemIconTintList(mIconStateList);
        mNavigationView.setItemTextColor(mTextStateList);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(R.id.navigation_home));
            if (myFragment != null && myFragment.isVisible()) {
                finish();
                return true;
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.replace(R.id.content_frame, new Risuscito(), String.valueOf(R.id.navigation_home)).commit();
            mNavigationView.getMenu().getItem(0).setChecked(true);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int color) {
        if (colorChooserDialog.isAccentMode())
            getThemeUtils().accentColor(color);
        else
            getThemeUtils().primaryColor(color);

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            recreate();
        }
        else {
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    //converte gli accordi salvati dalla lingua vecchia alla nuova
    private void convertTabs(SQLiteDatabase db, String conversion) {
//        Log.i(getClass().toString(), "CONVERSION: " + conversion);
        HashMap<String, String> mappa = null;
        if (conversion.equalsIgnoreCase("it-uk")) {
            mappa = new HashMap<>();
            for (int i = 0; i < CambioAccordi.accordi_it.length; i++)
                mappa.put(CambioAccordi.accordi_it[i], CambioAccordi.accordi_uk[i]);
        }
        if (conversion.equalsIgnoreCase("uk-it")) {
            mappa = new HashMap<>();
            for (int i = 0; i < CambioAccordi.accordi_it.length; i++)
                mappa.put(CambioAccordi.accordi_uk[i], CambioAccordi.accordi_it[i]);
        }
        if (mappa != null) {
            String query = "SELECT _id, saved_tab" +
                    "  FROM ELENCO";
            Cursor cursor = db.rawQuery(query, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (cursor.getString(1) != null && !cursor.getString(1).equals("")) {
//                Log.i(getClass().toString(),"ID " + cursor.getInt(0) +  " -> CONVERTO DA " + cursor.getString(1) + " A " + mappa.get(cursor.getString(1)) );
                    query = "UPDATE ELENCO" +
                            "  SET saved_tab = \'" + mappa.get(cursor.getString(1)) + "\' " +
                            "  WHERE _id =  " + cursor.getInt(0);
                    db.execSQL(query);
                }
                cursor.moveToNext();
            }
        }
    }

    private class TranslationTask extends AsyncTask<String, Integer, String> {

        public TranslationTask() {}

        private MaterialDialog translationDialog;

        @Override
        protected String doInBackground(String... sUrl) {
            getIntent().removeExtra(Utility.DB_RESET);
            DatabaseCanti listaCanti = new DatabaseCanti(MainActivity.this);
            SQLiteDatabase db = listaCanti.getReadableDatabase();
            DatabaseCanti.Backup[] backup = listaCanti.backupTables(db.getVersion(), db.getVersion(), db);
            DatabaseCanti.BackupLocalLink[] backupLink = listaCanti.backupLocalLink(db.getVersion(), db.getVersion(), db);
            listaCanti.reCreateDatabse(db);
            listaCanti.repopulateDB(db.getVersion(), db.getVersion(), db, backup, backupLink);
            convertTabs(db, getIntent().getStringExtra(Utility.CHANGE_LANGUAGE));
            db.close();
            listaCanti.close();
            return "";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prevOrientation = getRequestedOrientation();
            Utility.blockOrientation(MainActivity.this);
            translationDialog = new MaterialDialog.Builder(MainActivity.this)
                    .content(R.string.translation_running)
                    .progress(true, 0)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            setRequestedOrientation(prevOrientation);
                        }
                    })
                    .show();
        }

        @Override
        protected void onPostExecute(String result) {
            getIntent().removeExtra(Utility.CHANGE_LANGUAGE);
            try {
                if (translationDialog.isShowing())
                    translationDialog.dismiss();
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
        }
    }

    public void setupToolbar(View toolbar, int titleResId) {
        Toolbar mActionToolbar = (Toolbar) toolbar;
        setSupportActionBar(mActionToolbar);
        mActionToolbar.setBackgroundColor(getThemeUtils().primaryColor());
        getSupportActionBar().setTitle("");
        ((TextView)toolbar.findViewById(R.id.main_toolbarTitle)).setText(titleResId);
        mActionToolbar.setNavigationIcon(R.drawable.ic_menu_24dp);
        Drawable drawable = DrawableCompat.wrap(mActionToolbar.getNavigationIcon());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(MainActivity.this, android.R.color.white));
        mActionToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        Fragment fragment;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new Risuscito();
                break;
            case R.id.navigation_search:
                fragment = new GeneralSearch();
                break;
            case R.id.navigation_indexes:
                fragment = new GeneralIndex();
                break;
            case R.id.navitagion_lists:
                fragment = new CustomLists();
                break;
            case R.id.navigation_favorites:
                fragment = new FavouritesActivity();
                break;
            case R.id.navigation_settings:
                fragment = new PreferencesFragment();
                break;
            case R.id.navigation_changelog:
                fragment = new AboutActivity();
                break;
            case R.id.navigation_donate:
                fragment = new DonateActivity();
                break;
            case R.id.navigation_consegnati:
                fragment = new ConsegnatiFragment();
                break;
            case R.id.navigation_history:
                fragment = new HistoryFragment();
                break;
            case R.id.gplus_signout:
                accountMenu.performClick();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                prevOrientation = getRequestedOrientation();
                Utility.blockOrientation(MainActivity.this);
                MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.gplus_signout)
                        .content(R.string.dialog_acc_disconn_text)
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.dismiss)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                signOut();
                                setRequestedOrientation(prevOrientation);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                setRequestedOrientation(prevOrientation);
                            }
                        })
                        .show();
                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getAction() == KeyEvent.ACTION_UP) {
                            arg0.dismiss();
                            setRequestedOrientation(prevOrientation);
                            return true;
                        }
                        return false;
                    }
                });
                dialog.setCancelable(false);
                return true;
            case R.id.gplus_revoke:
                accountMenu.performClick();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                prevOrientation = getRequestedOrientation();
                Utility.blockOrientation(MainActivity.this);
                dialog = new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.gplus_revoke)
                        .content(R.string.dialog_acc_revoke_text)
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.dismiss)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                revokeAccess();
                                setRequestedOrientation(prevOrientation);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                setRequestedOrientation(prevOrientation);
                            }
                        })
                        .show();
                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getAction() == KeyEvent.ACTION_UP) {
                            arg0.dismiss();
                            setRequestedOrientation(prevOrientation);
                            return true;
                        }
                        return false;
                    }
                });
                dialog.setCancelable(false);
                return true;
            default:
                fragment = new Risuscito();
                break;
        }

        //creo il nuovo fragment solo se non è lo stesso che sto già visualizzando
        Fragment myFragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(item.getItemId()));
        if (myFragment == null || !myFragment.isVisible()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            transaction.replace(R.id.content_frame, fragment, String.valueOf(item.getItemId())).commit();

            android.os.Handler mHandler = new android.os.Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            }, 250);
        }
        return true;
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
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        Snackbar.make(findViewById(android.R.id.content), R.string.disconnected, Snackbar.LENGTH_SHORT).show();
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
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        Snackbar.make(findViewById(android.R.id.content), R.string.disconnected, Snackbar.LENGTH_SHORT).show();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
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
            handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(getClass().getName(), "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
            if (showSnackbar) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.connected_as, acct.getDisplayName()), Snackbar.LENGTH_SHORT).show();
                showSnackbar = false;
            }
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            acct = null;
            updateUI(false);
        }
    }
    // [END handleSignInResult]

    @SuppressWarnings("deprecation")
    private void updateUI(boolean signedIn) {
        if (signedIn) {
//            Log.d(getClass().getName(), "currentPerson: " + Plus.PeopleApi.getCurrentPerson(mGoogleApiClient));
//            if (mGoogleApiClient.isConnected() && Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
//                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
//
//                String personCoverUrl = currentPerson.getCover().getCoverPhoto().getUrl();
//                Picasso.with(this)
//                        .load(personCoverUrl)
//                        .error(R.drawable.copertina_about)
//                        .into(profileBackground);
            if (LUtils.hasJB())
                copertinaAccount.setBackground(new ColorDrawable(getThemeUtils().primaryColor()));
            else
                copertinaAccount.setBackgroundDrawable(new ColorDrawable(getThemeUtils().primaryColor()));
            profileBackground.setVisibility(View.INVISIBLE);

//            Log.d(getClass().getName(), "acct.getPhotoUrl().toString():" + acct.getPhotoUrl().toString());
            String personPhotoUrl = acct.getPhotoUrl().toString();
            // by default the profile url gives 50x50 px image only
            // we can replace the value with whatever dimension we want by
            // replacing sz=X
            personPhotoUrl = personPhotoUrl.substring(0,
                    personPhotoUrl.length() - 2)
                    + 400;
            Picasso.with(this)
                    .load(personPhotoUrl)
                    .error(R.drawable.copertina_about)
                    .into(profileImage);
            profileImage.setVisibility(View.VISIBLE);

            String personName = acct.getDisplayName();
//                Log.d(getClass().getName(), "personName: " + personName);
            usernameTextView.setText(personName);
            usernameTextView.setVisibility(View.VISIBLE);

//                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            String email = acct.getEmail();
//                Log.d(getClass().getName(), "email: " + email);
            emailTextView.setText(email);
            emailTextView.setVisibility(View.VISIBLE);

            if (findViewById(R.id.sign_in_button) != null)
                findViewById(R.id.sign_in_button).setVisibility(View.INVISIBLE);

            accountMenu.setVisibility(View.VISIBLE);
//            }
        }
        else {
            profileImage.setVisibility(View.INVISIBLE);
            usernameTextView.setVisibility(View.INVISIBLE);
            emailTextView.setVisibility(View.INVISIBLE);
            if (LUtils.hasJB())
                copertinaAccount.setBackground(new ColorDrawable(ContextCompat.getColor(MainActivity.this, android.R.color.transparent)));
            else
                copertinaAccount.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(MainActivity.this, android.R.color.transparent)));
            profileBackground.setVisibility(View.VISIBLE);
//            profileBackground.setImageResource(R.drawable.copertina_about);
            if (findViewById(R.id.sign_in_button) != null)
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            accountMenu.setVisibility(View.INVISIBLE);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null && !mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.hide();
    }

    public void setShowSnackbar(boolean showSnackbar) {
        this.showSnackbar = showSnackbar;
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }
}
