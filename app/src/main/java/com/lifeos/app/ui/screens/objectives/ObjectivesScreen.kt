package com.lifeos.app.ui.screens.objectives

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.lifeos.app.ui.common.AddEditObjectiveDialog
import com.lifeos.app.ui.common.AppViewModelFactory
import com.lifeos.app.ui.common.ObjectiveActionsDialog
import com.lifeos.app.ui.common.lifeOSApp
import com.lifeos.app.ui.components.LifeOSEmptyState
import com.lifeos.app.ui.components.LifeOSObjectiveCard
import com.lifeos.app.ui.components.LifeOSTopBar

@Composable
fun ObjectivesScreen() {
    val app = lifeOSApp(LocalContext.current.applicationContext as android.app.Application)
    val viewModel: ObjectivesViewModel = viewModel(factory = AppViewModelFactory(app))
    val state by viewModel.uiState.collectAsState()

    var showAdd by remember { mutableStateOf(false) }
    var actionsTarget by remember { mutableStateOf<Objective?>(null) }

    Scaffold(
        topBar = { LifeOSTopBar(title = "Objectives") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add objective")
            }
        }
    ) { padding ->
        if (state.objectives.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                LifeOSEmptyState(
                    "No objectives yet. Tap + to add a recurring goal.",
                    Modifier.align(androidx.compose.ui.Alignment.Center),
                    icon = androidx.compose.material.icons.Icons.Default.Flag
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.objectives, key = { it.id }) { objective ->
                    val instance = state.todayInstances.find { it.objectiveId == objective.id }
                    LifeOSObjectiveCard(
                        objective = objective,
                        isCompletedToday = instance?.status == InstanceStatus.COMPLETED,
                        onToggleToday = { instance?.let { viewModel.toggleToday(it) } },
                        onClick = { actionsTarget = objective }
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddEditObjectiveDialog(
            onDismiss = { showAdd = false },
            onConfirm = { result ->
                viewModel.addObjective(result.title, result.description, result.frequency, result.customDaysMask)
                showAdd = false
            }
        )
    }

    actionsTarget?.let { objective ->
        ObjectiveActionsDialog(
            objective = objective,
            onDismiss = { actionsTarget = null },
            onSkipToday = { viewModel.skipToday(objective) },
            onTogglePause = { viewModel.toggleActive(objective) },
            onDelete = { viewModel.delete(objective) }
        )
    }
}
