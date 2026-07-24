package com.creativeali.app.container

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creativeali.app.container.data.ContainerRepository
import com.creativeali.app.data.CreativeAliDatabase
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContainerViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ContainerRepository(CreativeAliDatabase.get(app).containerDao())

    private val _containers = MutableStateFlow<List<DlofContainer>>(emptyList())
    val containers: StateFlow<List<DlofContainer>> = _containers

    init {
        viewModelScope.launch {
            repo.observeAll().collect { _containers.value = it }
        }
    }

    /** ينشئ حاوية جديدة بمعرّفَي مذكرة ومخطط خاصَّين بها ويعيد الحاوية المحفوظة. */
    fun create(
        name: String,
        description: String,
        iconUri: String?,
        badges: List<DlofBadge>,
        allowSetTxt: Boolean,
        licenseText: String?,
    ): DlofContainer {
        val container = DlofContainer(
            name = name.ifBlank { "حاوية بدون اسم" },
            description = description,
            iconUri = iconUri,
            badges = badges.toMutableList(),
            allowSetTxt = allowSetTxt,
            licenseText = licenseText?.ifBlank { null },
            blogLoopId = "loop-${UUID.randomUUID()}",
            diagramId = "diagram-${UUID.randomUUID()}",
        )
        viewModelScope.launch { repo.save(container) }
        return container
    }

    fun rename(container: DlofContainer, newName: String) =
        viewModelScope.launch { repo.save(container.copy(name = newName.ifBlank { container.name })) }

    fun updateDetails(container: DlofContainer, description: String) =
        viewModelScope.launch { repo.save(container.copy(description = description)) }

    fun addBadge(container: DlofContainer, badge: DlofBadge) =
        viewModelScope.launch {
            repo.save(container.copy(badges = (container.badges + badge).toMutableList()))
        }

    fun delete(container: DlofContainer) = viewModelScope.launch { repo.delete(container.id) }
}
