package it.cammino.risuscito;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.ui.ThemeableActivity;

public class GeneralInsertSearch extends ThemeableActivity {

    private final String TAG = getClass().getCanonicalName();

    private int fromAdd;
    private int idLista;
    private int listPosition;

    @BindView(R.id.risuscito_toolbar) Toolbar mToolbar;
    @BindView(R.id.view_pager) ViewPager mViewPager;
    @BindView(R.id.material_tabs) TabLayout mTabLayout;
    @BindView(R.id.tabletToolbarBackground) @Nullable View mTabletBG;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_search);
        ButterKnife.bind(this);

//        Toolbar toolbar = ((Toolbar) findViewById(R.id.risuscito_toolbar));
        ((TextView)findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_inserisci_titolo);
        mToolbar.setBackgroundColor(getThemeUtils().primaryColor());
        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        LUtils mLUtils = LUtils.getInstance(GeneralInsertSearch.this);

        Bundle bundle = GeneralInsertSearch.this.getIntent().getExtras();
        fromAdd = bundle.getInt("fromAdd");
        idLista = bundle.getInt("idLista");
        listPosition = bundle.getInt("position");

//        final ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

        LUtils mLUtils = LUtils.getInstance(GeneralInsertSearch.this);
        if (mLUtils.isOnTablet() && mTabletBG != null)
            mTabletBG.setBackgroundColor(getThemeUtils().primaryColor());
        else
            mTabLayout.setBackgroundColor(getThemeUtils().primaryColor());
//        mTabLayout.setBackgroundColor(getThemeUtils().primaryColor());
        mTabLayout.setupWithViewPager(mViewPager);
//        mLUtils.applyFontedTab(mViewPager, mTabLayout);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(Activity.RESULT_CANCELED);
                finish();
                overridePendingTransition(0, R.anim.slide_out_right);
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        setResult(Activity.RESULT_CANCELED);
        finish();
        overridePendingTransition(0, R.anim.slide_out_right);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Bundle bundle = new Bundle();
                    bundle.putInt("fromAdd", fromAdd);
                    bundle.putInt("idLista", idLista);
                    bundle.putInt("position", listPosition);
                    InsertVeloceFragment insertVeloceFrag = new InsertVeloceFragment();
                    insertVeloceFrag.setArguments(bundle);
                    return insertVeloceFrag;
                case 1:
                    Bundle bundle1 = new Bundle();
                    bundle1.putInt("fromAdd", fromAdd);
                    bundle1.putInt("idLista", idLista);
                    bundle1.putInt("position", listPosition);
                    InsertAvanzataFragment insertAvanzataFrag = new InsertAvanzataFragment();
                    insertAvanzataFrag.setArguments(bundle1);
                    return insertAvanzataFrag;
                default:
                    Bundle bundle2 = new Bundle();
                    bundle2.putInt("fromAdd", fromAdd);
                    bundle2.putInt("idLista", idLista);
                    bundle2.putInt("position", listPosition);
                    InsertVeloceFragment insertVeloceFrag2 = new InsertVeloceFragment();
                    insertVeloceFrag2.setArguments(bundle2);
                    return insertVeloceFrag2;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            Locale l = getResources().getConfiguration().locale;
            Locale l = ThemeableActivity.getSystemLocalWrapper(getResources().getConfiguration());
            switch (position) {
                case 0:
                    return getString(R.string.fast_search_title).toUpperCase(l);
                case 1:
                    return getString(R.string.advanced_search_title).toUpperCase(l);
                default:
                    return "";
            }
        }
    }

}