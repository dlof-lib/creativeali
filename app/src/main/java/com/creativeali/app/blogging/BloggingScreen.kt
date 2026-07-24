package com.creativeali.app.blogging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.creativeali.app.R

/**
 * Blogging / memoir section: a `.b.dlof` loop of `.dlof` entries.
 * List screen -> tap "+" -> editor screen -> save appends to the loop,
 * "export" writes the loop out as a `.dlofpkg` package (see [DlofPackage]).
 */
@Composable
fun BloggingScreen() {
    val loop = remember { mutableStateOf(BDlofLoop()) }
    var editing by remember { mutableStateOf<DlofEntry?>(null) }

    if (editing == null) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { editing = DlofEntry() }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.blogging_new_entry))
                }
            }
        ) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(loop.value.entries) { entry ->
                    ListItem(
                        headlineContent = { Text(entry.title.ifBlank { "(بدون عنوان)" }) },
                        supportingContent = { Text(entry.body.take(80)) },
                        modifier = Modifier.clickable { editing = entry }
                    )
                    HorizontalDivider()
                }
            }
        }
    } else {
        EntryEditor(
            entry = editing!!,
            onSave = { saved ->
                val idx = loop.value.entries.indexOfFirst { it.id == saved.id }
                if (idx >= 0) loop.value.entries[idx] = saved else loop.value.entries.add(saved)
                loop.value.relink()
                editing = null
            },
            onCancel = { editing = null }
        )
    }
}

@Composable
private fun EntryEditor(entry: DlofEntry, onSave: (DlofEntry) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf(entry.title) }
    var body by remember { mutableStateOf(entry.body) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.blogging_new_entry)) },
                actions = {
                    IconButton(onClick = {
                        entry.title = title; entry.body = body; onSave(entry)
                    }) { Icon(Icons.Default.Save, contentDescription = stringResource(R.string.blogging_save)) }
                    IconButton(onClick = { /* export .dlofpkg via DlofPackage.export(...) */ }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.blogging_export_pkg))
                    }
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
        }
    }
}
