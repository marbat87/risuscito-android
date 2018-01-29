package it.cammino.risuscito.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import it.cammino.risuscito.database.CantoCronologia;
import it.cammino.risuscito.database.entities.Cronologia;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface CronologiaDao {

  @Query("DELETE FROM cronologia")
  void truncateTable();

  @Query(
      "SELECT A.*, B.ultimaVisita FROM canto A, cronologia B WHERE A.id = B.idCanto ORDER BY B.ultimaVisita DESC")
  LiveData<List<CantoCronologia>> getLiveCronologia();

  @Query(
          "SELECT A.*, B.ultimaVisita FROM canto A, cronologia B WHERE A.id = B.idCanto ORDER BY B.ultimaVisita DESC")
  List<CantoCronologia> getCronologia();

  @Query("DELETE FROM cronologia")
  void emptyCronologia();

  @Update
  void updateCronologia(Cronologia cronologia);

  @Insert(onConflict = REPLACE)
  void insertCronologia(Cronologia cronologia);

  @Delete
  void deleteCronologia(Cronologia cronologia);

  @Delete
  void deleteCronologia(List<Cronologia> cronologiaList);
}
