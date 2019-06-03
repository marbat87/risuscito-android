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
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.PosizioneTitleItem
import it.cammino.risuscito.utils.zipLiveData


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
            listaPersonalizzataResult = Transformations.map(zipLiveData(liveList, mDb.cantoDao().liveAll)) { results ->
                val mPosizioniList = ArrayList<ListaPersonalizzataItem>()
                listaPersonalizzata = results.first.lista
                listaPersonalizzataTitle = results.first.titolo

                listaPersonalizzata?.let { lista ->
                    for (cantoIndex in 0 until lista.numPosizioni) {
                        val list = ArrayList<PosizioneItem>()
                        if (lista.getCantoPosizione(cantoIndex).isNotEmpty()) {

                            val cantoTemp = results.second.find {
                                it.id == Integer.parseInt(
                                        lista.getCantoPosizione(cantoIndex))
                            }

                            list.add(
                                    PosizioneItem().apply {
                                        withTitle(LUtils.getResId(cantoTemp?.titolo, R.string::class.java))
                                        withPage(LUtils.getResId(cantoTemp?.pagina, R.string::class.java))
                                        withSource(LUtils.getResId(cantoTemp?.source, R.string::class.java))
                                        withColor(cantoTemp?.color ?: Canto.BIANCO)
                                        withId(cantoTemp?.id ?: 0)
                                        withTimestamp("")
                                    })
                        }

                        Log.d(TAG, "cantoIndex: $cantoIndex")
                        val result = ListaPersonalizzataItem().apply {
                            withTitleItem(PosizioneTitleItem(
                                    lista.getNomePosizione(cantoIndex),
                                    cantoIndex,
                                    cantoIndex,
                                    false))
                            withListItem(list)
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
