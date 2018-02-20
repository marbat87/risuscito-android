package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
open class Canto {

    @PrimaryKey
    var id: Int = 0

    var pagina: Int = 0

    var titolo: String? = null

    var source: String? = null

    var favorite: Int = 0

    var color: String? = null

    var link: String? = null

    var zoom: Int = 0

    var scrollX: Int = 0

    var scrollY: Int = 0

    var savedTab: String? = null

    var savedBarre: String? = null

    var savedSpeed: String? = null

}
