package it.cammino.risuscito.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import it.cammino.risuscito.R;

/**
 * Created by marcello.battain on 12/02/2015.
 */
public class ThemeUtils {

    public ThemeUtils(Activity context) {
        mContext = context;
        isChanged(false); // invalidate stored booleans
    }

    private Context mContext;
    private boolean mDarkMode;
    private boolean mTrueBlack;
    private int mLastPrimaryColor;
    private int mLastAccentColor;
    private boolean mLastColoredNav;
    private boolean mDirectoryCount;

    public int getPopupTheme() {
        if (mDarkMode || mTrueBlack) {
            return R.style.ThemeOverlay_AppCompat_Dark;
        } else {
            return R.style.ThemeOverlay_AppCompat_Light;
        }
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("dark_mode", false);
    }

    public static boolean isTrueBlack(Context context) {
        if (!isDarkMode(context)) return false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("true_black", false);
    }

    public static boolean isDirectoryCount(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("directory_count", false);
    }

    public int primaryColor() {
        final int defaultColor = mContext.getResources().getColor(R.color.theme_primary);
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt("primary_color", defaultColor);
    }

    public void primaryColor(int newColor) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("primary_color", newColor).commit();
    }

    public int primaryColorDark() {
        return ColorChooserDialog.shiftColorDown(primaryColor());
    }

    public int accentColor() {
        final int defaultColor = mContext.getResources().getColor(R.color.theme_accent);
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt("accent_color", defaultColor);
    }

    public int accentColorLight() {
        return ColorChooserDialog.shiftColorUp(accentColor());
    }

    public int accentColorDark() {
        return ColorChooserDialog.shiftColorDown(accentColor());
    }

    public void accentColor(int newColor) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("accent_color", newColor).commit();
    }

    public boolean isColoredNavBar() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("colored_navbar", true);
    }

    public boolean isChanged(boolean checkForChanged) {
        final boolean darkTheme = isDarkMode(mContext);
        final boolean blackTheme = isTrueBlack(mContext);
        final int primaryColor = primaryColor();
        final int accentColor = accentColor();
        final boolean coloredNav = isColoredNavBar();
        final boolean directoryCount = isDirectoryCount(mContext);

        boolean changed = false;
        if (checkForChanged) {
            changed = mDarkMode != darkTheme || mTrueBlack != blackTheme ||
                    mLastPrimaryColor != primaryColor || mLastAccentColor != accentColor ||
                    coloredNav != mLastColoredNav ||
                    directoryCount != mDirectoryCount;
        }

        mDarkMode = darkTheme;
        mTrueBlack = blackTheme;
        mLastPrimaryColor = primaryColor;
        mLastAccentColor = accentColor;
        mLastColoredNav = coloredNav;
        mDirectoryCount = directoryCount;

        return changed;
    }

//    public int getCurrent(boolean hasNavDrawer) {
//        if (hasNavDrawer) {
//            if (mTrueBlack) {
//                return R.style.Theme_CabinetTrueBlack_WithNavDrawer;
//            } else if (mDarkMode) {
//                return R.style.Theme_CabinetDark_WithNavDrawer;
//            } else {
//                return R.style.Theme_Cabinet_WithNavDrawer;
//            }
//        } else {
//            if (mTrueBlack) {
//                return R.style.Theme_CabinetTrueBlack;
//            } else if (mDarkMode) {
//                return R.style.Theme_CabinetDark;
//            } else {
//                return R.style.Theme_Cabinet;
//            }
//        }
//    }
}

