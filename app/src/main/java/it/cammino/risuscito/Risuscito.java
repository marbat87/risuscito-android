package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.google.android.gms.common.SignInButton;
import com.stephentuso.welcome.WelcomeScreenHelper;

import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.slides.IntroMainNew;

public class Risuscito extends Fragment implements SimpleDialogFragment.SimpleCallback {

    private final String TAG = getClass().getCanonicalName();

    private static final String VERSION_KEY = "PREFS_VERSION_KEY";
    private static final String NO_VERSION = "";
    public static final String BROADCAST_SIGNIN_VISIBLE = "it.cammino.risuscito.signin.SIGNIN_VISIBLE";
    public static final String DATA_VISIBLE = "it.cammino.risuscito.signin.data.DATA_VISIBLE";
    //    private static final String FIRST_OPEN_MENU = "FIRST_OPEN_LOGIN";
//    private int prevOrientation;
    private WelcomeScreenHelper mWelcomeScreen;

    private SignInButton mSignInButton;

    private MainActivity mMainActivity;

    private BroadcastReceiver signInVisibility = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Implement UI change code here once notification is received
            try {
                Log.d(getClass().getName(), "BROADCAST_SIGNIN_VISIBLE");
                Log.d(getClass().getName(), "DATA_VISIBLE: " + intent.getBooleanExtra(DATA_VISIBLE, false));
                mSignInButton.setVisibility(intent.getBooleanExtra(DATA_VISIBLE, false) ? View.VISIBLE : View.INVISIBLE);
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getName(), e.getLocalizedMessage(), e);
            }
        }
    };

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_risuscito, container, false);

        mMainActivity = (MainActivity) getActivity();

//        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.activity_homepage);
        mMainActivity.setupToolbarTitle(R.string.activity_homepage);
        if (!mMainActivity.isOnTablet())
            mMainActivity.enableFab(false);
        getActivity().findViewById(R.id.material_tabs).setVisibility(View.GONE);

        rootView.findViewById(R.id.imageView1)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.my_drawer_layout);
//                        drawerLayout.openDrawer(GravityCompat.START);
                        mMainActivity.getDrawer().openDrawer();
                    }
                });

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        // get version numbers
        String lastVersion = sp.getString(VERSION_KEY, NO_VERSION);
        String thisVersion;
//        Log.i("Changelog", "lastVersion: " + lastVersion);
        try {
            thisVersion = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            thisVersion = NO_VERSION;
//            Log.i("Changelog", "could not get version name from manifest!");
            e.printStackTrace();
        }
//        Log.i("Changelog", "appVersion: " + thisVersion);

        if (!thisVersion.equals(lastVersion)) {
//            prevOrientation = getActivity().getRequestedOrientation();
//            Utility.blockOrientation(getActivity());
//            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
//                    .title(R.string.dialog_change_title)
//                    .customView(R.layout.dialog_changelogview, false)
//                    .positiveText(R.string.dialog_chiudi)
//                    .onPositive(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
//                            getActivity().setRequestedOrientation(prevOrientation);
//                            if(PreferenceManager
//                                    .getDefaultSharedPreferences(getActivity())
//                                    .getBoolean(FIRST_OPEN_MENU, true)) {
//                                SharedPreferences.Editor editor = PreferenceManager
//                                        .getDefaultSharedPreferences(getActivity())
//                                        .edit();
//                                editor.putBoolean(FIRST_OPEN_MENU, false);
//                                editor.apply();
//                                showHelp();
//                            }
//                        }
//                    })
//                    .show();
//
//            dialog.setOnKeyListener(new Dialog.OnKeyListener() {
//                @Override
//                public boolean onKey(DialogInterface arg0, int keyCode,
//                                     KeyEvent event) {
//                    if (keyCode == KeyEvent.KEYCODE_BACK
//                            && event.getAction() == KeyEvent.ACTION_UP) {
//                        arg0.dismiss();
//                        getActivity().setRequestedOrientation(prevOrientation);
//                        if(PreferenceManager
//                                .getDefaultSharedPreferences(getActivity())
//                                .getBoolean(FIRST_OPEN_MENU, true)) {
//                            SharedPreferences.Editor editor = PreferenceManager
//                                    .getDefaultSharedPreferences(getActivity())
//                                    .edit();
//                            editor.putBoolean(FIRST_OPEN_MENU, false);
//                            editor.apply();
//                            showHelp();
//                        }
//                        return true;
//                    }
//                    return false;
//                }
//            });
//            dialog.setCancelable(false);
            mWelcomeScreen = new WelcomeScreenHelper(getActivity(), IntroMainNew.class);
            mWelcomeScreen.show(savedInstanceState);
//            if(PreferenceManager
//                    .getDefaultSharedPreferences(getActivity())
//                    .getBoolean(FIRST_OPEN_MENU, true)) {
//                SharedPreferences.Editor editor = PreferenceManager
//                        .getDefaultSharedPreferences(getActivity())
//                        .edit();
//                editor.putBoolean(FIRST_OPEN_MENU, false);
//                editor.apply();
//                showHelp();
//            }
            new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), Risuscito.this, "CHANGELOG")
                    .title(R.string.dialog_change_title)
                    .setCustomView(R.layout.dialog_changelogview)
                    .positiveButton(R.string.dialog_chiudi)
                    .show();
//                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
//                        @Override
//                        public void onCancel(DialogInterface dialog) {
//                            if(PreferenceManager
//                                    .getDefaultSharedPreferences(getActivity())
//                                    .getBoolean(FIRST_OPEN_MENU, true)) {
//                                SharedPreferences.Editor editor = PreferenceManager
//                                        .getDefaultSharedPreferences(getActivity())
//                                        .edit();
//                                editor.putBoolean(FIRST_OPEN_MENU, false);
//                                editor.apply();
//                                showHelp();
//                            }
//                        }
//                    });
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(VERSION_KEY, thisVersion);
            editor.apply();
        }
        else {
            mWelcomeScreen = new WelcomeScreenHelper(getActivity(), IntroMainNew.class);
            mWelcomeScreen.show(savedInstanceState);
//            if(PreferenceManager
//                    .getDefaultSharedPreferences(getActivity())
//                    .getBoolean(FIRST_OPEN_MENU, true)) {
//                SharedPreferences.Editor editor = PreferenceManager
//                        .getDefaultSharedPreferences(getActivity())
//                        .edit();
//                editor.putBoolean(FIRST_OPEN_MENU, false);
//                editor.apply();
////                final Runnable mMyRunnable = new Runnable() {
////                    @Override
////                    public void run() {
////                        showHelp();
////                    }
////                };
////                Handler myHandler = new Handler();
////                myHandler.postDelayed(mMyRunnable, 1000);
//
//            }
        }

        PaginaRenderActivity.notaCambio = null;
        PaginaRenderActivity.speedValue = null;
        PaginaRenderActivity.scrollPlaying = false;
        PaginaRenderActivity.mostraAudio = null;

        //apertura e chiusura database per consentire eventuale aggiornamento
        DatabaseCanti listaCanti = new DatabaseCanti(getActivity());
        SQLiteDatabase db = listaCanti.getReadableDatabase();
        db.close();
        listaCanti.close();

        mSignInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        mSignInButton.setSize(SignInButton.SIZE_WIDE);
//        signInButton.setScopes(gso.getScopeArray());
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainActivity.setShowSnackbar(true);
                mMainActivity.signIn();
            }
        });

//        if (getActivity() != null && getActivity() instanceof ThemeableActivity) {
//            MainActivity activity = (MainActivity) getActivity();
//            rootView.findViewById(R.id.sign_in_button).setVisibility(activity.getmGoogleApiClient().isConnected() ? View.INVISIBLE : View.VISIBLE);
        Log.d(TAG, "onCreateView: signed in = " + PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SIGNED_IN, false));
        rootView.findViewById(R.id.sign_in_button).setVisibility(PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SIGNED_IN, false) ? View.INVISIBLE : View.VISIBLE);
//        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(signInVisibility, new IntentFilter(
                BROADCAST_SIGNIN_VISIBLE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(signInVisibility);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.help_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
//                showHelp();
                mWelcomeScreen.forceShow();
                return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWelcomeScreen.onSaveInstanceState(outState);
    }

    private void showHelp() {
//        Intent intent = new Intent(getActivity(), IntroMain.class);
//        startActivity(intent);
        WelcomeScreenHelper welcomeScreen = new WelcomeScreenHelper(getActivity(), IntroMainNew.class);
        welcomeScreen.forceShow();
    }

    @Override
    public void onPositive(@NonNull String tag) {
//        switch (tag) {
//            case "CHANGELOG":
//                if(PreferenceManager
//                        .getDefaultSharedPreferences(getActivity())
//                        .getBoolean(FIRST_OPEN_MENU, true)) {
//                    SharedPreferences.Editor editor = PreferenceManager
//                            .getDefaultSharedPreferences(getActivity())
//                            .edit();
//                    editor.putBoolean(FIRST_OPEN_MENU, false);
//                    editor.apply();
//                    showHelp();
//                }
//                break;
//        }
    }
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}

}
