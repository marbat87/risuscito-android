package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity

@Entity(primaryKeys = ["idIndice", "idCanto"])
class IndiceLiturgico {

    var idIndice: Int = 0

    var idCanto: Int = 0

}
