package com.lifeos.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.lifeos.app.ui.components.LifeOSButton
import com.lifeos.app.ui.components.LifeOSTextField
import java.time.LocalDate

@Composable
fun JournalEntryDialog(
    initialContent: String = "",
    initialMood: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: (content: String, mood: Int?) -> Unit
) {
    var content by remember { mutableStateOf(initialContent) }
    var mood by remember { mutableStateOf(initialMood) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(20.dp)) {
                Text("Journal entry", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                LifeOSTextField(value = content, onValueChange = { content = it }, label = "What's on your mind?", singleLine = false, minLines = 5)
                Spacer(Modifier.height(12.dp))
                Text("Mood (optional)", style = MaterialTheme.typography.labelSmall)
                Row {
                    (1..5).forEach { m ->
                        FilterChip(
                            selected = mood == m,
                            onClick = { mood = if (mood == m) null else m },
                            label = { Text(m.toString()) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    LifeOSButton(text = "Save", enabled = content.isNotBlank(), onClick = { onConfirm(content.trim(), mood) })
                }
            }
        }
    }
}

@Composable
fun RescheduleDialog(
    taskTitle: String,
    currentDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    onDelete: () -> Unit
) {
    var confirmingDelete by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(20.dp)) {
                Text(taskTitle, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text("Reschedule to…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                listOf(
                    "Tomorrow" to currentDate.plusDays(1),
                    "In 3 days" to currentDate.plusDays(3),
                    "Next week" to currentDate.plusWeeks(1)
                ).forEach { (label, date) ->
                    TextButton(onClick = { onConfirm(date) }, modifier = Modifier.fillMaxWidth()) {
                        Text(label, modifier = Modifier.fillMaxWidth())
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { confirmingDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete task", modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Delete this task?") },
            text = { Text(taskTitle) },
            confirmButton = {
                TextButton(
                    onClick = { onDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") } }
        )
    }
}
