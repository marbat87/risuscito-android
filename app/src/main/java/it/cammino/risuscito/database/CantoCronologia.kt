package it.cammino.risuscito.database

import java.sql.Date

import it.cammino.risuscito.database.entities.Canto

class CantoCronologia : Canto() {

    var ultimaVisita: Date? = null

}
