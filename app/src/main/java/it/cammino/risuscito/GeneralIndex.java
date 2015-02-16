package it.cammino.risuscito;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	SlidingTabLayout mSlidingTabLayout = null;

	private static final String PAGE_VIEWED = "pageViewed";
  	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
				
		View rootView = inflater.inflate(R.layout.activity_general_index, container, false);
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_general_index);
        ((MainActivity) getActivity()).getSupportActionBar().setElevation(0);
		
	    mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
	    
        mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setBackgroundColor(getThemeUtils().primaryColor());
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);

        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(android.R.color.white));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
        
	    if (savedInstanceState == null) {
	    	SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
			mViewPager.setCurrentItem(pref.getInt(Utility.DEFAULT_INDEX, 0));
	    }
	    else {
	    	mViewPager.setCurrentItem(savedInstanceState.getInt(PAGE_VIEWED, 0));
	    }

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
            default:
                return new AlphabeticSectionFragment();
            }
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					return getString(R.string.letter_order_text).toUpperCase(l);
				else
					return getString(R.string.letter_order_text);
			case 1:
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					return getString(R.string.page_order_text).toUpperCase(l);
				else
					return getString(R.string.page_order_text);
			case 2:
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					return getString(R.string.arg_search_text).toUpperCase(l);
				else
					return getString(R.string.arg_search_text);
			case 3:
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					return getString(R.string.salmi_musica_index).toUpperCase(l);
				else
					return getString(R.string.salmi_musica_index);
			}
			return null;
		}
	}

    private ThemeUtils getThemeUtils() {
        return ((MainActivity)getActivity()).mThemeUtils;
    }
			
}