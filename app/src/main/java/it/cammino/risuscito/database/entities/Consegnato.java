package it.cammino.risuscito.database.entities;

import android.arch.persistence.room.Entity;

@Entity(primaryKeys = {"idConsegnato", "idCanto"})
public class Consegnato {

    public int idConsegnato;

    public int idCanto;

}
