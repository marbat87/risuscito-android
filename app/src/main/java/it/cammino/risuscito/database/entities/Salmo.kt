package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class Salmo {

    @PrimaryKey
    var id: Int = 0

    var numSalmo: String? = null

    var titoloSalmo: String? = null

}
