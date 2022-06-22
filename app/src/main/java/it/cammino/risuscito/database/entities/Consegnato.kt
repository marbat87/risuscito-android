package it.cammino.risuscito.database.entities

import androidx.room.Entity
import it.cammino.risuscito.utils.StringUtils

@Entity(primaryKeys = ["idConsegnato", "idCanto"])
class Consegnato {

    var idConsegnato: Int = 0

    var idCanto: Int = 0

    var txtNota: String = StringUtils.EMPTY
    
    var numPassaggio: Int = -1

}
