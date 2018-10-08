package it.cammino.risuscito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.sql.Date

@Entity
class Cronologia {

    @PrimaryKey
    var idCanto: Int = 0

    var ultimaVisita = Date(System.currentTimeMillis())

}
