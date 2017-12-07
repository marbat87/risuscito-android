package it.cammino.risuscito.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import it.cammino.risuscito.database.entities.LocalLink;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface LocalLinksDao {

  @Query("SELECT * FROM locallink WHERE idCanto = :id")
  LocalLink getLocalLinkByCantoId(int id);

  @Update
  void updateLocalLink(LocalLink localLink);

  @Insert(onConflict = REPLACE)
  void updateLocalLink(List<LocalLink> localLinkList);

  @Delete
  void deleteLocalLink(LocalLink localLink);

  @Delete
  void deleteLocalLink(List<LocalLink> localLinkList);

  @Insert
  void insertLocalLink(LocalLink localLink);

  @Insert
  void insertLocalLink(List<LocalLink> localLinkList);
}
