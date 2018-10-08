package it.cammino.risuscito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class LocalLink {

    @PrimaryKey
    var idCanto: Int = 0

    var localPath: String? = null

}
