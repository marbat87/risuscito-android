package it.cammino.risuscito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import android.widget.ImageView;

import com.google.android.gms.common.SignInButton;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.stephentuso.welcome.WelcomeHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.slides.IntroMainNew;

public class Risuscito extends Fragment implements SimpleDialogFragment.SimpleCallback {

    private final String TAG = getClass().getCanonicalName();

    private static final String VERSION_KEY = "PREFS_VERSION_KEY";
    private static final String NO_VERSION = "";
    public static final String BROADCAST_SIGNIN_VISIBLE = "it.cammino.risuscito.signin.SIGNIN_VISIBLE";
    public static final String DATA_VISIBLE = "it.cammino.risuscito.signin.data.DATA_VISIBLE";
    private WelcomeHelper mWelcomeScreen;

//    private SignInButton mSignInButton;

    private MainActivity mMainActivity;

    private Unbinder mUnbinder;

    private String thisVersion;

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

    @BindView(R.id.imageView1) ImageView mCover;
    @BindView(R.id.sign_in_button) SignInButton mSignInButton;
    @OnClick(R.id.imageView1)
    public void closeDrawer() {
        mMainActivity.getDrawer().openDrawer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_risuscito, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();

        mMainActivity.setupToolbarTitle(R.string.activity_homepage);
        mMainActivity.enableFab(false);
        if (!mMainActivity.isOnTablet()) {
            mMainActivity.enableBottombar(false);
        }
        mMainActivity.mTabLayout.setVisibility(View.GONE);

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        // get version numbers
        String lastVersion = sp.getString(VERSION_KEY, NO_VERSION);
//        String thisVersion;
        Log.d("Changelog", "lastVersion: " + lastVersion);
        try {
            thisVersion = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            thisVersion = NO_VERSION;
            Log.d("Changelog", "could not get version name from manifest!");
            e.printStackTrace();
        }
        Log.d("Changelog", "thisVersion: " + thisVersion);

        mWelcomeScreen = new WelcomeHelper(getActivity(), IntroMainNew.class);
        mWelcomeScreen.show(savedInstanceState);
        if (!thisVersion.equals(lastVersion)) {
//            mWelcomeScreen = new WelcomeHelper(getActivity(), IntroMainNew.class);
//            mWelcomeScreen.show(savedInstanceState);
            new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), Risuscito.this, "CHANGELOG")
                    .title(R.string.dialog_change_title)
                    .setCustomView(R.layout.dialog_changelogview)
                    .positiveButton(R.string.dialog_chiudi)
                    .setHasCancelListener()
                    .setCanceable()
                    .show();
//            SharedPreferences.Editor editor = sp.edit();
//            editor.putString(VERSION_KEY, thisVersion);
//            editor.apply();
        }
//        else {
//            mWelcomeScreen = new WelcomeHelper(getActivity(), IntroMainNew.class);
//            mWelcomeScreen.show(savedInstanceState);
//        }

        PaginaRenderActivity.notaCambio = null;
        PaginaRenderActivity.speedValue = null;
        PaginaRenderActivity.scrollPlaying = false;
        PaginaRenderActivity.mostraAudio = null;

        //apertura e chiusura database per consentire eventuale aggiornamento
        DatabaseCanti listaCanti = new DatabaseCanti(getActivity());
        SQLiteDatabase db = listaCanti.getReadableDatabase();
        db.close();
        listaCanti.close();

//        mSignInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        mSignInButton.setSize(SignInButton.SIZE_WIDE);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainActivity.setShowSnackbar();
                mMainActivity.signIn();
            }
        });

        Log.d(TAG, "onCreateView: signed in = " + PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SIGNED_IN, false));
        rootView.findViewById(R.id.sign_in_button).setVisibility(PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SIGNED_IN, false) ? View.INVISIBLE : View.VISIBLE);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(signInVisibility, new IntentFilter(
                BROADCAST_SIGNIN_VISIBLE));
        SimpleDialogFragment fragment = SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "CHANGELOG");
        if (fragment != null)
            fragment.setmCallback(Risuscito.this);
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
        menu.findItem(R.id.action_help).setIcon(
                new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_help_circle)
                        .sizeDp(24)
                        .paddingDp(2)
                        .color(Color.WHITE));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
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

    @Override
    public void onPositive(@NonNull String tag) {
        Log.d(TAG, "onPositive: " + tag);
        switch (tag) {
            case "CHANGELOG":
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity()).edit();
                editor.putString(VERSION_KEY, thisVersion);
                editor.apply();
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}

}
