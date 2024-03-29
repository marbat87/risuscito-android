package it.cammino.risuscito.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.ActivityCantoBinding
import it.cammino.risuscito.ui.fragment.CantoFragment
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.viewmodels.PaginaRenderViewModel

class CantoHostActivity : ThemeableActivity() {

    private val viewModel: PaginaRenderViewModel by viewModels()
    private lateinit var binding: ActivityCantoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!OSUtils.isObySamsung()) {
            // Set the transition name, which matches Activity A’s start view transition name, on
            // the root view.
            findViewById<View>(android.R.id.content).transitionName = "shared_element_container"

            // Attach a callback used to receive the shared elements from Activity A to be
            // used by the container transform transition.
            setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
            setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())

            // Set this Activity’s enter and return transition to a MaterialContainerTransform
            window.sharedElementEnterTransition = MaterialContainerTransform().apply {
                addTarget(android.R.id.content)
                duration = 700L
            }

            // Keep system bars (status bar, navigation bar) persistent throughout the transition.
            window.sharedElementsUseOverlay = false
        }
        super.onCreate(savedInstanceState)

        binding = ActivityCantoBinding.inflate(layoutInflater)
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