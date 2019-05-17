package it.cammino.risuscito.viewmodels

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import it.cammino.risuscito.ListaPersonalizzata
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import it.cammino.risuscito.items.ListaPersonalizzataItem
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.PosizioneTitleItem
import it.cammino.risuscito.utils.ioThread


class ListaPersonalizzataViewModel(application: Application, args: Bundle) : AndroidViewModel(application) {

    var posizioniList: List<ListaPersonalizzataItem> = ArrayList()
    var listaPersonalizzataId: Int = 0
    var listaPersonalizzata: ListaPersonalizzata? = null
    var listaPersonalizzataTitle: String? = null

    var listaPersonalizzataResult: MutableLiveData<MutableList<ListaPersonalizzataItem>> = MutableLiveData()
        private set

    private var listaPersonalizzataMediator: MediatorLiveData<ListaPers> = MediatorLiveData()

    init {
        listaPersonalizzataId = args.getInt("tipoLista")
        val mDb = RisuscitoDatabase.getInstance(getApplication())
        val listaPers = mDb.listePersDao().getLiveListById(listaPersonalizzataId)
        listaPersonalizzataMediator.addSource(listaPers!!) { mListaPers ->
            listaPersonalizzataMediator.postValue(mListaPers)
        }
        listaPersonalizzataMediator.observeForever { mListaPers ->
            if (mListaPers == null) {
                listaPersonalizzataResult.postValue(ArrayList())
                return@observeForever
            }
            listaPersonalizzata = mListaPers.lista
            listaPersonalizzataTitle = mListaPers.titolo
            ioThread {
                val mPosizioniList = ArrayList<ListaPersonalizzataItem>()

                for (cantoIndex in 0 until listaPersonalizzata!!.numPosizioni) {
                    val list = ArrayList<PosizioneItem>()
                    if (listaPersonalizzata!!.getCantoPosizione(cantoIndex).isNotEmpty()) {

                        val mCantoDao = mDb.cantoDao()

                        val cantoTemp = mCantoDao.getCantoById(
                                Integer.parseInt(
                                        listaPersonalizzata!!.getCantoPosizione(cantoIndex)))

                        list.add(
                                PosizioneItem(
                                        cantoTemp.pagina!!,
                                        cantoTemp.titolo!!,
                                        cantoTemp.color!!,
                                        cantoTemp.id,
                                        cantoTemp.source!!,
                                        ""))
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
                listaPersonalizzataResult.postValue(mPosizioniList)
            }
        }
    }

    companion object {
        internal val TAG = ListaPersonalizzataViewModel::class.java.canonicalName
    }

}
