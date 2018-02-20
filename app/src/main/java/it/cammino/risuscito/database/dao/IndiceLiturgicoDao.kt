package it.cammino.risuscito.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

import it.cammino.risuscito.database.CantoLiturgico
import it.cammino.risuscito.database.entities.IndiceLiturgico
import it.cammino.risuscito.database.entities.NomeLiturgico

@Dao
interface IndiceLiturgicoDao {

    @get:Query("SELECT C.*, A.idIndice, A.nome FROM nomeliturgico A, indiceliturgico B, canto c WHERE A.idIndice = B.idIndice AND b.idCanto = c.id ORDER BY A.nome ASC, C.titolo ASC")
    val liveAll: LiveData<List<CantoLiturgico>>

    @get:Query("SELECT C.*, A.idIndice, A.nome FROM nomeliturgico A, indiceliturgico B, canto c WHERE A.idIndice = B.idIndice AND b.idCanto = c.id ORDER BY A.nome ASC, C.titolo ASC")
    val all: List<CantoLiturgico>

    @Query("DELETE FROM indiceliturgico")
    fun truncateIndiceLiturgico()

    @Query("DELETE FROM nomeliturgico")
    fun truncateNomeIndiceLiturgico()

    @Insert
    fun insertIndice(indice: IndiceLiturgico)

    @Insert
    fun insertIndice(indiceList: List<IndiceLiturgico>)

    @Insert
    fun insertNomeIndice(nomeIndice: NomeLiturgico)

    @Insert
    fun insertNomeIndice(nomeIndiceList: List<NomeLiturgico>)
}
