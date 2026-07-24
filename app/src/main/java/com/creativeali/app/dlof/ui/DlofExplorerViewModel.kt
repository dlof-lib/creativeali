package com.creativeali.app.dlof.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creativeali.app.backup.BackupManager
import com.creativeali.app.dlof.DlofCrypto
import com.creativeali.app.dlof.DlofDocumentV2
import com.creativeali.app.dlof.DlofPackageValidator
import com.creativeali.app.dlof.DlofXmlCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipInputStream

data class DlofExplorerState(
    val fileName: String? = null,
    val isPackage: Boolean = false,
    val needsPassword: Boolean = false,
    val document: DlofDocumentV2? = null,
    val packageSummary: String? = null,
    val error: String? = null,
    val backupMessage: String? = null,
    val loading: Boolean = false,
)

class DlofExplorerViewModel : ViewModel() {

    private val _state = MutableStateFlow(DlofExplorerState())
    val state: StateFlow<DlofExplorerState> = _state.asStateFlow()

    private var pendingEncryptedBytes: ByteArray? = null

    fun openFile(context: Context, uri: Uri) {
        _state.update { it.copy(loading = true, error = null, needsPassword = false, backupMessage = null) }
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: throw IllegalStateException("تعذّرت قراءة الملف")

                val name = queryFileName(context, uri) ?: "ملف.dlof"
                val isZip = bytes.size >= 2 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()

                if (isZip) {
                    withContext(Dispatchers.IO) { openPackage(context, name, bytes) }
                } else if (DlofCrypto.isEncrypted(bytes)) {
                    pendingEncryptedBytes = bytes
                    _state.update { it.copy(fileName = name, needsPassword = true, loading = false) }
                } else {
                    val doc = DlofXmlCodec.parse(bytes.toString(Charsets.UTF_8))
                    _state.update { it.copy(fileName = name, document = doc, isPackage = false, loading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "خطأ في فتح الملف: ${e.message}", loading = false) }
            }
        }
    }

    fun decryptWithPassword(password: String) {
        val bytes = pendingEncryptedBytes ?: return
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.Default) { DlofCrypto.decrypt(bytes, password) }) {
                is DlofCrypto.Result.Success -> {
                    val doc = DlofXmlCodec.parse(result.data.toString(Charsets.UTF_8))
                    _state.update { it.copy(document = doc, needsPassword = false, error = null) }
                    pendingEncryptedBytes = null
                }
                is DlofCrypto.Result.Failure -> {
                    _state.update { it.copy(error = result.message) }
                }
            }
        }
    }

    private suspend fun openPackage(context: Context, name: String, bytes: ByteArray) {
        val tmpDir = File(context.cacheDir, "dlofpkg_${System.currentTimeMillis()}")
        tmpDir.mkdirs()
        ZipInputStream(bytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val target = File(tmpDir, entry.name)
                if (entry.isDirectory) {
                    target.mkdirs()
                } else {
                    target.parentFile?.mkdirs()
                    target.outputStream().use { out -> zip.copyTo(out) }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        val summary = DlofPackageValidator.summaryText(tmpDir)
        val firstDlof = DlofPackageValidator.findAllDlofFiles(tmpDir).firstOrNull()
        val doc = firstDlof?.let { runCatching { DlofXmlCodec.parse(it.readText()) }.getOrNull() }
        _state.update {
            it.copy(
                fileName = name, isPackage = true, packageSummary = summary,
                document = doc, loading = false,
            )
        }
    }

    private fun queryFileName(context: Context, uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && idx >= 0) cursor.getString(idx) else null
        }
    }.getOrNull()

    fun createBackup(context: Context, uri: Uri, password: String?) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    BackupManager.createBackup(context, out, password?.ifBlank { null })
                } ?: BackupManager.BackupResult(false, "تعذّر فتح وجهة الحفظ")
            }
            _state.update { it.copy(backupMessage = result.message) }
        }
    }

    fun restoreBackup(context: Context, uri: Uri, password: String?) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) BackupManager.BackupResult(false, "تعذّرت قراءة ملف النسخة الاحتياطية")
                else BackupManager.restoreBackup(context, bytes, password?.ifBlank { null })
            }
            _state.update { it.copy(backupMessage = result.message) }
        }
    }
}
