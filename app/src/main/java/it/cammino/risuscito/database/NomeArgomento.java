package it.cammino.risuscito.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class NomeArgomento {

    @PrimaryKey
    public int idArgomento;

    public String nomeArgomento;

}
