package it.cammino.risuscito.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.transition.MaterialSharedAxis
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.main.generalIndexesList
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.viewmodels.GeneralIndexViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GeneralIndexFragment : AccountMenuFragment() {

    private val mViewModel: GeneralIndexViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val localPagerState = mMainActivity?.getPagerState() ?: rememberPagerState(
                    pageCount = {
                        3
                    })
                HorizontalPager(
                    state = localPagerState,
                    beyondViewportPageCount = 1
                ) { page ->
                    // Our page content
                    when (page) {
                        0, 1 ->
                            AndroidFragment<SimpleIndexFragment>(
                                arguments = bundleOf(SimpleIndexFragment.INDICE_LISTA to page)
                            )

//                        2 ->
//                            AndroidFragment<SectionedIndexFragment>(
//                                arguments = bundleOf(SectionedIndexFragment.INDICE_LISTA to 0),
//                                fragmentState = fragmentState
//                            )

                        2 ->
                            AndroidFragment<SimpleIndexFragment>(
                                arguments = bundleOf(SimpleIndexFragment.INDICE_LISTA to 2)
                            )
                    }
                }
                mMainActivity?.setupMaterialTab(generalIndexesList)
                mMainActivity?.setTabVisible(true)

                LaunchedEffect(localPagerState) {
                    snapshotFlow { localPagerState.currentPage }.collect { page ->
                        mViewModel.pageViewed = page
                        mMainActivity?.changeMaterialTabPage(mViewModel.pageViewed)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity?.setupToolbarTitle(R.string.title_activity_general_index)
        mMainActivity?.enableFab(false)

        lifecycleScope.launch {
            delay(500)
            if (savedInstanceState == null) {
                val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                mViewModel.pageViewed = Integer.parseInt(
                    pref.getString(Utility.DEFAULT_INDEX, "0")
                        ?: "0"
                )
                mMainActivity?.changeMaterialTabPage(mViewModel.pageViewed)
                mMainActivity?.setTabVisible(true)
            }

        }
    }

    companion object {
        internal val TAG = GeneralIndexFragment::class.java.canonicalName
    }

}
