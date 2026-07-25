package com.creativeali.app.dlof

import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.io.StringWriter

/**
 * قارئ وكاتب كامل لصيغة DLoF مطابق لـ dlof.xsd (schema v1.0 + remoteSync/mediaFolder).
 * يدعم كل حقول <documentLoop>: metadata, loopLinks, content (6 أنواع), attachments,
 * mediaFolder, template. لا يدعم <webPublish> بعد (مخطط له في المرحلة القادمة).
 */
object DlofXmlCodec {

    // ═══════════════════════════ القراءة ═══════════════════════════

    fun parse(xml: String): DlofDocumentV2 {
        val parser = newParser(xml)
        val doc = DlofDocumentV2()
        var text = StringBuilder()
        val stack = ArrayDeque<String>()

        // حالة مؤقتة أثناء بناء عناصر معقدة
        var curTagAttrs: Map<String, String> = emptyMap()
        var curLinkRef: DlofLinkRef? = null
        var curAttachment: MutableMap<String, String>? = null
        var curContentType: String? = null
        val curContentFields = mutableMapOf<String, String>()
        var curContentAttrs: Map<String, String> = emptyMap()
        var inRemoteSync = false
        val remoteSyncFields = mutableMapOf<String, String>()
        var remoteSyncAttrs: Map<String, String> = emptyMap()

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name
                    stack.addLast(name)
                    curTagAttrs = attrsOf(parser)
                    text = StringBuilder()

                    when (name) {
                        "documentLoop" -> { /* id/version read at END via attrs captured below */ }
                        "previous", "next" -> curLinkRef = DlofLinkRef(
                            ref = curTagAttrs["ref"] ?: "",
                            title = curTagAttrs["title"]
                        )
                        "attachment" -> curAttachment = mutableMapOf(
                            "id" to (curTagAttrs["id"] ?: ""),
                            "fileName" to (curTagAttrs["fileName"] ?: ""),
                            "mimeType" to (curTagAttrs["mimeType"] ?: ""),
                            "kind" to (curTagAttrs["kind"] ?: "file"),
                            "sizeBytes" to (curTagAttrs["sizeBytes"] ?: "")
                        )
                        "mediaFile" -> {
                            doc.mediaFolder.add(
                                DlofMediaFile(
                                    path = curTagAttrs["path"] ?: "",
                                    kind = DlofAttachmentKind.fromWire(curTagAttrs["kind"]),
                                    label = curTagAttrs["label"]
                                )
                            )
                        }
                        "template" -> doc.template = DlofTemplate(
                            ref = curTagAttrs["ref"],
                            primaryColor = curTagAttrs["primaryColor"],
                            secondaryColor = curTagAttrs["secondaryColor"],
                            backgroundColor = curTagAttrs["backgroundColor"],
                            textColor = curTagAttrs["textColor"],
                            fontFamily = curTagAttrs["fontFamily"],
                            layout = DlofTemplateLayout.fromWire(curTagAttrs["layout"]),
                            headerAttachmentRef = curTagAttrs["headerAttachmentRef"]
                        )
                        "genericItem", "qaItem", "bookChapter", "termDefinition",
                        "infoExplain", "episodeItem" -> {
                            curContentType = name
                            curContentAttrs = curTagAttrs
                            curContentFields.clear()
                        }
                        "remoteSync" -> {
                            inRemoteSync = true
                            remoteSyncAttrs = curTagAttrs
                            remoteSyncFields.clear()
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    if (!parser.isWhitespace) text.append(parser.text)
                }

                XmlPullParser.END_TAG -> {
                    val name = parser.name
                    val value = text.toString()

                    if (inRemoteSync && name != "remoteSync") {
                        remoteSyncFields[name] = value
                    } else if (curContentType != null && name != curContentType && name != "remoteSync") {
                        curContentFields[name] = value
                    }

                    when (name) {
                        "documentLoop" -> { /* handled via top-level attrs at parse start below */ }
                        "title" -> if (stack.size >= 2 && stack[stack.size - 2] == "metadata") doc.metadata.title = value
                        "domain" -> doc.metadata.domain = DlofDomain.fromWire(value)
                        "author" -> doc.metadata.author = value
                        "createdAt" -> if (stack.getOrNull(stack.size - 2) == "metadata") doc.metadata.createdAt = value
                        "updatedAt" -> doc.metadata.updatedAt = value
                        "language" -> doc.metadata.language = value
                        "tag" -> doc.metadata.tags.add(value)
                        "loopRoot" -> doc.loopLinks.loopRoot = value.toBoolean()
                        "previous" -> doc.loopLinks.previous = curLinkRef
                        "next" -> doc.loopLinks.next = curLinkRef

                        "data" -> curAttachment?.put("data", value)
                        "uri" -> if (curAttachment != null) curAttachment!!["uri"] = value
                        "caption" -> if (curAttachment != null) curAttachment!!["caption"] = value
                        "attachment" -> curAttachment?.let { a ->
                            doc.attachments.add(
                                DlofAttachment(
                                    id = a["id"] ?: "",
                                    fileName = a["fileName"] ?: "",
                                    mimeType = a["mimeType"] ?: "",
                                    kind = DlofAttachmentKind.fromWire(a["kind"]),
                                    sizeBytes = a["sizeBytes"]?.toLongOrNull(),
                                    dataBase64 = a["data"],
                                    uri = a["uri"],
                                    caption = a["caption"],
                                )
                            )
                            curAttachment = null
                        }

                        "remoteSync" -> inRemoteSync = false

                        "genericItem", "qaItem", "bookChapter", "termDefinition",
                        "infoExplain", "episodeItem" -> {
                            val rs = if (remoteSyncFields.isNotEmpty()) buildRemoteSync(remoteSyncFields, remoteSyncAttrs) else null
                            doc.content.add(buildContent(name, curContentFields, curContentAttrs, rs))
                            curContentType = null
                            remoteSyncFields.clear()
                        }
                    }
                    if (stack.isNotEmpty()) stack.removeLast()
                    text = StringBuilder()
                }
            }
            event = parser.next()
        }

        // id/version من العنصر الجذر (قراءة ثانية سريعة عبر regex بسيط، أخف من إعادة تشغيل parser).
        // يقبل كلا الشكلين: <documentLoop> الرسمي حسب dlof.xsd، و<dlof> (شكل مختصر يصادَف
        // في بعض الملفات المولّدة، مثل قوالب الحلقات) لأقصى توافق عند القراءة.
        Regex("<(?:documentLoop|dlof)\\b[^>]*\\bid=\"([^\"]*)\"").find(xml)?.let {
            return doc.copy(
                id = it.groupValues[1],
                version = Regex("version=\"([^\"]*)\"").find(xml)?.groupValues?.get(1) ?: "1.0",
            )
        }
        return doc
    }

    private fun buildRemoteSync(f: Map<String, String>, attrs: Map<String, String>): DlofRemoteSync {
        return DlofRemoteSync(
            url = f["url"] ?: "",
            protocol = when (f["protocol"]) {
                "rss" -> DlofRemoteSync.SyncProtocol.RSS
                "sparql" -> DlofRemoteSync.SyncProtocol.SPARQL
                "git" -> DlofRemoteSync.SyncProtocol.GIT
                "webhook" -> DlofRemoteSync.SyncProtocol.WEBHOOK
                else -> DlofRemoteSync.SyncProtocol.HTTPS
            },
            field = f["field"],
            etag = f["etag"],
            syncedAt = f["syncedAt"],
            headersJson = f["headers"],
            transform = f["transform"],
            policy = when (attrs["policy"]) {
                "manual" -> DlofRemoteSync.SyncPolicy.MANUAL
                "auto" -> DlofRemoteSync.SyncPolicy.AUTO
                "readonly" -> DlofRemoteSync.SyncPolicy.READONLY
                else -> DlofRemoteSync.SyncPolicy.NOTIFY
            },
            intervalMinutes = attrs["intervalMinutes"]?.toIntOrNull(),
            targetField = attrs["targetField"],
        )
    }

    private fun buildContent(
        tag: String,
        f: Map<String, String>,
        attrs: Map<String, String>,
        rs: DlofRemoteSync?,
    ): DlofContent = when (tag) {
        "genericItem" -> DlofContent.Generic(
            type = f["type"] ?: "", element = f["element"] ?: "", body = f["body"] ?: "",
            customType = attrs["customType"], remoteSync = rs,
        )
        "qaItem" -> DlofContent.Qa(
            question = f["question"] ?: "", answer = f["answer"] ?: "",
            explanation = f["explanation"], difficulty = f["difficulty"], remoteSync = rs,
        )
        "bookChapter" -> DlofContent.BookChapter(
            chapterNumber = f["chapterNumber"]?.toIntOrNull(), chapterTitle = f["chapterTitle"] ?: "",
            text = f["text"] ?: "", summary = f["summary"], remoteSync = rs,
        )
        "termDefinition" -> DlofContent.TermDefinition(
            term = f["term"] ?: "", definition = f["definition"] ?: "", example = f["example"], remoteSync = rs,
        )
        "infoExplain" -> DlofContent.InfoExplain(
            topic = f["topic"] ?: "", explanation = f["explanation"] ?: "", source = f["source"], remoteSync = rs,
        )
        "episodeItem" -> DlofContent.Episode(
            episodeNumber = f["episodeNumber"]?.toIntOrNull(), seasonNumber = f["seasonNumber"]?.toIntOrNull(),
            episodeTitle = f["episodeTitle"] ?: "", synopsis = f["synopsis"],
            durationSeconds = f["duration"]?.toIntOrNull(), seriesTitle = f["seriesTitle"],
            mediaRef = f["mediaRef"], releaseDate = f["releaseDate"], body = f["body"],
            thumbnailBase64 = f["thumbnailBase64"], remoteSync = rs,
        )
        else -> DlofContent.Generic(type = "unknown", element = tag, body = "")
    }

    private fun attrsOf(parser: XmlPullParser): Map<String, String> {
        val m = mutableMapOf<String, String>()
        for (i in 0 until parser.attributeCount) m[parser.getAttributeName(i)] = parser.getAttributeValue(i)
        return m
    }

    private fun newParser(xml: String): XmlPullParser {
        // نستخدم كائن kxml2 مباشرة بدل XmlPullParserFactory.newInstance():
        // في android.jar الفعلي على الجهاز هذا يعمل تلقائيًا، لكن أثناء اختبارات
        // الوحدة (JVM وليس جهاز حقيقي) تكون فئات org.xmlpull.v1.* في android.jar
        // مجرد stubs، ومع isReturnDefaultValues=true فإن newInstance() يُرجع null
        // بصمت بدل رمي استثناء، فيسبب NullPointerException عند أول استخدام له.
        val parser: XmlPullParser = org.kxml2.io.KXmlParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))
        return parser
    }

    // ═══════════════════════════ الكتابة ═══════════════════════════

    fun write(doc: DlofDocumentV2): String {
        val sw = StringWriter()
        sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sw.append("<documentLoop xmlns=\"https://dlof.org/schema/1.0\" version=\"${esc(doc.version)}\" id=\"${esc(doc.id)}\">\n")

        sw.append("  <metadata>\n")
        sw.append("    <title>${esc(doc.metadata.title)}</title>\n")
        sw.append("    <domain>${doc.metadata.domain.wire}</domain>\n")
        doc.metadata.author?.let { sw.append("    <author>${esc(it)}</author>\n") }
        doc.metadata.createdAt?.let { sw.append("    <createdAt>${esc(it)}</createdAt>\n") }
        doc.metadata.updatedAt?.let { sw.append("    <updatedAt>${esc(it)}</updatedAt>\n") }
        sw.append("    <language>${esc(doc.metadata.language)}</language>\n")
        if (doc.metadata.tags.isNotEmpty()) {
            sw.append("    <tags>")
            doc.metadata.tags.forEach { sw.append("<tag>${esc(it)}</tag>") }
            sw.append("</tags>\n")
        }
        doc.metadata.signature?.let {
            sw.append("    <signature algorithm=\"${esc(it.algorithm)}\" value=\"${esc(it.value)}\"")
            it.signedBy?.let { s -> sw.append(" signedBy=\"${esc(s)}\"") }
            it.signedAt?.let { s -> sw.append(" signedAt=\"${esc(s)}\"") }
            sw.append("/>\n")
        }
        sw.append("  </metadata>\n")

        sw.append("  <loopLinks>\n")
        doc.loopLinks.previous?.let {
            sw.append("    <previous ref=\"${esc(it.ref)}\"")
            it.title?.let { t -> sw.append(" title=\"${esc(t)}\"") }
            sw.append("/>\n")
        }
        doc.loopLinks.next?.let {
            sw.append("    <next ref=\"${esc(it.ref)}\"")
            it.title?.let { t -> sw.append(" title=\"${esc(t)}\"") }
            sw.append("/>\n")
        }
        sw.append("    <loopRoot>${doc.loopLinks.loopRoot}</loopRoot>\n")
        sw.append("  </loopLinks>\n")

        sw.append("  <content>\n")
        doc.content.forEach { writeContent(sw, it) }
        sw.append("  </content>\n")

        if (doc.attachments.isNotEmpty()) {
            sw.append("  <attachments>\n")
            doc.attachments.forEach { a ->
                sw.append("    <attachment id=\"${esc(a.id)}\" fileName=\"${esc(a.fileName)}\" mimeType=\"${esc(a.mimeType)}\" kind=\"${a.kind.wire}\"")
                a.sizeBytes?.let { sw.append(" sizeBytes=\"$it\"") }
                sw.append(">\n")
                a.dataBase64?.let { sw.append("      <data>$it</data>\n") }
                a.uri?.let { sw.append("      <uri>${esc(it)}</uri>\n") }
                a.caption?.let { sw.append("      <caption>${esc(it)}</caption>\n") }
                sw.append("    </attachment>\n")
            }
            sw.append("  </attachments>\n")
        }

        if (doc.mediaFolder.isNotEmpty()) {
            sw.append("  <mediaFolder>\n")
            doc.mediaFolder.forEach { m ->
                sw.append("    <mediaFile path=\"${esc(m.path)}\" kind=\"${m.kind.wire}\"")
                m.label?.let { sw.append(" label=\"${esc(it)}\"") }
                sw.append("/>\n")
            }
            sw.append("  </mediaFolder>\n")
        }

        doc.template?.let { t ->
            sw.append("  <template")
            t.ref?.let { sw.append(" ref=\"${esc(it)}\"") }
            t.primaryColor?.let { sw.append(" primaryColor=\"${esc(it)}\"") }
            t.secondaryColor?.let { sw.append(" secondaryColor=\"${esc(it)}\"") }
            t.backgroundColor?.let { sw.append(" backgroundColor=\"${esc(it)}\"") }
            t.textColor?.let { sw.append(" textColor=\"${esc(it)}\"") }
            t.fontFamily?.let { sw.append(" fontFamily=\"${esc(it)}\"") }
            sw.append(" layout=\"${t.layout.wire}\"")
            t.headerAttachmentRef?.let { sw.append(" headerAttachmentRef=\"${esc(it)}\"") }
            sw.append("/>\n")
        }

        sw.append("</documentLoop>\n")
        return sw.toString()
    }

    private fun writeContent(sw: StringWriter, c: DlofContent) {
        when (c) {
            is DlofContent.Generic -> {
                sw.append("    <genericItem")
                c.customType?.let { sw.append(" customType=\"${esc(it)}\"") }
                sw.append(">\n")
                sw.append("      <type>${esc(c.type)}</type>\n")
                sw.append("      <element>${esc(c.element)}</element>\n")
                sw.append("      <body>${esc(c.body)}</body>\n")
                writeRemoteSync(sw, c.remoteSync)
                sw.append("    </genericItem>\n")
            }
            is DlofContent.Qa -> {
                sw.append("    <qaItem>\n")
                sw.append("      <question>${esc(c.question)}</question>\n")
                sw.append("      <answer>${esc(c.answer)}</answer>\n")
                c.explanation?.let { sw.append("      <explanation>${esc(it)}</explanation>\n") }
                c.difficulty?.let { sw.append("      <difficulty>${esc(it)}</difficulty>\n") }
                writeRemoteSync(sw, c.remoteSync)
                sw.append("    </qaItem>\n")
            }
            is DlofContent.BookChapter -> {
                sw.append("    <bookChapter>\n")
                c.chapterNumber?.let { sw.append("      <chapterNumber>$it</chapterNumber>\n") }
                sw.append("      <chapterTitle>${esc(c.chapterTitle)}</chapterTitle>\n")
                sw.append("      <text>${esc(c.text)}</text>\n")
                c.summary?.let { sw.append("      <summary>${esc(it)}</summary>\n") }
                writeRemoteSync(sw, c.remoteSync)
                sw.append("    </bookChapter>\n")
            }
            is DlofContent.TermDefinition -> {
                sw.append("    <termDefinition>\n")
                sw.append("      <term>${esc(c.term)}</term>\n")
                sw.append("      <definition>${esc(c.definition)}</definition>\n")
                c.example?.let { sw.append("      <example>${esc(it)}</example>\n") }
                writeRemoteSync(sw, c.remoteSync)
                sw.append("    </termDefinition>\n")
            }
            is DlofContent.InfoExplain -> {
                sw.append("    <infoExplain>\n")
                sw.append("      <topic>${esc(c.topic)}</topic>\n")
                sw.append("      <explanation>${esc(c.explanation)}</explanation>\n")
                c.source?.let { sw.append("      <source>${esc(it)}</source>\n") }
                writeRemoteSync(sw, c.remoteSync)
                sw.append("    </infoExplain>\n")
            }
            is DlofContent.Episode -> {
                sw.append("    <episodeItem>\n")
                c.episodeNumber?.let { sw.append("      <episodeNumber>$it</episodeNumber>\n") }
                c.seasonNumber?.let { sw.append("      <seasonNumber>$it</seasonNumber>\n") }
                sw.append("      <episodeTitle>${esc(c.episodeTitle)}</episodeTitle>\n")
                c.synopsis?.let { sw.append("      <synopsis>${esc(it)}</synopsis>\n") }
                c.durationSeconds?.let { sw.append("      <duration>$it</duration>\n") }
                c.seriesTitle?.let { sw.append("      <seriesTitle>${esc(it)}</seriesTitle>\n") }
                c.mediaRef?.let { sw.append("      <mediaRef>${esc(it)}</mediaRef>\n") }
                c.releaseDate?.let { sw.append("      <releaseDate>${esc(it)}</releaseDate>\n") }
                c.body?.let { sw.append("      <body>${esc(it)}</body>\n") }
                c.thumbnailBase64?.let { sw.append("      <thumbnailBase64>$it</thumbnailBase64>\n") }
                writeRemoteSync(sw, c.remoteSync)
                sw.append("    </episodeItem>\n")
            }
        }
    }

    private fun writeRemoteSync(sw: StringWriter, rs: DlofRemoteSync?) {
        if (rs == null) return
        sw.append("      <remoteSync policy=\"${rs.policy.name.lowercase()}\"")
        rs.intervalMinutes?.let { sw.append(" intervalMinutes=\"$it\"") }
        rs.targetField?.let { sw.append(" targetField=\"${esc(it)}\"") }
        sw.append(">\n")
        sw.append("        <url>${esc(rs.url)}</url>\n")
        sw.append("        <protocol>${rs.protocol.name.lowercase()}</protocol>\n")
        rs.field?.let { sw.append("        <field>${esc(it)}</field>\n") }
        rs.etag?.let { sw.append("        <etag>${esc(it)}</etag>\n") }
        rs.syncedAt?.let { sw.append("        <syncedAt>${esc(it)}</syncedAt>\n") }
        rs.headersJson?.let { sw.append("        <headers>${esc(it)}</headers>\n") }
        rs.transform?.let { sw.append("        <transform>${esc(it)}</transform>\n") }
        sw.append("      </remoteSync>\n")
    }

    private fun esc(s: String): String = s
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;")
}
