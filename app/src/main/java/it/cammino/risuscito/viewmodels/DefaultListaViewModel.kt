package it.cammino.risuscito.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

import it.cammino.risuscito.database.Posizione
import it.cammino.risuscito.database.RisuscitoDatabase


class DefaultListaViewModel(application: Application) : AndroidViewModel(application) {

    var cantiResult: LiveData<List<Posizione>>? = null
        private set

    var defaultListaId: Int = 0

    private var mDb: RisuscitoDatabase? = null

    fun createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication())
        // Receive changes
        subscribeToDbChanges()
    }

    private fun subscribeToDbChanges() {
        cantiResult = mDb!!.customListDao().getList(defaultListaId)
    }
}
