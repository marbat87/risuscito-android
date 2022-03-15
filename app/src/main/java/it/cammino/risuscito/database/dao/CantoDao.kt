package it.cammino.risuscito.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import it.cammino.risuscito.database.entities.Canto

@Suppress("unused")
@Dao
interface CantoDao {

    @get:Query("SELECT id, pagina, titolo, source, favorite, color, link, zoom, scrollX, scrollY, savedBarre, savedTab, coalesce(savedSpeed,\"2\") as savedSpeed FROM canto ORDER BY titolo ASC")
    val allByName: List<Canto>

    @get:Query("SELECT id, pagina, titolo, source, favorite, color, link, zoom, scrollX, scrollY, savedBarre, savedTab, coalesce(savedSpeed,\"2\") as savedSpeed FROM canto")
    val liveAll: LiveData<List<Canto>>

    @get:Query("SELECT A.id, A.pagina, A.titolo, A.source, A.favorite, A.color, link, A.zoom, A.scrollX, A.scrollY, A.savedBarre, A.savedTab, coalesce(A.savedSpeed,\"2\") as savedSpeed FROM canto A, consegnato B WHERE A.id = B.idCanto ORDER BY titolo ASC")
    val allByNameOnlyConsegnati: List<Canto>

    @get:Query("SELECT A.id, A.pagina, A.titolo, A.source, A.favorite, A.color, coalesce(B.localPath, A.link) as link, A.zoom, A.scrollX, A.scrollY, A.savedBarre, A.savedTab, A.savedSpeed FROM canto A LEFT JOIN locallink B ON (A.id = b.idCanto)")
    val allByWithLink: List<Canto>

    @get:Query("SELECT id, zoom, scrollX, scrollY, favorite, savedTab, savedBarre, savedSpeed FROM canto")
    val backup: List<Backup>

    @Query("DELETE FROM canto")
    fun truncateTable()

    @Query("SELECT id, pagina, titolo, source, favorite, color, link, zoom, scrollX, scrollY, savedBarre, savedTab, coalesce(savedSpeed,\"2\") as savedSpeed FROM canto WHERE id = :id")
    fun getCantoById(id: Int): Canto?

    @Query("SELECT id, pagina, titolo, source, favorite, color, link, zoom, scrollX, scrollY, savedBarre, savedTab, coalesce(savedSpeed,\"2\") as savedSpeed from canto WHERE source = :src")
    fun getCantiWithSource(src: String): List<Canto>?

    @Query("SELECT A.id, A.pagina, A.titolo, A.source, A.favorite, A.color, link, A.zoom, A.scrollX, A.scrollY, A.savedBarre, A.savedTab, coalesce(A.savedSpeed,\"2\") as savedSpeed from canto A, consegnato B WHERE A.source = :src AND A.id = B.idCanto")
    fun getCantiWithSourceOnlyConsegnati(src: String): List<Canto>?

    @Query("SELECT COUNT(*) FROM canto")
    fun count(): Int

    @Insert
    fun insertCanto(canto: Canto)

    @Insert
    fun insertCanto(cantiLists: ArrayList<Canto>)

    @Update
    fun updateCanto(canto: Canto)

    @Update
    fun updateCanti(cantiList: ArrayList<Canto>)

    @Query("UPDATE canto set zoom = :zoom, scrollX = :scrollX, scrollY = :scrollY, favorite = :favorite, savedTab = :savedTab, savedBarre = :savedBarre, savedSpeed = :savedSpeed WHERE id = :id")
    fun setBackup(
            id: Int,
            zoom: Int,
            scrollX: Int,
            scrollY: Int,
            favorite: Int,
            savedTab: String?,
            savedBarre: String?,
            savedSpeed: String?)

}
