package com.lifeos.app.ui.screens.journal

import androidx.compose.foundation.combinedClickable
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
import com.lifeos.app.data.entity.JournalEntry
import com.lifeos.app.ui.common.AppViewModelFactory
import com.lifeos.app.ui.common.JournalEntryDialog
import com.lifeos.app.ui.common.lifeOSApp
import com.lifeos.app.ui.components.LifeOSCard
import com.lifeos.app.ui.components.LifeOSEmptyState
import com.lifeos.app.ui.components.LifeOSTopBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun JournalScreen() {
    val app = lifeOSApp(LocalContext.current.applicationContext as android.app.Application)
    val viewModel: JournalViewModel = viewModel(factory = AppViewModelFactory(app))
    val entries by viewModel.entries.collectAsState()

    var showAdd by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<JournalEntry?>(null) }

    Scaffold(
        topBar = { LifeOSTopBar(title = "Journal") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add entry")
            }
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                LifeOSEmptyState(
                    "No journal entries yet.",
                    Modifier.align(androidx.compose.ui.Alignment.Center),
                    icon = androidx.compose.material.icons.Icons.Default.Edit
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    LifeOSCard(
                        modifier = Modifier.combinedClickable(onClick = {}, onLongClick = { deleteTarget = entry })
                    ) {
                        Text(
                            LocalDate.ofEpochDay(entry.dateEpochDay).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(entry.content, style = MaterialTheme.typography.bodyMedium)
                        if (entry.mood != null) {
                            Spacer(Modifier.height(4.dp))
                            Text("Mood: ${entry.mood}/5", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        JournalEntryDialog(
            onDismiss = { showAdd = false },
            onConfirm = { content, mood ->
                viewModel.addEntry(content, mood)
                showAdd = false
            }
        )
    }

    deleteTarget?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete entry?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEntry(entry); deleteTarget = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }
}
