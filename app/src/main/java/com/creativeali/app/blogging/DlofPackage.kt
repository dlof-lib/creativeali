package com.creativeali.app.blogging

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Builds a `.dlofpkg` (zip) containing:
 *   setting/dlotemplate.xml   -- the loop's structure, same schema as .b.dlof
 *   set.txt                   -- key/value package metadata (see SetTxt)
 *   media/<file>              -- any referenced images/videos, copied in as-is
 */
object DlofPackage {

    fun export(context: Context, loop: BDlofLoop, mediaUris: Map<String, Uri>, outFile: File) {
        ZipOutputStream(outFile.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("setting/dlotemplate.xml"))
            zip.write(DlofXml.writeLoop(loop).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("set.txt"))
            zip.write(SetTxt.build(loop).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            loop.entries.forEach { entry ->
                entry.mediaRefs.forEach { fileName ->
                    val uri = mediaUris[fileName] ?: return@forEach
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        zip.putNextEntry(ZipEntry("media/$fileName"))
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
