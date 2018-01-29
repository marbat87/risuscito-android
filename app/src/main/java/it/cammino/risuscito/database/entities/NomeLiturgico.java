package it.cammino.risuscito.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class NomeLiturgico {

    @PrimaryKey
    public int idIndice;

    public String nome;

}
