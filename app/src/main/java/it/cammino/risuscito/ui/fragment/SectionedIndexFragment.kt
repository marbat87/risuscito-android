package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.ExpandableItemType
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.risuscitoListItem
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.composable.ExpandableListItem
import it.cammino.risuscito.ui.composable.SimpleListItem
import it.cammino.risuscito.ui.composable.dialogs.AddToDropDownMenu
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleDialogTag
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.isGridLayout
import it.cammino.risuscito.utils.extension.openCanto
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
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
                val localItems = remember { mutableStateOf<List<RisuscitoListItem>>(emptyList()) }
                var offset = DpOffset.Zero
                val contextMenuExpanded = remember { mutableStateOf(false) }
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
                        contextMenuExpanded.value = true
                    }
                }

                val rememberedOnHeaderClick = remember<(RisuscitoListItem) -> Unit> {
                    { clickedItem ->
                        val newExpandedId =
                            if (expandedItem.intValue == clickedItem.identifier) -1 else clickedItem.identifier
                        expandedItem.intValue = newExpandedId
                        if (expandedItem.intValue == clickedItem.identifier)
                            coroutineScope.launch {
                                state.animateScrollToItem(index = clickedItem.identifier)
                            }
                    }
                }

                Box(
                    modifier = Modifier.pointerInteropFilter {
                        offset = DpOffset(it.x.dp, it.y.dp)
                        false
                    }
                ) {
                    val listModifier = Modifier
                        .fillMaxSize()
                        .then(
                            scrollBehaviorFromSharedVM?.let { Modifier.nestedScroll(it.nestedScrollConnection) }
                                ?: Modifier
                        )
                    if (context?.isGridLayout == true) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = listModifier
                        ) {
                            items(localItems.value) { simpleItem ->
                                SimpleListItem(
                                    requireContext(),
                                    simpleItem,
                                    onItemClick = rememberedOnItemClick,
                                    onItemLongClick = rememberedOnItemLongClick,
                                    selected = false,
                                    modifier = Modifier
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = state,
                            modifier = listModifier
                        ) {
                            items(
                                localItems.value,
                                key = { it.identifier },
                                contentType = { it.itemType }) { simpleItem ->
                                var isExpanded = false
                                if (simpleItem.itemType == ExpandableItemType.EXPANDABLE)
                                    isExpanded = expandedItem.intValue == simpleItem.identifier
                                if (simpleItem.itemType == ExpandableItemType.SUBITEM) {
                                    isExpanded = expandedItem.intValue == simpleItem.groupIndex
                                }

                                ExpandableListItem(
                                    requireContext(),
                                    simpleItem,
                                    onItemClick = rememberedOnItemClick,
                                    onItemLongClick = rememberedOnItemLongClick,
                                    onHeaderClicked = rememberedOnHeaderClick,
                                    isExpanded = isExpanded,
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                    AddToDropDownMenu(
                        this@SectionedIndexFragment,
                        mCantiViewModel,
                        SimpleDialogTag.LITURGICO_REPLACE,
                        SimpleDialogTag.LITURGICO_REPLACE_2,
                        listePersonalizzate,
                        contextMenuExpanded.value,
                        offset
                    ) { contextMenuExpanded.value = false }
                }
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

                val mDao = RisuscitoDatabase.getInstance(requireContext()).indiceLiturgicoDao()
                mDao.liveAll().observe(viewLifecycleOwner) { canti ->
                    val cantiList = ArrayList<RisuscitoListItem>()
                    val cantiSubItemList = ArrayList<RisuscitoListItem>()
                    var totCanti = 0
                    var ultimoGruppo = 0

                    for (i in canti.indices) {
//                AGGIUNTA RIGA DI GRUPPO
                        if (ultimoGruppo != canti[i].idGruppo) {
                            ultimoGruppo = canti[i].idGruppo
                            cantiList.add(
                                risuscitoListItem(
                                    itemType = ExpandableItemType.TITLE,
                                    titleRes = Utility.getResId(
                                        canti[i].nomeGruppo,
                                        R.string::class.java
                                    )
                                ) {
                                    identifier = totCanti++
                                }
                            )
                        }

                        cantiSubItemList.add(
                            risuscitoListItem(
                                itemType = ExpandableItemType.SUBITEM,
                                titleRes = Utility.getResId(canti[i].titolo, R.string::class.java)
                            ) {
                                pageRes = Utility.getResId(
                                    canti[i].pagina,
                                    R.string::class.java
                                )
                                sourceRes = Utility.getResId(canti[i].source, R.string::class.java)
                                setColor = canti[i].color
                                id = canti[i].id
                                groupIndex = totCanti
                            }
                        )

                        if ((i == (canti.size - 1) || canti[i].idIndice != canti[i + 1].idIndice)) {
                            // serve a non mettere il divisore sull'ultimo elemento della lista
                            cantiList.add(
                                risuscitoListItem(
                                    itemType = ExpandableItemType.EXPANDABLE,
                                    titleRes = Utility.getResId(canti[i].nome, R.string::class.java)
                                ) {
                                    pageRes = Utility.getResId(
                                        canti[i].pagina,
                                        R.string::class.java
                                    )
                                    sourceRes =
                                        Utility.getResId(canti[i].source, R.string::class.java)
                                    setColor = canti[i].color
                                    identifier = totCanti++
                                    subCantiCounter = cantiSubItemList.size
                                }
                            )

                            cantiSubItemList.forEach { subitem ->
                                subitem.identifier = totCanti++
                                cantiList.add(subitem)
                            }
                            cantiSubItemList.clear()

                        }
                    }
                    localItems.value = cantiList
                }

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

        fun newInstance(tipoLista: Int): SectionedIndexFragment {
            val f = SectionedIndexFragment()
            f.arguments = bundleOf(INDICE_LISTA to tipoLista)
            return f
        }
    }
}
