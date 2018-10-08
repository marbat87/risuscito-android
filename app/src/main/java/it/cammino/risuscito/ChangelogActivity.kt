package it.cammino.risuscito

import androidx.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.MenuItem
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

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        collapsingToolbarLayout!!.setContentScrimColor(themeUtils!!.primaryColor())

        if (mViewModel!!.appBarIsExpanded)
            Utility.setupTransparentTints(this@ChangelogActivity, Color.TRANSPARENT, false)
        else
            Utility.setupTransparentTints(
                    this@ChangelogActivity, themeUtils!!.primaryColorDark(), false)
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        finish()
        overridePendingTransition(0, R.anim.slide_out_bottom)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(0, R.anim.slide_out_bottom)
                true
            }
            else -> false
        }
    }

    public override fun onResume() {
        super.onResume()
        appbarlayout!!.addOnOffsetChangedListener(this)
    }

    public override fun onStop() {
        super.onStop()
        appbarlayout!!.removeOnOffsetChangedListener(this)
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
        mViewModel!!.appBarIsExpanded = verticalOffset >= -100
        if (mViewModel!!.appBarIsExpanded)
            Utility.setupTransparentTints(
                    this@ChangelogActivity,
                    ContextCompat.getColor(this@ChangelogActivity, android.R.color.transparent),
                    false)
        else
            Utility.setupTransparentTints(
                    this@ChangelogActivity, themeUtils!!.primaryColorDark(), false)
    }

    companion object {
        private val TAG = ChangelogActivity::class.java.canonicalName
    }

}
