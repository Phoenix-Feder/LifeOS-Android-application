package com.lifeos.app.ui.screens.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifeos.app.data.entity.InstanceStatus
import com.lifeos.app.data.entity.Objective
import com.lifeos.app.data.entity.Task
import com.lifeos.app.ui.common.*
import com.lifeos.app.ui.components.*
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen() {
    val app = lifeOSApp(LocalContext.current.applicationContext as android.app.Application)
    val viewModel: TodayViewModel = viewModel(factory = AppViewModelFactory(app))
    val state by viewModel.uiState.collectAsState()

    var showAddTask by remember { mutableStateOf(false) }
    var prefillTimeMinutes by remember { mutableStateOf<Int?>(null) }
    var rescheduleTarget by remember { mutableStateOf<Task?>(null) }
    var objectiveActionsTarget by remember { mutableStateOf<Objective?>(null) }

    val unscheduled = state.tasks.filter { it.dueTimeMinutes == null }

    Scaffold(
        topBar = {
            LifeOSTopBar(title = state.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")))
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { prefillTimeMinutes = null; showAddTask = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create") }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LifeOSWeekStrip(
                weekStart = state.weekStart,
                selectedDate = state.date,
                markers = state.weekMarkers,
                onSelectDate = {}, // Today tab always shows today; use Calendar to browse other days
                onPrevWeek = {},
                onNextWeek = {},
                showNav = false,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.overdueTasks.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item { LifeOSSectionHeader("Overdue (${state.overdueTasks.size})", modifier = Modifier.width(160.dp)) }
                    items(state.overdueTasks, key = { "overdue-${it.id}" }) { task ->
                        Box(Modifier.width(220.dp)) {
                            LifeOSTaskCard(
                                task = task,
                                onToggleComplete = { viewModel.toggleTaskComplete(task) },
                                onClick = { rescheduleTarget = task }
                            )
                        }
                    }
                }
            }

            if (state.objectives.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 180.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { LifeOSSectionHeader("Objectives today") }
                    items(state.objectives, key = { "obj-${it.id}" }) { objective ->
                        val instance = state.objectiveInstances.find { it.objectiveId == objective.id }
                        if (instance != null) {
                            LifeOSObjectiveCard(
                                objective = objective,
                                isCompletedToday = instance.status == InstanceStatus.COMPLETED,
                                onToggleToday = { viewModel.toggleObjectiveInstance(instance) },
                                onClick = { objectiveActionsTarget = objective }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            if (unscheduled.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 160.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { LifeOSSectionHeader("Unscheduled") }
                    items(unscheduled, key = { "unsched-${it.id}" }) { task ->
                        LifeOSTaskCard(task = task, onToggleComplete = { viewModel.toggleTaskComplete(task) }, onClick = { rescheduleTarget = task })
                    }
                }
            }

            LifeOSSectionHeader("Today's schedule", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            LifeOSHourlyTimeline(
                date = state.date,
                tasks = state.tasks,
                onSlotClick = { minutes -> prefillTimeMinutes = minutes; showAddTask = true },
                onTaskClick = { rescheduleTarget = it },
                onToggleComplete = { viewModel.toggleTaskComplete(it) },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )
        }
    }

    if (showAddTask) {
        AddEditTaskDialog(
            initialTimeMinutes = prefillTimeMinutes,
            onDismiss = { showAddTask = false },
            onConfirm = { result ->
                viewModel.addTask(result.title, result.description, result.priority, result.timeMinutes, result.durationMinutes)
                showAddTask = false
            }
        )
    }

    rescheduleTarget?.let { task ->
        RescheduleDialog(
            taskTitle = task.title,
            currentDate = java.time.LocalDate.ofEpochDay(task.dueDateEpochDay),
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

    objectiveActionsTarget?.let { objective ->
        ObjectiveActionsDialog(
            objective = objective,
            onDismiss = { objectiveActionsTarget = null },
            onSkipToday = { viewModel.skipObjectiveToday(objective) },
            onTogglePause = { viewModel.toggleObjectivePause(objective) },
            onDelete = { viewModel.deleteObjective(objective) }
        )
    }
}
