package it.cammino.risuscito.ui.composable.main

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.zIndex
import it.cammino.risuscito.R

enum class FabActionItem(
    val label: Int,
    val iconRes: Int,
) {
    //MOCK PER IL MAIN FAB
    MAIN(
        0,
        R.drawable.edit_24px
    ),

    PULISCI(
        R.string.dialog_reset_list_title,
        R.drawable.cleaning_services_24px
    ),

    ADDLISTA(
        R.string.action_add_list,
        R.drawable.add_24px
    ),

    CONDIVIDI(
        R.string.action_share,
        R.drawable.share_24px
    ),

    CONDIVIDIFILE(
        R.string.action_share_file,
        R.drawable.attach_file_24px
    ),

    EDIT(
        R.string.action_edit_list,
        R.drawable.edit_24px
    ),

    DELETE(
        R.string.action_remove_list,
        R.drawable.delete_24px
    )
}

val listaPersonalizzata =
    listOf(
        FabActionItem.DELETE,
        FabActionItem.EDIT,
        FabActionItem.CONDIVIDIFILE,
        FabActionItem.CONDIVIDI,
        FabActionItem.ADDLISTA,
        FabActionItem.PULISCI,
    )

val listaPredefinita =
    listOf(FabActionItem.CONDIVIDI, FabActionItem.ADDLISTA, FabActionItem.PULISCI)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RisuscitoFab(
    actions: List<FabActionItem>? = emptyList(),
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onFabActionClick: (FabActionItem) -> Unit = {},
    mainIconRes: Int
) {

    val focusRequester = remember { FocusRequester() }

    FloatingActionButtonMenu(
        modifier = Modifier.zIndex(999F),
        expanded = expanded,
        button = {
            if (actions.orEmpty().isEmpty()) {
                FloatingActionButton(
                    onClick = { onFabActionClick(FabActionItem.MAIN) },
                ) {
                    Icon(
                        painter = painterResource(mainIconRes),
                        contentDescription = "Floating action button."
                    )
                }
            } else {
                ToggleFloatingActionButton(
                    modifier =
                        Modifier
                            .semantics {
                                traversalIndex = -1f
                                stateDescription = if (expanded) "Expanded" else "Collapsed"
                                contentDescription = "Toggle menu"
                            },
                    checked = expanded,
                    onCheckedChange = { onExpandedChange(!expanded) },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add_24px),
                        contentDescription = null,
                        modifier = Modifier.rotate(checkedProgress * 135f)
                    )
                }
            }
        },
    ) {
        actions?.forEachIndexed { i, item ->
            FloatingActionButtonMenuItem(
                modifier =
                    Modifier
                        .semantics {
                            isTraversalGroup = true
                            // Add a custom a11y action to allow closing the menu when focusing
                            // the last menu item, since the close button comes before the first
                            // menu item in the traversal order.
                            if (i == actions.size - 1) {
                                customActions =
                                    listOf(
                                        CustomAccessibilityAction(
                                            label = "Close menu",
                                            action = {
                                                onExpandedChange(false)
                                                true
                                            },
                                        )
                                    )
                            }
                        }
                        .then(
                            if (i == 0) {
                                Modifier.onKeyEvent {
                                    // Navigating back from the first item should go back to the
                                    // FAB menu button.
                                    if (
                                        it.type == KeyEventType.KeyDown &&
                                        (it.key == Key.DirectionUp ||
                                                (it.isShiftPressed && it.key == Key.Tab))
                                    ) {
                                        focusRequester.requestFocus()
                                        return@onKeyEvent true
                                    }
                                    return@onKeyEvent false
                                }
                            } else {
                                Modifier
                            }
                        ),
                onClick = {
                    onExpandedChange(false)
                    onFabActionClick(item)
                },
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = stringResource(item.label)
                    )
                },
                text = { Text(text = stringResource(item.label)) },
            )
        }
    }
}