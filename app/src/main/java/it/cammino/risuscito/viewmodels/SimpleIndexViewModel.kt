package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.mikepenz.fastadapter.IItem
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.InsertItem
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.items.insertItem
import it.cammino.risuscito.items.simpleItem

class SimpleIndexViewModel(application: Application, args: Bundle) : GenericIndexViewModel(application) {

    var itemsResult: LiveData<List<SimpleItem>>? = null
        private set
    var titoliList: ArrayList<IItem<*>> = ArrayList()

    //-1 come valore per indicare che non è mai stato settato ancora (fragment appena creato)
    var tipoLista: Int = -1
    var advancedSearch: Boolean = false
    var consegnatiOnly: Boolean = false
    var insertItemsResult: LiveData<List<InsertItem>>? = null
        private set
    lateinit var aTexts: Array<Array<String?>>

    init {
        tipoLista = args.getInt(Utility.TIPO_LISTA)
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        when (tipoLista) {
            0, 1 ->
                itemsResult = mDb.cantoDao().liveAll.map { canti ->
                    val newList = ArrayList<SimpleItem>()
                    canti.forEach {
                        newList.add(
                                simpleItem {
                                    setTitle = LUtils.getResId(it.titolo, R.string::class.java)
                                    setPage = LUtils.getResId(it.pagina, R.string::class.java)
                                    setSource = LUtils.getResId(it.source, R.string::class.java)
                                    setColor = it.color
                                    id = it.id
                                    undecodedSource = it.source
                                }
                        )
                    }
                    newList
                }
            2 ->
                itemsResult = mDb.salmiDao().liveAll.map { canti ->
                    val newList = ArrayList<SimpleItem>()
                    canti.forEach {
                        newList.add(
                                simpleItem {
                                    setTitle = LUtils.getResId(it.titoloSalmo, R.string::class.java)
                                    setPage = LUtils.getResId(it.pagina, R.string::class.java)
                                    setSource = LUtils.getResId(it.source, R.string::class.java)
                                    setColor = it.color
                                    id = it.id
                                    setNumSalmo = it.numSalmo
                                }
                        )
                    }
                    newList
                }
            3 ->
                insertItemsResult = mDb.consegnatiDao().liveChoosen.map { canti ->
                    val newList = ArrayList<InsertItem>()
                    canti.forEach {
                        newList.add(
                                insertItem {
                                    setTitle = LUtils.getResId(it.titolo, R.string::class.java)
                                    setPage = LUtils.getResId(it.pagina, R.string::class.java)
                                    setSource = LUtils.getResId(it.source, R.string::class.java)
                                    setColor = it.color
                                    id = it.id
                                    undecodedSource = it.source ?: ""
                                    consegnato = it.consegnato
                                }
                        )
                    }
                    newList
                }
        }
    }

}
