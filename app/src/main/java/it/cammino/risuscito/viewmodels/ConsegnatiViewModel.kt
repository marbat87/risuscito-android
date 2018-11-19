package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.items.CheckableItem
import it.cammino.risuscito.items.SimpleItem
import java.util.*

class ConsegnatiViewModel(application: Application) : AndroidViewModel(application) {

    var editMode: Boolean = false

    var titoliChoose: List<CheckableItem> = ArrayList()
    var titoliChooseFiltered: List<CheckableItem> = ArrayList()

    var titoli: List<SimpleItem> = ArrayList()

    private var mIndexResult: LiveData<List<Canto>>? = null

    private var mDb: RisuscitoDatabase? = null

    val indexResult: LiveData<List<Canto>>
        get() {
            if (mIndexResult == null) mIndexResult = MutableLiveData()
            return mIndexResult!!
        }

    fun createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication())
        // Receive changes
        subscribeToDbChanges()
    }

    private fun subscribeToDbChanges() {
        mIndexResult = mDb!!.consegnatiDao().liveConsegnati
    }
}
