package it.cammino.risuscito.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Salmo {

    @PrimaryKey
    public int id;

    public String numSalmo;

    public String titoloSalmo;

}
