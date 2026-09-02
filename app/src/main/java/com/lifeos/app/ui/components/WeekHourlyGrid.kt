package com.lifeos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lifeos.app.data.entity.Task
import com.lifeos.app.data.entity.TaskStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

private val WEEK_HOUR_HEIGHT = 52.dp
private val WEEK_GUTTER_WIDTH = 36.dp
private const val WEEK_START_HOUR = 5
private const val WEEK_END_HOUR = 24

/**
 * Multi-column hourly grid — the classic Google Calendar week view. All 7
 * day columns share one vertical scroll position, with a fixed hour gutter
 * on the left and a sticky day-header row on top.
 */
@Composable
fun LifeOSWeekHourlyGrid(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    onSelectDate: (LocalDate) -> Unit,
    onSlotClick: (date: LocalDate, minutes: Int) -> Unit,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        val anchorHour = (LocalTime.now().hour - 1).coerceIn(WEEK_START_HOUR, WEEK_END_HOUR - 1)
        scrollState.scrollTo(((anchorHour - WEEK_START_HOUR) * WEEK_HOUR_HEIGHT.value).toInt())
    }

    Column(modifier.fillMaxSize()) {
        // Sticky header row: day-of-week + date, tappable to select.
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Box(Modifier.width(WEEK_GUTTER_WIDTH))
            days.forEach { date ->
                val today = date == LocalDate.now()
                val selected = date == selectedDate
                Column(
                    Modifier
                        .weight(1f)
                        .clickable { onSelectDate(date) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = when {
                                selected -> MaterialTheme.colorScheme.onPrimary
                                today -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        Box(Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState)) {
            Row(Modifier.fillMaxWidth()) {
                // Hour gutter, shared by all columns.
                Column(Modifier.width(WEEK_GUTTER_WIDTH)) {
                    for (hour in WEEK_START_HOUR until WEEK_END_HOUR) {
                        Box(Modifier.height(WEEK_HOUR_HEIGHT).fillMaxWidth()) {
                            Text(
                                weekHourLabel(hour),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.TopStart).padding(top = 2.dp)
                            )
                        }
                    }
                }
                days.forEach { date ->
                    DayColumn(
                        date = date,
                        tasks = tasksByDate[date].orEmpty(),
                        onSlotClick = { minutes -> onSlotClick(date, minutes) },
                        onTaskClick = onTaskClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayColumn(
    date: LocalDate,
    tasks: List<Task>,
    onSlotClick: (Int) -> Unit,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheduled = tasks.filter { it.dueTimeMinutes != null }
    Box(
        modifier
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            .padding(start = 1.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth()) {
            for (hour in WEEK_START_HOUR until WEEK_END_HOUR) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(WEEK_HOUR_HEIGHT)
                        .clickable { onSlotClick(hour * 60) }
                ) {
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.BottomStart),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                }
            }
        }
        scheduled.forEach { task ->
            val minutesFromStart = task.dueTimeMinutes!! - WEEK_START_HOUR * 60
            if (minutesFromStart >= 0) {
                val top: Dp = WEEK_HOUR_HEIGHT * (minutesFromStart / 60f)
                val height: Dp = (WEEK_HOUR_HEIGHT * (task.durationMinutes / 60f)).coerceAtLeast(16.dp)
                val color = priorityColor(task.priority)
                val done = task.status == TaskStatus.COMPLETED
                Box(
                    Modifier
                        .padding(horizontal = 1.dp)
                        .offset(y = top)
                        .height(height)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (done) color.copy(alpha = 0.35f) else color)
                        .clickable { onTaskClick(task) }
                        .padding(horizontal = 2.dp)
                ) {
                    if (height > 20.dp) {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.ui.graphics.Color.White,
                            maxLines = if (height > 36.dp) 2 else 1,
                            textDecoration = if (done) TextDecoration.LineThrough else null
                        )
                    }
                }
            }
        }
        if (date == LocalDate.now()) {
            val nowMinutes = LocalTime.now().let { it.hour * 60 + it.minute } - WEEK_START_HOUR * 60
            if (nowMinutes in 0..(WEEK_END_HOUR - WEEK_START_HOUR) * 60) {
                val top = WEEK_HOUR_HEIGHT * (nowMinutes / 60f)
                Box(
                    Modifier
                        .offset(y = top)
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(MaterialTheme.colorScheme.error)
                )
            }
        }
    }
}

private fun weekHourLabel(hour: Int): String {
    val h = hour % 24
    val display = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return display.toString()
}
