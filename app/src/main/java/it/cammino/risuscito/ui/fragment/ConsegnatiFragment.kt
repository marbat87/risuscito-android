package it.cammino.risuscito.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.postDelayed
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Consegnato
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.risuscitoListItem
import it.cammino.risuscito.ui.composable.AnimatedFadeContent
import it.cammino.risuscito.ui.composable.CheckableListItem
import it.cammino.risuscito.ui.composable.PassageListItem
import it.cammino.risuscito.ui.composable.dialogs.ListChoiceAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.PassaggesDropDownMenu
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleDialogTag
import it.cammino.risuscito.ui.composable.main.ActionModeItem
import it.cammino.risuscito.ui.composable.main.OptionMenuItem
import it.cammino.risuscito.ui.composable.main.consegnatiMenu
import it.cammino.risuscito.ui.composable.main.consegnatiOptionMenu
import it.cammino.risuscito.ui.composable.main.consegnatiResetOptionMenu
import it.cammino.risuscito.ui.composable.risuscito_medium_font
import it.cammino.risuscito.ui.interfaces.ActionModeFragment
import it.cammino.risuscito.ui.interfaces.FabFragment
import it.cammino.risuscito.ui.interfaces.OptionMenuFragment
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.getTypedValueResId
import it.cammino.risuscito.utils.extension.openCanto
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
    private var mRegularFont: Typeface? = null
    private var mMediumFont: Typeface? = null
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
                                false
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
                                val simpleListState = rememberLazyListState()
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
//                                if (hasGridLayout()) {
//                                    LazyVerticalGrid(
//                                        columns = Fixed(2),
//                                        modifier = listModifier
//                                    ) {
//                                        items(
//                                            consegnatiItems ?: emptyList(),
//                                            key = { it.id }) { simpleItem ->
//                                            PassageListItem(
//                                                simpleItem,
//                                                onItemClick = rememberedOnItemClick,
//                                                onIconClick = { openPassageModal(it) },
//                                                modifier = Modifier.animateItem()
//                                            )
//                                        }
//                                        item(span = { GridItemSpan(2) }) {
//                                            Spacer(Modifier.height(86.dp))
//                                        }
//                                    }
//                                } else {
                                LazyColumn(
                                    state = simpleListState,
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
//                                }
                            }

                            ConsegnatiViewModel.ViewMode.EDIT -> {
                                val checkableListState = rememberLazyListState()
                                val listModifier = Modifier
                                    .fillMaxSize()
//                                if (hasGridLayout()) {
//                                    LazyVerticalGrid(
//                                        columns = Fixed(2),
//                                        modifier = listModifier
//                                    ) {
//                                        items(
//                                            consegnatiSelectableItems ?: emptyList(),
//                                            key = { it.id }) { simpleItem ->
//                                            CheckableListItem(
//                                                simpleItem,
//                                                modifier = Modifier.animateItem(),
//                                                onSelect = {
//                                                    if (it) selectItem(simpleItem.id)
//                                                    else deselectItem(simpleItem.id)
//                                                },
//                                                selected = consegnatiSelectedItems?.contains(
//                                                    simpleItem.id
//                                                ) ?: false
//                                            )
//                                        }
//                                        item(span = { GridItemSpan(2) }) {
//                                            Spacer(Modifier.height(86.dp))
//                                        }
//                                    }
//                                } else {
                                LazyColumn(
                                    state = checkableListState,
                                    modifier = listModifier
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
//                                    }
                                }
                            }

                            ConsegnatiViewModel.ViewMode.EMPTY -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(), // Occupa solo l'altezza necessaria
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_sleeping_checklist),
                                        contentDescription = stringResource(id = R.string.no_consegnati),
                                        modifier = Modifier
                                            .size(120.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp)) // Spazio tra immagine e testo
                                    Text(
                                        text = stringResource(R.string.no_consegnati),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Colore secondario del testo
                                        fontFamily = risuscito_medium_font,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth() // Per centrare il testo se è multiriga
                                    )
                                }
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
                        consegnatiResetOptionMenu,
                        null
                    )

                    Handler(Looper.getMainLooper()).postDelayed(1) {
                        mMainActivity?.createOptionsMenu(
                            if (passaggiSelectedListState?.isEmpty() == true) consegnatiOptionMenu else consegnatiResetOptionMenu,
                            this@ConsegnatiFragment
                        )
                    }

                }

                BackHandler(backCallbackEnabled.value || mMainActivity?.isDrawerOpen() == true) {
                    Log.d(TAG, "handleOnBackPressed")
                    when {
                        mMainActivity?.isDrawerOpen() == true -> mMainActivity?.closeDrawer()
                        else -> {
                            mMainActivity?.destroyActionMode()
                            mMainActivity?.expandToolbar()
                            initFab()
                        }
                    }
                }

            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRegularFont = ResourcesCompat.getFont(
            requireContext(),
            requireContext().getTypedValueResId(R.attr.risuscito_regular_font)
        )
        mMediumFont = ResourcesCompat.getFont(
            requireContext(),
            requireContext().getTypedValueResId(R.attr.risuscito_medium_font)
        )

        mMainActivity?.setTabVisible(false)
        initFab()

        view.isFocusableInTouchMode = true
        view.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (!mSharedPrefs.getBoolean(Utility.INTRO_CONSEGNATI, false)) {
            fabIntro()
        }
    }

    private fun startCab() {
        mMainActivity?.updateActionModeTitle("")
        mMainActivity?.createActionMode(consegnatiMenu, this, true) { itemRoute ->
            when (itemRoute) {
                ActionModeItem.SELECTNONE -> {
                    consegnatiViewModel.consegnatiSelectedList.value = emptyList()
                    true
                }

                ActionModeItem.SELECTALL -> {
                    consegnatiViewModel.consegnatiFullList.value?.let { consegnati ->
                        consegnatiViewModel.consegnatiSelectedList.value =
                            (consegnati.map { it.id })
                    }
                    true
                }

                ActionModeItem.UNDO -> {
                    mMainActivity?.destroyActionMode()
                    initFab()
                    true
                }

//                ActionModeItem.HELP -> {
//                    managerIntro()
//                    true
//                }

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

    private fun fabIntro() {
        //TODO
//        mMainActivity?.getFab()?.let { fab ->
//            val colorOnPrimary =
//                MaterialColors.getColor(
//                    requireContext(),
//                    com.google.android.material.R.attr.colorOnPrimary,
//                    TAG
//                )
//            TapTargetView.showFor(
//                requireActivity(), // `this` is an Activity
//                TapTarget.forView(
//                    fab,
//                    getString(R.string.title_activity_consegnati),
//                    getString(R.string.showcase_consegnati_howto)
//                )
//                    .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                    .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                    .titleTypeface(mMediumFont) // Specify a typeface for the text
//                    .titleTextColorInt(colorOnPrimary)
//                    .textColorInt(colorOnPrimary)
//                    .tintTarget(false) // Whether to tint the target view's color
//                    .setForceCenteredTarget(true),
//                object :
//                    TapTargetView.Listener() { // The listener can listen for regular clicks, long clicks or cancels
//                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
//                        super.onTargetDismissed(view, userInitiated)
//                        context?.let {
//                            PreferenceManager.getDefaultSharedPreferences(it)
//                                .edit { putBoolean(Utility.INTRO_CONSEGNATI, true) }
//                        }
//                    }
//                })
//        }
    }

//    private fun managerIntro() {
//        val colorOnPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnPrimary, TAG)
//        mMainActivity?.getFab()?.let { fab ->
//            TapTargetSequence(requireActivity())
//                .continueOnCancel(true)
//                .targets(
//                    TapTarget.forView(
//                        fab,
//                        getString(R.string.title_activity_consegnati),
//                        getString(R.string.showcase_consegnati_confirm)
//                    )
//                        .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                        .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                        .titleTypeface(mMediumFont) // Specify a typeface for the text
//                        .titleTextColorInt(colorOnPrimary)
//                        .textColorInt(colorOnPrimary)
//                        .tintTarget(false)
//                        .setForceCenteredTarget(true),
//                    TapTarget.forToolbarMenuItem(
//                        mMainActivity?.activityContextualToolbar,
//                        R.id.cancel_change,
//                        getString(R.string.title_activity_consegnati),
//                        getString(R.string.showcase_consegnati_cancel)
//                    )
//                        .targetCircleColorInt(colorOnPrimary) // Specify a color for the target circle
//                        .descriptionTypeface(mRegularFont) // Specify a typeface for the text
//                        .titleTypeface(mMediumFont) // Specify a typeface for the text
//                        .titleTextColorInt(colorOnPrimary)
//                        .textColorInt(colorOnPrimary)
//                        .setForceCenteredTarget(true)
//                )
//                .listener(
//                    object :
//                        TapTargetSequence.Listener { // The listener can listen for regular clicks, long clicks or cancels
//                        override fun onSequenceFinish() {
//                            context?.let {
//                                PreferenceManager.getDefaultSharedPreferences(it)
//                                    .edit { putBoolean(Utility.INTRO_CONSEGNATI_2, true) }
//                            }
//                        }
//
//                        override fun onSequenceStep(tapTarget: TapTarget, b: Boolean) {
//                            // no-op
//                        }
//
//                        override fun onSequenceCanceled(tapTarget: TapTarget) {
//                            context?.let {
//                                PreferenceManager.getDefaultSharedPreferences(it)
//                                    .edit { putBoolean(Utility.INTRO_CONSEGNATI_2, true) }
//                            }
//                        }
//                    })
//                .start()
//        }
//    }

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
        mMainActivity?.let { activity ->
            consegnatiViewModel.mIdConsegnatoSelected = item.idConsegnato
            consegnatiViewModel.mIdCantoSelected = item.id
            consegnatiViewModel.dialogPrefill = item.numPassaggio
            consegnatiViewModel.dialogTag = SimpleDialogTag.ADD_PASSAGE_DIALOG
            consegnatiViewModel.showAlertDialog.value = true
        }
    }

    override fun onItemClick(route: String) {
        when (route) {
            OptionMenuItem.Filter.route -> {
                contextMenuExpanded.value = true
            }

            OptionMenuItem.FilterRemove.route -> {
                consegnatiViewModel.passaggiSelectedList.value = emptyList()
            }

            OptionMenuItem.Help.route -> {
                fabIntro()
            }
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
