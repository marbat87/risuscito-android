package it.cammino.risuscito.ui.composable.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import it.cammino.risuscito.R
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.ui.composable.SideMenuTitle
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel

@Composable
fun AddToDropDownMenu(
    fragment: Fragment,
    viewModel: SimpleIndexViewModel,
    dialogTag: SimpleDialogTag,
    dialogTag2: SimpleDialogTag,
    listePersonalizzate: List<ListaPers>?,
    menuExpanded: Boolean,
    offset: DpOffset,
    onDismissRequest: () -> Unit
) {

    var parolaExpanded by remember { mutableStateOf(false) }
    var eucarestiaExpanded by remember { mutableStateOf(false) }
    var subMenuExpanded by remember { mutableIntStateOf(0) }
    val pref = PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { onDismissRequest() },
        offset = offset
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.select_canto_popup)) },
            onClick = {},
            enabled = false
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.title_activity_favourites)) },
            onClick = {
                onDismissRequest()
                ListeUtils.addToFavorites(fragment, viewModel.idDaAgg, true)
            }
        )

        MenuExpandableItem(
            text = stringResource(R.string.title_activity_canti_parola),
            onClick = {
                onDismissRequest()
                parolaExpanded = true
            }
        )

        MenuExpandableItem(
            text = stringResource(R.string.title_activity_canti_eucarestia),
            onClick = {
                onDismissRequest()
                eucarestiaExpanded = true
            }
        )

        listePersonalizzate?.let {
            for (i in it.indices) {
                MenuExpandableItem(
                    text = it[i].lista!!.name,
                    onClick = {
                        onDismissRequest()
                        subMenuExpanded = 10 + i
                    }
                )
            }
        }
    }

    DropdownMenu(
        expanded = parolaExpanded,
        onDismissRequest = { parolaExpanded = false },
        offset = offset
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.title_activity_canti_parola)) },
            onClick = {},
            enabled = false
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.canto_iniziale)) },
            onClick = {
                parolaExpanded = false
                viewModel.addToListaNoDup(1, 1, fragment, dialogTag2)
            }
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.prima_lettura)) },
            onClick = {
                parolaExpanded = false
                viewModel.addToListaNoDup(1, 2, fragment, dialogTag2)
            }
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.seconda_lettura)) },
            onClick = {
                parolaExpanded = false
                viewModel.addToListaNoDup(1, 3, fragment, dialogTag2)
            }
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.terza_lettura)) },
            onClick = {
                parolaExpanded = false
                viewModel.addToListaNoDup(1, 4, fragment, dialogTag2)
            }
        )

        if (pref.getBoolean(Utility.SHOW_PACE, false)) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.canto_pace)) },
                onClick = {
                    parolaExpanded = false
                    viewModel.addToListaNoDup(1, 6, fragment, dialogTag2)
                }
            )
        }

        DropdownMenuItem(
            text = { Text(stringResource(R.string.canto_fine)) },
            onClick = {
                parolaExpanded = false
                viewModel.addToListaNoDup(1, 5, fragment, dialogTag2)
            }
        )

    }

    DropdownMenu(
        expanded = eucarestiaExpanded,
        onDismissRequest = { eucarestiaExpanded = false },
        offset = offset
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.title_activity_canti_eucarestia)) },
            onClick = {},
            enabled = false
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.canto_iniziale)) },
            onClick = {
                eucarestiaExpanded = false
                viewModel.addToListaNoDup(2, 1, fragment, dialogTag2)
            }
        )

        if (pref.getBoolean(Utility.SHOW_SECONDA, false)) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.seconda_lettura)) },
                onClick = {
                    eucarestiaExpanded = false
                    viewModel.addToListaNoDup(2, 6, fragment, dialogTag2)
                }
            )
        }

        DropdownMenuItem(
            text = { Text(stringResource(R.string.canto_pace)) },
            onClick = {
                eucarestiaExpanded = false
                viewModel.addToListaNoDup(2, 2, fragment, dialogTag2)
            }
        )

        if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false)) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.canto_offertorio)) },
                onClick = {
                    eucarestiaExpanded = false
                    viewModel.addToListaNoDup(2, 8, fragment, dialogTag2)
                }
            )
        }

        if (pref.getBoolean(Utility.SHOW_SANTO, false)) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.santo)) },
                onClick = {
                    eucarestiaExpanded = false
                    viewModel.addToListaNoDup(2, 8, fragment, dialogTag2)
                }
            )
        }

        DropdownMenuItem(
            text = { Text(stringResource(R.string.canto_pane)) },
            onClick = {
                eucarestiaExpanded = false
                viewModel.addToListaNoDup(2, 3, fragment, dialogTag2)
            }
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.canto_vino)) },
            onClick = {
                eucarestiaExpanded = false
                ListeUtils.addToListaDup(fragment, 2, 4, viewModel.idDaAgg)
            }
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.canto_fine)) },
            onClick = {
                eucarestiaExpanded = false
                viewModel.addToListaNoDup(2, 5, fragment, dialogTag2)
            }
        )
    }

    listePersonalizzate?.let { liste ->
        for (i in liste.indices) {
            DropdownMenu(
                expanded = (subMenuExpanded == 10 + i),
                onDismissRequest = { subMenuExpanded = 0 },
                offset = offset
            ) {
                DropdownMenuItem(
                    text = { Text(liste[i].lista!!.name) },
                    onClick = {},
                    enabled = false
                )

                for (k in 0 until liste[i].lista!!.numPosizioni) {
                    DropdownMenuItem(
                        text = { Text(liste[i].lista!!.getNomePosizione(k)) },
                        onClick = {
                            subMenuExpanded = 0
                            if (liste[i]
                                    .lista
                                    ?.getCantoPosizione(k)?.isEmpty() == true
                            ) {
                                liste[i]
                                    .lista
                                    ?.addCanto(
                                        (viewModel.idDaAgg).toString(), k
                                    )
                                ListeUtils.updateListaPersonalizzata(fragment, liste[i])
                            } else {
                                if (liste[i]
                                        .lista
                                        ?.getCantoPosizione(k) == (viewModel.idDaAgg).toString()
                                ) {
                                    Snackbar.make(
                                        fragment.requireActivity()
                                            .findViewById(android.R.id.content),
                                        R.string.present_yet,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                } else {
                                    viewModel.manageReplaceDialog(
                                        fragment, Integer.parseInt(
                                            liste[i]
                                                .lista
                                                ?.getCantoPosizione(k)
                                                ?: "0"
                                        ), dialogTag
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PassaggesDropDownMenu(
    menuExpanded: Boolean,
    nomiPassaggi: Array<String>,
    indiciPassaggi: IntArray,
    passaggiSelezionati: MutableLiveData<List<Int>>,
    onDismissRequest: () -> Unit,
    onSelect: (Int, Boolean) -> Unit,
) {

    val passaggiSelectedItems = passaggiSelezionati.observeAsState()

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { onDismissRequest() }
    ) {

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SideMenuTitle(stringResource(R.string.passage_filter))
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onDismissRequest() }) {
                    Icon(
                        painter = painterResource(R.drawable.close_24px),
                        contentDescription = "Close"
                    )
                }
            }

            indiciPassaggi.forEachIndexed { i, item ->
                MenuSelectabletem(
                    text = nomiPassaggi[i],
                    onSelect = {
                        onSelect(item, it)
                    },
                    selected = passaggiSelectedItems.value?.contains(item) ?: false
                )
            }

        }

    }

}

enum class DropDownMenuItem(
    val label: Int,
    val iconRes: Int,
    val value: Int
) {
    DEFAULT(
        0,
        0,
        0
    ),

    TONALITA_TRASPORTA(
        R.string.action_trasporta,
        R.drawable.swap_vert_24px,
        0
    ),

    TONO_DO(
        R.string.action_trasporta_do,
        0,
        R.string.title_trasporta_do
    ),

    TONO_DO_D(
        R.string.action_trasporta_dod,
        0,
        R.string.title_trasporta_dod
    ),

    TONO_RE(
        R.string.action_trasporta_re,
        0,
        R.string.title_trasporta_re
    ),

    TONO_MI_B(
        R.string.action_trasporta_mib,
        0,
        R.string.title_trasporta_mib
    ),

    TONO_MI(
        R.string.action_trasporta_mi,
        0,
        R.string.title_trasporta_mi
    ),

    TONO_FA(
        R.string.action_trasporta_fa,
        0,
        R.string.title_trasporta_fa
    ),

    TONO_FA_D(
        R.string.action_trasporta_fad,
        0,
        R.string.title_trasporta_fad
    ),

    TONO_SOL(
        R.string.action_trasporta_sol,
        0,
        R.string.title_trasporta_sol
    ),

    TONO_SOL_D(
        R.string.action_trasporta_sold,
        0,
        R.string.title_trasporta_sold
    ),

    TONO_LA(
        R.string.action_trasporta_la,
        0,
        R.string.title_trasporta_la
    ),

    TONO_SI_B(
        R.string.action_trasporta_sib,
        0,
        R.string.title_trasporta_sib
    ),

    TONO_SI(
        R.string.action_trasporta_si,
        0,
        R.string.title_trasporta_si
    ),

    TONALITA_SALVA(
        R.string.action_salva,
        R.drawable.save_24px,
        0
    ),

    TONALITA_RESET(
        R.string.action_reset_tonalita,
        R.drawable.refresh_24px,
        0
    ),

    /*TONALITA END
     */

    BARRE_TRASPORTA(
        R.string.action_trasporta,
        R.drawable.swap_vert_24px,
        0
    ),

    BARRE_NO(
        R.string.action_trabar_NO,
        0,
        0
    ),

    BARRE_I(
        R.string.action_trabar_I,
        0,
        R.string.title_trabar_I
    ),

    BARRE_II(
        R.string.action_trabar_II,
        0,
        R.string.title_trabar_II
    ),

    BARRE_III(
        R.string.action_trabar_III,
        0,
        R.string.title_trabar_III
    ),

    BARRE_IV(
        R.string.action_trabar_IV,
        0,
        R.string.title_trabar_IV
    ),

    BARRE_V(
        R.string.action_trabar_V,
        0,
        R.string.title_trabar_V
    ),

    BARRE_VI(
        R.string.action_trabar_VI,
        0,
        R.string.title_trabar_VI
    ),

    BARRE_VII(
        R.string.action_trabar_VII,
        0,
        R.string.title_trabar_VII
    ),

    BARRE_SALVA(
        R.string.action_salva,
        R.drawable.save_24px,
        0
    ),

    BARRE_RESET(
        R.string.action_reset_barre,
        R.drawable.refresh_24px,
        0
    ),

//    EXPORT_PDF(
//        R.string.action_exp_pdf,
//        R.drawable.picture_as_pdf_24px,
//        0
//    ),

//    HELP(
//        R.string.action_help,
//        R.drawable.help_24px,
//        0
//    )

}

val tontalitaDropDownMenu =
    mutableMapOf(
        DropDownMenuItem.TONALITA_TRASPORTA to listOf(
            DropDownMenuItem.TONO_DO,
            DropDownMenuItem.TONO_DO_D,
            DropDownMenuItem.TONO_RE,
            DropDownMenuItem.TONO_MI_B,
            DropDownMenuItem.TONO_MI,
            DropDownMenuItem.TONO_FA,
            DropDownMenuItem.TONO_FA_D,
            DropDownMenuItem.TONO_SOL,
            DropDownMenuItem.TONO_SOL_D,
            DropDownMenuItem.TONO_LA,
            DropDownMenuItem.TONO_SI_B,
            DropDownMenuItem.TONO_SI
        ),
        DropDownMenuItem.TONALITA_SALVA to emptyList(),
        DropDownMenuItem.TONALITA_RESET to emptyList()
    )

val barreDropDownMenu =
    mutableMapOf(
        DropDownMenuItem.BARRE_TRASPORTA to listOf(
            DropDownMenuItem.BARRE_NO,
            DropDownMenuItem.BARRE_I,
            DropDownMenuItem.BARRE_II,
            DropDownMenuItem.BARRE_III,
            DropDownMenuItem.BARRE_IV,
            DropDownMenuItem.BARRE_V,
            DropDownMenuItem.BARRE_VI,
            DropDownMenuItem.BARRE_VII
        ),
        DropDownMenuItem.BARRE_SALVA to emptyList(),
        DropDownMenuItem.BARRE_RESET to emptyList()
    )

//val otherDropDownMenu =
//    mutableMapOf<DropDownMenuItem, List<DropDownMenuItem>>(
//        DropDownMenuItem.EXPORT_PDF to emptyList(),
//        DropDownMenuItem.HELP to emptyList()
//    )

@Composable
fun CantoDropDownMenu(
    menu: Map<DropDownMenuItem, List<DropDownMenuItem>>,
    menuExpanded: Boolean,
    offset: DpOffset = DpOffset.Zero,
    onItemClick: (DropDownMenuItem) -> Unit,
    onDismissRequest: () -> Unit
) {

    var subMenuExpanded by remember { mutableStateOf(DropDownMenuItem.DEFAULT) }

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { onDismissRequest() },
        offset = offset
    ) {
        menu.forEach { item ->
            if (item.value.isNotEmpty()) {
                MenuExpandableItem(
                    text = stringResource(item.key.label),
                    onClick = {
                        onDismissRequest()
                        subMenuExpanded = item.key
                    },
                    iconRes = item.key.iconRes
                )
            } else {
                MenuSimpleItem(
                    textRes = item.key.label,
                    onClick = {
                        onDismissRequest()
                        onItemClick(item.key)
                    },
                    iconRes = item.key.iconRes
                )
            }
        }
    }

    menu.forEach { item ->
        if (item.value.isNotEmpty()) {
            DropdownMenu(
                expanded = subMenuExpanded == item.key,
                onDismissRequest = { subMenuExpanded = DropDownMenuItem.DEFAULT },
                offset = offset
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(item.key.label)) },
                    onClick = {},
                    enabled = false
                )

                item.value.forEach { listItem ->
                    MenuSimpleItem(
                        textRes = listItem.label,
                        onClick = {
                            subMenuExpanded = DropDownMenuItem.DEFAULT
                            onItemClick(listItem)
                        },
                        iconRes = listItem.iconRes
                    )
                }
            }
        }
    }

}