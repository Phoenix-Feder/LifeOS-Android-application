package com.lifeos.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lifeos.app.data.entity.Priority
import com.lifeos.app.ui.components.LifeOSButton
import com.lifeos.app.ui.components.LifeOSTextField
import java.time.LocalTime

data class TaskFormResult(
    val title: String,
    val description: String,
    val priority: Priority,
    val timeMinutes: Int?,
    val durationMinutes: Int
)

private val durationOptions = listOf(15, 30, 45, 60, 90, 120)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    initialTitle: String = "",
    initialDescription: String = "",
    initialPriority: Priority = Priority.MEDIUM,
    initialTimeMinutes: Int? = null,
    initialDurationMinutes: Int = 60,
    onDismiss: () -> Unit,
    onConfirm: (TaskFormResult) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var priority by remember { mutableStateOf(initialPriority) }
    var isScheduled by remember { mutableStateOf(initialTimeMinutes != null) }
    var timeMinutes by remember { mutableStateOf(initialTimeMinutes ?: (9 * 60)) }
    var duration by remember { mutableStateOf(initialDurationMinutes) }
    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text("Task", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                LifeOSTextField(value = title, onValueChange = { title = it }, label = "Title")
                Spacer(Modifier.height(8.dp))
                LifeOSTextField(value = description, onValueChange = { description = it }, label = "Description (optional)", singleLine = false, minLines = 2)
                Spacer(Modifier.height(12.dp))

                Text("Priority", style = MaterialTheme.typography.labelSmall)
                Row {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Give it a time slot", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isScheduled, onCheckedChange = { isScheduled = it })
                }

                if (isScheduled) {
                    Spacer(Modifier.height(8.dp))
                    val time = LocalTime.of(timeMinutes / 60, timeMinutes % 60)
                    val displayHour = time.hour.let { if (it == 0) 12 else if (it > 12) it - 12 else it }
                    OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Starts at $displayHour:${"%02d".format(time.minute)} ${if (time.hour < 12) "AM" else "PM"}")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Duration", style = MaterialTheme.typography.labelSmall)
                    Row {
                        durationOptions.forEach { d ->
                            FilterChip(
                                selected = duration == d,
                                onClick = { duration = d },
                                label = { Text(if (d < 60) "${d}m" else "${d / 60}h${if (d % 60 != 0) "${d % 60}m" else ""}") },
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    LifeOSButton(
                        text = "Save",
                        enabled = title.isNotBlank(),
                        onClick = {
                            onConfirm(
                                TaskFormResult(
                                    title = title.trim(),
                                    description = description.trim(),
                                    priority = priority,
                                    timeMinutes = if (isScheduled) timeMinutes else null,
                                    durationMinutes = duration
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(initialHour = timeMinutes / 60, initialMinute = timeMinutes % 60)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Choose a time") },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    timeMinutes = state.hour * 60 + state.minute
                    showTimePicker = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }
}
