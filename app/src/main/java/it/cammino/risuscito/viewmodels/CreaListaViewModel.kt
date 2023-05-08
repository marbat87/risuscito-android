package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.risuscito.ui.activity.CreaListaActivity
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.SwipeableItem
import it.cammino.risuscito.utils.StringUtils

class CreaListaViewModel(application: Application, args: Bundle) : AndroidViewModel(application) {

    var positionToRename = 0

    var tempTitle = StringUtils.EMPTY

    var elementi: ArrayList<SwipeableItem>? = null

    var idModifica: Int = -1
    var listaResult: LiveData<ListaPers>? = null
        private set

    init {
        idModifica = args.getInt(CreaListaActivity.ID_DA_MODIF)
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        listaResult = mDb.listePersDao().getLiveListById(idModifica)
    }

}
