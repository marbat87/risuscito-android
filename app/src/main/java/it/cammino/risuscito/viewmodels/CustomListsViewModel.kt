package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

import it.cammino.risuscito.objects.ListaPersonalizzata
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers

class CustomListsViewModel(application: Application) : AndroidViewModel(application) {

    var indDaModif = 0

    var idDaCanc: Int = 0

    var listaDaCanc: Int = 0

    var titoloDaCanc: String? = null

    var indexToShow = 0

    var celebrazioneDaCanc: ListaPersonalizzata? = null

    var customListResult: LiveData<List<ListaPers>>? = null
        private set

    init {
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        customListResult = mDb.listePersDao().liveAll
    }

}
