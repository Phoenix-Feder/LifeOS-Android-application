package com.lifeos.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.LifeOSApplication
import com.lifeos.app.data.entity.NotificationType
import com.lifeos.app.data.entity.Priority
import com.lifeos.app.data.entity.Task
import com.lifeos.app.data.entity.TaskStatus
import com.lifeos.app.ui.components.DayMarker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class CalendarMode { DAY, WEEK, MONTH, YEAR }

data class CalendarUiState(
    val mode: CalendarMode = CalendarMode.DAY,
    val selectedDate: LocalDate = LocalDate.now(),
    val weekStart: LocalDate = LocalDate.now(),
    val month: YearMonth = YearMonth.now(),
    val dayTasks: List<Task> = emptyList(),
    val weekTasks: Map<LocalDate, List<Task>> = emptyMap(),
    val weekMarkers: Map<LocalDate, DayMarker> = emptyMap(),
    val monthMarkers: Map<LocalDate, DayMarker> = emptyMap()
)

private fun weekStartOf(date: LocalDate): LocalDate = date.minusDays(date.dayOfWeek.value - 1L)

private fun markersFrom(tasks: List<Task>): Map<LocalDate, DayMarker> =
    tasks.groupBy { LocalDate.ofEpochDay(it.dueDateEpochDay) }
        .mapValues { (_, dayTasks) ->
            DayMarker(
                taskCount = dayTasks.size,
                completedTaskCount = dayTasks.count { it.status == TaskStatus.COMPLETED }
            )
        }

class CalendarViewModel(private val app: LifeOSApplication) : ViewModel() {
    private val taskRepo = app.taskRepository
    private val settingsRepo = app.settingsRepository
    private val scheduler = app.notificationScheduler

    private val mode = MutableStateFlow(CalendarMode.DAY)
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val weekStart = selectedDate.map { weekStartOf(it) }.distinctUntilChanged()
    private val month = selectedDate.map { YearMonth.from(it) }.distinctUntilChanged()

    private val dayTasks = selectedDate.flatMapLatest { taskRepo.observeForDate(it) }
    private val weekTasksFlow = weekStart.flatMapLatest { start -> taskRepo.observeForRange(start, start.plusDays(6)) }
    private val monthTasksFlow = month.flatMapLatest { m -> taskRepo.observeForRange(m.atDay(1), m.atEndOfMonth()) }

    private data class TaskBundle(val day: List<Task>, val week: List<Task>, val month: List<Task>)
    private val taskBundle = combine(dayTasks, weekTasksFlow, monthTasksFlow) { d, w, m -> TaskBundle(d, w, m) }

    private val year = selectedDate.map { it.year }.distinctUntilChanged()
    private val yearTasksFlow = year.flatMapLatest { y -> taskRepo.observeForRange(LocalDate.of(y, 1, 1), LocalDate.of(y, 12, 31)) }
    /** Per-month markers for the Year view — kept separate from the main combine chain to avoid a 6+ arg combine(). */
    val yearMonthMarkers: StateFlow<Map<YearMonth, DayMarker>> = yearTasksFlow.map { tasks ->
        tasks.groupBy { YearMonth.from(LocalDate.ofEpochDay(it.dueDateEpochDay)) }
            .mapValues { (_, monthTasks) ->
                DayMarker(taskCount = monthTasks.size, completedTaskCount = monthTasks.count { it.status == TaskStatus.COMPLETED })
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val uiState: StateFlow<CalendarUiState> = combine(
        mode, selectedDate, weekStart, month, taskBundle
    ) { m, sel, wStart, mo, bundle ->
        CalendarUiState(
            mode = m,
            selectedDate = sel,
            weekStart = wStart,
            month = mo,
            dayTasks = bundle.day,
            weekTasks = bundle.week.groupBy { LocalDate.ofEpochDay(it.dueDateEpochDay) },
            weekMarkers = markersFrom(bundle.week),
            monthMarkers = markersFrom(bundle.month)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun setMode(newMode: CalendarMode) { mode.value = newMode }
    fun selectDate(date: LocalDate) { selectedDate.value = date }

    fun goPrevDay() { selectedDate.value = selectedDate.value.minusDays(1) }
    fun goNextDay() { selectedDate.value = selectedDate.value.plusDays(1) }
    fun goPrevWeek() { selectedDate.value = selectedDate.value.minusWeeks(1) }
    fun goNextWeek() { selectedDate.value = selectedDate.value.plusWeeks(1) }
    fun goPrevMonth() { selectedDate.value = selectedDate.value.minusMonths(1) }
    fun goNextMonth() { selectedDate.value = selectedDate.value.plusMonths(1) }
    fun goPrevYear() { selectedDate.value = selectedDate.value.minusYears(1) }
    fun goNextYear() { selectedDate.value = selectedDate.value.plusYears(1) }
    fun selectMonth(yearMonth: YearMonth) { selectedDate.value = yearMonth.atDay(1) }

    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            if (task.status == TaskStatus.COMPLETED) {
                taskRepo.update(task.copy(status = TaskStatus.PENDING, completedAtEpochMillis = null))
            } else {
                taskRepo.markCompleted(task)
                scheduler.cancel(NotificationType.TASK_START, task.id)
            }
        }
    }

    fun addTask(date: LocalDate, title: String, description: String, priority: Priority, timeMinutes: Int?, durationMinutes: Int) {
        viewModelScope.launch {
            val id = taskRepo.create(
                Task(
                    title = title,
                    description = description,
                    dueDateEpochDay = date.toEpochDay(),
                    dueTimeMinutes = timeMinutes,
                    durationMinutes = durationMinutes,
                    priority = priority,
                    createdAtEpochMillis = System.currentTimeMillis()
                )
            )
            val remindersOn = settingsRepo.settings.first().taskRemindersEnabled
            scheduler.scheduleTaskStart(id, title, date.toEpochDay(), timeMinutes, remindersOn)
        }
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
