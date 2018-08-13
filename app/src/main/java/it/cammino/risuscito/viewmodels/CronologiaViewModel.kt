package it.cammino.risuscito.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

import java.util.ArrayList

import it.cammino.risuscito.database.CantoCronologia
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.SimpleHistoryItem

class CronologiaViewModel(application: Application) : AndroidViewModel(application) {

    var titoli: ArrayList<SimpleHistoryItem> = ArrayList()
    var cronologiaCanti: LiveData<List<CantoCronologia>>? = null
        private set
    private var mDb: RisuscitoDatabase? = null

    fun createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication())
        // Receive changes
        subscribeToDbChanges()
    }

    private fun subscribeToDbChanges() {
        cronologiaCanti = mDb!!.cronologiaDao().liveCronologia
    }
}
