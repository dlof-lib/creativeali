package com.creativeali.app.container

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.creativeali.app.R
import com.creativeali.app.blogging.BloggingScreen
import com.creativeali.app.diagrams.DiagramScreen

private enum class ContainerSection { OVERVIEW, BLOG, DIAGRAM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerDetailScreen(
    container: DlofContainer,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onEditDescription: (String) -> Unit,
    onAddBadge: (DlofBadge) -> Unit,
    onDelete: () -> Unit,
) {
    var section by remember(container.id) { mutableStateOf(ContainerSection.OVERVIEW) }
    var customizing by remember(container.id) { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showAddBadge by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(container.name) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (section != ContainerSection.OVERVIEW) section = ContainerSection.OVERVIEW
                        else onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        when (section) {
            ContainerSection.BLOG -> Box(Modifier.padding(padding)) { BloggingScreen(loopId = container.blogLoopId) }
            ContainerSection.DIAGRAM -> Box(Modifier.padding(padding)) { DiagramScreen(diagramId = container.diagramId) }
            ContainerSection.OVERVIEW -> Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(96.dp).clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (container.iconUri != null) {
                        AsyncImage(
                            model = container.iconUri, contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Folder, contentDescription = stringResource(R.string.container_icon_default), modifier = Modifier.size(56.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text(stringResource(R.string.container_badges_title), style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    container.badges.forEach { badge -> AssistChip(onClick = {}, label = { Text(badge.name) }) }
                    AssistChip(
                        onClick = { showAddBadge = true },
                        label = { Text("+") }
                    )
                }
                Spacer(Modifier.height(20.dp))

                Text(
                    container.description.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                if (customizing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AssistChip(
                            onClick = { section = ContainerSection.BLOG },
                            label = { Text(stringResource(R.string.container_open_blog)) },
                            leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) }
                        )
                        AssistChip(
                            onClick = { section = ContainerSection.DIAGRAM },
                            label = { Text(stringResource(R.string.container_open_diagram)) },
                            leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null) }
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                }

                FlowActionsRow(
                    onCustomize = { customizing = !customizing },
                    onDelete = { showDelete = true },
                    onRename = { showRename = true },
                    onEdit = { showEdit = true },
                )
            }
        }
    }

    if (showRename) {
        var value by remember { mutableStateOf(container.name) }
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text(stringResource(R.string.container_rename_title)) },
            text = {
                OutlinedTextField(value = value, onValueChange = { value = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = { onRename(value); showRename = false }) { Text(stringResource(R.string.container_rename)) }
            },
            dismissButton = { TextButton(onClick = { showRename = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showEdit) {
        var value by remember { mutableStateOf(container.description) }
        AlertDialog(
            onDismissRequest = { showEdit = false },
            title = { Text(stringResource(R.string.container_edit_title)) },
            text = {
                OutlinedTextField(
                    value = value, onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    label = { Text(stringResource(R.string.container_details_hint)) }
                )
            },
            confirmButton = {
                TextButton(onClick = { onEditDescription(value); showEdit = false }) { Text(stringResource(R.string.container_edit)) }
            },
            dismissButton = { TextButton(onClick = { showEdit = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.container_delete_confirm_title)) },
            text = { Text(stringResource(R.string.container_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { showDelete = false; onDelete() }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showAddBadge) {
        var name by remember { mutableStateOf("") }
        var example by remember { mutableStateOf("") }
        var svgRef by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddBadge = false },
            title = { Text(stringResource(R.string.container_badge_add)) },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text(stringResource(R.string.container_badge_name_hint)) }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = example, onValueChange = { example = it }, label = { Text(stringResource(R.string.container_badge_example_hint)) }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = svgRef, onValueChange = { svgRef = it }, singleLine = true, label = { Text(stringResource(R.string.container_badge_svg_hint)) }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddBadge(DlofBadge(name = name, example = example, svgIconRef = svgRef.ifBlank { null }))
                            showAddBadge = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) { Text(stringResource(R.string.container_badge_add)) }
            },
            dismissButton = { TextButton(onClick = { showAddBadge = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun FlowActionsRow(
    onCustomize: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(onClick = onCustomize, label = { Text(stringResource(R.string.container_customize)) }, leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) })
        AssistChip(onClick = onDelete, label = { Text(stringResource(R.string.container_delete)) }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) })
        AssistChip(onClick = onRename, label = { Text(stringResource(R.string.container_rename)) }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) })
        AssistChip(onClick = onEdit, label = { Text(stringResource(R.string.container_edit)) }, leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) })
    }
}
