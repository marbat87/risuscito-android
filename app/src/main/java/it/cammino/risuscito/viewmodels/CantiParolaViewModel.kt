package it.cammino.risuscito.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

import it.cammino.risuscito.database.Posizione
import it.cammino.risuscito.database.RisuscitoDatabase


class CantiParolaViewModel(application: Application) : AndroidViewModel(application) {

    var cantiParolaResult: LiveData<List<Posizione>>? = null
        private set

    private var mDb: RisuscitoDatabase? = null

    fun createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication())
        // Receive changes
        subscribeToDbChanges()
    }

    private fun subscribeToDbChanges() {
        cantiParolaResult = mDb!!.customListDao().getList(1)
    }
}
