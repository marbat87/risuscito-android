package it.cammino.risuscito.database

import androidx.room.TypeConverter
import it.cammino.risuscito.objects.ListaPersonalizzata
import java.sql.Date

@Suppress("unused")
class Converters {

    @TypeConverter
    fun fromBlob(blobAsBytes: ByteArray): ListaPersonalizzata? {
        return ListaPersonalizzata.deserializeObject(blobAsBytes) as ListaPersonalizzata?
    }

    @TypeConverter
    fun fromListaPersonalizzata(lista: ListaPersonalizzata): ByteArray? {
        return ListaPersonalizzata.serializeObject(lista)

    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}
