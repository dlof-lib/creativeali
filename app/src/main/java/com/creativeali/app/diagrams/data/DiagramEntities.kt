package com.creativeali.app.diagrams.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagrams")
data class DiagramEntity(
    @PrimaryKey val id: String,
    val name: String,
)

@Entity(tableName = "diagram_elements")
data class DiagramElementEntity(
    @PrimaryKey val id: String,
    val diagramId: String,
    val type: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rotationDeg: Float,
    val fillColor: Int,  // packed ARGB8888, via Color.toArgb() / Color(argb)
    val borderColor: Int,
    val borderWidthDp: Float,
    val cornerRadiusDp: Float,
    val text: String,
    val textColor: Int,
    val fontAsset: String?,
    val mediaUri: String?,
    val iconAsset: String?,
)
