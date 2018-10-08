package it.cammino.risuscito

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.android.synthetic.main.tabs_layout.*

class GeneralSearch : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_general_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mMainActivity = activity as MainActivity?
        mMainActivity!!.setupToolbarTitle(R.string.title_activity_search)

//        activity!!.view_pager!!.adapter = SectionsPagerAdapter(childFragmentManager)
        view_pager.adapter = SectionsPagerAdapter(childFragmentManager)

        mMainActivity.setTabVisible(true)
//        val tabs = activity!!.material_tabs
//        val tabs = mMainActivity.getMaterialTabs()
//        tabs.visibility = View.VISIBLE
        mMainActivity.enableFab(false)
//        if (!mMainActivity.isOnTablet) mMainActivity.enableBottombar(false)
        mMainActivity.enableBottombar(false)
//        tabs.setupWithViewPager(view_pager)
        mMainActivity.getMaterialTabs().setupWithViewPager(view_pager)
    }

    private inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> RicercaVeloceFragment()
                1 -> RicercaAvanzataFragment()
                else -> RicercaVeloceFragment()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val l = ThemeableActivity.getSystemLocalWrapper(activity!!.resources.configuration)
            return when (position) {
                0 -> getString(R.string.fast_search_title).toUpperCase(l)
                1 -> getString(R.string.advanced_search_title).toUpperCase(l)
                else -> ""
            }
        }
    }
}
