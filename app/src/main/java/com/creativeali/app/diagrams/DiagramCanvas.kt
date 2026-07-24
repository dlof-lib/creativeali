package com.creativeali.app.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.max

/**
 * Free-form canvas where every [DiagramElement] can be dragged with a finger
 * and, when selected, edited through the property panel in [DiagramScreen].
 */
@Composable
fun DiagramCanvas(
    diagram: Diagram,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    onMove: (String, Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xFFF2F7F0))
            .pointerInput(Unit) { detectDragGestures(onDragStart = { onSelect(null) }) { _, _ -> } }
    ) {
        diagram.elements.forEach { element ->
            ElementView(
                element = element,
                selected = element.id == selectedId,
                onTap = { onSelect(element.id) },
                onDrag = { delta -> onMove(element.id, delta) },
            )
        }
    }
}

@Composable
private fun ElementView(
    element: DiagramElement,
    selected: Boolean,
    onTap: () -> Unit,
    onDrag: (Offset) -> Unit,
) {
    val borderColor = if (selected) Color(0xFFF5B300) else element.borderColor
    val borderWidth = if (selected) max(element.borderWidthDp, 3f) else element.borderWidthDp

    Box(
        modifier = Modifier
            .offset { androidx.compose.ui.unit.IntOffset(element.position.x.toInt(), element.position.y.toInt()) }
            .size(width = element.width.dp, height = element.height.dp)
            .pointerInput(element.id) {
                detectDragGestures(onDragStart = { onTap() }) { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
    ) {
        when (element.type) {
            ShapeType.RECTANGLE, ShapeType.CIRCLE -> {
                Canvas(Modifier.fillMaxSize()) {
                    val strokePx = borderWidth.dp.toPx()
                    if (element.type == ShapeType.CIRCLE) {
                        drawOval(color = element.fillColor)
                        drawOval(color = borderColor, style = Stroke(strokePx))
                    } else {
                        val r = CornerRadius(element.cornerRadiusDp.dp.toPx())
                        drawRoundRect(color = element.fillColor, cornerRadius = r)
                        drawRoundRect(color = borderColor, cornerRadius = r, style = Stroke(strokePx))
                    }
                }
            }
            ShapeType.LINE -> {
                Canvas(Modifier.fillMaxSize()) {
                    drawLine(
                        color = element.borderColor,
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = element.borderWidthDp.dp.toPx(),
                    )
                }
            }
            ShapeType.TEXT -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(element.text.ifBlank { "نص" }, color = element.textColor)
                }
            }
            ShapeType.IMAGE -> {
                if (element.mediaUri != null) {
                    AsyncImage(model = element.mediaUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0xFFDDDDDD)))
                }
            }
            ShapeType.VIDEO -> {
                // Thumbnail preview only; playback is wired up with a VideoView/ExoPlayer
                // when the user taps the element in the full editor.
                if (element.mediaUri != null) {
                    AsyncImage(model = element.mediaUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0xFF333333)))
                }
            }
        }
    }
}
