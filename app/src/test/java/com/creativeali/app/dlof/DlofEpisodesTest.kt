package com.creativeali.app.dlof

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DlofEpisodesTest {

    @Test
    fun `validates complete sequence`() {
        val result = DlofEpisodes.validateSequence(listOf(1, 2, 3, 4))
        assertTrue(result.valid)
    }

    @Test
    fun `detects missing episode in sequence`() {
        val result = DlofEpisodes.validateSequence(listOf(1, 2, 4))
        assertFalse(result.valid)
        assertTrue(result.message.contains("3"))
    }

    @Test
    fun `formats roman numerals correctly`() {
        assertEquals("IV", DlofEpisodes.formatEpisodeNumber(4, DlofEpisodes.NumberingStyle.ROMAN))
        assertEquals("IX", DlofEpisodes.formatEpisodeNumber(9, DlofEpisodes.NumberingStyle.ROMAN))
    }

    @Test
    fun `links episodes to previous and next automatically`() {
        val episodes = listOf(
            DlofEpisodes.buildEpisodeDocument(1, "الحلقة الأولى"),
            DlofEpisodes.buildEpisodeDocument(2, "الحلقة الثانية"),
            DlofEpisodes.buildEpisodeDocument(3, "الحلقة الثالثة"),
        )
        val linked = DlofEpisodes.linkEpisodes(episodes)
        assertEquals(null, linked[0].loopLinks.previous)
        assertEquals(episodes[1].id, linked[0].loopLinks.next?.ref)
        assertEquals(episodes[0].id, linked[1].loopLinks.previous?.ref)
        assertEquals(episodes[2].id, linked[1].loopLinks.next?.ref)
        assertEquals(null, linked[2].loopLinks.next)
    }
}
