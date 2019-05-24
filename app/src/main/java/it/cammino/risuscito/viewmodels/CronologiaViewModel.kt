package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.risuscito.database.CantoCronologia
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.SimpleHistoryItem
import java.util.*

class CronologiaViewModel(application: Application) : AndroidViewModel(application) {

    var titoli: ArrayList<SimpleHistoryItem> = ArrayList()
    var cronologiaCanti: LiveData<List<CantoCronologia>>? = null
        private set

    init {
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        cronologiaCanti = mDb.cronologiaDao().liveCronologia
    }

}
