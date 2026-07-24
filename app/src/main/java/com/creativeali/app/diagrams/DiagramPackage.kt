package com.creativeali.app.diagrams

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Exports a [Diagram] as a `.diagrampkg` zip: structure + set.txt + media. */
object DiagramPackage {

    fun export(context: Context, diagram: Diagram, outStream: OutputStream) {
        ZipOutputStream(outStream).use { zip ->
            zip.putNextEntry(ZipEntry("setting/diagram.xml"))
            zip.write(DiagramXml.write(diagram).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("set.txt"))
            zip.write(setTxt(diagram).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            diagram.elements.forEachIndexed { index, element ->
                val uriString = element.mediaUri ?: return@forEachIndexed
                val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return@forEachIndexed
                val ext = context.contentResolver.getType(uri)
                    ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: "bin"
                context.contentResolver.openInputStream(uri)?.use { input ->
                    zip.putNextEntry(ZipEntry("media/${element.id}_$index.$ext"))
                    input.copyTo(zip)
                    zip.closeEntry()
                }
            }
        }
    }

    private fun setTxt(diagram: Diagram): String = buildString {
        appendLine("# Creative Ali — set.txt")
        appendLine("package.type=diagrampkg")
        appendLine("diagram.name=${diagram.name}")
        appendLine("diagram.id=${diagram.id}")
        appendLine("diagram.elementCount=${diagram.elements.size}")
        appendLine("diagram.fonts=${diagram.elements.mapNotNull { it.fontAsset }.distinct().joinToString(",")}")
        appendLine("diagram.icons=${diagram.elements.mapNotNull { it.iconAsset }.distinct().joinToString(",")}")
        appendLine("package.createdBy=Creative Ali")
        appendLine("package.format.version=1.0")
    }
}
