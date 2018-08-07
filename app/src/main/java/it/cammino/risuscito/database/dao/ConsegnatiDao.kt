package it.cammino.risuscito.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

import it.cammino.risuscito.database.CantoConsegnato
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.database.entities.Consegnato

import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Suppress("unused")
@Dao
interface ConsegnatiDao {

    @get:Query("SELECT A.* FROM canto A, consegnato B WHERE A.id = B.idCanto")
    val liveConsegnati: LiveData<List<Canto>>

    @get:Query("SELECT A.* FROM canto A, consegnato B WHERE A.id = B.idCanto ORDER BY a.titolo ASC")
    val consegnati: List<Canto>

    @get:Query("SELECT A.*, coalesce(B.idConsegnato,0) as consegnato FROM canto A LEFT JOIN consegnato B ON A.id = B.idCanto ORDER BY A.titolo ASC")
    val liveChoosen: LiveData<List<CantoConsegnato>>

    @get:Query("SELECT A.*, coalesce(B.idConsegnato,0) as consegnato FROM canto A LEFT JOIN consegnato B ON A.id = B.idCanto")
    val choosen: List<CantoConsegnato>

    @Query("DELETE FROM consegnato")
    fun truncateTable()

    @Query("DELETE FROM consegnato")
    fun emptyConsegnati()

    @Insert(onConflict = REPLACE)
    fun insertConsegnati(consegnato: Consegnato)

    @Insert(onConflict = REPLACE)
    fun insertConsegnati(consegnatoList: List<Consegnato>)
}
