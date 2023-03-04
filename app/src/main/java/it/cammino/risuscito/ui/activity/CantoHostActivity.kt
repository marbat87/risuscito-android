package it.cammino.risuscito.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import com.google.android.material.transition.platform.MaterialSharedAxis
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.ActivityFragmentHostBinding
import it.cammino.risuscito.ui.fragment.CantoFragment
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.viewmodels.PaginaRenderViewModel

class CantoHostActivity : ThemeableActivity() {

    private val viewModel: PaginaRenderViewModel by viewModels()
    private lateinit var binding: ActivityFragmentHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!OSUtils.isObySamsung()) {
            val enter = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
                duration = 700L
            }
            val exit = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
                duration = 700L
            }
            window.enterTransition = enter
            window.returnTransition = exit

            // Allow Activity A’s exit transition to play at the same time as this Activity’s
            // enter transition instead of playing them sequentially.
            window.allowEnterTransitionOverlap = true
        }
        super.onCreate(savedInstanceState)

        binding = ActivityFragmentHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val fragment = CantoFragment()
            if (viewModel.idCanto == 0) {
                viewModel.idCanto = this.intent.extras?.getInt(CantoFragment.ARG_ID_CANTO) ?: 0
                viewModel.pagina =
                    this.intent.extras?.getString(CantoFragment.ARG_NUM_PAGINA).orEmpty()
                viewModel.inActivity = true
            }

            fragment.arguments = bundleOf(
                CantoFragment.ARG_ID_CANTO to viewModel.idCanto,
                CantoFragment.ARG_NUM_PAGINA to viewModel.pagina,
                CantoFragment.ARG_ON_ACTIVITY to viewModel.inActivity
            )
            supportFragmentManager.commit {
                replace(
                    R.id.detail_fragment,
                    fragment,
                    R.id.canto_fragment.toString()
                )
            }
        }
    }

}