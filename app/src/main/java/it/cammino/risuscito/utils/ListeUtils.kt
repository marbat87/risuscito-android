package it.cammino.risuscito.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.SQLException
import android.os.AsyncTask
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.CustomList
import java.sql.Date

/** Created by marcello.battain on 14/11/2017.  */
object ListeUtils {

    fun addToListaDup(
            mContext: Context,
            rootView: View,
            idLista: Int,
            listPosition: Int,
            idDaAgg: Int) {
        Thread(
                Runnable {
                    try {
                        val mDao = RisuscitoDatabase.getInstance(mContext).customListDao()
                        val position = CustomList()
                        position.id = idLista
                        position.position = listPosition
                        position.idCanto = idDaAgg
                        position.timestamp = Date(System.currentTimeMillis())
                        mDao.insertPosition(position)
                        Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show()
                    } catch (e: SQLException) {
                        Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
                    }
                })
                .start()
    }

    fun addToListaNoDup(
            mContext: Context,
            rootView: View,
            idLista: Int,
            listPosition: Int,
            idDaAgg: Int): String {
        val db = RisuscitoDatabase.getInstance(mContext)
        val listeDao = db.customListDao()
        val cantoDao = db.cantoDao()
        val idPresente = listeDao.getIdByPosition(idLista, listPosition)
        if (idPresente != null) {
            return if (idDaAgg == idPresente) {
                Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
                ""
            } else {
                mContext.resources.getString(LUtils.getResId(cantoDao.getCantoById(idPresente).titolo, R.string::class.java))
            }
        }

        val position = CustomList()
        position.id = idLista
        position.position = listPosition
        position.idCanto = idDaAgg
        position.timestamp = Date(System.currentTimeMillis())
        listeDao.insertPosition(position)

        Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show()
        return ""
    }

    // aggiunge il canto premuto ai preferiti
    fun addToFavorites(mContext: Context, rootView: View, idDaAgg: Int) {
        UpdateFavoriteTask().execute(mContext, rootView, idDaAgg)
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateFavoriteTask : AsyncTask<Any, Void, View>() {
        override fun doInBackground(vararg params: Any?): View? {
            val mDao = RisuscitoDatabase.getInstance(params[0] as Context).favoritesDao()
            mDao.setFavorite(params[2] as Int)
            return params[1] as View
        }

        override fun onPostExecute(rootView: View?) {
            super.onPostExecute(rootView)
            Log.d("UpdateFavoriteTask", "snackbar from AsyncTask")
            Snackbar.make(rootView!!, R.string.favorite_added, Snackbar.LENGTH_SHORT).show()
        }
    }
}
