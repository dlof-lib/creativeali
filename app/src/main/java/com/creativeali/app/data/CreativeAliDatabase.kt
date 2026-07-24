package com.creativeali.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.creativeali.app.blogging.data.BloggingDao
import com.creativeali.app.blogging.data.Converters
import com.creativeali.app.blogging.data.EntryEntity
import com.creativeali.app.blogging.data.LoopEntity
import com.creativeali.app.diagrams.data.DiagramDao
import com.creativeali.app.diagrams.data.DiagramElementEntity
import com.creativeali.app.diagrams.data.DiagramEntity

@Database(
    entities = [
        LoopEntity::class, EntryEntity::class,
        DiagramEntity::class, DiagramElementEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class CreativeAliDatabase : RoomDatabase() {
    abstract fun bloggingDao(): BloggingDao
    abstract fun diagramDao(): DiagramDao

    companion object {
        @Volatile private var instance: CreativeAliDatabase? = null

        fun get(context: Context): CreativeAliDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CreativeAliDatabase::class.java,
                    "creative_ali.db",
                ).build().also { instance = it }
            }
    }
}
