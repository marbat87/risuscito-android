package it.cammino.risuscito.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class LocalLink {

    @PrimaryKey
    public int idCanto;

    public String localPath;

}
