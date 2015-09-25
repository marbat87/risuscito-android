package it.cammino.risuscito;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import it.cammino.risuscito.utils.ThemeUtils;

public class GeneralSearch extends Fragment {

    //    TabPageIndicator mSlidingTabLayout = null;
    private LUtils mLUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_general_search, container, false);
//		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_search);
//		((TextView)((MainActivity) getActivity()).findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_search);
//		((MainActivity) getActivity()).getSupportActionBar().setElevation(0);
        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.title_activity_search);

        mLUtils = LUtils.getInstance(getActivity());

        final ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));

//        TabPageIndicator mSlidingTabLayout = (TabPageIndicator) rootView.findViewById(R.id.sliding_tabs);
//        mSlidingTabLayout.setBackgroundColor(getThemeUtils().primaryColor());
//        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
//
//        Resources res = getResources();
//        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(android.R.color.white));
//        mSlidingTabLayout.setDistributeEvenly(false);
//        mSlidingTabLayout.setViewPager(mViewPager);
        // Bind the tabs to the ViewPager
//		MaterialTabs tabs = (MaterialTabs) rootView.findViewById(R.id.material_tabs);
//		tabs.setBackgroundColor(getThemeUtils().primaryColor());
//		tabs.setViewPager(mViewPager);

        final TabLayout tabs = (TabLayout) rootView.findViewById(R.id.material_tabs);
        tabs.setBackgroundColor(getThemeUtils().primaryColor());
//        tabs.setupWithViewPager(mViewPager);
//        mLUtils.applyFontedTab(mViewPager, tabs);
        tabs.post(new Runnable() {
            @Override
            public void run() {
                tabs.setupWithViewPager(mViewPager);
                mLUtils.applyFontedTab(mViewPager, tabs);
            }
        });

//        final Runnable mMyRunnable = new Runnable() {
//            @Override
//            public void run() {
//                tabs.setupWithViewPager(mViewPager);
//                mLUtils.applyFontedTab(mViewPager, tabs);
//            }
//        };
//        Handler myHandler = new Handler();
//        myHandler.postDelayed(mMyRunnable, 200);

        return rootView;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RicercaVeloceFragment();
                case 1:
                    return new RicercaAvanzataFragment();
                default:
                    return new RicercaVeloceFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = getActivity().getResources().getConfiguration().locale;
            switch (position) {
                case 0:
//				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    return getString(R.string.fast_search_title).toUpperCase(l);
//				else
//					return getString(R.string.fast_search_title);
                case 1:
//				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    return getString(R.string.advanced_search_title).toUpperCase(l);
//				else
//					return getString(R.string.advanced_search_title);
                default:
                    return null;
            }
        }
    }

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }

}