package it.cammino.risuscito.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import it.cammino.risuscito.database.entities.CustomList;
import it.cammino.risuscito.database.Posizione;

@Dao
public interface CustomListDao {

  @Query("SELECT * FROM customlist")
  List<CustomList> getAll();

  @Query(
      "SELECT B.*, A.timestamp, A.position FROM customlist A, canto B WHERE A.id = :id AND A.idCanto = B.id ORDER BY A.timestamp ASC")
  LiveData<List<Posizione>> getList(int id);

  @Query(
      "SELECT B.titolo FROM customlist A , canto B WHERE A.id = :id AND A.position = :position AND A.idCanto = B.id")
  String getTitoloByPosition(int id, int position);

  @Query("SELECT * from customlist WHERE id = :id AND position = :position")
  CustomList getPosition(int id, int position);

  @Query("UPDATE customlist SET idCanto = :idCanto WHERE id = :id AND position = :position")
  void updatePositionNoTimestamp(int idCanto, int id, int position);

  @Query(
      "UPDATE customlist SET idCanto = :idCantoNew WHERE id = :id AND position = :position AND idCanto = :idCantoOld")
  void updatePositionNoTimestamp(int idCantoNew, int id, int position, int idCantoOld);

  @Update
  int updatePosition(CustomList position);

  @Insert(onConflict = OnConflictStrategy.FAIL)
  void insertPosition(CustomList position);

  @Query("DELETE FROM customlist WHERE id = :id")
  void deleteListById(int id);

  @Delete
  void deletePosition(CustomList position);
}
