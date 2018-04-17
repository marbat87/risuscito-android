package it.cammino.risuscito.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class NomeArgomento {

    @PrimaryKey
    var idArgomento: Int = 0

    var nomeArgomento: String? = null

    companion object {
        fun defaultNomeArgData(): ArrayList<NomeArgomento> {
            val argNamesList = ArrayList<NomeArgomento>()

            var mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 1
            mNomeArgomento.nomeArgomento = "Canti_della_Bibbia_Antico_Testamento"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 2
            mNomeArgomento.nomeArgomento = "Canti_della_Bibbia_Nuovo_Testamento"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 3
            mNomeArgomento.nomeArgomento = "Canti_dalle_Odi_di_Salomone"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 4
            mNomeArgomento.nomeArgomento = "Ispirati_a_melodie_e_rituali_ebraici"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 5
            mNomeArgomento.nomeArgomento = "Canti_per_bambini"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 6
            mNomeArgomento.nomeArgomento = "Dall_ordinario_della_Messa"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 7
            mNomeArgomento.nomeArgomento = "Canti_per_la_frazione_del_pane"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 8
            mNomeArgomento.nomeArgomento = "Dalla_liturgia_della_veglia_pasquale"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 9
            mNomeArgomento.nomeArgomento = "Canti_per_il_sacramento_della_riconciliazione"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 10
            mNomeArgomento.nomeArgomento = "Inni_liturgici"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 11
            mNomeArgomento.nomeArgomento = "Canti_a_Maria"
            argNamesList.add(mNomeArgomento)

            mNomeArgomento = NomeArgomento()
            mNomeArgomento.idArgomento = 12
            mNomeArgomento.nomeArgomento = "Vari"
            argNamesList.add(mNomeArgomento)

            return argNamesList
        }
    }

}
