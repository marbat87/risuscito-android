package it.cammino.risuscito.ui.activity

import android.os.Bundle
import androidx.activity.addCallback
import com.google.android.material.transition.platform.MaterialSharedAxis
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.SettingsActivityFragmentHostBinding
import it.cammino.risuscito.ui.fragment.AboutFragment
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper
import it.cammino.risuscito.utils.extension.slideOutRight

class AboutActivity : ThemeableActivity() {

    private lateinit var binding: SettingsActivityFragmentHostBinding

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
            window.reenterTransition = exit
            window.exitTransition = enter

            // Allow Activity A’s exit transition to play at the same time as this Activity’s
            // enter transition instead of playing them sequentially.
            window.allowEnterTransitionOverlap = true
        }

        super.onCreate(savedInstanceState)
        binding = SettingsActivityFragmentHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.risuscitoToolbar)
        supportActionBar?.setTitle(R.string.title_activity_about)

        binding.risuscitoToolbar.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.risuscitoToolbar.setNavigationOnClickListener {
            onBackPressedAction()
        }

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.detail_fragment, AboutFragment())
            .commit()
    }

    private fun onBackPressedAction() {
        if (OSUtils.isObySamsung()) {
            finish()
            slideOutRight()
        } else
            finishAfterTransitionWrapper()
    }
}
