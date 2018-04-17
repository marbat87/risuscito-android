package it.cammino.risuscito.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import it.cammino.risuscito.database.entities.ListaPers

@Dao
interface ListePersDao {

    @get:Query("SELECT * FROM listapers ORDER BY id ASC")
    val liveAll: LiveData<List<ListaPers>>

    @get:Query("SELECT * FROM listapers ORDER BY id ASC")
    val all: List<ListaPers>

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
