package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.pojo.Posizione


class DefaultListaViewModel(application: Application, args: Bundle) : AndroidViewModel(application) {

    var cantiResult: LiveData<List<Posizione>>? = null
        private set
    var defaultListaId: Int = -1

    init {
        defaultListaId = args.getInt(Utility.TIPO_LISTA)
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        cantiResult = mDb.customListDao().getList(defaultListaId)
    }

}
