package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

open class GenericIndexViewModel(application: Application) : DialogManagerViewModel(application) {

    var idDaAgg: Int = 0
    var idListaDaAgg: Int = 0
    var posizioneDaAgg: Int = 0
    var idListaClick: Int = 0
    var idPosizioneClick: Int = 0

    fun addToListaNoDup(
        idLista: Int,
        listPosition: Int,
        fragment: Fragment,
        tag: String
    ) {
        idListaDaAgg = idLista
        posizioneDaAgg = listPosition
        dialogTag = tag
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

                    content.value = sb.toString()
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

            dialogTag = replaceTag
            content.value = sb.toString()
            showAlertDialog.postValue(true)
        }
    }

}
