package it.cammino.risuscito

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.postDelayed
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.colorInt
import com.mikepenz.iconics.paddingDp
import com.mikepenz.iconics.sizeDp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.android.synthetic.main.activity_pagina_render_fullscreen.*

class PaginaRenderFullScreen : ThemeableActivity() {
    private var currentCanto: Canto? = null
    private val mHandler = Handler()
    private val mScrollDown: Runnable = object : Runnable {
        override fun run() {
            try {
                cantoView.scrollBy(0, speedValue)
            } catch (e: NumberFormatException) {
                cantoView.scrollBy(0, 0)
            }

            mHandler.postDelayed(this, 700)
        }
    }
    private var mLUtils: LUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        mLUtils = LUtils.getInstance(this@PaginaRenderFullScreen)
        mLUtils!!.goFullscreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_render_fullscreen)

        // recupera il numero della pagina da visualizzare dal parametro passato dalla chiamata
        val bundle = this.intent.extras
        urlCanto = bundle!!.getString(Utility.URL_CANTO)
        speedValue = bundle.getInt(Utility.SPEED_VALUE)
        scrollPlaying = bundle.getBoolean(Utility.SCROLL_PLAYING)
        idCanto = bundle.getInt(Utility.ID_CANTO)

        val icon = IconicsDrawable(this@PaginaRenderFullScreen)
                .icon(CommunityMaterial.Icon.cmd_fullscreen_exit)
                .colorInt(Color.WHITE)
                .sizeDp(24)
                .paddingDp(2)
        fab_fullscreen_off.setImageDrawable(icon)
        fab_fullscreen_off.setOnClickListener { saveZoom() }
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        saveZoom()
    }

    public override fun onResume() {
        super.onResume()

        cantoView.loadUrl(urlCanto)
        if (scrollPlaying) {
            mScrollDown.run()
        }

        val webSettings = cantoView.settings
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)
        webSettings.loadWithOverviewMode = true

        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        cantoView.webViewClient = MyWebViewClient()
    }

    private fun saveZoom() {
        @Suppress("DEPRECATION")
        //aggiunto per evitare che la pagina venga chiusa troppo velocemente prima del caricamento del canto
        if (currentCanto != null) {
            currentCanto!!.zoom = (cantoView.scale * 100).toInt()
            currentCanto!!.scrollX = cantoView.scrollX
            currentCanto!!.scrollY = cantoView.scrollY
            ZoomSaverTask().execute()
        } else
            mLUtils!!.closeActivityWithFadeOut()
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            view.postDelayed(500) {
                ZoomLoaderTask().execute()
            }
            super.onPageFinished(view, url)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class ZoomSaverTask : AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg params: Void): Int? {
            val mDao = RisuscitoDatabase.getInstance(applicationContext).cantoDao()
            mDao.updateCanto(currentCanto!!)
            return 0
        }

        override fun onPostExecute(integer: Int?) {
            super.onPostExecute(integer)
            mLUtils!!.closeActivityWithFadeOut()
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class ZoomLoaderTask : AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg params: Void): Int? {
            val mDao = RisuscitoDatabase.getInstance(applicationContext).cantoDao()
            currentCanto = mDao.getCantoById(idCanto)
            return 0
        }

        override fun onPostExecute(integer: Int?) {
            super.onPostExecute(integer)
            Log.d(TAG, "onPostExecute: " + currentCanto!!.zoom + " - " + currentCanto!!.scrollX + " - " + currentCanto!!.scrollY)
            if (currentCanto!!.zoom > 0) cantoView.setInitialScale(currentCanto!!.zoom)
            if (currentCanto!!.scrollX > 0 || currentCanto!!.scrollY > 0)
                cantoView.scrollTo(currentCanto!!.scrollX, currentCanto!!.scrollY)
        }
    }

    companion object {
        private val TAG = PaginaRenderFullScreen::class.java.canonicalName
        var speedValue: Int = 0
        var scrollPlaying: Boolean = false
        var idCanto: Int = 0
        private var urlCanto: String? = null
    }
}
