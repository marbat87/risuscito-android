package it.cammino.risuscito.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.transition.platform.MaterialSharedAxis
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.ui.activity.CreaListaActivity
import it.cammino.risuscito.ui.activity.CreaListaActivity.Companion.EDIT_EXISTING_LIST
import it.cammino.risuscito.ui.activity.CreaListaActivity.Companion.ID_DA_MODIF
import it.cammino.risuscito.ui.activity.CreaListaActivity.Companion.LIST_TITLE
import it.cammino.risuscito.ui.composable.dialogs.InputDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleDialogTag
import it.cammino.risuscito.ui.composable.main.Destination
import it.cammino.risuscito.ui.composable.main.FabActionItem
import it.cammino.risuscito.ui.composable.main.listaPersonalizzata
import it.cammino.risuscito.ui.composable.main.listaPredefinita
import it.cammino.risuscito.ui.interfaces.FabFragment
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.launchForResultWithAnimation
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.CustomListsViewModel
import it.cammino.risuscito.viewmodels.InputDialogManagerViewModel
import it.cammino.risuscito.viewmodels.SharedTabViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomListsFragment : RisuscitoFragment(), SnackBarFragment,
    FabFragment {

    private val mCustomListsViewModel: CustomListsViewModel by viewModels()
    private val inputdialogViewModel: InputDialogManagerViewModel by viewModels()

    private val sharedTabViewModel: SharedTabViewModel by activityViewModels()

    val titoliListe = MutableLiveData(emptyArray<String?>())
    private var idListe: IntArray = IntArray(0)
    private var movePage: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                val localItems by titoliListe.observeAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val localPagerState = rememberPagerState(
                        pageCount = {
                            2 + (localItems.orEmpty().size)
                        })
                    HorizontalPager(
                        state = localPagerState
                    ) { page ->
                        // Our page content
                        when (page) {
                            0, 1 ->
                                AndroidFragment<ListaPredefinitaFragment>(
                                    arguments = bundleOf(ListaPredefinitaFragment.INDICE_LISTA to page + 1)
                                )

                            else ->
                                AndroidFragment<ListaPersonalizzataFragment>(
                                    arguments = bundleOf(ListaPersonalizzataFragment.INDICE_LISTA to idListe[page - 2])
                                )

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
                                if (sharedTabViewModel.tabsSelectedIndex.intValue != page) {
                                    sharedTabViewModel.tabsSelectedIndex.intValue = page
                                    initFabOptions(page >= 2)
                                }
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
                                initFabOptions(selectedIndex >= 2)
                            }
                    }

                    val showInputDialog by inputdialogViewModel.showAlertDialog.observeAsState()

                    val showAlertDialog by mCustomListsViewModel.showAlertDialog.observeAsState()

                    val rememberConfirmNewList = remember<(String) -> Unit> {
                        { text ->
                            inputdialogViewModel.showAlertDialog.value = false
                            Log.d(TAG, "idListe.size ${idListe.size}")
                            mCustomListsViewModel.indDaModif = 2 + idListe.size
                            Log.d(
                                TAG,
                                "mCustomListsViewModel.indDaModif ${mCustomListsViewModel.indDaModif}"
                            )
                            mMainActivity?.let { act ->
                                act.launchForResultWithAnimation(
                                    startListEditForResult,
                                    Intent(
                                        act, CreaListaActivity::class.java
                                    ).putExtras(
                                        bundleOf(
                                            LIST_TITLE to text,
                                            EDIT_EXISTING_LIST to false
                                        )
                                    ),
                                    MaterialSharedAxis.Y
                                )
                            }
                        }
                    }


                    if (showAlertDialog == true) {
                        SimpleAlertDialog(
                            onDismissRequest = {
                                mCustomListsViewModel.showAlertDialog.postValue(false)
                            },
                            onConfirmation = {
                                when (it) {
                                    SimpleDialogTag.RESET_LIST -> {
                                        mCustomListsViewModel.showAlertDialog.postValue(false)
                                        mMainActivity?.getFabActionsFragment()?.pulisci()
                                    }

                                    SimpleDialogTag.DELETE_LIST -> {
                                        mCustomListsViewModel.showAlertDialog.postValue(false)
                                        sharedTabViewModel.tabsSelectedIndex.intValue--
                                        lifecycleScope.launch { deleteList() }

                                    }

                                    else -> {}
                                }
                            },
                            dialogTitle = mCustomListsViewModel.dialogTitle.value.orEmpty(),
                            dialogText = mCustomListsViewModel.content.value.orEmpty(),
                            iconRes = mCustomListsViewModel.iconRes.value ?: 0,
                            confirmButtonText = mCustomListsViewModel.positiveButton.value.orEmpty(),
                            dismissButtonText = mCustomListsViewModel.negativeButton.value.orEmpty(),
                            dialogTag = mCustomListsViewModel.dialogTag
                        )
                    }

                    if (showInputDialog == true) {
                        InputDialog(
                            dialogTitleRes = R.string.lista_add_desc,
                            onDismissRequest = {
                                inputdialogViewModel.showAlertDialog.value = false
                            },
                            onConfirmation = { _, text -> rememberConfirmNewList(text) },
                            confirmationTextRes = R.string.create_confirm,
                            multiline = true,
                            required = true
                        )
                    }
                }

                BackHandler(mMainActivity?.getFabExpanded() == true) {
                    Log.d(TAG, "handleOnBackPressed")
                    mMainActivity?.setFabExpanded(false)
                }

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        movePage = false

        val mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        Log.d(
            TAG, "onCreate - INTRO_CUSTOMLISTS: " + mSharedPrefs.getBoolean(
                Utility.INTRO_CUSTOMLISTS, false
            )
        )

        subscribeUiChanges()

    }

    private val startListEditForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(
                    TAG,
                    "startListEditForResult mCustomListsViewModel.indDaModif: ${mCustomListsViewModel.indDaModif}"
                )
                mCustomListsViewModel.indexToShow = mCustomListsViewModel.indDaModif
                movePage = true
            }
        }

    private fun subscribeUiChanges() {
        mCustomListsViewModel.customListResult?.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "list size ${list.size}")
            val newArray = arrayOfNulls<String>(list.size)
            idListe = IntArray(list.size)

            for (i in list.indices) {
                newArray[i] = list[i].titolo
                idListe[i] = list[i].id
            }
            val destinationList = ArrayList<Destination>()
            destinationList.add(Destination.CantiParola)
            destinationList.add(Destination.CantiEucarestia)
            list.forEach {
                destinationList.add(Destination("LISTA_PERS_${it.id}", 0, it.titolo ?: ""))
            }
            titoliListe.value = newArray
            mMainActivity?.setupMaterialTab(
                destinationList,
                sharedTabViewModel.tabsSelectedIndex.intValue
            )
            mMainActivity?.setTabVisible(true)
            Log.d(
                TAG,
                "sharedTabViewModel.tabsSelectedIndex.intValue: ${sharedTabViewModel.tabsSelectedIndex.intValue}"
            )
            initFabOptions(sharedTabViewModel.tabsSelectedIndex.intValue >= 2)

            Handler(Looper.getMainLooper()).postDelayed(1000) {
                Log.d(TAG, "movePage: $movePage")
                Log.d(
                    TAG, "mCustomListsViewModel.indexToShow: ${mCustomListsViewModel.indexToShow}"
                )
                if (movePage) {
                    sharedTabViewModel.tabsSelectedIndex.intValue =
                        mCustomListsViewModel.indexToShow
                    movePage = false
                }
            }

//            mMainActivity?.createOptionsMenu(
//                cleanListOptionMenu,
//                null
//            )
//            Handler(Looper.getMainLooper()).postDelayed(1) {
//                mMainActivity?.createOptionsMenu(
//                    helpOptionMenu,
//                    null
//                )
//            }

        }

    }

    fun initFabOptions(customList: Boolean) {
        mMainActivity?.initFab(
            enable = true,
            fragment = this,
            iconRes = R.drawable.add_24px,
            fabActions = if (customList) listaPersonalizzata else listaPredefinita
        )
    }

    private suspend fun deleteListDialog() {
        mCustomListsViewModel.listaDaCanc = sharedTabViewModel.tabsSelectedIndex.intValue - 2
        mCustomListsViewModel.idDaCanc = idListe[mCustomListsViewModel.listaDaCanc]
        val mDao = RisuscitoDatabase.getInstance(requireContext()).listePersDao()
        val lista = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            mDao.getListById(mCustomListsViewModel.idDaCanc)
        }
        mCustomListsViewModel.titoloDaCanc = lista?.titolo
        mCustomListsViewModel.celebrazioneDaCanc = lista?.lista

        mCustomListsViewModel.dialogTag = SimpleDialogTag.DELETE_LIST
        mCustomListsViewModel.dialogTitle.postValue(getString(R.string.action_remove_list))
        mCustomListsViewModel.content.postValue(getString(R.string.delete_list_dialog))
        mCustomListsViewModel.iconRes.postValue(R.drawable.delete_24px)
        mCustomListsViewModel.positiveButton.postValue(getString(R.string.delete_confirm))
        mCustomListsViewModel.negativeButton.postValue(getString(R.string.cancel))
        mCustomListsViewModel.showAlertDialog.postValue(true)
    }

    private suspend fun deleteList() {
        val mDao = RisuscitoDatabase.getInstance(requireContext()).listePersDao()
        val listToDelete = ListaPers()
        listToDelete.id = mCustomListsViewModel.idDaCanc
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.deleteList(listToDelete) }
        showSnackBar(
            "${getString(R.string.list_removed)}${mCustomListsViewModel.titoloDaCanc}'!",
            getString(R.string.cancel).uppercase(systemLocale)
        )
    }

    override fun showSnackBar(message: String, label: String?) {
        mMainActivity?.showSnackBar(
            message = message,
            callback = this,
            label = label
        )
    }

    override fun onActionPerformed() {
        mCustomListsViewModel.indexToShow = mCustomListsViewModel.listaDaCanc + 2
        movePage = true
        val mListePersDao =
            RisuscitoDatabase.getInstance(requireContext()).listePersDao()
        val listaToRestore = ListaPers()
        listaToRestore.id = mCustomListsViewModel.idDaCanc
        listaToRestore.titolo = mCustomListsViewModel.titoloDaCanc
        listaToRestore.lista = mCustomListsViewModel.celebrazioneDaCanc
        lifecycleScope.launch(Dispatchers.IO) {
            mListePersDao.insertLista(
                listaToRestore
            )
        }
    }

    override fun onDismissed() {}

//    override fun onItemClick(route: String) {
//        when (route) {
//            OptionMenuItem.Help.route -> {
//                playIntro()
//            }
//        }
//    }

    override fun onFabClick(item: String) {
        when (item) {
            FabActionItem.PULISCI.id -> {
                mCustomListsViewModel.dialogTag = SimpleDialogTag.RESET_LIST
                mCustomListsViewModel.dialogTitle.postValue(getString(R.string.dialog_reset_list_title))
                mCustomListsViewModel.content.postValue(getString(R.string.reset_list_question))
                mCustomListsViewModel.iconRes.postValue(R.drawable.cleaning_services_24px)
                mCustomListsViewModel.positiveButton.postValue(getString(R.string.reset_confirm))
                mCustomListsViewModel.negativeButton.postValue(getString(R.string.cancel))
                mCustomListsViewModel.showAlertDialog.postValue(true)
            }

            FabActionItem.ADDLISTA.id -> {
                inputdialogViewModel.showAlertDialog.value = true
            }

            FabActionItem.CONDIVIDI.id -> {
                mMainActivity?.getFabActionsFragment()?.condividi()
            }

            FabActionItem.CONDIVIDIFILE.id -> {
                mMainActivity?.getFabActionsFragment()?.inviaFile()
            }

            FabActionItem.DELETE.id -> {
                lifecycleScope.launch { deleteListDialog() }
            }

            FabActionItem.EDIT.id -> {
                mCustomListsViewModel.indDaModif =
                    sharedTabViewModel.tabsSelectedIndex.intValue - 2
                mMainActivity?.let { act ->
                    act.launchForResultWithAnimation(
                        startListEditForResult, Intent(
                            act, CreaListaActivity::class.java
                        ).putExtras(
                            bundleOf(
                                ID_DA_MODIF to idListe[sharedTabViewModel.tabsSelectedIndex.intValue - 2],
                                EDIT_EXISTING_LIST to true
                            )
                        ), MaterialSharedAxis.Y
                    )
                }
            }

            else -> {}
        }
    }

    companion object {
        const val RESULT_OK = 0
        const val RESULT_KO = -1
        const val RESULT_CANCELED = -2
        private val TAG = CustomListsFragment::class.java.canonicalName
    }
}
