package com.creativeali.app.blogging

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.creativeali.app.R
import kotlinx.coroutines.launch

/**
 * Blogging / memoir section: a `.b.dlof` loop of `.dlof` entries, persisted
 * locally through [BloggingViewModel]. List screen -> tap "+" -> editor ->
 * save writes the entry to Room; the share icon exports the whole loop as a
 * `.dlofpkg` package (see [DlofPackage]).
 */
@Composable
fun BloggingScreen(viewModel: BloggingViewModel = viewModel()) {
    val loop by viewModel.loop.collectAsStateWithLifecycle()
    var editingId by remember { mutableStateOf<String?>(null) }
    var creatingNew by remember { mutableStateOf(false) }

    val editingEntry = if (creatingNew) DlofEntry() else loop.entries.firstOrNull { it.id == editingId }

    if (editingEntry == null) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            scope.launch {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        DlofPackage.export(context, loop, out)
                    }
                }.onSuccess {
                    Toast.makeText(context, "تم تصدير الحزمة بنجاح", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "فشل التصدير: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(loop.name) },
                    actions = {
                        IconButton(onClick = { exportLauncher.launch("${loop.name}.dlofpkg") }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.blogging_export_pkg))
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { creatingNew = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.blogging_new_entry))
                }
            }
        ) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(loop.entries, key = { it.id }) { entry ->
                    ListItem(
                        headlineContent = { Text(entry.title.ifBlank { "(بدون عنوان)" }) },
                        supportingContent = { Text(entry.body.take(80)) },
                        modifier = Modifier.clickable { editingId = entry.id }
                    )
                    HorizontalDivider()
                }
            }
        }
    } else {
        EntryEditor(
            entry = editingEntry,
            onSave = { saved ->
                viewModel.save(saved)
                editingId = null
                creatingNew = false
            },
            onCancel = { editingId = null; creatingNew = false }
        )
    }
}

@Composable
private fun EntryEditor(entry: DlofEntry, onSave: (DlofEntry) -> Unit, onCancel: () -> Unit) {
    var title by remember(entry.id) { mutableStateOf(entry.title) }
    var body by remember(entry.id) { mutableStateOf(entry.body) }
    val mediaRefs = remember(entry.id) { mutableStateListOf(*entry.mediaRefs.toTypedArray()) }

    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { mediaRefs.add(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.blogging_new_entry)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { mediaPicker.launch("*/*") }) {
                        Icon(Icons.Default.AttachFile, contentDescription = null)
                    }
                    IconButton(onClick = {
                        entry.title = title
                        entry.body = body
                        entry.mediaRefs = mediaRefs.toMutableList()
                        onSave(entry)
                    }) { Icon(Icons.Default.Save, contentDescription = stringResource(R.string.blogging_save)) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.blogging_title_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = body, onValueChange = { body = it },
                label = { Text(stringResource(R.string.blogging_body_hint)) },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            if (mediaRefs.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("مرفقات: ${mediaRefs.size}", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
