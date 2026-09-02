package com.lifeos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.lifeos.app.data.entity.Objective
import com.lifeos.app.data.entity.Priority
import com.lifeos.app.data.entity.Task
import com.lifeos.app.data.entity.TaskStatus
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

fun minutesToTimeLabel(minutes: Int): String = LocalTime.of(minutes / 60, minutes % 60).format(timeFormatter)

fun priorityColor(priority: Priority) = when (priority) {
    Priority.HIGH -> Color(0xFFD93025)   // tomato
    Priority.MEDIUM -> Color(0xFFF9AB00) // banana
    Priority.LOW -> Color(0xFF188038)    // basil
}

@Composable
fun LifeOSTaskCard(
    task: Task,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LifeOSCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(4.dp, 34.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    .background(priorityColor(task.priority))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough else null,
                    color = if (task.status == TaskStatus.COMPLETED) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (task.dueTimeMinutes != null) {
                    Text(
                        minutesToTimeLabel(task.dueTimeMinutes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (task.status == TaskStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle complete",
                    tint = if (task.status == TaskStatus.COMPLETED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LifeOSObjectiveCard(
    objective: Objective,
    isCompletedToday: Boolean,
    onToggleToday: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LifeOSCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(objective.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    objective.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleToday) {
                Icon(
                    imageVector = if (isCompletedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle today",
                    tint = if (isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Small dot used in month-grid cells to indicate a day has items. */
@Composable
fun LifeOSDot(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.size(5.dp).clip(CircleShape).background(color))
}
