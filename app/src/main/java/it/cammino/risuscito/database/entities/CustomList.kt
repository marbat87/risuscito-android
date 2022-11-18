package it.cammino.risuscito.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import it.cammino.risuscito.utils.StringUtils

import java.sql.Date

@Entity(primaryKeys = ["id", "position", "idCanto"])
class CustomList {

    var id: Int = 0

    var position: Int = 0

    var idCanto: Int = 0

    //IMPORTANTE PER FAR FUNZIONARE L'AUTO-MIGRATION DI ROOM
    @ColumnInfo(defaultValue = StringUtils.EMPTY)
    var notaPosizione: String = StringUtils.EMPTY

    var timestamp: Date? = null
}
