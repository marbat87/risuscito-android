package it.cammino.risuscito.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.alertdialogpro.AlertDialogPro;

import it.cammino.risuscito.MainActivity;
import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;
import it.cammino.risuscito.view.CircleView;

/**
 * Created by marcello.battain on 12/02/2015.
 */
public class ColorChooserDialog extends DialogFragment implements View.OnClickListener {

    private ColorCallback mCallback;
    private int[] mColors;
    private GridView mGrid;
    private int prevOrientation;
    private ActionBarActivity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (ColorCallback) activity;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            final int index = (Integer) v.getTag();
            getArguments().putInt("preselect", mColors[index]);
            invalidateGrid();
        }
    }

    public static interface ColorCallback {
        void onColorSelection(int title, int color);
    }

    public ColorChooserDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        prevOrientation = activity.getRequestedOrientation();
        AlertDialogPro.Builder builder = new AlertDialogPro.Builder(activity);
        AlertDialogPro dialog = builder.setTitle(getString(getArguments().getInt("title", 0)))
                .setView(R.layout.dialog_color_chooser)
                .setPositiveButton(R.string.single_choice_ok, new ButtonClickedListener(Utility.CHANGE_COLOR))
//                .setNeutralButton(R.string.defaultStr, new ButtonClickedListener(Utility.RESET_COLOR))
                .setNegativeButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
                .setCancelable(false)
                .show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
                ((MainActivity)activity).getThemeUtils().accentColor());
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(
                ((MainActivity)activity).getThemeUtils().accentColor());
        final boolean primary = getArguments().getInt("title", 0) == R.string.primary_color;
        final TypedArray ta = activity.getResources().obtainTypedArray(
                primary ? R.array.colors_primary : R.array.colors_accent);
        mColors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++)
            mColors[i] = ta.getColor(i, 0);
        ta.recycle();
        mGrid = (GridView) dialog.findViewById(R.id.gridview);
        invalidateGrid();
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    arg0.dismiss();
                    activity.setRequestedOrientation(prevOrientation);
                    return true;
                }
                return false;
            }
        });
        return dialog;
    }

    private class ButtonClickedListener implements DialogInterface.OnClickListener {
        private int clickedCode;

        public ButtonClickedListener(int code) {
            clickedCode = code;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (clickedCode) {
                case Utility.DISMISS:
                    activity.setRequestedOrientation(prevOrientation);
                    break;
                case Utility.CHANGE_COLOR:
                    activity.setRequestedOrientation(prevOrientation);
                    final int title = getArguments().getInt("title", 0);
                    final int preselect = getArguments().getInt("preselect", -1);
                    dismiss();
                    mCallback.onColorSelection(title, preselect);
                    break;
                case Utility.RESET_COLOR:
                    if (getArguments().getInt("title", 0) == R.string.primary_color) {
                        getArguments().putInt("preselect", getResources().getColor(R.color.theme_primary));
                    } else if (getArguments().getInt("title", 0) == R.string.accent_color) {
                        getArguments().putInt("preselect", getResources().getColor(R.color.theme_accent));
                    }
                    invalidateGrid();
                    break;
                default:
                    activity.setRequestedOrientation(prevOrientation);
                    break;
            }
        }
    }

    private void invalidateGrid() {
        if (mGrid.getAdapter() == null) {
            mGrid.setAdapter(new ColorGridAdapter());
            mGrid.setSelector(getResources().getDrawable(R.drawable.md_transparent));
        } else ((BaseAdapter) mGrid.getAdapter()).notifyDataSetChanged();
    }

    private class ColorGridAdapter extends BaseAdapter implements View.OnClickListener {

        public ColorGridAdapter() {
        }

        @Override
        public int getCount() {
            return mColors.length;
        }

        @Override
        public Object getItem(int position) {
            return mColors[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(activity).inflate(R.layout.griditem_color_chooser, null);
//            final boolean dark = ThemeUtils.isDarkMode(getActivity());
            CircleView child = (CircleView) convertView;
            child.setChecked(getArguments().getInt("preselect") == mColors[position]);
            child.setBackgroundColor(mColors[position]);
//            child.setBorderColor(dark ? Color.WHITE : Color.BLACK);
            child.setBorderColor(Color.BLACK);
            child.setTag(position);
            child.setOnClickListener(this);

            Drawable selector = createSelector(mColors[position]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_pressed}
                };
                int[] colors = new int[]{
                        shiftColorDown(mColors[position])
                };
                ColorStateList rippleColors = new ColorStateList(states, colors);
                child.setForeground(new RippleDrawable(rippleColors, selector, null));
            } else {
                child.setForeground(selector);
            }
            return convertView;
        }

        @Override
        public void onClick(View v) {
            final int index = (Integer) v.getTag();
            getArguments().putInt("preselect", mColors[index]);
            invalidateGrid();
        }
    }

    public static int shiftColorDown(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

    public static int shiftColorUp(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 1.1f; // value component
        return Color.HSVToColor(hsv);
    }

    public static int translucentColor(int color) {
        final float factor = 0.7f;
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    static Drawable createSelector(int color) {
        ShapeDrawable darkerCircle = new ShapeDrawable(new OvalShape());
        darkerCircle.getPaint().setColor(translucentColor(shiftColorDown(color)));
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, darkerCircle);
        return stateListDrawable;
    }

    public void show(ActionBarActivity context, int title, int preselect) {
        this.activity = context;
        Bundle args = new Bundle();
        args.putInt("preselect", preselect);
        args.putInt("title", title);
        setArguments(args);
        show(activity.getSupportFragmentManager(), "COLOR_SELECTOR");
    }
}

