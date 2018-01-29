package it.cammino.risuscito.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import it.cammino.risuscito.database.CantoConsegnato;
import it.cammino.risuscito.database.entities.Canto;
import it.cammino.risuscito.database.entities.Consegnato;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@SuppressWarnings("unused")
@Dao
public interface ConsegnatiDao {

  @Query("DELETE FROM consegnato")
  void truncateTable();

  @Query("SELECT A.* FROM canto A, consegnato B WHERE A.id = B.idCanto ORDER BY a.titolo ASC")
  LiveData<List<Canto>> getLiveConsegnati();

  @Query("SELECT A.* FROM canto A, consegnato B WHERE A.id = B.idCanto ORDER BY a.titolo ASC")
  List<Canto> getConsegnati();

  @Query(
      "SELECT A.*, coalesce(B.idConsegnato,0) as consegnato FROM canto A LEFT JOIN consegnato B ON A.id = B.idCanto ORDER BY A.titolo ASC")
  LiveData<List<CantoConsegnato>> getLiveChoosen();

  @Query(
      "SELECT A.*, coalesce(B.idConsegnato,0) as consegnato FROM canto A LEFT JOIN consegnato B ON A.id = B.idCanto ORDER BY A.titolo ASC")
  List<CantoConsegnato> getChoosen();

  @Query("DELETE FROM consegnato")
  void emptyConsegnati();

  @Insert(onConflict = REPLACE)
  void insertConsegnati(Consegnato consegnato);

  @Insert(onConflict = REPLACE)
  void insertConsegnati(List<Consegnato> consegnatoList);
}
