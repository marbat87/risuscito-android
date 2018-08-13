package it.cammino.risuscito.viewmodels

import android.app.Application
import android.arch.lifecycle.LiveData

import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.SalmoCanto

class SalmiIndexViewModel(application: Application) : GenericIndexViewModel(application) {

    var indexResult: LiveData<List<SalmoCanto>>? = null
        private set

    fun createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication())
        // Receive changes
        subscribeToDbChanges()
    }

    private fun subscribeToDbChanges() {
        indexResult = mDb!!.salmiDao().liveAll
    }
}
