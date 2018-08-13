package it.cammino.risuscito.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

import it.cammino.risuscito.database.SalmoCanto
import it.cammino.risuscito.database.entities.Salmo

@Suppress("unused")
@Dao
interface SalmiDao {

    @get:Query("SELECT B.*, A.numSalmo, A.titoloSalmo FROM salmo A, canto B WHERE A.id = B.id ORDER BY A.numSalmo ASC, A.titoloSalmo ASC")
    val liveAll: LiveData<List<SalmoCanto>>

    @get:Query("SELECT B.*, A.numSalmo, A.titoloSalmo FROM salmo A, canto B WHERE A.id = B.id ORDER BY A.numSalmo ASC, A.titoloSalmo ASC")
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
