package com.creativeali.app.blogging

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.creativeali.app.blogging.data.BloggingRepository
import com.creativeali.app.blogging.data.DEFAULT_LOOP_ID
import com.creativeali.app.data.CreativeAliDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * [loopId] scopes this view model to one container's memoir/blog loop.
 * Defaults to [DEFAULT_LOOP_ID] for backward compatibility with any
 * screen still using the single shared loop.
 */
class BloggingViewModel(app: Application, private val loopId: String = DEFAULT_LOOP_ID) : AndroidViewModel(app) {
    private val repo = BloggingRepository(CreativeAliDatabase.get(app).bloggingDao())

    private val _loop = MutableStateFlow(BDlofLoop(id = loopId))
    val loop: StateFlow<BDlofLoop> = _loop

    init {
        viewModelScope.launch {
            repo.ensureDefaultLoop(loopId)
            repo.observeLoop(loopId).collect { _loop.value = it }
        }
    }

    fun save(entry: DlofEntry) = viewModelScope.launch { repo.saveEntry(loopId = loopId, entry = entry) }

    fun delete(entryId: String) = viewModelScope.launch { repo.deleteEntry(entryId) }

    class Factory(private val app: Application, private val loopId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BloggingViewModel(app, loopId) as T
    }
}
