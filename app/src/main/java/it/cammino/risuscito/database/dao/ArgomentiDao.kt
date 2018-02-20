package it.cammino.risuscito.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

import it.cammino.risuscito.database.entities.Argomento
import it.cammino.risuscito.database.CantoArgomento
import it.cammino.risuscito.database.entities.NomeArgomento

@Dao
interface ArgomentiDao {

    @get:Query("SELECT C.id, C.pagina, C.titolo, C.source, C.color, C.link, B.idArgomento, A.nomeArgomento FROM nomeargomento A, argomento B, canto c WHERE A.idArgomento = B.idArgomento AND b.idCanto = c.id ORDER BY A.nomeArgomento ASC, C.titolo ASC")
    val liveAll: LiveData<List<CantoArgomento>>

    @get:Query("SELECT C.id, C.pagina, C.titolo, C.source, C.color, C.link, B.idArgomento, A.nomeArgomento FROM nomeargomento A, argomento B, canto c WHERE A.idArgomento = B.idArgomento AND b.idCanto = c.id ORDER BY A.nomeArgomento ASC, C.titolo ASC")
    val all: List<CantoArgomento>

    @Query("DELETE FROM argomento")
    fun truncateArgomento()

    @Query("DELETE FROM nomeargomento")
    fun truncateNomeArgomento()

    @Insert
    fun insertArgomento(argomento: Argomento)

    @Insert
    fun insertArgomento(argomentoList: List<Argomento>)

    @Insert
    fun insertNomeArgomento(nomeArgomento: NomeArgomento)

    @Insert
    fun insertNomeArgomento(nomeArgomentoList: List<NomeArgomento>)
}
