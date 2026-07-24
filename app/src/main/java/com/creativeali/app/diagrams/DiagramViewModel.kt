package com.creativeali.app.diagrams

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creativeali.app.data.CreativeAliDatabase
import com.creativeali.app.diagrams.data.DEFAULT_DIAGRAM_ID
import com.creativeali.app.diagrams.data.DiagramRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiagramViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = DiagramRepository(CreativeAliDatabase.get(app).diagramDao())

    private val _diagram = MutableStateFlow(Diagram(id = DEFAULT_DIAGRAM_ID))
    val diagram: StateFlow<Diagram> = _diagram

    init {
        viewModelScope.launch {
            repo.ensureDefaultDiagram()
            repo.observeDiagram().collect { _diagram.value = it }
        }
    }

    fun save(element: DiagramElement) = viewModelScope.launch { repo.saveElement(element = element) }

    fun delete(elementId: String) = viewModelScope.launch { repo.deleteElement(elementId) }
}
