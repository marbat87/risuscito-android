package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData

import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto

class SearchViewModel(application: Application) : GenericIndexViewModel(application) {

    var indexResult: LiveData<List<Canto>>? = null
        private set

    fun createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication())
        // Receive changes
        subscribeToDbChanges()
    }

    private fun subscribeToDbChanges() {
        indexResult = mDb!!.cantoDao().liveAll
    }
}
