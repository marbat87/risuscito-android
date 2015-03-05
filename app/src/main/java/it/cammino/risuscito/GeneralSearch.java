package it.cammino.risuscito;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
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

    SlidingTabLayout mSlidingTabLayout = null;
  	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.activity_general_search, container, false);
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_search);
        ((MainActivity) getActivity()).getSupportActionBar().setElevation(0);

        ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
        
        mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setBackgroundColor(getThemeUtils().primaryColor());
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        
        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(android.R.color.white));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
	    
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
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					return getString(R.string.fast_search_title).toUpperCase(l);
				else
					return getString(R.string.fast_search_title);
			case 1:
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					return getString(R.string.advanced_search_title).toUpperCase(l);
				else
					return getString(R.string.advanced_search_title);
			default:
				return null;
			}
		}
	}

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).getThemeUtils();
    }
			
}