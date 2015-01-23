package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.alertdialogpro.AlertDialogPro;

import it.cammino.utilities.showcaseview.OnShowcaseEventListener;
import it.cammino.utilities.showcaseview.ShowcaseView;
import it.cammino.utilities.showcaseview.targets.ViewTarget;
import it.gmariotti.changelibs.library.internal.ChangeLogAdapter;
import it.gmariotti.changelibs.library.view.ChangeLogListView;

public class Risuscito extends Fragment {

    private static final String VERSION_KEY = "PREFS_VERSION_KEY";
    private static final String NO_VERSION = "";
    private static final String FIRST_OPEN_MENU = "FIRST_OPEN_MENU4";
    private int prevOrientation;
    private int screenWidth;
    private int screenHeight;

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_risuscito, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.activity_homepage);
        ((MainActivity) getActivity()).getSupportActionBar()
                .setElevation(getResources().getInteger(R.integer.toolbar_elevation));

        rootView.findViewById(R.id.imageView1)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.my_drawer_layout);
                        drawerLayout.openDrawer(Gravity.START);
                    }
                });

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            screenWidth = display.getWidth();
            screenHeight = display.getHeight();
        }
        else {
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
        }

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

//        if (true) {
        if (!thisVersion.equals(lastVersion)) {
            prevOrientation = getActivity().getRequestedOrientation();
            Utility.blockOrientation(getActivity());
            AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
            ChangeLogListView chglv = new ChangeLogListView(getActivity());
            ChangeLogAdapter adapter = (ChangeLogAdapter) chglv.getAdapter();
            adapter.setmRowHeaderLayoutId(R.layout.changelogrowheader_material_layout);
            adapter.setmRowLayoutId(R.layout.changelogrow_material_layout);
            AlertDialogPro dialog = builder.setTitle(getResources().getString(R.string.dialog_change_title))
                    .setView(chglv)
                    .setPositiveButton(getResources().getString(R.string.dialog_chiudi), new ButtonClickedListener())
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
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                                editor.commit();
                            } else {
                                editor.apply();
                            }
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
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                editor.commit();
            } else {
                editor.apply();
            }
        }
        else {
            if(PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .getBoolean(FIRST_OPEN_MENU, true)) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit();
                editor.putBoolean(FIRST_OPEN_MENU, false);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    editor.commit();
                } else {
                    editor.apply();
                }
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
        PaginaRenderActivity.mostraAudio = false;

//        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.help_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
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

    @SuppressLint("NewApi")
    private class ButtonClickedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            getActivity().setRequestedOrientation(prevOrientation);
            if(PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .getBoolean(FIRST_OPEN_MENU, true)) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit();
                editor.putBoolean(FIRST_OPEN_MENU, false);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    editor.commit();
                } else {
                    editor.apply();
                }
                showHelp();
            }
        }
    }

    private void showHelp() {
        prevOrientation = getActivity().getRequestedOrientation();
        Utility.blockOrientation(getActivity());

        //nuovo menu
        ShowcaseView showcaseView = ShowcaseView.insertShowcaseView(
                new ViewTarget(R.id.imageView1, getActivity())
                , getActivity()
                , R.string.help_new_menu_title
                , R.string.help_new_menu_desc);
        showcaseView.setShowcase(ShowcaseView.NONE);
        showcaseView.animateGesture(0, screenHeight/2, screenWidth/3, screenHeight/2, true);
        showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
            @Override
            public void onShowcaseViewShow(ShowcaseView showcaseView) { }

            @Override
            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                getActivity().setRequestedOrientation(prevOrientation);
            }
            @Override
            public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
        });
    }

}
