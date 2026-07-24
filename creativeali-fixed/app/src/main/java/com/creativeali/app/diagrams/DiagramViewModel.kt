package com.creativeali.app.diagrams

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.creativeali.app.data.CreativeAliDatabase
import com.creativeali.app.diagrams.data.DEFAULT_DIAGRAM_ID
import com.creativeali.app.diagrams.data.DiagramRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * [diagramId] scopes this view model to one container's diagram/schematic.
 * Defaults to [DEFAULT_DIAGRAM_ID] for backward compatibility.
 */
class DiagramViewModel(app: Application, private val diagramId: String = DEFAULT_DIAGRAM_ID) : AndroidViewModel(app) {
    private val repo = DiagramRepository(CreativeAliDatabase.get(app).diagramDao())

    private val _diagram = MutableStateFlow(Diagram(id = diagramId))
    val diagram: StateFlow<Diagram> = _diagram

    init {
        viewModelScope.launch {
            repo.ensureDefaultDiagram(diagramId)
            repo.observeDiagram(diagramId).collect { _diagram.value = it }
        }
    }

    fun save(element: DiagramElement) = viewModelScope.launch { repo.saveElement(diagramId = diagramId, element = element) }

    fun delete(elementId: String) = viewModelScope.launch { repo.deleteElement(elementId) }

    class Factory(private val app: Application, private val diagramId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DiagramViewModel(app, diagramId) as T
    }
}
