package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.SignInButton;

import it.cammino.risuscito.slides.IntroMain;
import it.cammino.risuscito.ui.ThemeableActivity;

public class Risuscito extends Fragment {

    private static final String VERSION_KEY = "PREFS_VERSION_KEY";
    private static final String NO_VERSION = "";
    private static final String FIRST_OPEN_MENU = "FIRST_OPEN_MENU4";
    private int prevOrientation;

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_risuscito, container, false);

        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.activity_homepage);

        rootView.findViewById(R.id.imageView1)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.my_drawer_layout);
                        drawerLayout.openDrawer(GravityCompat.START);
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
            prevOrientation = getActivity().getRequestedOrientation();
            Utility.blockOrientation(getActivity());
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_change_title)
                    .customView(R.layout.dialog_changelogview, false)
                    .positiveText(R.string.dialog_chiudi)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            getActivity().setRequestedOrientation(prevOrientation);
                            if(PreferenceManager
                                    .getDefaultSharedPreferences(getActivity())
                                    .getBoolean(FIRST_OPEN_MENU, true)) {
                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(getActivity())
                                        .edit();
                                editor.putBoolean(FIRST_OPEN_MENU, false);
                                editor.apply();
                                showHelp();
                            }
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
                        getActivity().setRequestedOrientation(prevOrientation);
                        if(PreferenceManager
                                .getDefaultSharedPreferences(getActivity())
                                .getBoolean(FIRST_OPEN_MENU, true)) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity())
                                    .edit();
                            editor.putBoolean(FIRST_OPEN_MENU, false);
                            editor.apply();
                            showHelp();
                        }
                        return true;
                    }
                    return false;
                }
            });
            dialog.setCancelable(false);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(VERSION_KEY, thisVersion);
            editor.apply();
        }
        else {
            if(PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .getBoolean(FIRST_OPEN_MENU, true)) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit();
                editor.putBoolean(FIRST_OPEN_MENU, false);
                editor.apply();
                final Runnable mMyRunnable = new Runnable() {
                    @Override
                    public void run() {
                        showHelp();
                    }
                };
                Handler myHandler = new Handler();
                myHandler.postDelayed(mMyRunnable, 1000);
            }
        }

        PaginaRenderActivity.notaCambio = null;
        PaginaRenderActivity.speedValue = null;
        PaginaRenderActivity.scrollPlaying = false;
        PaginaRenderActivity.mostraAudio = null;

        SignInButton signInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
//        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).setShowSnackbar(true);
                ((MainActivity)getActivity()).signIn();
            }
        });

        if (getActivity() != null && getActivity() instanceof ThemeableActivity) {
            MainActivity activity = (MainActivity) getActivity();
            Log.d(getClass().getName(), "activity.getmGoogleApiClient().isConnected(): " + activity.getmGoogleApiClient().isConnected());
            if (activity.getmGoogleApiClient() != null
                    && activity.getmGoogleApiClient().isConnected())
                rootView.findViewById(R.id.sign_in_button).setVisibility(View.INVISIBLE);
            else
                rootView.findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }

        return rootView;
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
                showHelp();
                return true;
        }
        return false;
    }

    private void showHelp() {
        Intent intent = new Intent(getActivity(), IntroMain.class);
        startActivity(intent);
    }

}
