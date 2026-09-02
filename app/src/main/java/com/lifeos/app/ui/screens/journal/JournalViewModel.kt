package com.lifeos.app.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.LifeOSApplication
import com.lifeos.app.data.entity.JournalEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(private val app: LifeOSApplication) : ViewModel() {
    private val repo = app.journalRepository

    val entries: StateFlow<List<JournalEntry>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(content: String, mood: Int?) {
        viewModelScope.launch {
            repo.create(
                JournalEntry(
                    dateEpochDay = java.time.LocalDate.now().toEpochDay(),
                    content = content,
                    mood = mood,
                    createdAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch { repo.delete(entry) }
    }
}
