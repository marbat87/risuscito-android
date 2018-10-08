package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

import it.cammino.risuscito.database.entities.Argomento
import it.cammino.risuscito.database.CantoArgomento
import it.cammino.risuscito.database.entities.NomeArgomento

@Suppress("unused")
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
