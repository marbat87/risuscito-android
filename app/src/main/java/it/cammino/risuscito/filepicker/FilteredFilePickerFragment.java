package it.cammino.risuscito.filepicker;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;

public class FilteredFilePickerFragment extends FilePickerFragment {

    // File extension to filter on
    private static final String EXTENSION = ".mp3";

    /**
     *
     * @param file
     * @return The file extension. If file has no extension, it returns null.
     */
    private String getExtension(@NonNull File file) {
        String path = file.getPath();
        int i = path.lastIndexOf(".");
        if (i < 0) {
            return null;
        } else {
            return path.substring(i);
        }
    }

    @Override
    protected boolean isItemVisible(final File file) {
        if (!isDir(file) && (mode == MODE_FILE || mode == MODE_FILE_AND_DIR)) {
            return EXTENSION.equalsIgnoreCase(getExtension(file));
        }
        return isDir(file);
    }

//    protected Toolbar mToolbar;

    @Override
    protected void setupToolbar(Toolbar toolbar) {
        // Prevent it from being set as main toolbar by NOT calling super.setupToolbar().
        toolbar.setBackgroundColor(((ThemedFilePickerActivity)getActivity()).getThemeUtils().primaryColor());
        super.setupToolbar(toolbar);
    }
}
