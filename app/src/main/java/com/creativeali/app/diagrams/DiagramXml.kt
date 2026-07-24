package com.creativeali.app.diagrams

import androidx.compose.ui.graphics.toArgb

/**
 * Minimal XML writer for a [Diagram], bundled into `.diagrampkg` exports
 * alongside `set.txt` (mirrors the `.b.dlof` / `.dlofpkg` pairing in the
 * blogging section — see DlofXml / DlofPackage).
 */
object DiagramXml {
    fun write(diagram: Diagram): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        append("<diagram id=\"${diagram.id}\" name=\"${escape(diagram.name)}\">")
        diagram.elements.forEach { e ->
            append("<element id=\"${e.id}\" type=\"${e.type.name}\" ")
            append("x=\"${e.position.x}\" y=\"${e.position.y}\" width=\"${e.width}\" height=\"${e.height}\" ")
            append("rotation=\"${e.rotationDeg}\" fill=\"${e.fillColor.toArgb()}\" border=\"${e.borderColor.toArgb()}\" ")
            append("borderWidth=\"${e.borderWidthDp}\" cornerRadius=\"${e.cornerRadiusDp}\" ")
            append("textColor=\"${e.textColor.toArgb()}\" fontAsset=\"${e.fontAsset ?: ""}\" ")
            append("mediaUri=\"${e.mediaUri ?: ""}\" iconAsset=\"${e.iconAsset ?: ""}\">")
            append(escape(e.text))
            append("</element>")
        }
        append("</diagram>")
    }

    private fun escape(s: String): String = s
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
