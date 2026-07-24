package com.creativeali.app.container

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
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
import kotlinx.coroutines.delay

/**
 * معالج إنشاء حاوية DLoF بثلاث خطوات:
 * 1) الاسم/الوصف/الأيقونة
 * 2) الوسامات + خيارات التشغيل (set.txt) + ترخيص اختياري
 * 3) تسلسل "جاري الإنشاء" التلقائي ثم فتح الحاوية الجديدة.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerCreateWizard(
    onCancel: () -> Unit,
    onSubmit: (
        name: String,
        description: String,
        iconUri: String?,
        badges: List<DlofBadge>,
        allowSetTxt: Boolean,
        licenseText: String?,
    ) -> DlofContainer,
    onFinished: (DlofContainer) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var iconUri by remember { mutableStateOf<String?>(null) }
    val badges = remember { mutableStateListOf<DlofBadge>() }
    var allowSetTxt by remember { mutableStateOf(false) }
    var licenseText by remember { mutableStateOf("") }
    var showAddBadge by remember { mutableStateOf(false) }
    var createdContainer by remember { mutableStateOf<DlofContainer?>(null) }

    val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { iconUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.container_new)) },
                navigationIcon = {
                    if (step < 2) {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (step) {
            0 -> Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
            ) {
                Box(
                    Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .clickable { iconPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (iconUri != null) {
                        AsyncImage(
                            model = iconUri, contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Folder, contentDescription = stringResource(R.string.container_icon_default), modifier = Modifier.size(48.dp))
                    }
                }
                TextButton(onClick = { iconPicker.launch("image/*") }) {
                    Text(stringResource(R.string.container_icon_pick))
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.container_name_hint)) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text(stringResource(R.string.container_description_hint)) },
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { step = 1 },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.container_step_next)) }
            }

            1 -> Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.container_badges_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    badges.forEach { badge ->
                        AssistChip(onClick = {}, label = { Text(badge.name) })
                    }
                    AssistChip(
                        onClick = { showAddBadge = true },
                        label = { Text(stringResource(R.string.container_badge_add)) },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier.fillMaxWidth().clickable { allowSetTxt = !allowSetTxt },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = allowSetTxt, onCheckedChange = { allowSetTxt = it })
                    Text(stringResource(R.string.container_allow_settxt))
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.container_license_optional), style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = licenseText, onValueChange = { licenseText = it },
                    label = { Text(stringResource(R.string.container_license_hint)) },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { step = 0 }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.container_step_back))
                    }
                    Button(
                        onClick = {
                            createdContainer = onSubmit(name, description, iconUri, badges, allowSetTxt, licenseText)
                            step = 2
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.container_step_create)) }
                }
            }

            else -> CreationProgress(
                onDone = { createdContainer?.let(onFinished) },
                modifier = Modifier.padding(padding)
            )
        }

        if (showAddBadge) {
            AddBadgeDialog(
                onDismiss = { showAddBadge = false },
                onAdd = { badge -> badges.add(badge); showAddBadge = false }
            )
        }
    }
}

@Composable
private fun CreationProgress(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val steps = listOf(
        R.string.container_progress_creating,
        R.string.container_progress_preparing,
        R.string.container_progress_badges,
        R.string.container_progress_uploading,
        R.string.container_progress_done,
        R.string.container_progress_opening,
    )
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        for (i in steps.indices) {
            index = i
            delay(450)
        }
        onDone()
    }

    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (index < steps.lastIndex) {
            CircularProgressIndicator()
        } else {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(steps[index]), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AddBadgeDialog(onDismiss: () -> Unit, onAdd: (DlofBadge) -> Unit) {
    var name by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var svgRef by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.container_badge_add)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.container_badge_name_hint)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = example, onValueChange = { example = it },
                    label = { Text(stringResource(R.string.container_badge_example_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = svgRef, onValueChange = { svgRef = it },
                    label = { Text(stringResource(R.string.container_badge_svg_hint)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(DlofBadge(name = name, example = example, svgIconRef = svgRef.ifBlank { null }))
                    }
                },
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.container_badge_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
