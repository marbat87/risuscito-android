package it.cammino.risuscito.ui

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText


fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

/**
 * Add ability to clear input with right button
 *
 * Arguments:
 *  @param onIsNotEmpty - callback which is invoked when input is completed and is not empty. Is good for clearing error
 *  @param onCanceled - callbacks which is invoked when cancel button is clicked and input is cleared
 *  @param clearDrawable - right drawable which is used as cancel button to clear input
 */
fun TextInputEditText.makeClearableEditText(
        onIsNotEmpty: (() -> Unit)?,
        onCanceled: (() -> Unit)?,
        clearDrawable: Drawable
) {
    val updateRightDrawable = {
        this.setCompoundDrawables(null, null,
                if (text!!.isNotEmpty()) clearDrawable else null,
                null)
    }
    updateRightDrawable()

    this.afterTextChanged {
        if (it.isNotEmpty()) {
            onIsNotEmpty?.invoke()
        }
        updateRightDrawable()
    }
    this.onRightDrawableClicked {
        this.text!!.clear()
        this.setCompoundDrawables(null, null, null, null)
        onCanceled?.invoke()
        this.requestFocus()
    }
}

private const val COMPOUND_DRAWABLE_RIGHT_INDEX = 2

/**
 *
 * Calculate right compound drawable and in case it exists calls
 * @see TextInputEditText.makeClearableEditText
 *
 * Arguments:
 *  @param onIsNotEmpty - callback which is invoked when input is completed and is not empty. Is good for clearing error
 *  @param onCanceled - callbacks which is invoked when cancel button is clicked and input is cleared
 */
@Suppress("unused")
fun TextInputEditText.makeClearableEditText(onIsNotEmpty: (() -> Unit)?, onCanceled: (() -> Unit)?) {
    compoundDrawables[COMPOUND_DRAWABLE_RIGHT_INDEX]?.let { clearDrawable ->
        makeClearableEditText(onIsNotEmpty, onCanceled, clearDrawable)
    }
}

/**
 * Based on View.OnTouchListener. Be careful EditText replaces old View.OnTouchListener when setting new one
 */
@SuppressLint("ClickableViewAccessibility")
fun TextInputEditText.onRightDrawableClicked(onClicked: (view: TextInputEditText) -> Unit) {
    this.setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is TextInputEditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}