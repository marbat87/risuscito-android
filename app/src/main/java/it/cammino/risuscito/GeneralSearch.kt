package it.cammino.risuscito

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.android.synthetic.main.activity_main.*
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

        activity!!.view_pager!!.adapter = SectionsPagerAdapter(childFragmentManager)

        val tabs = activity!!.material_tabs
        tabs.visibility = View.VISIBLE
        mMainActivity.enableFab(false)
        if (!mMainActivity.isOnTablet) mMainActivity.enableBottombar(false)
        tabs.setupWithViewPager(view_pager)
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
