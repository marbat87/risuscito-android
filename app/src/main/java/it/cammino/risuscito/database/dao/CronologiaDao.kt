package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import it.cammino.risuscito.database.entities.Cronologia
import it.cammino.risuscito.database.pojo.CantoCronologia

@Suppress("unused")
@Dao
interface CronologiaDao {

    @get:Query("SELECT A.titolo, A.pagina, A.source, A.color, A.id, B.ultimaVisita FROM canto A, cronologia B WHERE A.id = B.idCanto ORDER BY B.ultimaVisita DESC")
    val liveCronologia: LiveData<List<CantoCronologia>>

    @get:Query("SELECT A.titolo, A.pagina, A.source, A.color, A.id, B.ultimaVisita FROM canto A, cronologia B WHERE A.id = B.idCanto ORDER BY B.ultimaVisita DESC")
    val cronologia: List<CantoCronologia>

    @get:Query("SELECT * FROM cronologia")
    val all: List<Cronologia>

    @Query("DELETE FROM cronologia")
    fun truncateTable()

    @Query("DELETE FROM cronologia")
    fun emptyCronologia()

    @Update
    fun updateCronologia(cronologia: Cronologia)

    @Insert(onConflict = REPLACE)
    fun insertCronologia(cronologia: Cronologia)

    @Delete
    fun deleteCronologia(cronologia: Cronologia)

    @Delete
    fun deleteCronologia(cronologiaList: List<Cronologia>)
}
