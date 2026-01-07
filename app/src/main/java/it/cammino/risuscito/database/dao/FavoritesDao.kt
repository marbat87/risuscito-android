package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

import it.cammino.risuscito.database.entities.Canto

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM canto WHERE favorite = 1")
    fun liveFavorites(): LiveData<List<Canto>>

    @Query("UPDATE canto set favorite = 0")
    fun resetFavorites()

    @Query("UPDATE canto SET favorite = 0 WHERE id = :id")
    fun removeFavorite(id: Int)

    @Query("UPDATE canto SET favorite = 1 WHERE id = :id")
    fun setFavorite(id: Int)

}
