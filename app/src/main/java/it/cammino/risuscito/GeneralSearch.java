package it.cammino.risuscito;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import butterknife.Unbinder;
import it.cammino.risuscito.ui.ThemeableActivity;

public class GeneralSearch extends Fragment {

  @BindView(R.id.view_pager)
  ViewPager mViewPager;

  private Unbinder mUnbinder;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.activity_general_search, container, false);
    mUnbinder = ButterKnife.bind(this, rootView);

    MainActivity mMainActivity = (MainActivity) getActivity();
    mMainActivity.setupToolbarTitle(R.string.title_activity_search);

    mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));

    final TabLayout tabs = mMainActivity.mTabLayout;
    tabs.setVisibility(View.VISIBLE);
    mMainActivity.enableFab(false);
    if (!mMainActivity.isOnTablet()) mMainActivity.enableBottombar(false);
    tabs.setupWithViewPager(mViewPager);

    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mUnbinder.unbind();
  }

  private class SectionsPagerAdapter extends FragmentPagerAdapter {

    SectionsPagerAdapter(FragmentManager fm) {
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
      Locale l =
          ThemeableActivity.getSystemLocalWrapper(getActivity().getResources().getConfiguration());
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
