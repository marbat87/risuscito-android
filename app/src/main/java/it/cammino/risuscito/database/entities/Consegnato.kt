package it.cammino.risuscito.database.entities

import androidx.room.Entity

@Entity(primaryKeys = ["idConsegnato", "idCanto"])
class Consegnato {

    var idConsegnato: Int = 0

    var idCanto: Int = 0

    var txtNota: String = ""

}
