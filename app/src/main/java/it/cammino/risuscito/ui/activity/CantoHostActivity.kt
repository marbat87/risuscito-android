package it.cammino.risuscito.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.fragment.compose.AndroidFragment
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.fragment.CantoFragment
import it.cammino.risuscito.viewmodels.PaginaRenderViewModel

class CantoHostActivity : ThemeableActivity() {

    private val viewModel: PaginaRenderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
//        setEnterTransition()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (viewModel.idCanto == 0) {
            viewModel.idCanto = this.intent.extras?.getInt(CantoFragment.ARG_ID_CANTO) ?: 0
            viewModel.pagina =
                this.intent.extras?.getString(CantoFragment.ARG_NUM_PAGINA).orEmpty()
            viewModel.inActivity = true
        }

        setContent {
            RisuscitoTheme {
                AndroidFragment<CantoFragment>(
                    arguments = Bundle().apply {
                        putInt(CantoFragment.ARG_ID_CANTO, viewModel.idCanto)
                        putString(CantoFragment.ARG_NUM_PAGINA, viewModel.pagina)
                        putBoolean(CantoFragment.ARG_ON_ACTIVITY, viewModel.inActivity)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

    }

}