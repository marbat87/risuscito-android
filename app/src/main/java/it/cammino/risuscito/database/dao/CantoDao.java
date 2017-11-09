package it.cammino.risuscito.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import it.cammino.risuscito.database.Canto;

@Dao
public interface CantoDao {

    @Query("SELECT * FROM canto")
    List<Canto> getAll();

    @Query("SELECT * FROM canto WHERE id = :id")
    Canto getCantoById(int id);

    @Query("SELECT COUNT(*) FROM canto")
    int count();

    @Insert
    long[] insertCanto(List<Canto> cantiLists);

    @Update
    int updateCanto(Canto canto);

    @Update
    int updateCanti(List<Canto> cantiList);

}
