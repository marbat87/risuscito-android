package it.cammino.risuscito.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import it.cammino.risuscito.database.entities.ListaPers;

@Dao
public interface ListePersDao {

    @Query("DELETE FROM listapers")
    void truncateTable();

    @Query("SELECT * FROM listapers ORDER BY id ASC")
    LiveData<List<ListaPers>> getLiveAll();

    @Query("SELECT * FROM listapers ORDER BY id ASC")
    List<ListaPers> getAll();

    @Update
    void updateLista(ListaPers lista);

    @Insert
    void insertLista(ListaPers lista);

    @Query("SELECT * FROM listapers WHERE id = :id")
    ListaPers getListById(int id);

    @Delete
    void deleteList(ListaPers lista);

}
