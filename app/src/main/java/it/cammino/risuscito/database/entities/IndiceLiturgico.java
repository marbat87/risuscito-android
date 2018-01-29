package it.cammino.risuscito.database.entities;

import android.arch.persistence.room.Entity;

@Entity(primaryKeys = {"idIndice", "idCanto"})
public class IndiceLiturgico {

    public int idIndice;

    public int idCanto;

}
