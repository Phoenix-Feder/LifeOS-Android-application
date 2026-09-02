package com.lifeos.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.LifeOSApplication
import com.lifeos.app.data.settings.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val app: LifeOSApplication) : ViewModel() {
    private val settingsRepo = app.settingsRepository
    private val planner = app.recurringNotificationPlanner

    val settings: StateFlow<UserSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    fun update(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            settingsRepo.update(transform)
            planner.scheduleAll()
        }
    }
}
