package com.creativeali.app.diagrams

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun DiagramScreen(diagramId: String = com.creativeali.app.diagrams.data.DEFAULT_DIAGRAM_ID) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: DiagramViewModel = viewModel(
        factory = DiagramViewModel.Factory(application, diagramId),
        key = "diagram-$diagramId",
    )
    val diagram by viewModel.diagram.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf<String?>(null) }
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

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Toolbar: add shape / text / image / video / export
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(10.dp),
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
        }

        HorizontalDivider()

        // Canvas
        DiagramCanvas(
            diagram = diagram,
            selectedId = selectedId,
            onSelect = { selectedId = it },
            onMoveEnd = { id, totalDelta ->
                diagram.elements.firstOrNull { it.id == id }?.let {
                    it.position = Offset(it.position.x + totalDelta.x, it.position.y + totalDelta.y)
                    viewModel.save(it)
                }
            },
            onResizeEnd = { id, totalDelta ->
                diagram.elements.firstOrNull { it.id == id }?.let {
                    it.width = max(40f, it.width + totalDelta.x)
                    it.height = max(40f, it.height + totalDelta.y)
                    viewModel.save(it)
                }
            },
            modifier = Modifier.weight(1f)
        )

        // Property panel for the selected element
        if (selected != null) {
            HorizontalDivider()
            PropertyPanel(
                element = selected,
                onChanged = { viewModel.save(selected) },
                onDelete = {
                    viewModel.delete(selected.id)
                    selectedId = null
                },
                onDuplicate = {
                    val copy = selected.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        position = Offset(selected.position.x + 24f, selected.position.y + 24f)
                    )
                    viewModel.save(copy)
                    selectedId = copy.id
                }
            )
        } else {
            Text(
                stringResource(R.string.diagram_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun PropertyPanel(
    element: DiagramElement,
    onChanged: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
) {
    val palette = listOf(
        Color(0xFF0F5C1E), Color(0xFF1E8A2E), Color(0xFF8DC63F),
        Color(0xFFF5B300), Color.White, Color.Black, Color(0xFFE53935), Color(0xFF1565C0)
    )
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = onDuplicate,
                label = { Text(stringResource(R.string.diagram_duplicate_element)) },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) })
            AssistChip(onClick = onDelete,
                label = { Text(stringResource(R.string.diagram_delete_element)) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                colors = AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.error))
        }
        Spacer(Modifier.height(8.dp))

        if (element.type == ShapeType.TEXT) {
            var text by remember(element.id) { mutableStateOf(element.text) }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it; element.text = it; onChanged() },
                label = { Text(stringResource(R.string.diagram_text_content)) },
                modifier = Modifier.fillMaxWidth()
            )
            var fontSize by remember(element.id) { mutableStateOf(element.fontSizeSp) }
            Text("${stringResource(R.string.diagram_font_size)}: ${fontSize.toInt()}sp")
            Slider(value = fontSize, onValueChange = { fontSize = it; element.fontSizeSp = it; onChanged() }, valueRange = 8f..64f)
        }

        Text(stringResource(R.string.diagram_fill_color), style = MaterialTheme.typography.labelMedium)
        SwatchRow(palette, selected = element.fillColor) { element.fillColor = it; onChanged() }

        Text(stringResource(R.string.diagram_border_color), style = MaterialTheme.typography.labelMedium)
        SwatchRow(palette, selected = element.borderColor) { element.borderColor = it; onChanged() }

        Text(stringResource(R.string.diagram_text_color), style = MaterialTheme.typography.labelMedium)
        SwatchRow(palette, selected = element.textColor) { element.textColor = it; onChanged() }

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
private fun SwatchRow(colors: List<Color>, selected: Color, onPick: (Color) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
        colors.forEach { c ->
            val isSelected = c == selected
            Box(
                Modifier
                    .padding(4.dp)
                    .size(if (isSelected) 32.dp else 28.dp)
                    .background(c, shape = androidx.compose.foundation.shape.CircleShape)
                    .then(
                        if (isSelected) Modifier.border(
                            2.5.dp,
                            MaterialTheme.colorScheme.primary,
                            androidx.compose.foundation.shape.CircleShape
                        ) else Modifier
                    )
                    .clickable { onPick(c) }
            )
        }
    }
}
