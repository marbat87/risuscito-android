package it.cammino.risuscito.database.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import it.cammino.risuscito.ListaPersonalizzata;

@Entity
public class ListaPers {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String titolo;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public ListaPersonalizzata lista;
}
