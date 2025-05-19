package it.cammino.risuscito.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class NomeGruppoLiturgico {

    @PrimaryKey
    var idGruppo: Int = 0

    var nomeGruppo: String? = null

    companion object {
        fun defaultData(): ArrayList<NomeGruppoLiturgico> {
            val mList = ArrayList<NomeGruppoLiturgico>()

            var mDato = NomeGruppoLiturgico()
            mDato.idGruppo = 1
            mDato.nomeGruppo = "gruppo_eucarestia"
            mList.add(mDato)

            mDato = NomeGruppoLiturgico()
            mDato.idGruppo = 2
            mDato.nomeGruppo = "gruppo_tempi"
            mList.add(mDato)

            mDato = NomeGruppoLiturgico()
            mDato.idGruppo = 3
            mDato.nomeGruppo = "gruppo_altro"
            mList.add(mDato)

            return mList
        }
    }
}
