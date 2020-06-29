package it.cammino.risuscito.utils

import android.database.SQLException
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.android.material.snackbar.Snackbar
import it.cammino.risuscito.CustomLists
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Cronologia
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import it.cammino.risuscito.items.SimpleHistoryItem
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

object ListeUtils {

//    private const val CANTO_PRESENTE = "CANTO_PRESENTE"

    fun addToListaDup(fragment: Fragment, idLista: Int, listPosition: Int, idDaAgg: Int) {
//        AddToListaDupTask(fragment, idLista, listPosition, idDaAgg).execute()
        fragment.lifecycleScope.launch {
            try {
                val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
                val position = CustomList()
                position.id = idLista
                position.position = listPosition
                position.idCanto = idDaAgg
                position.timestamp = Date(System.currentTimeMillis())
                withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.insertPosition(position) }
            } catch (e: SQLException) {
                Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.present_yet, Snackbar.LENGTH_SHORT).show()
            }
            Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.list_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun addToListaNoDup(fragment: Fragment, idLista: Int, listPosition: Int, idDaAgg: Int, replaceTag: String) {
//        AddToListaNoDupTask(fragment, idLista, listPosition, idDaAgg, replaceTag).execute()
        fragment.lifecycleScope.launch {
            val db = RisuscitoDatabase.getInstance(fragment.requireContext())
            val listeDao = db.customListDao()
            val cantoDao = db.cantoDao()
            val idPresente = withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { listeDao.getIdByPosition(idLista, listPosition) }
            idPresente?.let {
                if (idDaAgg == it) {
                    Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.present_yet, Snackbar.LENGTH_SHORT).show()
                    return@launch
                } else {
                    val titoloPresente = withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { fragment.resources.getString(LUtils.getResId(cantoDao.getCantoById(it).titolo, R.string::class.java)) }
                    SimpleDialogFragment.show(SimpleDialogFragment.Builder(
                            fragment.requireActivity() as AppCompatActivity,
                            replaceTag)
                            .title(R.string.dialog_replace_title)
                            .content(
                                    (fragment.getString(R.string.dialog_present_yet)
                                            + " "
                                            + titoloPresente
                                            + fragment.getString(R.string.dialog_wonna_replace)))
                            .positiveButton(R.string.replace_confirm)
                            .negativeButton(R.string.cancel),
                            fragment.requireActivity().supportFragmentManager)
                    return@launch
                }
            }

            val position = CustomList()
            position.id = idLista
            position.position = listPosition
            position.idCanto = idDaAgg
            position.timestamp = Date(System.currentTimeMillis())
            withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { listeDao.insertPosition(position) }
            Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.list_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    // aggiunge il canto premuto ai preferiti
    fun addToFavorites(fragment: Fragment, idDaAgg: Int, showSnackbar: Boolean) {
//        UpdateFavoriteTask(fragment, true, idDaAgg).execute()
        fragment.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).favoritesDao()
            withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.setFavorite(idDaAgg) }
            if (showSnackbar)
                Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.favorite_added, Snackbar.LENGTH_SHORT).show()
        }
    }

//    // aggiunge il canto premuto ai preferiti
//    fun addToFavoritesNoSnackbar(fragment: Fragment, idDaAgg: Int) {
//        UpdateFavoriteTask(fragment, false, idDaAgg).execute()
//    }

    fun removeFavoritesWithUndo(fragment: Fragment, mRemovedItems: Set<SimpleItem>?) {
//        RemoveFavoriteTask(fragment, mRemovedItems).execute()
        fragment.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).favoritesDao()
            mRemovedItems?.let { removedItems ->
                withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                    for (removedItem in removedItems)
                        mDao.removeFavorite(removedItem.id)
                }
                Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), fragment.resources.getQuantityString(R.plurals.favorites_removed, removedItems.size, removedItems.size), Snackbar.LENGTH_SHORT)
                        .setAction(fragment.getString(R.string.cancel).toUpperCase(getSystemLocale(fragment.resources))) {
                            for (removedItem in removedItems)
                                addToFavorites(fragment, removedItem.id, false)
                        }.show()
            }

        }
    }

    fun removeHistoriesWithUndo(fragment: Fragment, mRemovedItems: Set<SimpleHistoryItem>?) {
//        RemoveHistoryTask(fragment, mRemovedItems).execute()
        fragment.lifecycleScope.launch {
            mRemovedItems?.let { removedItems ->
                val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).cronologiaDao()
                for (removedItem in removedItems) {
                    val cronTemp = Cronologia()
                    cronTemp.idCanto = removedItem.id
                    withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.deleteCronologia(cronTemp) }
                }
                Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), fragment.resources.getQuantityString(R.plurals.histories_removed, removedItems.size, removedItems.size), Snackbar.LENGTH_SHORT)
                        .setAction(fragment.getString(R.string.cancel).toUpperCase(getSystemLocale(fragment.resources))) {
                            for (removedItem in removedItems) {
//                                UpdateHistoryTask(fragment, removedItem.id, removedItem.timestamp?.getText(fragment.requireContext()).toString()).execute()
                                val cronTemp = Cronologia()
                                cronTemp.idCanto = removedItem.id
                                cronTemp.ultimaVisita = Date(java.lang.Long.parseLong(removedItem.timestamp?.getText(fragment.requireContext()).toString()))
                                fragment.lifecycleScope.launch(Dispatchers.IO) { mDao.insertCronologia(cronTemp) }
                            }
                        }.show()
            }
        }
    }

    fun updateListaPersonalizzata(fragment: Fragment, listaUpd: ListaPers) {
//        UpdateListaPersonalizzataTask(fragment).execute(listaUpd)
        fragment.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).listePersDao()
            withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.updateLista(listaUpd) }
            Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.list_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun updatePosizione(fragment: Fragment, idDaAgg: Int, idListaDaAgg: Int, posizioneDaAgg: Int) {
//        UpdatePosizioneTask(fragment, idDaAgg, idListaDaAgg, posizioneDaAgg).execute()
        fragment.lifecycleScope.launch {
            val mCustomListDao = RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
            withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { mCustomListDao.updatePositionNoTimestamp(idDaAgg, idListaDaAgg, posizioneDaAgg) }
            Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.list_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun removePositionWithUndo(fragment: Fragment, idLista: Int, posizioneDaCanc: Int, idDaCanc: Int, timestampDaCanc: String) {
//        RemovePosizioneTask(fragment, idLista, posizioneDaCanc, idDaCanc, timestampDaCanc).execute()
        fragment.lifecycleScope.launch {
            val positionToDelete = CustomList()
            positionToDelete.id = idLista
            positionToDelete.position = posizioneDaCanc
            positionToDelete.idCanto = idDaCanc
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
            withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.deletePosition(positionToDelete) }
            Snackbar.make(
                    fragment.requireActivity().findViewById(R.id.main_content),
                    R.string.song_removed,
                    Snackbar.LENGTH_LONG)
                    .setAction(
                            fragment.getString(R.string.cancel).toUpperCase(getSystemLocale(fragment.resources))
                    ) {
//                        ReinsertPositionTask(fragment, idLista, posizioneDaCanc, idDaCanc, timestampDaCanc).execute()
                        val positionToInsert = CustomList()
                        positionToInsert.id = idLista
                        positionToInsert.position = posizioneDaCanc
                        positionToInsert.idCanto = idDaCanc
                        positionToInsert.timestamp = Date(java.lang.Long.parseLong(timestampDaCanc))
                        val mDao2 = RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
                        fragment.lifecycleScope.launch(Dispatchers.IO) { mDao2.insertPosition(positionToInsert) }
                    }
                    .show()
        }
    }

    fun manageReplaceDialog(fragment: Fragment, idCanto: Int, replaceTag: String) {
//        ManageReplaceDialogTask(fragment, idCanto, replaceTag).execute()
        fragment.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).cantoDao()
            val existingTitle = withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.getCantoById(idCanto).titolo ?: ""
            }
            SimpleDialogFragment.show(SimpleDialogFragment.Builder(
                    fragment.requireActivity() as AppCompatActivity,
                    replaceTag)
                    .title(R.string.dialog_replace_title)
                    .content(
                            (fragment.getString(R.string.dialog_present_yet)
                                    + " "
                                    + fragment.resources.getString(LUtils.getResId(existingTitle, R.string::class.java))
                                    + fragment.getString(R.string.dialog_wonna_replace)))
                    .positiveButton(R.string.replace_confirm)
                    .negativeButton(R.string.cancel),
                    fragment.requireActivity().supportFragmentManager)
        }
    }

    fun scambioConVuoto(fragment: Fragment, idLista: Int, posizioneDaCanc: Int, idDaCanc: Int, newPosition: Int) {
//        ScambioConVuotoTask(fragment, idLista, posizioneDaCanc, idDaCanc, newPosition).execute()
        fragment.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).customListDao()
            val existingTitle = withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                mDao.checkExistsPosition(idLista, newPosition, idDaCanc)
            }
            if (existingTitle > 0)
                Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.present_yet, Snackbar.LENGTH_SHORT).show()
            else {
                withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) {
                    val positionToDelete = mDao.getPositionSpecific(idLista, posizioneDaCanc, idDaCanc)
                    mDao.deletePosition(positionToDelete)
                    val positionToInsert = CustomList()
                    positionToInsert.id = idLista
                    positionToInsert.position = newPosition
                    positionToInsert.idCanto = idDaCanc
                    positionToInsert.timestamp = Date(System.currentTimeMillis())
                    mDao.insertPosition(positionToInsert)
                }
                Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.switch_done, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    fun scambioCanto(fragment: Fragment, idLista: Int, posizioneDaCanc: Int, idDaCanc: Int, newPosition: Int, newId: Int) {
//        ScambioCantoTask(fragment, idLista, posizioneDaCanc, idDaCanc, newPosition, newId).execute()
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
                        && newPosition != posizioneDaCanc) {
                    Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.present_yet, Snackbar.LENGTH_SHORT).show()
                } else {
                    val positionToDelete = withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.getPositionSpecific(idLista, newPosition, newId) }
                    withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.deletePosition(positionToDelete) }

                    val positionToInsert = CustomList()
                    positionToInsert.id = idLista
                    positionToInsert.position = newPosition
                    positionToInsert.idCanto = idDaCanc
                    positionToInsert.timestamp = positionToDelete.timestamp

                    withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.updatePositionNoTimestamp(newId, idLista, posizioneDaCanc, idDaCanc) }
                    withContext(fragment.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.insertPosition(positionToInsert) }
                    Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.switch_done, Snackbar.LENGTH_SHORT).show()
                }
            } else
                Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), R.string.switch_impossible, Snackbar.LENGTH_SHORT).show()
        }
    }

//    fun cleanList(context: Context, idLista: Int) {
//        ioThread { RisuscitoDatabase.getInstance(context).customListDao().deleteListById(idLista) }
//    }

    fun addToListaDupAndFinish(activity: AppCompatActivity, idLista: Int, listPosition: Int, idDaAgg: Int) {
//        AddToListaDupTaskWithFinish(activity, idLista, listPosition, idDaAgg).execute()
        activity.lifecycleScope.launch {
            try {
                val mDao = RisuscitoDatabase.getInstance(activity).customListDao()
                val position = CustomList()
                position.id = idLista
                position.position = listPosition
                position.idCanto = idDaAgg
                position.timestamp = Date(System.currentTimeMillis())
                withContext(activity.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.insertPosition(position) }
            } catch (e: SQLException) {
                activity.setResult(CustomLists.RESULT_KO)
                activity.finish()
                Animatoo.animateShrink(activity)
            }
            activity.setResult(CustomLists.RESULT_OK)
            activity.finish()
            Animatoo.animateShrink(activity)
        }
    }

    fun updateListaPersonalizzataAndFinish(activity: AppCompatActivity, idLista: Int, idCanto: Int, listPosition: Int) {
//        UpdateListaPersonalizzataTaskWithFinish(activity, idLista, idCanto, listPosition).execute()
        activity.lifecycleScope.launch {
            val mDao = RisuscitoDatabase.getInstance(activity).listePersDao()
            val listaPers = withContext(activity.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.getListById(idLista) }
            if (listaPers?.lista != null) {
                listaPers.lista?.addCanto(idCanto.toString(), listPosition)
                withContext(activity.lifecycleScope.coroutineContext + Dispatchers.IO) { mDao.updateLista(listaPers) }
                activity.setResult(CustomLists.RESULT_OK)
                activity.finish()
                Animatoo.animateShrink(activity)
                return@launch
            }
            activity.setResult(CustomLists.RESULT_CANCELED)
            activity.finish()
            Animatoo.animateShrink(activity)
            return@launch
        }
    }

//    private class AddToListaNoDupTask internal constructor(fragment: Fragment, private val idLista: Int, private val listPosition: Int, private val idDaAgg: Int, private val replaceTag: String) : AsyncTask<Void, Void, String>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg params: Void?): String? {
//            fragmentReference.get()?.let { fragment ->
//                val db = RisuscitoDatabase.getInstance(fragment.requireContext())
//                val listeDao = db.customListDao()
//                val cantoDao = db.cantoDao()
//                val idPresente = listeDao.getIdByPosition(idLista, listPosition)
//                idPresente?.let {
//                    return if (idDaAgg == it)
//                        CANTO_PRESENTE
//                    else
//                        fragment.resources.getString(LUtils.getResId(cantoDao.getCantoById(it).titolo, R.string::class.java))
//                }
//
//                val position = CustomList()
//                position.id = idLista
//                position.position = listPosition
//                position.idCanto = idDaAgg
//                position.timestamp = Date(System.currentTimeMillis())
//                listeDao.insertPosition(position)
//            }
//            return ""
//        }
//
//        override fun onPostExecute(titoloPresente: String?) {
//            super.onPostExecute(titoloPresente)
//            fragmentReference.get()?.let {
//                if (titoloPresente != null && titoloPresente == CANTO_PRESENTE) {
//                    Snackbar.make(it.requireActivity().findViewById(R.id.main_content), R.string.present_yet, Snackbar.LENGTH_SHORT).show()
//                } else
//                    if (titoloPresente != null && titoloPresente.isNotEmpty()) {
//                        SimpleDialogFragment.show(SimpleDialogFragment.Builder(
//                                it.requireActivity() as AppCompatActivity,
//                                replaceTag)
//                                .title(R.string.dialog_replace_title)
//                                .content(
//                                        (it.getString(R.string.dialog_present_yet)
//                                                + " "
//                                                + titoloPresente
//                                                + it.getString(R.string.dialog_wonna_replace)))
//                                .positiveButton(R.string.replace_confirm)
//                                .negativeButton(R.string.cancel),
//                                it.requireActivity().supportFragmentManager)
//                    } else
//                        Snackbar.make(it.requireActivity().findViewById(R.id.main_content), R.string.list_added, Snackbar.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private class AddToListaDupTask internal constructor(fragment: Fragment, private val idLista: Int, private val listPosition: Int, private val idDaAgg: Int) : AsyncTask<Void, Void, Boolean>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg params: Void?): Boolean {
//            fragmentReference.get()?.let {
//                try {
//                    val mDao = RisuscitoDatabase.getInstance(it.requireContext()).customListDao()
//                    val position = CustomList()
//                    position.id = idLista
//                    position.position = listPosition
//                    position.idCanto = idDaAgg
//                    position.timestamp = Date(System.currentTimeMillis())
//                    mDao.insertPosition(position)
//                } catch (e: SQLException) {
//                    return false
//                }
//                return true
//            }
//            return false
//        }
//
//        override fun onPostExecute(updated: Boolean) {
//            super.onPostExecute(updated)
//            fragmentReference.get()?.let {
//                if (updated)
//                    Snackbar.make(it.requireActivity().findViewById(R.id.main_content), R.string.list_added, Snackbar.LENGTH_SHORT).show()
//                else
//                    Snackbar.make(it.requireActivity().findViewById(R.id.main_content), R.string.present_yet, Snackbar.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private class UpdateFavoriteTask internal constructor(fragment: Fragment, private val showSnackbar: Boolean, private val idDaAgg: Int) : AsyncTask<Void, Void, Int>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg p0: Void?): Int {
//            fragmentReference.get()?.let {
//                val mDao = RisuscitoDatabase.getInstance(it.requireContext()).favoritesDao()
//                mDao.setFavorite(idDaAgg)
//            }
//            return 0
//        }
//
//        override fun onPostExecute(result: Int) {
//            super.onPostExecute(result)
//            fragmentReference.get()?.let {
//                if (showSnackbar)
//                    Snackbar.make(it.requireActivity().findViewById(R.id.main_content), R.string.favorite_added, Snackbar.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private class RemoveFavoriteTask internal constructor(fragment: Fragment, private val mRemovedItems: Set<SimpleItem>?) : AsyncTask<Void, Void, Int>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg p0: Void?): Int {
//            val apiResult = Pair(fragmentReference.get(), mRemovedItems)
//            apiResult.letCheckNull { fragment, removedItems ->
//                val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).favoritesDao()
//                for (removedItem in removedItems)
//                    mDao.removeFavorite(removedItem.id)
//            }
//            return 0
//        }
//
//        override fun onPostExecute(result: Int) {
//            super.onPostExecute(result)
//            val apiResult = Pair(fragmentReference.get(), mRemovedItems)
//            apiResult.letCheckNull { fragment, removedItems ->
//                Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), fragment.resources.getQuantityString(R.plurals.favorites_removed, removedItems.size, removedItems.size), Snackbar.LENGTH_SHORT)
//                        .setAction(fragment.getString(R.string.cancel).toUpperCase(getSystemLocale(fragment.resources))) {
//                            for (removedItem in removedItems)
////                                UpdateFavoriteTask(fragment, false, removedItem.id).execute()
//                                addToFavorites(fragment, removedItem.id, false)
//                        }.show()
//            }
//        }
//    }

//    private class UpdateListaPersonalizzataTask internal constructor(fragment: Fragment) : AsyncTask<Any, Void, Int>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg p0: Any?): Int {
//            fragmentReference.get()?.let {
//                val mDao = RisuscitoDatabase.getInstance(it.requireContext()).listePersDao()
//                mDao.updateLista(p0[0] as ListaPers)
//            }
//            return 0
//        }
//
//        override fun onPostExecute(result: Int) {
//            super.onPostExecute(result)
//            fragmentReference.get()?.let {
//                Snackbar.make(it.requireActivity().findViewById(R.id.main_content), R.string.list_added, Snackbar.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private class UpdatePosizioneTask internal constructor(fragment: Fragment, private val idDaAgg: Int, private val idListaDaAgg: Int, private val posizioneDaAgg: Int) : AsyncTask<Any, Void, Int>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg params: Any?): Int {
//            fragmentReference.get()?.let {
//                val mCustomListDao = RisuscitoDatabase.getInstance(it.requireContext()).customListDao()
//                mCustomListDao.updatePositionNoTimestamp(idDaAgg, idListaDaAgg, posizioneDaAgg)
//            }
//            return 0
//        }
//
//        override fun onPostExecute(result: Int) {
//            super.onPostExecute(result)
//            fragmentReference.get()?.let {
//                Snackbar.make(it.requireActivity().findViewById(R.id.main_content), R.string.list_added, Snackbar.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private class ManageReplaceDialogTask internal constructor(fragment: Fragment, private val idCanto: Int, private val replaceTag: String) : AsyncTask<Any, Void, String>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg params: Any?): String {
//            fragmentReference.get()?.let {
//                val mDao = RisuscitoDatabase.getInstance(it.requireContext()).cantoDao()
//                return mDao.getCantoById(idCanto).titolo ?: ""
//            }
//            return ""
//        }
//
//        override fun onPostExecute(titoloPresente: String) {
//            super.onPostExecute(titoloPresente)
//            fragmentReference.get()?.let {
//                SimpleDialogFragment.show(SimpleDialogFragment.Builder(
//                        it.requireActivity() as AppCompatActivity,
//                        replaceTag)
//                        .title(R.string.dialog_replace_title)
//                        .content(
//                                (it.getString(R.string.dialog_present_yet)
//                                        + " "
//                                        + it.resources.getString(LUtils.getResId(titoloPresente, R.string::class.java))
//                                        + it.getString(R.string.dialog_wonna_replace)))
//                        .positiveButton(R.string.replace_confirm)
//                        .negativeButton(R.string.cancel),
//                        it.requireActivity().supportFragmentManager)
//            }
//        }
//    }

//    private class ScambioConVuotoTask internal constructor(fragment: Fragment, private val idLista: Int, private val posizioneDaCanc: Int, private val idDaCanc: Int, private val newPosition: Int) : AsyncTask<Any, Void, Boolean>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg params: Any?): Boolean {
//            fragmentReference.get()?.let {
//                val mDao = RisuscitoDatabase.getInstance(it.requireContext()).customListDao()
//
//                return if (mDao.checkExistsPosition(idLista, newPosition, idDaCanc) > 0)
//                    true
//                else {
//                    val positionToDelete = mDao.getPositionSpecific(idLista, posizioneDaCanc, idDaCanc)
//                    mDao.deletePosition(positionToDelete)
//
//                    val positionToInsert = CustomList()
//                    positionToInsert.id = idLista
//                    positionToInsert.position = newPosition
//                    positionToInsert.idCanto = idDaCanc
//                    positionToInsert.timestamp = Date(System.currentTimeMillis())
//                    mDao.insertPosition(positionToInsert)
//                    false
//                }
//            }
//            return true
//        }
//
//        override fun onPostExecute(cantoPresente: Boolean) {
//            super.onPostExecute(cantoPresente)
//            fragmentReference.get()?.let {
//                Snackbar.make(it.requireActivity().findViewById(R.id.main_content), if (cantoPresente) R.string.present_yet else R.string.switch_done, Snackbar.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private class ScambioCantoTask internal constructor(fragment: Fragment, private val idLista: Int, private val posizioneDaCanc: Int, private val idDaCanc: Int, private val newPosition: Int, private val newId: Int) : AsyncTask<Any, Void, Int>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg params: Any?): Int {
//            fragmentReference.get()?.let {
//                return if (newId != idDaCanc || posizioneDaCanc != newPosition) {
//                    val mDao = RisuscitoDatabase.getInstance(it.requireContext()).customListDao()
//                    if ((mDao.checkExistsPosition(idLista, newPosition, idDaCanc) > 0
//                                    || mDao.checkExistsPosition(idLista, posizioneDaCanc, newId) > 0)
//                            && newPosition != posizioneDaCanc) {
//                        1
//                    } else {
//                        val positionToDelete = mDao.getPositionSpecific(idLista, newPosition, newId)
//                        mDao.deletePosition(positionToDelete)
//
//                        val positionToInsert = CustomList()
//                        positionToInsert.id = idLista
//                        positionToInsert.position = newPosition
//                        positionToInsert.idCanto = idDaCanc
//                        positionToInsert.timestamp = positionToDelete.timestamp
//
//                        mDao.updatePositionNoTimestamp(newId, idLista, posizioneDaCanc, idDaCanc)
//                        mDao.insertPosition(positionToInsert)
//                        0
//                    }
//                } else
//                    2
//            }
//            return 2
//        }
//
//        override fun onPostExecute(cantoPresente: Int) {
//            super.onPostExecute(cantoPresente)
//            fragmentReference.get()?.let {
//                when (cantoPresente) {
//                    0 -> Snackbar.make(it.requireActivity().findViewById(R.id.main_content), R.string.switch_done, Snackbar.LENGTH_SHORT).show()
//                    1 -> Snackbar.make(it.requireActivity().findViewById(R.id.main_content), R.string.present_yet, Snackbar.LENGTH_SHORT).show()
//                    2 -> Snackbar.make(it.requireActivity().findViewById(R.id.main_content), R.string.switch_impossible, Snackbar.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

//    private class RemovePosizioneTask internal constructor(fragment: Fragment, private val idLista: Int, private val posizioneDaCanc: Int, private val idDaCanc: Int, private val timestampDaCanc: String) : AsyncTask<Any, Void, Int>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg params: Any?): Int {
//            fragmentReference.get()?.let {
//                val positionToDelete = CustomList()
//                positionToDelete.id = idLista
//                positionToDelete.position = posizioneDaCanc
//                positionToDelete.idCanto = idDaCanc
//                val mDao = RisuscitoDatabase.getInstance(it.requireContext()).customListDao()
//                mDao.deletePosition(positionToDelete)
//            }
//            return 0
//        }
//
//        override fun onPostExecute(result: Int) {
//            super.onPostExecute(result)
//            fragmentReference.get()?.let { fragment ->
//                Snackbar.make(
//                        fragment.requireActivity().findViewById(R.id.main_content),
//                        R.string.song_removed,
//                        Snackbar.LENGTH_LONG)
//                        .setAction(
//                                fragment.getString(R.string.cancel).toUpperCase(getSystemLocale(fragment.resources))
//                        ) {
//                            ReinsertPositionTask(fragment, idLista, posizioneDaCanc, idDaCanc, timestampDaCanc).execute()
//                        }
//                        .show()
//            }
//        }
//    }

//    private class ReinsertPositionTask internal constructor(fragment: Fragment, private val idLista: Int, private val posizioneDaCanc: Int, private val idDaCanc: Int, private val timestampDaCanc: String) : AsyncTask<Any, Void, Int>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg params: Any?): Int {
//            fragmentReference.get()?.let {
//                val positionToInsert = CustomList()
//                positionToInsert.id = idLista
//                positionToInsert.position = posizioneDaCanc
//                positionToInsert.idCanto = idDaCanc
//                positionToInsert.timestamp = Date(java.lang.Long.parseLong(timestampDaCanc))
//                val mDao = RisuscitoDatabase.getInstance(it.requireContext()).customListDao()
//                mDao.insertPosition(positionToInsert)
//            }
//            return 0
//        }
//    }

//    private class RemoveHistoryTask internal constructor(fragment: Fragment, private val mRemovedItems: Set<SimpleHistoryItem>?) : AsyncTask<Void, Void, Int>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg p0: Void?): Int {
//            val apiResult = Pair(fragmentReference.get(), mRemovedItems)
//            apiResult.letCheckNull { fragment, removedItems ->
//                val mDao = RisuscitoDatabase.getInstance(fragment.requireContext()).cronologiaDao()
//                for (removedItem in removedItems) {
//                    val cronTemp = Cronologia()
//                    cronTemp.idCanto = removedItem.id
//                    mDao.deleteCronologia(cronTemp)
//                }
//            }
//            return 0
//        }
//
//        override fun onPostExecute(result: Int) {
//            super.onPostExecute(result)
//            val apiResult = Pair(fragmentReference.get(), mRemovedItems)
//            apiResult.letCheckNull { fragment, removedItems ->
//                Snackbar.make(fragment.requireActivity().findViewById(R.id.main_content), fragment.resources.getQuantityString(R.plurals.histories_removed, removedItems.size, removedItems.size), Snackbar.LENGTH_SHORT)
//                        .setAction(fragment.getString(R.string.cancel).toUpperCase(getSystemLocale(fragment.resources))) {
//                            for (removedItem in removedItems)
//                                UpdateHistoryTask(fragment, removedItem.id, removedItem.timestamp?.getText(fragment.requireContext()).toString()).execute()
//                        }.show()
//            }
//        }
//    }

//    private class UpdateHistoryTask internal constructor(fragment: Fragment, private val removedHistoryId: Int, private val removedHistoryTimestamp: String) : AsyncTask<Void, Void, Int>() {
//
//        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
//
//        override fun doInBackground(vararg p0: Void?): Int {
//            fragmentReference.get()?.let {
//                val mDao = RisuscitoDatabase.getInstance(it.requireContext()).cronologiaDao()
//                val cronTemp = Cronologia()
//                cronTemp.idCanto = removedHistoryId
//                cronTemp.ultimaVisita = Date(java.lang.Long.parseLong(removedHistoryTimestamp))
//                mDao.insertCronologia(cronTemp)
//            }
//            return 0
//        }
//    }

//    private class AddToListaDupTaskWithFinish internal constructor(activity: Activity, private val idLista: Int, private val listPosition: Int, private val idDaAgg: Int) : AsyncTask<Void, Void, Boolean>() {
//
//        private val activityReference: WeakReference<Activity> = WeakReference(activity)
//
//        override fun doInBackground(vararg params: Void?): Boolean {
//            activityReference.get()?.let {
//                try {
//                    val mDao = RisuscitoDatabase.getInstance(it).customListDao()
//                    val position = CustomList()
//                    position.id = idLista
//                    position.position = listPosition
//                    position.idCanto = idDaAgg
//                    position.timestamp = Date(System.currentTimeMillis())
//                    mDao.insertPosition(position)
//                } catch (e: SQLException) {
//                    return false
//                }
//                return true
//            }
//            return false
//        }
//
//        override fun onPostExecute(updated: Boolean) {
//            super.onPostExecute(updated)
//            activityReference.get()?.let {
//                it.setResult(if (updated) CustomLists.RESULT_OK else CustomLists.RESULT_KO)
//                it.finish()
//                Animatoo.animateShrink(it)
//            }
//        }
//    }

//    private class UpdateListaPersonalizzataTaskWithFinish internal constructor(activity: Activity, private val idLista: Int, private val idCanto: Int, private val listPosition: Int) : AsyncTask<Void, Void, Boolean>() {
//
//        private val activityReference: WeakReference<Activity> = WeakReference(activity)
//
//        override fun doInBackground(vararg p0: Void?): Boolean {
//            activityReference.get()?.let {
//                val mDao = RisuscitoDatabase.getInstance(it).listePersDao()
//                val listaPers = mDao.getListById(idLista)
//                if (listaPers?.lista != null) {
//                    listaPers.lista?.addCanto(idCanto.toString(), listPosition)
//                    mDao.updateLista(listaPers)
//                    return true
//                }
//            }
//            return false
//        }
//
//        override fun onPostExecute(updated: Boolean) {
//            super.onPostExecute(updated)
//            activityReference.get()?.let {
//                it.setResult(if (updated) CustomLists.RESULT_OK else CustomLists.RESULT_CANCELED)
//                it.finish()
//                Animatoo.animateShrink(it)
//            }
//        }
//    }

//    private var TAG = ListeUtils::class.java.canonicalName

}
