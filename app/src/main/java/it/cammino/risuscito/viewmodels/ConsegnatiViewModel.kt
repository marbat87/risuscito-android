package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.CheckableItem
import it.cammino.risuscito.items.CheckablePassageItem
import it.cammino.risuscito.items.NotableItem
import it.cammino.risuscito.items.notableItem
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.useOldIndex

class ConsegnatiViewModel(application: Application) : AndroidViewModel(application) {

    var editMode: MutableLiveData<Boolean> = MutableLiveData(false)

    var titoliChoose: List<CheckableItem> = ArrayList()
    var titoliChooseFiltered: List<CheckableItem> = ArrayList()

    var titoli: List<NotableItem> = ArrayList()
    var passaggi = ArrayList<CheckablePassageItem>()

    var mIndexResult: LiveData<List<NotableItem>>? = null

    var mIdConsegnatoSelected: Int = 0
    var mIdCantoSelected: Int = 0

    init {
        val useOldIndex = application.useOldIndex()
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        mIndexResult = mDb.consegnatiDao().liveConsegnati().map { canti ->
            val newList = ArrayList<NotableItem>()
            canti.forEach {
                newList.add(
                    notableItem {
                        setTitle = Utility.getResId(it.titolo, R.string::class.java)
                        setPage = Utility.getResId(
                            if (useOldIndex) it.pagina + Utility.OLD_PAGE_SUFFIX else it.pagina,
                            R.string::class.java
                        )
                        setSource = Utility.getResId(it.source, R.string::class.java)
                        setColor = it.color
                        id = it.id
                        idConsegnato = it.consegnato
                        numPassaggio = it.numPassaggio
                    }
                )
            }
            newList
        }
    }

}
