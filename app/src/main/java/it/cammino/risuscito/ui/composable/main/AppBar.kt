package it.cammino.risuscito.ui.composable.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterNone
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.compose.AndroidFragment
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.ContextualToolbarTitle
import it.cammino.risuscito.ui.fragment.SimpleIndexFragment
import it.cammino.risuscito.viewmodels.SharedSearchViewModel
import kotlinx.coroutines.launch

sealed class ActionModeItem(
    val route: String,
    val label: Int,
    val icon: ImageVector,
) {
    object Delete :
        ActionModeItem(
            "action_remove_item",
            R.string.action_remove,
            Icons.Outlined.Delete
        )

    object Undo :
        ActionModeItem(
            "action_remove_item",
            android.R.string.cancel,
            Icons.AutoMirrored.Outlined.Undo
        )

    object SelectNone :
        ActionModeItem(
            "select_none",
            R.string.select_none,
            Icons.Outlined.FilterNone
        )

    object SelectAll :
        ActionModeItem(
            "select_all",
            android.R.string.selectAll,
            Icons.Outlined.LibraryAddCheck
        )

    object Help :
        ActionModeItem(
            "action_help",
            R.string.action_help,
            Icons.AutoMirrored.Outlined.Help
        )

    object Swap :
        ActionModeItem(
            "action_switch_item",
            R.string.action_switch_to,
            Icons.Filled.Shuffle
        )

    object Close :
        ActionModeItem(
            "action_mode_close",
            R.string.material_drawer_close,
            Icons.AutoMirrored.Outlined.ArrowBack
        )
}

val deleteMenu =
    listOf(ActionModeItem.Delete)

val consegnatiMenu =
    listOf(
        ActionModeItem.Undo,
        ActionModeItem.SelectNone,
        ActionModeItem.SelectAll,
        ActionModeItem.Help
    )

val customListsMenu =
    listOf(ActionModeItem.Swap, ActionModeItem.Delete)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithSearch(
    searchBarState: SearchBarState = rememberSearchBarState(),
    onMenuClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    isActionMode: Boolean = false,
    actionModeMenu: List<ActionModeItem> = emptyList(),
    hideNavigation: Boolean = false,
    onActionModeClick: (String) -> Unit = {},
    contextualTitle: String = "",
    sharedSearchViewModel: SharedSearchViewModel
) {

    TopAppBar(
        title = {
            if (!isActionMode) {
                val textFieldState = rememberTextFieldState()

                LaunchedEffect(searchBarState.currentValue) {
                    Log.d("APPBAR", "searchBarState1: $searchBarState")
                    textFieldState.edit { replace(0, length, "") }
                    sharedSearchViewModel.searchFilter.value = ""
                }

                val scope = rememberCoroutineScope()
                val inputField =
                    @Composable {
                        SearchBarDefaults.InputField(
                            query = textFieldState.text.toString(),
                            onQueryChange = {
                                textFieldState.edit { replace(0, length, it) }
                                sharedSearchViewModel.searchFilter.value = it
                            },
                            expanded = searchBarState.currentValue == SearchBarValue.Expanded,
                            onExpandedChange = {
                                if (it) scope.launch { searchBarState.animateToExpanded() }
                                else {
                                    scope.launch { searchBarState.animateToCollapsed() }
                                }
                            },
                            onSearch = { },
                            placeholder = {
                                Text(
                                    if (searchBarState.currentValue == SearchBarValue.Expanded) stringResource(
                                        R.string.search_hint
                                    ) else stringResource(R.string.search_name_text),
                                    textAlign = if (searchBarState.currentValue == SearchBarValue.Expanded) TextAlign.Start else TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            leadingIcon = {
                                if (searchBarState.currentValue == SearchBarValue.Expanded) {
                                    IconButton(onClick = {
                                        scope.launch { searchBarState.animateToCollapsed() }
                                    }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.material_drawer_close)
                                        )
                                    }
                                }
                            },
                            trailingIcon = {
                                if (textFieldState.text.isNotEmpty()) {
                                    IconButton(onClick = {
                                        textFieldState.edit { replace(0, length, "") }
                                        sharedSearchViewModel.searchFilter.value = ""
                                    }) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Cancella"
                                        )
                                    }
                                }
                            }
                        )
                    }
                SearchBar(
                    state = searchBarState,
                    inputField = inputField,
                    modifier =
                        Modifier
                            .padding(top = 8.dp, bottom = 8.dp, start = 0.dp, end = 16.dp)
                            .fillMaxWidth()
                )
                ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {
                    var selected by remember { mutableStateOf(false) }
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp)
                        ) {
                            FilterChip(
                                onClick = {
                                    selected = !selected
                                    sharedSearchViewModel.advancedSearchFilter.value = selected
                                },
                                label = {
                                    Text(stringResource(R.string.advanced_search_subtitle))
                                },
                                selected = selected,
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = "Done icon",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                        AndroidFragment<SimpleIndexFragment>(
                            arguments = bundleOf(
                                SimpleIndexFragment.INDICE_LISTA to 0,
                                SimpleIndexFragment.IS_SEARCH to true
                            )
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = isActionMode,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                ContextualToolbarTitle(contextualTitle)
            }
        },
        colors = if (isActionMode) TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ) else TopAppBarDefaults.topAppBarColors(),
        navigationIcon = {
            if (isActionMode) {
                if (!hideNavigation) { // Mostra l'icona del menu solo se la barra di ricerca non è espansa
                    IconButton(onClick = { onActionModeClick(ActionModeItem.Close.route) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.material_drawer_close),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.material_drawer_open)
                    )
                }
            }
        },
        actions = {
            if (isActionMode) {
                actionModeMenu.forEach { item ->
                    IconButton(onClick = {
                        onActionModeClick(item.route)
                    }) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(item.label),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun StatusBarProtection(
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    heightProvider: () -> Float = calculateGradientHeight(),
) {

    Canvas(Modifier.fillMaxSize()) {
        val calculatedHeight = heightProvider()
        val gradient = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 1f),
                color.copy(alpha = .8f),
                Color.Transparent
            ),
            startY = 0f,
            endY = calculatedHeight
        )
        drawRect(
            brush = gradient,
            size = Size(size.width, calculatedHeight),
        )
    }
}

@Composable
fun calculateGradientHeight(): () -> Float {
    val statusBars = WindowInsets.statusBars
    val density = LocalDensity.current
    return { statusBars.getTop(density).times(1.2f) }
}