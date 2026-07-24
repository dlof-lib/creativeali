package com.creativeali.app.dlof

import java.io.File

/**
 * يُدير هيكل الحلقات (episodeItem) داخل مسلسل/سلسلة — مطابق لمنطق
 * EpisodeHelper (ep.kt) في تطبيق DLoF المرجعي، لكنه يعمل مباشرة مع
 * [DlofDocumentV2] بدل بناء XML يدويًا.
 */
object DlofEpisodes {

    enum class NumberingStyle { ARABIC, LATIN, ROMAN, CUSTOM }
    data class ValidationResult(val valid: Boolean, val message: String)

    /** ينشئ حلقة جديدة كملف .dlof كامل (domain=series) ويربطها تلقائيًا بالحلقة السابقة. */
    fun buildEpisodeDocument(
        number: Int,
        title: String,
        seriesTitle: String? = null,
        seasonNumber: Int? = null,
        previousEpisodeRef: String? = null,
        nextEpisodeRef: String? = null,
    ): DlofDocumentV2 {
        val doc = DlofDocumentV2(
            metadata = DlofMetadata(
                title = title,
                domain = DlofDomain.SERIES,
                language = "ar",
            ),
            loopLinks = DlofLoopLinks(
                previous = previousEpisodeRef?.let { DlofLinkRef(it) },
                next = nextEpisodeRef?.let { DlofLinkRef(it) },
            ),
        )
        doc.content.add(
            DlofContent.Episode(
                episodeNumber = number,
                seasonNumber = seasonNumber,
                episodeTitle = title,
                seriesTitle = seriesTitle,
            )
        )
        return doc
    }

    /** يقرأ كل ملفات الحلقات (episode*.dlof) من مجلد ويرتّبها حسب الرقم. */
    fun readEpisodes(episodesDir: File): List<Pair<File, DlofDocumentV2>> {
        if (!episodesDir.exists()) return emptyList()
        return episodesDir.listFiles { f -> f.isFile && f.extension in listOf("dlof", "ep", "episode") }
            ?.mapNotNull { file ->
                runCatching { file to DlofXmlCodec.parse(file.readText()) }.getOrNull()
            }
            ?.sortedBy { (_, doc) -> (doc.content.firstOrNull() as? DlofContent.Episode)?.episodeNumber ?: 0 }
            ?: emptyList()
    }

    /** يربط كل حلقة بالسابقة/التالية تلقائيًا (loopLinks) حسب ترتيبها. */
    fun linkEpisodes(episodes: List<DlofDocumentV2>): List<DlofDocumentV2> =
        episodes.mapIndexed { index, doc ->
            doc.copy(
                loopLinks = doc.loopLinks.copy(
                    previous = if (index > 0) DlofLinkRef(episodes[index - 1].id) else null,
                    next = if (index < episodes.size - 1) DlofLinkRef(episodes[index + 1].id) else null,
                )
            )
        }

    fun formatEpisodeNumber(number: Int, style: NumberingStyle = NumberingStyle.ARABIC): String = when (style) {
        NumberingStyle.ARABIC -> toArabicNumber(number)
        NumberingStyle.LATIN -> number.toString()
        NumberingStyle.ROMAN -> number.toRoman()
        NumberingStyle.CUSTOM -> "EP${number.toString().padStart(3, '0')}"
    }

    /** يتحقق من عدم وجود ثغرات في تسلسل أرقام الحلقات. */
    fun validateSequence(episodeNumbers: List<Int>): ValidationResult {
        if (episodeNumbers.isEmpty()) return ValidationResult(false, "لا توجد حلقات")
        val sorted = episodeNumbers.sorted()
        val expected = (1..sorted.size).toList()
        return if (sorted == expected) {
            ValidationResult(true, "تسلسل الحلقات صحيح: ${sorted.size} حلقة")
        } else {
            val missing = expected - sorted.toSet()
            ValidationResult(false, "حلقات مفقودة: ${missing.joinToString(", ")}")
        }
    }

    private fun Int.toRoman(): String {
        val values = listOf(
            1000 to "M", 900 to "CM", 500 to "D", 400 to "CD", 100 to "C", 90 to "XC",
            50 to "L", 40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I"
        )
        var n = this
        return buildString { for ((v, s) in values) while (n >= v) { append(s); n -= v } }
    }

    private fun toArabicNumber(n: Int): String = n.toString()
        .replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣").replace("4", "٤")
        .replace("5", "٥").replace("6", "٦").replace("7", "٧").replace("8", "٨").replace("9", "٩")
}
