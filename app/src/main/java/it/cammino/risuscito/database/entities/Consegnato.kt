package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity

@Entity(primaryKeys = ["idConsegnato", "idCanto"])
class Consegnato {

    var idConsegnato: Int = 0

    var idCanto: Int = 0

}
