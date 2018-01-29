package it.cammino.risuscito.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.sql.Date;

@Entity
public class Cronologia {

    @PrimaryKey
    public int idCanto;

    public Date ultimaVisita = new Date(System.currentTimeMillis());

}
