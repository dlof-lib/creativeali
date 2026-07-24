package com.creativeali.app.diagrams

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlin.math.max

/**
 * Free-form canvas where every [DiagramElement] can be dragged, resized
 * (bottom-right handle) and rotated (assigned via the property panel), and
 * when selected, edited through the property panel in [DiagramScreen].
 */
@Composable
fun DiagramCanvas(
    diagram: Diagram,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    onMove: (String, Offset) -> Unit,
    onResize: (String, Offset) -> Unit,
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
                onResizeDrag = { delta -> onResize(element.id, delta) },
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
    onResizeDrag: (Offset) -> Unit,
) {
    val borderColor = if (selected) Color(0xFFF5B300) else element.borderColor
    val borderWidth = if (selected) max(element.borderWidthDp, 3f) else element.borderWidthDp
    var showVideoDialog by remember(element.id) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset { IntOffset(element.position.x.toInt(), element.position.y.toInt()) }
            .size(width = element.width.dp, height = element.height.dp)
            .graphicsLayer { rotationZ = element.rotationDeg }
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
                Box(Modifier.fillMaxSize()) {
                    if (element.mediaUri != null) {
                        AsyncImage(model = element.mediaUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(Modifier.fillMaxSize().background(Color(0xFF333333)))
                    }
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                            .clickable { showVideoDialog = true }
                    )
                }
                if (showVideoDialog && element.mediaUri != null) {
                    Dialog(onDismissRequest = { showVideoDialog = false }) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    setVideoURI(Uri.parse(element.mediaUri))
                                    setOnPreparedListener { it.start() }
                                }
                            },
                            modifier = Modifier.size(320.dp, 220.dp)
                        )
                    }
                }
            }
        }

        // Resize handle: only shown for the selected element, bottom-right corner.
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .background(Color(0xFFF5B300), shape = androidx.compose.foundation.shape.CircleShape)
                    .pointerInput(element.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onResizeDrag(dragAmount)
                        }
                    }
            )
        }
    }
}
