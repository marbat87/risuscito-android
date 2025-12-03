package it.cammino.risuscito.ui.fragment

import android.content.Intent
import android.net.Uri
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
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.ListaPersonalizzataRisuscitoListItem
import it.cammino.risuscito.ui.activity.InsertActivity
import it.cammino.risuscito.ui.activity.MainActivity
import it.cammino.risuscito.ui.composable.PosizioneListItem
import it.cammino.risuscito.ui.composable.dialogs.InputDialog
import it.cammino.risuscito.ui.composable.hasTwoPanes
import it.cammino.risuscito.ui.composable.main.ActionModeItem
import it.cammino.risuscito.ui.composable.main.customListsMenu
import it.cammino.risuscito.ui.interfaces.ActionModeFragment
import it.cammino.risuscito.ui.interfaces.FabActionsFragment
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.launchForResultWithAnimation
import it.cammino.risuscito.utils.extension.listToXML
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.InputDialogManagerViewModel
import it.cammino.risuscito.viewmodels.ListaPersonalizzataViewModel
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import it.cammino.risuscito.viewmodels.ViewModelWithArgumentsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListaPersonalizzataFragment : Fragment(), ActionModeFragment, SnackBarFragment,
    FabActionsFragment {

    private val mCantiViewModel: ListaPersonalizzataViewModel by viewModels {
        ViewModelWithArgumentsFactory(requireActivity().application, Bundle().apply {
            putInt(Utility.TIPO_LISTA, arguments?.getInt(INDICE_LISTA) ?: 0)
        })
    }
    private val inputdialogViewModel: InputDialogManagerViewModel by viewModels()

    private lateinit var cantoDaCanc: String
    private lateinit var notaDaCanc: String
    private var mSwhitchMode: Boolean = false
    private var longclickedPos: Int = 0
    private var longClickedChild: Int = 0
    private var actionModeOk: Boolean = false
    private var backCallbackEnabled = mutableStateOf(false)
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

                val hasTwoPanes = hasTwoPanes()

                val rememberAddClick = remember<(Int) -> Unit> {
                    { idPosizione ->
                        if (mSwhitchMode) {
                            scambioConVuoto(idPosizione)
                        } else {
                            if (mMainActivity?.isActionMode?.value != true) {
                                mMainActivity?.let {
                                    it.launchForResultWithAnimation(
                                        startListInsertForResult,
                                        Intent(it, InsertActivity::class.java).putExtras(
                                            bundleOf(
                                                InsertActivity.FROM_ADD to 0,
                                                InsertActivity.ID_LISTA to mCantiViewModel.listaPersonalizzataId,
                                                InsertActivity.POSITION to idPosizione
                                            )
                                        )
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
                                mCantiViewModel.posizioneDaCanc = item.idPosizione
                                snackBarRimuoviCanto(item)
                            } else {
                                mMainActivity?.openCanto(
                                    TAG,
                                    item.id,
                                    getString(item.sourceRes),
                                    !hasTwoPanes
                                )
                            }
                        else {
                            scambioCanto(item.idPosizione)
                        }
                    }
                }

                val rememberItemLongClick =
                    remember<(ListaPersonalizzataRisuscitoListItem) -> Unit> {
                        { item ->
                            mCantiViewModel.posizioneDaCanc = item.idPosizione
                            snackBarRimuoviCanto(item)
                        }
                    }

                val rememberNoteClick = remember<(ListaPersonalizzataRisuscitoListItem) -> Unit> {
                    { item ->
                        inputdialogViewModel.outputItemId = item.idPosizione
                        inputdialogViewModel.dialogPrefill = item.nota
                        inputdialogViewModel.showAlertDialog.value = true
                    }
                }

                val rememberConfirmEditNote = remember<(String) -> Unit> {
                    { text ->
                        inputdialogViewModel.showAlertDialog.value = false
                        Log.d(
                            TAG,
                            "inputdialogViewModel.outputText $text"
                        )
                        Log.d(
                            TAG,
                            " mCantiViewModel.posizioneDaCanc ${mCantiViewModel.posizioneDaCanc}"
                        )
                        mCantiViewModel.listaPersonalizzata?.addNota(
                            text,
                            inputdialogViewModel.outputItemId
                        )
                        runUpdate()
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
                        Spacer(Modifier.height(86.dp))
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

                mCantiViewModel.listaPersonalizzataResult?.observe(viewLifecycleOwner) { listaPersonalizzataResult ->
                    mCantiViewModel.posizioniList.value = listaPersonalizzataResult
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
//        mMainActivity?.setFabActionsFragment(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMainActivity?.destroyActionMode()
    }

    private fun getSendIntent(exportUri: Uri): Intent {
        return Intent(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_STREAM, exportUri)
            .setType("text/xml")
    }

    private fun snackBarRimuoviCanto(item: ListaPersonalizzataRisuscitoListItem) {
        mMainActivity?.destroyActionMode()
        longclickedPos = item.tagPosizione
        longClickedChild = item.itemTag
        startCab()
    }

    private fun scambioCanto(posizioneNew: Int) {
        if (posizioneNew != mCantiViewModel.posizioneDaCanc) {

            val cantoTmp = mCantiViewModel.listaPersonalizzata?.getCantoPosizione(posizioneNew)
            val notaTmp = mCantiViewModel.listaPersonalizzata?.getNotaPosizione(posizioneNew)
            mCantiViewModel.listaPersonalizzata?.addCanto(
                mCantiViewModel.listaPersonalizzata?.getCantoPosizione(mCantiViewModel.posizioneDaCanc),
                posizioneNew
            )
            mCantiViewModel.listaPersonalizzata?.addNota(
                mCantiViewModel.listaPersonalizzata?.getNotaPosizione(mCantiViewModel.posizioneDaCanc),
                posizioneNew
            )
            mCantiViewModel.listaPersonalizzata?.addCanto(cantoTmp, mCantiViewModel.posizioneDaCanc)
            mCantiViewModel.listaPersonalizzata?.addNota(notaTmp, mCantiViewModel.posizioneDaCanc)

            runUpdate()

            actionModeOk = true
            mMainActivity?.destroyActionMode()
            showSnackBar(R.string.switch_done)

        } else
            showSnackBar(R.string.switch_impossible)
    }

    private fun scambioConVuoto(posizioneNew: Int) {
        //        Log.i(getClass().toString(), "positioneNew: " + posizioneNew);
        //        Log.i(getClass().toString(), "posizioneDaCanc: " + posizioneDaCanc);
        mCantiViewModel.listaPersonalizzata?.addCanto(
            mCantiViewModel.listaPersonalizzata?.getCantoPosizione(mCantiViewModel.posizioneDaCanc),
            posizioneNew
        )
        mCantiViewModel.listaPersonalizzata?.addNota(
            mCantiViewModel.listaPersonalizzata?.getNotaPosizione(mCantiViewModel.posizioneDaCanc),
            posizioneNew
        )
        mCantiViewModel.listaPersonalizzata?.removeCanto(mCantiViewModel.posizioneDaCanc)
        mCantiViewModel.listaPersonalizzata?.removeNota(mCantiViewModel.posizioneDaCanc)

        runUpdate()

        actionModeOk = true
        mMainActivity?.destroyActionMode()
        showSnackBar(R.string.switch_done)
    }

    private fun startCab() {
        mSwhitchMode = false
        selectItem(true)
        actionModeOk = false
        mMainActivity?.createActionMode(customListsMenu, this) { itemRoute ->
            when (itemRoute) {
                ActionModeItem.DELETE -> {
                    cantoDaCanc =
                        mCantiViewModel.listaPersonalizzata?.getCantoPosizione(mCantiViewModel.posizioneDaCanc)
                            .orEmpty()
                    notaDaCanc =
                        mCantiViewModel.listaPersonalizzata?.getNotaPosizione(mCantiViewModel.posizioneDaCanc)
                            .orEmpty()
                    mCantiViewModel.listaPersonalizzata?.removeCanto(mCantiViewModel.posizioneDaCanc)
                    mCantiViewModel.listaPersonalizzata?.removeNota(mCantiViewModel.posizioneDaCanc)
                    runUpdate()
                    actionModeOk = true
                    mMainActivity?.destroyActionMode()
                    showSnackBar(
                        R.string.song_removed,
                        R.string.cancel
                    )
                }

                ActionModeItem.SWAP -> {
                    cantoDaCanc =
                        mCantiViewModel.listaPersonalizzata?.getCantoPosizione(mCantiViewModel.posizioneDaCanc)
                            .orEmpty()
                    notaDaCanc =
                        mCantiViewModel.listaPersonalizzata?.getNotaPosizione(mCantiViewModel.posizioneDaCanc)
                            .orEmpty()
                    mSwhitchMode = true
                    updateActionModeTitle(true)
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.switch_tooltip),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                ActionModeItem.CLOSE -> {
                    actionModeOk = false
                    mMainActivity?.destroyActionMode()
                }

                else -> {}
            }
        }
        updateActionModeTitle(false)
        backCallbackEnabled.value = true
    }

    private fun selectItem(selected: Boolean) {
        Log.d(
            TAG,
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
        Log.d(TAG, "MaterialCab onDestroy: $actionModeOk")
        Log.d(
            TAG,
            "MaterialCab onDestroy - longclickedPos: $longclickedPos / listaPersonalizzataId: ${mCantiViewModel.listaPersonalizzataId}"
        )
        mSwhitchMode = false
        if (!actionModeOk) {
            try {
                selectItem(false)
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }
        }
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

    private fun runUpdate() {
        mMainActivity?.let {
            val listaNew = ListaPers()
            listaNew.lista = mCantiViewModel.listaPersonalizzata
            listaNew.id = mCantiViewModel.listaPersonalizzataId
            listaNew.titolo = mCantiViewModel.listaPersonalizzataTitle
            val mDao = RisuscitoDatabase.getInstance(it).listePersDao()
            lifecycleScope.launch(Dispatchers.IO) { mDao.updateLista(listaNew) }
        }
    }

    private val shareIntent: Intent
        get() = Intent(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_TEXT, titlesList)
            .setType("text/plain")

    private val titlesList: String
        get() {
            val l = systemLocale
            val result = StringBuilder()
            result.append("-- ").append(mCantiViewModel.listaPersonalizzata?.name?.uppercase(l))
                .append(" --\n")
            for (i in 0 until (mCantiViewModel.listaPersonalizzata?.numPosizioni ?: 0)) {
                result.append(
                    mCantiViewModel.listaPersonalizzata?.getNomePosizione(i)?.uppercase(l)
                ).append("\n")
                if (!mCantiViewModel.listaPersonalizzata?.getCantoPosizione(i).isNullOrEmpty()) {
                    for (tempItem in mCantiViewModel.posizioniList.value.orEmpty()[i].posizioni) {
                        result
                            .append(getText(tempItem.titleRes))
                            .append(" - ")
                            .append(getString(R.string.page_contracted))
                            .append(getText(tempItem.pageRes))
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
                if (i < (mCantiViewModel.listaPersonalizzata?.numPosizioni
                        ?: 0) - 1
                ) result.append("\n")
            }

            return result.toString()
        }

    private val startListInsertForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == CustomListsFragment.RESULT_OK || result.resultCode == CustomListsFragment.RESULT_KO)
                showSnackBar(if (result.resultCode == CustomListsFragment.RESULT_OK) R.string.list_added else R.string.present_yet)
        }

    override fun onActionPerformed() {
        mCantiViewModel.listaPersonalizzata?.addCanto(
            cantoDaCanc,
            mCantiViewModel.posizioneDaCanc
        )
        mCantiViewModel.listaPersonalizzata?.addNota(
            notaDaCanc,
            mCantiViewModel.posizioneDaCanc
        )
        runUpdate()
    }

    override fun onDismissed() {}

    private fun showSnackBar(messageRes: Int) {
        showSnackBar(getString(messageRes))
    }

    private fun showSnackBar(messageRes: Int, labelRes: Int) {
        showSnackBar(
            getString(messageRes),
            getString(labelRes).uppercase(requireContext().systemLocale)
        )
    }

    override fun showSnackBar(message: String, label: String?) {
        mMainActivity?.showSnackBar(
            message = message,
            callback = this,
            label = label
        )
    }

    override fun pulisci() {
        Log.d(TAG, "pulisci: ${mCantiViewModel.listaPersonalizzataId}")
        for (i in 0 until (mCantiViewModel.listaPersonalizzata?.numPosizioni ?: 0))
            mCantiViewModel.listaPersonalizzata?.removeCanto(i)
        runUpdate()
    }

    override fun condividi() {
        mMainActivity?.showBottomSheet(R.string.share_by, shareIntent)
    }

    override fun inviaFile() {
        val exportUri = activity?.listToXML(mCantiViewModel.listaPersonalizzata)
        Log.d(TAG, "onClick: exportUri = $exportUri")
        exportUri?.let {
            mMainActivity?.showBottomSheet(R.string.share_by, getSendIntent(it))
        } ?: run {
            showSnackBar(R.string.xml_error)
        }
    }

    companion object {
        internal val TAG = ListaPersonalizzataFragment::class.java.canonicalName
        const val INDICE_LISTA = "indiceLista"
    }
}
