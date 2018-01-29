package it.cammino.risuscito.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import it.cammino.risuscito.database.SalmoCanto;
import it.cammino.risuscito.database.entities.Salmo;

@Dao
public interface SalmiDao {

    @Query("DELETE FROM salmo")
    void truncateTable();

    @Query("SELECT B.*, A.numSalmo, A.titoloSalmo FROM salmo A, canto B WHERE A.id = B.id ORDER BY A.numSalmo ASC, A.titoloSalmo ASC")
    LiveData<List<SalmoCanto>> getLiveAll();

    @Query("SELECT B.*, A.numSalmo, A.titoloSalmo FROM salmo A, canto B WHERE A.id = B.id ORDER BY A.numSalmo ASC, A.titoloSalmo ASC")
    List<SalmoCanto> getAll();

    @Insert
    void insertSalmo(Salmo salmo);

    @Insert
    void insertSalmo(List<Salmo> salmi);

    @Update
    int updateSalmo(Salmo salmo);

    @Update
    int updateSalmo(List<Salmo> salmi);

}
