package it.cammino.risuscito.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
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
import kotlin.time.Duration.Companion.milliseconds

class GeneralIndexFragment : RisuscitoFragment() {

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

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    HorizontalPager(
                        state = localPagerState
                    ) { page ->
                        // Our page content
                        when (page) {
                            0, 1 ->
                                AndroidFragment<SimpleIndexFragment>(
                                    arguments = Bundle().apply {
                                        putInt(SimpleIndexFragment.INDICE_LISTA, page)
                                    }
                                )

                            2 ->
                                AndroidFragment<SectionedIndexFragment>(
                                    arguments = Bundle().apply {
                                        putInt(SectionedIndexFragment.INDICE_LISTA, 4)
                                    }
                                )

                            3 ->
                                AndroidFragment<SimpleIndexFragment>(
                                    arguments = Bundle().apply {
                                        putInt(SimpleIndexFragment.INDICE_LISTA, 2)
                                    }
                                )
                        }
                    }
                }

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

            }

            mMainActivity?.createOptionsMenu(
                emptyList(),
                null
            )

            mMainActivity?.setupMaterialTab(generalIndexesList)
            mMainActivity?.setTabVisible(true)
            mMainActivity?.initFab(enable = false)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            delay(500.milliseconds)
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
