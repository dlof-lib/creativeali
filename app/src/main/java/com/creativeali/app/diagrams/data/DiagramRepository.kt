package com.creativeali.app.diagrams.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.creativeali.app.diagrams.Diagram
import com.creativeali.app.diagrams.DiagramElement
import com.creativeali.app.diagrams.ShapeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

const val DEFAULT_DIAGRAM_ID = "default-diagram"

class DiagramRepository(private val dao: DiagramDao) {

    suspend fun ensureDefaultDiagram(diagramId: String = DEFAULT_DIAGRAM_ID) =
        dao.insertDiagramIfAbsent(DiagramEntity(id = diagramId, name = "مخطط بدون اسم"))

    fun observeDiagram(diagramId: String = DEFAULT_DIAGRAM_ID): Flow<Diagram> =
        dao.observeDiagram(diagramId).combine(dao.observeElements(diagramId)) { diagramEntity, elementEntities ->
            Diagram(
                id = diagramEntity?.id ?: diagramId,
                name = diagramEntity?.name ?: "مخطط بدون اسم",
                elements = elementEntities.map { it.toDomain() }.toMutableList(),
            )
        }

    suspend fun saveElement(diagramId: String = DEFAULT_DIAGRAM_ID, element: DiagramElement) =
        dao.upsertElement(element.toEntity(diagramId))

    suspend fun deleteElement(elementId: String) = dao.deleteElement(elementId)
}

private fun DiagramElementEntity.toDomain() = DiagramElement(
    id = id,
    type = ShapeType.valueOf(type),
    position = Offset(x, y),
    width = width,
    height = height,
    rotationDeg = rotationDeg,
    fillColor = Color(fillColor),
    borderColor = Color(borderColor),
    borderWidthDp = borderWidthDp,
    cornerRadiusDp = cornerRadiusDp,
    text = text,
    textColor = Color(textColor),
    fontAsset = fontAsset,
    mediaUri = mediaUri,
    iconAsset = iconAsset,
)

private fun DiagramElement.toEntity(diagramId: String) = DiagramElementEntity(
    id = id, diagramId = diagramId, type = type.name,
    x = position.x, y = position.y, width = width, height = height, rotationDeg = rotationDeg,
    fillColor = fillColor.toArgb(), borderColor = borderColor.toArgb(),
    borderWidthDp = borderWidthDp, cornerRadiusDp = cornerRadiusDp,
    text = text, textColor = textColor.toArgb(),
    fontAsset = fontAsset, mediaUri = mediaUri, iconAsset = iconAsset,
)
