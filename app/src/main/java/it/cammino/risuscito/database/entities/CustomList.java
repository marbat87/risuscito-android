package it.cammino.risuscito.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.sql.Date;

@Entity(primaryKeys = {"id", "position", "idCanto"})
public class CustomList {

  public int id;

  public int position;

  public int idCanto;

  public Date timestamp;
}
