package it.cammino.risuscito;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

import it.cammino.risuscito.utils.ThemeUtils;

public class GeneralIndex extends Fragment {

    private ViewPager mViewPager;
//    private TabPageIndicator mSlidingTabLayout = null;

    private static final String PAGE_VIEWED = "pageViewed";
    private LUtils mLUtils;
    private int lastPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.tabs_layout, container, false);
//        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_general_index);
//        ((TextView)((MainActivity) getActivity()).findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_general_index);
//        ((MainActivity) getActivity()).getSupportActionBar().setElevation(0);
        ((MainActivity) getActivity()).setupToolbar(rootView.findViewById(R.id.risuscito_toolbar), R.string.title_activity_general_index);

        mLUtils = LUtils.getInstance(getActivity());

        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));

//        TabPageIndicator mSlidingTabLayout = (TabPageIndicator) rootView.findViewById(R.id.sliding_tabs);
//        mSlidingTabLayout.setBackgroundColor(getThemeUtils().primaryColor());
//        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);

//        Resources res = getResources();
//        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(android.R.color.white));
//        mSlidingTabLayout.setDistributeEvenly(false);
//        mSlidingTabLayout.setViewPager(mViewPager);

        // Bind the tabs to the ViewPager
//        MaterialTabs tabs = (MaterialTabs) rootView.findViewById(R.id.material_tabs);
//        tabs.setBackgroundColor(getThemeUtils().primaryColor());
//        tabs.setViewPager(mViewPager);

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

//        Log.i(getClass().getName(), "ViewCompat.isLaidOut(tabs): " + ViewCompat.isLaidOut(tabs));
//        if (ViewCompat.isLaidOut(tabs)) {
//            tabs.setupWithViewPager(mViewPager);
//        } else
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            tabs.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//                @Override
//                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
//                    tabs.setupWithViewPager(mViewPager);
//                    mLUtils.applyFontedTab(mViewPager, tabs);
//                    tabs.removeOnLayoutChangeListener(this);
//                }
//            });
//        }
//        else {
//            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    tabs.setupWithViewPager(mViewPager);
//                    mLUtils.applyFontedTab(mViewPager, tabs);
//                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                }
//            });
//        }
        final Bundle bundle = savedInstanceState;
        final Runnable mMyRunnable = new Runnable() {
            @Override
            public void run() {
                if (bundle == null) {
                    SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
                    mViewPager.setCurrentItem(pref.getInt(Utility.DEFAULT_INDEX, 0));
                }
                else
                    tabs.getTabAt(bundle.getInt(PAGE_VIEWED, 0)).select();
            }
        };
        Handler myHandler = new Handler();
        myHandler.postDelayed(mMyRunnable, 200);
//        final Runnable mMyRunnable2 = new Runnable() {
//            @Override
//            public void run() {
//                if (bundle == null) {
//                    SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
//                    mViewPager.setCurrentItem(pref.getInt(Utility.DEFAULT_INDEX, 0));
//                }
//                else
//                    tabs.getTabAt(bundle.getInt(PAGE_VIEWED, 0)).select();
//            }
//        };
//        myHandler.postDelayed(mMyRunnable2, 400);

//        if (savedInstanceState == null) {
//            SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
//            mViewPager.setCurrentItem(pref.getInt(Utility.DEFAULT_INDEX, 0));
//        }
//        else {
//            mViewPager.setCurrentItem(savedInstanceState.getInt(PAGE_VIEWED, 0));
//        }

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
        return ((MainActivity)getActivity()).getThemeUtils();
    }

}