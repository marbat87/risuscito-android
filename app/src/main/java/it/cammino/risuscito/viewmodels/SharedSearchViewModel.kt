package it.cammino.risuscito.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.cammino.risuscito.items.RisuscitoListItem

class SharedSearchViewModel : ViewModel() {

    var itemsResultFiltered: MutableLiveData<List<RisuscitoListItem>> = MutableLiveData(emptyList())

    var advancedSearchFilter: MutableLiveData<Boolean> = MutableLiveData(false)

    var searchFilter: MutableLiveData<String> = MutableLiveData("")

    lateinit var aTexts: Array<Array<String?>>

    var titoli: List<RisuscitoListItem> = ArrayList()
}