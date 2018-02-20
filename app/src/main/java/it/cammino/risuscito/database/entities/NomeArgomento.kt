package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class NomeArgomento {

    @PrimaryKey
    var idArgomento: Int = 0

    var nomeArgomento: String? = null

}
