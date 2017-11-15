package it.cammino.risuscito.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Canto {

    @PrimaryKey
    public int id;

    public int pagina;

    public String titolo;

    public String source;

    public int favorite;

    public String color;

    public String link;

    public int zoom;

    public int scrollX;

    public int scrollY;

    public String savedTab;

    public String savedBarre;

    public String savedSpeed;

}
