package it.cammino.risuscito.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import it.cammino.risuscito.database.entities.LocalLink

@Suppress("unused")
@Dao
interface LocalLinksDao {

    @Query("DELETE FROM locallink")
    fun truncateTable()

    @Query("SELECT * FROM locallink WHERE idCanto = :id")
    fun getLocalLinkByCantoId(id: Int): LocalLink?

    @Update
    fun updateLocalLink(localLink: LocalLink)

    @Insert(onConflict = REPLACE)
    fun updateLocalLink(localLinkList: List<LocalLink>)

    @Delete
    fun deleteLocalLink(localLink: LocalLink)

    @Delete
    fun deleteLocalLink(localLinkList: List<LocalLink>)

    @Insert
    fun insertLocalLink(localLink: LocalLink)

    @Insert
    fun insertLocalLink(localLinkList: List<LocalLink>)
}
