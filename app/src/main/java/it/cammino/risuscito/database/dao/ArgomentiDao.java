package it.cammino.risuscito.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import it.cammino.risuscito.database.entities.Argomento;
import it.cammino.risuscito.database.CantoArgomento;
import it.cammino.risuscito.database.entities.NomeArgomento;

@SuppressWarnings("unused")
@Dao
public interface ArgomentiDao {

  @Query("DELETE FROM argomento")
  void truncateArgomento();

  @Query("DELETE FROM nomeargomento")
  void truncateNomeArgomento();

  @Query(
      "SELECT C.id, C.pagina, C.titolo, C.source, C.color, C.link, B.idArgomento, A.nomeArgomento FROM nomeargomento A, argomento B, canto c WHERE A.idArgomento = B.idArgomento AND b.idCanto = c.id ORDER BY A.nomeArgomento ASC, C.titolo ASC")
  LiveData<List<CantoArgomento>> getLiveAll();

  @Query(
      "SELECT C.id, C.pagina, C.titolo, C.source, C.color, C.link, B.idArgomento, A.nomeArgomento FROM nomeargomento A, argomento B, canto c WHERE A.idArgomento = B.idArgomento AND b.idCanto = c.id ORDER BY A.nomeArgomento ASC, C.titolo ASC")
  List<CantoArgomento> getAll();

  @Insert
  void insertArgomento(Argomento argomento);

  @Insert
  void insertArgomento(List<Argomento> argomentoList);

  @Insert
  void insertNomeArgomento(NomeArgomento nomeArgomento);

  @Insert
  void insertNomeArgomento(List<NomeArgomento> nomeArgomentoList);
}
