package it.cammino.risuscito;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.utils.ThemeUtils;

public class GeneralIndex extends Fragment {

//    private ViewPager mViewPager;

    private static final String PAGE_VIEWED = "pageViewed";

    private MainActivity mMainActivity;

    @BindView(R.id.view_pager) ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.tabs_layout, container, false);
        ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();
        mMainActivity.setupToolbarTitle(R.string.title_activity_general_index);

        LUtils mLUtils = LUtils.getInstance(getActivity());

//        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));

//        final TabLayout tabs = (TabLayout) getActivity().findViewById(R.id.material_tabs);
        final TabLayout tabs = mMainActivity.mTabLayout;
        tabs.setVisibility(View.VISIBLE);
        if (!mMainActivity.isOnTablet()) {
            mMainActivity.enableFab(false);
            mMainActivity.enableBottombar(false);
        }
        if (savedInstanceState == null) {
            SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
            mViewPager.setCurrentItem(pref.getInt(Utility.DEFAULT_INDEX, 0));
        }
        else
            mViewPager.setCurrentItem(savedInstanceState.getInt(PAGE_VIEWED, 0));
        tabs.setBackgroundColor(getThemeUtils().primaryColor());
        tabs.setupWithViewPager(mViewPager);
        mLUtils.applyFontedTab(mViewPager, tabs);

        return rootView;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_VIEWED, mViewPager.getCurrentItem());
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AlphabeticSectionFragment();
                case 1:
                    return new NumericSectionFragment();
                case 2:
                    return new ArgumentsSectionFragment();
                case 3:
                    return new SalmiSectionFragment();
                case 4:
                    return new IndiceLiturgicoFragment();
                default:
                    return new AlphabeticSectionFragment();
            }
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = getActivity().getResources().getConfiguration().locale;
            switch (position) {
                case 0:
                    return getString(R.string.letter_order_text).toUpperCase(l);
                case 1:
                    return getString(R.string.page_order_text).toUpperCase(l);
                case 2:
                    return getString(R.string.arg_search_text).toUpperCase(l);
                case 3:
                    return getString(R.string.salmi_musica_index).toUpperCase(l);
                case 4:
                    return getString(R.string.indice_liturgico_index).toUpperCase(l);
            }
            return null;
        }
    }

    private ThemeUtils getThemeUtils() {
        return mMainActivity.getThemeUtils();
    }

}