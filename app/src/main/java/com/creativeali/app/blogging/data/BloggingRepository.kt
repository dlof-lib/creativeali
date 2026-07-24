package com.creativeali.app.blogging.data

import com.creativeali.app.blogging.BDlofLoop
import com.creativeali.app.blogging.DlofEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

const val DEFAULT_LOOP_ID = "default-loop"

class BloggingRepository(private val dao: BloggingDao) {

    suspend fun ensureDefaultLoop() = dao.ensureLoopExists(DEFAULT_LOOP_ID, "مذكراتي")

    fun observeLoop(loopId: String = DEFAULT_LOOP_ID): Flow<BDlofLoop> =
        dao.observeLoop(loopId).combine(dao.observeEntries(loopId)) { loopEntity, entryEntities ->
            val loop = BDlofLoop(
                id = loopEntity?.id ?: loopId,
                name = loopEntity?.name ?: "مذكراتي",
                closed = loopEntity?.closed ?: false,
            )
            loop.entries.addAll(entryEntities.map { it.toDomain() })
            loop
        }

    suspend fun saveEntry(loopId: String = DEFAULT_LOOP_ID, entry: DlofEntry) {
        val order = dao.existingOrderIndex(entry.id) ?: (dao.maxOrderIndex(loopId) + 1)
        dao.upsertEntry(entry.toEntity(loopId, order))
    }

    suspend fun deleteEntry(entryId: String) = dao.deleteEntry(entryId)
}

private fun EntryEntity.toDomain() = DlofEntry(
    id = id, title = title, body = body, createdAt = createdAt,
    mediaRefs = mediaRefs.toMutableList(),
)

private fun DlofEntry.toEntity(loopId: String, orderIndex: Int) = EntryEntity(
    id = id, loopId = loopId, title = title, body = body, createdAt = createdAt,
    orderIndex = orderIndex, mediaRefs = mediaRefs,
)
