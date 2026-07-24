package com.creativeali.app.blogging.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "loops")
data class LoopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val closed: Boolean,
)

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey val id: String,
    val loopId: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val orderIndex: Int,
    val mediaRefs: List<String>, // stored via Converters below
)

/** Joins a list of URI strings into one column using a delimiter that never
 * appears in a content:// / file:// URI, so no extra dependency (e.g. Gson)
 * is needed just to persist a handful of media references. */
class Converters {
    @TypeConverter
    fun listToString(list: List<String>): String = list.joinToString("§")

    @TypeConverter
    fun stringToList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split("§")
}
