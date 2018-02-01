package it.cammino.risuscito;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

import pub.devrel.easypermissions.EasyPermissions;

public class SettingsFragment extends PreferenceFragmentCompatDividers {

  private final String TAG = getClass().getCanonicalName();
  CharSequence[] mEntries;
  CharSequence[] mEntryValues;
  MainActivity mMainActivity;

  @Override
  public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    mMainActivity = (MainActivity) getActivity();
    if (mMainActivity != null) mMainActivity.setupToolbarTitle(R.string.title_activity_settings);

    mMainActivity.mTabLayout.setVisibility(View.GONE);
    mMainActivity.enableFab(false);
    if (!mMainActivity.isOnTablet()) mMainActivity.enableBottombar(false);

    final ListPreference listPreference =
        (ListPreference) findPreference("memoria_salvataggio_scelta");

    loadStorageList(
        EasyPermissions.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE));

    listPreference.setOnPreferenceClickListener(
        new Preference.OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            listPreference.setEntries(mEntries);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String saveLocation = pref.getString(Utility.SAVE_LOCATION, "0");
            listPreference.setDefaultValue(saveLocation);
            listPreference.setEntryValues(mEntryValues);
            return false;
          }
        });

    final SwitchPreferenceCompat darkTheme = (SwitchPreferenceCompat) findPreference("dark_mode");
    darkTheme.setOnPreferenceChangeListener(
        new Preference.OnPreferenceChangeListener() {
          @Override
          public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (mMainActivity != null) mMainActivity.recreate();
            return true;
          }
        });

    return super.onCreateView(inflater, container, savedInstanceState);
  }

  void loadStorageList(boolean external) {
    Log.d(
        TAG,
        "loadStorageList: WRITE_EXTERNAL_STORAGE "
            + Utility.isExternalStorageWritable()
            + " / "
            + external);
    if (Utility.isExternalStorageWritable() && external) {
      mEntries = getResources().getStringArray(R.array.save_location_sd_entries);
      mEntryValues = getResources().getStringArray(R.array.save_location_sd_values);
    } else {
      mEntries = getResources().getStringArray(R.array.save_location_nosd_entries);
      mEntryValues = getResources().getStringArray(R.array.save_location_nosd_values);
    }
  }
}
