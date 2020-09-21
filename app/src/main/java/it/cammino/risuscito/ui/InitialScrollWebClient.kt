package it.cammino.risuscito.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.postDelayed
import it.cammino.risuscito.database.entities.Canto

open class InitialScrollWebClient(val canto: Canto?) : WebViewClient() {

    override fun onPageFinished(view: WebView, url: String) {
        view.postDelayed(600) {
            if ((canto?.scrollX
                            ?: 0) > 0 || (canto?.scrollY ?: 0) > 0)
                view.scrollTo(
                        canto?.scrollX
                                ?: 0, canto?.scrollY ?: 0)
        }
        super.onPageFinished(view, url)
    }

}