package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.SimpleHistoryItem
import it.cammino.risuscito.items.simpleHistoryItem
import it.cammino.risuscito.utils.Utility

class CronologiaViewModel(application: Application) : AndroidViewModel(application) {

    var cronologiaCanti: LiveData<List<SimpleHistoryItem>>? = null
        private set

    init {
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        cronologiaCanti = mDb.cronologiaDao().liveCronologia.map { canti ->
            val newList = ArrayList<SimpleHistoryItem>()
            canti.forEach {
                newList.add(
                    simpleHistoryItem {
                        setTitle = Utility.getResId(it.titolo, R.string::class.java)
                        setPage = Utility.getResId(it.pagina, R.string::class.java)
                        setSource = Utility.getResId(it.source, R.string::class.java)
                        setColor = it.color
                        id = it.id
                        setTimestamp = it.ultimaVisita?.time.toString()
                    }
                )
            }
            newList
        }
    }

}
