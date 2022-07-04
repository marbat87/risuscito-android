package it.cammino.risuscito.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.postDelayed
import androidx.core.view.isVisible
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.michaelflisar.changelog.ChangelogBuilder
import it.cammino.risuscito.databinding.ChangelogLayoutBinding
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper

class ChangelogActivity : ThemeableActivity() {

    private lateinit var binding: ChangelogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!OSUtils.isObySamsung()) {
            // Set the transition name, which matches Activity A’s start view transition name, on
            // the root view.
            findViewById<View>(android.R.id.content).transitionName = "shared_element_about"

            // Attach a callback used to receive the shared elements from Activity A to be
            // used by the container transform transition.
            setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

            // Set this Activity’s enter and return transition to a MaterialContainerTransform
            window.sharedElementEnterTransition = MaterialContainerTransform().apply {
                addTarget(android.R.id.content)
                duration = 700L
            }
            // Keep system bars (status bar, navigation bar) persistent throughout the transition.
            window.sharedElementsUseOverlay = false
        }

        super.onCreate(savedInstanceState)
        binding = ChangelogLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.risuscitoToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Handler(Looper.getMainLooper()).postDelayed(1000) {
            ChangelogBuilder()
                .withUseBulletList(true) // true if you want to show bullets before each changelog row, false otherwise
                .buildAndSetup(binding.aboutText) // second parameter defines, if the dialog has a dark or light theme
            binding.loadingBar.isVisible = false
        }

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

    }

    private fun onBackPressedAction() {
        Log.d(TAG, "onBackPressed: ")
        binding.aboutText.isVisible = false
        finishAfterTransitionWrapper()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                binding.aboutText.isVisible = false
                finishAfterTransitionWrapper()
                true
            }
            else -> false
        }
    }

    companion object {
        private val TAG = ChangelogActivity::class.java.canonicalName
    }

}
