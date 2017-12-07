package it.cammino.risuscito.database.entities;

import android.arch.persistence.room.Entity;

@Entity(primaryKeys = {"idArgomento", "idCanto"})
public class Argomento {

    public int idArgomento;

    public int idCanto;

}
