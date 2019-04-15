package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.risuscito.database.CantoConsegnato
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.InsertItem
import java.util.*

class InsertSearchViewModel(application: Application) : AndroidViewModel(application) {

    var titoli: List<InsertItem> = ArrayList()
    internal var mDb: RisuscitoDatabase? = null
    var indexResult: LiveData<List<CantoConsegnato>>? = null
        private set

    fun createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication())
        // Receive changes
        subscribeToDbChanges()
    }

    private fun subscribeToDbChanges() {
        indexResult = mDb!!.consegnatiDao().liveChoosen
    }
}
