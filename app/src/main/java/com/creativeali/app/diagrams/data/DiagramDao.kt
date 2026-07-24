package com.creativeali.app.diagrams.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagramDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiagram(diagram: DiagramEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDiagramIfAbsent(diagram: DiagramEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertElement(element: DiagramElementEntity)

    @Query("DELETE FROM diagram_elements WHERE id = :elementId")
    suspend fun deleteElement(elementId: String)

    @Query("SELECT * FROM diagrams WHERE id = :diagramId LIMIT 1")
    fun observeDiagram(diagramId: String): Flow<DiagramEntity?>

    @Query("SELECT * FROM diagram_elements WHERE diagramId = :diagramId")
    fun observeElements(diagramId: String): Flow<List<DiagramElementEntity>>
}
