package com.lifeos.app.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lifeos.app.data.entity.Task
import com.lifeos.app.ui.common.AddEditTaskDialog
import com.lifeos.app.ui.common.AppViewModelFactory
import com.lifeos.app.ui.common.RescheduleDialog
import com.lifeos.app.ui.common.lifeOSApp
import com.lifeos.app.ui.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun CalendarScreen(navController: NavController? = null) {
    val app = lifeOSApp(LocalContext.current.applicationContext as android.app.Application)
    val viewModel: CalendarViewModel = viewModel(factory = AppViewModelFactory(app))
    val state by viewModel.uiState.collectAsState()

    var showAddTask by remember { mutableStateOf(false) }
    var addTaskDate by remember { mutableStateOf(state.selectedDate) }
    var prefillTimeMinutes by remember { mutableStateOf<Int?>(null) }
    var rescheduleTarget by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            LifeOSTopBar(
                title = "Calendar",
                actions = {
                    TextButton(onClick = { viewModel.selectDate(LocalDate.now()) }) { Text("Today") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { addTaskDate = state.selectedDate; prefillTimeMinutes = null; showAddTask = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create") }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LifeOSSegmentedControl(
                options = CalendarMode.entries,
                selected = state.mode,
                onSelect = viewModel::setMode,
                label = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
            )

            when (state.mode) {
                CalendarMode.DAY -> DayView(
                    state = state,
                    viewModel = viewModel,
                    onSlotClick = { minutes -> addTaskDate = state.selectedDate; prefillTimeMinutes = minutes; showAddTask = true },
                    onTaskClick = { rescheduleTarget = it }
                )
                CalendarMode.WEEK -> WeekView(
                    state = state,
                    viewModel = viewModel,
                    onSlotClick = { date, minutes -> addTaskDate = date; prefillTimeMinutes = minutes; showAddTask = true },
                    onTaskClick = { rescheduleTarget = it },
                    onReflect = { navController?.navigate("reviews") }
                )
                CalendarMode.MONTH -> MonthView(
                    state = state,
                    viewModel = viewModel,
                    onOpenDay = { date -> viewModel.selectDate(date); viewModel.setMode(CalendarMode.DAY) },
                    onReflect = { navController?.navigate("reviews") }
                )
                CalendarMode.YEAR -> YearView(
                    state = state,
                    viewModel = viewModel,
                    onOpenMonth = { ym -> viewModel.selectMonth(ym); viewModel.setMode(CalendarMode.MONTH) }
                )
            }
        }
    }

    if (showAddTask) {
        AddEditTaskDialog(
            initialTimeMinutes = prefillTimeMinutes,
            onDismiss = { showAddTask = false },
            onConfirm = { result ->
                viewModel.addTask(addTaskDate, result.title, result.description, result.priority, result.timeMinutes, result.durationMinutes)
                showAddTask = false
            }
        )
    }

    rescheduleTarget?.let { task ->
        RescheduleDialog(
            taskTitle = task.title,
            currentDate = LocalDate.ofEpochDay(task.dueDateEpochDay),
            onDismiss = { rescheduleTarget = null },
            onConfirm = { date ->
                viewModel.rescheduleTask(task, date)
                rescheduleTarget = null
            },
            onDelete = {
                viewModel.deleteTask(task)
                rescheduleTarget = null
            }
        )
    }
}

@Composable
private fun DayView(
    state: CalendarUiState,
    viewModel: CalendarViewModel,
    onSlotClick: (Int) -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val unscheduled = state.dayTasks.filter { it.dueTimeMinutes == null }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::goPrevDay) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day") }
            Text(dayHeaderLabel(state.selectedDate), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = viewModel::goNextDay) { Icon(Icons.Default.ChevronRight, contentDescription = "Next day") }
        }
        if (unscheduled.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.heightIn(max = 160.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { LifeOSSectionHeader("Unscheduled") }
                androidx.compose.foundation.lazy.items(unscheduled, key = { it.id }) { task ->
                    LifeOSTaskCard(task = task, onToggleComplete = { viewModel.toggleComplete(task) }, onClick = { onTaskClick(task) })
                }
            }
        }
        LifeOSHourlyTimeline(
            date = state.selectedDate,
            tasks = state.dayTasks,
            onSlotClick = onSlotClick,
            onTaskClick = onTaskClick,
            onToggleComplete = { viewModel.toggleComplete(it) },
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
        )
    }
}

private fun dayHeaderLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
}

@Composable
private fun WeekView(
    state: CalendarUiState,
    viewModel: CalendarViewModel,
    onSlotClick: (LocalDate, Int) -> Unit,
    onTaskClick: (Task) -> Unit,
    onReflect: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::goPrevWeek) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous week") }
            TextButton(onClick = onReflect) {
                Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reflect on this week")
            }
            IconButton(onClick = viewModel::goNextWeek) { Icon(Icons.Default.ChevronRight, contentDescription = "Next week") }
        }
        LifeOSWeekHourlyGrid(
            weekStart = state.weekStart,
            selectedDate = state.selectedDate,
            tasksByDate = state.weekTasks,
            onSelectDate = viewModel::selectDate,
            onSlotClick = onSlotClick,
            onTaskClick = onTaskClick,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun MonthView(state: CalendarUiState, viewModel: CalendarViewModel, onOpenDay: (LocalDate) -> Unit, onReflect: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        LifeOSMonthGrid(
            month = state.month,
            selectedDate = state.selectedDate,
            markers = state.monthMarkers,
            onDayClick = { date -> viewModel.selectDate(date) },
            onPrevMonth = viewModel::goPrevMonth,
            onNextMonth = viewModel::goNextMonth
        )
        Spacer(Modifier.height(16.dp))
        val tasksForSelected = state.monthMarkers[state.selectedDate]
        LifeOSCard(onClick = { onOpenDay(state.selectedDate) }) {
            Text(dayHeaderLabel(state.selectedDate), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                if (tasksForSelected == null || tasksForSelected.taskCount == 0) "No tasks planned — tap to open the day"
                else "${tasksForSelected.completedTaskCount}/${tasksForSelected.taskCount} tasks done — tap to open the day",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))
        LifeOSCard(onClick = onReflect, tonal = true) {
            Text("Reflect on this month", style = MaterialTheme.typography.titleSmall)
            Text("Open the monthly review", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun YearView(state: CalendarUiState, viewModel: CalendarViewModel, onOpenMonth: (java.time.YearMonth) -> Unit) {
    val markers by viewModel.yearMonthMarkers.collectAsState()
    val year = state.selectedDate.year
    val months = remember(year) { (1..12).map { java.time.YearMonth.of(year, it) } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::goPrevYear) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous year") }
            Text(year.toString(), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = viewModel::goNextYear) { Icon(Icons.Default.ChevronRight, contentDescription = "Next year") }
        }
        Spacer(Modifier.height(12.dp))
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            androidx.compose.foundation.lazy.grid.items(months) { ym ->
                val marker = markers[ym]
                LifeOSCard(onClick = { onOpenMonth(ym) }) {
                    Text(
                        ym.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(6.dp))
                    if (marker != null && marker.taskCount > 0) {
                        Text(
                            "${marker.completedTaskCount}/${marker.taskCount} done",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text("—", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
