package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import it.cammino.risuscito.database.SalmoCanto
import it.cammino.risuscito.database.entities.Salmo

@Suppress("unused")
@Dao
interface SalmiDao {

    @get:Query("SELECT B.pagina, B.source, B.color, B.id, A.numSalmo, A.titoloSalmo FROM salmo A, canto B WHERE A.id = B.id ORDER BY A.numSalmo ASC, A.titoloSalmo ASC")
    val liveAll: LiveData<List<SalmoCanto>>

    @get:Query("SELECT B.pagina, B.source, B.color, B.id, A.numSalmo, A.numSalmo, A.titoloSalmo FROM salmo A, canto B WHERE A.id = B.id ORDER BY A.numSalmo ASC, A.titoloSalmo ASC")
    val all: List<SalmoCanto>

    @Query("DELETE FROM salmo")
    fun truncateTable()

    @Insert
    fun insertSalmo(salmo: Salmo)

    @Insert
    fun insertSalmo(salmi: List<Salmo>)

    @Update
    fun updateSalmo(salmo: Salmo): Int

    @Update
    fun updateSalmo(salmi: List<Salmo>): Int

}
