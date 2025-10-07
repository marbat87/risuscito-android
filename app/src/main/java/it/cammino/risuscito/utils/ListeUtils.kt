package it.cammino.risuscito.utils

import android.database.SQLException
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.ui.fragment.CustomListsFragment
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

object ListeUtils {

    fun addToListaDup(fragment: Fragment, idLista: Int, listPosition: Int, idDaAgg: Int) {
        fragment.lifecycleScope.launch {
            try {
                val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
                val position = CustomList()
                position.id = idLista
                position.position = listPosition
                position.idCanto = idDaAgg
                position.timestamp = Date(System.currentTimeMillis())
                withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                    mDao.insertPosition(
                        position
                    )
                }
            } catch (_: SQLException) {
                (fragment as? SnackBarFragment)?.showSnackBar(message = fragment.getString(R.string.present_yet))
            }
            (fragment as? SnackBarFragment)?.showSnackBar(message = fragment.getString(R.string.list_added))
        }
    }

    // aggiunge il canto premuto ai preferiti
    fun addToFavorites(fragment: Fragment, idDaAgg: Int, showSnackbar: Boolean) {
        fragment.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).favoritesDao()
            withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.setFavorite(
                    idDaAgg
                )
            }
            if (showSnackbar)
                (fragment as? SnackBarFragment)?.showSnackBar(message = fragment.getString(R.string.favorite_added))
        }
    }

    fun updateListaPersonalizzata(fragment: Fragment, listaUpd: ListaPers) {
        fragment.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).listePersDao()
            withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.updateLista(
                    listaUpd
                )
            }
            (fragment as? SnackBarFragment)?.showSnackBar(message = fragment.getString(R.string.list_added))
        }
    }

    fun updatePosizione(fragment: Fragment, idDaAgg: Int, idListaDaAgg: Int, posizioneDaAgg: Int) {
        fragment.lifecycleScope.launch {
            val mCustomListDao =
                RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
            withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                mCustomListDao.updatePositionNoTimestamp(
                    idDaAgg,
                    idListaDaAgg,
                    posizioneDaAgg
                )
            }
            (fragment as? SnackBarFragment)?.showSnackBar(message = fragment.getString(R.string.list_added))
        }
    }

    fun addToListaDupAndFinish(
        activity: AppCompatActivity,
        idLista: Int,
        listPosition: Int,
        idDaAgg: Int
    ) {
        activity.lifecycleScope.launch {
            try {
                val mDao = RisuscitoDatabase.getInstance(activity).customListDao()
                val position = CustomList()
                position.id = idLista
                position.position = listPosition
                position.idCanto = idDaAgg
                position.timestamp = Date(System.currentTimeMillis())
                withContext(activity.lifecycleScope.coroutineContext + Dispatchers.IO) {
                    mDao.insertPosition(
                        position
                    )
                }
            } catch (e: SQLException) {
                Firebase.crashlytics.recordException(e)
                activity.setResult(CustomListsFragment.RESULT_KO)
                activity.finishAfterTransitionWrapper()
            }
            activity.setResult(CustomListsFragment.RESULT_OK)
            activity.finishAfterTransitionWrapper()
        }
    }

    fun updateListaPersonalizzataAndFinish(
        activity: AppCompatActivity,
        idLista: Int,
        idCanto: Int,
        listPosition: Int
    ) {
        activity.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(activity).listePersDao()
            val listaPers = withContext(activity.lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.getListById(idLista)
            }
            if (listaPers?.lista != null) {
                listaPers.lista?.addCanto(idCanto.toString(), listPosition)
                withContext(activity.lifecycleScope.coroutineContext + Dispatchers.IO) {
                    mDao.updateLista(
                        listaPers
                    )
                }
                activity.setResult(CustomListsFragment.RESULT_OK)
                activity.finishAfterTransitionWrapper()
                return@launch
            }
            activity.setResult(CustomListsFragment.RESULT_CANCELED)
            activity.finishAfterTransitionWrapper()
            return@launch
        }
    }

}
