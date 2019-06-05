package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.items.SimpleHistoryItem
import it.cammino.risuscito.items.simpleHistoryItem
import java.util.*

class CronologiaViewModel(application: Application) : AndroidViewModel(application) {

    var cronologiaCanti: LiveData<List<SimpleHistoryItem>>? = null
        private set

    init {
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        cronologiaCanti = Transformations.map(mDb.cronologiaDao().liveCronologia) { canti ->
            val newList = ArrayList<SimpleHistoryItem>()
            canti.forEach {
                newList.add(
                        simpleHistoryItem {
                            withTitle(LUtils.getResId(it.titolo, R.string::class.java))
                            withPage(LUtils.getResId(it.pagina, R.string::class.java))
                            withSource(LUtils.getResId(it.source, R.string::class.java))
                            withColor(it.color ?: Canto.BIANCO)
                            withId(it.id)
                            withTimestamp(it.ultimaVisita?.time.toString())
                        }
                )
            }
            newList
        }
    }

}
