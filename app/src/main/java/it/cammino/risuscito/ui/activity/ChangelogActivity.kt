package it.cammino.risuscito.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.core.os.postDelayed
import androidx.core.view.isVisible
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.michaelflisar.changelog.ChangelogBuilder
import it.cammino.risuscito.databinding.ChangelogLayoutBinding
import it.cammino.risuscito.utils.OSUtils
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper
import it.cammino.risuscito.utils.extension.slideOutRight

class ChangelogActivity : ThemeableActivity() {

    private lateinit var binding: ChangelogLayoutBinding

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
        if (OSUtils.isObySamsung()) {
            finish()
            slideOutRight()
        } else
            finishAfterTransitionWrapper()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                binding.aboutText.isVisible = false
                finishAfterTransitionWrapper()
                slideOutRight()
                true
            }
            else -> false
        }
    }

    companion object {
        private val TAG = ChangelogActivity::class.java.canonicalName
    }

}
