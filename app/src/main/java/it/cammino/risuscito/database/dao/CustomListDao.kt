package it.cammino.risuscito.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import it.cammino.risuscito.database.Posizione
import it.cammino.risuscito.database.entities.CustomList

@Suppress("unused")
@Dao
interface CustomListDao {

    @get:Query("SELECT * FROM customlist")
    val all: List<CustomList>

    @Query("DELETE FROM customlist")
    fun truncateTable()

    @Query("SELECT B.*, A.timestamp, A.position FROM customlist A, canto B WHERE A.id = :id AND A.idCanto = B.id ORDER BY A.timestamp ASC")
    fun getList(id: Int): LiveData<List<Posizione>>

    @Query("SELECT B.titolo FROM customlist A , canto B WHERE A.id = :id AND A.position = :position AND A.idCanto = B.id")
    fun getTitoloByPosition(id: Int, position: Int): String?

    @Query("SELECT A.idCanto FROM customlist A WHERE A.id = :id AND A.position = :position")
    fun getIdByPosition(id: Int, position: Int): Int?

    @Query("SELECT * from customlist WHERE id = :id AND position = :position")
    fun getPosition(id: Int, position: Int): CustomList

    @Query("UPDATE customlist SET idCanto = :idCanto WHERE id = :id AND position = :position")
    fun updatePositionNoTimestamp(idCanto: Int, id: Int, position: Int)

    @Query("UPDATE customlist SET idCanto = :idCantoNew WHERE id = :id AND position = :position AND idCanto = :idCantoOld")
    fun updatePositionNoTimestamp(idCantoNew: Int, id: Int, position: Int, idCantoOld: Int)

    @Update
    fun updatePosition(position: CustomList): Int

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insertPosition(position: CustomList)

    @Query("DELETE FROM customlist WHERE id = :id")
    fun deleteListById(id: Int)

    @Delete
    fun deletePosition(position: CustomList)
}
