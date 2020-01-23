package it.cammino.risuscito.database.serializer

import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.sql.Date


class DateTimeSerializer : JsonSerializer<Date> {
    override fun serialize(src: Date, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.time)
    }
}