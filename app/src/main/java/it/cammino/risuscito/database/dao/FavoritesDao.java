package it.cammino.risuscito.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import it.cammino.risuscito.database.Canto;

@Dao
public interface FavoritesDao {

    @Query("SELECT * FROM canto WHERE favorite = 1")
    LiveData<List<Canto>> getLiveFavorites();

    @Query("UPDATE canto set favorite = 0")
    void resetFavorites();

    @Query("UPDATE canto SET favorite = 0 WHERE id = :favorited_id")
    void removeFavorite(int favorited_id);

}
