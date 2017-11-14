package it.cammino.risuscito.database;

import android.arch.persistence.room.TypeConverter;

import java.sql.Date;

import it.cammino.risuscito.ListaPersonalizzata;

public class Converters {

    @TypeConverter
    public static ListaPersonalizzata fromBlob(byte[] blobAsBytes) {
//        try {
//            int blobLength = (int) blob.length();
//            byte[] blobAsBytes = blob.getBytes(1, blobLength);
//            return (ListaPersonalizzata) ListaPersonalizzata.deserializeObject(blobAsBytes);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
        return (ListaPersonalizzata) ListaPersonalizzata.deserializeObject(blobAsBytes);
    }

    @TypeConverter
    public static byte[] fromListaPersonalizzata (ListaPersonalizzata lista) {
        return ListaPersonalizzata.serializeObject(lista);

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
