package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.risuscitoListItem
import it.cammino.risuscito.utils.Utility

class SimpleIndexViewModel(application: Application, args: Bundle) :
    GenericIndexViewModel(application) {

    var itemsResult: LiveData<List<RisuscitoListItem>>? = null
        private set

    //-1 come valore per indicare che non è mai stato settato ancora (fragment appena creato)
    var tipoLista: Int = -1

    init {
        tipoLista = args.getInt(Utility.TIPO_LISTA)
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        when (tipoLista) {
            0, 1 ->
                itemsResult = mDb.cantoDao().liveAll().map { canti ->
                    val newList = ArrayList<RisuscitoListItem>()
                    canti.forEach {
                        newList.add(
                            risuscitoListItem(
                                titleRes = Utility.getResId(it.titolo, R.string::class.java)
                            ) {
                                pageRes = Utility.getResId(
                                    it.pagina,
                                    R.string::class.java
                                )
                                sourceRes = Utility.getResId(it.source, R.string::class.java)
                                setColor = it.color
                                id = it.id
                                undecodedSource = it.source
                            }
                        )
                    }
                    newList
                }

            2 ->
                itemsResult = mDb.indiceBiblicoDao().liveAll().map { canti ->
                    val newList = ArrayList<RisuscitoListItem>()
                    canti.forEach {
                        newList.add(
                            risuscitoListItem(
                                titleRes = Utility.getResId(it.titoloIndice, R.string::class.java)
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

            3 ->
                itemsResult = mDb.consegnatiDao().liveChoosen().map { canti ->
                    val newList = ArrayList<RisuscitoListItem>()
                    canti.forEach {
                        newList.add(
                            risuscitoListItem(
                                    titleRes = Utility.getResId(it.titolo, R.string::class.java)
                        ) {
                                pageRes = Utility.getResId(
                                    it.pagina,
                                    R.string::class.java
                                )
                                sourceRes = Utility.getResId(it.source, R.string::class.java)
                                setColor = it.color
                                id = it.id
                                undecodedSource = it.source.orEmpty()
                                consegnato = it.consegnato
                            }
                        )
                    }
                    newList
                }
        }
    }

}
