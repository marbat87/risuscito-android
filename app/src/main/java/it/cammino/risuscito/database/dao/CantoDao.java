package it.cammino.risuscito.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import it.cammino.risuscito.database.entities.Canto;

@Dao
public interface CantoDao {

    @Query("SELECT * FROM canto ORDER BY titolo ASC")
    List<Canto> getAllByName();

    @Query("SELECT * FROM canto ORDER BY titolo ASC")
    LiveData<List<Canto>> getLiveAllByName();

    @Query("SELECT * FROM canto ORDER BY pagina ASC, titolo ASC")
    LiveData<List<Canto>> getLiveAllByPage();

    @Query("SELECT * FROM canto WHERE id = :id")
    Canto getCantoById(int id);

    @Query("SELECT COUNT(*) FROM canto")
    int count();

    @Insert
    void insertCanto(List<Canto> cantiLists);

    @Update
    int updateCanto(Canto canto);

    @Update
    int updateCanti(List<Canto> cantiList);

}
