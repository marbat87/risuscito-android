package it.cammino.risuscito

import android.app.Activity
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.postDelayed
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.mikepenz.iconics.dsl.iconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import io.multifunctions.letCheckNull
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.android.synthetic.main.activity_pagina_render_fullscreen.*
import java.lang.ref.WeakReference

class PaginaRenderFullScreen : ThemeableActivity() {
    private var currentCanto: Canto? = null
    var speedValue: Int = 0
    private var scrollPlaying: Boolean = false
    var idCanto: Int = 0
    private var urlCanto: String? = null
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
        mLUtils = LUtils.getInstance(this)
        mLUtils?.goFullscreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_render_fullscreen)

        // recupera il numero della pagina da visualizzare dal parametro passato dalla chiamata
        val bundle = this.intent.extras
        urlCanto = bundle?.getString(Utility.URL_CANTO)
        speedValue = bundle?.getInt(Utility.SPEED_VALUE) ?: 0
        scrollPlaying = bundle?.getBoolean(Utility.SCROLL_PLAYING) ?: false
        idCanto = bundle?.getInt(Utility.ID_CANTO) ?: 0

//        val icon = IconicsDrawable(this, CommunityMaterial.Icon.cmd_fullscreen_exit)
//                .colorInt(Color.WHITE)
//                .sizeDp(24)
//                .paddingDp(2)
        val icon = iconicsDrawable(CommunityMaterial.Icon.cmd_fullscreen_exit) {
            color = colorInt(Color.WHITE)
            size = sizeDp(24)
            padding = sizeDp(2)
        }
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
        if (scrollPlaying)
            mScrollDown.run()

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
        currentCanto?.let {
            it.zoom = (cantoView.scale * 100).toInt()
            it.scrollX = cantoView.scrollX
            it.scrollY = cantoView.scrollY
            Log.d(TAG, "it.id ${it.id} / it.zoom ${it.zoom} / it.scrollX ${it.scrollX} / it.scrollY ${it.scrollY}")
            ZoomSaverTask(this, it).execute()
            return
        }
        finish()
        Animatoo.animateZoom(this)
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            view.postDelayed(500) {
                ZoomLoaderTask(this@PaginaRenderFullScreen).execute()
            }
            super.onPageFinished(view, url)
        }
    }

    companion object {
        private val TAG = PaginaRenderFullScreen::class.java.canonicalName

        private class ZoomSaverTask internal constructor(activity: Activity, private val canto: Canto) : AsyncTask<Void, Void, Int>() {

            private val activityReference: WeakReference<Activity> = WeakReference(activity)

            override fun doInBackground(vararg params: Void): Int? {
                activityReference.get()?.let {
                    val mDao = RisuscitoDatabase.getInstance(it).cantoDao()
                    Log.d(TAG, "canto.id ${canto.id} / canto.zoom ${canto.zoom} / canto.scrollX ${canto.scrollX} / canto.scrollY ${canto.scrollY}")
                    mDao.updateCanto(canto)
                }
                return 0
            }

            override fun onPostExecute(integer: Int?) {
                super.onPostExecute(integer)
                activityReference.get()?.let {
                    it.finish()
                    Animatoo.animateZoom(it)
                }
            }
        }

        private class ZoomLoaderTask internal constructor(activity: PaginaRenderFullScreen) : AsyncTask<Void, Void, Int>() {

            private val activityReference: WeakReference<PaginaRenderFullScreen> = WeakReference(activity)

            override fun doInBackground(vararg params: Void): Int? {
                activityReference.get()?.let {
                    val mDao = RisuscitoDatabase.getInstance(it).cantoDao()
                    it.currentCanto = mDao.getCantoById(it.idCanto)
                }
                return 0
            }

            override fun onPostExecute(integer: Int?) {
                super.onPostExecute(integer)
                val apiResult = Pair(activityReference.get(), activityReference.get()?.currentCanto)
                apiResult.letCheckNull { activity, canto ->
                    Log.d(TAG, "onPostExecute: ${canto.zoom} - ${canto.scrollX} - ${canto.scrollY}")
                    if (canto.zoom > 0) activity.cantoView.setInitialScale(canto.zoom)
                    if (canto.scrollX > 0 || canto.scrollY > 0)
                        activity.cantoView.scrollTo(canto.scrollX, canto.scrollY)
                }
            }
        }
    }
}
