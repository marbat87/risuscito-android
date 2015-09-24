package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import it.cammino.risuscito.utils.ColorChooserDialog;
import it.cammino.risuscito.utils.ThemeUtils;

public class PreferencesFragment extends Fragment {

    private int prevOrientation;
    private SwitchCompat screenSwitch, secondaSwitch, paceSwitch, santoSwitch, audioSwitch;
    private int saveEntries;
//    private AlertDialogPro languageDialog;
//    private AlertDialogPro defIndexDialog;
//    private AlertDialogPro defMemoryDialog;

//    private int checkedItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.preference_screen, container, false);
//        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_settings);
//        ((TextView)((MainActivity) getActivity()).findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_settings);
//        ((MainActivity) getActivity()).getSupportActionBar()
//                .setElevation(dpToPx(getResources().getInteger(R.integer.toolbar_elevation)));
        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.title_activity_settings);

        screenSwitch = (SwitchCompat) rootView.findViewById(R.id.screen_on);

        // controllo l'attuale impostazione di always on
//        if (PreferenceManager
//                .getDefaultSharedPreferences(getActivity())
//                .getBoolean(Utility.SCREEN_ON, false))
//            screenSwitch.setChecked(true);
//        else
//            screenSwitch.setChecked(false);
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
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    editor.commit();
                } else {
                    editor.apply();
                }
            }
        });

        rootView.findViewById(R.id.screen_on_layout).setOnClickListener(new OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (screenSwitch.isChecked())
                    screenSwitch.setChecked(false);
                else
                    screenSwitch.setChecked(true);

                ((MainActivity) getActivity()).checkScreenAwake();
            }
        });


        secondaSwitch = (SwitchCompat) rootView.findViewById(R.id.show_seconda_eucarestia);

        // controllo l'attuale impostazione della visualizzazione seconda lettura
//        if (PreferenceManager
//                .getDefaultSharedPreferences(getActivity())
//                .getBoolean(Utility.SHOW_SECONDA, false))
//            secondaSwitch.setChecked(true);
//        else
//            secondaSwitch.setChecked(false);
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
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    editor.commit();
                } else {
                    editor.apply();
                }
            }
        });

        rootView.findViewById(R.id.show_seconda_eucarestia_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                secondaSwitch.setChecked(!secondaSwitch.isChecked());
            }
        });

        santoSwitch = (SwitchCompat) rootView.findViewById(R.id.show_santo);

        // controllo l'attuale impostazione della visualizzazione seconda lettura
//        if (PreferenceManager
//                .getDefaultSharedPreferences(getActivity())
//                .getBoolean(Utility.SHOW_SANTO, false))
//            santoSwitch.setChecked(true);
//        else
//            santoSwitch.setChecked(false);
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
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    editor.commit();
                } else {
                    editor.apply();
                }
            }
        });

        rootView.findViewById(R.id.show_santo_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                santoSwitch.setChecked(!santoSwitch.isChecked());
            }
        });


        paceSwitch = (SwitchCompat) rootView.findViewById(R.id.show_pace_parola);

        // controllo l'attuale impostazione della visualizzazione canto alla pace
//        if (PreferenceManager
//                .getDefaultSharedPreferences(getActivity())
//                .getBoolean(Utility.SHOW_PACE, false))
//            paceSwitch.setChecked(true);
//        else
//            paceSwitch.setChecked(false);
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
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    editor.commit();
                } else {
                    editor.apply();
                }
            }
        });

        rootView.findViewById(R.id.show_pace_parola_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                paceSwitch.setChecked(!paceSwitch.isChecked());
            }
        });

        View defaultIndexView = rootView.findViewById(R.id.default_index_layout);
        defaultIndexView.setOnClickListener(new OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                int checkedItem = PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getInt(Utility.DEFAULT_INDEX, 0);
//                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
//                defIndexDialog = builder.setTitle(R.string.default_index_title)
//                        .setSingleChoiceItems(getResources().getStringArray(R.array.pref_default_index_entries),
//                                checkedItem,
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        checkedItem = which;
//                                        defIndexDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
//                                        defIndexDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
//                                                getThemeUtils().accentColor());
//                                    }
//                                })
//                        .setNegativeButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
//                        .setPositiveButton(R.string.single_choice_ok, new ButtonClickedListener(Utility.PREFERENCE_DEFINDEX_OK))
//                        .show();
                MaterialDialog defIndexDialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.default_index_title)
                        .items(R.array.pref_default_index_entries)
                        .itemsCallbackSingleChoice(checkedItem, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(getActivity())
                                        .edit();
                                editor.putInt(Utility.DEFAULT_INDEX, which);
                                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                                    editor.commit();
                                } else {
                                    editor.apply();
                                }
                                getActivity().setRequestedOrientation(prevOrientation);
                                return true;
                            }
                        })
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                getActivity().setRequestedOrientation(prevOrientation);
                            }
                        })
                        .show();
                defIndexDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getAction() == KeyEvent.ACTION_UP) {
                            arg0.dismiss();
                            getActivity().setRequestedOrientation(prevOrientation);
                            return true;
                        }
                        return false;
                    }
                });
                defIndexDialog.setCancelable(false);
//                defIndexDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
//                defIndexDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
//                        getResources().getColor(R.color.btn_disabled_text));
            }
        });

        View saveLocationView = rootView.findViewById(R.id.save_location_layout);

        if (Utility.isExternalStorageWritable()) {
            saveEntries = R.array.save_location_sd_entries;
        }
        else {
            saveEntries = R.array.save_location_nosd_entries;
        }

        saveLocationView.setOnClickListener(new OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                int checkedItem = PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getInt(Utility.SAVE_LOCATION, 0);
//                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
//                defMemoryDialog = builder.setTitle(R.string.save_location_title)
//                        .setSingleChoiceItems(getResources().getStringArray(saveEntries),
//                                checkedItem,
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        checkedItem = which;
//                                        defMemoryDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
//                                        defMemoryDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
//                                                getThemeUtils().accentColor());
//                                    }
//                                })
//                        .setNegativeButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
//                        .setPositiveButton(R.string.single_choice_ok, new ButtonClickedListener(Utility.PREFERENCE_SAVELOC_OK))
//                        .show();
                MaterialDialog defMemoryDialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.save_location_title)
                        .items(saveEntries)
                        .itemsCallbackSingleChoice(checkedItem, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(getActivity())
                                        .edit();
                                editor.putInt(Utility.SAVE_LOCATION, which);
                                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                                    editor.commit();
                                } else {
                                    editor.apply();
                                }
                                getActivity().setRequestedOrientation(prevOrientation);
                                return true;
                            }
                        })
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                getActivity().setRequestedOrientation(prevOrientation);
                            }
                        })
                        .show();
                defMemoryDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getAction() == KeyEvent.ACTION_UP) {
                            arg0.dismiss();
                            getActivity().setRequestedOrientation(prevOrientation);
                            return true;
                        }
                        return false;
                    }
                });
                defMemoryDialog.setCancelable(false);
//                defMemoryDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
//                defMemoryDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
//                        getResources().getColor(R.color.btn_disabled_text));
            }
        });

        setColorViewValue(rootView.findViewById(R.id.primaryCircle), getThemeUtils().primaryColor());
        rootView.findViewById(R.id.primary_color_selection).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                ColorChooserDialog colorChooser = ColorChooserDialog.newInstance(
//                        R.string.primary_color,
//                        ColorPalette.PRIMARY_COLORS,
//                        getThemeUtils().primaryColor(),
//                        4,
//                        ColorPickerDialog.SIZE_SMALL);
                //il SIZE_SMALL è ininfluente perchè in realtà va in base alla dimensione ed è automatico
//                colorChooser.show(getFragmentManager(),"primaryCC");
                new ColorChooserDialog.Builder((MainActivity) getActivity(), R.string.primary_color)
                        .preselect(getThemeUtils().primaryColor())  // optional color int, preselects a color
//                        .doneButton(R.string.single_choice_ok)  // optional string, changes done button label
//                        .cancelButton(R.string.cancel)  // optional string, changes cancel button label
//                        .backButton(R.string.dialog_back)  // optional string, changes back button label
                        .show();
            }
        });

        setColorViewValue(rootView.findViewById(R.id.accentCircle), getThemeUtils().accentColor());
        rootView.findViewById(R.id.accent_color_selection).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                ColorChooserDialog colorChooser = ColorChooserDialog.newInstance(
//                        R.string.accent_color,
//                        getIntArray(R.array.colors_accent),
//                        getThemeUtils().accentColor(),
//                        4,
//                        ColorPickerDialog.SIZE_SMALL);
                //il SIZE_SMALL è ininfluente perchè in realtà va in base alla dimensione ed è automatico
//                colorChooser.show(getFragmentManager(),"primaryCC");
                new ColorChooserDialog.Builder((MainActivity) getActivity(), R.string.accent_color)
                        .accentMode(true)  // optional boolean, true shows accent palette
                        .preselect(getThemeUtils().accentColor())  // optional color int, preselects a color
                        .doneButton(R.string.single_choice_ok)  // optional string, changes done button label
                        .cancelButton(R.string.cancel)  // optional string, changes cancel button label
                        .backButton(R.string.dialog_back)  // optional string, changes back button label
                        .show();
            }
        });

        audioSwitch = (SwitchCompat) rootView.findViewById(R.id.show_audio);

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
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    editor.commit();
                } else {
                    editor.apply();
                }
            }
        });

        rootView.findViewById(R.id.show_audio_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                audioSwitch.setChecked(!audioSwitch.isChecked());
            }
        });

        rootView.findViewById(R.id.language_selection).setOnClickListener(new OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                int checkedItem = 0;
                if (getActivity().getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
                    checkedItem = 1;

//                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
//                languageDialog = builder.setTitle(R.string.language_title)
//                        .setSingleChoiceItems(getResources().getStringArray(R.array.pref_languages),
//                                checkedItem,
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        checkedItem = which;
//                                        languageDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
//                                        languageDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
//                                                getThemeUtils().accentColor());
//                                    }
//                                })
//                        .setNegativeButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
//                        .setPositiveButton(R.string.single_choice_ok, new ButtonClickedListener(Utility.PREFERENCE_LANGUAGE_OK))
//                        .show();
                MaterialDialog languageDialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.language_title)
                        .items(R.array.pref_languages)
                        .itemsCallbackSingleChoice(checkedItem, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(getActivity())
                                        .edit();
                                switch (which) {
                                    case 0:
                                        editor.putString(Utility.SYSTEM_LANGUAGE, "it");
                                        break;
                                    case 1:
                                        editor.putString(Utility.SYSTEM_LANGUAGE, "uk");
                                        break;
                                }
                                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                                    editor.commit();
                                } else {
                                    editor.apply();
                                }
                                getActivity().setRequestedOrientation(prevOrientation);
                                Intent i = getActivity().getBaseContext().getPackageManager()
                                        .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                i.putExtra(Utility.DB_RESET, true);
                                String currentLang = "it";
                                if (getActivity().getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
                                    currentLang = "uk";
                                i.putExtra(Utility.CHANGE_LANGUAGE,
                                        currentLang + "-" + PreferenceManager.getDefaultSharedPreferences(getActivity())
                                                .getString(Utility.SYSTEM_LANGUAGE, ""));
                                startActivity(i);
                                return true;
                            }
                        })
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                getActivity().setRequestedOrientation(prevOrientation);
                            }
                        })
                        .show();
                languageDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getAction() == KeyEvent.ACTION_UP) {
                            arg0.dismiss();
                            getActivity().setRequestedOrientation(prevOrientation);
                            return true;
                        }
                        return false;
                    }
                });
                languageDialog.setCancelable(false);
//                languageDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
//                languageDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
//                        getResources().getColor(R.color.btn_disabled_text));
            }
        });

        return rootView;
    }

//    @SuppressLint("NewApi")
//    private class ButtonClickedListener implements DialogInterface.OnClickListener {
//        private int clickedCode;
//
//        public ButtonClickedListener(int code) {
//            clickedCode = code;
//        }
//
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            switch (clickedCode) {
//                case Utility.DISMISS:
//                    getActivity().setRequestedOrientation(prevOrientation);
//                    break;
//                case Utility.PREFERENCE_DEFINDEX_OK:
//                    SharedPreferences.Editor editor = PreferenceManager
//                            .getDefaultSharedPreferences(getActivity())
//                            .edit();
//                    editor.putInt(Utility.DEFAULT_INDEX, checkedItem);
//                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
//                        editor.commit();
//                    } else {
//                        editor.apply();
//                    }
//                    getActivity().setRequestedOrientation(prevOrientation);
//                    break;
//                case Utility.PREFERENCE_SAVELOC_OK:
//                    editor = PreferenceManager
//                            .getDefaultSharedPreferences(getActivity())
//                            .edit();
//                    editor.putInt(Utility.SAVE_LOCATION, checkedItem);
//                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
//                        editor.commit();
//                    } else {
//                        editor.apply();
//                    }
//                    getActivity().setRequestedOrientation(prevOrientation);
//                    break;
//                case Utility.PREFERENCE_LANGUAGE_OK:
//                    editor = PreferenceManager
//                            .getDefaultSharedPreferences(getActivity())
//                            .edit();
//                    switch (checkedItem) {
//                        case 0:
//                            editor.putString(Utility.SYSTEM_LANGUAGE, "it");
//                            break;
//                        case 1:
//                            editor.putString(Utility.SYSTEM_LANGUAGE, "uk");
//                            break;
//                    }
//                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
//                        editor.commit();
//                    } else {
//                        editor.apply();
//                    }
//                    getActivity().setRequestedOrientation(prevOrientation);
//                    Intent i = getActivity().getBaseContext().getPackageManager()
//                            .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
//                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    i.putExtra(Utility.DB_RESET, true);
//                    String currentLang = "it";
//                    if (getActivity().getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("uk"))
//                        currentLang = "uk";
//                    i.putExtra(Utility.CHANGE_LANGUAGE,
//                            currentLang + "-" + PreferenceManager.getDefaultSharedPreferences(getActivity())
//                                    .getString(Utility.SYSTEM_LANGUAGE, ""));
//                            startActivity(i);
//                    break;
//                default:
//                    getActivity().setRequestedOrientation(prevOrientation);
//                    break;
//            }
//        }
//    }

//    private int dpToPx(int dp) {
//        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
//        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//        return px;
//    }

//    private int[] getIntArray(int arrayId) {
//        final TypedArray ta = getActivity().getResources().obtainTypedArray(arrayId);
//        int[] mColors = new int[ta.length()];
//        for (int i = 0; i < ta.length(); i++)
//            mColors[i] = ta.getColor(i, 0);
//        ta.recycle();
//        return mColors;
//    }

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
        return ((MainActivity)getActivity()).getThemeUtils();
    }

}
