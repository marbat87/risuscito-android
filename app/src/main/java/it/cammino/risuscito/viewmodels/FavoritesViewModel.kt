package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.risuscitoListItem
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.useOldIndex

class FavoritesViewModel(application: Application) : DialogManagerViewModel(application) {

    var mFavoritesResult: LiveData<List<RisuscitoListItem>>? = null
        private set

    val mFavoritesSortedResult = MutableLiveData(emptyList<RisuscitoListItem>())

    init {
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        val useOldIndex = application.useOldIndex()
        mFavoritesResult = mDb.favoritesDao().liveFavorites().map { canti ->
            val newList = ArrayList<RisuscitoListItem>()
            canti.forEach {
                newList.add(
                    risuscitoListItem {
                        titleRes = Utility.getResId(it.titolo, R.string::class.java)
                        pageRes = Utility.getResId(
                            if (useOldIndex) it.pagina + Utility.OLD_PAGE_SUFFIX else it.pagina,
                            R.string::class.java
                        )
                        sourceRes = Utility.getResId(it.source, R.string::class.java)
                        setColor = it.color ?: Canto.BIANCO
                        id = it.id
                    }
                )
            }
            newList
        }
    }

}
