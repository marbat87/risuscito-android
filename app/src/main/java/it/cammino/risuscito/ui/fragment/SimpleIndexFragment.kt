package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.composable.SimpleListItem
import it.cammino.risuscito.ui.composable.dialogs.AddToDropDownMenu
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.risuscito_medium_font
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.isGridLayout
import it.cammino.risuscito.utils.extension.openCanto
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.SharedSearchViewModel
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.Collator

class SimpleIndexFragment : Fragment(), SnackBarFragment {

    private val mCantiViewModel: SimpleIndexViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply {
            putInt(Utility.TIPO_LISTA, arguments?.getInt(INDICE_LISTA, 0) ?: 0)
        })
    }
    private val sharedScrollViewModel: SharedScrollViewModel by activityViewModels()

    private val sharedSearchViewModel: SharedSearchViewModel by activityViewModels()

    val showSearchProgress = mutableStateOf(false)

    private var listePersonalizzate: List<ListaPers>? = null

    private var job: Job = Job()
    private var mActivity: MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as? MainActivity
    }

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

                val scrollBehaviorFromSharedVM by sharedScrollViewModel.scrollBehavior.collectAsState()

                val isSearch = arguments?.getBoolean(IS_SEARCH, false) == true

                RisuscitoTheme {
                    if (isSearch) {
                        if (localItems.value.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_search_question_mark),
                                    contentDescription = stringResource(id = R.string.search_no_results),
                                    modifier = Modifier
                                        .size(120.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp)) // Spazio tra immagine e testo
                                Text(
                                    text = stringResource(R.string.search_no_results),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Colore secondario del testo
                                    fontFamily = risuscito_medium_font,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth() // Per centrare il testo se è multiriga
                                )
                            }
                        }
                    }
                    if (showSearchProgress.value) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(
                            modifier = Modifier.width(64.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        ) }
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
                                    val source = stringResource(simpleItem.sourceRes)
                                    SimpleListItem(
                                        requireContext(),
                                        simpleItem,
                                        onItemClick = { item ->
                                            mActivity?.openCanto(
                                                TAG,
                                                item.id,
                                                source,
                                                false
                                            )
                                        },
                                        onItemLongClick = { item ->
                                            mCantiViewModel.idDaAgg = item.id
                                            contextMenuExpanded.value = true
                                        },
                                        selected = false,
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                state = state,
                                modifier = listModifier
                            ) {
                                items(localItems.value) { simpleItem ->
                                    val source = stringResource(simpleItem.sourceRes)
                                    SimpleListItem(
                                        requireContext(),
                                        simpleItem,
                                        onItemClick = { item ->
                                            mActivity?.openCanto(
                                                TAG,
                                                item.id,
                                                source,
                                                false
                                            )
                                        },
                                        onItemLongClick = { item ->
                                            mCantiViewModel.idDaAgg = item.id
                                            contextMenuExpanded.value = true
                                        },
                                        selected = false,
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
                        }
                        when (mCantiViewModel.tipoLista) {
                            0 -> AddToDropDownMenu(
                                this@SimpleIndexFragment,
                                mCantiViewModel,
                                ALPHA_REPLACE,
                                ALPHA_REPLACE_2,
                                listePersonalizzate,
                                contextMenuExpanded.value,
                                offset
                            ) { contextMenuExpanded.value = false }

                            1 -> AddToDropDownMenu(
                                this@SimpleIndexFragment,
                                mCantiViewModel,
                                NUMERIC_REPLACE,
                                NUMERIC_REPLACE_2,
                                listePersonalizzate,
                                contextMenuExpanded.value,
                                offset
                            ) { contextMenuExpanded.value = false }

                            2 -> AddToDropDownMenu(
                                this@SimpleIndexFragment,
                                mCantiViewModel,
                                SALMI_REPLACE,
                                SALMI_REPLACE_2,
                                listePersonalizzate,
                                contextMenuExpanded.value,
                                offset
                            ) { contextMenuExpanded.value = false }
                        }
                    }
                    if (mCantiViewModel.showAlertDialog.observeAsState().value == true) {
                        SimpleAlertDialog(
                            onDismissRequest = { mCantiViewModel.showAlertDialog.postValue(false) },
                            onConfirmation = {
                                Log.d(
                                    TAG,
                                    "mCantiViewModel.shownDialogTag ${mCantiViewModel.shownDialogTag}"
                                )
                                mCantiViewModel.showAlertDialog.postValue(false)
                                when (mCantiViewModel.shownDialogTag) {
                                    ALPHA_REPLACE, NUMERIC_REPLACE, SALMI_REPLACE -> {
                                        listePersonalizzate?.let { lista ->
                                            lista[mCantiViewModel.idListaClick]
                                                .lista?.addCanto(
                                                    (mCantiViewModel.idDaAgg).toString(),
                                                    mCantiViewModel.idPosizioneClick
                                                )
                                            ListeUtils.updateListaPersonalizzata(
                                                this@SimpleIndexFragment,
                                                lista[mCantiViewModel.idListaClick]
                                            )
                                        }
                                    }

                                    ALPHA_REPLACE_2, NUMERIC_REPLACE_2, SALMI_REPLACE_2 -> {
                                        ListeUtils.updatePosizione(
                                            this@SimpleIndexFragment,
                                            mCantiViewModel.idDaAgg,
                                            mCantiViewModel.idListaDaAgg,
                                            mCantiViewModel.posizioneDaAgg
                                        )
                                    }
                                }
                            },
                            dialogTitle = stringResource(R.string.dialog_replace_title),
                            dialogText = mCantiViewModel.alertDialogContent,
                            icon = painterResource(R.drawable.find_replace_24px),
                            confirmButtonText = stringResource(R.string.replace_confirm),
                            dismissButtonText = stringResource(R.string.cancel)
                        )
                    }
                }

                Log.d(TAG, "onCreateView: IS_SEARCH ${arguments?.getBoolean(IS_SEARCH, false)}")
                if (arguments?.getBoolean(IS_SEARCH, false) == true) {
                    sharedSearchViewModel.advancedSearchFilter.observe(viewLifecycleOwner) {
                        job.cancel()
                        ricercaStringa()
                    }

                    sharedSearchViewModel.searchFilter.observe(viewLifecycleOwner) {
                        job.cancel()
                        ricercaStringa()
                    }

                    sharedSearchViewModel.itemsResultFiltered.observe(viewLifecycleOwner) { canti ->
                        localItems.value =
                            canti.sortedWith(compareBy(Collator.getInstance(systemLocale)) {
                                getString(it.titleRes)
                            })
                    }
                } else {
                    mCantiViewModel.itemsResult?.observe(viewLifecycleOwner) { canti ->
                        localItems.value =
                            when (mCantiViewModel.tipoLista) {
                                0 -> canti.sortedWith(compareBy(Collator.getInstance(systemLocale)) {
                                    getString(it.titleRes)
                                })

                                1 -> canti.sortedBy { getString(it.pageRes).toInt() }
                                2 -> canti
                                else -> canti
                            }
                    }
                }

            }
        }
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

    private fun ricercaStringa() {
        val s = sharedSearchViewModel.searchFilter.value ?: StringUtils.EMPTY
        Log.d(TAG, "performSearch STRINGA: $s")
        job = lifecycleScope.launch {
            // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
            if (s.trim { it <= ' ' }.length >= 3) {
                showSearchProgress.value = true
                val titoliResult = ArrayList<RisuscitoListItem>()

                Firebase.crashlytics.log("function: search_text - search_string: $s - advanced: ${sharedSearchViewModel.advancedSearchFilter.value}")

                Log.d(TAG, "performSearch STRINGA: $s")
                Log.d(TAG, "performSearch ADVANCED: ${sharedSearchViewModel.advancedSearchFilter.value}")
                if (sharedSearchViewModel.advancedSearchFilter.value == true) {
                    val words =
                        s.split("\\W".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    for (aText in sharedSearchViewModel.aTexts) {
                        if (!isActive) return@launch

                        if (aText[0] == null || aText[0].isNullOrEmpty()) break

                        var found = true
                        for (word in words) {
                            if (!isActive) return@launch

                            if (word.trim { it <= ' ' }.length > 1) {
                                var text = word.trim { it <= ' ' }
                                text = text.lowercase(systemLocale)
                                text = Utility.removeAccents(text)

                                if (aText[1]?.contains(text) != true) found = false
                            }
                        }

                        if (found) {
                            Log.d(TAG, "aText[0]: ${aText[0]}")
                            sharedSearchViewModel.titoli.filter { (aText[0].orEmpty()) == it.undecodedSource }
                                .forEach {
                                    if (!isActive) return@launch
                                    titoliResult.add(it.apply { filter = StringUtils.EMPTY })
                                }
                        }
                    }
                } else {
                    val stringa = Utility.removeAccents(s).lowercase(systemLocale)
                    Log.d(TAG, "performSearch onTextChanged: stringa $stringa")
                    sharedSearchViewModel.titoli.filter {
                        Utility.removeAccents(
                            getString(it.titleRes)
                        ).lowercase(systemLocale).contains(stringa)
                    }.forEach {
                        if (!isActive) return@launch
                        titoliResult.add(it.apply { filter = stringa })
                    }
                }
                if (isActive) {
                    sharedSearchViewModel.itemsResultFiltered.value =
                        titoliResult.sortedWith(
                            compareBy(
                                Collator.getInstance(systemLocale)
                            ) { getString(it.titleRes) })
                    showSearchProgress.value = false
                }
            } else {
                if (s.isEmpty()) {
//                    binding.searchViewLayout.matchedList.isVisible = false
                    sharedSearchViewModel.itemsResultFiltered.value = sharedSearchViewModel.titoli
                    showSearchProgress.value = false
//                    expandToolbar()
                }
            }
        }
    }

    companion object {
        private val TAG = SimpleIndexFragment::class.java.canonicalName
        private const val ALPHA_REPLACE = "ALPHA_REPLACE"
        private const val ALPHA_REPLACE_2 = "ALPHA_REPLACE_2"
        private const val NUMERIC_REPLACE = "NUMERIC_REPLACE"
        private const val NUMERIC_REPLACE_2 = "NUMERIC_REPLACE_2"
        private const val SALMI_REPLACE = "SALMI_REPLACE"
        private const val SALMI_REPLACE_2 = "SALMI_REPLACE_2"
        const val INDICE_LISTA = "indiceLista"

        const val IS_SEARCH = "isSearch"

        fun newInstance(tipoLista: Int, isSearch: Boolean = false): SimpleIndexFragment {
            val f = SimpleIndexFragment()
            f.arguments = bundleOf(INDICE_LISTA to tipoLista)
            f.arguments = bundleOf(IS_SEARCH to isSearch)
            return f
        }
    }

}