package it.cammino.risuscito.database.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import it.cammino.risuscito.ListaPersonalizzata

@Entity
class ListaPers {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var titolo: String? = null

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var lista: ListaPersonalizzata? = null
}
