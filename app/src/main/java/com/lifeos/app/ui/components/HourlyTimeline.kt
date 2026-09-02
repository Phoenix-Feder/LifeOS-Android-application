package com.lifeos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lifeos.app.data.entity.Task
import com.lifeos.app.data.entity.TaskStatus
import java.time.LocalDate
import java.time.LocalTime

private val HOUR_HEIGHT = 64.dp
private val GUTTER_WIDTH = 52.dp
private const val START_HOUR = 5   // 5am — most planning happens in waking hours
private const val END_HOUR = 24

private fun hourLabel(hour: Int): String {
    val h = hour % 24
    val period = if (h < 12) "AM" else "PM"
    val display = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return "$display $period"
}

/**
 * Scrollable hour-by-hour schedule for a single day. Scheduled tasks (with
 * dueTimeMinutes set) render as blocks positioned/sized by time; tapping an
 * empty hour proposes adding a task there. Unscheduled tasks are shown by
 * the caller separately (see TodayScreen / Calendar day view).
 */
@Composable
fun LifeOSHourlyTimeline(
    date: LocalDate,
    tasks: List<Task>,
    onSlotClick: (minutes: Int) -> Unit,
    onTaskClick: (Task) -> Unit,
    onToggleComplete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scheduled = tasks.filter { it.dueTimeMinutes != null }
    val isToday = date == LocalDate.now()

    LaunchedEffect(Unit) {
        // Land the view roughly an hour before "now" (or 7am for other days) instead of at midnight.
        val anchorHour = if (isToday) (LocalTime.now().hour - 1).coerceIn(START_HOUR, END_HOUR - 1) else 7
        scrollState.scrollTo(((anchorHour - START_HOUR) * HOUR_HEIGHT.value).toInt())
    }

    Box(
        modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Column(Modifier.fillMaxWidth()) {
            for (hour in START_HOUR until END_HOUR) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(HOUR_HEIGHT)
                        .clickable { onSlotClick(hour * 60) }
                ) {
                    Box(Modifier.width(GUTTER_WIDTH).fillMaxHeight()) {
                        Text(
                            hourLabel(hour),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp, top = 2.dp)
                        )
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.align(Alignment.BottomStart),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        // Task blocks, absolutely positioned over the grid.
        scheduled.forEach { task ->
            val minutesFromStart = task.dueTimeMinutes!! - START_HOUR * 60
            if (minutesFromStart >= 0) {
                val top: Dp = HOUR_HEIGHT * (minutesFromStart / 60f)
                val height: Dp = (HOUR_HEIGHT * (task.durationMinutes / 60f)).coerceAtLeast(30.dp)
                Box(
                    Modifier
                        .padding(start = GUTTER_WIDTH + 4.dp, end = 8.dp)
                        .offset(y = top)
                        .height(height)
                        .fillMaxWidth()
                ) {
                    TimelineTaskBlock(task, onClick = { onTaskClick(task) }, onToggle = { onToggleComplete(task) })
                }
            }
        }

        if (isToday) {
            val nowMinutes = LocalTime.now().let { it.hour * 60 + it.minute } - START_HOUR * 60
            if (nowMinutes in 0..(END_HOUR - START_HOUR) * 60) {
                val top = HOUR_HEIGHT * (nowMinutes / 60f)
                Row(
                    Modifier
                        .offset(y = top - 5.dp)
                        .padding(start = GUTTER_WIDTH - 5.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineTaskBlock(task: Task, onClick: () -> Unit, onToggle: () -> Unit) {
    val done = task.status == TaskStatus.COMPLETED
    val color = priorityColor(task.priority)
    Row(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(if (done) color.copy(alpha = 0.12f) else color.copy(alpha = 0.16f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = if (done) TextDecoration.LineThrough else null,
                maxLines = 2
            )
            Text(
                minutesToTimeLabel(task.dueTimeMinutes!!),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = "Toggle complete",
            tint = if (done) color else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp).clickable { onToggle() }
        )
    }
}
