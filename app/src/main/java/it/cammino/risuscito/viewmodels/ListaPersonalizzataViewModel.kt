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
import it.cammino.risuscito.database.RisuscitoDatabase
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
        listaPersonalizzataId = args.getInt("tipoLista")
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        listaPersonalizzataResult = Transformations.map(zipLiveData(mDb.listePersDao().getLiveListById(listaPersonalizzataId)!!, mDb.cantoDao().liveAll)) { results ->
            val mPosizioniList = ArrayList<ListaPersonalizzataItem>()
            listaPersonalizzata = results.first.lista
            listaPersonalizzataTitle = results.first.titolo

            for (cantoIndex in 0 until listaPersonalizzata!!.numPosizioni) {
                val list = ArrayList<PosizioneItem>()
                if (listaPersonalizzata!!.getCantoPosizione(cantoIndex).isNotEmpty()) {

                    val cantoTemp = results.second.find {
                        it.id == Integer.parseInt(
                                listaPersonalizzata!!.getCantoPosizione(cantoIndex))
                    }

                    list.add(
                            PosizioneItem()
                                    .withTitle(LUtils.getResId(cantoTemp!!.titolo, R.string::class.java))
                                    .withPage(LUtils.getResId(cantoTemp.pagina, R.string::class.java))
                                    .withSource(LUtils.getResId(cantoTemp.source, R.string::class.java))
                                    .withColor(cantoTemp.color!!)
                                    .withId(cantoTemp.id)
                                    .withTimestamp(""))
                }

                Log.d(TAG, "cantoIndex: $cantoIndex")
                val result = ListaPersonalizzataItem()
                        .withTitleItem(PosizioneTitleItem(
                                listaPersonalizzata!!.getNomePosizione(cantoIndex),
                                cantoIndex,
                                cantoIndex,
                                false))
                        .withListItem(list)
                        .withId(cantoIndex)


                mPosizioniList.add(result)
            }
            mPosizioniList
        }
    }

    companion object {
        internal val TAG = ListaPersonalizzataViewModel::class.java.canonicalName
    }
}
