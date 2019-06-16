package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
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
import it.cammino.risuscito.utils.zipLiveDataNullable


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
            listaPersonalizzataResult = Transformations.map(zipLiveDataNullable(liveList, mDb.cantoDao().liveAll)) { result ->
                //            listaPersonalizzataResult = liveList.combine(mDb.cantoDao().liveAll) { listaPers, cantiList ->
                val mPosizioniList = ArrayList<ListaPersonalizzataItem>()
                listaPersonalizzata = result.first?.lista
                listaPersonalizzataTitle = result.first?.titolo

                listaPersonalizzata?.let { lista ->
                    for (cantoIndex in 0 until lista.numPosizioni) {
                        val list = ArrayList<PosizioneItem>()
                        if (lista.getCantoPosizione(cantoIndex).isNotEmpty()) {
                            result.second?.find {
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
                        val listaResult = listaPersonalizzataItem {
                            posizioneTitleItem {
                                titoloPosizione = lista.getNomePosizione(cantoIndex)
                                idPosizione = cantoIndex
                                tagPosizione = cantoIndex
                            }
                            listItem = list
                            id = cantoIndex
                        }

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
