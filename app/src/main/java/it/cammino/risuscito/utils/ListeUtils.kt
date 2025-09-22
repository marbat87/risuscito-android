package it.cammino.risuscito.utils

import android.database.SQLException
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.ui.fragment.CustomListsFragment
import it.cammino.risuscito.ui.interfaces.SnackBarFragment
import it.cammino.risuscito.utils.extension.finishAfterTransitionWrapper
import it.cammino.risuscito.utils.extension.systemLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

    fun removePositionWithUndo(
        fragment: Fragment,
        idLista: Int,
        posizioneDaCanc: Int,
        idDaCanc: Int,
        timestampDaCanc: String,
        notaDaCanc: String
    ) {
        fragment.lifecycleScope.launch {
            val positionToDelete = CustomList()
            positionToDelete.id = idLista
            positionToDelete.position = posizioneDaCanc
            positionToDelete.idCanto = idDaCanc
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
            withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.deletePosition(
                    positionToDelete
                )
            }
            Snackbar.make(
                fragment.requireActivity().findViewById(android.R.id.content),
                R.string.song_removed,
                Snackbar.LENGTH_LONG
            )
                .setAction(
                    fragment.getString(R.string.cancel)
                        .uppercase(fragment.systemLocale)
                ) {
                    val positionToInsert = CustomList()
                    positionToInsert.id = idLista
                    positionToInsert.position = posizioneDaCanc
                    positionToInsert.idCanto = idDaCanc
                    positionToInsert.timestamp = Date(java.lang.Long.parseLong(timestampDaCanc))
                    positionToInsert.notaPosizione = notaDaCanc
                    val mDao2 =
                        RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
                    fragment.lifecycleScope.launch(Dispatchers.IO) {
                        mDao2.insertPosition(
                            positionToInsert
                        )
                    }
                }
                .show()
        }
    }

//    fun manageReplaceDialog(fragment: Fragment, idCanto: Int, replaceTag: String) {
//        fragment.lifecycleScope.launch {
//            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).cantoDao()
//            val existingTitle =
//                withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
//                    mDao.getCantoById(idCanto)?.titolo.orEmpty()
//                }
//
//            val sb = StringBuilder()
//            sb.append(fragment.getString(R.string.dialog_present_yet))
//            sb.append(" ")
//            sb.append(
//                fragment.resources.getString(
//                    Utility.getResId(
//                        existingTitle,
//                        R.string::class.java
//                    )
//                )
//            )
//            sb.append(".")
//            sb.append(System.lineSeparator())
//            sb.append(fragment.getString(R.string.dialog_wonna_replace))
//
//            SimpleDialogFragment.show(
//                SimpleDialogFragment.Builder(
//                    replaceTag
//                )
//                    .title(R.string.dialog_replace_title)
//                    .icon(R.drawable.find_replace_24px)
//                    .content(sb.toString())
//                    .positiveButton(R.string.replace_confirm)
//                    .negativeButton(R.string.cancel),
//                fragment.requireActivity().supportFragmentManager
//            )
//        }
//    }

    fun scambioConVuoto(
        fragment: Fragment,
        idLista: Int,
        posizioneDaCanc: Int,
        idDaCanc: Int,
        notaDaCanc: String,
        newPosition: Int
    ) {
        fragment.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
            val existingTitle =
                withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                    mDao.checkExistsPosition(idLista, newPosition, idDaCanc)
                }
            if (existingTitle > 0)
                Snackbar.make(
                    fragment.requireActivity().findViewById(android.R.id.content),
                    R.string.present_yet,
                    Snackbar.LENGTH_SHORT
                ).show()
            else {
                withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                    val positionToDelete =
                        mDao.getPositionSpecific(idLista, posizioneDaCanc, idDaCanc)
                    mDao.deletePosition(positionToDelete)
                    val positionToInsert = CustomList()
                    positionToInsert.id = idLista
                    positionToInsert.position = newPosition
                    positionToInsert.idCanto = idDaCanc
                    positionToInsert.timestamp = Date(System.currentTimeMillis())
                    positionToInsert.notaPosizione = notaDaCanc
                    mDao.insertPosition(positionToInsert)
                }
                Snackbar.make(
                    fragment.requireActivity().findViewById(android.R.id.content),
                    R.string.switch_done,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun scambioCanto(
        fragment: Fragment,
        idLista: Int,
        posizioneDaCanc: Int,
        idDaCanc: Int,
        notaDaCanc: String,
        newPosition: Int,
        newId: Int,
        newNota: String
    ) {
        fragment.lifecycleScope.launch {
            if (newId != idDaCanc || posizioneDaCanc != newPosition) {
                val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
                val existingOldTitle = fragment.lifecycleScope.async(Dispatchers.IO) {
                    mDao.checkExistsPosition(idLista, newPosition, idDaCanc)
                }
                val existingNewTitle = fragment.lifecycleScope.async(Dispatchers.IO) {
                    mDao.checkExistsPosition(idLista, posizioneDaCanc, newId)
                }
                if ((existingOldTitle.await() > 0
                            || existingNewTitle.await() > 0)
                    && newPosition != posizioneDaCanc
                ) {
                    Snackbar.make(
                        fragment.requireActivity().findViewById(android.R.id.content),
                        R.string.present_yet,
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    val positionToDelete =
                        withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                            mDao.getPositionSpecific(
                                idLista,
                                newPosition,
                                newId
                            )
                        }
                    withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                        mDao.deletePosition(
                            positionToDelete
                        )
                    }

                    val positionToInsert = CustomList()
                    positionToInsert.id = idLista
                    positionToInsert.position = newPosition
                    positionToInsert.idCanto = idDaCanc
                    positionToInsert.timestamp = positionToDelete.timestamp
                    positionToInsert.notaPosizione = notaDaCanc

                    withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                        mDao.updatePositionNoTimestamp(
                            newId,
                            newNota,
                            idLista,
                            posizioneDaCanc,
                            idDaCanc
                        )
                    }
                    withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                        mDao.insertPosition(
                            positionToInsert
                        )
                    }
                    Snackbar.make(
                        fragment.requireActivity().findViewById(android.R.id.content),
                        R.string.switch_done,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else
                Snackbar.make(
                    fragment.requireActivity().findViewById(android.R.id.content),
                    R.string.switch_impossible,
                    Snackbar.LENGTH_SHORT
                ).show()
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
