package it.cammino.risuscito.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

import java.util.ArrayList

import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.SimpleItem

open class GenericIndexViewModel(application: Application) : AndroidViewModel(application) {

    var idDaAgg: Int = 0
    var idListaDaAgg: Int = 0
    var posizioneDaAgg: Int = 0
    var idListaClick: Int = 0
    var idPosizioneClick: Int = 0
    var titoli: ArrayList<SimpleItem> = ArrayList()
    internal var mDb: RisuscitoDatabase? = null
}
