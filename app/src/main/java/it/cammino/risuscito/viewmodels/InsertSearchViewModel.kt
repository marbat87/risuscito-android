package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import it.cammino.risuscito.database.CantoConsegnato
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.InsertItem
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import java.util.*

class InsertSearchViewModel(application: Application) : AndroidViewModel(application) {

    var titoli: List<InsertItem> = ArrayList()
    internal var mDb: RisuscitoDatabase? = null
    var itemsResult: LiveData<List<InsertItem>>? = null
        private set

    fun createDb() {
        mDb = RisuscitoDatabase.getInstance(getApplication())
        // Receive changes
        subscribeToDbChanges()
    }

    private fun subscribeToDbChanges() {
        itemsResult = Transformations.map(mDb!!.consegnatiDao().liveChoosen) { canti ->
            val newList = ArrayList<InsertItem>()
            canti.forEach {
                newList.add(
                        InsertItem()
                                .withTitle(LUtils.getResId(it.titolo, R.string::class.java))
                                .withPage(LUtils.getResId(it.pagina, R.string::class.java))
                                .withSource(LUtils.getResId(it.source, R.string::class.java))
                                .withColor(it.color!!)
                                .withId(it.id)
                                .withUndecodedSource(it.source ?: "")
                                .withConsegnato(it.consegnato)
                )
            }
            newList
        }
    }
}
