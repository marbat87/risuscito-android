package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import io.lamart.livedata.utils.combine
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.ListaPersonalizzata
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.items.ListaPersonalizzataItem
import it.cammino.risuscito.items.listaPersonalizzataItem
import it.cammino.risuscito.items.posizioneTitleItem
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.posizioneItem


class ListaPersonalizzataViewModel(application: Application, args: Bundle) : AndroidViewModel(application) {

    var posizioniList: List<ListaPersonalizzataItem> = ArrayList()
    var listaPersonalizzataId: Int = 0
    var listaPersonalizzata: ListaPersonalizzata? = null
    var listaPersonalizzataTitle: String? = null

    var listaPersonalizzataResult: LiveData<List<ListaPersonalizzataItem>>? = null
        private set

    init {
        listaPersonalizzataId = args.getInt(Utility.TIPO_LISTA)
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        mDb.listePersDao().getLiveListById(listaPersonalizzataId)?.let { liveList ->
            listaPersonalizzataResult = liveList.combine(mDb.cantoDao().liveAll) { listaPers, cantiList ->
                val mPosizioniList = ArrayList<ListaPersonalizzataItem>()
                listaPersonalizzata = listaPers.lista
                listaPersonalizzataTitle = listaPers.titolo

                listaPersonalizzata?.let { lista ->
                    for (cantoIndex in 0 until lista.numPosizioni) {
                        val list = ArrayList<PosizioneItem>()
                        if (lista.getCantoPosizione(cantoIndex).isNotEmpty()) {
                            cantiList.find {
                                it.id == Integer.parseInt(
                                        lista.getCantoPosizione(cantoIndex))
                            }?.let {
                                list.add(
                                        posizioneItem {
                                            withTitle(LUtils.getResId(it.titolo, R.string::class.java))
                                            withPage(LUtils.getResId(it.pagina, R.string::class.java))
                                            withSource(LUtils.getResId(it.source, R.string::class.java))
                                            withColor(it.color ?: Canto.BIANCO)
                                            withId(it.id)
                                            withTimestamp("")
                                        })
                            }
                        }

                        Log.d(TAG, "cantoIndex: $cantoIndex")
                        val result = listaPersonalizzataItem {
                            posizioneTitleItem {
                                titoloPosizione = lista.getNomePosizione(cantoIndex)
                                idPosizione = cantoIndex
                                tagPosizione = cantoIndex
                            }
                            listItem = list
                            withId(cantoIndex)
                        }

                        mPosizioniList.add(result)
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
