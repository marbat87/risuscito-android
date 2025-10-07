package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.pojo.Posizione
import it.cammino.risuscito.items.ListaPersonalizzataPositionListItem
import it.cammino.risuscito.utils.Utility


class DefaultListaViewModel(application: Application, args: Bundle) : DialogManagerViewModel(application) {

    var cantiResult: LiveData<List<Posizione>>? = null
        private set
    var defaultListaId: Int = -1

    var posizioniList = MutableLiveData(emptyList<ListaPersonalizzataPositionListItem>())

    init {
        defaultListaId = args.getInt(Utility.TIPO_LISTA)
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        cantiResult = mDb.customListDao().getList(defaultListaId)
    }

}
