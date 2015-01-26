package it.cammino.risuscito;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.alertdialogpro.AlertDialogPro;

public class PreferencesFragment extends Fragment {
	
	private int prevOrientation;
	private SwitchCompat screenSwitch, secondaSwitch, paceSwitch, santoSwitch;
	private int saveEntries;
	
	private int checkedItem;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.preference_screen, container, false);
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_settings);
        ((MainActivity) getActivity()).getSupportActionBar()
                .setElevation(dpToPx(getResources().getInteger(R.integer.toolbar_elevation)));
		
		screenSwitch = (SwitchCompat) rootView.findViewById(R.id.screen_on);

        // controllo l'attuale impostazione di always on
        if (PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SCREEN_ON, false))
            screenSwitch.setChecked(true);
        else
            screenSwitch.setChecked(false);

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
        if (PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SHOW_SECONDA, false))
            secondaSwitch.setChecked(true);
        else
            secondaSwitch.setChecked(false);

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
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				if (secondaSwitch.isChecked())
					secondaSwitch.setChecked(false);
				else
					secondaSwitch.setChecked(true);
			}
		});

        santoSwitch = (SwitchCompat) rootView.findViewById(R.id.show_santo);

        // controllo l'attuale impostazione della visualizzazione seconda lettura
        if (PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SHOW_SANTO, false))
            santoSwitch.setChecked(true);
        else
            santoSwitch.setChecked(false);

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
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (santoSwitch.isChecked())
                    santoSwitch.setChecked(false);
                else
                    santoSwitch.setChecked(true);
            }
        });


        paceSwitch = (SwitchCompat) rootView.findViewById(R.id.show_pace_parola);

        // controllo l'attuale impostazione della visualizzazione canto alla pace
        if (PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(Utility.SHOW_PACE, false))
            paceSwitch.setChecked(true);
        else
            paceSwitch.setChecked(false);

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
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (paceSwitch.isChecked())
                    paceSwitch.setChecked(false);
                else
                    paceSwitch.setChecked(true);
            }
        });

		View defaultIndexView = rootView.findViewById(R.id.default_index_layout);
		defaultIndexView.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
                prevOrientation = getActivity().getRequestedOrientation();
                Utility.blockOrientation(getActivity());
                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
                AlertDialogPro dialog = builder.setTitle(R.string.default_index_title)
                        .setSingleChoiceItems(getResources().getStringArray(R.array.pref_default_index_entries),
                        		PreferenceManager
                				.getDefaultSharedPreferences(getActivity())
                				.getInt(Utility.DEFAULT_INDEX, 0),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    	checkedItem = which;
                                    }
                                })
                        .setNegativeButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
                        .setPositiveButton(R.string.single_choice_ok, new ButtonClickedListener(Utility.PREFERENCE_DEFINDEX_OK))
                        .show();
                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
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
                dialog.setCancelable(false);
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
                AlertDialogPro.Builder builder = new AlertDialogPro.Builder(getActivity());
                AlertDialogPro dialog = builder.setTitle(R.string.save_location_title)
                        .setSingleChoiceItems(getResources().getStringArray(saveEntries),
                        		PreferenceManager
                				.getDefaultSharedPreferences(getActivity())
                				.getInt(Utility.SAVE_LOCATION, 0),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    	checkedItem = which;
                                    }
                                })
                        .setNegativeButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
                        .setPositiveButton(R.string.single_choice_ok, new ButtonClickedListener(Utility.PREFERENCE_SAVELOC_OK))
                        .show();
                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
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
                dialog.setCancelable(false);
			}
		});
		
		return rootView;
	}

	@SuppressLint("NewApi")
	private class ButtonClickedListener implements DialogInterface.OnClickListener {
        private int clickedCode;

        public ButtonClickedListener(int code) {
        	clickedCode = code;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (clickedCode) {
			case Utility.DISMISS:
				getActivity().setRequestedOrientation(prevOrientation);
				break;
			case Utility.PREFERENCE_DEFINDEX_OK:
				SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .edit();
				editor.putInt(Utility.DEFAULT_INDEX, checkedItem);
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
					editor.commit();
				} else {
					editor.apply();
				}
				getActivity().setRequestedOrientation(prevOrientation);
				break;
			case Utility.PREFERENCE_SAVELOC_OK:
				editor = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .edit();
				editor.putInt(Utility.SAVE_LOCATION, checkedItem);
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
					editor.commit();
				} else {
					editor.apply();
				}
				getActivity().setRequestedOrientation(prevOrientation);
				break;
			default:
				getActivity().setRequestedOrientation(prevOrientation);
				break;
			}
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

}
