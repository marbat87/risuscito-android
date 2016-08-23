package it.cammino.risuscito.filepicker;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.nononsenseapps.filepicker.AbstractFilePickerActivity;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;

import java.io.File;

import it.cammino.risuscito.Utility;
import it.cammino.risuscito.utils.ThemeUtils;

public class ThemedFilePickerActivity extends AbstractFilePickerActivity<File> {

    private ThemeUtils mThemeUtils;

    public ThemedFilePickerActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThemeUtils = new ThemeUtils(ThemedFilePickerActivity.this);
        setTheme(mThemeUtils.getFilePickerCurrent());
        Utility.setupTransparentTints(ThemedFilePickerActivity.this, mThemeUtils.primaryColorDark(), false);
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(@Nullable String startPath, int mode, boolean allowMultiple, boolean allowCreateDir, boolean allowExistingFile, boolean singleClick) {
        AbstractFilePickerFragment<File> fragment = new FilteredFilePickerFragment();
        fragment.setArgs(startPath, mode, allowMultiple, allowCreateDir, allowExistingFile, singleClick);
        return fragment;
    }

//    @Override
//    protected AbstractFilePickerFragment<File> getFragment(
//            final String startPath, final int mode, final boolean allowMultiple,
//            final boolean allowCreateDir) {
//        // Only the fragment in this line needs to be changed
////        Utility.blockOrientation(this);
////        mThemeUtils = new ThemeUtils(ThemedFilePickerActivity.this);
////        setTheme(mThemeUtils.getFilePickerCurrent());
////        Utility.setupTransparentTints(ThemedFilePickerActivity.this, mThemeUtils.primaryColorDark(), false);
//        AbstractFilePickerFragment<File> fragment = new FilteredFilePickerFragment();
//        fragment.setArgs(startPath, mode, allowMultiple, allowCreateDir);
//        return fragment;
//    }

    public ThemeUtils getThemeUtils() {
        return mThemeUtils;
    }
}

