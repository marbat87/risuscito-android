package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import it.cammino.risuscito.ListaPersonalizzata
import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.items.ListaPersonalizzataPositionListItem
import it.cammino.risuscito.items.ListaPersonalizzataRisuscitoListItem
import it.cammino.risuscito.items.listaPersonalizzataPositionListItem
import it.cammino.risuscito.items.listaPersonalizzataRisuscitoListItem
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.extension.zipLiveDataNullable


class ListaPersonalizzataViewModel(application: Application, args: Bundle) :
    DialogManagerViewModel(application) {

    var posizioniList = MutableLiveData(emptyList<ListaPersonalizzataPositionListItem>())
    var listaPersonalizzataId: Int = 0
    var listaPersonalizzata: ListaPersonalizzata? = null
    var listaPersonalizzataTitle: String? = null
    var posizioneDaCanc: Int = 0

    var listaPersonalizzataResult: LiveData<List<ListaPersonalizzataPositionListItem>>? = null
        private set

    init {
        listaPersonalizzataId = args.getInt(Utility.TIPO_LISTA)
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        mDb.listePersDao().getLiveListById(listaPersonalizzataId)?.let { liveList ->
            listaPersonalizzataResult =
                zipLiveDataNullable(liveList, mDb.cantoDao().liveAll()).map { result ->
                    val mPosizioniList = ArrayList<ListaPersonalizzataPositionListItem>()
                    listaPersonalizzata = result.first?.lista
                    listaPersonalizzataTitle = result.first?.titolo

                    listaPersonalizzata?.let { lista ->
                        for (cantoIndex in 0 until lista.numPosizioni) {
                            val list = ArrayList<ListaPersonalizzataRisuscitoListItem>()
                            if (lista.getCantoPosizione(cantoIndex).isNotEmpty()) {
                                result.second?.find {
                                    it.id == Integer.parseInt(
                                        lista.getCantoPosizione(cantoIndex)
                                    )
                                }?.let {
                                    list.add(
                                        listaPersonalizzataRisuscitoListItem(
                                            titleRes = Utility.getResId(
                                                it.titolo,
                                                R.string::class.java
                                            ),
                                            nota = lista.getNotaPosizione(cantoIndex),
                                            selected = false,
                                            timestamp = StringUtils.EMPTY
                                        ) {
                                            pageRes = Utility.getResId(
                                                it.pagina,
                                                R.string::class.java
                                            )
                                            sourceRes =
                                                Utility.getResId(it.source, R.string::class.java)
                                            setColor = it.color
                                            id = it.id
                                            idPosizione = cantoIndex
                                            tagPosizione = cantoIndex
                                            itemTag = 0
                                        }
                                    )
                                }
                            }

                            val listaResult = listaPersonalizzataPositionListItem(
                                titoloPosizione = lista.getNomePosizione(cantoIndex),
                                idPosizione = cantoIndex,
                                tagPosizione = cantoIndex,
                                isMultiple = false,
                                posizioni = list
                            )

//                            val listaResult = listaPersonalizzataItem {
//                                posizioneTitleItem {
//                                    titoloPosizione = lista.getNomePosizione(cantoIndex)
//                                    idPosizione = cantoIndex
//                                    tagPosizione = cantoIndex
//                                }
//                                listItem = list
//                                id = cantoIndex
//                            }

                            mPosizioniList.add(listaResult)
                        }
                    }
                    mPosizioniList
                }
        }
    }

    companion object {
        internal val TAG = ListaPersonalizzataViewModel::class.java.canonicalName
    }
}
