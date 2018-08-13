package it.cammino.risuscito.database

import it.cammino.risuscito.database.entities.Canto

class Posizione : Canto() {

    var timestamp: java.sql.Date? = null

    var position: Int = 0

}
