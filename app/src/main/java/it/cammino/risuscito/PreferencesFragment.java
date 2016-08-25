package it.cammino.risuscito;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.cammino.risuscito.dialogs.SimpleDialogFragment;
import it.cammino.risuscito.dialogs.SingleChoiceDialogFragment;
import it.cammino.risuscito.utils.ColorPalette;
import it.cammino.risuscito.utils.ThemeUtils;

public class PreferencesFragment extends Fragment implements SingleChoiceDialogFragment.SingleChoiceCallback, SimpleDialogFragment.SimpleCallback {

    //    private int prevOrientation;
//    private SwitchCompat screenSwitch, secondaSwitch, paceSwitch, santoSwitch, audioSwitch;
    private int saveEntries;

    private MainActivity mMainActivity;

    @BindView(R.id.screen_on) SwitchCompat screenSwitch;
    @BindView(R.id.show_seconda_eucarestia) SwitchCompat secondaSwitch;
    @BindView(R.id.show_pace_parola) SwitchCompat paceSwitch;
    @BindView(R.id.show_santo) SwitchCompat santoSwitch;
    @BindView(R.id.show_audio) SwitchCompat audioSwitch;
    @BindView(R.id.primaryCircle) View mPrimaryCircle;
    @BindView(R.id.accentCircle) View mAccentCircle;

    @OnClick(R.id.screen_on_layout)
    public void switchScreeon() {
        if (screenSwitch.isChecked())
            screenSwitch.setChecked(false);
        else
            screenSwitch.setChecked(true);
        mMainActivity.checkScreenAwake();
    }

    @OnClick(R.id.show_seconda_eucarestia_layout)
    public void switchSecondaCheked() {
        secondaSwitch.setChecked(!secondaSwitch.isChecked());
    }

    @OnClick(R.id.show_santo_layout)
    public void switchSantoCheked() {
        santoSwitch.setChecked(!santoSwitch.isChecked());
    }

    @OnClick(R.id.show_pace_parola_layout)
    public void switchSPaceParolaCheked() {
        paceSwitch.setChecked(!paceSwitch.isChecked());
    }

    @OnClick(R.id.save_location_layout)
    public void showSaveLocation() {
        int checkedItem = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getInt(Utility.SAVE_LOCATION, 0);
        new SingleChoiceDialogFragment.Builder((AppCompatActivity)getActivity(), PreferencesFragment.this, "SAVE_LOCATION")
                .title(R.string.save_location_title)
                .items(saveEntries)
                .defaultIndex(checkedItem)
                .negativeButton(R.string.cancel)
                .show();
    }

    @OnClick(R.id.primary_color_selection)
    public void showPrimarySelection() {
        new ColorChooserDialog.Builder(mMainActivity, R.string.primary_color)
                .allowUserColorInput(false)
                .customColors(ColorPalette.PRIMARY_COLORS, ColorPalette.PRIMARY_COLORS_SUB)
                .doneButton(R.string.single_choice_ok)  // changes label of the done button
                .cancelButton(R.string.cancel)  // changes label of the cancel button
                .backButton(R.string.dialog_back)  // changes label of the back button
                .preselect(getThemeUtils().primaryColor())  // optional color int, preselects a color
                .show();
    }

    @OnClick(R.id.accent_color_selection)
    public void showAccentSelection() {
        new ColorChooserDialog.Builder(mMainActivity, R.string.accent_color)
                .allowUserColorInput(false)
                .customColors(ColorPalette.ACCENT_COLORS, ColorPalette.ACCENT_COLORS_SUB)
                .accentMode(true)  // optional boolean, true shows accent palette
                .doneButton(R.string.single_choice_ok)  // changes label of the done button
                .cancelButton(R.string.cancel)  // changes label of the cancel button
                .backButton(R.string.dialog_back)  // changes label of the back button
                .preselect(getThemeUtils().accentColor())  // optional color int, preselects a color
                .show();
    }

    @OnClick(R.id.show_audio_layout)
    public void switchAudioCheked() {
        audioSwitch.setChecked(!audioSwitch.isChecked());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.preference_screen, container, false);
        ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();
        mMainActivity.setupToolbarTitle(R.string.title_activity_settings);

//        getActivity().findViewById(R.id.material_tabs).setVisibility(View.GONE);
        mMainActivity.mTabLayout.setVisibility(View.GONE);
        if (!mMainActivity.isOnTablet()) {
            mMainActivity.enableFab(false);
            mMainActivity.enableBottombar(false);
        }

        screenSwitch = (SwitchCompat) rootView.findViewById(R.id.screen_on);

        // controllo l'attuale impostazione di always on
        screenSwitch.setChecked(PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SCREEN_ON, false));

        screenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit();
                editor.putBoolean(Utility.SCREEN_ON, isChecked);
                editor.apply();
            }
        });

//        rootView.findViewById(R.id.screen_on_layout).setOnClickListener(new OnClickListener() {
//            @SuppressLint("NewApi")
//            @Override
//            public void onClick(View v) {
//                if (screenSwitch.isChecked())
//                    screenSwitch.setChecked(false);
//                else
//                    screenSwitch.setChecked(true);
//
//                mMainActivity.checkScreenAwake();
//            }
//        });


//        secondaSwitch = (SwitchCompat) rootView.findViewById(R.id.show_seconda_eucarestia);

        // controllo l'attuale impostazione della visualizzazione seconda lettura
        secondaSwitch.setChecked(PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SHOW_SECONDA, false));

        secondaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit();
                editor.putBoolean(Utility.SHOW_SECONDA, isChecked);
                editor.apply();
            }
        });

//        rootView.findViewById(R.id.show_seconda_eucarestia_layout).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                secondaSwitch.setChecked(!secondaSwitch.isChecked());
//            }
//        });

//        santoSwitch = (SwitchCompat) rootView.findViewById(R.id.show_santo);

        // controllo l'attuale impostazione della visualizzazione seconda lettura
        santoSwitch.setChecked(PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SHOW_SANTO, false));

        santoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit();
                editor.putBoolean(Utility.SHOW_SANTO, isChecked);
                editor.apply();
            }
        });

//        rootView.findViewById(R.id.show_santo_layout).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                santoSwitch.setChecked(!santoSwitch.isChecked());
//            }
//        });


//        paceSwitch = (SwitchCompat) rootView.findViewById(R.id.show_pace_parola);

        // controllo l'attuale impostazione della visualizzazione canto alla pace
        paceSwitch.setChecked(PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SHOW_PACE, false));

        paceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit();
                editor.putBoolean(Utility.SHOW_PACE, isChecked);
                editor.apply();
            }
        });

//        rootView.findViewById(R.id.show_pace_parola_layout).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                paceSwitch.setChecked(!paceSwitch.isChecked());
//            }
//        });

        View defaultIndexView = rootView.findViewById(R.id.default_index_layout);
        defaultIndexView.setOnClickListener(new OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                int checkedItem = PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getInt(Utility.DEFAULT_INDEX, 0);
                new SingleChoiceDialogFragment.Builder((AppCompatActivity)getActivity(), PreferencesFragment.this, "DEFAULT_INDEX")
                        .title(R.string.default_index_title)
                        .items(R.array.pref_default_index_entries)
                        .defaultIndex(checkedItem)
                        .negativeButton(R.string.cancel)
                        .show();
            }
        });

//        View saveLocationView = rootView.findViewById(R.id.save_location_layout);

        if (Utility.hasMarshmallow())
            checkStoragePermissions();
        else
            loadExternalStorage();

//        saveLocationView.setOnClickListener(new OnClickListener() {
//
//            @SuppressLint("NewApi")
//            @Override
//            public void onClick(View v) {
//                int checkedItem = PreferenceManager.getDefaultSharedPreferences(getActivity())
//                        .getInt(Utility.SAVE_LOCATION, 0);
//                new SingleChoiceDialogFragment.Builder((AppCompatActivity)getActivity(), PreferencesFragment.this, "SAVE_LOCATION")
//                        .title(R.string.save_location_title)
//                        .items(saveEntries)
//                        .defaultIndex(checkedItem)
//                        .negativeButton(R.string.cancel)
//                        .show();
//            }
//        });

//        setColorViewValue(rootView.findViewById(R.id.primaryCircle), getThemeUtils().primaryColor());
        setColorViewValue(mPrimaryCircle, getThemeUtils().primaryColor());
//        rootView.findViewById(R.id.primary_color_selection).setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                new ColorChooserDialog.Builder(mMainActivity, R.string.primary_color)
//                        .allowUserColorInput(false)
//                        .customColors(ColorPalette.PRIMARY_COLORS, ColorPalette.PRIMARY_COLORS_SUB)
//                        .doneButton(R.string.single_choice_ok)  // changes label of the done button
//                        .cancelButton(R.string.cancel)  // changes label of the cancel button
//                        .backButton(R.string.dialog_back)  // changes label of the back button
//                        .preselect(getThemeUtils().primaryColor())  // optional color int, preselects a color
//                        .show();
//            }
//        });

//        setColorViewValue(rootView.findViewById(R.id.accentCircle), getThemeUtils().accentColor());
        setColorViewValue(mAccentCircle, getThemeUtils().accentColor());
//        rootView.findViewById(R.id.accent_color_selection).setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                new ColorChooserDialog.Builder(mMainActivity, R.string.accent_color)
//                        .allowUserColorInput(false)
//                        .customColors(ColorPalette.ACCENT_COLORS, ColorPalette.ACCENT_COLORS_SUB)
//                        .accentMode(true)  // optional boolean, true shows accent palette
//                        .doneButton(R.string.single_choice_ok)  // changes label of the done button
//                        .cancelButton(R.string.cancel)  // changes label of the cancel button
//                        .backButton(R.string.dialog_back)  // changes label of the back button
//                        .preselect(getThemeUtils().accentColor())  // optional color int, preselects a color
//                        .show();
//            }
//        });

//        audioSwitch = (SwitchCompat) rootView.findViewById(R.id.show_audio);

        // controllo l'attuale impostazione di always on
        audioSwitch.setChecked(PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SHOW_AUDIO, true));

        audioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit();
                editor.putBoolean(Utility.SHOW_AUDIO, isChecked);
                editor.apply();
            }
        });

//        rootView.findViewById(R.id.show_audio_layout).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                audioSwitch.setChecked(!audioSwitch.isChecked());
//            }
//        });

        rootView.findViewById(R.id.language_selection).setOnClickListener(new OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                int checkedItem = 0;
                if (getActivity().getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
                    checkedItem = 1;
                new SingleChoiceDialogFragment.Builder((AppCompatActivity)getActivity(), PreferencesFragment.this, "LANGUAGE")
                        .title(R.string.language_title)
                        .items(R.array.pref_languages)
                        .defaultIndex(checkedItem)
                        .negativeButton(R.string.cancel)
                        .show();
            }
        });

        if (SingleChoiceDialogFragment.findVisible((AppCompatActivity) getActivity(), "DEFAULT_INDEX") != null)
            SingleChoiceDialogFragment.findVisible((AppCompatActivity) getActivity(), "DEFAULT_INDEX").setmCallback(PreferencesFragment.this);
        if (SingleChoiceDialogFragment.findVisible((AppCompatActivity) getActivity(), "SAVE_LOCATION") != null)
            SingleChoiceDialogFragment.findVisible((AppCompatActivity) getActivity(), "SAVE_LOCATION").setmCallback(PreferencesFragment.this);
        if (SingleChoiceDialogFragment.findVisible((AppCompatActivity) getActivity(), "LANGUAGE") != null)
            SingleChoiceDialogFragment.findVisible((AppCompatActivity) getActivity(), "LANGUAGE").setmCallback(PreferencesFragment.this);
        if (SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "EXTERNAL_RATIONALE") != null)
            SimpleDialogFragment.findVisible((AppCompatActivity) getActivity(), "EXTERNAL_RATIONALE").setmCallback(PreferencesFragment.this);

        return rootView;
    }

    private static void setColorViewValue(View view, int color) {
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            Resources res = imageView.getContext().getResources();

            Drawable currentDrawable = imageView.getDrawable();
            GradientDrawable colorChoiceDrawable;
            if (currentDrawable != null && currentDrawable instanceof GradientDrawable) {
                // Reuse drawable
                colorChoiceDrawable = (GradientDrawable) currentDrawable;
            } else {
                colorChoiceDrawable = new GradientDrawable();
                colorChoiceDrawable.setShape(GradientDrawable.OVAL);
            }

            // Set stroke to dark version of color
            int darkenedColor = Color.rgb(
                    Color.red(color) * 192 / 256,
                    Color.green(color) * 192 / 256,
                    Color.blue(color) * 192 / 256);

            colorChoiceDrawable.setColor(color);
            colorChoiceDrawable.setStroke((int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics()), darkenedColor);
            imageView.setImageDrawable(colorChoiceDrawable);

        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
    }

    private ThemeUtils getThemeUtils() {
        return mMainActivity.getThemeUtils();
    }

    private void checkStoragePermissions() {
        // Here, thisActivity is the current activity
        if(ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showRationaleForExternalDownload();
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Utility.WRITE_STORAGE_RC);
            }
        }
        else
            loadExternalStorage();
    }

    void loadExternalStorage() {
        Log.d(getClass().getName(), "WRITE_EXTERNAL_STORAGE OK");
        if (Utility.isExternalStorageWritable()) {
            saveEntries = R.array.save_location_sd_entries;
        } else {
            saveEntries = R.array.save_location_nosd_entries;
        }
    }

    void showRationaleForExternalDownload() {
        Log.d(getClass().getName(), "WRITE_EXTERNAL_STORAGE RATIONALE");
        new SimpleDialogFragment.Builder((AppCompatActivity)getActivity(), PreferencesFragment.this, "EXTERNAL_RATIONALE")
                .title(R.string.external_storage_title)
                .content(R.string.external_storage_pref_rationale)
                .positiveButton(R.string.dialog_chiudi)
                .show()
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                Utility.WRITE_STORAGE_RC);
                    }
                });
    }

    void showDeniedForExternalDownload() {
        Log.d(getClass().getName(), "WRITE_EXTERNAL_STORAGE DENIED");
        saveEntries = R.array.save_location_nosd_entries;
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
    public boolean onSelection(@NonNull String tag, @NonNull AppCompatActivity context, MaterialDialog dialog, View view, int which, CharSequence text) {
        Log.d(getClass().getName(), "onSelection: " + tag);
        switch (tag) {
            case "DEFAULT_INDEX":
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .edit();
                editor.putInt(Utility.DEFAULT_INDEX, which);
                editor.apply();
                break;
            case "SAVE_LOCATION":
                editor = PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .edit();
                editor.putInt(Utility.SAVE_LOCATION, which);
                editor.apply();
                break;
            case "LANGUAGE":
                editor = PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .edit();
                switch (which) {
                    case 0:
                        editor.putString(Utility.SYSTEM_LANGUAGE, "it");
                        break;
                    case 1:
                        editor.putString(Utility.SYSTEM_LANGUAGE, "uk");
                        break;
                }
                editor.apply();
                Intent i = context.getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(context.getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra(Utility.DB_RESET, true);
                String currentLang = "it";
                if (context.getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
                    currentLang = "uk";
                i.putExtra(Utility.CHANGE_LANGUAGE,
                        currentLang + "-" + PreferenceManager.getDefaultSharedPreferences(context)
                                .getString(Utility.SYSTEM_LANGUAGE, ""));
                context.startActivity(i);
                break;
        }
        return true;
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
