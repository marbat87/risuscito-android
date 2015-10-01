package it.cammino.risuscito.filepicker;

import com.nononsenseapps.filepicker.AbstractFilePickerActivity;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;

import java.io.File;

import it.cammino.risuscito.Utility;
import it.cammino.risuscito.utils.ThemeUtils;

/**
 * Created by marcello.battain on 01/10/2015.
 */
public class ThemedFilePickerActivity extends AbstractFilePickerActivity<File> {

    private ThemeUtils mThemeUtils;

    public ThemedFilePickerActivity() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(
            final String startPath, final int mode, final boolean allowMultiple,
            final boolean allowCreateDir) {
        // Only the fragment in this line needs to be changed
        mThemeUtils = new ThemeUtils(ThemedFilePickerActivity.this);
        setTheme(mThemeUtils.getFilePickerCurrent());
        Utility.setupTransparentTints(ThemedFilePickerActivity.this, mThemeUtils.primaryColorDark(), false);
        AbstractFilePickerFragment<File> fragment = new FilteredFilePickerFragment();
        fragment.setArgs(startPath, mode, allowMultiple, allowCreateDir);
        return fragment;
    }

    public ThemeUtils getThemeUtils() {
        return mThemeUtils;
    }
}

