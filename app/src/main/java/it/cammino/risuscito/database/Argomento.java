package it.cammino.risuscito.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(primaryKeys = {"idArgomento", "idCanto"})
public class Argomento {

    public int idArgomento;

    public int idCanto;

}
