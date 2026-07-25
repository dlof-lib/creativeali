package com.creativeali.app.diagrams

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlin.math.max

/**
 * Free-form canvas where every [DiagramElement] can be dragged, resized
 * (bottom-right handle) and rotated (assigned via the property panel), and
 * when selected, edited through the property panel in [DiagramScreen].
 *
 * Dragging/resizing is tracked purely in local Compose state while the
 * gesture is in progress (so the canvas stays perfectly smooth at 60fps)
 * and is only committed — a single call to [onMoveEnd]/[onResizeEnd] — once
 * the finger lifts. That single commit is what [DiagramScreen] persists to
 * the database, instead of writing on every pixel of movement.
 */
@Composable
fun DiagramCanvas(
    diagram: Diagram,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    onMoveEnd: (String, Offset) -> Unit,
    onResizeEnd: (String, Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridDot = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .drawDotGrid(gridDot)
            .pointerInput(Unit) { detectTapGestures(onTap = { onSelect(null) }) }
    ) {
        diagram.elements.forEach { element ->
            ElementView(
                element = element,
                selected = element.id == selectedId,
                onTap = { onSelect(element.id) },
                onDragEnd = { total -> onMoveEnd(element.id, total) },
                onResizeEnd = { total -> onResizeEnd(element.id, total) },
            )
        }
    }
}

/** Subtle dot grid so the canvas reads as a real design surface rather than a blank rectangle. */
private fun Modifier.drawDotGrid(dotColor: Color): Modifier = this.then(
    Modifier.drawBehind {
        val step = 24.dp.toPx()
        val radius = 1.2.dp.toPx()
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawCircle(color = dotColor, radius = radius, center = Offset(x, y))
                x += step
            }
            y += step
        }
    }
)

@Composable
private fun ElementView(
    element: DiagramElement,
    selected: Boolean,
    onTap: () -> Unit,
    onDragEnd: (Offset) -> Unit,
    onResizeEnd: (Offset) -> Unit,
) {
    val accent = MaterialTheme.colorScheme.secondary
    val borderColor = if (selected) accent else element.borderColor
    val borderWidth = if (selected) max(element.borderWidthDp, 3f) else element.borderWidthDp
    var showVideoDialog by remember(element.id) { mutableStateOf(false) }

    // Live, purely-local drag/resize preview — never touches the database mid-gesture.
    var dragPreview by remember(element.id) { mutableStateOf(Offset.Zero) }
    var resizePreview by remember(element.id) { mutableStateOf(Offset.Zero) }

    val livePosition = element.position + dragPreview
    val liveWidth = max(40f, element.width + resizePreview.x)
    val liveHeight = max(40f, element.height + resizePreview.y)

    Box(
        modifier = Modifier
            .offset { IntOffset(livePosition.x.toInt(), livePosition.y.toInt()) }
            .size(width = liveWidth.dp, height = liveHeight.dp)
            .graphicsLayer { rotationZ = element.rotationDeg }
            .then(if (selected) Modifier.shadow(6.dp, RoundedCornerOrCircleShape(element)) else Modifier)
            .pointerInput(element.id) {
                detectDragGestures(
                    onDragStart = { onTap() },
                    onDragEnd = { onDragEnd(dragPreview); dragPreview = Offset.Zero },
                    onDragCancel = { dragPreview = Offset.Zero },
                ) { change, dragAmount ->
                    change.consume()
                    dragPreview += dragAmount
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
                    Text(element.text.ifBlank { "نص" }, color = element.textColor, fontSize = element.fontSizeSp.sp)
                }
            }
            ShapeType.IMAGE -> {
                if (element.mediaUri != null) {
                    AsyncImage(model = element.mediaUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                }
            }
            ShapeType.VIDEO -> {
                Box(Modifier.fillMaxSize()) {
                    if (element.mediaUri != null) {
                        AsyncImage(model = element.mediaUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
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
                    .shadow(3.dp, CircleShape)
                    .size(22.dp)
                    .background(accent, shape = CircleShape)
                    .pointerInput(element.id) {
                        detectDragGestures(
                            onDragEnd = { onResizeEnd(resizePreview); resizePreview = Offset.Zero },
                            onDragCancel = { resizePreview = Offset.Zero },
                        ) { change, dragAmount ->
                            change.consume()
                            resizePreview += dragAmount
                        }
                    }
            )
        }
    }
}

/** Matches the resize-handle shadow to the element's own silhouette so it doesn't look boxy on circles. */
private fun RoundedCornerOrCircleShape(element: DiagramElement) =
    if (element.type == ShapeType.CIRCLE) CircleShape
    else androidx.compose.foundation.shape.RoundedCornerShape(element.cornerRadiusDp.dp)
