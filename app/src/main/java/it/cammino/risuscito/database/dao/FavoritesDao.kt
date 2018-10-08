package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

import it.cammino.risuscito.database.entities.Canto

@Dao
interface FavoritesDao {

    @get:Query("SELECT * FROM canto WHERE favorite = 1")
    val liveFavorites: LiveData<List<Canto>>

    @Query("UPDATE canto set favorite = 0")
    fun resetFavorites()

    @Query("UPDATE canto SET favorite = 0 WHERE id = :favorited_id")
    fun removeFavorite(favorited_id: Int)

    @Query("UPDATE canto SET favorite = 1 WHERE id = :favorite_id")
    fun setFavorite(favorite_id: Int)

}
