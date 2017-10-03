package it.cammino.risuscito.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.marverenic.colors.AccentColor;
import com.marverenic.colors.PrimaryColor;

import it.cammino.risuscito.Utility;

public class ThemeUtils {

    public ThemeUtils(Context context) {
        mContext = context;
//        isChanged(false); // invalidate stored booleans
    }

    private  final static String PRIMARY_COLOR_PREF_KEY = "risuscito_primary_color";
    private  final static String ACCENT_COLOR_PREF_KEY = "risuscito_accent_color";

    private Context mContext;
//    private boolean mDarkMode;
//    private boolean mTrueBlack;
//    private int mLastPrimaryColor;
//    private int mLastAccentColor;
//    private boolean mLastColoredNav;
//    private boolean mDirectoryCount;

//    public int getPopupTheme() {
//        if (mDarkMode || mTrueBlack) {
//            return R.style.ThemeOverlay_AppCompat_Dark;
//        } else {
//            return R.style.ThemeOverlay_AppCompat_Light;
//        }
//    }

    public boolean isDarkMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getBoolean(Utility.NIGHT_THEME, false);
    }

//    public static boolean isTrueBlack(Context context) {
//        if (!isDarkMode(context)) return false;
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        return prefs.getBoolean("true_black", false);
//    }

    public static boolean isDirectoryCount(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("directory_count", false);
    }

    public PrimaryColor primaryColorNew() {
        PrimaryColor defaultPrimary = PrimaryColor.INDIGO_500;
        return PrimaryColor.findById(PreferenceManager.getDefaultSharedPreferences(mContext).getString(PRIMARY_COLOR_PREF_KEY, defaultPrimary.getId()));
    }

    public int primaryColor() {
//        final int defaultColor = ContextCompat.getColor(mContext, R.color._color_lib_indigo_500);
//        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(PRIMARY_COLOR_PREF_KEY, defaultColor);
        return ContextCompat.getColor(mContext, primaryColorNew().getPrimaryColorRes());
    }


//    public void primaryColor(int newColor) {
//        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(PRIMARY_COLOR_PREF_KEY, newColor).apply();
//    }

    public void primaryColor(PrimaryColor newColor) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(PRIMARY_COLOR_PREF_KEY, newColor.getId()).apply();
    }


    public int primaryColorDark() {
        return ContextCompat.getColor(mContext, primaryColorNew().getPrimaryDarkColorRes());
//        return shiftColorDown(primaryColor());
    }

//    public int primaryColorLight() {
//        return lighter(primaryColor(), 0.5f);
//
//    }

    public AccentColor accentColorNew() {
        AccentColor defaultAccent = AccentColor.PINK_A200;
        return AccentColor.findById(PreferenceManager.getDefaultSharedPreferences(mContext).getString(ACCENT_COLOR_PREF_KEY, defaultAccent.getId()));
    }

    public int accentColor() {
//        final int defaultColor = ContextCompat.getColor(mContext, R.color._color_lib_pink_A200);
//        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(ACCENT_COLOR_PREF_KEY, defaultColor);
        return ContextCompat.getColor(mContext, accentColorNew().getAccentColorRes());
    }

    public int accentColorLight() {
        return lighter(accentColor(), 0.5f);

    }

    public int accentColorDark() {
        return shiftColorDown(accentColor());
    }

//    public void accentColor(int newColor) {
//        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(ACCENT_COLOR_PREF_KEY, newColor).apply();
//    }

    public void accentColor(AccentColor newColor) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(ACCENT_COLOR_PREF_KEY, newColor.getId()).apply();
    }

    public boolean isColoredNavBar() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("colored_navbar", true);
    }

//    public boolean isChanged(boolean checkForChanged) {
//        final boolean darkTheme = isDarkMode(mContext);
//        final boolean blackTheme = isTrueBlack(mContext);
//        final int primaryColor = primaryColor();
//        final int accentColor = accentColor();
//        final boolean coloredNav = isColoredNavBar();
//        final boolean directoryCount = isDirectoryCount(mContext);
//
//        boolean changed = false;
//        if (checkForChanged) {
//            changed = mDarkMode != darkTheme || mTrueBlack != blackTheme ||
//                    mLastPrimaryColor != primaryColor || mLastAccentColor != accentColor ||
//                    coloredNav != mLastColoredNav ||
//                    directoryCount != mDirectoryCount;
//        }
//
//        mDarkMode = darkTheme;
//        mTrueBlack = blackTheme;
//        mLastPrimaryColor = primaryColor;
//        mLastAccentColor = accentColor;
//        mLastColoredNav = coloredNav;
//        mDirectoryCount = directoryCount;
//
//        return changed;
//    }

    private static int shiftColorDown(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

//    public static int shiftColorUp(int color) {
//        float[] hsv = new float[3];
//        Color.colorToHSV(color, hsv);
//        hsv[2] = 0.2f + 0.8f * hsv[2];
//        return Color.HSVToColor(hsv);
//    }

    /**
     * Lightens a color by a given factor.
     *
     * @param color
     *            The color to lighten
     * @param factor
     *            The factor to lighten the color. 0 will make the color unchanged. 1 will make the
     *            color white.
     * @return lighter version of the specified color.
     */
    private static int lighter(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }

    public boolean isLightTheme() {
        int color = primaryColor();
        double a = 1 -  (Color.red(color)*0.299 + Color.green(color)*0.587 + Color.blue(color)*0.114) / 255;
        return a < 0.5;
    }

}

