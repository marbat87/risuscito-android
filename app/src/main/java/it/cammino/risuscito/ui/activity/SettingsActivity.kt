package it.cammino.risuscito.ui.activity

import android.os.Bundle
import androidx.activity.addCallback
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.SettingsActivityFragmentHostBinding
import it.cammino.risuscito.ui.fragment.SettingsFragment
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper
import it.cammino.risuscito.utils.extension.setEnterTransition
import it.cammino.risuscito.utils.extension.slideOutRight

class SettingsActivity : ThemeableActivity() {

    private lateinit var binding: SettingsActivityFragmentHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setEnterTransition()
        super.onCreate(savedInstanceState)
        binding = SettingsActivityFragmentHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.risuscitoToolbar)
        supportActionBar?.setTitle(R.string.title_activity_settings)

        binding.risuscitoToolbar.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.risuscitoToolbar.setNavigationOnClickListener {
            onBackPressedAction()
        }

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.detail_fragment, SettingsFragment())
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
