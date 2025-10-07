package it.cammino.risuscito.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import it.cammino.risuscito.ui.composable.main.generalIndexesList
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.viewmodels.SharedTabViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class GeneralIndexFragment : AccountMenuFragment() {

    private val sharedTabViewModel: SharedTabViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                val localPagerState = rememberPagerState(pageCount = {
                    4
                })

                HorizontalPager(
                    state = localPagerState
                ) { page ->
                    // Our page content
                    when (page) {
                        0, 1 ->
                            AndroidFragment<SimpleIndexFragment>(
                                arguments = bundleOf(SimpleIndexFragment.INDICE_LISTA to page)
                            )

                        2 ->
                            AndroidFragment<SectionedIndexFragment>(
                                arguments = bundleOf(SectionedIndexFragment.INDICE_LISTA to 0)
                            )

                        3 ->
                            AndroidFragment<SimpleIndexFragment>(
                                arguments = bundleOf(SimpleIndexFragment.INDICE_LISTA to 2)
                            )
                    }
                }
                mMainActivity?.setupMaterialTab(generalIndexesList)
                mMainActivity?.setTabVisible(true)

                LaunchedEffect(localPagerState) {
                    snapshotFlow { localPagerState.currentPage }
                        .distinctUntilChanged()
                        .collect { page ->
                            Log.d(
                                TAG,
                                "localPagerState.currentPage CHANGED (from snapshotFlow): $page"
                            )
                            if (sharedTabViewModel.tabsSelectedIndex.intValue != page)
                                sharedTabViewModel.tabsSelectedIndex.intValue = page
                        }
                }

                LaunchedEffect(Unit) { // Esegui una volta e colleziona il flow
                    snapshotFlow { sharedTabViewModel.tabsSelectedIndex.intValue }
                        .collect { selectedIndex ->
                            Log.d(
                                TAG,
                                "Tabs selected index CHANGED (from snapshotFlow): $selectedIndex"
                            )
                            if (localPagerState.currentPage != selectedIndex) {
                                Log.d(TAG, "Animating pager to page: $selectedIndex")
                                localPagerState.scrollToPage(selectedIndex)
                            }
                        }
                }

                mMainActivity?.createOptionsMenu(
                    emptyList(),
                    null
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity?.enableFab(false)

        lifecycleScope.launch {
            delay(500)
            if (sharedTabViewModel.resetTab.value) {
                Log.d(TAG, "GeneralIndexFragment newINSTANCE")
                sharedTabViewModel.resetTab.value = false
                val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                sharedTabViewModel.tabsSelectedIndex.intValue = Integer.parseInt(
                    pref.getString(Utility.DEFAULT_INDEX, "0")
                        ?: "0"
                )
                mMainActivity?.setTabVisible(true)
            }

        }
    }

    companion object {
        internal val TAG = GeneralIndexFragment::class.java.canonicalName
    }

}
