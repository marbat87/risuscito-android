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
import it.cammino.risuscito.R

open class FabActionItem(
    val id: String,
    val label: Int,
    val iconRes: Int,
    val selectedLabel: Int = 0,
    val selectedIconRes: Int = 0,
    val selected: Boolean = false
) {
    //MOCK PER IL MAIN FAB
    object MAIN : FabActionItem(
        "MAIN",
        0,
        R.drawable.edit_24px
    )

    object PULISCI : FabActionItem(
        "PULISCI",
        R.string.dialog_reset_list_title,
        R.drawable.cleaning_services_24px
    )

    object ADDLISTA : FabActionItem(
        "ADDLISTA",
        R.string.action_add_list,
        R.drawable.add_24px
    )

    object CONDIVIDI : FabActionItem(
        "CONDIVIDI",
        R.string.action_share,
        R.drawable.share_24px
    )

    object CONDIVIDIFILE : FabActionItem(
        "CONDIVIDIFILE",
        R.string.action_share_file,
        R.drawable.attach_file_24px
    )

    object EDIT : FabActionItem(
        "EDIT",
        R.string.action_edit_list,
        R.drawable.edit_24px
    )

    object DELETE : FabActionItem(
        "DELETE",
        R.string.action_remove_list,
        R.drawable.delete_24px
    )

    object FULLSCREEN : FabActionItem(
        "FULLSCREEN",
        R.string.fullscreen,
        R.drawable.fullscreen_24px
    )

    object SOUND : FabActionItem(
        "SOUND",
        R.string.audio_on,
        R.drawable.headphones_24px,
        R.string.audio_off,
        R.drawable.headset_off_24px
    )

    object DELETEFILE : FabActionItem(
        "DELETEFILE",
        R.string.fab_delete_unlink,
        R.drawable.delete_24px,
        R.string.dialog_delete_link_title,
        R.drawable.link_off_24px
    )

    object SAVEFILE : FabActionItem(
        "SAVEFILE",
        R.string.save_file,
        R.drawable.file_download_24px
    )

    object LINKFILE : FabActionItem(
        "LINKFILE",
        R.string.only_link_title,
        R.drawable.add_link_24px
    )

    object FAVORITE : FabActionItem(
        "FAVORITE",
        R.string.favorite_on,
        R.drawable.bookmark_add_24px,
        R.string.favorite_off,
        R.drawable.bookmark_remove_24px
    )

    fun copy(newSelected: Boolean): FabActionItem {
        return FabActionItem(
            id = id,
            label = label,
            iconRes = iconRes,
            selectedLabel = selectedLabel,
            selectedIconRes = selectedIconRes,
            selected = newSelected
        )
    }
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

val cantoFabActions =
    listOf(
        FabActionItem.FAVORITE,
        FabActionItem.LINKFILE,
        FabActionItem.SAVEFILE,
        FabActionItem.DELETEFILE,
        FabActionItem.SOUND,
        FabActionItem.FULLSCREEN
    )

val listaPredefinita =
    listOf(FabActionItem.CONDIVIDI, FabActionItem.ADDLISTA, FabActionItem.PULISCI)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RisuscitoFab(
    actions: List<FabActionItem>? = emptyList(),
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onFabActionClick: (String) -> Unit = {},
    mainIconRes: Int
) {

    val focusRequester = remember { FocusRequester() }

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            if (actions.orEmpty().isEmpty()) {
                FloatingActionButton(
                    onClick = { onFabActionClick(FabActionItem.MAIN.id) },
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
                    onFabActionClick(item.id)
                },
                icon = {
                    Icon(
                        painter = painterResource(if (item.selected) item.selectedIconRes else item.iconRes),
                        contentDescription = stringResource(if (item.selected) item.selectedLabel else item.label)
                    )
                },
                text = { Text(text = stringResource(if (item.selected) item.selectedLabel else item.label)) },
            )
        }
    }
}