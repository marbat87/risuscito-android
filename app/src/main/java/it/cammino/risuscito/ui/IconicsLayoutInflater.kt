@file:Suppress("DEPRECATION")
package it.cammino.risuscito.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.LayoutInflaterFactory

class IconicsLayoutInflater(
        private val appCompatDelegate: AppCompatDelegate
) : LayoutInflaterFactory {

    override fun onCreateView(
            parent: View?,
            name: String,
            context: Context,
            attrs: AttributeSet
    ): View? {
        val result = appCompatDelegate.createView(parent, name, context, attrs)
        return IconicsFactory.onViewCreated(result, context, attrs)
    }
}