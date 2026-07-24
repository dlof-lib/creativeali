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
import com.creativeali.app.container.data.ContainerConverters
import com.creativeali.app.container.data.ContainerDao
import com.creativeali.app.container.data.ContainerEntity
import com.creativeali.app.diagrams.data.DiagramDao
import com.creativeali.app.diagrams.data.DiagramElementEntity
import com.creativeali.app.diagrams.data.DiagramEntity

@Database(
    entities = [
        LoopEntity::class, EntryEntity::class,
        DiagramEntity::class, DiagramElementEntity::class,
        ContainerEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class, ContainerConverters::class)
abstract class CreativeAliDatabase : RoomDatabase() {
    abstract fun bloggingDao(): BloggingDao
    abstract fun diagramDao(): DiagramDao
    abstract fun containerDao(): ContainerDao

    companion object {
        @Volatile private var instance: CreativeAliDatabase? = null

        fun get(context: Context): CreativeAliDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CreativeAliDatabase::class.java,
                    "creative_ali.db",
                )
                    // v1 -> v2 أضافت جدول الحاويات؛ لا توجد بيانات إنتاجية بعد
                    // لذا الأبسط هو إعادة إنشاء القاعدة بدل كتابة Migration كاملة.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
