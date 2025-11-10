package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.ExpandableItemType
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.composable.ExpandableListItem
import it.cammino.risuscito.ui.composable.ListTitleItem
import it.cammino.risuscito.ui.composable.dialogs.AddToDropDownMenu
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleDialogTag
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.openCanto
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SectionedIndexFragment : Fragment(), SnackBarFragment {

    private val mCantiViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply {
            putInt(Utility.TIPO_LISTA, arguments?.getInt(INDICE_LISTA, 0) ?: 0)
        })
    }
    private var listePersonalizzate: List<ListaPers>? = null

    private val sharedScrollViewModel: SharedScrollViewModel by activityViewModels()
    private var mActivity: MainActivity? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = rememberLazyListState()
                val localItems by mCantiViewModel.modelSectionedItemsResult.observeAsState()
                val coroutineScope = rememberCoroutineScope()
                val expandedItem = remember { mutableIntStateOf(-1) }

                val scrollBehaviorFromSharedVM by sharedScrollViewModel.scrollBehavior.collectAsState()

                // In SectionedIndexFragment, dentro setContent
                val rememberedOnItemClick = remember<(RisuscitoListItem) -> Unit> {
                    { item ->
                        mActivity?.openCanto(TAG, item.id, getString(item.sourceRes), false)
                    }
                }

                val rememberedOnItemLongClick = remember<(RisuscitoListItem) -> Unit> {
                    { item ->
                        mCantiViewModel.idDaAgg = item.id
                    }
                }

                val rememberedOnHeaderClick = remember<(RisuscitoListItem) -> Unit> {
                    { clickedItem ->
                        Log.d(
                            TAG,
                            "rememberedOnHeaderClick - groupIndex:${clickedItem.groupIndex} - identifier:${clickedItem.identifier}"
                        )
                        expandedItem.intValue =
                            if (expandedItem.intValue == clickedItem.groupIndex) -1 else clickedItem.groupIndex
                        if (expandedItem.intValue == clickedItem.groupIndex)
                            coroutineScope.launch {
                                delay(500)
                                state.animateScrollToItem(index = clickedItem.identifier)
                            }
                    }
                }

                val listModifier = Modifier
                    .fillMaxSize()
                    .then(
                        scrollBehaviorFromSharedVM?.let { Modifier.nestedScroll(it.nestedScrollConnection) }
                            ?: Modifier
                    )
//                if (hasGridLayout()) {
//                    LazyVerticalGrid(
//                        columns = GridCells.Fixed(2),
//                        modifier = listModifier
//                    ) {
//                        localItems.orEmpty().forEach { (initial, songsForGroup) ->
//                            stickyHeader {
//                                ListTitleItem(initial)
//                            }
//
//                            songsForGroup.forEach { simpleItem ->
//                                item(span = { GridItemSpan(if (simpleItem.itemType == ExpandableItemType.SUBITEM) 1 else 2) }) {
//                                    Box(
//                                        modifier = Modifier
//                                            .wrapContentHeight()
//                                            .fillMaxWidth()
//                                    )
//                                    {
//                                        val itemContextMenuExpanded =
//                                            remember { mutableStateOf(false) }
//                                        val offset = remember { mutableStateOf(DpOffset.Zero) }
//                                        var isExpanded = false
//                                        if (simpleItem.itemType == ExpandableItemType.EXPANDABLE)
//                                            isExpanded =
//                                                expandedItem.intValue == simpleItem.groupIndex
//                                        if (simpleItem.itemType == ExpandableItemType.SUBITEM) {
//                                            isExpanded =
//                                                expandedItem.intValue == simpleItem.groupIndex
//                                        }
//
//                                        ExpandableListItem(
//                                            requireContext(),
//                                            simpleItem,
//                                            onItemClick = rememberedOnItemClick,
//                                            onItemLongClick = {
//                                                rememberedOnItemLongClick(it)
//                                                itemContextMenuExpanded.value = true
//                                            },
//                                            onHeaderClicked = rememberedOnHeaderClick,
//                                            isExpanded = isExpanded,
//                                            modifier = Modifier.onSizeChanged {
//                                                offset.value = DpOffset((it.width / 12).dp, 0.dp)
//                                            }
//                                        )
//                                        AddToDropDownMenu(
//                                            this@SectionedIndexFragment,
//                                            mCantiViewModel,
//                                            SimpleDialogTag.LITURGICO_REPLACE,
//                                            SimpleDialogTag.LITURGICO_REPLACE_2,
//                                            listePersonalizzate,
//                                            itemContextMenuExpanded.value,
//                                            offset.value
//                                        ) { itemContextMenuExpanded.value = false }
//                                    }
//                                }
//                            }
//
//                        }
//                    }
//                } else {
                LazyColumn(
                    state = state,
                    modifier = listModifier
                ) {

                    localItems.orEmpty().forEach { (initial, songsForGroup) ->
                        stickyHeader {
                            ListTitleItem(initial)
                        }

                        items(songsForGroup) { simpleItem ->
                            Box(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                            )
                            {
                                val itemContextMenuExpanded =
                                    remember { mutableStateOf(false) }
                                val offset = remember { mutableStateOf(DpOffset.Zero) }
                                var isExpanded = false
                                if (simpleItem.itemType == ExpandableItemType.EXPANDABLE)
                                    isExpanded =
                                        expandedItem.intValue == simpleItem.groupIndex
                                if (simpleItem.itemType == ExpandableItemType.SUBITEM) {
                                    isExpanded =
                                        expandedItem.intValue == simpleItem.groupIndex
                                }

                                ExpandableListItem(
                                    requireContext(),
                                    simpleItem,
                                    onItemClick = rememberedOnItemClick,
                                    onItemLongClick = {
                                        rememberedOnItemLongClick(it)
                                        itemContextMenuExpanded.value = true
                                    },
                                    onHeaderClicked = rememberedOnHeaderClick,
                                    isExpanded = isExpanded,
                                    modifier = Modifier.onSizeChanged {
                                        offset.value = DpOffset((it.width / 12).dp, 0.dp)
                                    }
                                )
                                AddToDropDownMenu(
                                    this@SectionedIndexFragment,
                                    mCantiViewModel,
                                    SimpleDialogTag.LITURGICO_REPLACE,
                                    SimpleDialogTag.LITURGICO_REPLACE_2,
                                    listePersonalizzate,
                                    itemContextMenuExpanded.value,
                                    offset.value
                                ) { itemContextMenuExpanded.value = false }
                            }
                        }
                    }
                }
//                }

                if (mCantiViewModel.showAlertDialog.observeAsState().value == true) {
                    SimpleAlertDialog(
                        onDismissRequest = { mCantiViewModel.showAlertDialog.postValue(false) },
                        onConfirmation = {
                            mCantiViewModel.showAlertDialog.postValue(false)
                            when (mCantiViewModel.dialogTag) {
                                SimpleDialogTag.LITURGICO_REPLACE -> {
                                    listePersonalizzate?.let { lista ->
                                        lista[mCantiViewModel.idListaClick]
                                            .lista?.addCanto(
                                                (mCantiViewModel.idDaAgg).toString(),
                                                mCantiViewModel.idPosizioneClick
                                            )
                                        ListeUtils.updateListaPersonalizzata(
                                            this@SectionedIndexFragment,
                                            lista[mCantiViewModel.idListaClick]
                                        )
                                    }
                                }

                                SimpleDialogTag.LITURGICO_REPLACE_2 -> {
                                    ListeUtils.updatePosizione(
                                        this@SectionedIndexFragment,
                                        mCantiViewModel.idDaAgg,
                                        mCantiViewModel.idListaDaAgg,
                                        mCantiViewModel.posizioneDaAgg
                                    )
                                }

                                else -> {}
                            }
                        },
                        dialogTitle = stringResource(R.string.dialog_replace_title),
                        dialogText = mCantiViewModel.content.value.orEmpty(),
                        iconRes = R.drawable.find_replace_24px,
                        confirmButtonText = stringResource(R.string.replace_confirm),
                        dismissButtonText = stringResource(R.string.cancel)
                    )
                }

            }

            mCantiViewModel.sectionedItemsResult?.observe(viewLifecycleOwner) { itemList ->
                mCantiViewModel.modelSectionedItemsResult.value =
                    itemList as MutableMap<Int, List<RisuscitoListItem>>?
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as? MainActivity
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            listePersonalizzate =
                RisuscitoDatabase.getInstance(requireContext()).listePersDao().all()
        }
    }

    override fun onActionPerformed() {}

    override fun onDismissed() {}

    override fun showSnackBar(message: String, label: String?) {
        mActivity?.showSnackBar(message = message)
    }

    companion object {
        private val TAG = SectionedIndexFragment::class.java.canonicalName
        const val INDICE_LISTA = "indiceLista"
    }
}
