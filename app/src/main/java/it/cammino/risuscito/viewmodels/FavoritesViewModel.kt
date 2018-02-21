package it.cammino.risuscito.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.items.SimpleItem
import java.util.*

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    var titoli: ArrayList<SimpleItem> = ArrayList()
    var mFavoritesResult: LiveData<List<Canto>>? = null
        private set
    private var mDb: RisuscitoDatabase? = null

    fun createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication())
        // Receive changes
        subscribeToDbChanges()
    }

    private fun subscribeToDbChanges() {
        mFavoritesResult = mDb!!.favoritesDao().liveFavorites
    }
}
