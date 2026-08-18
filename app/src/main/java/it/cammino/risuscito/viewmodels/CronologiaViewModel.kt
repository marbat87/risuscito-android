package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.risuscitoListItem
import it.cammino.risuscito.utils.Utility

class CronologiaViewModel(application: Application) : DialogManagerViewModel(application) {

    val viewMode = mutableStateOf(ViewMode.VIEW)

    var cronologiaCanti: LiveData<List<RisuscitoListItem>>? = null
        private set

    val historySortedResult = MutableLiveData(emptyList<RisuscitoListItem>())

    init {
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        cronologiaCanti = mDb.cronologiaDao().liveCronologia().map { canti ->
            val newList = ArrayList<RisuscitoListItem>()
            canti.forEach {
                newList.add(
                    risuscitoListItem(
                        titleRes = Utility.getResId(it.titolo, R.string::class.java),
                        timestamp = it.ultimaVisita?.time.toString()
                    ) {
                        pageRes = Utility.getResId(
                            it.pagina,
                            R.string::class.java
                        )
                        sourceRes = Utility.getResId(it.source, R.string::class.java)
                        setColor = it.color
                        id = it.id
                    }
                )
            }
            newList
        }
    }

    enum class ViewMode {
        VIEW,
        EMPTY
    }

}
