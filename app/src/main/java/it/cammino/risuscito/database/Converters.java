package it.cammino.risuscito.database;

import android.arch.persistence.room.TypeConverter;

import java.sql.Date;

import it.cammino.risuscito.ListaPersonalizzata;

public class Converters {

    @TypeConverter
    public static ListaPersonalizzata fromBlob(byte[] blobAsBytes) {
        return (ListaPersonalizzata) ListaPersonalizzata.Companion.deserializeObject(blobAsBytes);
    }

    @TypeConverter
    public static byte[] fromListaPersonalizzata (ListaPersonalizzata lista) {
        return ListaPersonalizzata.Companion.serializeObject(lista);

    }

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
