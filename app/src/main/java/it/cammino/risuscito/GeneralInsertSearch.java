package it.cammino.risuscito;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Locale;

import it.cammino.risuscito.ui.ThemeableActivity;

public class GeneralInsertSearch extends ThemeableActivity {

    private int fromAdd;
    private int idLista;
    private int listPosition;
    private LUtils mLUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_search);

        Toolbar toolbar = ((Toolbar) findViewById(R.id.risuscito_toolbar));
        toolbar.setTitle("");
        ((TextView)findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_inserisci_titolo);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setBackgroundColor(getThemeUtils().primaryColor());
        setSupportActionBar(toolbar);

        mLUtils = LUtils.getInstance(GeneralInsertSearch.this);

        Bundle bundle = GeneralInsertSearch.this.getIntent().getExtras();
        fromAdd = bundle.getInt("fromAdd");
        idLista = bundle.getInt("idLista");
        listPosition = bundle.getInt("position");

        final ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

        final TabLayout tabs = (TabLayout) findViewById(R.id.material_tabs);
        tabs.setBackgroundColor(getThemeUtils().primaryColor());
        tabs.setupWithViewPager(mViewPager);
        mLUtils.applyFontedTab(mViewPager, tabs);
//        tabs.setupWithViewPager(mViewPager);
//        tabs.post(new Runnable() {
//            @Override
//            public void run() {
//                tabs.setupWithViewPager(mViewPager);
//                mLUtils.applyFontedTab(mViewPager, tabs);
//            }
//        });

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
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            overridePendingTransition(0, R.anim.slide_out_right);
        }
        return super.onKeyUp(keyCode, event);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
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
            Locale l = getResources().getConfiguration().locale;
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