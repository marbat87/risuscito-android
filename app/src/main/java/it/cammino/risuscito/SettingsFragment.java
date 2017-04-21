package it.cammino.risuscito;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import it.cammino.risuscito.dialogs.SimpleDialogFragment;

public class SettingsFragment extends PreferenceFragmentCompat implements SimpleDialogFragment.SimpleCallback {

//    private MainActivity mMainActivity;
    CharSequence[] mEntries;
    CharSequence[] mEntryValues;

//    @Override
//    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//        // Load the preferences from an XML resource
//        setPreferencesFromResource(R.xml.preferences, rootKey);
//
//    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        MainActivity mMainActivity = (MainActivity) getActivity();
        mMainActivity.setupToolbarTitle(R.string.title_activity_settings);

        mMainActivity.mTabLayout.setVisibility(View.GONE);
        if (!mMainActivity.isOnTablet()) {
            mMainActivity.enableFab(false);
            mMainActivity.enableBottombar(false);
        }

        final ListPreference listPreference = (ListPreference) findPreference("memoria_salvataggio_scelta");

        listPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                listPreference.setEntries(mEntries);
                SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
                String saveLocation = pref.getString(Utility.SAVE_LOCATION, "0");
                listPreference.setDefaultValue(saveLocation);
                listPreference.setEntryValues(mEntryValues);
                return false;
            }
        });

        if (Utility.hasMarshmallow())
            checkStoragePermissions();
        else
            loadExternalStorage();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void checkStoragePermissions() {
        // Here, thisActivity is the current activity
        if(ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showRationaleForExternalDownload();
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Utility.WRITE_STORAGE_RC);
            }
        }
        else
            loadExternalStorage();
    }

    void loadExternalStorage() {
        Log.d(getClass().getName(), "WRITE_EXTERNAL_STORAGE OK");
        if (Utility.isExternalStorageWritable()) {
            mEntries = getResources().getStringArray(R.array.save_location_sd_entries);
            mEntryValues = getResources().getStringArray(R.array.save_location_sd_values);
        } else {
            mEntries = getResources().getStringArray(R.array.save_location_nosd_entries);
            mEntryValues = getResources().getStringArray(R.array.save_location_nosd_values);
        }
    }

    void showRationaleForExternalDownload() {
        Log.d(getClass().getName(), "WRITE_EXTERNAL_STORAGE RATIONALE");
        new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), SettingsFragment.this, "EXTERNAL_RATIONALE")
                .title(R.string.external_storage_title)
                .content(R.string.external_storage_pref_rationale)
                .positiveButton(R.string.dialog_chiudi)
                .setCanceable(true)
                .cancelListener(true)
                .show();
    }

    void showDeniedForExternalDownload() {
        Log.d(getClass().getName(), "WRITE_EXTERNAL_STORAGE DENIED");
//        saveEntries = R.array.save_location_nosd_entries;
        mEntries = getResources().getStringArray(R.array.save_location_nosd_entries);
        mEntryValues = getResources().getStringArray(R.array.save_location_nosd_values);
        Snackbar.make(getActivity().findViewById(android.R.id.content)
                , getString(R.string.external_storage_denied)
                , Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d(getClass().getName(), "onRequestPermissionsResult-request: " + requestCode);
        Log.d(getClass().getName(), "onRequestPermissionsResult-result: " + grantResults[0]);
        switch (requestCode) {
            case Utility.WRITE_STORAGE_RC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                    loadExternalStorage();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showDeniedForExternalDownload();
                }
            }
        }
    }

    @Override
    public void onPositive(@NonNull String tag) {
        Log.d(getClass().getName(), "onPositive: " + tag);
        switch (tag) {
            case "EXTERNAL_RATIONALE":
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Utility.WRITE_STORAGE_RC);
                break;
        }
    }
    @Override
    public void onNegative(@NonNull String tag) {}
    @Override
    public void onNeutral(@NonNull String tag) {}
}
