package com.creativeali.app.blogging.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BloggingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLoop(loop: LoopEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLoopIfAbsent(loop: LoopEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE id = :entryId")
    suspend fun deleteEntry(entryId: String)

    @Query("SELECT * FROM loops WHERE id = :loopId LIMIT 1")
    fun observeLoop(loopId: String): Flow<LoopEntity?>

    @Query("SELECT * FROM entries WHERE loopId = :loopId ORDER BY orderIndex ASC")
    fun observeEntries(loopId: String): Flow<List<EntryEntity>>

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM entries WHERE loopId = :loopId")
    suspend fun maxOrderIndex(loopId: String): Int

    @Query("SELECT orderIndex FROM entries WHERE id = :entryId LIMIT 1")
    suspend fun existingOrderIndex(entryId: String): Int?

    @Transaction
    suspend fun ensureLoopExists(loopId: String, defaultName: String) {
        insertLoopIfAbsent(LoopEntity(id = loopId, name = defaultName, closed = false))
    }
}
