package it.cammino.risuscito.database.dao

import androidx.room.*
import it.cammino.risuscito.database.entities.LocalLink

@Suppress("unused")
@Dao
interface LocalLinksDao {

    @Query("DELETE FROM locallink")
    fun truncateTable()

    @get:Query("SELECT * FROM locallink")
    val all: List<LocalLink>

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
