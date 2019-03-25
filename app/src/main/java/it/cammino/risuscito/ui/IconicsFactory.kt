package it.cammino.risuscito.ui

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.animation.tryToEnableIconicsAnimation
import com.mikepenz.iconics.context.IconicsAttrsApplier
import it.cammino.risuscito.R

internal object IconicsFactory {

    @JvmStatic
    fun onViewCreated(view: View?, context: Context, attrs: AttributeSet): View? {
        if (view != null && view.getTag(R.id.iconics_tag_id) != true) {
            onViewCreatedInternal(view, context, attrs)
            view.setTag(R.id.iconics_tag_id, true)
        }
        return view
    }

    @JvmStatic
    @SuppressLint("RestrictedApi")
    private fun onViewCreatedInternal(view: View, context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }

        when (view) {
            is ActionMenuItemView -> {
                IconicsAttrsApplier.getIconicsDrawable(context, attrs)?.let {
                    view.setIcon(view.tryToEnableIconicsAnimation(it))
                }
            }
            is EditText -> {
                //for an editText we only style initial as styling the Editable causes problems!
                Iconics.Builder().on(view as TextView).build()
            }
            is TextView -> {
                //handle iconics
                Iconics.Builder().on(view).build()

                view.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(cs: CharSequence, i: Int, i1: Int, i2: Int) {
                    }

                    override fun onTextChanged(cs: CharSequence, i: Int, i1: Int, i2: Int) {
                    }

                    override fun afterTextChanged(editable: Editable) {
                        Iconics.styleEditable(editable)
                    }
                })
            }
            is ImageView -> {
                IconicsAttrsApplier.getIconicsDrawable(context, attrs)?.let {
                    view.setImageDrawable(view.tryToEnableIconicsAnimation(it))
                }
            }
        }
    }
}