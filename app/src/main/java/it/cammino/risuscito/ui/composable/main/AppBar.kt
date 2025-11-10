package it.cammino.risuscito.ui.composable.main

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.Card
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.compose.AndroidFragment
import it.cammino.risuscito.R
import it.cammino.risuscito.ui.composable.ContextualToolbarTitle
import it.cammino.risuscito.ui.composable.dialogs.AccountMenuImage
import it.cammino.risuscito.ui.composable.hasDrawer
import it.cammino.risuscito.ui.fragment.SimpleIndexFragment
import it.cammino.risuscito.viewmodels.SharedSearchViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

enum class ActionModeItem(
    val label: Int,
    val iconRes: Int,
) {
    DELETE(
        R.string.action_remove,
        R.drawable.delete_24px
    ),

    UNDO(
        android.R.string.cancel,
        R.drawable.undo_24px
    ),

    SELECTNONE(
        R.string.select_none,
        R.drawable.filter_none_24px
    ),

    SELECTALL(
        android.R.string.selectAll,
        R.drawable.library_add_check_24px
    ),

    HELP(
        R.string.action_help,
        R.drawable.help_24px
    ),

    SWAP(
        R.string.action_switch_to,
        R.drawable.shuffle_24px
    ),

    CLOSE(
        R.string.material_drawer_close,
        R.drawable.arrow_back_24px
    ),

    TONALITA(
        R.string.action_tonalita,
        R.drawable.music_note_24px
    ),

    BARRE(
        R.string.action_barre,
        R.drawable.guitar_acoustic_24
    ),

    EXPORT_PDF(
        R.string.action_exp_pdf,
        R.drawable.picture_as_pdf_24px,
    )

}

val deleteMenu =
    listOf(ActionModeItem.DELETE)

val consegnatiMenu =
    listOf(
        ActionModeItem.UNDO,
        ActionModeItem.SELECTNONE,
        ActionModeItem.SELECTALL
    )
//val consegnatiMenu =
//    listOf(
//        ActionModeItem.UNDO,
//        ActionModeItem.SELECTNONE,
//        ActionModeItem.SELECTALL,
//        ActionModeItem.HELP
//    )

val customListsMenu =
    listOf(ActionModeItem.SWAP, ActionModeItem.DELETE)

val creaListaMenu =
    listOf(ActionModeItem.HELP)

val cantoMenu =
    listOf(ActionModeItem.TONALITA, ActionModeItem.BARRE, ActionModeItem.EXPORT_PDF)
//val cantoMenu =
//    listOf(ActionModeItem.TONALITA, ActionModeItem.BARRE, ActionModeItem.EXPORT_PDF, ActionModeItem.HELP)


sealed class OptionMenuItem(
    val route: String,
    val label: Int,
    val iconRes: Int,
) {
    object ClearAll :
        OptionMenuItem(
            "list_reset",
            R.string.dialog_reset_favorites_title,
            R.drawable.clear_all_24px
        )

    object Help :
        OptionMenuItem(
            "action_help",
            R.string.action_help,
            R.drawable.help_24px
        )

    object FilterRemove :
        OptionMenuItem(
            "action_filter_remove",
            R.string.filters_remove,
            R.drawable.filter_list_off_24px
        )

    object Filter :
        OptionMenuItem(
            "action_filter",
            R.string.passage_filter,
            R.drawable.filter_list_24px
        )
}

val helpOptionMenu =
    listOf(OptionMenuItem.Help)

val cleanListOptionMenu =
    listOf(OptionMenuItem.ClearAll, OptionMenuItem.Help)

val consegnatiOptionMenu =
    listOf(OptionMenuItem.Filter)
//val consegnatiOptionMenu =
//    listOf(OptionMenuItem.Filter, OptionMenuItem.Help)

val consegnatiResetOptionMenu =
    listOf(OptionMenuItem.FilterRemove, OptionMenuItem.Filter)
//val consegnatiResetOptionMenu =
//    listOf(OptionMenuItem.FilterRemove, OptionMenuItem.Filter, OptionMenuItem.Help)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithSearch(
    onMenuClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior,
    isActionMode: Boolean = false,
    actionModeMenu: List<ActionModeItem> = emptyList(),
    hideNavigation: Boolean = false,
    onActionModeClick: (ActionModeItem) -> Unit = {},
    contextualTitle: String = "",
    sharedSearchViewModel: SharedSearchViewModel,
    optionMenu: List<OptionMenuItem>? = emptyList(),
    onOptionMenuClick: (String) -> Unit = {},
    loggedIn: Boolean = false,
    profilePhotoUrl: String = "",
    onProfileClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {

    TopAppBar(
        title = {
            if (!isActionMode) {
                val textFieldState = rememberTextFieldState()
                val searchBarState = rememberSearchBarState()
                val scope = rememberCoroutineScope()

                LaunchedEffect(searchBarState) {
                    snapshotFlow { searchBarState.currentValue }
                        .distinctUntilChanged()
                        .collect {
                            if (it == SearchBarValue.Collapsed) {
                                textFieldState.edit { replace(0, length, "") }
                                sharedSearchViewModel.searchFilter.value = ""
                            }
                        }
                }

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
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                if (searchBarState.currentValue == SearchBarValue.Expanded) {
                                    TooltipBox(
                                        positionProvider =
                                            TooltipDefaults.rememberTooltipPositionProvider(
                                                TooltipAnchorPosition.Above
                                            ),
                                        tooltip = { PlainTooltip { Text(stringResource(R.string.material_drawer_close)) } },
                                        state = rememberTooltipState(),
                                    ) {
                                        IconButton(
                                            onClick = { scope.launch { searchBarState.animateToCollapsed() } }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.arrow_back_24px),
                                                contentDescription = stringResource(R.string.material_drawer_close),
                                            )
                                        }
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
                                            painter = painterResource(R.drawable.close_24px),
                                            contentDescription = "Cancella"
                                        )
                                    }
                                } else {
                                    if (searchBarState.currentValue == SearchBarValue.Collapsed) {
                                        AppBarRow(overflowIndicator = {}) {
                                            optionMenu?.forEach {
                                                clickableItem(
                                                    onClick = { onOptionMenuClick(it.route) },
                                                    icon = {
                                                        Icon(
                                                            painter = painterResource(it.iconRes),
                                                            contentDescription = stringResource(it.label)
                                                        )
                                                    },
                                                    label = "",
                                                )
                                            }
                                        }
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
                            .padding(vertical = 8.dp, horizontal = 4.dp)
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
                                            painter = painterResource(R.drawable.check_24px),
                                            contentDescription = "Done icon",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            AndroidFragment<SimpleIndexFragment>(
                                arguments = bundleOf(
                                    SimpleIndexFragment.INDICE_LISTA to 0,
                                    SimpleIndexFragment.IS_SEARCH to true
                                )
                            )
                        }
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
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ) else TopAppBarDefaults.topAppBarColors(),
        navigationIcon = {
            if (isActionMode) {
                if (!hideNavigation) { // Mostra l'icona del menu solo se la barra di ricerca non è espansa
                    IconButton(
                        onClick = { onActionModeClick(ActionModeItem.CLOSE) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_24px),
                            contentDescription = stringResource(R.string.material_drawer_close),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                if (hasDrawer()) {
                    IconButton(
                        onClick = onMenuClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.menu_24px),
                            contentDescription = stringResource(R.string.material_drawer_open)
                        )
                    }
                }
            }
        },
        actions = {
            if (isActionMode) {
                AppBarRow(overflowIndicator = {}) {
                    actionModeMenu.forEach {
                        clickableItem(
                            onClick = { onActionModeClick(it) },
                            icon = {
                                Icon(
                                    painter = painterResource(it.iconRes),
                                    contentDescription = stringResource(it.label),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            label = "",
                        )
                    }
                }
            } else {
                AccountMenuImage(
                    onProfileClick = onProfileClick,
                    onLoginClick = onLoginClick,
                    loggedIn = loggedIn,
                    profilePhotoUrl = profilePhotoUrl
                )
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