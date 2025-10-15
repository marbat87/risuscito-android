package it.cammino.risuscito.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.os.postDelayed
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Cronologia
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.risuscitoListItem
import it.cammino.risuscito.ui.composable.HistoryListItem
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.main.ActionModeItem
import it.cammino.risuscito.ui.composable.main.OptionMenuItem
import it.cammino.risuscito.ui.composable.main.cleanListOptionMenu
import it.cammino.risuscito.ui.composable.main.deleteMenu
import it.cammino.risuscito.ui.composable.main.helpOptionMenu
import it.cammino.risuscito.ui.composable.risuscito_medium_font
import it.cammino.risuscito.ui.dialog.SimpleDialogFragment
import it.cammino.risuscito.ui.interfaces.ActionModeFragment
import it.cammino.risuscito.ui.interfaces.OptionMenuFragment
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.isGridLayout
import it.cammino.risuscito.utils.extension.openCanto
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.CronologiaViewModel
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

class HistoryFragment : RisuscitoFragment(), ActionModeFragment, SnackBarFragment,
    OptionMenuFragment {

    private val mCronologiaViewModel: CronologiaViewModel by viewModels()

    private val sharedScrollViewModel: SharedScrollViewModel by activityViewModels()
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })

    private var actionModeOk: Boolean = false

    private val selectedItems = MutableLiveData(ArrayList<RisuscitoListItem>())

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()
                val localItems by mCronologiaViewModel.historySortedResult.observeAsState()
                val localSelectedItems by selectedItems.observeAsState()

                val viewMode by remember { mCronologiaViewModel.viewMode }

                val scrollBehaviorFromSharedVM by sharedScrollViewModel.scrollBehavior.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        viewMode,
                        transitionSpec = {
                            fadeIn(
                                animationSpec = tween(1000)
                            ) togetherWith fadeOut(animationSpec = tween(1000))
                        },
                        label = "Animated Content"
                    )
                    { targetState ->
                        when (targetState) {
                            CronologiaViewModel.ViewMode.VIEW -> {
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

                                if (context?.isGridLayout == true) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        modifier = listModifier
                                    ) {
                                        items(
                                            localItems.orEmpty(),
                                            key = { it.id }) { simpleItem ->
                                            val isItemSelected =
                                                localSelectedItems.orEmpty()
                                                    .any { it.id == simpleItem.id }
                                            val source = stringResource(simpleItem.sourceRes)
                                            HistoryListItem(
                                                requireContext(),
                                                simpleItem,
                                                onItemClick = { item ->
                                                    if (mMainActivity?.isActionMode?.value == true) {
                                                        if (isItemSelected) {
                                                            deselectItem(item.id)
                                                        } else {
                                                            selectItem(item.id, item.timestamp)
                                                        }
                                                        if (localSelectedItems.orEmpty()
                                                                .isEmpty()
                                                        ) {
                                                            mMainActivity?.destroyActionMode()
                                                        } else updateActionModeTitle()
                                                    } else {
                                                        mMainActivity?.openCanto(
                                                            TAG,
                                                            item.id,
                                                            source,
                                                            false
                                                        )
                                                    }
                                                },
                                                onItemLongClick = { item ->
                                                    if (mMainActivity?.isActionMode?.value != true && !isItemSelected) {
                                                        selectItem(item.id, item.timestamp)
                                                        startCab()
                                                    }
                                                },
                                                selected = isItemSelected,
                                                modifier = Modifier.animateItem()
                                            )
                                        }
                                    }
                                }
                                LazyColumn(
                                    state = state,
                                    modifier = listModifier
                                ) {
                                    items(
                                        localItems.orEmpty(),
                                        key = { it.id }) { simpleItem ->
                                        val isItemSelected =
                                            localSelectedItems.orEmpty()
                                                .any { it.id == simpleItem.id }
                                        val source = stringResource(simpleItem.sourceRes)
                                        HistoryListItem(
                                            requireContext(),
                                            simpleItem,
                                            onItemClick = { item ->
                                                if (mMainActivity?.isActionMode?.value == true) {
                                                    if (isItemSelected) {
                                                        deselectItem(item.id)
                                                    } else {
                                                        selectItem(item.id, item.timestamp)
                                                    }
                                                    if (localSelectedItems.orEmpty()
                                                            .isEmpty()
                                                    ) {
                                                        mMainActivity?.destroyActionMode()
                                                    } else updateActionModeTitle()
                                                } else {
                                                    mMainActivity?.openCanto(
                                                        TAG,
                                                        item.id,
                                                        source,
                                                        false
                                                    )
                                                }
                                            },
                                            onItemLongClick = { item ->
                                                if (mMainActivity?.isActionMode?.value != true && !isItemSelected) {
                                                    selectItem(item.id, item.timestamp)
                                                    startCab()
                                                }
                                            },
                                            selected = isItemSelected,
                                            modifier = Modifier.animateItem(tween())
                                        )
                                    }
                                }
                            }

                            CronologiaViewModel.ViewMode.EMPTY -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(), // Occupa solo l'altezza necessaria
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_history_clock),
                                        contentDescription = stringResource(id = R.string.history_empty),
                                        modifier = Modifier
                                            .size(120.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp)) // Spazio tra immagine e testo
                                    Text(
                                        text = stringResource(R.string.history_empty),
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
                if (mCronologiaViewModel.showAlertDialog.observeAsState().value == true) {
                    SimpleAlertDialog(
                        onDismissRequest = {
                            mCronologiaViewModel.showAlertDialog.postValue(false)
                        },
                        onConfirmation = {
                            mCronologiaViewModel.showAlertDialog.postValue(false)
                            simpleDialogViewModel.handled = true
                            val mDao =
                                RisuscitoDatabase.getInstance(requireContext())
                                    .cronologiaDao()
                            coroutineScope.launch(Dispatchers.IO) { mDao.emptyCronologia() }
                        },
                        dialogTitle = stringResource(R.string.dialog_reset_favorites_title),
                        dialogText = stringResource(R.string.dialog_reset_favorites_desc),
                        icon = Icons.Outlined.ClearAll,
                        confirmButtonText = stringResource(R.string.clear_confirm),
                        dismissButtonText = stringResource(R.string.cancel)
                    )
                }

                mCronologiaViewModel.cronologiaCanti?.observe(viewLifecycleOwner) { canti ->
                    mCronologiaViewModel.historySortedResult.postValue(canti)
                    mMainActivity?.createOptionsMenu(
                        if (canti.isNotEmpty()) cleanListOptionMenu else helpOptionMenu,
                        this@HistoryFragment
                    )
                    mCronologiaViewModel.viewMode.value =
                        if (canti.isEmpty()) CronologiaViewModel.ViewMode.EMPTY else CronologiaViewModel.ViewMode.VIEW
                    if (canti.isEmpty())
                        mMainActivity?.expandToolbar()
                }

            }
        }
    }

    private fun selectItem(idCanto: Int, timestampCanto: String) {
        val currentSelected = selectedItems.value.orEmpty()
        val newSelected = ArrayList(currentSelected) // Crea una nuova lista
        newSelected.add(
            risuscitoListItem(
                timestamp = timestampCanto
            ) {
                id = idCanto
            })
        selectedItems.value = newSelected // Assegna la nuova lista
    }

    private fun deselectItem(id: Int) {
        val currentSelected = selectedItems.value.orEmpty()
        val newSelected =
            ArrayList(currentSelected.filter { it.id != id }) // Crea una nuova lista
        selectedItems.value = newSelected // Assegna la nuova lista
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity?.initFab(enable = false)
        mMainActivity?.setTabVisible(false)

        if (!PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean(Utility.HISTORY_OPEN, false)
        ) {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit { putBoolean(Utility.HISTORY_OPEN, true) }
            Handler(Looper.getMainLooper()).postDelayed(250) {
                Toast.makeText(
                    activity,
                    getString(R.string.new_hint_remove),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

    }

    private fun startCab() {
        actionModeOk = false
        mMainActivity?.createActionMode(deleteMenu, this) {
            when (it) {
                ActionModeItem.Delete.route -> {
                    removeHistories()
                    actionModeOk = true
                    mMainActivity?.destroyActionMode()
                    true
                }

                ActionModeItem.Close.route -> {
                    actionModeOk = false
                    mMainActivity?.destroyActionMode()
                    true
                }
            }
        }
        updateActionModeTitle()
    }

    override fun destroyActionMode() {
        if (!actionModeOk)
            selectedItems.value = ArrayList()
    }

    private fun updateActionModeTitle() {
        val itemSelectedCount = selectedItems.value.orEmpty().count()
        mMainActivity?.updateActionModeTitle(
            resources.getQuantityString(
                R.plurals.item_selected, itemSelectedCount, itemSelectedCount
            )
        )
    }

    private fun removeHistories() {
        lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(requireContext()).cronologiaDao()
            selectedItems.value?.let { removedItems ->
                withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                    for (removedItem in removedItems)
                        mDao.deleteCronologiaById(
                            removedItem.id
                        )
                }
                showSnackBar(
                    message = resources.getQuantityString(
                        R.plurals.histories_removed,
                        selectedItems.value.orEmpty().count(),
                        selectedItems.value.orEmpty().count()
                    ),
                    label = getString(R.string.cancel)
                        .uppercase(systemLocale)
                )
            }
        }
    }

    override fun onActionPerformed() {
        val mDao = RisuscitoDatabase.getInstance(requireContext()).cronologiaDao()
        for (removedItem in selectedItems.value.orEmpty()) {
            val cronTemp = Cronologia()
            cronTemp.idCanto = removedItem.id
            cronTemp.ultimaVisita = Date(
                java.lang.Long.parseLong(
                    removedItem.timestamp
                )
            )
            lifecycleScope.launch(Dispatchers.IO) {
                mDao.insertCronologia(
                    cronTemp
                )
            }
        }
        selectedItems.value = ArrayList()
    }

    override fun onDismissed() {
        selectedItems.value = ArrayList()
    }

    override fun showSnackBar(message: String, label: String?) {
        mMainActivity?.showSnackBar(
            message = message,
            callback = this,
            label = label
        )
    }

    override fun onItemClick(route: String) {
        when (route) {
            OptionMenuItem.ClearAll.route -> {
                mCronologiaViewModel.showAlertDialog.postValue(true)
            }

            OptionMenuItem.Help.route -> {
                Toast.makeText(
                    activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private val TAG = HistoryFragment::class.java.canonicalName
    }
}
