package it.cammino.risuscito.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.TabsLayoutBinding
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.viewmodels.GeneralIndexViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GeneralIndexFragment : AccountMenuFragment() {

    private val mViewModel: GeneralIndexViewModel by viewModels()

    private val mPageChange: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d(TAG, "onPageSelected: $position")
                mViewModel.pageViewed = position
            }
        }

    private var _binding: TabsLayoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        binding.viewPager.unregisterOnPageChangeCallback(mPageChange)
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity?.setupToolbarTitle(R.string.title_activity_general_index)
        mMainActivity?.setTabVisible(true)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)

        binding.viewPager.adapter = IndexTabsAdapter(this)
        mMainActivity?.getMaterialTabs()?.let {
            TabLayoutMediator(it, binding.viewPager) { tab, position ->
                tab.setText(
                    when (position) {
                        0 -> R.string.letter_order_text
                        1 -> R.string.page_order_text
                        2 -> R.string.indice_liturgico_index
                        3 -> R.string.salmi_musica_index
                        else -> R.string.letter_order_text
                    }
                )
            }.attach()
        }
        binding.viewPager.registerOnPageChangeCallback(mPageChange)

        lifecycleScope.launch {
            delay(500)
            if (savedInstanceState == null) {
                val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                binding.viewPager.currentItem = Integer.parseInt(
                    pref.getString(Utility.DEFAULT_INDEX, "0")
                        ?: "0"
                )
            } else
                binding.viewPager.currentItem = mViewModel.pageViewed
        }

    }

    private class IndexTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> SimpleIndexFragment.newInstance(0)
                1 -> SimpleIndexFragment.newInstance(1)
                2 -> SectionedIndexFragment.newInstance(0)
                3 -> SimpleIndexFragment.newInstance(2)
                else -> SimpleIndexFragment.newInstance(0)
            }
    }

    companion object {
        internal val TAG = GeneralIndexFragment::class.java.canonicalName
    }

}
