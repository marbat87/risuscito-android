package it.cammino.risuscito.viewmodels

import android.app.Application
import android.view.Gravity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.InsertItem
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.ListeUtils
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

open class GenericIndexViewModel(application: Application) : AndroidViewModel(application) {

    var idDaAgg: Int = 0
    var idListaDaAgg: Int = 0
    var posizioneDaAgg: Int = 0
    var idListaClick: Int = 0
    var idPosizioneClick: Int = 0
    var titoliInsert: List<InsertItem> = ArrayList()


    //dialog properties
    var shownDialogTag: String = StringUtils.EMPTY
    var alertDialogContent = ""
    val showAlertDialog = MutableLiveData(false)

    fun popupMenu(
        fragment: Fragment,
        view: View,
        dialogTag: String,
        dialogTag2: String,
        listePersonalizzate: List<ListaPers>?
    ) {
        val pref = PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())
        val mPopupMenu = PopupMenu(fragment.requireContext(), view, Gravity.END)

        listePersonalizzate?.let {
            for (i in it.indices) {
                val subMenu = mPopupMenu.menu.addSubMenu(
                    ID_FITTIZIO, Menu.NONE, 10 + i, it[i].lista!!.name
                )
                for (k in 0 until it[i].lista!!.numPosizioni) {
                    subMenu.add(100 + i, k, k, it[i].lista!!.getNomePosizione(k))
                }
            }
        }
        mPopupMenu.inflate(R.menu.add_to_menu)

        mPopupMenu.menu.findItem(R.id.add_to_p_pace).isVisible =
            pref.getBoolean(Utility.SHOW_PACE, false)
        mPopupMenu.menu.findItem(R.id.add_to_e_seconda).isVisible =
            pref.getBoolean(Utility.SHOW_SECONDA, false)
        mPopupMenu.menu.findItem(R.id.add_to_e_offertorio).isVisible =
            pref.getBoolean(Utility.SHOW_OFFERTORIO, false)
        mPopupMenu.menu.findItem(R.id.add_to_e_santo).isVisible =
            pref.getBoolean(Utility.SHOW_SANTO, false)

        mPopupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add_to_favorites -> {
                    ListeUtils.addToFavorites(fragment, idDaAgg, true)
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
                                    ?.getCantoPosizione(idPosizioneClick)?.isEmpty() == true
                            ) {
                                liste[idListaClick]
                                    .lista
                                    ?.addCanto(
                                        (idDaAgg).toString(), idPosizioneClick
                                    )
                                ListeUtils.updateListaPersonalizzata(fragment, liste[idListaClick])
                            } else {
                                if (liste[idListaClick]
                                        .lista
                                        ?.getCantoPosizione(idPosizioneClick) == (idDaAgg).toString()
                                ) {
                                    (fragment as? SnackBarFragment)?.showSnackBar(message = fragment.getString(R.string.present_yet))
                                } else {
                                    manageReplaceDialog(
                                        fragment, Integer.parseInt(
                                            liste[idListaClick]
                                                .lista
                                                ?.getCantoPosizione(idPosizioneClick)
                                                ?: "0"
                                        ), dialogTag
                                    )
                                }
                            }
                            true
                        } else
                            false
                    } == true
                }
            }
        }
        mPopupMenu.show()
    }

    fun addToListaNoDup(
        idLista: Int,
        listPosition: Int,
        fragment: Fragment,
        dialogTag: String
    ) {
        idListaDaAgg = idLista
        posizioneDaAgg = listPosition
        shownDialogTag = dialogTag
        fragment.lifecycleScope.launch {
            val db = RisuscitoDatabase.getInstance(fragment.requireContext())
            val listeDao = db.customListDao()
            val cantoDao = db.cantoDao()
            val idPresente =
                withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                    listeDao.getIdByPosition(
                        idLista,
                        listPosition
                    )
                }
            idPresente?.let {
                if (idDaAgg == it) {
                    (fragment as? SnackBarFragment)?.showSnackBar(message = fragment.getString(R.string.present_yet))
                    return@launch
                } else {
                    val titoloPresente =
                        withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                            fragment.resources.getString(
                                Utility.getResId(
                                    cantoDao.getCantoById(it)?.titolo.orEmpty(),
                                    R.string::class.java
                                )
                            )
                        }

                    val sb = StringBuilder()
                    sb.append(fragment.getString(R.string.dialog_present_yet))
                    sb.append(" ")
                    sb.append(titoloPresente)
                    sb.append(".")
                    sb.append(System.lineSeparator())
                    sb.append(fragment.getString(R.string.dialog_wonna_replace))

                    alertDialogContent = sb.toString()
                    showAlertDialog.postValue(true)

                    return@launch
                }
            }

            val position = CustomList()
            position.id = idLista
            position.position = listPosition
            position.idCanto = idDaAgg
            position.timestamp = Date(System.currentTimeMillis())
            withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                listeDao.insertPosition(
                    position
                )
            }
            (fragment as? SnackBarFragment)?.showSnackBar(message = fragment.getString(R.string.list_added))
        }
    }

    fun manageReplaceDialog(fragment: Fragment, idCanto: Int, replaceTag: String) {
        fragment.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).cantoDao()
            val existingTitle =
                withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                    mDao.getCantoById(idCanto)?.titolo.orEmpty()
                }

            val sb = StringBuilder()
            sb.append(fragment.getString(R.string.dialog_present_yet))
            sb.append(" ")
            sb.append(
                fragment.resources.getString(
                    Utility.getResId(
                        existingTitle,
                        R.string::class.java
                    )
                )
            )
            sb.append(".")
            sb.append(System.lineSeparator())
            sb.append(fragment.getString(R.string.dialog_wonna_replace))

            shownDialogTag = replaceTag
            alertDialogContent = sb.toString()
            showAlertDialog.postValue(true)
        }
    }

    companion object {
        private const val ID_FITTIZIO = 99999999
    }
}
