package it.cammino.risuscito.viewmodels

import android.app.Application
import android.view.Gravity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.android.material.snackbar.Snackbar
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.InsertItem
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.utils.ListeUtils
import java.util.*

open class GenericIndexViewModel(application: Application) : AndroidViewModel(application) {

    var idDaAgg: Int = 0
    var idListaDaAgg: Int = 0
    var posizioneDaAgg: Int = 0
    var idListaClick: Int = 0
    var idPosizioneClick: Int = 0
    var titoli: List<SimpleItem> = ArrayList()
    var titoliInsert: List<InsertItem> = ArrayList()
    internal var mDb: RisuscitoDatabase? = null

    fun popupMenu(fragment: Fragment, view: View, dialogTag: String, dialogTag2: String, listePersonalizzate: List<ListaPers>?) {
        val popupMenu = popupMenu {
            dropdownGravity = Gravity.END
            section {
                title = fragment.getString(R.string.select_canto) + ":"
                item {
                    labelRes = R.string.title_activity_favourites
                    callback = {
                        ListeUtils.addToFavorites(fragment, idDaAgg)
                    }
                }
                item {
                    labelRes = R.string.title_activity_canti_parola
                    hasNestedItems = true
                    callback = {
                        subPopupMenu(fragment.getString(R.string.title_activity_canti_parola), 1, view, 0, fragment, dialogTag, dialogTag2, listePersonalizzate)
                    }
                }
                item {
                    labelRes = R.string.title_activity_canti_eucarestia
                    hasNestedItems = true
                    callback = {
                        subPopupMenu(fragment.getString(R.string.title_activity_canti_eucarestia), 2, view, 0, fragment, dialogTag, dialogTag2, listePersonalizzate)
                    }
                }
                listePersonalizzate?.let {
                    for (i in it.indices) {
                        item {
                            label = it[i].lista!!.name
                            hasNestedItems = true
                            callback = {
                                subPopupMenu(it[i].lista!!.name!!, 3, view, i, fragment, dialogTag, dialogTag2, listePersonalizzate)
                            }
                        }
                    }
                }
            }
        }
        popupMenu.show(fragment.context!!, view)
    }

    private fun subPopupMenu(menuTitle: String, tipoLista: Int, mView: View, idLista: Int, fragment: Fragment, dialogTag: String, dialogTag2: String, listePersonalizzate: List<ListaPers>?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(fragment.context)
        val popupMenu = popupMenu {
            dropdownGravity = Gravity.END
            section {
                title = menuTitle
                when (tipoLista) {
                    1 -> {
                        item {
                            labelRes = R.string.canto_iniziale
                            callback = {
                                //optional
                                addToListaNoDup(1, 1, fragment, dialogTag2)
                            }
                        }
                        item {
                            labelRes = R.string.prima_lettura
                            callback = {
                                //optional
                                addToListaNoDup(1, 2, fragment, dialogTag2)
                            }
                        }
                        item {
                            labelRes = R.string.seconda_lettura
                            callback = {
                                //optional
                                addToListaNoDup(1, 3, fragment, dialogTag2)
                            }
                        }
                        item {
                            labelRes = R.string.terza_lettura
                            callback = {
                                //optional
                                addToListaNoDup(1, 4, fragment, dialogTag2)
                            }
                        }
                        if (pref.getBoolean(Utility.SHOW_PACE, false)) {
                            item {
                                labelRes = R.string.canto_pace
                                callback = {
                                    //optional
                                    addToListaNoDup(1, 6, fragment, dialogTag2)
                                }
                            }
                        }
                        item {
                            labelRes = R.string.canto_fine
                            callback = {
                                //optional
                                addToListaNoDup(1, 5, fragment, dialogTag2)
                            }
                        }
                    }
                    2 -> {
                        item {
                            labelRes = R.string.canto_iniziale
                            callback = {
                                //optional
                                addToListaNoDup(2, 1, fragment, dialogTag2)
                            }
                        }
                        if (pref.getBoolean(Utility.SHOW_SECONDA, false)) {
                            item {
                                labelRes = R.string.seconda_lettura
                                callback = {
                                    //optional
                                    addToListaNoDup(2, 6, fragment, dialogTag2)
                                }
                            }
                        }
                        item {
                            labelRes = R.string.canto_pace
                            callback = {
                                //optional
                                addToListaNoDup(2, 2, fragment, dialogTag2)
                            }
                        }
                        if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false)) {
                            item {
                                labelRes = R.string.canto_offertorio
                                callback = {
                                    //optional
                                    addToListaNoDup(2, 8, fragment, dialogTag2)
                                }
                            }
                        }
                        if (pref.getBoolean(Utility.SHOW_SANTO, false)) {
                            item {
                                labelRes = R.string.santo
                                callback = {
                                    //optional
                                    addToListaNoDup(2, 7, fragment, dialogTag2)
                                }
                            }
                        }
                        item {
                            labelRes = R.string.canto_pane
                            callback = {
                                //optional
                                ListeUtils.addToListaDup(fragment, 2, 3, idDaAgg)
                            }
                        }
                        item {
                            labelRes = R.string.canto_vino
                            callback = {
                                //optional
                                ListeUtils.addToListaDup(fragment, 2, 4, idDaAgg)
                            }
                        }
                        item {
                            labelRes = R.string.canto_fine
                            callback = {
                                //optional
                                addToListaNoDup(2, 5, fragment, dialogTag2)
                            }
                        }
                    }
                    3 -> {
                        for (i in 0 until listePersonalizzate!![idLista].lista!!.numPosizioni) {
                            item {
                                label = listePersonalizzate[idLista].lista!!.getNomePosizione(i)
                                callback = {
                                    //optional
                                    idListaClick = idLista
                                    idPosizioneClick = i

                                    if (listePersonalizzate[idListaClick]
                                                    .lista!!
                                                    .getCantoPosizione(idPosizioneClick) == "") {
                                        listePersonalizzate[idListaClick]
                                                .lista!!
                                                .addCanto(
                                                        (idDaAgg).toString(), idPosizioneClick)
                                        ListeUtils.updateListaPersonalizzata(fragment, listePersonalizzate[idListaClick])
                                    } else {
                                        if (listePersonalizzate[idListaClick]
                                                        .lista!!
                                                        .getCantoPosizione(idPosizioneClick) == (idDaAgg).toString()) {
                                            Snackbar.make(fragment.view!!, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
                                        } else {
                                            ListeUtils.manageReplaceDialog(fragment, Integer.parseInt(
                                                    listePersonalizzate[idListaClick]
                                                            .lista!!
                                                            .getCantoPosizione(idPosizioneClick)), dialogTag)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        popupMenu.show(fragment.context!!, mView)
    }

    private fun addToListaNoDup(idLista: Int, listPosition: Int, fragment: Fragment, dialogTag: String) {
        idListaDaAgg = idLista
        posizioneDaAgg = listPosition
        ListeUtils.addToListaNoDup(fragment, idLista, listPosition, idDaAgg, dialogTag)
    }
}
