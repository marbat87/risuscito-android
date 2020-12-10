package it.cammino.risuscito

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.addCallback
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.android.material.elevation.ElevationOverlayProvider
import com.michaelflisar.changelog.ChangelogBuilder
import it.cammino.risuscito.databinding.ChangelogLayoutBinding
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils

class ChangelogActivity : ThemeableActivity() {

    private lateinit var binding: ChangelogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChangelogLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.risuscitoToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Utility.setupTransparentTints(this, Color.TRANSPARENT, hasNavDrawer, mViewModel.isOnTablet)

        ChangelogBuilder()
                .withUseBulletList(true) // true if you want to show bullets before each changelog row, false otherwise
                .buildAndSetup(binding.aboutText) // second parameter defines, if the dialog has a dark or light theme

        onBackPressedDispatcher.addCallback(this) {
            onBackPressedAction()
        }

        if (ThemeUtils.isDarkMode(this)) {
            val elevatedSurfaceColor = ElevationOverlayProvider(this).compositeOverlayWithThemeSurfaceColorIfNeeded(resources.getDimension(R.dimen.design_appbar_elevation))
            binding.collapsingToolbarLayout.setContentScrimColor(elevatedSurfaceColor)
            binding.appBarLayout.background = ColorDrawable(elevatedSurfaceColor)
        }
    }

    private fun onBackPressedAction() {
        Log.d(TAG, "onBackPressed: ")
        finish()
        Animatoo.animateSlideDown(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                Animatoo.animateSlideDown(this)
                true
            }
            else -> false
        }
    }

    companion object {
        private val TAG = ChangelogActivity::class.java.canonicalName
    }

}
