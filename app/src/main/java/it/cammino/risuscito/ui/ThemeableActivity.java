package it.cammino.risuscito.ui;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.reflect.Field;

import it.cammino.risuscito.Utility;
import it.cammino.risuscito.utils.ThemeUtils;

public abstract class ThemeableActivity extends ActionBarActivity {

    private ThemeUtils mThemeUtils;
    protected boolean alsoLollipop = true;
    protected boolean hasNavDrawer = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (isMenuWorkaroundRequired()) {
            forceOverflowMenu();
        }
        super.onCreate(savedInstanceState);
        mThemeUtils = new ThemeUtils(this);
        setTheme(mThemeUtils.getCurrent(hasNavDrawer));

        // setta il colore della barra di stato, solo su KITKAT
        Utility.setupTransparentTints(ThemeableActivity.this, mThemeUtils.primaryColorDark(), alsoLollipop);
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT
//        		|| Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT_WATCH) {
//        	findViewById(R.id.content_layout).setPadding(0, getStatusBarHeight(), 0, 0);
//        	findViewById(R.id.navdrawer).setPadding(0, getStatusBarHeight(), 0, 0);
//        }

    }

    @Override
    protected void onResume() {
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
            ViewConfiguration config       = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.w(getClass().toString(), "Failed to force overflow menu.");
        }
    }

    public ThemeUtils getThemeUtils() {
        return mThemeUtils;
    }

}
