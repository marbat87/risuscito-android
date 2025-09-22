package it.cammino.risuscito.ui.composable.dialogs

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import it.cammino.risuscito.R
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.viewmodels.SimpleIndexViewModel

@Composable
fun AddToDropDownMenu(
    fragment: Fragment,
    viewModel: SimpleIndexViewModel,
    dialogTag: String,
    dialogTag2: String,
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