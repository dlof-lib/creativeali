package com.creativeali.app.blogging

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.io.StringWriter

/**
 * Minimal XML (de)serialization for the DLoF format used by Creative Ali.
 * Schema (single entry, `.dlof`):
 *
 * <dlof id="..." createdAt="...">
 *   <title>...</title>
 *   <body>...</body>
 *   <media><ref>photo1.jpg</ref></media>
 *   <loop nextId="..." prevId="..." />
 * </dlof>
 *
 * A `.b.dlof` file wraps many <dlof> entries inside a <loop> root so the
 * whole chain travels together:
 *
 * <dlofLoop id="..." name="..." closed="true|false">
 *   <dlof>...</dlof>
 *   <dlof>...</dlof>
 * </dlofLoop>
 */
object DlofXml {

    fun writeEntry(entry: DlofEntry): String {
        val sw = StringWriter()
        sw.append("<dlof id=\"${entry.id}\" createdAt=\"${entry.createdAt}\">")
        sw.append("<title>${escape(entry.title)}</title>")
        sw.append("<body>${escape(entry.body)}</body>")
        sw.append("<media>")
        entry.mediaRefs.forEach { sw.append("<ref>${escape(it)}</ref>") }
        sw.append("</media>")
        sw.append("<loop nextId=\"${entry.nextId ?: ""}\" prevId=\"${entry.prevId ?: ""}\" />")
        sw.append("</dlof>")
        return sw.toString()
    }

    fun writeLoop(loop: BDlofLoop): String {
        val sw = StringWriter()
        sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        sw.append("<dlofLoop id=\"${loop.id}\" name=\"${escape(loop.name)}\" closed=\"${loop.closed}\">")
        loop.entries.forEach { sw.append(writeEntry(it)) }
        sw.append("</dlofLoop>")
        return sw.toString()
    }

    fun parseEntry(xml: String): DlofEntry {
        val parser = newParser(xml)
        val entry = DlofEntry()
        var tag = ""
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    tag = parser.name
                    when (tag) {
                        "dlof" -> {
                            parser.getAttributeValue(null, "id")?.let { /* keep generated id if absent */ }
                            parser.getAttributeValue(null, "createdAt")?.toLongOrNull()?.let { entry.createdAt = it }
                        }
                        "loop" -> {
                            entry.nextId = parser.getAttributeValue(null, "nextId")?.ifBlank { null }
                            entry.prevId = parser.getAttributeValue(null, "prevId")?.ifBlank { null }
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    when (tag) {
                        "title" -> entry.title = parser.text ?: ""
                        "body" -> entry.body = parser.text ?: ""
                        "ref" -> parser.text?.let { entry.mediaRefs.add(it) }
                    }
                }
            }
            eventType = parser.next()
        }
        return entry
    }

    fun parseLoop(xml: String): BDlofLoop {
        val loop = BDlofLoop()
        // Split into individual <dlof>...</dlof> blocks and parse each with parseEntry.
        val regex = Regex("<dlof\\b.*?</dlof>", RegexOption.DOT_MATCHES_ALL)
        regex.findAll(xml).forEach { loop.entries.add(parseEntry(it.value)) }
        Regex("name=\"(.*?)\"").find(xml.substringBefore("<dlof"))?.let { loop.name = unescape(it.groupValues[1]) }
        loop.closed = xml.substringBefore("<dlof").contains("closed=\"true\"")
        return loop
    }

    private fun newParser(xml: String): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))
        return parser
    }

    private fun escape(s: String): String = s
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun unescape(s: String): String = s
        .replace("&quot;", "\"").replace("&gt;", ">").replace("&lt;", "<")
        .replace("&amp;", "&")
}
