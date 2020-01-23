package it.cammino.risuscito.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class LollipopFixedWebView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : WebView(context.createConfigurationContext(Configuration()), attrs, defStyleAttr, defStyleRes)