package it.cammino.risuscito.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.transition.platform.MaterialSharedAxis
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.database.pojo.Posizione
import it.cammino.risuscito.items.ListaPersonalizzataPositionListItem
import it.cammino.risuscito.items.ListaPersonalizzataRisuscitoListItem
import it.cammino.risuscito.items.listaPersonalizzataPositionListItem
import it.cammino.risuscito.items.listaPersonalizzataRisuscitoListItem
import it.cammino.risuscito.ui.activity.InsertActivity
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.composable.PosizioneListItem
import it.cammino.risuscito.ui.composable.dialogs.InputDialog
import it.cammino.risuscito.ui.composable.main.ActionModeItem
import it.cammino.risuscito.ui.composable.main.customListsMenu
import it.cammino.risuscito.ui.dialog.BottomSheetFragment
import it.cammino.risuscito.ui.interfaces.ActionModeFragment
import it.cammino.risuscito.ui.interfaces.FabActionsFragment
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.launchForResultWithAnimation
import it.cammino.risuscito.utils.extension.openCanto
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.DefaultListaViewModel
import it.cammino.risuscito.viewmodels.InputDialogManagerViewModel
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

class ListaPredefinitaFragment : Fragment(), ActionModeFragment, FabActionsFragment,
    SnackBarFragment {

    private val mCantiViewModel: DefaultListaViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply {
            putInt(Utility.TIPO_LISTA, arguments?.getInt(INDICE_LISTA, 0) ?: 0)
        })
    }
    private val inputdialogViewModel: InputDialogManagerViewModel by viewModels()

    private var posizioneDaCanc: Int = 0
    private var notaDaCanc: String = StringUtils.EMPTY
    private var idDaCanc: Int = 0
    private var timestampDaCanc: String? = null
    private var mSwhitchMode: Boolean = false
    private var backCallbackEnabled = mutableStateOf(false)
    private var longclickedPos: Int = 0
    private var longClickedChild: Int = 0
    private var mMainActivity: MainActivity? = null

    private val sharedScrollViewModel: SharedScrollViewModel by activityViewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = rememberLazyListState()
                val localItems by mCantiViewModel.posizioniList.observeAsState()

                val scrollBehaviorFromSharedVM by sharedScrollViewModel.scrollBehavior.collectAsState()

                val showInputDialog by inputdialogViewModel.showAlertDialog.observeAsState()

                val rememberAddClick = remember<(Int) -> Unit> {
                    { idPosizione ->
                        if (mSwhitchMode) {
                            mMainActivity?.destroyActionMode()
                            scambioConVuoto(idPosizione)
                        } else {
                            if (mMainActivity?.isActionMode?.value != true) {
                                mMainActivity?.let {
                                    it.launchForResultWithAnimation(
                                        startListInsertForResult,
                                        Intent(it, InsertActivity::class.java).putExtras(
                                            bundleOf(
                                                InsertActivity.FROM_ADD to 1,
                                                InsertActivity.ID_LISTA to mCantiViewModel.defaultListaId,
                                                InsertActivity.POSITION to idPosizione
                                            )
                                        ),
                                        MaterialSharedAxis.Y
                                    )
                                }
                            }
                        }
                    }
                }

                val rememberItemClick = remember<(ListaPersonalizzataRisuscitoListItem) -> Unit> {
                    { item ->
                        if (!mSwhitchMode)
                            if (mMainActivity?.isActionMode?.value == true) {
                                posizioneDaCanc = item.idPosizione
                                idDaCanc = item.id
                                timestampDaCanc = item.timestamp
                                notaDaCanc = item.nota
                                snackBarRimuoviCanto(item)
                            } else {
                                //apri canto
                                mMainActivity?.openCanto(
                                    TAG,
                                    item.id,
                                    getString(item.sourceRes),
                                    false
                                )
                            }
                        else {
                            mMainActivity?.destroyActionMode()
                            scambioCanto(
                                item.idPosizione,
                                item.id,
                                item.nota
                            )
                        }
                    }
                }

                val rememberItemLongClick =
                    remember<(ListaPersonalizzataRisuscitoListItem) -> Unit> {
                        { item ->
                            posizioneDaCanc = item.idPosizione
                            idDaCanc = item.id
                            timestampDaCanc = item.timestamp
                            notaDaCanc = item.nota
                            snackBarRimuoviCanto(item)
                        }
                    }

                val rememberNoteClick = remember<(ListaPersonalizzataRisuscitoListItem) -> Unit> {
                    { item ->
                        inputdialogViewModel.outputItemId = item.idPosizione
                        inputdialogViewModel.outputCantoId = item.id
                        inputdialogViewModel.dialogPrefill = item.nota
                        inputdialogViewModel.showAlertDialog.value = true
                    }
                }

                val rememberConfirmEditNote = remember<(String) -> Unit> {
                    { text ->
                        inputdialogViewModel.showAlertDialog.value = false
                        Log.d(TAG, "inputdialogViewModel.outputText $text")
                        Log.d(
                            TAG,
                            " mCantiViewModel.defaultListaId ${mCantiViewModel.defaultListaId}"
                        )
                        Log.d(
                            TAG,
                            " inputdialogViewModel.outputItemId ${inputdialogViewModel.outputItemId}"
                        )
                        Log.d(
                            TAG,
                            " inputdialogViewModel.outputCantoId ${inputdialogViewModel.outputCantoId}"
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
                            val mDao =
                                RisuscitoDatabase.getInstance(requireContext())
                                    .customListDao()
                            mDao.updateNotaPosition(
                                text,
                                mCantiViewModel.defaultListaId,
                                inputdialogViewModel.outputItemId,
                                inputdialogViewModel.outputCantoId
                            )
                        }
                        showSnackBar(R.string.edit_note_confirm_message)
                    }
                }

                LazyColumn(
                    state = state,
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            scrollBehaviorFromSharedVM?.let {
                                Modifier.nestedScroll(
                                    it.nestedScrollConnection
                                )
                            }
                                ?: Modifier
                        )
                ) {
                    items(localItems.orEmpty()) { posizione ->
                        PosizioneListItem(
                            titoloPosizione = posizione.titoloPosizione,
                            idPosizione = posizione.idPosizione,
                            isMultiple = posizione.isMultiple,
                            posizioni = posizione.posizioni,
                            addClickListener = rememberAddClick,
                            cantoClickListener = rememberItemClick,
                            cantoLongClickListener = rememberItemLongClick,
                            noteClickListener = rememberNoteClick
                        )
                    }

                    item {
                        Spacer(Modifier.height(112.dp))
                    }
                }

                if (showInputDialog == true) {
                    InputDialog(
                        dialogTitleRes = R.string.edit_note_title,
                        onDismissRequest = { inputdialogViewModel.showAlertDialog.value = false },
                        onConfirmation = { _, text -> rememberConfirmEditNote(text) },
                        confirmationTextRes = R.string.action_salva,
                        prefill = inputdialogViewModel.dialogPrefill,
                        multiline = true
                    )
                }

                mCantiViewModel.cantiResult?.observe(viewLifecycleOwner) { mCanti ->
                    var progressiveTag = 0
                    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val newList = ArrayList<ListaPersonalizzataPositionListItem>()

                    when (mCantiViewModel.defaultListaId) {
                        1 -> {
                            newList.add(
                                getCantofromPosition(
                                    mCanti,
                                    getString(R.string.canto_iniziale),
                                    1,
                                    progressiveTag++
                                )
                            )
                            newList.add(
                                getCantofromPosition(
                                    mCanti,
                                    getString(R.string.prima_lettura),
                                    2,
                                    progressiveTag++
                                )
                            )
                            newList.add(
                                getCantofromPosition(
                                    mCanti,
                                    getString(R.string.seconda_lettura),
                                    3,
                                    progressiveTag++
                                )
                            )
                            newList.add(
                                getCantofromPosition(
                                    mCanti,
                                    getString(R.string.terza_lettura),
                                    4,
                                    progressiveTag++
                                )
                            )

                            if (pref.getBoolean(Utility.SHOW_PACE, false))
                                newList.add(
                                    getCantofromPosition(
                                        mCanti,
                                        getString(R.string.canto_pace),
                                        6,
                                        progressiveTag++
                                    )
                                )

                            newList.add(
                                getCantofromPosition(
                                    mCanti,
                                    getString(R.string.canto_fine),
                                    5,
                                    progressiveTag
                                )
                            )
                        }

                        2 -> {
                            newList.add(
                                getCantofromPosition(
                                    mCanti,
                                    getString(R.string.canto_iniziale),
                                    1,
                                    progressiveTag++
                                )
                            )

                            if (pref.getBoolean(Utility.SHOW_SECONDA, false))
                                newList.add(
                                    getCantofromPosition(
                                        mCanti,
                                        getString(R.string.seconda_lettura),
                                        6,
                                        progressiveTag++
                                    )
                                )

                            if (pref.getBoolean(Utility.SHOW_EUCARESTIA_PACE, true))
                                newList.add(
                                    getCantofromPosition(
                                        mCanti,
                                        getString(R.string.canto_pace),
                                        2,
                                        progressiveTag++
                                    )
                                )

                            if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false))
                                newList.add(
                                    getCantofromPosition(
                                        mCanti,
                                        getString(R.string.canto_offertorio),
                                        8,
                                        progressiveTag++
                                    )
                                )

                            if (pref.getBoolean(Utility.SHOW_SANTO, false))
                                newList.add(
                                    getCantofromPosition(
                                        mCanti,
                                        getString(R.string.santo),
                                        7,
                                        progressiveTag++
                                    )
                                )

                            newList.add(
                                getCantofromPosition(
                                    mCanti, getString(R.string.canto_pane), 3, progressiveTag++
                                )
                            )
                            newList.add(
                                getCantofromPosition(
                                    mCanti, getString(R.string.canto_vino), 4, progressiveTag++
                                )
                            )
                            newList.add(
                                getCantofromPosition(
                                    mCanti,
                                    getString(R.string.canto_fine),
                                    5,
                                    progressiveTag
                                )
                            )
                        }
                    }
                    mCantiViewModel.posizioniList.value = newList
                }

                BackHandler(backCallbackEnabled.value) {
                    Log.d(TAG, "handleOnBackPressed")
                    mMainActivity?.destroyActionMode()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMainActivity = activity as? MainActivity
        mSwhitchMode = false
        mMainActivity?.setFabActionsFragment(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMainActivity?.destroyActionMode()
    }

    private fun getCantofromPosition(
        posizioni: List<Posizione>, title: String, position: Int, tag: Int
    ): ListaPersonalizzataPositionListItem {
        val listItem = posizioni.filter { it.position == position }.mapIndexed { index, it ->
            listaPersonalizzataRisuscitoListItem(
                titleRes = Utility.getResId(it.titolo, R.string::class.java),
                nota = it.notaPosizione,
                selected = false,
                timestamp = it.timestamp?.time.toString()
            ) {
                pageRes = Utility.getResId(
                    it.pagina,
                    R.string::class.java
                )
                sourceRes =
                    Utility.getResId(it.source, R.string::class.java)
                setColor = it.color
                id = it.id
                idPosizione = position
                tagPosizione = tag
                itemTag = index
            }
        }

        return listaPersonalizzataPositionListItem(
            titoloPosizione = title,
            idPosizione = position,
            tagPosizione = tag,
            isMultiple = if (mCantiViewModel.defaultListaId == 2)
                (position == 4 || position == 3)
            else
                false,
            posizioni = listItem
        )
    }

    // recupera il titolo del canto in posizione "position" nella lista "list"
    private fun getTitoloToSendFromPosition(position: Int): String {
        val result = StringBuilder()

        val items = mCantiViewModel.posizioniList.value.orEmpty()[position].posizioni

        if (items.isNotEmpty()) {
            for (tempItem in items) {
                result
                    .append(getString(tempItem.titleRes))
                    .append(" - ")
                    .append(getString(R.string.page_contracted))
                    .append(getString(tempItem.pageRes))
                if (tempItem.nota.isNotEmpty())
                    result.append(" (")
                        .append(tempItem.nota)
                        .append(")")
                result.append("\n")
            }
        } else {
            result.append(">> ").append(getString(R.string.to_be_chosen)).append(" <<")
            result.append("\n")
        }

        return result.toString()
    }

    private fun snackBarRimuoviCanto(item: ListaPersonalizzataRisuscitoListItem) {
        mMainActivity?.destroyActionMode()
        longclickedPos = item.tagPosizione
        longClickedChild = item.itemTag
        Log.d(
            TAG,
            "snackBarRimuoviCanto - longclickedPos: $longclickedPos / defaultListaId: ${mCantiViewModel.defaultListaId} / longCLickedChild: $longClickedChild"
        )
        startCab()
    }

    fun scambioCanto(
        newPosition: Int,
        newId: Int,
        newNota: String
    ) {
        lifecycleScope.launch {
            if (newId != idDaCanc || posizioneDaCanc != newPosition) {
                val mDao = RisuscitoDatabase.getInstance(requireContext()).customListDao()
                val existingOldTitle = lifecycleScope.async(Dispatchers.IO) {
                    mDao.checkExistsPosition(mCantiViewModel.defaultListaId, newPosition, idDaCanc)
                }
                val existingNewTitle = lifecycleScope.async(Dispatchers.IO) {
                    mDao.checkExistsPosition(mCantiViewModel.defaultListaId, posizioneDaCanc, newId)
                }
                if ((existingOldTitle.await() > 0
                            || existingNewTitle.await() > 0)
                    && newPosition != posizioneDaCanc
                ) {
                    showSnackBar(R.string.present_yet)
                } else {
                    val positionToDelete =
                        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                            mDao.getPositionSpecific(
                                mCantiViewModel.defaultListaId,
                                newPosition,
                                newId
                            )
                        }
                    withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                        mDao.deletePosition(
                            positionToDelete
                        )
                    }

                    val positionToInsert = CustomList()
                    positionToInsert.id = mCantiViewModel.defaultListaId
                    positionToInsert.position = newPosition
                    positionToInsert.idCanto = idDaCanc
                    positionToInsert.timestamp = positionToDelete.timestamp
                    positionToInsert.notaPosizione = notaDaCanc

                    withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                        mDao.updatePositionNoTimestamp(
                            newId,
                            newNota,
                            mCantiViewModel.defaultListaId,
                            posizioneDaCanc,
                            idDaCanc
                        )
                    }
                    withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                        mDao.insertPosition(
                            positionToInsert
                        )
                    }
                    showSnackBar(R.string.switch_done)
                }
            } else
                showSnackBar(R.string.switch_impossible)
        }
    }

    fun scambioConVuoto(
        newPosition: Int
    ) {
        lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(requireContext()).customListDao()
            val existingTitle =
                withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                    mDao.checkExistsPosition(mCantiViewModel.defaultListaId, newPosition, idDaCanc)
                }
            if (existingTitle > 0)
                showSnackBar(R.string.present_yet)
            else {
                withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                    val positionToDelete =
                        mDao.getPositionSpecific(
                            mCantiViewModel.defaultListaId,
                            posizioneDaCanc,
                            idDaCanc
                        )
                    mDao.deletePosition(positionToDelete)
                    val positionToInsert = CustomList()
                    positionToInsert.id = mCantiViewModel.defaultListaId
                    positionToInsert.position = newPosition
                    positionToInsert.idCanto = idDaCanc
                    positionToInsert.timestamp = Date(System.currentTimeMillis())
                    positionToInsert.notaPosizione = notaDaCanc
                    mDao.insertPosition(positionToInsert)
                }
                showSnackBar(R.string.switch_done)
            }
        }
    }

    private fun startCab() {
        mSwhitchMode = false
        selectItem(true)
        mMainActivity?.createActionMode(customListsMenu, this) { itemRoute ->
            when (itemRoute) {
                ActionModeItem.DELETE -> {
                    mMainActivity?.destroyActionMode()
                    removePositionWithUndo()
                    true
                }

                ActionModeItem.SWAP -> {
                    mSwhitchMode = true
                    updateActionModeTitle(true)
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.switch_tooltip),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    true
                }

                ActionModeItem.CLOSE -> {
                    mMainActivity?.destroyActionMode()
                    true
                }

                else -> {}
            }
        }
        updateActionModeTitle(false)
        backCallbackEnabled.value = true
    }

    fun removePositionWithUndo() {
        lifecycleScope.launch {
            val positionToDelete = CustomList()
            positionToDelete.id = mCantiViewModel.defaultListaId
            positionToDelete.position = posizioneDaCanc
            positionToDelete.idCanto = idDaCanc
            val mDao = RisuscitoDatabase.getInstance(requireContext()).customListDao()
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.deletePosition(
                    positionToDelete
                )
            }
            showSnackBar(R.string.song_removed, R.string.cancel)
        }
    }

    private fun selectItem(selected: Boolean) {
        Log.d(
            ListaPersonalizzataFragment.Companion.TAG,
            "selectItem: $selected / longclickedPos: $longclickedPos / longClickedChild: $longClickedChild"
        )
        val currentList = mCantiViewModel.posizioniList.value.orEmpty()

        // 1. Usa `mapIndexed` per creare una NUOVA lista di `ListaPersonalizzataPositionListItem`.
        val newList = currentList.mapIndexed { positionIndex, positionItem ->
            if (positionIndex == longclickedPos) {
                // È la posizione che contiene l'elemento da modificare. Dobbiamo creare una copia di questo `positionItem`.

                // 2. Crea la NUOVA lista interna di canti (`ListaPersonalizzataRisuscitoListItem`)
                val newCantiList = positionItem.posizioni.mapIndexed { cantoIndex, cantoItem ->
                    if (cantoIndex == longClickedChild) {
                        // È il canto da modificare. Creiamo una sua copia con la proprietà `selected` aggiornata.
                        cantoItem.copy(selected = selected).apply {
                            pageRes = cantoItem.pageRes
                            sourceRes = cantoItem.sourceRes
                            color = cantoItem.color
                            id = cantoItem.id
                            idPosizione = cantoItem.idPosizione
                            tagPosizione = cantoItem.tagPosizione
                            itemTag = cantoItem.itemTag
                        }
                    } else {
                        // Questo canto non è modificato, manteniamo l'istanza originale.
                        cantoItem
                    }
                }

                // 3. Crea una copia del `ListaPersonalizzataPositionListItem` usando la nuova lista di canti appena creata.
                positionItem.copy(posizioni = newCantiList)
            } else {
                // Questa posizione non è modificata, manteniamo l'istanza originale.
                positionItem
            }
        }

        // 4. Assegna la NUOVA lista (che contiene NUOVE istanze degli oggetti modificati) al LiveData.
        // Compose ora vedrà un cambiamento e si ricomporrà correttamente.
        mCantiViewModel.posizioniList.value = newList
    }

    override fun destroyActionMode() {
        Log.d(
            TAG,
            "MaterialCab onDestroy - longclickedPos: $longclickedPos / defaultListaId: ${mCantiViewModel.defaultListaId} "
        )
        mSwhitchMode = false
        selectItem(false)
        backCallbackEnabled.value = false
    }

    private fun updateActionModeTitle(switchMode: Boolean) {
        mMainActivity?.updateActionModeTitle(
            if (switchMode)
                resources.getString(R.string.switch_started)
            else
                resources.getQuantityString(R.plurals.item_selected, 1, 1)
        )
    }

    private val defaultIntent: Intent
        get() {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, titlesList)
            intent.type = "text/plain"
            return intent
        }

    private val titlesList: String
        get() {
            val l = systemLocale
            val result = StringBuilder()
            var progressivePos = 0

            when (mCantiViewModel.defaultListaId) {
                1 -> {
                    result
                        .append("-- ")
                        .append(getString(R.string.title_activity_canti_parola).uppercase(l))
                        .append(" --\n")

                    result.append(resources.getString(R.string.canto_iniziale).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.prima_lettura).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.seconda_lettura).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.terza_lettura).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

                    if (pref.getBoolean(Utility.SHOW_PACE, false)) {
                        result.append(resources.getString(R.string.canto_pace).uppercase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }
                    result.append(resources.getString(R.string.canto_fine).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos))
                }

                2 -> {
                    result
                        .append("-- ")
                        .append(getString(R.string.title_activity_canti_eucarestia).uppercase(l))
                        .append(" --")
                    result.append("\n")
                    result.append(resources.getString(R.string.canto_iniziale).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    if (pref.getBoolean(Utility.SHOW_SECONDA, false)) {
                        result.append(resources.getString(R.string.seconda_lettura).uppercase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }

                    if (pref.getBoolean(Utility.SHOW_EUCARESTIA_PACE, true)) {
                        result.append(resources.getString(R.string.canto_pace).uppercase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }

                    if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false)) {
                        result.append(resources.getString(R.string.canto_offertorio).uppercase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }
                    if (pref.getBoolean(Utility.SHOW_SANTO, false)) {
                        result.append(resources.getString(R.string.santo).uppercase(l))
                        result.append("\n")

                        result.append(getTitoloToSendFromPosition(progressivePos++))
                        result.append("\n")
                    }
                    result.append(resources.getString(R.string.canto_pane).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.canto_vino).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos++))
                    result.append("\n")
                    result.append(resources.getString(R.string.canto_fine).uppercase(l))
                    result.append("\n")

                    result.append(getTitoloToSendFromPosition(progressivePos))
                }
            }
            return result.toString()
        }

    private val startListInsertForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.i(TAG, "startListInsertForResult result.resultCode ${result.resultCode}")
            if (result.resultCode == CustomListsFragment.RESULT_OK || result.resultCode == CustomListsFragment.RESULT_KO)
                showSnackBar(if (result.resultCode == CustomListsFragment.RESULT_OK) R.string.list_added else R.string.present_yet)
        }

    override fun pulisci() {
        Log.d(TAG, "pulisci: ${mCantiViewModel.defaultListaId}")
        lifecycleScope.launch(Dispatchers.IO) {
            RisuscitoDatabase.getInstance(requireContext()).customListDao()
                .deleteListById(mCantiViewModel.defaultListaId)
        }
    }

    override fun condividi() {
        val bottomSheetDialog =
            BottomSheetFragment.newInstance(R.string.share_by, defaultIntent)
        bottomSheetDialog.show(parentFragmentManager, null)
    }

    override fun inviaFile() {}

    override fun onActionPerformed() {
        val positionToInsert = CustomList()
        positionToInsert.id = mCantiViewModel.defaultListaId
        positionToInsert.position = posizioneDaCanc
        positionToInsert.idCanto = idDaCanc
        positionToInsert.timestamp =
            Date(java.lang.Long.parseLong(timestampDaCanc.orEmpty()))
        positionToInsert.notaPosizione = notaDaCanc
        val mDao2 =
            RisuscitoDatabase.getInstance(requireContext()).customListDao()
        lifecycleScope.launch(Dispatchers.IO) {
            mDao2.insertPosition(
                positionToInsert
            )
        }
    }

    override fun onDismissed() {}

    private fun showSnackBar(messageRes: Int) {
        showSnackBar(getString(messageRes))
    }

    private fun showSnackBar(messageRes: Int, labelRes: Int) {
        showSnackBar(getString(messageRes), getString(labelRes).uppercase(systemLocale))
    }

    override fun showSnackBar(message: String, label: String?) {
        mMainActivity?.showSnackBar(
            message = message,
            callback = this,
            label = label
        )
    }

    companion object {
        const val INDICE_LISTA = "indiceLista"
        private val TAG = ListaPredefinitaFragment::class.java.canonicalName
    }
}
