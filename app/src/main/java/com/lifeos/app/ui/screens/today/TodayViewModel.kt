package com.lifeos.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.LifeOSApplication
import com.lifeos.app.data.entity.*
import com.lifeos.app.notification.NotificationScheduler
import com.lifeos.app.ui.components.DayMarker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodayUiState(
    val date: LocalDate = LocalDate.now(),
    val weekStart: LocalDate = LocalDate.now(),
    val tasks: List<Task> = emptyList(),
    val overdueTasks: List<Task> = emptyList(),
    val objectives: List<Objective> = emptyList(),
    val objectiveInstances: List<ObjectiveInstance> = emptyList(),
    val weekMarkers: Map<LocalDate, DayMarker> = emptyMap()
)

private fun weekStartOf(date: LocalDate): LocalDate = date.minusDays(date.dayOfWeek.value - 1L)

class TodayViewModel(private val app: LifeOSApplication) : ViewModel() {
    private val taskRepo = app.taskRepository
    private val objectiveRepo = app.objectiveRepository
    private val settingsRepo = app.settingsRepository
    private val scheduler: NotificationScheduler = app.notificationScheduler
    private val today = LocalDate.now()
    private val weekStart = weekStartOf(today)

    init {
        viewModelScope.launch { objectiveRepo.ensureInstancesGenerated(today, today.plusDays(14)) }
    }

    private val weekTasks = taskRepo.observeForRange(weekStart, weekStart.plusDays(6))
    private val weekMarkers = weekTasks.map { tasks ->
        tasks.groupBy { LocalDate.ofEpochDay(it.dueDateEpochDay) }
            .mapValues { (_, dayTasks) ->
                DayMarker(
                    taskCount = dayTasks.size,
                    completedTaskCount = dayTasks.count { it.status == TaskStatus.COMPLETED }
                )
            }
    }

    val uiState: StateFlow<TodayUiState> = combine(
        taskRepo.observeForDate(today),
        taskRepo.observeOverdue(today),
        objectiveRepo.observeActive(),
        objectiveRepo.observeInstancesForDate(today),
        weekMarkers
    ) { tasks, overdue, objectives, instances, markers ->
        TodayUiState(today, weekStart, tasks, overdue, objectives, instances, markers)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUiState(today, weekStart))

    fun addTask(title: String, description: String, priority: Priority, timeMinutes: Int?, durationMinutes: Int) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                dueDateEpochDay = today.toEpochDay(),
                dueTimeMinutes = timeMinutes,
                durationMinutes = durationMinutes,
                priority = priority,
                createdAtEpochMillis = System.currentTimeMillis()
            )
            val id = taskRepo.create(task)
            val remindersOn = settingsRepo.settings.first().taskRemindersEnabled
            scheduler.scheduleTaskStart(id, title, task.dueDateEpochDay, timeMinutes, remindersOn)
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            if (task.status == TaskStatus.COMPLETED) {
                taskRepo.update(task.copy(status = TaskStatus.PENDING, completedAtEpochMillis = null))
            } else {
                taskRepo.markCompleted(task)
                scheduler.cancel(NotificationType.TASK_START, task.id)
            }
        }
    }

    fun toggleObjectiveInstance(instance: ObjectiveInstance) {
        viewModelScope.launch {
            val newStatus = if (instance.status == InstanceStatus.COMPLETED) InstanceStatus.PENDING else InstanceStatus.COMPLETED
            objectiveRepo.setInstanceStatus(instance, newStatus)
        }
    }

    fun skipObjectiveToday(objective: Objective) {
        viewModelScope.launch {
            val instance = uiState.value.objectiveInstances.find { it.objectiveId == objective.id } ?: return@launch
            objectiveRepo.setInstanceStatus(instance, InstanceStatus.SKIPPED)
        }
    }

    fun toggleObjectivePause(objective: Objective) {
        viewModelScope.launch { objectiveRepo.update(objective.copy(active = !objective.active)) }
    }

    fun deleteObjective(objective: Objective) {
        viewModelScope.launch { objectiveRepo.delete(objective) }
    }

    fun rescheduleTask(task: Task, newDate: LocalDate) {
        viewModelScope.launch {
            taskRepo.reschedule(task, newDate)
            val remindersOn = settingsRepo.settings.first().taskRemindersEnabled
            scheduler.scheduleTaskStart(task.id, task.title, newDate.toEpochDay(), task.dueTimeMinutes, remindersOn)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepo.delete(task)
            scheduler.cancel(NotificationType.TASK_START, task.id)
        }
    }
}
