package it.cammino.risuscito.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import it.cammino.risuscito.ListaPersonalizzata

@Entity
class ListaPers {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var titolo: String? = null

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var lista: ListaPersonalizzata? = null
}
