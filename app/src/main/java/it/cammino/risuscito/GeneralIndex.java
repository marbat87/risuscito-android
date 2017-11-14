package it.cammino.risuscito;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.utils.ThemeUtils;

public class GeneralIndex extends Fragment {

  private static final String PAGE_VIEWED = "pageViewed";
  final String TAG = getClass().getCanonicalName();
  @BindView(R.id.view_pager)
  ViewPager mViewPager;
  private MainActivity mMainActivity;
  private Unbinder mUnbinder;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View rootView = inflater.inflate(R.layout.tabs_layout, container, false);
    mUnbinder = ButterKnife.bind(this, rootView);

    mMainActivity = (MainActivity) getActivity();
    mMainActivity.setupToolbarTitle(R.string.title_activity_general_index);

    mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));

    final TabLayout tabs = mMainActivity.mTabLayout;
    tabs.setVisibility(View.VISIBLE);
    mMainActivity.enableFab(false);
    if (!mMainActivity.isOnTablet()) mMainActivity.enableBottombar(false);
    if (savedInstanceState == null) {
      SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
      mViewPager.setCurrentItem(Integer.parseInt(pref.getString(Utility.DEFAULT_INDEX, "0")));
    } else mViewPager.setCurrentItem(savedInstanceState.getInt(PAGE_VIEWED, 0));
    if (!mMainActivity.isOnTablet()) tabs.setBackgroundColor(getThemeUtils().primaryColor());
    tabs.setupWithViewPager(mViewPager);

    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mUnbinder.unbind();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(PAGE_VIEWED, mViewPager.getCurrentItem());
  }

  private ThemeUtils getThemeUtils() {
    return mMainActivity.getThemeUtils();
  }

  private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    SectionsPagerAdapter(FragmentManager fm) {
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
      //            Locale l = getActivity().getResources().getConfiguration().locale;
      Locale l =
          ThemeableActivity.getSystemLocalWrapper(getActivity().getResources().getConfiguration());
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
}
