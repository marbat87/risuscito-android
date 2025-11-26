package it.cammino.risuscito.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.ui.activity.ThemeableActivity
import it.cammino.risuscito.ui.composable.EmptyListView
import it.cammino.risuscito.ui.composable.SimpleListItem
import it.cammino.risuscito.ui.composable.dialogs.AddToDropDownMenu
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleDialogTag
import it.cammino.risuscito.ui.composable.hasNavigationBar
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.SharedSearchViewModel
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut
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
    private var mActivity: ThemeableActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as? ThemeableActivity
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                val state = rememberLazyListState()
                val localItems = remember { mutableStateOf<List<RisuscitoListItem>>(emptyList()) }

                val scrollBehaviorFromSharedVM by sharedScrollViewModel.scrollBehavior.collectAsState()

                val isSearch = arguments?.getBoolean(IS_SEARCH, false) == true

                val isInsert = arguments?.getBoolean(IS_INSERT, false) == true

                val hasNavigationBar = hasNavigationBar()


                val rememberItemClick = remember<(RisuscitoListItem) -> Unit> {
                    { item ->
                        if (isInsert) {
                            sharedSearchViewModel.insertItemId = item.id
                            sharedSearchViewModel.done.value = true
                        } else {
                            if (isSearch && hasNavigationBar) {
                                mActivity?.closeSearch()
                            }
                            mActivity?.openCanto(
                                TAG,
                                item.id,
                                getString(item.sourceRes),
                                false
                            )
                        }
                    }
                }

                val rememberIconClick = remember<(RisuscitoListItem) -> Unit> {
                    { item ->
                        Log.d(TAG, "rememberIconClick: ${item.id}")
                        mActivity?.openCanto(
                            TAG,
                            item.id,
                            getString(item.sourceRes),
                            true
                        )
                    }
                }

                val rememberItemLongClick = remember<(RisuscitoListItem) -> Unit> {
                    { item ->
                        if (!isInsert) {
                            mCantiViewModel.idDaAgg = item.id
                        }
                    }
                }

                if (isSearch) {
                    AnimatedVisibility(
                        visible = localItems.value.isEmpty(),
                        enter = materialFadeThroughIn(),
                        exit = materialFadeThroughOut()
                    ) {
                        EmptyListView(
                            modifier = Modifier.padding(16.dp),
                            iconRes = R.drawable.search_off_24px,
                            textRes = R.string.search_no_results
                        )
                    }
                }
                if (showSearchProgress.value) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LoadingIndicator(modifier = Modifier.size(64.dp))
                    }
                }

                val listModifier =
                    if (!isSearch)
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .then(
                                scrollBehaviorFromSharedVM?.let { Modifier.nestedScroll(it.nestedScrollConnection) }
                                    ?: Modifier
                            )
                    else
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()

                LazyColumn(
                    state = state,
                    modifier = listModifier,
                    verticalArrangement = Arrangement.spacedBy(if (isSearch) 2.dp else 0.dp)
                ) {
                    items(localItems.value) { simpleItem ->
                        Box(
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                        )
                        {
                            val itemContextMenuExpanded = remember { mutableStateOf(false) }
                            val offset = remember { mutableStateOf(DpOffset.Zero) }
                            SimpleListItem(
                                requireContext(),
                                simpleItem,
                                onItemClick = { rememberItemClick(it) },
                                onItemLongClick = {
                                    rememberItemLongClick(it)
                                    itemContextMenuExpanded.value = true
                                },
                                selected = false,
                                modifier = Modifier
                                    .animateItem()
                                    .onSizeChanged {
                                        offset.value = DpOffset((it.width / 12).dp, 0.dp)
                                    },
                                isInsert = isInsert,
                                onIconClick = { rememberIconClick(it) }
                            )

                            if (mCantiViewModel.tipoLista == 0 ||
                                mCantiViewModel.tipoLista == 1 ||
                                mCantiViewModel.tipoLista == 2
                            ) {
                                val tag1 = when (mCantiViewModel.tipoLista) {
                                    0 -> SimpleDialogTag.ALPHA_REPLACE
                                    1 -> SimpleDialogTag.NUMERIC_REPLACE
                                    else -> SimpleDialogTag.SALMI_REPLACE
                                }
                                val tag2 = when (mCantiViewModel.tipoLista) {
                                    0 -> SimpleDialogTag.ALPHA_REPLACE_2
                                    1 -> SimpleDialogTag.NUMERIC_REPLACE_2
                                    else -> SimpleDialogTag.SALMI_REPLACE_2
                                }
                                AddToDropDownMenu(
                                    this@SimpleIndexFragment,
                                    mCantiViewModel,
                                    tag1,
                                    tag2,
                                    listePersonalizzate,
                                    itemContextMenuExpanded.value,
                                    offset.value
                                ) { itemContextMenuExpanded.value = false }
                            }
                        }
                    }
                }

                if (mCantiViewModel.showAlertDialog.observeAsState().value == true) {
                    SimpleAlertDialog(
                        onDismissRequest = { mCantiViewModel.showAlertDialog.postValue(false) },
                        onConfirmation = {
                            Log.d(
                                TAG,
                                "mCantiViewModel.shownDialogTag ${mCantiViewModel.dialogTag}"
                            )
                            mCantiViewModel.showAlertDialog.postValue(false)
                            when (mCantiViewModel.dialogTag) {
                                SimpleDialogTag.ALPHA_REPLACE, SimpleDialogTag.NUMERIC_REPLACE, SimpleDialogTag.SALMI_REPLACE -> {
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

                                SimpleDialogTag.ALPHA_REPLACE_2, SimpleDialogTag.NUMERIC_REPLACE_2, SimpleDialogTag.SALMI_REPLACE_2 -> {
                                    ListeUtils.updatePosizione(
                                        this@SimpleIndexFragment,
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

                Log.d(
                    TAG,
                    "onCreateView: IS_SEARCH ${arguments?.getBoolean(IS_SEARCH, false)}"
                )
                if (arguments?.getBoolean(IS_SEARCH, false) == true) {
                    sharedSearchViewModel.advancedSearchFilter.observe(viewLifecycleOwner) {
                        job.cancel()
                        ricercaStringa()
                    }

                    sharedSearchViewModel.consegnatiOnlyFilter.observe(viewLifecycleOwner) {
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

                    sharedSearchViewModel.titoli.observe(viewLifecycleOwner) { _ ->
                        job.cancel()
                        ricercaStringa()
                    }

                } else {
                    mCantiViewModel.itemsResult?.observe(viewLifecycleOwner) { canti ->
                        localItems.value =
                            when (mCantiViewModel.tipoLista) {
                                0 -> canti.sortedWith(
                                    compareBy(
                                        Collator.getInstance(
                                            systemLocale
                                        )
                                    ) {
                                        getString(it.titleRes)
                                    })

                                1 -> canti.sortedBy { getString(it.pageRes).toInt() }
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
        val s = sharedSearchViewModel.searchFilter.value.orEmpty()
        Log.d(TAG, "performSearch STRINGA: $s")
        job = lifecycleScope.launch {
            // abilita il pulsante solo se la stringa ha più di 3 caratteri, senza contare gli spazi
            if (s.trim { it <= ' ' }.length >= 3) {
                showSearchProgress.value = true
                val titoliResult = ArrayList<RisuscitoListItem>()

                Firebase.crashlytics.log("function: search_text - search_string: $s - advanced: ${sharedSearchViewModel.advancedSearchFilter.value}")

                Log.d(TAG, "performSearch STRINGA: $s")
                Log.d(
                    TAG,
                    "performSearch ADVANCED: ${sharedSearchViewModel.advancedSearchFilter.value}"
                )
                Log.d(
                    TAG,
                    "performSearch CONSEGNATI ONLY: ${sharedSearchViewModel.consegnatiOnlyFilter.value}"
                )
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
                            sharedSearchViewModel.titoli.value.orEmpty()
                                .filter { (aText[0].orEmpty()) == it.undecodedSource && (sharedSearchViewModel.consegnatiOnlyFilter.value != true || it.consegnato != -1) }
                                .forEach {
                                    if (!isActive) return@launch
                                    titoliResult.add(it.apply { filter = StringUtils.EMPTY })
                                }
                        }
                    }
                } else {
                    val stringa = Utility.removeAccents(s).lowercase(systemLocale)
                    Log.d(TAG, "performSearch onTextChanged: stringa $stringa")
                    sharedSearchViewModel.titoli.value.orEmpty().filter {
                        Utility.removeAccents(
                            getString(it.titleRes)
                        ).lowercase(systemLocale)
                            .contains(stringa) && (sharedSearchViewModel.consegnatiOnlyFilter.value != true || it.consegnato != -1)
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
                    sharedSearchViewModel.itemsResultFiltered.value =
                        sharedSearchViewModel.titoli.value
                    showSearchProgress.value = false
                }
            }
        }
    }

    companion object {
        private val TAG = SimpleIndexFragment::class.java.canonicalName
        const val INDICE_LISTA = "indiceLista"

        const val IS_INSERT = "isInsert"

        const val IS_SEARCH = "isSearch"

    }

}