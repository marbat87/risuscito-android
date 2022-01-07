package it.cammino.risuscito

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.michaelflisar.changelog.ChangelogBuilder
import it.cammino.risuscito.databinding.ChangelogLayoutBinding
import it.cammino.risuscito.ui.ThemeableActivity

class ChangelogActivity : ThemeableActivity() {

    private lateinit var binding: ChangelogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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

        super.onCreate(savedInstanceState)
        binding = ChangelogLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.risuscitoToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ChangelogBuilder()
            .withUseBulletList(true) // true if you want to show bullets before each changelog row, false otherwise
            .buildAndSetup(binding.aboutText) // second parameter defines, if the dialog has a dark or light theme

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

    }

    private fun onBackPressedAction() {
        Log.d(TAG, "onBackPressed: ")
        finishAfterTransition()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            else -> false
        }
    }

    companion object {
        private val TAG = ChangelogActivity::class.java.canonicalName
    }

}
