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

import butterknife.BindView;
import butterknife.ButterKnife;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.utils.ThemeUtils;

public class GeneralSearch extends Fragment {

    private MainActivity mMainActivity;

    @BindView(R.id.view_pager) ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_general_search, container, false);
        ButterKnife.bind(this, rootView);

        mMainActivity = (MainActivity) getActivity();

        mMainActivity.setupToolbarTitle(R.string.title_activity_search);

        LUtils mLUtils = LUtils.getInstance(getActivity());

//        final ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));

//        final TabLayout tabs = (TabLayout) getActivity().findViewById(R.id.material_tabs);
        final TabLayout tabs = mMainActivity.mTabLayout;
        tabs.setVisibility(View.VISIBLE);
        if (!mMainActivity.isOnTablet()) {
            mMainActivity.enableFab(false);
            mMainActivity.enableBottombar(false);
        }
        tabs.setBackgroundColor(getThemeUtils().primaryColor());
        tabs.setupWithViewPager(mViewPager);
        mLUtils.applyFontedTab(mViewPager, tabs);

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
//            Locale l = getActivity().getResources().getConfiguration().locale;
            Locale l = ThemeableActivity.getSystemLocalWrapper(getActivity().getResources().getConfiguration());
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

    private ThemeUtils getThemeUtils() {
        return mMainActivity.getThemeUtils();
    }

}