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

  @Query("SELECT A.* FROM canto A, consegnato B WHERE A.id = B.idCanto ORDER BY titolo ASC")
  List<Canto> getAllByNameOnlyConsegnati();

  @Query("SELECT * FROM canto ORDER BY pagina ASC, titolo ASC")
  LiveData<List<Canto>> getLiveAllByPage();

  @Query("SELECT * FROM canto WHERE id = :id")
  Canto getCantoById(int id);

  @Query("SELECT * from canto WHERE source = :src")
  List<Canto> getCantiWithSource(String src);

  @Query("SELECT A.id, A.pagina, A.titolo, A.source, A.favorite, A.color, coalesce(B.localPath, A.link) as link, A.zoom, A.scrollX, A.scrollY, A.savedTab, A.savedTab, A.savedSpeed FROM canto A LEFT JOIN locallink B ON (A.id = b.idCanto)")
  List<Canto> getAllByWithLink();

  @Query("SELECT COUNT(*) FROM canto")
  int count();

  @Insert
  void insertCanto(List<Canto> cantiLists);

  @Update
  int updateCanto(Canto canto);

  @Update
  int updateCanti(List<Canto> cantiList);
}
