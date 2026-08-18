package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.SwipeableRisuscitoListItem
import it.cammino.risuscito.ui.activity.CreaListaActivity

class CreaListaViewModel(application: Application, args: Bundle) : DialogManagerViewModel(application) {

    var positionToRename = 0

    var tempTitle = mutableStateOf("")


    var idModifica: Int = -1
    var listaResult: LiveData<ListaPers>? = null
        private set

    val elementi = MutableLiveData(emptyList<SwipeableRisuscitoListItem>())

    init {
        idModifica = args.getInt(CreaListaActivity.ID_DA_MODIF)
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        listaResult = mDb.listePersDao().getLiveListById(idModifica)
    }

}
