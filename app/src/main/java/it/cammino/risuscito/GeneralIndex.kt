package it.cammino.risuscito

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils
import it.cammino.risuscito.viewmodels.GeneralIndexViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tabs_layout.*

class GeneralIndex : Fragment() {

    private var mMainActivity: MainActivity? = null

    private var mViewModel: GeneralIndexViewModel? = null

    private val themeUtils: ThemeUtils
        get() = mMainActivity!!.themeUtils!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.tabs_layout, container, false)

        mViewModel = ViewModelProviders.of(this).get(GeneralIndexViewModel::class.java)

        mMainActivity = activity as MainActivity?
        mMainActivity!!.setupToolbarTitle(R.string.title_activity_general_index)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view_pager!!.adapter = SectionsPagerAdapter(childFragmentManager)

        val tabs = activity!!.material_tabs
        tabs.visibility = View.VISIBLE
        mMainActivity!!.enableFab(false)
//        if (!mMainActivity!!.isOnTablet) mMainActivity!!.enableBottombar(false)
        mMainActivity!!.enableBottombar(false)
        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(activity)
            view_pager!!.currentItem = Integer.parseInt(pref.getString(Utility.DEFAULT_INDEX, "0")!!)
        } else
            view_pager!!.currentItem = mViewModel!!.pageViewed
        if (!mMainActivity!!.isOnTablet) tabs.setBackgroundColor(themeUtils.primaryColor())
        tabs.setupWithViewPager(view_pager)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mViewModel!!.pageViewed = view_pager!!.currentItem
    }

    private inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> AlphabeticSectionFragment()
                1 -> NumericSectionFragment()
                2 -> ArgumentsSectionFragment()
                3 -> SalmiSectionFragment()
                4 -> IndiceLiturgicoFragment()
                else -> AlphabeticSectionFragment()
            }
        }

        override fun getCount(): Int {
            return 5
        }

        override fun getPageTitle(position: Int): CharSequence? {
            //            Locale l = getActivity().getResources().getConfiguration().locale;
            val l = ThemeableActivity.getSystemLocalWrapper(activity!!.resources.configuration)
            when (position) {
                0 -> return getString(R.string.letter_order_text).toUpperCase(l)
                1 -> return getString(R.string.page_order_text).toUpperCase(l)
                2 -> return getString(R.string.arg_search_text).toUpperCase(l)
                3 -> return getString(R.string.salmi_musica_index).toUpperCase(l)
                4 -> return getString(R.string.indice_liturgico_index).toUpperCase(l)
            }
            return null
        }
    }

    companion object {
        internal val TAG = GeneralIndex::class.java.canonicalName
    }

}
