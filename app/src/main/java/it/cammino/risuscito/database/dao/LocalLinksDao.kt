package it.cammino.risuscito.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import it.cammino.risuscito.database.entities.LocalLink

@Dao
interface LocalLinksDao {

    @Query("DELETE FROM locallink")
    fun truncateTable()

    @Query("SELECT * FROM locallink")
    fun all(): List<LocalLink>

    @Query("SELECT * FROM locallink WHERE idCanto = :id")
    fun getLocalLinkByCantoId(id: Int): LocalLink?

    @Update
    fun updateLocalLink(localLink: LocalLink)

    @Upsert
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
