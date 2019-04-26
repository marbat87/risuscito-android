package it.cammino.risuscito

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.viewmodels.GeneralIndexViewModel
import kotlinx.android.synthetic.main.tabs_layout.*

class GeneralSearch : Fragment() {

    private var mMainActivity: MainActivity? = null

    private var mViewModel: GeneralIndexViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mViewModel = ViewModelProviders.of(this).get(GeneralIndexViewModel::class.java)

        mMainActivity = activity as MainActivity?
        mMainActivity!!.setupToolbarTitle(R.string.title_activity_search)

        return inflater.inflate(R.layout.activity_general_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view_pager.adapter = SectionsPagerAdapter(childFragmentManager)

        mMainActivity!!.setTabVisible(true)
        mMainActivity!!.enableFab(false)
        mMainActivity!!.enableBottombar(false)
        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            view_pager.currentItem = Integer.parseInt(pref.getString(Utility.DEFAULT_SEARCH, "0")!!)
        } else
            view_pager.currentItem = mViewModel!!.pageViewed
        mMainActivity!!.getMaterialTabs().setupWithViewPager(view_pager)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mViewModel!!.pageViewed = view_pager.currentItem
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
