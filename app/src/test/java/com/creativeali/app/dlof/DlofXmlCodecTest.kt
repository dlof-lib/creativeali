package com.creativeali.app.dlof

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DlofXmlCodecTest {

    @Test
    fun `writes and re-parses a qaItem document round trip`() {
        val doc = DlofDocumentV2(
            metadata = DlofMetadata(title = "سؤال تجريبي", domain = DlofDomain.EDUCATION, language = "ar"),
            loopLinks = DlofLoopLinks(next = DlofLinkRef("q2"), loopRoot = true),
        )
        doc.content.add(DlofContent.Qa(question = "ما عاصمة مصر؟", answer = "القاهرة", difficulty = "easy"))

        val xml = DlofXmlCodec.write(doc)
        val reparsed = requireNotNull(DlofXmlCodec.parse(xml)) { "DlofXmlCodec.parse returned null for qaItem round-trip" }

        assertEquals("سؤال تجريبي", reparsed.metadata.title)
        assertEquals(DlofDomain.EDUCATION, reparsed.metadata.domain)
        assertTrue(reparsed.loopLinks.loopRoot)
        assertEquals("q2", reparsed.loopLinks.next?.ref)
        val qa = reparsed.content.first() as DlofContent.Qa
        assertEquals("ما عاصمة مصر؟", qa.question)
        assertEquals("القاهرة", qa.answer)
    }

    @Test
    fun `parses episodeItem with season and duration`() {
        val doc = DlofDocumentV2(metadata = DlofMetadata(title = "الحلقة 1", domain = DlofDomain.SERIES))
        doc.content.add(
            DlofContent.Episode(
                episodeNumber = 1, seasonNumber = 2, episodeTitle = "البداية",
                durationSeconds = 1800, seriesTitle = "مسلسلي",
            )
        )
        val reparsed = requireNotNull(DlofXmlCodec.parse(DlofXmlCodec.write(doc))) { "DlofXmlCodec.parse returned null for episodeItem round-trip" }
        val ep = reparsed.content.first() as DlofContent.Episode
        assertEquals(1, ep.episodeNumber)
        assertEquals(2, ep.seasonNumber)
        assertEquals(1800, ep.durationSeconds)
        assertEquals("مسلسلي", ep.seriesTitle)
    }

    @Test
    fun `round trips attachments and template`() {
        val doc = DlofDocumentV2(metadata = DlofMetadata(title = "مع مرفقات", domain = DlofDomain.COMIC))
        doc.content.add(DlofContent.Generic(type = "panel", element = "page1", body = "..."))
        doc.attachments.add(DlofAttachment(fileName = "cover.jpg", mimeType = "image/jpeg", kind = DlofAttachmentKind.IMAGE, uri = "media/images/cover.jpg"))
        doc.template = DlofTemplate(primaryColor = "#FF0000", layout = DlofTemplateLayout.CARD)

        val reparsed = requireNotNull(DlofXmlCodec.parse(DlofXmlCodec.write(doc))) { "DlofXmlCodec.parse returned null for attachments/template round-trip" }
        assertEquals(1, reparsed.attachments.size)
        assertEquals("cover.jpg", reparsed.attachments.first().fileName)
        assertEquals(DlofTemplateLayout.CARD, reparsed.template?.layout)
        assertEquals("#FF0000", reparsed.template?.primaryColor)
    }
}
