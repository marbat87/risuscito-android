package it.cammino.risuscito.utils

import android.content.Context
import android.database.SQLException
import android.support.design.widget.Snackbar
import android.view.View
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

    // aggiunge il canto premuto ad una lista e in una posizione che NON ammetta duplicati
    fun addToListaNoDup(
            mContext: Context,
            rootView: View,
            idLista: Int,
            listPosition: Int,
            titoloDaAgg: String,
            idDaAgg: Int): String {
        val mDao = RisuscitoDatabase.getInstance(mContext).customListDao()
        val titoloPresente = if (mDao.getTitoloByPosition(idLista, listPosition) != null) mContext.resources.getString(LUtils.getResId(mDao.getTitoloByPosition(idLista, listPosition), R.string::class.java)) else null
        if (titoloPresente != null && !titoloPresente.isEmpty()) {
            return if (titoloDaAgg.equals(titoloPresente, ignoreCase = true)) {
                Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show()
                ""
            } else {
                titoloPresente
            }
        }

        val position = CustomList()
        position.id = idLista
        position.position = listPosition
        position.idCanto = idDaAgg
        position.timestamp = Date(System.currentTimeMillis())
        mDao.insertPosition(position)

        Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show()
        return ""
    }

    // aggiunge il canto premuto ai preferiti
    fun addToFavorites(mContext: Context, rootView: View, idDaAgg: Int) {
        Thread(
                Runnable {
                    val mDao = RisuscitoDatabase.getInstance(mContext).favoritesDao()
                    mDao.setFavorite(idDaAgg)
                    Snackbar.make(rootView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show()
                })
                .start()
    }
}
