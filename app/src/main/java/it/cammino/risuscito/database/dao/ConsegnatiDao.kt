package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.risuscito.database.entities.Consegnato
import it.cammino.risuscito.database.pojo.CantoConsegnato

@Suppress("unused")
@Dao
interface ConsegnatiDao {

    @get:Query("SELECT A.titolo, A.pagina, A.source, A.color, A.id, B.idConsegnato as consegnato, B.txtNota, B.numPassaggio FROM canto A, consegnato B WHERE A.id = B.idCanto")
    val liveConsegnati: LiveData<List<CantoConsegnato>>

    @get:Query("SELECT A.titolo, A.pagina, A.source, A.color, A.id, B.idConsegnato as consegnato, B.txtNota, B.numPassaggio FROM canto A, consegnato B WHERE A.id = B.idCanto ORDER BY a.titolo ASC")
    val consegnati: List<CantoConsegnato>

    @get:Query("SELECT A.titolo, A.pagina, A.source, A.color, A.id, coalesce(B.idConsegnato,-1) as consegnato, coalesce(B.txtNota, '') as txtNota, B.numPassaggio FROM canto A LEFT JOIN consegnato B ON A.id = B.idCanto ORDER BY A.titolo ASC")
    val liveChoosen: LiveData<List<CantoConsegnato>>

    @get:Query("SELECT A.titolo, A.pagina, A.source, A.color, A.id, coalesce(B.idConsegnato,-1) as consegnato, coalesce(B.txtNota, '') as txtNota, B.numPassaggio FROM canto A LEFT JOIN consegnato B ON A.id = B.idCanto")
    val choosen: List<CantoConsegnato>

    @Query("SELECT COALESCE((SELECT numPassaggio FROM consegnato WHERE idCanto = :id), -1)")
    fun getNumPassaggio(id: Int): Int

    @get:Query("SELECT * FROM consegnato")
    val all: List<Consegnato>

    @Query("DELETE FROM consegnato")
    fun truncateTable()

    @Query("DELETE FROM consegnato")
    fun emptyConsegnati()

    @Upsert
    fun insertConsegnati(consegnato: Consegnato)

    @Upsert
    fun insertConsegnati(consegnatoList: List<Consegnato>)

    @Update
    fun updateConsegnato(consegnato: Consegnato)
}
