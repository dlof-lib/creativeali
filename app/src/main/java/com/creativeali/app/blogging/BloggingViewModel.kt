package com.creativeali.app.blogging

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creativeali.app.blogging.data.BloggingRepository
import com.creativeali.app.blogging.data.DEFAULT_LOOP_ID
import com.creativeali.app.data.CreativeAliDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BloggingViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BloggingRepository(CreativeAliDatabase.get(app).bloggingDao())

    private val _loop = MutableStateFlow(BDlofLoop(id = DEFAULT_LOOP_ID))
    val loop: StateFlow<BDlofLoop> = _loop

    init {
        viewModelScope.launch {
            repo.ensureDefaultLoop()
            repo.observeLoop().collect { _loop.value = it }
        }
    }

    fun save(entry: DlofEntry) = viewModelScope.launch { repo.saveEntry(entry = entry) }

    fun delete(entryId: String) = viewModelScope.launch { repo.deleteEntry(entryId) }
}
