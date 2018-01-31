package it.cammino.risuscito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.SignInButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class Risuscito extends Fragment
    implements SimpleDialogFragment.SimpleCallback, EasyPermissions.PermissionCallbacks {

  public static final String BROADCAST_SIGNIN_VISIBLE =
      "it.cammino.risuscito.signin.SIGNIN_VISIBLE";
  public static final String DATA_VISIBLE = "it.cammino.risuscito.signin.data.DATA_VISIBLE";
  private static final String VERSION_KEY = "PREFS_VERSION_KEY";
  private static final String NO_VERSION = "";
  private final String TAG = getClass().getCanonicalName();

  @BindView(R.id.sign_in_button)
  @Nullable
  SignInButton mSignInButton;

  //  private WelcomeHelper mWelcomeScreen;
  private MainActivity mMainActivity;
  private Unbinder mUnbinder;
  private String thisVersion;
  private View rootView;
  private BroadcastReceiver signInVisibility =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          // Implement UI change code here once notification is received
          try {
            Log.d(getClass().getName(), "BROADCAST_SIGNIN_VISIBLE");
            Log.d(
                getClass().getName(),
                "DATA_VISIBLE: " + intent.getBooleanExtra(DATA_VISIBLE, false));
            if (mSignInButton != null)
              mSignInButton.setVisibility(
                  intent.getBooleanExtra(DATA_VISIBLE, false) ? View.VISIBLE : View.INVISIBLE);
          } catch (IllegalArgumentException e) {
            Log.e(getClass().getName(), e.getLocalizedMessage(), e);
          }
        }
      };

  @OnClick(R.id.imageView1)
  public void closeDrawer() {
    mMainActivity.getDrawer().openDrawer();
  }

  @OnClick(R.id.sign_in_button)
  public void signIn() {
    mMainActivity.setShowSnackbar();
    mMainActivity.signIn();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.activity_risuscito, container, false);
    mUnbinder = ButterKnife.bind(this, rootView);

    mMainActivity = (MainActivity) getActivity();

    mMainActivity.setupToolbarTitle(R.string.activity_homepage);
    mMainActivity.enableFab(false);
    if (!mMainActivity.isOnTablet()) {
      mMainActivity.enableBottombar(false);
    }
    mMainActivity.mTabLayout.setVisibility(View.GONE);

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

    // get version numbers
    String lastVersion = sp.getString(VERSION_KEY, NO_VERSION);
    //        String thisVersion;
    Log.d("Changelog", "lastVersion: " + lastVersion);
    try {
      thisVersion =
          getActivity()
              .getPackageManager()
              .getPackageInfo(getActivity().getPackageName(), 0)
              .versionName;
    } catch (NameNotFoundException e) {
      thisVersion = NO_VERSION;
      Log.d("Changelog", "could not get version name from manifest!");
      e.printStackTrace();
    }
    Log.d("Changelog", "thisVersion: " + thisVersion);

    //    mWelcomeScreen = new WelcomeHelper(getActivity(), IntroMainNew.class);
    //    mWelcomeScreen.show(savedInstanceState);
    if (!thisVersion.equals(lastVersion)) {
      new SimpleDialogFragment.Builder(
              (AppCompatActivity) getActivity(), Risuscito.this, "CHANGELOG")
          .title(R.string.dialog_change_title)
          .setCustomView(R.layout.dialog_changelogview)
          .positiveButton(android.R.string.ok)
          .setHasCancelListener()
          .setCanceable()
          .show();
    }

    //    PaginaRenderActivity.notaCambio = null;
    //    PaginaRenderActivity.speedValue = null;
    //    PaginaRenderActivity.scrollPlaying = false;
    //    PaginaRenderActivity.mostraAudio = null;

    // apertura e chiusura database per consentire eventuale aggiornamento
    //    DatabaseCanti listaCanti = new DatabaseCanti(getActivity());
    //    SQLiteDatabase db = listaCanti.getReadableDatabase();
    //    db.close();
    //    listaCanti.close();

    mSignInButton.setSize(SignInButton.SIZE_WIDE);

    Log.d(
        TAG,
        "onCreateView: signed in = "
            + PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SIGNED_IN, false));
    rootView
        .findViewById(R.id.sign_in_button)
        .setVisibility(
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getBoolean(Utility.SIGNED_IN, false)
                ? View.INVISIBLE
                : View.VISIBLE);

    checkStoragePermissions();

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
    getActivity().registerReceiver(signInVisibility, new IntentFilter(BROADCAST_SIGNIN_VISIBLE));
    SimpleDialogFragment fragment =
        SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "CHANGELOG");
    if (fragment != null) fragment.setmCallback(Risuscito.this);
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

  //  @Override
  //  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
  //    IconicsMenuInflaterUtil.inflate(
  //        getActivity().getMenuInflater(), getActivity(), R.menu.help_menu, menu);
  //    super.onCreateOptionsMenu(menu, inflater);
  //    getActivity().getMenuInflater().inflate(R.menu.help_menu, menu);
  //    menu.findItem(R.id.action_help)
  //        .setIcon(
  //            new IconicsDrawable(getActivity(), CommunityMaterial.Icon.cmd_help_circle)
  //                .sizeDp(24)
  //                .paddingDp(2)
  //                .color(Color.WHITE));
  //  }

  //  @Override
  //  public boolean onOptionsItemSelected(MenuItem item) {
  //    switch (item.getItemId()) {
  //      case R.id.action_help:
  //        mWelcomeScreen.forceShow();
  //        return true;
  //    }
  //    return false;
  //  }

  //  @Override
  //  public void onSaveInstanceState(@NonNull Bundle outState) {
  //    super.onSaveInstanceState(outState);
  //    mWelcomeScreen.onSaveInstanceState(outState);
  //  }

  @Override
  public void onPositive(@NonNull String tag) {
    Log.d(TAG, "onPositive: " + tag);
    switch (tag) {
      case "CHANGELOG":
        SharedPreferences.Editor editor =
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString(VERSION_KEY, thisVersion);
        editor.apply();
        break;
    }
  }

  @Override
  public void onNegative(@NonNull String tag) {}

  @Override
  public void onNeutral(@NonNull String tag) {}

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    // Forward results to EasyPermissions
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @AfterPermissionGranted(Utility.WRITE_STORAGE_RC)
  private void checkStoragePermissions() {
    Log.d(TAG, "checkStoragePermissions: ");
    if (!EasyPermissions.hasPermissions(
        getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      // Ask for one permission
      //      EasyPermissions.requestPermissions(
      //          Risuscito.this,
      //          getString(R.string.external_storage_pref_rationale),
      //          Utility.WRITE_STORAGE_RC,
      //          android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
      EasyPermissions.requestPermissions(
          new PermissionRequest.Builder(
                  this,
                  Utility.WRITE_STORAGE_RC,
                  android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
              .setRationale(R.string.external_storage_pref_rationale)
              .build());
    }
  }

  @Override
  public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
    // Some permissions have been
    Log.d(TAG, "onPermissionsGranted: ");
    Snackbar.make(rootView, getString(R.string.permission_ok), Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void onPermissionsDenied(int requestCode, @NonNull List<String> list) {
    // Some permissions have been denied
    Log.d(TAG, "onPermissionsDenied: ");
    SharedPreferences.Editor editor =
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
    editor.putString(Utility.SAVE_LOCATION, "0");
    editor.apply();
    Snackbar.make(rootView, getString(R.string.external_storage_denied), Snackbar.LENGTH_SHORT)
        .show();
  }
}
