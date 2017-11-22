package it.cammino.risuscito.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import it.cammino.risuscito.database.entities.Canto;

@SuppressWarnings("unused")
@Dao
public interface CantoDao {

  @Query("DELETE FROM canto")
  void truncateTable();

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

  @Query(
      "SELECT A.id, A.pagina, A.titolo, A.source, A.favorite, A.color, coalesce(B.localPath, A.link) as link, A.zoom, A.scrollX, A.scrollY, A.savedBarre, A.savedTab, A.savedSpeed FROM canto A LEFT JOIN locallink B ON (A.id = b.idCanto)")
  List<Canto> getAllByWithLink();

  @Query("SELECT COUNT(*) FROM canto")
  int count();

  @Insert
  void insertCanto(List<Canto> cantiLists);

  @Update
  void updateCanto(Canto canto);

  @Update
  void updateCanti(List<Canto> cantiList);

  @Query(
      "UPDATE canto set zoom = :zoom, scrollX = :scrollX, scrollY = :scrollY, favorite = :favorite, savedTab = :savedTab, savedBarre = :savedBarre, savedSpeed = :savedSpeed WHERE id = :id")
  void setBackup(
      int id,
      int zoom,
      int scrollX,
      int scrollY,
      int favorite,
      String savedTab,
      String savedBarre,
      String savedSpeed);

  @Query("SELECT id, zoom, scrollX, scrollY, favorite, savedTab, savedBarre, savedSpeed FROM canto")
  List<Backup> getBackup();

  class Backup {
    public int id;
    public int zoom;
    public int scrollX;
    public int scrollY;
    public int favorite;
    public String savedTab;
    public String savedBarre;
    public String savedSpeed;
  }
}
