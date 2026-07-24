package com.creativeali.app.blogging

import java.util.UUID

/**
 * A single journal/memoir entry stored as one `.dlof` file.
 * `.b.dlof` is the same schema, saved with the `.b.dlof` extension to mark it
 * as part of a blog *loop* (see [BDlofLoop]) rather than a standalone document.
 */
data class DlofEntry(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var body: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var mediaRefs: MutableList<String> = mutableListOf(), // filenames under media/
    var nextId: String? = null,   // loop link: next entry
    var prevId: String? = null,   // loop link: previous entry
)

/**
 * A `.b.dlof` blog loop: an ordered, self-referential chain of entries.
 * The loop is "closed" when [closed] is true, meaning the last entry's
 * [DlofEntry.nextId] points back to the first entry's id.
 */
data class BDlofLoop(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "مذكراتي",
    val entries: MutableList<DlofEntry> = mutableListOf(),
    var closed: Boolean = false,
) {
    fun relink() {
        for (i in entries.indices) {
            entries[i].prevId = if (i > 0) entries[i - 1].id else if (closed) entries.last().id else null
            entries[i].nextId = if (i < entries.size - 1) entries[i + 1].id else if (closed) entries.first().id else null
        }
    }
}
