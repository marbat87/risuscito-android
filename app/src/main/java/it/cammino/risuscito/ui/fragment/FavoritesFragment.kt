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
import it.cammino.risuscito.ui.composable.SimpleListItem
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.main.ActionModeItem
import it.cammino.risuscito.ui.composable.main.OptionMenuItem
import it.cammino.risuscito.ui.composable.main.cleanListOptionMenu
import it.cammino.risuscito.ui.composable.main.deleteMenu
import it.cammino.risuscito.ui.composable.main.helpOptionMenu
import it.cammino.risuscito.ui.composable.risuscito_medium_font
import it.cammino.risuscito.ui.interfaces.ActionModeFragment
import it.cammino.risuscito.ui.interfaces.OptionMenuFragment
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.isGridLayout
import it.cammino.risuscito.utils.extension.openCanto
import it.cammino.risuscito.utils.extension.systemLocale
import it.cammino.risuscito.viewmodels.FavoritesViewModel
import it.cammino.risuscito.viewmodels.SharedScrollViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator

class FavoritesFragment : RisuscitoFragment(), ActionModeFragment, SnackBarFragment,
    OptionMenuFragment {
    private val mFavoritesViewModel: FavoritesViewModel by viewModels()
    private val sharedScrollViewModel: SharedScrollViewModel by activityViewModels()
    private var actionModeOk: Boolean = false

    private val selectedItems = MutableLiveData(ArrayList<Int>())

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()
                val localItems by mFavoritesViewModel.mFavoritesSortedResult.observeAsState()
                val localSelectedItems by selectedItems.observeAsState()

                val viewMode by remember { mFavoritesViewModel.viewMode }

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
                            FavoritesViewModel.ViewMode.VIEW -> {
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
                                                localSelectedItems.orEmpty().contains(simpleItem.id)
                                            val source = stringResource(simpleItem.sourceRes)
                                            SimpleListItem(
                                                requireContext(),
                                                simpleItem,
                                                onItemClick = { item ->
                                                    if (mMainActivity?.isActionMode?.value == true) {
                                                        if (isItemSelected) {
                                                            deselectItem(item.id)
                                                        } else {
                                                            selectItem(item.id)
                                                        }
                                                        if (localSelectedItems?.isEmpty() == true) {
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
                                                        selectItem(item.id)
                                                        startCab()
                                                    }
                                                },
                                                selected = isItemSelected,
                                                modifier = Modifier.animateItem()
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        state = state,
                                        modifier = listModifier
                                    ) {
                                        items(
                                            localItems.orEmpty(),
                                            key = { it.id }) { simpleItem ->
                                            val isItemSelected =
                                                localSelectedItems.orEmpty().contains(simpleItem.id)
                                            val source = stringResource(simpleItem.sourceRes)
                                            SimpleListItem(
                                                requireContext(),
                                                simpleItem,
                                                onItemClick = { item ->
                                                    if (mMainActivity?.isActionMode?.value == true) {
                                                        if (isItemSelected) {
                                                            deselectItem(item.id)
                                                        } else {
                                                            selectItem(item.id)
                                                        }
                                                        if (localSelectedItems?.isEmpty() == true) {
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
                                                        selectItem(item.id)
                                                        startCab()
                                                    }
                                                },
                                                selected = isItemSelected,
                                                modifier = Modifier.animateItem(tween())
                                            )
                                        }
                                    }
                                }
                            }

                            FavoritesViewModel.ViewMode.EMPTY -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(), // Occupa solo l'altezza necessaria
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_sunglassed_star),
                                        contentDescription = stringResource(id = R.string.no_favourites_short),
                                        modifier = Modifier
                                            .size(120.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp)) // Spazio tra immagine e testo
                                    Text(
                                        text = stringResource(R.string.no_favourites_short),
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
                if (mFavoritesViewModel.showAlertDialog.observeAsState().value == true) {
                    SimpleAlertDialog(
                        onDismissRequest = {
                            mFavoritesViewModel.showAlertDialog.postValue(false)
                        },
                        onConfirmation = {
                            mFavoritesViewModel.showAlertDialog.postValue(false)
                            val mDao =
                                RisuscitoDatabase.getInstance(requireContext())
                                    .favoritesDao()
                            coroutineScope.launch(Dispatchers.IO) { mDao.resetFavorites() }
                        },
                        dialogTitle = stringResource(R.string.dialog_reset_favorites_title),
                        dialogText = stringResource(R.string.dialog_reset_favorites_desc),
                        icon = Icons.Outlined.ClearAll,
                        confirmButtonText = stringResource(R.string.clear_confirm),
                        dismissButtonText = stringResource(R.string.cancel)
                    )
                }

                mFavoritesViewModel.mFavoritesResult?.observe(viewLifecycleOwner) { canti ->
                    mFavoritesViewModel.mFavoritesSortedResult.value =
                        canti.sortedWith(
                            compareBy(
                                Collator.getInstance(systemLocale)
                            ) { getString(it.titleRes) })
                    mMainActivity?.createOptionsMenu(
                        if (canti.isNotEmpty()) cleanListOptionMenu else helpOptionMenu,
                        this@FavoritesFragment
                    )
                    mFavoritesViewModel.viewMode.value =
                        if (canti.isEmpty()) FavoritesViewModel.ViewMode.EMPTY else FavoritesViewModel.ViewMode.VIEW
                    if (canti.isEmpty())
                        mMainActivity?.expandToolbar()
                }
            }
        }
    }

    private fun selectItem(id: Int) {
        val currentSelected = selectedItems.value.orEmpty()
        val newSelected = ArrayList(currentSelected) // Crea una nuova lista
        newSelected.add(id)
        selectedItems.value = newSelected // Assegna la nuova lista
    }

    private fun deselectItem(id: Int) {
        val currentSelected = selectedItems.value.orEmpty()
        val newSelected = ArrayList(currentSelected) // Crea una nuova lista
        newSelected.remove(id)
        selectedItems.value = newSelected // Assegna la nuova lista
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivity?.setTabVisible(false)
        mMainActivity?.initFab(enable = false)

        if (!PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean(Utility.PREFERITI_OPEN, false)
        ) {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit { putBoolean(Utility.PREFERITI_OPEN, true) }
            Handler(Looper.getMainLooper()).postDelayed(250) {
                Toast.makeText(activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun startCab() {
        actionModeOk = false
        mMainActivity?.createActionMode(deleteMenu, this) { itemRoute ->
            when (itemRoute) {
                ActionModeItem.Delete.route -> {
                    removeFavoritesWithUndo()
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

    private fun updateActionModeTitle() {
        val itemSelectedCount = selectedItems.value.orEmpty().count()
        mMainActivity?.updateActionModeTitle(
            resources.getQuantityString(
                R.plurals.item_selected, itemSelectedCount, itemSelectedCount
            )
        )
    }

    override fun destroyActionMode() {
        if (!actionModeOk)
            selectedItems.value = ArrayList()
    }

    fun removeFavoritesWithUndo() {
        lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(requireContext()).favoritesDao()
            selectedItems.value?.let { removedItems ->
                withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                    for (removedItem in removedItems)
                        mDao.removeFavorite(removedItem)
                }
                showSnackBar(
                    message = resources.getQuantityString(
                        R.plurals.favorites_removed,
                        selectedItems.value.orEmpty().count(),
                        selectedItems.value.orEmpty().count()
                    ),
                    label = getString(R.string.cancel)
                        .uppercase(systemLocale)
                )
            }
        }
    }

    override fun showSnackBar(message: String, label: String?) {
        mMainActivity?.showSnackBar(
            message = message,
            callback = this,
            label = label
        )
    }

    override fun onActionPerformed() {
        for (removedItem in selectedItems.value.orEmpty())
            ListeUtils.addToFavorites(
                this@FavoritesFragment,
                removedItem,
                false
            )
        selectedItems.value = ArrayList()
    }

    override fun onDismissed() {
        selectedItems.value = ArrayList()
    }

    override fun onItemClick(route: String) {
        when (route) {
            OptionMenuItem.ClearAll.route -> {
                mFavoritesViewModel.showAlertDialog.postValue(true)
            }

            OptionMenuItem.Help.route -> {
                Toast.makeText(
                    activity, getString(R.string.new_hint_remove), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private val TAG = FavoritesFragment::class.java.canonicalName
    }

}
