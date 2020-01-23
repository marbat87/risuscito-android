package it.cammino.risuscito.viewmodels

import android.app.Application
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.InsertItem
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.utils.ListeUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

open class GenericIndexViewModel(application: Application) : AndroidViewModel(application) {

    var idDaAgg: Int = 0
    var idListaDaAgg: Int = 0
    var posizioneDaAgg: Int = 0
    var idListaClick: Int = 0
    var idPosizioneClick: Int = 0
    var titoli: List<SimpleItem> = ArrayList()
    var titoliInsert: List<InsertItem> = ArrayList()

//    fun popupMenu(fragment: Fragment, view: View, dialogTag: String, dialogTag2: String, listePersonalizzate: List<ListaPers>?) {
//        val popupMenu = popupMenu {
//            dropdownGravity = Gravity.END
//            section {
//                title = fragment.getString(R.string.select_canto) + ":"
//                item {
//                    labelRes = R.string.title_activity_favourites
//                    callback = {
//                        ListeUtils.addToFavorites(fragment, idDaAgg)
//                    }
//                }
//                item {
//                    labelRes = R.string.title_activity_canti_parola
//                    hasNestedItems = true
//                    callback = {
//                        subPopupMenuPredefinite(fragment.getString(R.string.title_activity_canti_parola), 1, view, fragment, dialogTag2)
//                    }
//                }
//                item {
//                    labelRes = R.string.title_activity_canti_eucarestia
//                    hasNestedItems = true
//                    callback = {
//                        subPopupMenuPredefinite(fragment.getString(R.string.title_activity_canti_eucarestia), 2, view, fragment, dialogTag2)
//                    }
//                }
//                listePersonalizzate?.let {
//                    for (i in it.indices) {
//                        item {
//                            label = it[i].lista?.name
//                            hasNestedItems = true
//                            callback = {
//                                subPopupMenuPersonalizzate(it[i].lista?.name, view, i, fragment, dialogTag, it)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        popupMenu.show(fragment.requireContext(), view)
//    }

    fun popupMenu(fragment: Fragment, view: View, dialogTag: String, dialogTag2: String, listePersonalizzate: List<ListaPers>?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(fragment.context)
        val wrapper = ContextThemeWrapper(fragment.requireContext(), R.style.Widget_MaterialComponents_PopupMenu_Risuscito)
        val mPopupMenu = if (LUtils.hasK()) PopupMenu(wrapper, view, Gravity.END) else PopupMenu(wrapper, view)

        listePersonalizzate?.let {
            for (i in it.indices) {
                val subMenu = mPopupMenu.menu.addSubMenu(
                        ID_FITTIZIO, Menu.NONE, 10 + i, it[i].lista!!.name)
                for (k in 0 until it[i].lista!!.numPosizioni) {
                    subMenu.add(100 + i, k, k, it[i].lista!!.getNomePosizione(k))
                }
            }
        }
        mPopupMenu.inflate(R.menu.add_to_menu)

        mPopupMenu.menu.findItem(R.id.add_to_p_pace).isVisible = pref.getBoolean(Utility.SHOW_PACE, false)
        mPopupMenu.menu.findItem(R.id.add_to_e_seconda).isVisible = pref.getBoolean(Utility.SHOW_SECONDA, false)
        mPopupMenu.menu.findItem(R.id.add_to_e_offertorio).isVisible = pref.getBoolean(Utility.SHOW_OFFERTORIO, false)
        mPopupMenu.menu.findItem(R.id.add_to_e_santo).isVisible = pref.getBoolean(Utility.SHOW_SANTO, false)

        mPopupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add_to_favorites -> {
                    ListeUtils.addToFavorites(fragment, idDaAgg)
                    true
                }
                R.id.add_to_p_iniziale -> {
                    addToListaNoDup(1, 1, fragment, dialogTag2)
                    true
                }
                R.id.add_to_p_prima -> {
                    addToListaNoDup(1, 2, fragment, dialogTag2)
                    true
                }
                R.id.add_to_p_seconda -> {
                    addToListaNoDup(1, 3, fragment, dialogTag2)
                    true
                }
                R.id.add_to_p_terza -> {
                    addToListaNoDup(1, 4, fragment, dialogTag2)
                    true
                }
                R.id.add_to_p_pace -> {
                    addToListaNoDup(1, 6, fragment, dialogTag2)
                    true
                }
                R.id.add_to_p_fine -> {
                    addToListaNoDup(1, 5, fragment, dialogTag2)
                    true
                }
                R.id.add_to_e_iniziale -> {
                    addToListaNoDup(2, 1, fragment, dialogTag2)
                    true
                }
                R.id.add_to_e_seconda -> {
                    addToListaNoDup(2, 6, fragment, dialogTag2)
                    true
                }
                R.id.add_to_e_pace -> {
                    addToListaNoDup(2, 2, fragment, dialogTag2)
                    true
                }
                R.id.add_to_e_offertorio -> {
                    addToListaNoDup(2, 8, fragment, dialogTag2)
                    true
                }
                R.id.add_to_e_santo -> {
                    addToListaNoDup(2, 7, fragment, dialogTag2)
                    true
                }
                R.id.add_to_e_pane -> {
                    ListeUtils.addToListaDup(fragment, 2, 3, idDaAgg)
                    true
                }
                R.id.add_to_e_vino -> {
                    ListeUtils.addToListaDup(fragment, 2, 4, idDaAgg)
                    true
                }
                R.id.add_to_e_fine -> {
                    addToListaNoDup(2, 5, fragment, dialogTag2)
                    true
                }
                else -> {
                    idListaClick = it.groupId
                    idPosizioneClick = it.itemId
                    listePersonalizzate?.let { liste ->
                        if (idListaClick != ID_FITTIZIO && idListaClick >= 100) {
                            idListaClick -= 100
                            if (liste[idListaClick]
                                            .lista
                                            ?.getCantoPosizione(idPosizioneClick)?.isEmpty() == true) {
                                liste[idListaClick]
                                        .lista
                                        ?.addCanto(
                                                (idDaAgg).toString(), idPosizioneClick)
                                ListeUtils.updateListaPersonalizzata(fragment, liste[idListaClick])
                            } else {
                                if (liste[idListaClick]
                                                .lista
                                                ?.getCantoPosizione(idPosizioneClick) == (idDaAgg).toString()) {
                                    Snackbar.make(fragment.requireActivity().main_content, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
                                } else {
                                    ListeUtils.manageReplaceDialog(fragment, Integer.parseInt(
                                            liste[idListaClick]
                                                    .lista
                                                    ?.getCantoPosizione(idPosizioneClick)
                                                    ?: "0"), dialogTag)
                                }
                            }
                            true
                        } else
                            false
                    } ?: false
                }
            }
        }
        mPopupMenu.show()
    }

//    private fun subPopupMenuPredefinite(menuTitle: String?, tipoLista: Int, mView: View, fragment: Fragment, dialogTag2: String) {
//        val pref = PreferenceManager.getDefaultSharedPreferences(fragment.context)
//        val popupMenu = popupMenu {
//            dropdownGravity = Gravity.END
//            section {
//                title = menuTitle
//                when (tipoLista) {
//                    1 -> {
//                        item {
//                            labelRes = R.string.canto_iniziale
//                            callback = {
//                                //optional
//                                addToListaNoDup(1, 1, fragment, dialogTag2)
//                            }
//                        }
//                        item {
//                            labelRes = R.string.prima_lettura
//                            callback = {
//                                //optional
//                                addToListaNoDup(1, 2, fragment, dialogTag2)
//                            }
//                        }
//                        item {
//                            labelRes = R.string.seconda_lettura
//                            callback = {
//                                //optional
//                                addToListaNoDup(1, 3, fragment, dialogTag2)
//                            }
//                        }
//                        item {
//                            labelRes = R.string.terza_lettura
//                            callback = {
//                                //optional
//                                addToListaNoDup(1, 4, fragment, dialogTag2)
//                            }
//                        }
//                        if (pref.getBoolean(Utility.SHOW_PACE, false)) {
//                            item {
//                                labelRes = R.string.canto_pace
//                                callback = {
//                                    //optional
//                                    addToListaNoDup(1, 6, fragment, dialogTag2)
//                                }
//                            }
//                        }
//                        item {
//                            labelRes = R.string.canto_fine
//                            callback = {
//                                //optional
//                                addToListaNoDup(1, 5, fragment, dialogTag2)
//                            }
//                        }
//                    }
//                    2 -> {
//                        item {
//                            labelRes = R.string.canto_iniziale
//                            callback = {
//                                //optional
//                                addToListaNoDup(2, 1, fragment, dialogTag2)
//                            }
//                        }
//                        if (pref.getBoolean(Utility.SHOW_SECONDA, false)) {
//                            item {
//                                labelRes = R.string.seconda_lettura
//                                callback = {
//                                    //optional
//                                    addToListaNoDup(2, 6, fragment, dialogTag2)
//                                }
//                            }
//                        }
//                        item {
//                            labelRes = R.string.canto_pace
//                            callback = {
//                                //optional
//                                addToListaNoDup(2, 2, fragment, dialogTag2)
//                            }
//                        }
//                        if (pref.getBoolean(Utility.SHOW_OFFERTORIO, false)) {
//                            item {
//                                labelRes = R.string.canto_offertorio
//                                callback = {
//                                    //optional
//                                    addToListaNoDup(2, 8, fragment, dialogTag2)
//                                }
//                            }
//                        }
//                        if (pref.getBoolean(Utility.SHOW_SANTO, false)) {
//                            item {
//                                labelRes = R.string.santo
//                                callback = {
//                                    //optional
//                                    addToListaNoDup(2, 7, fragment, dialogTag2)
//                                }
//                            }
//                        }
//                        item {
//                            labelRes = R.string.canto_pane
//                            callback = {
//                                //optional
//                                ListeUtils.addToListaDup(fragment, 2, 3, idDaAgg)
//                            }
//                        }
//                        item {
//                            labelRes = R.string.canto_vino
//                            callback = {
//                                //optional
//                                ListeUtils.addToListaDup(fragment, 2, 4, idDaAgg)
//                            }
//                        }
//                        item {
//                            labelRes = R.string.canto_fine
//                            callback = {
//                                //optional
//                                addToListaNoDup(2, 5, fragment, dialogTag2)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        popupMenu.show(fragment.requireContext(), mView)
//    }

//    private fun subPopupMenuPersonalizzate(menuTitle: String?, mView: View, idLista: Int, fragment: Fragment, dialogTag: String, listePersonalizzate: List<ListaPers>) {
//        val popupMenu = popupMenu {
//            dropdownGravity = Gravity.END
//            section {
//                title = menuTitle
//                listePersonalizzate[idLista].lista?.let { list ->
//                    for (i in 0 until list.numPosizioni) {
//                        item {
//                            label = list.getNomePosizione(i)
//                            callback = {
//                                //optional
//                                idListaClick = idLista
//                                idPosizioneClick = i
//
//                                if (listePersonalizzate[idListaClick]
//                                                .lista?.getCantoPosizione(idPosizioneClick) == "") {
//                                    listePersonalizzate[idListaClick]
//                                            .lista?.addCanto(
//                                            (idDaAgg).toString(), idPosizioneClick)
//                                    ListeUtils.updateListaPersonalizzata(fragment, listePersonalizzate[idListaClick])
//                                } else {
//                                    if (listePersonalizzate[idListaClick]
//                                                    .lista?.getCantoPosizione(idPosizioneClick) == (idDaAgg).toString()) {
//                                        Snackbar.make(fragment.requireActivity().main_content, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
//                                    } else {
//                                        ListeUtils.manageReplaceDialog(fragment, Integer.parseInt(
//                                                listePersonalizzate[idListaClick]
//                                                        .lista?.getCantoPosizione(idPosizioneClick)
//                                                        ?: "0"), dialogTag)
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        popupMenu.show(fragment.requireContext(), mView)
//    }

    private fun addToListaNoDup(idLista: Int, listPosition: Int, fragment: Fragment, dialogTag: String) {
        idListaDaAgg = idLista
        posizioneDaAgg = listPosition
        ListeUtils.addToListaNoDup(fragment, idLista, listPosition, idDaAgg, dialogTag)
    }

    companion object {
        private const val ID_FITTIZIO = 99999999
    }
}
