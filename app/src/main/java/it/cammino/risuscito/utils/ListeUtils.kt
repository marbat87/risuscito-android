package it.cammino.risuscito.utils

import android.app.Activity
import android.content.Context
import android.database.SQLException
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
import java.lang.ref.WeakReference
import java.sql.Date

object ListeUtils {

    private const val CANTO_PRESENTE = "CANTO_PRESENTE"

    fun addToListaDup(fragment: Fragment, idLista: Int, listPosition: Int, idDaAgg: Int) {
        AddToListaDupTask(fragment, idLista, listPosition, idDaAgg).execute()
    }

    fun addToListaNoDup(fragment: Fragment, idLista: Int, listPosition: Int, idDaAgg: Int, replaceTag: String) {
        AddToListaNoDupTask(fragment, idLista, listPosition, idDaAgg, replaceTag).execute()
    }

    // aggiunge il canto premuto ai preferiti
    fun addToFavorites(fragment: Fragment, idDaAgg: Int) {
        UpdateFavoriteTask(fragment, true, idDaAgg).execute()
    }

//    // aggiunge il canto premuto ai preferiti
//    fun addToFavoritesNoSnackbar(fragment: Fragment, idDaAgg: Int) {
//        UpdateFavoriteTask(fragment, false, idDaAgg).execute()
//    }

    fun removeFavoritesWithUndo(fragment: Fragment, mRemovedItems: Set<SimpleItem>) {
        RemoveFavoriteTask(fragment, true, mRemovedItems, mRemovedItems.size).execute()
    }

    fun removeHistoriesWithUndo(fragment: Fragment, mRemovedItems: Set<SimpleHistoryItem>) {
        RemoveHistoryTask(fragment, mRemovedItems, mRemovedItems.size).execute()
    }

    fun updateListaPersonalizzata(fragment: Fragment, listaUpd: ListaPers) {
        UpdateListaPersonalizzataTask(fragment).execute(listaUpd)
    }

    fun updatePosizione(fragment: Fragment, idDaAgg: Int, idListaDaAgg: Int, posizioneDaAgg: Int) {
        UpdatePosizioneTask(fragment, idDaAgg, idListaDaAgg, posizioneDaAgg).execute()
    }

    fun removePositionWithUndo(fragment: Fragment, idLista: Int, posizioneDaCanc: Int, idDaCanc: Int, timestampDaCanc: String) {
        RemovePosizioneTask(fragment, idLista, posizioneDaCanc, idDaCanc, timestampDaCanc).execute()
    }

    fun manageReplaceDialog(fragment: Fragment, idCanto: Int, replaceTag: String) {
        ManageReplaceDialogTask(fragment, idCanto, replaceTag).execute()
    }

    fun scambioConVuoto(fragment: Fragment, idLista: Int, posizioneDaCanc: Int, idDaCanc: Int, newPosition: Int) {
        ScambioConVuotoTask(fragment, idLista, posizioneDaCanc, idDaCanc, newPosition).execute()
    }

    fun scambioCanto(fragment: Fragment, idLista: Int, posizioneDaCanc: Int, idDaCanc: Int, newPosition: Int, newId: Int) {
        ScambioCantoTask(fragment, idLista, posizioneDaCanc, idDaCanc, newPosition, newId).execute()
    }

    fun cleanList(context: Context, idLista: Int) {
        Thread(
                Runnable {
                    val mDao = RisuscitoDatabase.getInstance(context).customListDao()
                    mDao.deleteListById(idLista)
                })
                .start()
    }

    fun addToListaDupAndFinish(activity: Activity, idLista: Int, listPosition: Int, idDaAgg: Int) {
        AddToListaDupTaskWithFinish(activity, idLista, listPosition, idDaAgg).execute()
    }

    fun updateListaPersonalizzataAndFinish(activity: Activity, idLista: Int, idCanto: Int, listPosition: Int) {
        UpdateListaPersonalizzataTaskWithFinish(activity, idLista, idCanto, listPosition).execute()
    }

    private class AddToListaNoDupTask internal constructor(fragment: Fragment, private val idLista: Int, private val listPosition: Int, private val idDaAgg: Int, private val replaceTag: String) : AsyncTask<Void, Void, String>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Void?): String? {
            val db = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!)
            val listeDao = db.customListDao()
            val cantoDao = db.cantoDao()
            val idPresente = listeDao.getIdByPosition(idLista, listPosition)
            if (idPresente != null) {
                return if (idDaAgg == idPresente)
                    CANTO_PRESENTE
                else
                    fragmentReference.get()!!.resources.getString(LUtils.getResId(cantoDao.getCantoById(idPresente).titolo, R.string::class.java))
            }

            val position = CustomList()
            position.id = idLista
            position.position = listPosition
            position.idCanto = idDaAgg
            position.timestamp = Date(System.currentTimeMillis())
            listeDao.insertPosition(position)

            return ""
        }

        override fun onPostExecute(titoloPresente: String?) {
            super.onPostExecute(titoloPresente)
            if (titoloPresente != null && titoloPresente == CANTO_PRESENTE) {
                Snackbar.make(fragmentReference.get()!!.view!!, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
            } else
                if (titoloPresente != null && titoloPresente.isNotEmpty()) {
                    SimpleDialogFragment.Builder(
                            (fragmentReference.get()!!.activity as AppCompatActivity?)!!,
                            fragmentReference.get()!! as SimpleDialogFragment.SimpleCallback,
                            replaceTag)
                            .title(R.string.dialog_replace_title)
                            .content(
                                    (fragmentReference.get()!!.getString(R.string.dialog_present_yet)
                                            + " "
                                            + titoloPresente
                                            + fragmentReference.get()!!.getString(R.string.dialog_wonna_replace)))
                            .positiveButton(R.string.replace_confirm)
                            .negativeButton(android.R.string.no)
                            .show()
                } else
                    Snackbar.make(fragmentReference.get()!!.view!!, R.string.list_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    private class AddToListaDupTask internal constructor(fragment: Fragment, private val idLista: Int, private val listPosition: Int, private val idDaAgg: Int) : AsyncTask<Void, Void, Boolean>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Void?): Boolean {
            try {
                val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).customListDao()
                val position = CustomList()
                position.id = idLista
                position.position = listPosition
                position.idCanto = idDaAgg
                position.timestamp = Date(System.currentTimeMillis())
                mDao.insertPosition(position)
            } catch (e: SQLException) {
                return false
            }
            return true
        }

        override fun onPostExecute(updated: Boolean) {
            super.onPostExecute(updated)
            if (updated)
                Snackbar.make(fragmentReference.get()!!.view!!, R.string.list_added, Snackbar.LENGTH_SHORT).show()
            else
                Snackbar.make(fragmentReference.get()!!.view!!, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
        }
    }

    private class UpdateFavoriteTask internal constructor(fragment: Fragment, private val showSnackbar: Boolean, private val idDaAgg: Int) : AsyncTask<Void, Void, Int>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg p0: Void?): Int {
            val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).favoritesDao()
            mDao.setFavorite(idDaAgg)
            return 0
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            if (showSnackbar)
                Snackbar.make(fragmentReference.get()!!.view!!, R.string.favorite_added, Snackbar.LENGTH_SHORT).show()

        }
    }

    private class RemoveFavoriteTask internal constructor(fragment: Fragment, private val withUndo: Boolean, private val mRemovedItems: Set<SimpleItem>, private val iRemoved: Int) : AsyncTask<Void, Void, Int>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg p0: Void?): Int {
            val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).favoritesDao()
            for (removedItem in mRemovedItems) {
                mDao.removeFavorite(removedItem.id)
            }
            return 0
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            if (withUndo)
                Snackbar.make(fragmentReference.get()!!.view!!, fragmentReference.get()!!.resources.getQuantityString(R.plurals.favorites_removed, iRemoved, iRemoved), Snackbar.LENGTH_SHORT)
                        .setAction(fragmentReference.get()!!.getString(android.R.string.cancel).toUpperCase()) {
                            for (removedItem in mRemovedItems)
                                UpdateFavoriteTask(fragmentReference.get()!!, false, removedItem.id).execute()
                        }.show()

        }
    }

    private class UpdateListaPersonalizzataTask internal constructor(fragment: Fragment) : AsyncTask<Any, Void, Int>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg p0: Any?): Int {
            val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).listePersDao()
            mDao.updateLista(p0[0] as ListaPers)
            return 0
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            Snackbar.make(fragmentReference.get()!!.view!!, R.string.list_added, Snackbar.LENGTH_SHORT).show()

        }
    }

    private class UpdatePosizioneTask internal constructor(fragment: Fragment, private val idDaAgg: Int, private val idListaDaAgg: Int, private val posizioneDaAgg: Int) : AsyncTask<Any, Void, Int>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Any?): Int {
            val mCustomListDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).customListDao()
            mCustomListDao.updatePositionNoTimestamp(idDaAgg, idListaDaAgg, posizioneDaAgg)
            return 0
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            Snackbar.make(fragmentReference.get()!!.view!!, R.string.list_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    private class ManageReplaceDialogTask internal constructor(fragment: Fragment, private val idCanto: Int, private val replaceTag: String) : AsyncTask<Any, Void, String>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Any?): String {
            val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).cantoDao()
            return mDao.getCantoById(idCanto).titolo!!
        }

        override fun onPostExecute(titoloPresente: String) {
            super.onPostExecute(titoloPresente)
            SimpleDialogFragment.Builder(
                    (fragmentReference.get()!!.activity as AppCompatActivity?)!!,
                    fragmentReference.get()!! as SimpleDialogFragment.SimpleCallback,
                    replaceTag)
                    .title(R.string.dialog_replace_title)
                    .content(
                            (fragmentReference.get()!!.getString(R.string.dialog_present_yet)
                                    + " "
                                    + fragmentReference.get()!!.resources.getString(LUtils.getResId(titoloPresente, R.string::class.java))
                                    + fragmentReference.get()!!.getString(R.string.dialog_wonna_replace)))
                    .positiveButton(R.string.replace_confirm)
                    .negativeButton(android.R.string.no)
                    .show()
        }
    }

    private class ScambioConVuotoTask internal constructor(fragment: Fragment, private val idLista: Int, private val posizioneDaCanc: Int, private val idDaCanc: Int, private val newPosition: Int) : AsyncTask<Any, Void, Boolean>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Any?): Boolean {
            val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).customListDao()

            return if (mDao.checkExistsPosition(idLista, newPosition, idDaCanc) > 0)
                true
            else {
                val positionToDelete = mDao.getPositionSpecific(idLista, posizioneDaCanc, idDaCanc)
                mDao.deletePosition(positionToDelete)

                val positionToInsert = CustomList()
                positionToInsert.id = idLista
                positionToInsert.position = newPosition
                positionToInsert.idCanto = idDaCanc
                positionToInsert.timestamp = positionToDelete.timestamp
                mDao.insertPosition(positionToInsert)
                false
            }
        }

        override fun onPostExecute(cantoPresente: Boolean) {
            super.onPostExecute(cantoPresente)
            Snackbar.make(fragmentReference.get()!!.view!!, if (cantoPresente) R.string.present_yet else R.string.switch_done, Snackbar.LENGTH_SHORT).show()
        }
    }

    private class ScambioCantoTask internal constructor(fragment: Fragment, private val idLista: Int, private val posizioneDaCanc: Int, private val idDaCanc: Int, private val newPosition: Int, private val newId: Int) : AsyncTask<Any, Void, Int>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Any?): Int {
            return if (newId != idDaCanc || posizioneDaCanc != newPosition) {
                val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).customListDao()
                if ((mDao.checkExistsPosition(idLista, newPosition, idDaCanc) > 0
                                || mDao.checkExistsPosition(idLista, posizioneDaCanc, newId) > 0)
                        && newPosition != posizioneDaCanc) {
                    1
                } else {
                    val positionToDelete = mDao.getPositionSpecific(idLista, newPosition, newId)
                    mDao.deletePosition(positionToDelete)

                    val positionToInsert = CustomList()
                    positionToInsert.id = idLista
                    positionToInsert.position = newPosition
                    positionToInsert.idCanto = idDaCanc
                    positionToInsert.timestamp = positionToDelete.timestamp

                    mDao.updatePositionNoTimestamp(newId, idLista, posizioneDaCanc, idDaCanc)
                    mDao.insertPosition(positionToInsert)
                    0
                }
            } else
                2
        }

        override fun onPostExecute(cantoPresente: Int) {
            super.onPostExecute(cantoPresente)
            when (cantoPresente) {
                0 -> Snackbar.make(fragmentReference.get()!!.view!!, R.string.switch_done, Snackbar.LENGTH_SHORT).show()
                1 -> Snackbar.make(fragmentReference.get()!!.view!!, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
                2 -> Snackbar.make(fragmentReference.get()!!.view!!, R.string.switch_impossible, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private class RemovePosizioneTask internal constructor(fragment: Fragment, private val idLista: Int, private val posizioneDaCanc: Int, private val idDaCanc: Int, private val timestampDaCanc: String) : AsyncTask<Any, Void, Int>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Any?): Int {
            val positionToDelete = CustomList()
            positionToDelete.id = idLista
            positionToDelete.position = posizioneDaCanc
            positionToDelete.idCanto = idDaCanc
            val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).customListDao()
            mDao.deletePosition(positionToDelete)
            return 0
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            Snackbar.make(
                    fragmentReference.get()!!.view!!,
                    R.string.song_removed,
                    Snackbar.LENGTH_LONG)
                    .setAction(
                            fragmentReference.get()!!.getString(android.R.string.cancel).toUpperCase()
                    ) {
                        ReinsertPositionTask(fragmentReference.get()!!, idLista, posizioneDaCanc, idDaCanc, timestampDaCanc).execute()
                    }
                    .show()
        }
    }

    private class ReinsertPositionTask internal constructor(fragment: Fragment, private val idLista: Int, private val posizioneDaCanc: Int, private val idDaCanc: Int, private val timestampDaCanc: String) : AsyncTask<Any, Void, Int>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Any?): Int {
            val positionToInsert = CustomList()
            positionToInsert.id = idLista
            positionToInsert.position = posizioneDaCanc
            positionToInsert.idCanto = idDaCanc
            positionToInsert.timestamp = Date(java.lang.Long.parseLong(timestampDaCanc))
            val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).customListDao()
            mDao.insertPosition(positionToInsert)
            return 0
        }
    }

    private class RemoveHistoryTask internal constructor(fragment: Fragment, private val mRemovedItems: Set<SimpleHistoryItem>, private val iRemoved: Int) : AsyncTask<Void, Void, Int>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg p0: Void?): Int {
            val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).cronologiaDao()
            for (removedItem in mRemovedItems) {
                val cronTemp = Cronologia()
                cronTemp.idCanto = removedItem.id
                mDao.deleteCronologia(cronTemp)
            }
            return 0
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            Snackbar.make(fragmentReference.get()!!.view!!, fragmentReference.get()!!.resources.getQuantityString(R.plurals.histories_removed, iRemoved, iRemoved), Snackbar.LENGTH_SHORT)
                    .setAction(fragmentReference.get()!!.getString(android.R.string.cancel).toUpperCase()) {
                        for (removedItem in mRemovedItems)
                            UpdateHistoryTask(fragmentReference.get()!!, removedItem.id, removedItem.timestamp!!.text.toString()).execute()
                    }.show()

        }
    }

    private class UpdateHistoryTask internal constructor(fragment: Fragment, private val removedHistoryId: Int, private val removedHistoryTimestamp: String) : AsyncTask<Void, Void, Int>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg p0: Void?): Int {
            val mDao = RisuscitoDatabase.getInstance(fragmentReference.get()!!.context!!).cronologiaDao()
            val cronTemp = Cronologia()
            cronTemp.idCanto = removedHistoryId
            cronTemp.ultimaVisita = Date(java.lang.Long.parseLong(removedHistoryTimestamp))
            mDao.insertCronologia(cronTemp)
            return 0
        }
    }

    private class AddToListaDupTaskWithFinish internal constructor(activity: Activity, private val idLista: Int, private val listPosition: Int, private val idDaAgg: Int) : AsyncTask<Void, Void, Boolean>() {

        private val activityReference: WeakReference<Activity> = WeakReference(activity)

        override fun doInBackground(vararg params: Void?): Boolean {
            try {
                val mDao = RisuscitoDatabase.getInstance(activityReference.get()!!).customListDao()
                val position = CustomList()
                position.id = idLista
                position.position = listPosition
                position.idCanto = idDaAgg
                position.timestamp = Date(System.currentTimeMillis())
                mDao.insertPosition(position)
            } catch (e: SQLException) {
                return false
            }
            return true
        }

        override fun onPostExecute(updated: Boolean) {
            super.onPostExecute(updated)
            activityReference.get()!!.setResult(if (updated) CustomLists.RESULT_OK else CustomLists.RESULT_KO)
            activityReference.get()!!.finish()
            Animatoo.animateShrink(activityReference.get()!!)
        }
    }

    private class UpdateListaPersonalizzataTaskWithFinish internal constructor(activity: Activity, private val idLista: Int, private val idCanto: Int, private val listPosition: Int) : AsyncTask<Void, Void, Boolean>() {

        private val activityReference: WeakReference<Activity> = WeakReference(activity)

        override fun doInBackground(vararg p0: Void?): Boolean {
            val mDao = RisuscitoDatabase.getInstance(activityReference.get()!!).listePersDao()
            val listaPers = mDao.getListById(idLista)
            if (listaPers?.lista != null) {
                listaPers.lista!!.addCanto(idCanto.toString(), listPosition)
                mDao.updateLista(listaPers)
                return true
            }
            return false
        }

        override fun onPostExecute(updated: Boolean) {
            super.onPostExecute(updated)
            activityReference.get()!!.setResult(if (updated) CustomLists.RESULT_OK else CustomLists.RESULT_CANCELED)
            activityReference.get()!!.finish()
            Animatoo.animateShrink(activityReference.get()!!)
        }
    }

}
