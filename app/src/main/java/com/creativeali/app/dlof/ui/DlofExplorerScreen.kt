package com.creativeali.app.dlof.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.creativeali.app.R
import com.creativeali.app.ads.AdBanner
import com.creativeali.app.dlof.DlofContent

@Composable
fun DlofExplorerScreen(viewModel: DlofExplorerViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var password by remember { mutableStateOf("") }
    var backupPassword by remember { mutableStateOf("") }

    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.openFile(context, it) }
    }
    val backupCreateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let { viewModel.createBackup(context, it, backupPassword.ifBlank { null }) } }
    val backupRestoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.restoreBackup(context, it, backupPassword.ifBlank { null }) }
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Button(
                    onClick = { openLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.dlof_open_file))
                }
            }

            item {
                Text(
                    text = state.fileName ?: stringResource(R.string.dlof_no_file),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            state.error?.let { err ->
                item { Text(err, color = MaterialTheme.colorScheme.error) }
            }

            if (state.needsPassword) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(stringResource(R.string.dlof_encrypted_prompt))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text(stringResource(R.string.dlof_password_hint)) },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            )
                            Button(
                                onClick = { viewModel.decryptWithPassword(password) },
                                modifier = Modifier.padding(top = 8.dp),
                            ) { Text(stringResource(R.string.dlof_decrypt)) }
                        }
                    }
                }
            }

            state.packageSummary?.let { summary ->
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(stringResource(R.string.dlof_package_summary), fontWeight = FontWeight.Bold)
                            Text(summary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            state.document?.let { doc ->
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(doc.metadata.title, style = MaterialTheme.typography.titleLarge)
                            Text("النطاق: ${doc.metadata.domain.wire} • اللغة: ${doc.metadata.language}")
                            doc.metadata.author?.let { Text("المؤلف: $it") }
                            if (doc.metadata.tags.isNotEmpty()) Text("الوسوم: ${doc.metadata.tags.joinToString(", ")}")
                        }
                    }
                }
                items(doc.content) { c -> ContentCard(c) }

                if (doc.attachments.isNotEmpty()) {
                    item { Text("المرفقات (${doc.attachments.size})", fontWeight = FontWeight.Bold) }
                    items(doc.attachments) { a ->
                        Card(Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text(a.fileName) },
                                supportingContent = { Text("${a.kind.wire} • ${a.mimeType}") },
                            )
                        }
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                Text("النسخ الاحتياطي", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = backupPassword,
                    onValueChange = { backupPassword = it },
                    label = { Text("كلمة مرور النسخة (اختياري)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { backupCreateLauncher.launch("creative-ali-backup.caibak") }) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.dlof_backup_create))
                    }
                    OutlinedButton(onClick = { backupRestoreLauncher.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.dlof_backup_restore))
                    }
                }
                state.backupMessage?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }
            }
        }

        AdBanner(modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun ContentCard(content: DlofContent) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            when (content) {
                is DlofContent.Generic -> {
                    Text("عنصر عام: ${content.element}", fontWeight = FontWeight.Bold)
                    Text(content.body)
                }
                is DlofContent.Qa -> {
                    Text("سؤال: ${content.question}", fontWeight = FontWeight.Bold)
                    Text("الإجابة: ${content.answer}")
                    content.explanation?.let { Text("الشرح: $it") }
                }
                is DlofContent.BookChapter -> {
                    Text("الفصل ${content.chapterNumber ?: ""}: ${content.chapterTitle}", fontWeight = FontWeight.Bold)
                    Text(content.text, maxLines = 5)
                }
                is DlofContent.TermDefinition -> {
                    Text(content.term, fontWeight = FontWeight.Bold)
                    Text(content.definition)
                }
                is DlofContent.InfoExplain -> {
                    Text(content.topic, fontWeight = FontWeight.Bold)
                    Text(content.explanation)
                }
                is DlofContent.Episode -> {
                    Text("الحلقة ${content.episodeNumber ?: ""}: ${content.episodeTitle}", fontWeight = FontWeight.Bold)
                    content.synopsis?.let { Text(it) }
                }
            }
        }
    }
}
