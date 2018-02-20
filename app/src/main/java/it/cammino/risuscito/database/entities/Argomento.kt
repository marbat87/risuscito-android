package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity

@Entity(primaryKeys = ["idArgomento", "idCanto"])
class Argomento {

    var idArgomento: Int = 0

    var idCanto: Int = 0

}
