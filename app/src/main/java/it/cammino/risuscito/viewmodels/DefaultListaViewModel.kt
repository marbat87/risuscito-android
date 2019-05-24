package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

import it.cammino.risuscito.database.Posizione
import it.cammino.risuscito.database.RisuscitoDatabase


class DefaultListaViewModel(application: Application, args: Bundle) : AndroidViewModel(application) {

    var cantiResult: LiveData<List<Posizione>>? = null
        private set
    var defaultListaId: Int = -1

    init {
        defaultListaId = args.getInt("tipoLista")
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        cantiResult = mDb.customListDao().getList(defaultListaId)
    }

}
