package it.cammino.risuscito.utils;

import android.content.Context;
import android.database.SQLException;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.sql.Date;

import it.cammino.risuscito.R;
import it.cammino.risuscito.database.CustomList;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.CustomListDao;
import it.cammino.risuscito.database.dao.FavoritesDao;

/** Created by marcello.battain on 14/11/2017. */
public class ListeUtils {

  public static void addToListaDup(
      final Context mContext,
      final View rootView,
      final int idLista,
      final int listPosition,
      final int idDaAgg) {
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                try {
                  CustomListDao mDao = RisuscitoDatabase.getInstance(mContext).customListDao();
                  CustomList position = new CustomList();
                  position.id = idLista;
                  position.position = listPosition;
                  position.idCanto = idDaAgg;
                  position.timestamp = new Date(System.currentTimeMillis());
                  mDao.insertPosition(position);
                  Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
                } catch (SQLException e) {
                  Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show();
                }
              }
            })
        .start();
  }

  // aggiunge il canto premuto ad una lista e in una posizione che NON ammetta duplicati
  public static String addToListaNoDup(
      final Context mContext,
      final View rootView,
      final int idLista,
      final int listPosition,
      final String titoloDaAgg,
      final int idDaAgg) {
    CustomListDao mDao = RisuscitoDatabase.getInstance(mContext).customListDao();
    String titoloPresente = mDao.getTitoloByPosition(idLista, listPosition);
    if (titoloPresente != null && !titoloPresente.isEmpty()) {
      if (titoloDaAgg.equalsIgnoreCase(titoloPresente)) {
        Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT).show();
        return "";
      } else {
        return titoloPresente;
      }
    }

    CustomList position = new CustomList();
    position.id = idLista;
    position.position = listPosition;
    position.idCanto = idDaAgg;
    position.timestamp = new Date(System.currentTimeMillis());
    mDao.insertPosition(position);

    Snackbar.make(rootView, R.string.list_added, Snackbar.LENGTH_SHORT).show();
    return "";
  }

  // aggiunge il canto premuto ai preferiti
  public static void addToFavorites(final Context mContext, final View rootView, final int idDaAgg) {
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                FavoritesDao mDao = RisuscitoDatabase.getInstance(mContext).favoritesDao();
                mDao.setFavorite(idDaAgg);
                Snackbar.make(rootView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
              }
            })
        .start();
  }
}
