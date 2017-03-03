package it.cammino.risuscito.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.color.ColorChooserDialog;

import it.cammino.risuscito.MainActivity;
import it.cammino.risuscito.R;
import it.cammino.risuscito.utils.ColorPalette;

public class ColorPickerPreference extends Preference {
    int mCurrentValue;
//    MainActivity mContext;
    static final int DEFAULT_VALUE = Color.parseColor("#000000");
    int mDefaultValue;

    private final String TAG = getClass().getCanonicalName();
    
    public ColorPickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "ColorPickerPreference: " + context);
//        if (context instanceof ContextThemeWrapper)
//        {
//            Log.d(TAG, "ColorPickerPreference: ContextThemeWrapper");
//            if (((ContextThemeWrapper) context).getBaseContext() instanceof MainActivity) {
//                Log.d(TAG, "ColorPickerPreference: MainActivity1");
//                mContext = (MainActivity) ((ContextThemeWrapper) context).getBaseContext();
//            }
//        }
//        else if (context instanceof MainActivity) {
//            Log.d(TAG, "ColorPickerPreference: MainActivity2");
//            mContext = (MainActivity) context;
//        }
//        setWidgetLayoutResource(R.layout.color_prefence);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        Log.d(TAG, "onBindViewHolder: mCurrentValue = " + mCurrentValue);
        super.onBindViewHolder(holder);
        final View mColorCircle = holder.findViewById(R.id.color_circle);
        setColorViewValue(mColorCircle, mCurrentValue);
        holder.itemView.setClickable(true); // disable parent click
//        View mPreference = holder.findViewById(android.R.id.widget_frame);
//        mPreference.setClickable(true); // disable parent click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorChooserDialog.Builder(getPrefActivity(), getTitle().equals(getPrefActivity().getString(R.string.primary_color)) ? R.string.primary_color : R.string.accent_color)
                        .allowUserColorInput(false)
                        .customColors(getTitle().equals(getPrefActivity().getString(R.string.primary_color)) ? ColorPalette.PRIMARY_COLORS : ColorPalette.ACCENT_COLORS
                                , getTitle().equals(getPrefActivity().getString(R.string.primary_color)) ? ColorPalette.PRIMARY_COLORS_SUB : ColorPalette.ACCENT_COLORS_SUB)
                        .doneButton(R.string.single_choice_ok)  // changes label of the done button
                        .cancelButton(R.string.cancel)  // changes label of the cancel button
                        .backButton(R.string.dialog_back)  // changes label of the back button
                        .preselect(mCurrentValue)  // optional color int, preselects a color
                        .accentMode(getTitle().equals(getPrefActivity().getString(R.string.accent_color)))
                        .show();
            }
        });
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Log.d(TAG, "onGetDefaultValue: " + String.valueOf(a.getInteger(index, DEFAULT_VALUE)));
//        return getTitle().equals(getPrefActivity().getString(R.string.primary_color))? getPrefActivity().getThemeUtils().primaryColor() : getPrefActivity().getThemeUtils().accentColor();
        return a.getInteger(index, DEFAULT_VALUE);
//        return ContextCompat.getColor(getContext(), getTitle().equals(mContext.getString(R.string.primary_color)) ? R.color.theme_primary : R.color.theme_accent);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        Log.d(TAG, "onSetInitialValue: ");
        if (restorePersistedValue) {
            // Restore existing state
            mCurrentValue = this.getPersistedInt(getTitle().equals(getPrefActivity().getString(R.string.primary_color))? getPrefActivity().getThemeUtils().primaryColor() : getPrefActivity().getThemeUtils().accentColor());
        } else {
            // Set default state from the XML attribute
            mCurrentValue = (Integer) defaultValue;
            persistInt(mCurrentValue);
        }
        Log.d(TAG, "onSetInitialValue: " + mCurrentValue);
    }

//    @Override
//    protected Parcelable onSaveInstanceState() {
//        final Parcelable superState = super.onSaveInstanceState();
//        // Check whether this Preference is persistent (continually saved)
//        if (isPersistent()) {
//            // No need to save instance state since it's persistent,
//            // use superclass state
//            return superState;
//        }
//
//        // Create instance of custom BaseSavedState
//        final SavedState myState = new SavedState(superState);
//        // Set the state's value with the class member that holds current
//        // setting value
//        myState.value = mNewValue;
//        return myState;
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        // Check whether we saved the state in onSaveInstanceState
//        if (state == null || !state.getClass().equals(SavedState.class)) {
//            // Didn't save the state, so call superclass
//            super.onRestoreInstanceState(state);
//            return;
//        }
//
//        // Cast state to custom BaseSavedState and pass to superclass
//        SavedState myState = (SavedState) state;
//        super.onRestoreInstanceState(myState.getSuperState());
//
//        // Set this Preference's widget to reflect the restored state
////        mNumberPicker.setValue(myState.value);
//    }
//
//    private static class SavedState extends BaseSavedState {
//        // Member that holds the setting's value
//        // Change this data type to match the type saved by your Preference
//        int value;
//
//        public SavedState(Parcelable superState) {
//            super(superState);
//        }
//
//        public SavedState(Parcel source) {
//            super(source);
//            // Get the current preference's value
//            value = source.readInt();  // Change this to read the appropriate data type
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            super.writeToParcel(dest, flags);
//            // Write the preference's value
//            dest.writeInt(value);  // Change this to write the appropriate data type
//        }
//
//        // Standard creator object using an instance of this class
//        public static final Parcelable.Creator<SavedState> CREATOR =
//                new Parcelable.Creator<SavedState>() {
//
//                    public SavedState createFromParcel(Parcel in) {
//                        return new SavedState(in);
//                    }
//
//                    public SavedState[] newArray(int size) {
//                        return new SavedState[size];
//                    }
//                };
//    }

    private static void setColorViewValue(View view, int color) {
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            Resources res = imageView.getContext().getResources();

            Drawable currentDrawable = imageView.getDrawable();
            GradientDrawable colorChoiceDrawable;
            if (currentDrawable != null && currentDrawable instanceof GradientDrawable) {
                // Reuse drawable
                colorChoiceDrawable = (GradientDrawable) currentDrawable;
            } else {
                colorChoiceDrawable = new GradientDrawable();
                colorChoiceDrawable.setShape(GradientDrawable.OVAL);
            }

            // Set stroke to dark version of color
            int darkenedColor = Color.rgb(
                    Color.red(color) * 192 / 256,
                    Color.green(color) * 192 / 256,
                    Color.blue(color) * 192 / 256);

            colorChoiceDrawable.setColor(color);
            colorChoiceDrawable.setStroke((int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics()), darkenedColor);
            imageView.setImageDrawable(colorChoiceDrawable);

        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
    }

    public MainActivity getPrefActivity() {
        Context context = getContext();
        if (context instanceof ContextThemeWrapper)
        {
//            Log.d(TAG, "ColorPickerPreference: ContextThemeWrapper");
            if (((ContextThemeWrapper) context).getBaseContext() instanceof MainActivity) {
//                Log.d(TAG, "ColorPickerPreference: MainActivity1");
                return (MainActivity) ((ContextThemeWrapper) context).getBaseContext();
            }
        }
        else if (context instanceof MainActivity) {
//            Log.d(TAG, "ColorPickerPreference: MainActivity2");
            return (MainActivity) context;
        }
        return null;
    }

}