package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import it.cammino.risuscito.database.entities.ListaPers

@Dao
interface ListePersDao {

    @Query("SELECT * FROM listapers ORDER BY id ASC")
    fun liveAll(): LiveData<List<ListaPers>>

    @Query("SELECT * FROM listapers ORDER BY id ASC")
    fun all(): List<ListaPers>

    @Query("DELETE FROM listapers")
    fun truncateTable()

    @Update
    fun updateLista(lista: ListaPers)

    @Insert
    fun insertLista(lista: ListaPers)

    @Query("SELECT * FROM listapers WHERE id = :id")
    fun getListById(id: Int): ListaPers?

    @Query("SELECT * FROM listapers WHERE id = :id")
    fun getLiveListById(id: Int): LiveData<ListaPers>?

    @Delete
    fun deleteList(lista: ListaPers)

}
