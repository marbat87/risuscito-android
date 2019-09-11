package it.cammino.risuscito

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.michaelflisar.changelog.ChangelogBuilder
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.android.synthetic.main.changelog_layout.*

class ChangelogActivity : ThemeableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.changelog_layout)

        setSupportActionBar(risuscito_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Utility.setupTransparentTints(this, Color.TRANSPARENT, hasNavDrawer, isOnTablet)

        ChangelogBuilder()
                .withUseBulletList(true) // true if you want to show bullets before each changelog row, false otherwise
                .buildAndSetup(aboutText) // second parameter defines, if the dialog has a dark or light theme
    }

    override fun onBackPressed() {
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
