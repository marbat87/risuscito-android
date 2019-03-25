package it.cammino.risuscito.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDelegate

class IconicsLayoutInflater2(
        private val appCompatDelegate: AppCompatDelegate
) : LayoutInflater.Factory2 {

    override fun onCreateView(
            parent: View?,
            name: String,
            context: Context,
            attrs: AttributeSet
    ): View? {
        val result = appCompatDelegate.createView(parent, name, context, attrs)
        return IconicsFactory.onViewCreated(result, context, attrs)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        val result = appCompatDelegate.createView(null, name, context, attrs)
        return IconicsFactory.onViewCreated(result, context, attrs)
    }
}