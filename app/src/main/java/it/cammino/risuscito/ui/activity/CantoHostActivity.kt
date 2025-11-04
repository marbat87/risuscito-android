package it.cammino.risuscito.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.fragment.compose.AndroidFragment
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.fragment.CantoFragment
import it.cammino.risuscito.utils.extension.setEnterTransition
import it.cammino.risuscito.viewmodels.PaginaRenderViewModel

class CantoHostActivity : ThemeableActivity() {

    private val viewModel: PaginaRenderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setEnterTransition()
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
                    arguments = bundleOf(CantoFragment.ARG_ID_CANTO to viewModel.idCanto,
                        CantoFragment.ARG_NUM_PAGINA to viewModel.pagina,
                        CantoFragment.ARG_ON_ACTIVITY to viewModel.inActivity),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

    }

}