package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.items.simpleItem
import it.cammino.risuscito.utils.Utility

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    var mFavoritesResult: LiveData<List<SimpleItem>>? = null
        private set

    init {
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        mFavoritesResult = mDb.favoritesDao().liveFavorites.map { canti ->
            val newList = ArrayList<SimpleItem>()
            canti.forEach {
                newList.add(
                    simpleItem {
                        setTitle = Utility.getResId(it.titolo, R.string::class.java)
                        setPage = Utility.getResId(it.pagina, R.string::class.java)
                        setSource = Utility.getResId(it.source, R.string::class.java)
                        setColor = it.color ?: Canto.BIANCO
                        id = it.id
                    }
                )
            }
            newList
        }
    }

}
