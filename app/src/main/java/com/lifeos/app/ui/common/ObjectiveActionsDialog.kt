package com.lifeos.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lifeos.app.data.entity.Objective

/**
 * The one place "what can I do with an objective" is decided — reused by
 * every screen that shows an objective (Today, Objectives) so the actions
 * available don't drift between screens. Mirrors RescheduleDialog's
 * stacked-action-list shape for a consistent feel with task actions.
 */
@Composable
fun ObjectiveActionsDialog(
    objective: Objective,
    onDismiss: () -> Unit,
    onSkipToday: (() -> Unit)? = null,
    onTogglePause: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmingDelete by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(20.dp)) {
                Text(objective.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (objective.active) "Active" else "Paused",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                if (onSkipToday != null) {
                    TextButton(onClick = { onSkipToday(); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Skip today", modifier = Modifier.fillMaxWidth())
                    }
                }
                TextButton(onClick = { onTogglePause(); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (objective.active) "Pause" else "Resume", modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { confirmingDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete objective", modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Delete this objective?") },
            text = { Text("This removes \"${objective.title}\" and its history. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(); confirmingDelete = false; onDismiss() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") } }
        )
    }
}
