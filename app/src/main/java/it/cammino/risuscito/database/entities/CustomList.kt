package it.cammino.risuscito.database.entities

import androidx.room.Entity

import java.sql.Date

@Entity(primaryKeys = ["id", "position", "idCanto"])
class CustomList {

    var id: Int = 0

    var position: Int = 0

    var idCanto: Int = 0

    var timestamp: Date? = null
}
