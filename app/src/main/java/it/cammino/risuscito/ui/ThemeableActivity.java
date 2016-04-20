package it.cammino.risuscito.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

import it.cammino.risuscito.Utility;
import it.cammino.risuscito.utils.ThemeUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class ThemeableActivity extends AppCompatActivity {

    private ThemeUtils mThemeUtils;
    protected boolean hasNavDrawer = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (isMenuWorkaroundRequired()) {
            forceOverflowMenu();
        }
        mThemeUtils = new ThemeUtils(this);
        setTheme(mThemeUtils.getCurrent());
        // setta il colore della barra di stato, solo su KITKAT
        Utility.setupTransparentTints(ThemeableActivity.this, mThemeUtils.primaryColorDark(), hasNavDrawer);

        //lingua
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        // get version numbers
        String language = sp.getString(Utility.SYSTEM_LANGUAGE, "");
        if (!language.equals("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        try {
            float actualScale = getResources().getConfiguration().fontScale;
            Log.d(getClass().toString(), "actualScale: " + actualScale);
            float systemScale = Settings.System.getFloat(getContentResolver(), Settings.System.FONT_SCALE);
            Log.d(getClass().toString(), "systemScale: " + systemScale);
            if (actualScale != systemScale) {
                Configuration config = new Configuration();
                config.fontScale = systemScale;
                getResources().updateConfiguration(config, getResources().getDisplayMetrics());
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.e(getClass().toString(), "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: " + e.getLocalizedMessage());
//            Log.e(getClass().getName(), "ECCEZIONE: " +  e.toString());
//            for (StackTraceElement ste: e.getStackTrace()) {
//                Log.e(getClass().toString(), ste.toString());
//            }
        }
        catch (NullPointerException e) {
            Log.e(getClass().toString(), "NullPointerException - FUNZIONE RESIZE TESTO NON SUPPORTATA: " + e.getLocalizedMessage());
//            Log.e(getClass().getName(), "ECCEZIONE: " +  e.toString());
//            for (StackTraceElement ste: e.getStackTrace()) {
//                Log.e(getClass().toString(), ste.toString());
//            }
        }
        super.onResume();

        checkScreenAwake();
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
                android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1 &&
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
    protected boolean loadSharedPreferencesFromFile(InputStream in) {
        boolean res = false;
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
                    res = true;
                }
            } catch (IOException e) {
                String error = "loadSharedPreferencesFromFile - IOException: " + e.getLocalizedMessage();
                Log.e(getClass().getName(), error, e);
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
            }
        }
        return res;
    }

}


