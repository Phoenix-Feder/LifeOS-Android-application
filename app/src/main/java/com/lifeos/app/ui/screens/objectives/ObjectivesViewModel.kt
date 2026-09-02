package com.lifeos.app.ui.screens.objectives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.LifeOSApplication
import com.lifeos.app.data.entity.Frequency
import com.lifeos.app.data.entity.InstanceStatus
import com.lifeos.app.data.entity.Objective
import com.lifeos.app.data.entity.ObjectiveInstance
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ObjectivesUiState(
    val objectives: List<Objective> = emptyList(),
    val todayInstances: List<ObjectiveInstance> = emptyList()
)

class ObjectivesViewModel(private val app: LifeOSApplication) : ViewModel() {
    private val repo = app.objectiveRepository
    private val today = LocalDate.now()

    val uiState: StateFlow<ObjectivesUiState> = combine(
        repo.observeAll(),
        repo.observeInstancesForDate(today)
    ) { objectives, instances -> ObjectivesUiState(objectives, instances) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ObjectivesUiState())

    fun addObjective(title: String, description: String, frequency: Frequency, customMask: Int) {
        viewModelScope.launch {
            repo.create(
                Objective(
                    title = title,
                    description = description,
                    frequency = frequency,
                    customDaysMask = customMask,
                    createdAtEpochMillis = System.currentTimeMillis()
                )
            )
            repo.ensureInstancesGenerated(today, today.plusDays(14))
        }
    }

    fun toggleActive(objective: Objective) {
        viewModelScope.launch { repo.update(objective.copy(active = !objective.active)) }
    }

    fun delete(objective: Objective) {
        viewModelScope.launch { repo.delete(objective) }
    }

    fun toggleToday(instance: ObjectiveInstance) {
        viewModelScope.launch {
            val newStatus = if (instance.status == InstanceStatus.COMPLETED) InstanceStatus.PENDING else InstanceStatus.COMPLETED
            repo.setInstanceStatus(instance, newStatus)
        }
    }

    fun skipToday(objective: Objective) {
        viewModelScope.launch {
            val instance = uiState.value.todayInstances.find { it.objectiveId == objective.id } ?: return@launch
            repo.setInstanceStatus(instance, InstanceStatus.SKIPPED)
        }
    }
}
