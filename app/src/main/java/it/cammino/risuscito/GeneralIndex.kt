package it.cammino.risuscito

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import it.cammino.risuscito.databinding.TabsLayoutBinding
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.viewmodels.GeneralIndexViewModel

class GeneralIndex : Fragment() {

    private var mMainActivity: MainActivity? = null

    private val mViewModel: GeneralIndexViewModel by viewModels()

    private val mPageChange: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            // no-op
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            // no-op
        }

        override fun onPageSelected(position: Int) {
            Log.d(TAG, "onPageSelected: $position")
            mViewModel.pageViewed = position
        }
    }

    private var _binding: TabsLayoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = TabsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        binding.viewPager.removeOnPageChangeListener(mPageChange)
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity = activity as? MainActivity
        mMainActivity?.setupToolbarTitle(R.string.title_activity_general_index)
        mMainActivity?.setTabVisible(true)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)

        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            binding.viewPager.currentItem = Integer.parseInt(pref.getString(Utility.DEFAULT_INDEX, "0")
                    ?: "0")
        } else
            binding.viewPager.currentItem = mViewModel.pageViewed
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.adapter = SectionsPagerAdapter(childFragmentManager)
        binding.viewPager.addOnPageChangeListener(mPageChange)
        mMainActivity?.getMaterialTabs()?.setupWithViewPager(binding.viewPager)
    }

    private inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> SimpleIndexFragment.newInstance(0)
                1 -> SimpleIndexFragment.newInstance(1)
                2 -> SectionedIndexFragment.newInstance(0)
                3 -> SimpleIndexFragment.newInstance(2)
                4 -> SectionedIndexFragment.newInstance(1)
                else -> SimpleIndexFragment.newInstance(1)
            }
        }

        override fun getCount(): Int {
            return 5
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val l = getSystemLocale(resources)
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
