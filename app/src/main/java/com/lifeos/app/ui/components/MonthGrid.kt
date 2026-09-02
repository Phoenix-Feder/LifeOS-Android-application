package com.lifeos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Summary of a single day, used to render its dot/ring in the month grid. */
data class DayMarker(
    val taskCount: Int = 0,
    val completedTaskCount: Int = 0,
    val hasObjectiveActivity: Boolean = false
)

@Composable
fun LifeOSMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    markers: Map<LocalDate, DayMarker>,
    onDayClick: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month") }
            Text(
                "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, contentDescription = "Next month") }
        }

        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(d, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        val firstOfMonth = month.atDay(1)
        val leadingBlanks = firstOfMonth.dayOfWeek.value - 1 // Mon=1 -> 0 blanks
        val totalDays = month.lengthOfMonth()
        val totalCells = leadingBlanks + totalDays
        val rows = (totalCells + 6) / 7

        var dayCounter = 1
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    if (cellIndex < leadingBlanks || dayCounter > totalDays) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = month.atDay(dayCounter)
                        val marker = markers[date]
                        DayCell(
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            date = date,
                            marker = marker,
                            onClick = { onDayClick(date) },
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                        )
                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    marker: DayMarker?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.padding(3.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    date.dayOfMonth.toString(),
                    style = if (isToday && !isSelected) MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                if (marker != null && marker.taskCount > 0) {
                    val dotColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else if (marker.completedTaskCount == marker.taskCount) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                    LifeOSDot(dotColor)
                }
            }
        }
    }
}
