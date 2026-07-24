package com.creativeali.app.container

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.creativeali.app.R

private enum class ListMode { LIST, CREATE, DETAIL }

/**
 * قسم "حاوية DLoF" الموحّد: يجمع أقسام المدونة/المذكرة والمخطط/التخطيط
 * السابقة تحت حاويات (مجلدات/مشاريع) مستقلة، كل واحدة منها لها اسمها
 * ووصفها ووسامها وأيقونتها الخاصة.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerListScreen(viewModel: ContainerViewModel = viewModel()) {
    val containers by viewModel.containers.collectAsStateWithLifecycle()
    var mode by remember { mutableStateOf(ListMode.LIST) }
    var selectedId by remember { mutableStateOf<String?>(null) }

    var pendingContainer by remember { mutableStateOf<DlofContainer?>(null) }
    val selected = containers.firstOrNull { it.id == selectedId }
        ?: pendingContainer?.takeIf { it.id == selectedId }

    when {
        mode == ListMode.CREATE -> ContainerCreateWizard(
            onCancel = { mode = ListMode.LIST },
            onSubmit = { name, description, iconUri, badges, allowSetTxt, licenseText ->
                viewModel.create(name, description, iconUri, badges, allowSetTxt, licenseText)
            },
            onFinished = { created ->
                pendingContainer = created
                selectedId = created.id
                mode = ListMode.DETAIL
            }
        )

        mode == ListMode.DETAIL && selected != null -> ContainerDetailScreen(
            container = selected,
            onBack = { mode = ListMode.LIST },
            onRename = { newName -> viewModel.rename(selected, newName) },
            onEditDescription = { desc -> viewModel.updateDetails(selected, desc) },
            onAddBadge = { badge -> viewModel.addBadge(selected, badge) },
            onDelete = {
                viewModel.delete(selected)
                mode = ListMode.LIST
            }
        )

        else -> Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(R.string.container_list_title)) }) },
            floatingActionButton = {
                FloatingActionButton(onClick = { mode = ListMode.CREATE }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.container_new))
                }
            }
        ) { padding ->
            if (containers.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.container_list_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    items(containers, key = { it.id }) { container ->
                        ListItem(
                            leadingContent = {
                                Box(Modifier.size(40.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                                    if (container.iconUri != null) {
                                        AsyncImage(
                                            model = container.iconUri, contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Folder, contentDescription = null)
                                    }
                                }
                            },
                            headlineContent = { Text(container.name) },
                            supportingContent = { Text(container.description.take(60)) },
                            modifier = Modifier.clickable {
                                selectedId = container.id
                                mode = ListMode.DETAIL
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
