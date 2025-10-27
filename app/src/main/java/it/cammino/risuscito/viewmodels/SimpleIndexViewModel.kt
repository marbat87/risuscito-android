package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.ExpandableItemType
import it.cammino.risuscito.items.RisuscitoListItem
import it.cammino.risuscito.items.risuscitoListItem
import it.cammino.risuscito.utils.Utility

class SimpleIndexViewModel(application: Application, args: Bundle) :
    GenericIndexViewModel(application) {

    var itemsResult: LiveData<List<RisuscitoListItem>>? = null
        private set

    var sectionedItemsResult: LiveData<Map<Int, List<RisuscitoListItem>>>? = null
            private set

    var modelSectionedItemsResult = MutableLiveData(mutableMapOf<Int, List<RisuscitoListItem>>())

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
            4 ->
                sectionedItemsResult = mDb.indiceLiturgicoDao().liveAll().map { canti ->
                    val finalMap = mutableMapOf<Int, List<RisuscitoListItem>>()
                    val cantiList = ArrayList<RisuscitoListItem>()
                    val cantiSubItemList = ArrayList<RisuscitoListItem>()
                    var totCanti = 0
                    var totGruppi = 0
                    var ultimoGruppo = 0

                    for (i in canti.indices) {
//                AGGIUNTA RIGA DI GRUPPO
                        cantiSubItemList.add(
                            risuscitoListItem(
                                itemType = ExpandableItemType.SUBITEM,
                                titleRes = Utility.getResId(canti[i].titolo, R.string::class.java)
                            ) {
                                pageRes = Utility.getResId(
                                    canti[i].pagina,
                                    R.string::class.java
                                )
                                sourceRes = Utility.getResId(canti[i].source, R.string::class.java)
                                setColor = canti[i].color
                                id = canti[i].id
                                groupIndex = totGruppi
                            }
                        )

                        if ((i == (canti.size - 1) || canti[i].idIndice != canti[i + 1].idIndice)) {
                            cantiList.add(
                                risuscitoListItem(
                                    itemType = ExpandableItemType.EXPANDABLE,
                                    titleRes = Utility.getResId(canti[i].nome, R.string::class.java)
                                ) {
                                    pageRes = Utility.getResId(
                                        canti[i].pagina,
                                        R.string::class.java
                                    )
                                    sourceRes =
                                        Utility.getResId(canti[i].source, R.string::class.java)
                                    setColor = canti[i].color
                                    identifier = totCanti++
                                    groupIndex = totGruppi++
                                    subCantiCounter = cantiSubItemList.size
                                }
                            )

                            cantiSubItemList.forEach { subitem ->
                                subitem.identifier = totCanti++
                                cantiList.add(subitem)
                            }
                            cantiSubItemList.clear()

                        }

                        if (i == canti.size - 1 || canti[i].idGruppo != canti[i+1].idGruppo) {
                            ultimoGruppo = canti[i].idGruppo
                            finalMap[Utility.getResId(
                                canti[i].nomeGruppo,
                                R.string::class.java
                            )] = ArrayList(cantiList)
                            cantiList.clear()
                        }

                    }
                    finalMap
                }
        }
    }

}
