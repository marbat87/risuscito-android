package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.risuscitoListItem
import it.cammino.risuscito.utils.Utility

class ConsegnatiViewModel(application: Application) : DialogManagerViewModel(application) {

    val viewMode = mutableStateOf(ViewMode.VIEW)

    var consegnatiList: LiveData<List<RisuscitoListItem>>? = null

    val consegnatiSortedList = MutableLiveData(emptyList<RisuscitoListItem>())

    val passaggiSelectedList = MutableLiveData(emptyList<Int>())

    val consegnatiFullList = MutableLiveData(emptyList<RisuscitoListItem>())

    val consegnatiSelectedList = MutableLiveData(emptyList<Int>())

    var mIdConsegnatoSelected: Int = 0
    var mIdCantoSelected: Int = 0

    init {
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        consegnatiList = mDb.consegnatiDao().liveConsegnati().map { canti ->
            val newList = ArrayList<RisuscitoListItem>()
            canti.forEach {
                newList.add(
                    risuscitoListItem(
                        titleRes = Utility.getResId(it.titolo, R.string::class.java),
                        numPassaggio = it.numPassaggio
                    ) {
                        pageRes = Utility.getResId(
                            it.pagina,
                            R.string::class.java
                        )
                        sourceRes = Utility.getResId(it.source, R.string::class.java)
                        setColor = it.color ?: Canto.BIANCO
                        id = it.id
                        idConsegnato = it.consegnato
                    }
                )
            }
            newList
        }
    }

    enum class ViewMode {
        EDIT,
        LOADING,
        VIEW,
        EMPTY
    }

}
