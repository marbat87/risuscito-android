package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.sql.Date

@Entity
class Cronologia {

    @PrimaryKey
    var idCanto: Int = 0

    var ultimaVisita = Date(System.currentTimeMillis())

}
