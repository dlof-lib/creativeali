package com.creativeali.app.dlof

import java.util.UUID

/**
 * ══════════════════════════════════════════════════════════════
 * DlofModel — النموذج الكامل لصيغة DLoF (Document Loop Format)
 * مطابق لمخطط https://dlof.org/schema/1.0 (dlof.xsd)
 * ══════════════════════════════════════════════════════════════
 *
 * هذا النموذج يحل محل الشكل المبسّط القديم (DlofEntry/BDlofLoop) ويدعم
 * الملف الكامل كما يقرأه أي تطبيق DLoF متوافق: metadata + loopLinks +
 * content (6 أنواع) + attachments + mediaFolder + template اختياريان.
 *
 * الأشكال القديمة (DlofEntry, BDlofLoop) بقيت كما هي في DlofDocument.kt
 * القديم لأغراض التوافق الخلفي مع مذكرات "التدوين"، لكن أي ملف .dlof
 * جديد يُقرأ أو يُكتب عبر هذا النموذج يكون متوافقًا 100% مع مواصفة dlof.org.
 */

/** جذر أي ملف `.dlof`: العنصر <documentLoop>. */
data class DlofDocumentV2(
    val id: String = UUID.randomUUID().toString(),
    val version: String = "1.0",
    var metadata: DlofMetadata = DlofMetadata(),
    var loopLinks: DlofLoopLinks = DlofLoopLinks(),
    var content: MutableList<DlofContent> = mutableListOf(),
    var attachments: MutableList<DlofAttachment> = mutableListOf(),
    var mediaFolder: MutableList<DlofMediaFile> = mutableListOf(),
    var template: DlofTemplate? = null,
)

data class DlofMetadata(
    var title: String = "",
    var domain: DlofDomain = DlofDomain.CUSTOM,
    var author: String? = null,
    var createdAt: String? = null,   // ISO-8601 dateTime
    var updatedAt: String? = null,
    var language: String = "ar",
    var tags: MutableList<String> = mutableListOf(),
    var signature: DlofSignature? = null,
)

/** جميع المجالات (domain) المدعومة في المواصفة الرسمية. */
enum class DlofDomain(val wire: String) {
    EDUCATION("education"), BOOK("book"), INFO_APP("infoApp"), INFO_LOOP("infoLoop"),
    RECIPE("recipe"), JOURNAL("journal"), SERIES("series"), COMIC("comic"), MANGA("manga"),
    PODCAST("podcast"), MUSIC("music"), CHARACTERS("characters"), SOFTWARE("software"),
    BLOG("blog"), NEWS("news"), ART("art"), GAME("game"), BUSINESS("business"), CUSTOM("custom");

    companion object {
        fun fromWire(v: String?): DlofDomain = entries.firstOrNull { it.wire == v } ?: CUSTOM
    }
}

data class DlofSignature(
    val algorithm: String,
    val value: String,
    val signedBy: String? = null,
    val signedAt: String? = null,
)

data class DlofLoopLinks(
    var previous: DlofLinkRef? = null,
    var next: DlofLinkRef? = null,
    var loopRoot: Boolean = false,
)

data class DlofLinkRef(val ref: String, val title: String? = null)

/** مصدر مزامنة حي اختياري (remoteSync) يمكن إرفاقه بأي نوع محتوى. */
data class DlofRemoteSync(
    val url: String,
    val protocol: SyncProtocol,
    val field: String? = null,
    val etag: String? = null,
    val contentHashSha256: String? = null,
    val syncedAt: String? = null,
    val headersJson: String? = null,
    val transform: String? = null,
    val policy: SyncPolicy = SyncPolicy.NOTIFY,
    val intervalMinutes: Int? = null,
    val targetField: String? = null,
) {
    enum class SyncProtocol { HTTPS, RSS, SPARQL, GIT, WEBHOOK }
    enum class SyncPolicy { MANUAL, NOTIFY, AUTO, READONLY }
}

/** أنواع المحتوى الستة المدعومة في <content> (xs:choice maxOccurs=unbounded). */
sealed class DlofContent {
    abstract val remoteSync: DlofRemoteSync?

    data class Generic(
        val type: String,
        val element: String,
        val body: String,
        val customType: String? = null,
        override val remoteSync: DlofRemoteSync? = null,
    ) : DlofContent()

    data class Qa(
        val question: String,
        val answer: String,
        val explanation: String? = null,
        val difficulty: String? = null,
        override val remoteSync: DlofRemoteSync? = null,
    ) : DlofContent()

    data class BookChapter(
        val chapterNumber: Int? = null,
        val chapterTitle: String,
        val text: String,
        val summary: String? = null,
        override val remoteSync: DlofRemoteSync? = null,
    ) : DlofContent()

    data class TermDefinition(
        val term: String,
        val definition: String,
        val example: String? = null,
        override val remoteSync: DlofRemoteSync? = null,
    ) : DlofContent()

    data class InfoExplain(
        val topic: String,
        val explanation: String,
        val source: String? = null,
        override val remoteSync: DlofRemoteSync? = null,
    ) : DlofContent()

    data class Episode(
        val episodeNumber: Int? = null,
        val seasonNumber: Int? = null,
        val episodeTitle: String,
        val synopsis: String? = null,
        val durationSeconds: Int? = null,
        val seriesTitle: String? = null,
        val mediaRef: String? = null,
        val releaseDate: String? = null,
        val body: String? = null,
        val thumbnailBase64: String? = null,
        override val remoteSync: DlofRemoteSync? = null,
    ) : DlofContent()
}

enum class DlofAttachmentKind(val wire: String) {
    IMAGE("image"), VIDEO("video"), AUDIO("audio"), SUBTITLE("subtitle"), FILE("file");
    companion object {
        fun fromWire(v: String?): DlofAttachmentKind = entries.firstOrNull { it.wire == v } ?: FILE
        fun fromExtension(ext: String): DlofAttachmentKind = when (ext.lowercase()) {
            "png", "jpg", "jpeg", "gif", "webp", "svg", "bmp", "tiff", "ico", "heic" -> IMAGE
            "mp4", "mkv", "avi", "mov", "webm", "flv", "wmv", "m4v", "3gp" -> VIDEO
            "mp3", "wav", "ogg", "aac", "flac", "m4a", "wma", "opus" -> AUDIO
            "srt", "vtt", "ass", "ssa" -> SUBTITLE
            else -> FILE
        }
    }
}

data class DlofAttachment(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val mimeType: String,
    val kind: DlofAttachmentKind,
    val sizeBytes: Long? = null,
    val dataBase64: String? = null,   // مضمّن داخل الملف
    val uri: String? = null,          // أو مرجع خارجي/مسار نسبي في media/
    val caption: String? = null,
)

/** فهرس ملف واحد داخل مجلد media/ المجاور (بدون تضمين base64). */
data class DlofMediaFile(
    val path: String,
    val kind: DlofAttachmentKind,
    val label: String? = null,
)

enum class DlofTemplateLayout(val wire: String) {
    STANDARD("standard"), CARD("card"), MAGAZINE("magazine"), MINIMAL("minimal");
    companion object {
        fun fromWire(v: String?): DlofTemplateLayout = entries.firstOrNull { it.wire == v } ?: STANDARD
    }
}

data class DlofTemplate(
    val ref: String? = null,
    val primaryColor: String? = null,
    val secondaryColor: String? = null,
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val fontFamily: String? = null,
    val layout: DlofTemplateLayout = DlofTemplateLayout.STANDARD,
    val headerAttachmentRef: String? = null,
)
