package it.cammino.risuscito.utils

import android.content.Context
import android.database.SQLException
import android.os.AsyncTask
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.CustomList
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.dialogs.SimpleDialogFragment
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
    fun addToFavorites(mContext: Context, rootView: View, idDaAgg: Int) {
        UpdateFavoriteTask().execute(mContext, rootView, idDaAgg)
    }

    fun updateListaPersonalizzata(mContext: Context, rootView: View, listaUpd: ListaPers) {
        UpdateListaPersonalizzataTask().execute(mContext, rootView, listaUpd)
    }

    fun updatePosizione(mContext: Context, rootView: View, idDaAgg: Int, idListaDaAgg: Int, posizioneDaAgg: Int) {
        UpdatePosizioneTask().execute(mContext, rootView, idDaAgg, idListaDaAgg, posizioneDaAgg)
    }

    fun manageReplaceDialog(fragment: Fragment, idCanto: Int, replaceTag: String) {
        ManageReplaceDialogTask(fragment, idCanto, replaceTag).execute()
    }

    private class AddToListaNoDupTask internal constructor(fragment: Fragment, private val idLista: Int, private val listPosition: Int, private val idDaAgg: Int, private val replaceTag: String) : AsyncTask<Any, Void, String>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Any?): String? {
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

    private class AddToListaDupTask internal constructor(fragment: Fragment, private val idLista: Int, private val listPosition: Int, private val idDaAgg: Int) : AsyncTask<Any, Void, Boolean>() {

        private val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Any?): Boolean {
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

    private class UpdateFavoriteTask : AsyncTask<Any, Void, View>() {
        override fun doInBackground(vararg params: Any?): View? {
            val mDao = RisuscitoDatabase.getInstance(params[0] as Context).favoritesDao()
            mDao.setFavorite(params[2] as Int)
            return params[1] as View
        }

        override fun onPostExecute(rootView: View?) {
            super.onPostExecute(rootView)
            Snackbar.make(rootView!!, R.string.favorite_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    private class UpdateListaPersonalizzataTask : AsyncTask<Any, Void, View>() {
        override fun doInBackground(vararg params: Any?): View? {
            val mDao = RisuscitoDatabase.getInstance(params[0] as Context).listePersDao()
            mDao.updateLista(params[2] as ListaPers)
            return params[1] as View
        }

        override fun onPostExecute(rootView: View?) {
            super.onPostExecute(rootView)
            Snackbar.make(rootView!!, R.string.list_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    private class UpdatePosizioneTask : AsyncTask<Any, Void, View>() {
        override fun doInBackground(vararg params: Any?): View? {
            val mCustomListDao = RisuscitoDatabase.getInstance(params[0] as Context).customListDao()
            mCustomListDao.updatePositionNoTimestamp(
                    params[2] as Int,
                    params[3] as Int,
                    params[4] as Int)
            return params[1] as View
        }

        override fun onPostExecute(rootView: View?) {
            super.onPostExecute(rootView)
            Snackbar.make(rootView!!, R.string.list_added, Snackbar.LENGTH_SHORT).show()
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

}
