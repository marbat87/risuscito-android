package it.cammino.risuscito.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.lifecycleScope
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.ui.composable.WebView
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.fragment.CantoFragment
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.checkScreenAwake
import it.cammino.risuscito.utils.extension.exitZoom
import it.cammino.risuscito.utils.extension.goFullscreen
import it.cammino.risuscito.viewmodels.PaginaRenderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaginaRenderFullScreen : AppCompatActivity() {

    private val paginaRenderViewModel: PaginaRenderViewModel by viewModels()

    private val htmlContent = mutableStateOf(StringUtils.EMPTY)
    private val initialScale = mutableIntStateOf(0)
    private val seekBarScrollValue = mutableFloatStateOf(0.02f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goFullscreen()

        setContent {

            RisuscitoTheme {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { onBackPressedAction() },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.fullscreen_exit_24px),
                                contentDescription = "Floating action button."
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { innerPadding ->
                    WebView(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        canto = paginaRenderViewModel.mCurrentCanto,
                        content = htmlContent.value,
                        initialScale = initialScale.intValue,
                        autoScroll = paginaRenderViewModel.scrollPlaying.value,
                        scrollSpeed = seekBarScrollValue.floatValue,
                        onScrollChange = { scrollX, scrollY ->
                            Log.d(TAG, "onScrollChange: $scrollX, $scrollY")
                            paginaRenderViewModel.scrollXValue = scrollX
                            paginaRenderViewModel.scrollYValue = scrollY
                        },
                        onZoomChange = {
                            Log.d(TAG, "onZoomChange: $it")
                            paginaRenderViewModel.zoomValue = it
                        }
                    )

                }
            }

            BackHandler {
                Log.d(TAG, "handleOnBackPressed")
                onBackPressedAction()
            }
        }

        // recupera il numero della pagina da visualizzare dal parametro passato dalla chiamata
        val bundle = this.intent.extras

        seekBarScrollValue.floatValue = (bundle?.getFloat(Utility.SPEED_VALUE) ?: 0f)
        paginaRenderViewModel.scrollPlaying.value =
            bundle?.getBoolean(Utility.SCROLL_PLAYING) == true
        initialScale.intValue = bundle?.getInt(Utility.ZOOM_VALUE) ?: 0
        paginaRenderViewModel.zoomValue = initialScale.intValue
        paginaRenderViewModel.scrollXValue = bundle?.getInt(Utility.SCROLL_X_VALUE) ?: 0
        paginaRenderViewModel.scrollYValue = bundle?.getInt(Utility.SCROLL_Y_VALUE) ?: 0
        paginaRenderViewModel.idCanto = bundle?.getInt(CantoFragment.ARG_ID_CANTO) ?: 0
        paginaRenderViewModel.mCurrentCanto = Canto().apply {
            scrollX = paginaRenderViewModel.scrollXValue
            scrollY = paginaRenderViewModel.scrollYValue
        }

        htmlContent.value = bundle?.getString(Utility.HTML_CONTENT).orEmpty()

        lifecycleScope.launch { loadCantoData() }

    }

    override fun onResume() {
        super.onResume()
        checkScreenAwake()
    }

    private fun onBackPressedAction() {
        lifecycleScope.launch { saveZoom() }
    }

    private suspend fun saveZoom() {
        paginaRenderViewModel.mCurrentCanto?.let {
            it.zoom = paginaRenderViewModel.zoomValue
            it.scrollX = paginaRenderViewModel.scrollXValue
            it.scrollY = paginaRenderViewModel.scrollYValue
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

    private suspend fun loadCantoData() {
        val mDao = RisuscitoDatabase.getInstance(this).cantoDao()
        paginaRenderViewModel.mCurrentCanto =
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.getCantoById(paginaRenderViewModel.idCanto)
            }

    }

    companion object {
        private val TAG = PaginaRenderFullScreen::class.java.canonicalName
    }
}
