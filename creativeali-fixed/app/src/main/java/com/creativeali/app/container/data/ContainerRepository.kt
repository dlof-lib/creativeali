package com.creativeali.app.container.data

import com.creativeali.app.container.DlofContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContainerRepository(private val dao: ContainerDao) {

    fun observeAll(): Flow<List<DlofContainer>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeOne(containerId: String): Flow<DlofContainer?> =
        dao.observeOne(containerId).map { it?.toDomain() }

    suspend fun save(container: DlofContainer) = dao.upsert(container.toEntity())

    suspend fun delete(containerId: String) = dao.delete(containerId)
}

private fun ContainerEntity.toDomain() = DlofContainer(
    id = id, name = name, description = description, iconUri = iconUri,
    badges = badges.toMutableList(), allowSetTxt = allowSetTxt, licenseText = licenseText,
    blogLoopId = blogLoopId, diagramId = diagramId, createdAt = createdAt,
)

private fun DlofContainer.toEntity() = ContainerEntity(
    id = id, name = name, description = description, iconUri = iconUri,
    badges = badges, allowSetTxt = allowSetTxt, licenseText = licenseText,
    blogLoopId = blogLoopId, diagramId = diagramId, createdAt = createdAt,
)
