package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class NomeLiturgico {

    @PrimaryKey
    var idIndice: Int = 0

    var nome: String? = null

}
