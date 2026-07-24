package com.creativeali.app.diagrams

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.creativeali.app.R
import kotlin.math.max
import kotlinx.coroutines.launch

/**
 * Diagrams / project-charts section: drag-and-drop shapes, images, videos
 * and text onto a free canvas, resize/move them, then adjust border, corner,
 * and text color for whichever element is selected. Persisted through
 * [DiagramViewModel]; the share icon exports the diagram as `.diagrampkg`.
 */
@Composable
fun DiagramScreen(viewModel: DiagramViewModel = viewModel()) {
    val diagram by viewModel.diagram.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.save(DiagramElement(type = ShapeType.IMAGE, mediaUri = uri.toString()))
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.save(DiagramElement(type = ShapeType.VIDEO, mediaUri = uri.toString()))
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    DiagramPackage.export(context, diagram, out)
                }
            }.onSuccess {
                Toast.makeText(context, "تم تصدير المخطط بنجاح", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "فشل التصدير: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val selected = diagram.elements.firstOrNull { it.id == selectedId }

    Column(Modifier.fillMaxSize()) {
        // Toolbar: add shape / text / image / video / export
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(onClick = { viewModel.save(DiagramElement(type = ShapeType.RECTANGLE)) },
                label = { Text(stringResource(R.string.diagram_add_shape)) },
                leadingIcon = { Icon(Icons.Default.CropSquare, contentDescription = null) })
            AssistChip(onClick = { viewModel.save(DiagramElement(type = ShapeType.CIRCLE)) },
                label = { Text("دائرة") }, leadingIcon = { Icon(Icons.Default.Circle, contentDescription = null) })
            AssistChip(onClick = { viewModel.save(DiagramElement(type = ShapeType.LINE, height = 4f)) },
                label = { Text("خط") }, leadingIcon = { Icon(Icons.Default.Remove, contentDescription = null) })
            AssistChip(onClick = { viewModel.save(DiagramElement(type = ShapeType.TEXT, text = "نص جديد")) },
                label = { Text(stringResource(R.string.diagram_add_text)) },
                leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) })
            AssistChip(onClick = { imagePicker.launch("image/*") },
                label = { Text(stringResource(R.string.diagram_add_image)) },
                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) })
            AssistChip(onClick = { videoPicker.launch("video/*") },
                label = { Text(stringResource(R.string.diagram_add_video)) },
                leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) })
            AssistChip(onClick = { exportLauncher.launch("${diagram.name}.diagrampkg") },
                label = { Text("تصدير") }, leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) })
        }

        HorizontalDivider()

        // Canvas
        DiagramCanvas(
            diagram = diagram,
            selectedId = selectedId,
            onSelect = { selectedId = it },
            onMove = { id, delta ->
                diagram.elements.firstOrNull { it.id == id }?.let {
                    it.position = Offset(it.position.x + delta.x, it.position.y + delta.y)
                    viewModel.save(it)
                }
            },
            onResize = { id, delta ->
                diagram.elements.firstOrNull { it.id == id }?.let {
                    it.width = max(40f, it.width + delta.x)
                    it.height = max(40f, it.height + delta.y)
                    viewModel.save(it)
                }
            },
            modifier = Modifier.weight(1f)
        )

        // Property panel for the selected element
        if (selected != null) {
            HorizontalDivider()
            PropertyPanel(selected) { viewModel.save(selected) }
        }
    }
}

@Composable
private fun PropertyPanel(element: DiagramElement, onChanged: () -> Unit) {
    val palette = listOf(
        Color(0xFF0F5C1E), Color(0xFF1E8A2E), Color(0xFF8DC63F),
        Color(0xFFF5B300), Color.White, Color.Black, Color(0xFFE53935), Color(0xFF1565C0)
    )
    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Text(stringResource(R.string.diagram_fill_color), style = MaterialTheme.typography.labelMedium)
        SwatchRow(palette) { element.fillColor = it; onChanged() }

        Text(stringResource(R.string.diagram_border_color), style = MaterialTheme.typography.labelMedium)
        SwatchRow(palette) { element.borderColor = it; onChanged() }

        Text(stringResource(R.string.diagram_text_color), style = MaterialTheme.typography.labelMedium)
        SwatchRow(palette) { element.textColor = it; onChanged() }

        var corner by remember(element.id) { mutableStateOf(element.cornerRadiusDp) }
        Text("${stringResource(R.string.diagram_corner_radius)}: ${corner.toInt()}dp")
        Slider(value = corner, onValueChange = { corner = it; element.cornerRadiusDp = it; onChanged() }, valueRange = 0f..48f)

        var border by remember(element.id) { mutableStateOf(element.borderWidthDp) }
        Text("${stringResource(R.string.diagram_border_width)}: ${border.toInt()}dp")
        Slider(value = border, onValueChange = { border = it; element.borderWidthDp = it; onChanged() }, valueRange = 0f..12f)

        var rotation by remember(element.id) { mutableStateOf(element.rotationDeg) }
        Text("الدوران: ${rotation.toInt()}°")
        Slider(value = rotation, onValueChange = { rotation = it; element.rotationDeg = it; onChanged() }, valueRange = 0f..360f)
    }
}

@Composable
private fun SwatchRow(colors: List<Color>, onPick: (Color) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
        colors.forEach { c ->
            Box(
                Modifier
                    .padding(4.dp)
                    .size(28.dp)
                    .background(c, shape = androidx.compose.foundation.shape.CircleShape)
                    .clickable { onPick(c) }
            )
        }
    }
}
