package com.creativeali.app.blogging

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Builds a `.dlofpkg` (zip) containing:
 *   setting/dlotemplate.xml   -- the loop's structure, same schema as .b.dlof
 *   set.txt                   -- key/value package metadata (see SetTxt)
 *   media/<file>              -- every media reference, resolved straight
 *                                from the content:// / file:// URI stored in
 *                                DlofEntry.mediaRefs
 *
 * [outStream] is whatever the caller obtained from a SAF
 * `ActivityResultContracts.CreateDocument` picker (or any other writable
 * stream) — this class never touches the filesystem directly so it works
 * the same whether the destination is scoped storage, a SAF document, or
 * (in tests) an in-memory buffer.
 */
object DlofPackage {

    fun export(context: Context, loop: BDlofLoop, outStream: OutputStream) {
        ZipOutputStream(outStream).use { zip ->
            zip.putNextEntry(ZipEntry("setting/dlotemplate.xml"))
            zip.write(DlofXml.writeLoop(loop).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("set.txt"))
            zip.write(SetTxt.build(loop).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            loop.entries.forEach { entry ->
                entry.mediaRefs.forEachIndexed { index, uriString ->
                    val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return@forEachIndexed
                    val ext = context.contentResolver.getType(uri)
                        ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: "bin"
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        zip.putNextEntry(ZipEntry("media/${entry.id}_$index.$ext"))
                        input.copyTo(zip)
                        zip.closeEntry()
                    }
                }
            }
        }
    }
}

/** Builds the `set.txt` metadata file bundled with every `.dlofpkg`. */
object SetTxt {
    fun build(loop: BDlofLoop): String = buildString {
        appendLine("# Creative Ali — set.txt")
        appendLine("package.type=dlofpkg")
        appendLine("package.name=${loop.name}")
        appendLine("package.id=${loop.id}")
        appendLine("package.entries=${loop.entries.size}")
        appendLine("package.closedLoop=${loop.closed}")
        appendLine("package.createdBy=Creative Ali")
        appendLine("package.format.version=1.0")
    }
}
