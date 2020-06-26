package it.cammino.risuscito

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.databinding.ActivityPaginaRenderFullscreenBinding
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.coroutines.*

class PaginaRenderFullScreen : ThemeableActivity() {

    private var currentCanto: Canto? = null
    var speedValue: Int = 0
    private var scrollPlaying: Boolean = false
    var idCanto: Int = 0
    private lateinit var urlCanto: String
    private val mHandler = Handler(Looper.getMainLooper())
    private val mScrollDown: Runnable = object : Runnable {
        override fun run() {
            try {
                binding.cantoView.scrollBy(0, speedValue)
            } catch (e: NumberFormatException) {
                binding.cantoView.scrollBy(0, 0)
            }

            mHandler.postDelayed(this, 700)
        }
    }
    private var mLUtils: LUtils? = null

    private lateinit var binding: ActivityPaginaRenderFullscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        mLUtils = LUtils.getInstance(this)
        mLUtils?.goFullscreen()
        super.onCreate(savedInstanceState)
        binding = ActivityPaginaRenderFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // recupera il numero della pagina da visualizzare dal parametro passato dalla chiamata
        val bundle = this.intent.extras
        urlCanto = bundle?.getString(Utility.URL_CANTO) ?: ""
        speedValue = bundle?.getInt(Utility.SPEED_VALUE) ?: 0
        scrollPlaying = bundle?.getBoolean(Utility.SCROLL_PLAYING) ?: false
        idCanto = bundle?.getInt(Utility.ID_CANTO) ?: 0

        val icon = IconicsDrawable(this, CommunityMaterial.Icon.cmd_fullscreen_exit).apply {
            colorInt = Color.WHITE
            sizeDp = 24
            paddingDp = 2
        }
        binding.fabFullscreenOff.setImageDrawable(icon)
        binding.fabFullscreenOff.setOnClickListener { lifecycleScope.launch { saveZoom() } }

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

    }

    private fun onBackPressedAction() {
        Log.d(TAG, "onBackPressed: ")
        lifecycleScope.launch { saveZoom() }
    }

    public override fun onResume() {
        super.onResume()

        binding.cantoView.loadUrl(urlCanto)
        if (scrollPlaying)
            mScrollDown.run()

        val webSettings = binding.cantoView.settings
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)
        webSettings.loadWithOverviewMode = true

        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        binding.cantoView.webViewClient = MyWebViewClient()
    }

    private suspend fun saveZoom() {
        @Suppress("DEPRECATION")
        currentCanto?.let {
            it.zoom = (binding.cantoView.scale * 100).toInt()
            it.scrollX = binding.cantoView.scrollX
            it.scrollY = binding.cantoView.scrollY
            Log.d(TAG, "it.id ${it.id} / it.zoom ${it.zoom} / it.scrollX ${it.scrollX} / it.scrollY ${it.scrollY}")
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                RisuscitoDatabase.getInstance(applicationContext).cantoDao().updateCanto(it)
            }
        }
        finish()
        Animatoo.animateZoom(this)
    }

    private suspend fun loadZoom() {
        val mDao = RisuscitoDatabase.getInstance(this).cantoDao()
        currentCanto = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.getCantoById(idCanto)
        }
        delay(500)
        currentCanto?.let {
            if (it.zoom > 0) binding.cantoView.setInitialScale(it.zoom)
            if (it.scrollX > 0 || it.scrollY > 0)
                binding.cantoView.scrollTo(it.scrollX, it.scrollY)
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            lifecycleScope.launch { loadZoom() }
            super.onPageFinished(view, url)
        }
    }

    companion object {
        private val TAG = PaginaRenderFullScreen::class.java.canonicalName
    }
}
