package com.lifeos.app.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifeos.app.ui.common.AppViewModelFactory
import com.lifeos.app.ui.common.lifeOSApp
import com.lifeos.app.ui.components.LifeOSMetricCard
import com.lifeos.app.ui.components.LifeOSProgressBar
import com.lifeos.app.ui.components.LifeOSTopBar

@Composable
fun AnalyticsScreen() {
    val app = lifeOSApp(LocalContext.current.applicationContext as android.app.Application)
    val viewModel: AnalyticsViewModel = viewModel(factory = AppViewModelFactory(app))
    val range by viewModel.range.collectAsState()
    val summary by viewModel.summary.collectAsState()

    Scaffold(topBar = { LifeOSTopBar(title = "Analytics") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnalyticsRange.entries.forEach { r ->
                    FilterChip(selected = range == r, onClick = { viewModel.setRange(r) }, label = { Text(r.label) })
                }
            }
            Spacer(Modifier.height(16.dp))
            summary?.let { s ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LifeOSMetricCard(
                        title = "Tasks completed",
                        value = "${s.completedTasks}/${s.plannedTasks}",
                        modifier = Modifier.weight(1f)
                    )
                    LifeOSMetricCard(
                        title = "Journal days",
                        value = "${s.journaledDays}/${s.eligibleJournalDays}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(20.dp))
                LifeOSProgressBar(progress = s.taskCompletionRate, label = "Task completion rate")
                Spacer(Modifier.height(16.dp))
                LifeOSProgressBar(progress = s.objectiveConsistency, label = "Objective consistency")
                Spacer(Modifier.height(16.dp))
                LifeOSProgressBar(progress = s.journalConsistency, label = "Journal consistency")
            }
        }
    }
}
