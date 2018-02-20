package it.cammino.risuscito.preferences

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.color.ColorChooserDialog
import it.cammino.risuscito.MainActivity
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.ColorPalette

class ColorPickerPreference private constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : Preference(context, attrs, defStyleAttr) {
    private var mCurrentValue: Int = 0

    private val prefActivity: MainActivity?
        get() {
            val context = context
            if (context is ContextThemeWrapper) {
                if (context.baseContext is MainActivity) {
                    return context.baseContext as MainActivity
                }
            } else if (context is MainActivity) {
                return context
            }
            return null
        }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    init {
        Log.d(TAG, "ColorPickerPreference: " + context)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        Log.d(TAG, "onBindViewHolder: mCurrentValue = " + mCurrentValue)
        super.onBindViewHolder(holder)
        val mColorCircle = holder.findViewById(R.id.color_circle)
        setColorViewValue(mColorCircle, mCurrentValue)
        holder.itemView.isClickable = true // disable parent click
        holder.itemView.setOnClickListener {
            ColorChooserDialog.Builder(prefActivity!!, if (title == prefActivity!!.getString(R.string.primary_color)) R.string.primary_color else R.string.accent_color)
                    .allowUserColorInput(false)
                    .customColors(if (title == prefActivity!!.getString(R.string.primary_color)) ColorPalette.PRIMARY_COLORS else ColorPalette.ACCENT_COLORS, if (title == prefActivity!!.getString(R.string.primary_color)) ColorPalette.PRIMARY_COLORS_SUB else ColorPalette.ACCENT_COLORS_SUB)
                    .doneButton(android.R.string.ok)  // changes label of the done button
                    .cancelButton(android.R.string.cancel)  // changes label of the cancel button
                    .backButton(R.string.dialog_back)  // changes label of the back button
                    .preselect(mCurrentValue)  // optional color int, preselects a color
                    .accentMode(title == prefActivity!!.getString(R.string.accent_color))
                    .show(prefActivity!!)
        }
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        Log.d(TAG, "onGetDefaultValue: " + a!!.getInteger(index, DEFAULT_VALUE).toString())
        return a.getInteger(index, DEFAULT_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        Log.d(TAG, "onSetInitialValue: ")
        if (restorePersistedValue) {
            // Restore existing state
            mCurrentValue = this.getPersistedInt(if (title == prefActivity!!.getString(R.string.primary_color)) prefActivity!!.themeUtils!!.primaryColor() else prefActivity!!.themeUtils!!.accentColor())
        } else {
            // Set default state from the XML attribute
            mCurrentValue = (defaultValue as Int?)!!
            persistInt(mCurrentValue)
        }
        Log.d(TAG, "onSetInitialValue: " + mCurrentValue)
    }

    companion object {
        private val TAG = ColorPickerPreference::class.java.canonicalName
        private val DEFAULT_VALUE = Color.parseColor("#000000")

        private fun setColorViewValue(view: View, color: Int) {
            if (view is ImageView) {
                val res = view.context.resources

                val currentDrawable = view.drawable
                val colorChoiceDrawable: GradientDrawable
                if (currentDrawable != null && currentDrawable is GradientDrawable) {
                    // Reuse drawable
                    colorChoiceDrawable = currentDrawable
                } else {
                    colorChoiceDrawable = GradientDrawable()
                    colorChoiceDrawable.shape = GradientDrawable.OVAL
                }

                // Set stroke to dark version of color
                val darkenedColor = Color.rgb(
                        Color.red(color) * 192 / 256,
                        Color.green(color) * 192 / 256,
                        Color.blue(color) * 192 / 256)

                colorChoiceDrawable.setColor(color)
                colorChoiceDrawable.setStroke(TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 1f, res.displayMetrics).toInt(), darkenedColor)
                view.setImageDrawable(colorChoiceDrawable)

            } else (view as? TextView)?.setTextColor(color)
        }
    }

}