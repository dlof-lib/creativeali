package com.creativeali.app.container.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(container: ContainerEntity)

    @Query("DELETE FROM containers WHERE id = :containerId")
    suspend fun delete(containerId: String)

    @Query("SELECT * FROM containers ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM containers WHERE id = :containerId LIMIT 1")
    fun observeOne(containerId: String): Flow<ContainerEntity?>
}
