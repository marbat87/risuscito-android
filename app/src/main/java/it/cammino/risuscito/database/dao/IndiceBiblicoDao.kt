package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import it.cammino.risuscito.database.entities.IndiceBiblico
import it.cammino.risuscito.database.pojo.CantoBiblico

@Dao
interface IndiceBiblicoDao {

    @Query("SELECT B.pagina, B.source, B.color, B.id, A.ordinamento, A.titoloIndice FROM indicebiblico A, canto B WHERE A.idCanto = B.id ORDER BY A.ordinamento ASC")
    fun liveAll(): LiveData<List<CantoBiblico>>

    @Query("SELECT B.pagina, B.source, B.color, B.id, A.ordinamento, A.titoloIndice FROM indicebiblico A, canto B WHERE A.idCanto = B.id ORDER BY A.ordinamento ASC")
    fun all(): List<CantoBiblico>

    @Query("DELETE FROM indicebiblico")
    fun truncateTable()

    @Insert
    fun insertIndiceBiblico(indiceBiblico: IndiceBiblico)

    @Insert
    fun insertIndiceBiblico(indiceBiblicoList: List<IndiceBiblico>)

    @Update
    fun updateIndiceBiblico(indiceBiblico: IndiceBiblico): Int

    @Update
    fun updateIndiceBiblico(indiceBiblicoList: List<IndiceBiblico>): Int

}
