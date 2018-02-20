package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class LocalLink {

    @PrimaryKey
    var idCanto: Int = 0

    var localPath: String? = null

}
