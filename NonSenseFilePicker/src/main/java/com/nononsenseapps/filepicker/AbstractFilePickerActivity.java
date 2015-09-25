/*
 * Copyright (c) 2014 Jonas Kalderstam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.filepicker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;



/**
 * An abstract base activity that handles all the fluff you don't care about.
 * <p/>
 * Usage: To start a child activity you could either use an intent starting the
 * activity directly, or you could use an implicit intent with GET_CONTENT, if
 * it
 * is also defined in your manifest. It is defined to be handled here in case
 * you
 * want the user to be able to use other file pickers on the system.
 * <p/>
 * That means using an intent with action GET_CONTENT
 * If you want to be able to select multiple items, include EXTRA_ALLOW_MULTIPLE
 * (default false).
 * <p/>
 * Two non-standard extra arguments are supported as well: EXTRA_ONLY_DIRS
 * (defaults to false)
 * allows only directories to be selected.
 * And EXTRA_START_PATH (default null), which should specify the starting path.
 * <p/>
 * The result of the user's action is returned in onActivityResult intent,
 * access it using getUri.
 * In case of multiple choices, these can be accessed with getClipData
 * containing Uri objects.
 * If running earlier than JellyBean you can access them with
 * getStringArrayListExtra(EXTRA_PATHS)
 *
 * @param <T>
 */
public abstract class AbstractFilePickerActivity<T> extends AppCompatActivity
        implements AbstractFilePickerFragment.OnFilePickedListener {
    public static final String EXTRA_START_PATH =
            "nononsense.intent" + ".START_PATH";
    public static final String EXTRA_MODE = "nononsense.intent.MODE";
    public static final String EXTRA_ALLOW_CREATE_DIR = "nononsense.intent" + ".ALLOW_CREATE_DIR";
    // For compatibility
    public static final String EXTRA_ALLOW_MULTIPLE =
            "android.intent.extra" + ".ALLOW_MULTIPLE";
    public static final String EXTRA_PATHS = "nononsense.intent.PATHS";
    public static final int MODE_FILE = AbstractFilePickerFragment.MODE_FILE;
    public static final int MODE_FILE_AND_DIR =
            AbstractFilePickerFragment.MODE_FILE_AND_DIR;
    public static final int MODE_DIR = AbstractFilePickerFragment.MODE_DIR;
    public static final String PRIMARY_COLOR = AbstractFilePickerFragment.KEY_PRIMARY_COLOR;
    public static final String ACCENT_COLOR = AbstractFilePickerFragment.KEY_ACCENT_COLOR;
    protected static final String TAG = "filepicker_fragment";
    protected String startPath = null;
    protected int mode = AbstractFilePickerFragment.MODE_FILE;
    protected boolean allowCreateDir = false;
    protected boolean allowMultiple = false;
    protected int primaryColor = 0;
    protected int accentColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setupFauxDialog();
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            startPath = intent.getStringExtra(EXTRA_START_PATH);
            mode = intent.getIntExtra(EXTRA_MODE, mode);
            allowCreateDir = intent.getBooleanExtra(EXTRA_ALLOW_CREATE_DIR,
                    allowCreateDir);
            allowMultiple =
                    intent.getBooleanExtra(EXTRA_ALLOW_MULTIPLE, allowMultiple);
            primaryColor = intent.getIntExtra(PRIMARY_COLOR, primaryColor);
            accentColor = intent.getIntExtra(ACCENT_COLOR, accentColor);

        }
        if (accentColor != 0)
            setTheme(getCurrentTheme(accentColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(shiftColorDown(primaryColor));
        setContentView(R.layout.activity_filepicker);
        setupActionBar();

        FragmentManager fm = getSupportFragmentManager();
        AbstractFilePickerFragment<T> fragment =
                (AbstractFilePickerFragment<T>) fm.findFragmentByTag(TAG);

        if (fragment == null) {
            fragment =
                    getFragment(startPath, mode, allowMultiple, allowCreateDir, primaryColor);
        }

        if (fragment != null) {
            fm.beginTransaction().replace(R.id.fragment, fragment, TAG)
                    .commit();
        }

        // Default to cancelled
        setResult(Activity.RESULT_CANCELED);
    }

    protected void setupFauxDialog() {
        // Check if this should be a dialog
        TypedValue tv = new TypedValue();
        if (!getTheme().resolveAttribute(R.attr.isDialog, tv, true) ||
                tv.data == 0) {
            return;
        }

        // Should be a dialog; set up the window parameters.
        DisplayMetrics dm = getResources().getDisplayMetrics();

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources()
                .getDimensionPixelSize(R.dimen.configure_dialog_width);
        params.height = Math.min(getResources()
                        .getDimensionPixelSize(R.dimen.configure_dialog_max_height),
                dm.heightPixels * 3 / 4);
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);
    }

    protected void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.risuscito_toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getWindowTitle());
        if (primaryColor != 0)
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(primaryColor));
    }

    protected abstract AbstractFilePickerFragment<T> getFragment(
            final String startPath, final int mode, final boolean allowMultiple,
            final boolean allowCreateDir, final int color);

    /**
     * @return the title to apply to the window
     */
    protected String getWindowTitle() {
        final int res;
        switch (mode) {
            case AbstractFilePickerFragment.MODE_DIR:
                res = R.plurals.select_dir;
                break;
            case AbstractFilePickerFragment.MODE_FILE_AND_DIR:
                res = R.plurals.select_dir_or_file;
                break;
            case AbstractFilePickerFragment.MODE_FILE:
            default:
                res = R.plurals.select_file;
                break;
        }

        final int count;
        if (allowMultiple) {
            count = 99;
        } else {
            count = 1;
        }

        return getResources().getQuantityString(res, count);
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
    }

    @Override
    public void onFilePicked(final Uri file) {
        Intent i = new Intent();
        i.setData(file);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onFilesPicked(final List<Uri> files) {
        Intent i = new Intent();
        i.putExtra(EXTRA_ALLOW_MULTIPLE, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ClipData clip = null;
            for (Uri file : files) {
                if (clip == null) {
                    clip = new ClipData("Paths", new String[]{},
                            new ClipData.Item(file));
                } else {
                    clip.addItem(new ClipData.Item(file));
                }
            }
            i.setClipData(clip);
        } else {
            ArrayList<String> paths = new ArrayList<>();
            for (Uri file : files) {
                paths.add(file.toString());
            }
            i.putStringArrayListExtra(EXTRA_PATHS, paths);
        }

        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public void onCancelled() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onCancelled();
                return true;
            default:
                return false;
        }
    }

    public int getCurrentTheme(int color) {
//        if (color == getResources().getColor(R.color.blue_dark))
//            return R.style.FilePicker_Theme_BlueDark;
//        if (color == getResources().getColor(R.color.grey))
//            return R.style.FilePicker_Theme_Grey;
//        if (color == getResources().getColor(R.color.blue_grey))
//            return R.style.FilePicker_Theme_BlueGrey;
//        if (color == getResources().getColor(R.color.black))
//            return R.style.FilePicker_Theme_Black;
//        if (color == getResources().getColor(R.color.brown))
//            return R.style.FilePicker_Theme_Brown;
//        if (color == getResources().getColor(R.color.red))
//            return R.style.FilePicker_Theme_Red;
//        if (color == getResources().getColor(R.color.pink))
//            return R.style.FilePicker_Theme_Pink;
//        if (color == getResources().getColor(R.color.purple))
//            return R.style.FilePicker_Theme_Purple;
//        if (color == getResources().getColor(R.color.violet))
//            return R.style.FilePicker_Theme_Violet;
//        if (color == getResources().getColor(R.color.blue))
//            return R.style.FilePicker_Theme_Blue;
//        if (color == getResources().getColor(R.color.blue_light))
//            return R.style.FilePicker_Theme_BlueLight;
//        if (color == getResources().getColor(R.color.torqouise))
//            return R.style.FilePicker_Theme_Torqouise;
//        if (color == getResources().getColor(R.color.green_water))
//            return R.style.FilePicker_Theme_GreenWater;
//        if (color == getResources().getColor(R.color.green))
//            return R.style.FilePicker_Theme_Green;
//        if (color == getResources().getColor(R.color.green_light))
//            return R.style.FilePicker_Theme_GreenLight;
//        if (color == getResources().getColor(R.color.green_bean))
//            return R.style.FilePicker_Theme_GreenBean;
//        if (color == getResources().getColor(R.color.yellow))
//            return R.style.FilePicker_Theme_Yellow;
//        if (color == getResources().getColor(R.color.orange_light))
//            return R.style.FilePicker_Theme_OrangeLight;
//        if (color == getResources().getColor(R.color.orange))
//            return R.style.FilePicker_Theme_Orange;
//        if (color == getResources().getColor(R.color.red_light))
//            return R.style.FilePicker_Theme_RedLight;
//        else
//            return R.style.FilePicker_Theme;

        if (color == Color.parseColor("#FF8A80"))
            return R.style.FilePicker_Theme_Red1;
        if (color == Color.parseColor("#FF5252"))
            return R.style.FilePicker_Theme_Red2;
        if (color == Color.parseColor("#FF1744"))
            return R.style.FilePicker_Theme_Red3;
        if (color == Color.parseColor("#D50000"))
            return R.style.FilePicker_Theme_Red4;

        if (color == Color.parseColor("#FF80AB"))
            return R.style.FilePicker_Theme_Pink1;
        if (color == Color.parseColor("#FF4081"))
            return R.style.FilePicker_Theme_Pink2;
        if (color == Color.parseColor("#F50057"))
            return R.style.FilePicker_Theme_Pink3;
        if (color == Color.parseColor("#C51162"))
            return R.style.FilePicker_Theme_Pink4;

        if (color == Color.parseColor("#EA80FC"))
            return R.style.FilePicker_Theme_Purple1;
        if (color == Color.parseColor("#E040FB"))
            return R.style.FilePicker_Theme_Purple2;
        if (color == Color.parseColor("#D500F9"))
            return R.style.FilePicker_Theme_Purple3;
        if (color == Color.parseColor("#AA00FF"))
            return R.style.FilePicker_Theme_Purple4;

        if (color == Color.parseColor("#B388FF"))
            return R.style.FilePicker_Theme_Violet1;
        if (color == Color.parseColor("#7C4DFF"))
            return R.style.FilePicker_Theme_Violet2;
        if (color == Color.parseColor("#651FFF"))
            return R.style.FilePicker_Theme_Violet3;
        if (color == Color.parseColor("#6200EA"))
            return R.style.FilePicker_Theme_Violet4;

        if (color == Color.parseColor("#8C9EFF"))
            return R.style.FilePicker_Theme_Blue1;
        if (color == Color.parseColor("#536DFE"))
            return R.style.FilePicker_Theme_Blue2;
        if (color == Color.parseColor("#3D5AFE"))
            return R.style.FilePicker_Theme_Blue3;
        if (color == Color.parseColor("#304FFE"))
            return R.style.FilePicker_Theme_Blue4;

        if (color == Color.parseColor("#82B1FF"))
            return R.style.FilePicker_Theme_Azure1;
        if (color == Color.parseColor("#448AFF"))
            return R.style.FilePicker_Theme_Azure2;
        if (color == Color.parseColor("#2979FF"))
            return R.style.FilePicker_Theme_Azure3;
        if (color == Color.parseColor("#2962FF"))
            return R.style.FilePicker_Theme_Azure4;

        if (color == Color.parseColor("#80D8FF"))
            return R.style.FilePicker_Theme_Turqouise1;
        if (color == Color.parseColor("#40C4FF"))
            return R.style.FilePicker_Theme_Turqouise2;
        if (color == Color.parseColor("#00B0FF"))
            return R.style.FilePicker_Theme_Turqouise3;
        if (color == Color.parseColor("#0091EA"))
            return R.style.FilePicker_Theme_Turqouise4;

        if (color == Color.parseColor("#84FFFF"))
            return R.style.FilePicker_Theme_BlueLight1;
        if (color == Color.parseColor("#18FFFF"))
            return R.style.FilePicker_Theme_BlueLight2;
        if (color == Color.parseColor("#00E5FF"))
            return R.style.FilePicker_Theme_BlueLight3;
        if (color == Color.parseColor("#00B8D4"))
            return R.style.FilePicker_Theme_BlueLight4;

        if (color == Color.parseColor("#A7FFEB"))
            return R.style.FilePicker_Theme_GreenWater1;
        if (color == Color.parseColor("#64FFDA"))
            return R.style.FilePicker_Theme_GreenWater2;
        if (color == Color.parseColor("#1DE9B6"))
            return R.style.FilePicker_Theme_GreenWater3;
        if (color == Color.parseColor("#00BFA5"))
            return R.style.FilePicker_Theme_GreenWater4;

        if (color == Color.parseColor("#B9F6CA"))
            return R.style.FilePicker_Theme_Green1;
        if (color == Color.parseColor("#69F0AE"))
            return R.style.FilePicker_Theme_Green2;
        if (color == Color.parseColor("#00E676"))
            return R.style.FilePicker_Theme_Green3;
        if (color == Color.parseColor("#00C853"))
            return R.style.FilePicker_Theme_Green4;

        if (color == Color.parseColor("#CCFF90"))
            return R.style.FilePicker_Theme_GreenLight1;
        if (color == Color.parseColor("#B2FF59"))
            return R.style.FilePicker_Theme_GreenLight2;
        if (color == Color.parseColor("#76FF03"))
            return R.style.FilePicker_Theme_GreenLight3;
        if (color == Color.parseColor("#64DD17"))
            return R.style.FilePicker_Theme_GreenLight4;

        if (color == Color.parseColor("#F4FF81"))
            return R.style.FilePicker_Theme_Lime1;
        if (color == Color.parseColor("#EEFF41"))
            return R.style.FilePicker_Theme_Lime2;
        if (color == Color.parseColor("#C6FF00"))
            return R.style.FilePicker_Theme_Lime3;
        if (color == Color.parseColor("#AEEA00"))
            return R.style.FilePicker_Theme_Lime4;

        if (color == Color.parseColor("#FFFF8D"))
            return R.style.FilePicker_Theme_Yellow1;
        if (color == Color.parseColor("#FFFF00"))
            return R.style.FilePicker_Theme_Yellow2;
        if (color == Color.parseColor("#FFEA00"))
            return R.style.FilePicker_Theme_Yellow3;
        if (color == Color.parseColor("#FFD600"))
            return R.style.FilePicker_Theme_Yellow4;

        if (color == Color.parseColor("#FFE57F"))
            return R.style.FilePicker_Theme_OrangeLight1;
        if (color == Color.parseColor("#FFD740"))
            return R.style.FilePicker_Theme_OrangeLight2;
        if (color == Color.parseColor("#FFC400"))
            return R.style.FilePicker_Theme_OrangeLight3;
        if (color == Color.parseColor("#FFAB00"))
            return R.style.FilePicker_Theme_OrangeLight4;

        if (color == Color.parseColor("#FFD180"))
            return R.style.FilePicker_Theme_Orange1;
        if (color == Color.parseColor("#FFAB40"))
            return R.style.FilePicker_Theme_Orange2;
        if (color == Color.parseColor("#FF9100"))
            return R.style.FilePicker_Theme_Orange3;
        if (color == Color.parseColor("#FF6D00"))
            return R.style.FilePicker_Theme_Orange4;

        if (color == Color.parseColor("#FF9E80"))
            return R.style.FilePicker_Theme_OrangeDark1;
        if (color == Color.parseColor("#FF6E40"))
            return R.style.FilePicker_Theme_OrangeDark2;
        if (color == Color.parseColor("#FF3D00"))
            return R.style.FilePicker_Theme_OrangeDark3;
        if (color == Color.parseColor("#DD2C00"))
            return R.style.FilePicker_Theme_OrangeDark4;
        else
            return R.style.FilePicker_Theme;
    }

    public static int shiftColorDown(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

}
