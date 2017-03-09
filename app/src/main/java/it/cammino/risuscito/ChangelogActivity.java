package it.cammino.risuscito;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.ui.ThemeableActivity;


public class ChangelogActivity extends ThemeableActivity implements AppBarLayout.OnOffsetChangedListener {

    private boolean appBarIsExpanded = true;
    @BindView(R.id.appbarlayout) AppBarLayout mAppBarLayout;
    @BindView(R.id.risuscito_toolbar) Toolbar mToolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changelog_layout);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        collapsingToolbarLayout.setContentScrimColor(getThemeUtils().primaryColor());

        if (savedInstanceState != null)
            appBarIsExpanded = savedInstanceState.getBoolean("appBarIsExpanded", true);

        if (appBarIsExpanded)
            Utility.setupTransparentTints(ChangelogActivity.this, Color.TRANSPARENT, false);
        else
            Utility.setupTransparentTints(ChangelogActivity.this, getThemeUtils().primaryColorDark(), false);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(0, R.anim.slide_out_bottom);
            return true;
        }
        return  super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0, R.anim.slide_out_bottom);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("appBarIsExpanded", appBarIsExpanded);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAppBarLayout.removeOnOffsetChangedListener(this);
    }

    /**
     * Called when the {@link AppBarLayout}'s layout offset has been changed. This allows
     * child views to implement custom behavior based on the offset (for instance pinning a
     * view at a certain y value).
     *
     * @param appBarLayout   the {@link AppBarLayout} which offset has changed
     * @param verticalOffset the vertical offset for the parent {@link AppBarLayout}, in px
     */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        appBarIsExpanded = (verticalOffset >= -100);
        if (appBarIsExpanded)
            Utility.setupTransparentTints(ChangelogActivity.this, ContextCompat.getColor(ChangelogActivity.this, android.R.color.transparent), false);
        else
            Utility.setupTransparentTints(ChangelogActivity.this, getThemeUtils().primaryColorDark(), false);
    }
}
