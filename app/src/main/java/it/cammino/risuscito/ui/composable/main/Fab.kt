package it.cammino.risuscito.ui.composable.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
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

sealed class FabActionItem(
    val route: String,
    val label: Int,
    val icon: ImageVector,
) {
    //MOCK PER IL MAIN FAB
    object Main :
        FabActionItem(
            "main_fab",
            0,
            icon = Icons.Outlined.Edit
        )

    object Pulisci :
        FabActionItem(
            "fab_pulisci",
            R.string.dialog_reset_list_title,
            Icons.Outlined.CleaningServices
        )

    object AddLista :
        FabActionItem(
            "fab_add_lista",
            R.string.action_add_list,
            Icons.Filled.Add
        )

    object Condividi :
        FabActionItem(
            "fab_condividi",
            R.string.action_share,
            Icons.Outlined.Share
        )

    object CondividiFile :
        FabActionItem(
            "fab_condividi_file",
            R.string.action_share_file,
            Icons.Outlined.AttachFile
        )

    object Edit :
        FabActionItem(
            "fab_edit_lista",
            R.string.action_edit_list,
            Icons.Outlined.Edit
        )

    object Delete :
        FabActionItem(
            "fab_delete_lista",
            R.string.action_remove_list,
            Icons.Outlined.Delete
        )
}

val listaPersonalizzata =
    listOf(
        FabActionItem.Delete,
        FabActionItem.Edit,
        FabActionItem.CondividiFile,
        FabActionItem.Condividi,
        FabActionItem.AddLista,
        FabActionItem.Pulisci,
    )

val listaPredefinita =
    listOf(FabActionItem.Condividi, FabActionItem.AddLista, FabActionItem.Pulisci)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RisuscitoFab(
    actions: List<FabActionItem>? = emptyList(),
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onFabActionClick: (FabActionItem) -> Unit = {},
    mainIcon: ImageVector
) {

    val focusRequester = remember { FocusRequester() }

    FloatingActionButtonMenu(
        modifier = Modifier.zIndex(999F),
        expanded = expanded,
        button = {
            if (actions.orEmpty().isEmpty()) {
                FloatingActionButton(
                    onClick = { onFabActionClick(FabActionItem.Main) },
                ) {
                    Icon(mainIcon, "Floating action button.")
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
                        imageVector = Icons.Filled.Add,
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
                icon = { Icon(item.icon, contentDescription = null) },
                text = { Text(text = stringResource(item.label)) },
            )
        }
    }
}