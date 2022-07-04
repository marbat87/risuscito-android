package it.cammino.risuscito.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.databinding.ActivityPaginaRenderFullscreenBinding
import it.cammino.risuscito.ui.InitialScrollWebClient
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.exitZoom
import it.cammino.risuscito.utils.extension.goFullscreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

class PaginaRenderFullScreen : ThemeableActivity() {

    private var currentCanto: Canto? = null
    var speedValue: Int = 0
    private var scrollPlaying: Boolean = false
    var idCanto: Int = 0
    private lateinit var htmlContent: String
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
    private lateinit var binding: ActivityPaginaRenderFullscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goFullscreen()
        binding = ActivityPaginaRenderFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // recupera il numero della pagina da visualizzare dal parametro passato dalla chiamata
        val bundle = this.intent.extras
        htmlContent = bundle?.getString(Utility.HTML_CONTENT).orEmpty()
        speedValue = bundle?.getInt(Utility.SPEED_VALUE) ?: 0
        scrollPlaying = bundle?.getBoolean(Utility.SCROLL_PLAYING) ?: false
        idCanto = bundle?.getInt(Utility.ID_CANTO) ?: 0

        binding.fabFullscreenOff.setOnClickListener { lifecycleScope.launch { saveZoom() } }

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

        lifecycleScope.launch { loadWebView() }

    }

    private fun onBackPressedAction() {
        Log.d(TAG, "onBackPressed: ")
        lifecycleScope.launch { saveZoom() }
    }

    private suspend fun saveZoom() {
        @Suppress("DEPRECATION")
        currentCanto?.let {
            it.zoom = (binding.cantoView.scale * 100).toInt()
            it.scrollX = binding.cantoView.scrollX
            it.scrollY = binding.cantoView.scrollY
            Log.d(
                TAG,
                "it.id ${it.id} / it.zoom ${it.zoom} / it.scrollX ${it.scrollX} / it.scrollY ${it.scrollY}"
            )
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                RisuscitoDatabase.getInstance(applicationContext).cantoDao().updateCanto(it)
            }
        }
        finish()
        exitZoom()
    }

    private suspend fun loadWebView() {
        val mDao = RisuscitoDatabase.getInstance(this).cantoDao()
        currentCanto = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.getCantoById(idCanto)
        }

        loadContentIntoWebView(htmlContent)

        val webSettings = binding.cantoView.settings
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)
        webSettings.loadWithOverviewMode = true

        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        currentCanto?.let {
            if (it.zoom > 0) binding.cantoView.setInitialScale(it.zoom)
        }

        binding.cantoView.webViewClient = InitialScrollWebClient(currentCanto)

        if (scrollPlaying)
            mScrollDown.run()

    }

    private fun loadContentIntoWebView(content: String?) {
        if (!content.isNullOrEmpty()) binding.cantoView.loadData(
            Base64.encodeToString(
                content.toByteArray(Charset.forName(ECONDING_UTF8)),
                Base64.DEFAULT
            ), DEFAULT_MIME_TYPE, ECONDING_BASE64
        )
    }

    companion object {
        private val TAG = PaginaRenderFullScreen::class.java.canonicalName
        private const val DEFAULT_MIME_TYPE = "text/html; charset=utf-8"
        private const val ECONDING_UTF8 = "utf-8"
        private const val ECONDING_BASE64 = "base64"
    }
}
