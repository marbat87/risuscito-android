package it.cammino.risuscito.ui;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.view.LayoutInflaterCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.marverenic.colors.Colors;
import com.marverenic.colors.NightMode;
import com.marverenic.colors.activity.ColorsAppCompatActivity;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

import it.cammino.risuscito.LUtils;
import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;
import it.cammino.risuscito.utils.ThemeUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class ThemeableActivity extends ColorsAppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ThemeUtils mThemeUtils;
    protected boolean hasNavDrawer = false;

    final String TAG = getClass().getCanonicalName();

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(TAG, "onSharedPreferenceChanged: " + s);
        if (s.equals(Utility.SYSTEM_LANGUAGE)) {
//            Log.d(TAG, "onSharedPreferenceChanged: cur lang" + getResources().getConfiguration().locale.getLanguage());
            Log.d(TAG, "onSharedPreferenceChanged: cur lang " + ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage());
            Log.d(TAG, "onSharedPreferenceChanged: cur set " + sharedPreferences.getString(s, ""));
            if (!ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage()
                    .equalsIgnoreCase(sharedPreferences.getString(s, "it"))) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra(Utility.DB_RESET, true);
                    String currentLang = ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration()).getLanguage();
                    i.putExtra(Utility.CHANGE_LANGUAGE,
                            currentLang + "-" + sharedPreferences.getString(s, ""));
                }
                startActivity(i);
            }
        }
        if (s.equals(Utility.SCREEN_ON))
            ThemeableActivity.this.checkScreenAwake();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (isMenuWorkaroundRequired()) {
            forceOverflowMenu();
        }
        mThemeUtils = new ThemeUtils(this);
        LUtils mLUtils = LUtils.getInstance(this);
        mLUtils.convertIntPreferences();
//        setTheme(mThemeUtils.getCurrent());
        Colors.setTheme(mThemeUtils.primaryColorNew(), mThemeUtils.accentColorNew(), mThemeUtils.isDarkMode() ? NightMode.NIGHT : NightMode.DAY);
        setTheme(R.style.RisuscitoTheme);
//        Colors.setTheme(mThemeUtils.primaryColorNew(), mThemeUtils.accentColorNew(), mThemeUtils.isDarkMode() ? NightMode.NIGHT : NightMode.DAY);

        // setta il colore della barra di stato, solo su KITKAT
        Utility.setupTransparentTints(ThemeableActivity.this, mThemeUtils.primaryColorDark(), hasNavDrawer);

        if (LUtils.hasL()) {
            // Since our app icon has the same color as colorPrimary, our entry in the Recent Apps
            // list gets weird. We need to change either the icon or the color
            // of the TaskDescription.
            ActivityManager.TaskDescription taskDesc =
                    new ActivityManager.TaskDescription(
                            null,
                            null,
                            mThemeUtils.primaryColor());
            setTaskDescription(taskDesc);
        }

        //Iconic
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        checkScreenAwake();

        PreferenceManager.getDefaultSharedPreferences(ThemeableActivity.this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(ThemeableActivity.this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && isMenuWorkaroundRequired()) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return (keyCode == KeyEvent.KEYCODE_MENU && isMenuWorkaroundRequired()) || super.onKeyDown(keyCode, event);
    }

    //controlla se l'app deve mantenere lo schermo acceso
    public void checkScreenAwake() {
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(this);
        boolean screenOn = pref.getBoolean(Utility.SCREEN_ON, false);
        if (screenOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static boolean isMenuWorkaroundRequired() {
        return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT          &&
//                android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1 &&
                ("LGE".equalsIgnoreCase(Build.MANUFACTURER) || "E6710".equalsIgnoreCase(Build.DEVICE));
    }

    private void forceOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (IllegalAccessException e) {
            Log.w(getClass().toString(), "IllegalAccessException - Failed to force overflow menu.", e);
        } catch (NoSuchFieldException e) {
            Log.w(getClass().toString(), "NoSuchFieldException - Failed to force overflow menu.", e);
        }
    }

    public ThemeUtils getThemeUtils() {
        return mThemeUtils;
    }

    @Override
    protected void attachBaseContext(Context newBase) {

        Configuration config = new Configuration();
//        boolean changeConfig = false;

        //lingua
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(newBase);
        String language = sp.getString(Utility.SYSTEM_LANGUAGE, "");
        Log.d(TAG, "attachBaseContext - language: " + language);
        //ho settato almeno una volta la lingua --> imposto quella
        if (!language.equals("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            ThemeableActivity.setSystemLocalWrapper(config, locale);
//            changeConfig = true;
        }
        // non è ancora stata impostata nessuna lingua nelle impostazioni --> setto una lingua selezionabile oppure IT se non presente
        else {
            SharedPreferences.Editor mEditor = sp.edit();
            String mLanguage;
            switch (getSystemLocalWrapper(newBase.getResources().getConfiguration()).getLanguage()) {
                case "uk":
                    mLanguage = "uk";
                    break;
                case "en":
                    mLanguage = "en";
                    break;
                default:
                    mLanguage = "it";
                    break;
            }
            mEditor.putString(Utility.SYSTEM_LANGUAGE, mLanguage);
            mEditor.apply();
            Locale locale = new Locale(mLanguage);
            Locale.setDefault(locale);
            ThemeableActivity.setSystemLocalWrapper(config, locale);
//            changeConfig = true;
        }

        //fond dimension
        try {
            float actualScale = newBase.getResources().getConfiguration().fontScale;
            Log.d(TAG, "actualScale: " + actualScale);
            float systemScale = Settings.System.getFloat(getContentResolver(), Settings.System.FONT_SCALE);
            Log.d(TAG, "systemScale: " + systemScale);
            if (actualScale != systemScale) {
                config.fontScale = systemScale;
//                changeConfig = true;
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: " + e.getLocalizedMessage());
        }
        catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException - FUNZIONE RESIZE TESTO NON SUPPORTATA: " + e.getLocalizedMessage());
        }

//        if (changeConfig) {
        if (LUtils.hasJB()) {
            newBase = newBase.createConfigurationContext(config);
        } else {
            //noinspection deprecation
            newBase.getResources().updateConfiguration(config, newBase.getResources().getDisplayMetrics());
        }
//        }

        //Calligraphy
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected boolean saveSharedPreferencesToFile(OutputStream out) {
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(out);
            SharedPreferences pref =
                    PreferenceManager.getDefaultSharedPreferences(ThemeableActivity.this);
            output.writeObject(pref.getAll());

        }
        catch (IOException e) {
            String error = "saveSharedPreferencesToFile - IOException: " + e.getLocalizedMessage();
            Log.e(getClass().getName(), error, e);
            Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
        }
        finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                    res = true;
                }
            } catch (IOException e) {
                String error = "saveSharedPreferencesToFile - IOException: " + e.getLocalizedMessage();
                Log.e(getClass().getName(), error, e);
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
            }
        }
        return res;
    }

    @SuppressWarnings({ "unchecked" })
    protected void loadSharedPreferencesFromFile(InputStream in) {
//        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(in);
            SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(ThemeableActivity.this).edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, (Boolean) v);
                else if (v instanceof Float)
                    prefEdit.putFloat(key, (Float) v);
                else if (v instanceof Integer)
                    prefEdit.putInt(key, (Integer) v);
                else if (v instanceof Long)
                    prefEdit.putLong(key, (Long) v);
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.apply();
        } catch (ClassNotFoundException e) {
            String error = "loadSharedPreferencesFromFile - ClassNotFoundException: " + e.getLocalizedMessage();
            Log.e(getClass().getName(), error, e);
            Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            String error = "loadSharedPreferencesFromFile - IOException: " + e.getLocalizedMessage();
            Log.e(getClass().getName(), error, e);
            Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
        }
        finally {
            try {
                if (input != null) {
                    input.close();
//                    res = true;
                }
            } catch (IOException e) {
                String error = "loadSharedPreferencesFromFile - IOException: " + e.getLocalizedMessage();
                Log.e(getClass().getName(), error, e);
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static Locale getSystemLocaleLegacy(Configuration config) {
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Locale getSystemLocale(Configuration config) {
        return config.getLocales().get(0);
    }

    public static Locale getSystemLocalWrapper(Configuration config) {
        if (LUtils.hasN())
            return ThemeableActivity.getSystemLocale(config);
        else
            return ThemeableActivity.getSystemLocaleLegacy(config);
    }

    @SuppressWarnings("deprecation")
    private static void setSystemLocaleLegacy(Configuration config, Locale locale){
        config.locale = locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static void setSystemLocale(Configuration config, Locale locale){
        config.setLocale(locale);
    }

    public static void setSystemLocalWrapper(Configuration config, Locale locale){
        if (LUtils.hasN())
            ThemeableActivity.setSystemLocale(config, locale);
        else
            ThemeableActivity.setSystemLocaleLegacy(config, locale);
    }

}


