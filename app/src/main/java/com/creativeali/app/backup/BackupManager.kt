package com.creativeali.app.backup

import android.content.Context
import com.creativeali.app.dlof.DlofCrypto
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * نسخ احتياطي كامل لتطبيق Creative Ali: قاعدة بيانات Room (المذكرات،
 * المخططات) + كل الملفات الداخلية (حزم dlofpkg، الوسائط المحفوظة) في
 * أرشيف `.caibak` واحد (zip، مع تشفير AES-256-GCM اختياري عبر [DlofCrypto]).
 *
 * بنية الأرشيف:
 *   manifest.json          — إصدار التطبيق، تاريخ الإنسخة، قائمة الملفات
 *   db/creative_ali.db      — قاعدة البيانات (+ -wal / -shm إن وُجدا)
 *   files/...               — نسخة كاملة من مجلد filesDir الداخلي
 */
object BackupManager {

    private const val DB_NAME = "creative_ali.db"
    private const val MANIFEST_ENTRY = "manifest.json"

    data class BackupResult(val success: Boolean, val message: String, val sizeBytes: Long = 0)

    /** ينشئ نسخة احتياطية ويكتبها إلى [outStream]. إن مُرّرت [password] تُشفَّر النسخة كاملة. */
    fun createBackup(context: Context, outStream: OutputStream, password: String? = null): BackupResult {
        return try {
            val raw = buildZipBytes(context)
            if (password.isNullOrBlank()) {
                outStream.write(raw)
            } else {
                when (val enc = DlofCrypto.encrypt(String(raw, Charsets.ISO_8859_1), password)) {
                    is DlofCrypto.Result.Success -> outStream.write(enc.data)
                    is DlofCrypto.Result.Failure -> return BackupResult(false, enc.message)
                }
            }
            BackupResult(true, "تم إنشاء النسخة الاحتياطية بنجاح", raw.size.toLong())
        } catch (e: Exception) {
            BackupResult(false, "فشل إنشاء النسخة الاحتياطية: ${e.message}")
        }
    }

    private fun buildZipBytes(context: Context): ByteArray {
        val buffer = ByteArrayOutputStream()
        ZipOutputStream(buffer).use { zip ->
            val dbFile = context.getDatabasePath(DB_NAME)
            listOf(dbFile, File(dbFile.path + "-wal"), File(dbFile.path + "-shm")).forEach { f ->
                if (f.exists()) {
                    zip.putNextEntry(ZipEntry("db/${f.name}"))
                    f.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }

            val filesDir = context.filesDir
            if (filesDir.exists()) {
                filesDir.walkTopDown().filter { it.isFile }.forEach { f ->
                    val relative = f.relativeTo(filesDir).path
                    zip.putNextEntry(ZipEntry("files/$relative"))
                    f.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }

            zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
            zip.write(
                buildString {
                    append("{")
                    append("\"app\":\"CreativeAli\",")
                    append("\"createdAt\":${System.currentTimeMillis()},")
                    append("\"dbIncluded\":${dbFile.exists()}")
                    append("}")
                }.toByteArray(Charsets.UTF_8)
            )
            zip.closeEntry()
        }
        return buffer.toByteArray()
    }

    /** يستعيد نسخة احتياطية. يُنصح بإعادة تشغيل التطبيق بعد النجاح لإعادة فتح قاعدة البيانات. */
    fun restoreBackup(context: Context, backupBytes: ByteArray, password: String? = null): BackupResult {
        return try {
            val zipBytes: ByteArray = if (DlofCrypto.isEncrypted(backupBytes)) {
                if (password.isNullOrBlank()) return BackupResult(false, "هذه النسخة مشفّرة وتحتاج كلمة مرور")
                when (val dec = DlofCrypto.decrypt(backupBytes, password)) {
                    is DlofCrypto.Result.Success -> dec.data.toString(Charsets.ISO_8859_1).toByteArray(Charsets.ISO_8859_1)
                    is DlofCrypto.Result.Failure -> return BackupResult(false, dec.message)
                }
            } else backupBytes

            ZipInputStream(zipBytes.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val target = when {
                        entry.name.startsWith("db/") -> File(context.getDatabasePath(DB_NAME).parentFile, entry.name.removePrefix("db/"))
                        entry.name.startsWith("files/") -> File(context.filesDir, entry.name.removePrefix("files/"))
                        else -> null // manifest.json وأي شيء آخر يُتجاهل عند الاستعادة
                    }
                    if (target != null) {
                        target.parentFile?.mkdirs()
                        target.outputStream().use { out -> zip.copyTo(out) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
            BackupResult(true, "تمت الاستعادة بنجاح — أعد تشغيل التطبيق لتفعيل البيانات المستعادة")
        } catch (e: Exception) {
            BackupResult(false, "فشلت الاستعادة: ${e.message}")
        }
    }
}
