package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.risuscito.database.entities.Cronologia
import it.cammino.risuscito.database.pojo.CantoCronologia

@Suppress("unused")
@Dao
interface CronologiaDao {

    @Query("SELECT A.titolo, A.pagina, A.source, A.color, A.id, B.ultimaVisita FROM canto A, cronologia B WHERE A.id = B.idCanto ORDER BY B.ultimaVisita DESC")
    fun liveCronologia(): LiveData<List<CantoCronologia>>

    @Query("SELECT A.titolo, A.pagina, A.source, A.color, A.id, B.ultimaVisita FROM canto A, cronologia B WHERE A.id = B.idCanto ORDER BY B.ultimaVisita DESC")
    fun cronologia(): List<CantoCronologia>

    @Query("SELECT * FROM cronologia")
    fun all(): List<Cronologia>

    @Query("DELETE FROM cronologia")
    fun truncateTable()

    @Query("DELETE FROM cronologia")
    fun emptyCronologia()

    @Update
    fun updateCronologia(cronologia: Cronologia)

    @Upsert
    fun insertCronologia(cronologia: Cronologia)

    @Delete
    fun deleteCronologia(cronologia: Cronologia)

    @Delete
    fun deleteCronologia(cronologiaList: List<Cronologia>)
}
