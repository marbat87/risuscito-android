package it.cammino.risuscito.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import it.cammino.risuscito.database.CantoLiturgico;
import it.cammino.risuscito.database.entities.IndiceLiturgico;
import it.cammino.risuscito.database.entities.NomeLiturgico;

@SuppressWarnings("unused")
@Dao
public interface IndiceLiturgicoDao {

  @Query("DELETE FROM indiceliturgico")
  void truncateIndiceLiturgico();

  @Query("DELETE FROM nomeliturgico")
  void truncateNomeIndiceLiturgico();

  @Query(
      "SELECT C.*, A.idIndice, A.nome FROM nomeliturgico A, indiceliturgico B, canto c WHERE A.idIndice = B.idIndice AND b.idCanto = c.id ORDER BY A.nome ASC, C.titolo ASC")
  LiveData<List<CantoLiturgico>> getLiveAll();

  @Query(
      "SELECT C.*, A.idIndice, A.nome FROM nomeliturgico A, indiceliturgico B, canto c WHERE A.idIndice = B.idIndice AND b.idCanto = c.id ORDER BY A.nome ASC, C.titolo ASC")
  List<CantoLiturgico> getAll();

  @Insert
  void insertIndice(IndiceLiturgico indice);

  @Insert
  void insertIndice(List<IndiceLiturgico> indiceList);

  @Insert
  void insertNomeIndice(NomeLiturgico nomeIndice);

  @Insert
  void insertNomeIndice(List<NomeLiturgico> nomeIndiceList);
}
