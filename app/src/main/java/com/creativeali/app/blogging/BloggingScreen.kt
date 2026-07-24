package com.creativeali.app.blogging

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.creativeali.app.R
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch

/**
 * Blogging / memoir section: a `.b.dlof` loop of `.dlof` entries, persisted
 * locally through [BloggingViewModel]. List screen -> tap "+" -> editor ->
 * save writes the entry to Room; the share icon exports the whole loop as a
 * `.dlofpkg` package (see [DlofPackage]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloggingScreen(loopId: String = com.creativeali.app.blogging.data.DEFAULT_LOOP_ID) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: BloggingViewModel = viewModel(
        factory = BloggingViewModel.Factory(application, loopId),
        key = "blogging-$loopId",
    )
    val loop by viewModel.loop.collectAsStateWithLifecycle()
    var editingId by remember { mutableStateOf<String?>(null) }
    var creatingNew by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    val editingEntry = if (creatingNew) DlofEntry() else loop.entries.firstOrNull { it.id == editingId }

    if (editingEntry == null) {
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

        val filtered = remember(loop.entries, query) {
            if (query.isBlank()) loop.entries
            else loop.entries.filter {
                it.title.contains(query, ignoreCase = true) || it.body.contains(query, ignoreCase = true)
            }
        }
        val dateFormat = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }

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
            Column(Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.blogging_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                            }
                        }
                    },
                    singleLine = true
                )

                when {
                    loop.entries.isEmpty() -> EmptyState(stringResource(R.string.blogging_empty_state))
                    filtered.isEmpty() -> EmptyState(stringResource(R.string.blogging_no_results))
                    else -> LazyColumn(Modifier.fillMaxSize()) {
                        items(filtered, key = { it.id }) { entry ->
                            ListItem(
                                headlineContent = { Text(entry.title.ifBlank { "(بدون عنوان)" }) },
                                supportingContent = {
                                    Column {
                                        Text(dateFormat.format(Date(entry.createdAt)), style = MaterialTheme.typography.labelSmall)
                                        Text(entry.body.take(80), maxLines = 2)
                                    }
                                },
                                trailingContent = {
                                    IconButton(onClick = { pendingDeleteId = entry.id }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.blogging_delete_entry),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                modifier = Modifier.clickable { editingId = entry.id }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        if (pendingDeleteId != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteId = null },
                title = { Text(stringResource(R.string.blogging_delete_confirm_title)) },
                text = { Text(stringResource(R.string.blogging_delete_confirm_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.delete(pendingDeleteId!!)
                        pendingDeleteId = null
                    }) { Text(stringResource(R.string.delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteId = null }) { Text(stringResource(R.string.cancel)) }
                }
            )
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
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mediaRefs.forEachIndexed { index, ref ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(ref.substringAfterLast('/').take(24)) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.blogging_remove_attachment),
                                    modifier = Modifier.clickable { mediaRefs.removeAt(index) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
