package it.cammino.risuscito.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Consegnato
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.risuscitoListItem
import it.cammino.risuscito.ui.composable.CheckableListItem
import it.cammino.risuscito.ui.composable.EmptyListView
import it.cammino.risuscito.ui.composable.PassageListItem
import it.cammino.risuscito.ui.composable.animations.AnimatedFadeContent
import it.cammino.risuscito.ui.composable.dialogs.ListChoiceAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.PassaggesDropDownMenu
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleDialogTag
import it.cammino.risuscito.ui.composable.hasTwoPanes
import it.cammino.risuscito.ui.composable.main.ActionModeItem
import it.cammino.risuscito.ui.composable.main.OptionMenuItem
import it.cammino.risuscito.ui.composable.main.consegnatiMenu
import it.cammino.risuscito.ui.composable.main.consegnatiOptionMenu
import it.cammino.risuscito.ui.composable.main.consegnatiResetOptionMenu
import it.cammino.risuscito.ui.interfaces.ActionModeFragment
import it.cammino.risuscito.ui.interfaces.FabFragment
import it.cammino.risuscito.ui.interfaces.OptionMenuFragment
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.ConsegnatiViewModel
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator

class ConsegnatiFragment : RisuscitoFragment(), ActionModeFragment, OptionMenuFragment,
    FabFragment {

    private val consegnatiViewModel: ConsegnatiViewModel by viewModels()

    private val sharedScrollViewModel: SharedScrollViewModel by activityViewModels()

    private val contextMenuExpanded = mutableStateOf(false)
    private var backCallbackEnabled = mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val consegnatiItems by consegnatiViewModel.consegnatiSortedList.observeAsState()
                val consegnatiSelectableItems by consegnatiViewModel.consegnatiFullList.observeAsState()
                val consegnatiSelectedItems by consegnatiViewModel.consegnatiSelectedList.observeAsState()
                val passaggiSelectedListState by consegnatiViewModel.passaggiSelectedList.observeAsState()
                val scrollBehaviorFromSharedVM by sharedScrollViewModel.scrollBehavior.collectAsState()

                val viewMode by remember { consegnatiViewModel.viewMode }

                val hasTwoPanes = hasTwoPanes()

                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    // In SectionedIndexFragment, dentro setContent
                    val rememberedOnItemClick = remember<(RisuscitoListItem) -> Unit> {
                        { item ->
                            mMainActivity?.openCanto(
                                TAG,
                                item.id,
                                getString(item.sourceRes),
                                !hasTwoPanes
                            )
                        }
                    }

                    AnimatedFadeContent(viewMode)
                    { targetState ->
                        when (targetState) {
                            ConsegnatiViewModel.ViewMode.LOADING -> {
                                LoadingIndicator()
                            }

                            ConsegnatiViewModel.ViewMode.VIEW -> {
                                val listModifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        scrollBehaviorFromSharedVM?.let {
                                            Modifier.nestedScroll(
                                                it.nestedScrollConnection
                                            )
                                        }
                                            ?: Modifier
                                    )
                                LazyColumn(
                                    state = rememberLazyListState(),
                                    modifier = listModifier
                                ) {
                                    items(
                                        consegnatiItems ?: emptyList(),
                                        key = { it.id }) { simpleItem ->
                                        PassageListItem(
                                            simpleItem,
                                            onItemClick = rememberedOnItemClick,
                                            onIconClick = { openPassageModal(it) },
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                    item {
                                        Spacer(Modifier.height(86.dp))
                                    }
                                }
                            }

                            ConsegnatiViewModel.ViewMode.EDIT -> {
                                LazyColumn(
                                    state = rememberLazyListState(),
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    items(
                                        consegnatiSelectableItems ?: emptyList(),
                                        key = { it.id }) { simpleItem ->
                                        CheckableListItem(
                                            simpleItem,
                                            modifier = Modifier.animateItem(),
                                            onSelect = {
                                                if (it) selectItem(simpleItem.id)
                                                else deselectItem(simpleItem.id)
                                            },
                                            selected = consegnatiSelectedItems?.contains(
                                                simpleItem.id
                                            ) ?: false
                                        )
                                    }
                                    item {
                                        Spacer(Modifier.height(86.dp))
                                    }
                                }
                            }

                            ConsegnatiViewModel.ViewMode.EMPTY -> {
                                EmptyListView(
                                    iconRes = R.drawable.assignment_turned_in_24px,
                                    textRes = R.string.no_consegnati
                                )
                            }
                        }
                    }
                }

                val passaggiArray = integerArrayResource(R.array.passaggi_values)
                val passaggiTitle = stringArrayResource(R.array.passaggi_entries)

                if (consegnatiViewModel.showAlertDialog.observeAsState().value == true) {
                    when (consegnatiViewModel.dialogTag) {
                        SimpleDialogTag.SAVE_CONSEGNATI_DIALOG -> {
                            SimpleAlertDialog(
                                onDismissRequest = {
                                    consegnatiViewModel.showAlertDialog.postValue(false)
                                },
                                onConfirmation = {
                                    consegnatiViewModel.showAlertDialog.postValue(false)
                                    mMainActivity?.destroyActionMode()
                                    lifecycleScope.launch { saveConsegnati() }
                                },
                                dialogTitle = stringResource(R.string.dialog_save_consegnati_title),
                                dialogText = stringResource(R.string.dialog_save_consegnati_desc),
                                iconRes = R.drawable.save_24px,
                                confirmButtonText = stringResource(R.string.action_salva),
                                dismissButtonText = stringResource(R.string.cancel)
                            )
                        }

                        SimpleDialogTag.ADD_PASSAGE_DIALOG -> {
                            ListChoiceAlertDialog(
                                onDismissRequest = {
                                    consegnatiViewModel.showAlertDialog.postValue(false)
                                },
                                onConfirmation = { passaggio ->
                                    consegnatiViewModel.showAlertDialog.postValue(false)
                                    val consegnato = Consegnato().apply {
                                        idConsegnato =
                                            consegnatiViewModel.mIdConsegnatoSelected
                                        idCanto = consegnatiViewModel.mIdCantoSelected
                                        numPassaggio = passaggio
                                    }
                                    val mDao =
                                        RisuscitoDatabase.getInstance(requireContext())
                                            .consegnatiDao()
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        mDao.updateConsegnato(
                                            consegnato
                                        )
                                    }
                                },
                                nomiPassaggi = passaggiTitle,
                                indiciPassaggi = passaggiArray,
                                passaggioSelezionato = consegnatiViewModel.dialogPrefill
                            )
                        }

                        else -> {}
                    }
                }

                PassaggesDropDownMenu(
                    contextMenuExpanded.value,
                    passaggiTitle,
                    passaggiArray,
                    consegnatiViewModel.passaggiSelectedList,
                    { contextMenuExpanded.value = false }
                ) { item, selected ->
                    if (selected) selectPassaggiItem(item) else deselectPassaggiItem(
                        item
                    )
                }

                consegnatiViewModel.consegnatiList?.observe(viewLifecycleOwner) { canti ->
                    consegnatiViewModel.consegnatiSortedList.value = canti.sortedWith(
                        compareBy(
                            Collator.getInstance(systemLocale)
                        ) { getString(it.titleRes) }).filter { consegnato ->
                        consegnatiViewModel.passaggiSelectedList.value?.isEmpty() == true
                                || consegnatiViewModel.passaggiSelectedList.value?.any { it == consegnato.numPassaggio } == true
                    }
                    consegnatiViewModel.viewMode.value =
                        if (canti.isNotEmpty()) ConsegnatiViewModel.ViewMode.VIEW else ConsegnatiViewModel.ViewMode.EMPTY

                    if (canti.isEmpty())
                        mMainActivity?.expandToolbar()
                }

                LaunchedEffect(passaggiSelectedListState) {
                    consegnatiViewModel.consegnatiSortedList.value =
                        consegnatiViewModel.consegnatiList?.value?.sortedWith(
                            compareBy(
                                Collator.getInstance(systemLocale)
                            ) { getString(it.titleRes) })?.filter { consegnato ->
                            passaggiSelectedListState?.isEmpty() == true ||
                                    passaggiSelectedListState?.any { it == consegnato.numPassaggio }
                                    ?: false
                        }

                    mMainActivity?.createOptionsMenu(
                        if (passaggiSelectedListState?.isEmpty() == true) consegnatiOptionMenu else consegnatiResetOptionMenu,
                        this@ConsegnatiFragment
                    )

                }

                BackHandler(backCallbackEnabled.value) {
                    Log.d(TAG, "handleOnBackPressed")
                    mMainActivity?.destroyActionMode()
                    mMainActivity?.expandToolbar()
                    initFab()
                }

            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity?.setTabVisible(false)
        initFab()

    }

    private fun startCab() {
        mMainActivity?.updateActionModeTitle("")
        mMainActivity?.createActionMode(consegnatiMenu, this, true) { itemRoute ->
            when (itemRoute) {
                ActionModeItem.SELECTNONE -> {
                    consegnatiViewModel.consegnatiSelectedList.value = emptyList()
                }

                ActionModeItem.SELECTALL -> {
                    consegnatiViewModel.consegnatiFullList.value?.let { consegnati ->
                        consegnatiViewModel.consegnatiSelectedList.value =
                            (consegnati.map { it.id })
                    }
                }

                ActionModeItem.UNDO -> {
                    mMainActivity?.destroyActionMode()
                    initFab()
                }

                else -> {}
            }
        }
        backCallbackEnabled.value = true
    }

    override fun destroyActionMode() {
        consegnatiViewModel.viewMode.value =
            if (consegnatiViewModel.consegnatiList?.value?.isNotEmpty() == true) ConsegnatiViewModel.ViewMode.VIEW else ConsegnatiViewModel.ViewMode.EMPTY
        backCallbackEnabled.value = false
    }

    private fun initFab() {
        mMainActivity?.initFab(
            enable = true,
            fragment = this,
            iconRes = if (consegnatiViewModel.viewMode.value == ConsegnatiViewModel.ViewMode.EDIT) R.drawable.save_24px else R.drawable.edit_24px
        )
    }

    private suspend fun updateChooseList() {
        Log.i(TAG, "updateChooseList start")
        val mDao = RisuscitoDatabase.getInstance(requireContext()).consegnatiDao()
        val canti = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.choosen() }
        consegnatiViewModel.consegnatiFullList.value = canti.map { canto ->
            risuscitoListItem(
                titleRes = Utility.getResId(canto.titolo, R.string::class.java)
            ) {
                pageRes = Utility.getResId(
                    canto.pagina,
                    R.string::class.java
                )
                setColor = canto.color
                id = canto.id
            }
        }.sortedWith(
            compareBy(
                Collator.getInstance(systemLocale)
            ) { getString(it.titleRes) })
        consegnatiViewModel.consegnatiSelectedList.value =
            canti.filter { it.consegnato != -1 }.map { it.id }
    }

    private suspend fun saveConsegnati() {
        consegnatiViewModel.viewMode.value = ConsegnatiViewModel.ViewMode.LOADING

//        val mSelected = selectExtension.selectedItems
//        val mSelectedId = mSelected.mapTo(ArrayList()) { item -> item.id }
//
//        //IMPORTANTE PER AGGIUNGERE ALLA LISTA DEGLI ID SELEZIONATI (FILTRATI) ANCHCE QUELLI CHE AL MOMENTO NON SONO VISIBILI (MA SELEZIONATI COMUNQUE)
//        consegnatiViewModel.titoliChoose.forEach { item ->
//            if (item.isSelected)
//                if (!mSelectedId.any { i -> i == item.id })
//                    mSelectedId.add(item.id)
//        }

        val mDao = RisuscitoDatabase.getInstance(requireContext()).consegnatiDao()
        val consegnati = ArrayList<Consegnato>()
        for ((i, id) in consegnatiViewModel.consegnatiSelectedList.value?.withIndex()!!) {
            val tempConsegnato = Consegnato()
            tempConsegnato.idConsegnato = i
            tempConsegnato.idCanto = id
            tempConsegnato.numPassaggio =
                withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                    mDao.getNumPassaggio(id)
                }
            consegnati.add(tempConsegnato)
        }
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.emptyConsegnati()
            mDao.insertConsegnati(consegnati)
        }
        initFab()
    }

    private fun openPassageModal(item: RisuscitoListItem) {
        consegnatiViewModel.mIdConsegnatoSelected = item.idConsegnato
        consegnatiViewModel.mIdCantoSelected = item.id
        consegnatiViewModel.dialogPrefill = item.numPassaggio
        consegnatiViewModel.dialogTag = SimpleDialogTag.ADD_PASSAGE_DIALOG
        consegnatiViewModel.showAlertDialog.value = true
    }

    override fun onItemClick(route: String) {
        when (route) {
            OptionMenuItem.Filter.route -> {
                contextMenuExpanded.value = true
            }

            OptionMenuItem.FilterRemove.route -> {
                consegnatiViewModel.passaggiSelectedList.value = emptyList()
            }

//            OptionMenuItem.Help.route -> {
//                fabIntro()
//            }
        }
    }

    override fun onFabClick(item: String) {
        if (consegnatiViewModel.viewMode.value == ConsegnatiViewModel.ViewMode.EDIT) {
            consegnatiViewModel.dialogTag = SimpleDialogTag.SAVE_CONSEGNATI_DIALOG
            consegnatiViewModel.showAlertDialog.value = true
        } else {
            consegnatiViewModel.viewMode.value = ConsegnatiViewModel.ViewMode.EDIT
            mMainActivity?.expandToolbar()
            startCab()
            initFab()
            lifecycleScope.launch { updateChooseList() }
//            val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
//            if (!mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI_2, false)) {
//                managerIntro()
//            }
        }
    }

    private fun selectItem(id: Int) {
        val currentSelected = consegnatiViewModel.consegnatiSelectedList.value ?: ArrayList()
        val newSelected = ArrayList(currentSelected) // Crea una nuova lista
        newSelected.add(id)
        consegnatiViewModel.consegnatiSelectedList.value = newSelected // Assegna la nuova lista
    }

    private fun deselectItem(id: Int) {
        val currentSelected = consegnatiViewModel.consegnatiSelectedList.value ?: ArrayList()
        val newSelected = ArrayList(currentSelected) // Crea una nuova lista
        newSelected.remove(id)
        consegnatiViewModel.consegnatiSelectedList.value = newSelected // Assegna la nuova lista
    }

    private fun selectPassaggiItem(id: Int) {
        val currentSelected = consegnatiViewModel.passaggiSelectedList.value ?: ArrayList()
        val newSelected = ArrayList(currentSelected) // Crea una nuova lista
        newSelected.add(id)
        consegnatiViewModel.passaggiSelectedList.value = newSelected // Assegna la nuova lista
    }

    private fun deselectPassaggiItem(id: Int) {
        val currentSelected = consegnatiViewModel.passaggiSelectedList.value ?: ArrayList()
        val newSelected = ArrayList(currentSelected) // Crea una nuova lista
        newSelected.remove(id)
        consegnatiViewModel.passaggiSelectedList.value = newSelected // Assegna la nuova lista
    }

    companion object {
        private val TAG = ConsegnatiFragment::class.java.canonicalName
    }
}
