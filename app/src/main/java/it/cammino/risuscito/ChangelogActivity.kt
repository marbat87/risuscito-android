package it.cammino.risuscito

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.android.material.appbar.AppBarLayout
import com.michaelflisar.changelog.ChangelogBuilder
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.viewmodels.ChangelogViewModel
import kotlinx.android.synthetic.main.changelog_layout.*

class ChangelogActivity : ThemeableActivity(), AppBarLayout.OnOffsetChangedListener {

    private var mViewModel: ChangelogViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.changelog_layout)

        mViewModel = ViewModelProviders.of(this).get(ChangelogViewModel::class.java)

        setSupportActionBar(risuscito_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (mViewModel?.appBarIsExpanded == true)
            Utility.setupTransparentTints(this, Color.TRANSPARENT, false)
        else
            Utility.setupTransparentTints(
                    this, themeUtils.primaryColorDark(), false)

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

    public override fun onResume() {
        super.onResume()
        appbarlayout?.addOnOffsetChangedListener(this)
    }

    public override fun onStop() {
        super.onStop()
        appbarlayout?.removeOnOffsetChangedListener(this)
    }

    /**
     * Called when the [AppBarLayout]'s layout offset has been changed. This allows child views
     * to implement custom behavior based on the offset (for instance pinning a view at a certain y
     * value).
     *
     * @param appBarLayout the [AppBarLayout] which offset has changed
     * @param verticalOffset the vertical offset for the parent [AppBarLayout], in px
     */
    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        mViewModel?.appBarIsExpanded = verticalOffset >= -100
        if (mViewModel?.appBarIsExpanded == true)
            Utility.setupTransparentTints(
                    this,
                    Color.TRANSPARENT,
                    false)
        else
            Utility.setupTransparentTints(
                    this, themeUtils.primaryColorDark(), false)
    }

    companion object {
        private val TAG = ChangelogActivity::class.java.canonicalName
    }

}
