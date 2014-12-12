package it.cammino.risuscito;

import java.util.Locale;

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

public class GeneralSearch extends Fragment {

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	SlidingTabLayout mSlidingTabLayout = null;
  	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
//		getSupportActionBar().setTitle(R.string.title_activity_search);
		View rootView = inflater.inflate(R.layout.activity_general_search, container, false);
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_activity_search);
		
		// Create the adapter that will return a fragment for each of the three
//		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
//	    mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
//	    mViewPager.setAdapter(mSectionsPagerAdapter);
//	    mViewPager.setCurrentItem(0);
//	    
//	    PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
//	    tabs.setViewPager(mViewPager);
		
//		checkScreenAwake();
  
//	    setHasOptionsMenu(true);
	    
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
	    mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        
        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.theme_accent));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
	    
        return rootView;
	}

//    @Override
//    public void onResume() {
//    	super.onResume();
//    	checkScreenAwake();
//    }
	
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		getActivity().getMenuInflater().inflate(R.menu.risuscito, menu);
//		super.onCreateOptionsMenu(menu, inflater);
//	}
	
//    @Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.action_settings:
//			startActivity(new Intent(getActivity(), Settings.class));
//			return true;
//		case R.id.action_favourites:
//			startActivity(new Intent(getActivity(), FavouritesActivity.class));
//			return true;
//		case R.id.action_donate:
//			startActivity(new Intent(getActivity(), DonateActivity.class));
//			return true;
//		case R.id.action_about:
//			startActivity(new Intent(getActivity(), AboutActivity.class));
//			return true;
//		}
//		return false;
//	}

//    //controlla se l'app deve mantenere lo schermo acceso
//    public void checkScreenAwake() {
//    	SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());
//		boolean screenOn = pref.getBoolean(Utility.SCREEN_ON, false);
//		if (screenOn)
//			mViewPager.setKeepScreenOn(true);
//		else
//			mViewPager.setKeepScreenOn(false);
//    }
    
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
			Locale l = Locale.getDefault();
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
			
}