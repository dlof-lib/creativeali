package com.creativeali.app.diagrams

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class ShapeType { RECTANGLE, CIRCLE, LINE, TEXT, IMAGE, VIDEO }

/**
 * A single element placed on the diagram canvas. One class covers every
 * kind of node (geometric shape, text label, image, video) so drag/resize/
 * style logic in [DiagramCanvas] stays uniform.
 */
data class DiagramElement(
    val id: String = UUID.randomUUID().toString(),
    var type: ShapeType,
    var position: Offset = Offset(120f, 120f),
    var width: Float = 160f,
    var height: Float = 100f,
    var rotationDeg: Float = 0f,

    // Style
    var fillColor: Color = Color(0xFF1E8A2E),
    var borderColor: Color = Color(0xFF0F5C1E),
    var borderWidthDp: Float = 2f,
    var cornerRadiusDp: Float = 8f,

    // Text
    var text: String = "",
    var textColor: Color = Color.White,
    var fontAsset: String? = null, // path under assets/fonts/, from FontLibrary

    // Media
    var mediaUri: String? = null,  // content:// or file:// uri, for IMAGE/VIDEO
    var iconAsset: String? = null, // path under assets/icons/, from IconLibrary
)

/** One diagram/project chart: a free-form canvas of [DiagramElement]s. */
data class Diagram(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "مخطط بدون اسم",
    val elements: MutableList<DiagramElement> = mutableListOf(),
)
