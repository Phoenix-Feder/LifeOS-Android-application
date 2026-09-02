package com.lifeos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.format.TextStyle
import java.util.Locale

/**
 * A 7-day strip with a completion dot per day. Reused by Today (contextual
 * week glance) and Calendar's Week view (day picker).
 */
@Composable
fun LifeOSWeekStrip(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    markers: Map<LocalDate, DayMarker>,
    onSelectDate: (LocalDate) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier,
    showNav: Boolean = true
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showNav) {
                IconButton(onClick = onPrevWeek) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous week") }
            }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (0..6).forEach { offset ->
                    val date = weekStart.plusDays(offset.toLong())
                    val selected = date == selectedDate
                    val today = date == LocalDate.now()
                    val marker = markers[date]
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { onSelectDate(date) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = when {
                                selected -> MaterialTheme.colorScheme.onPrimary
                                today -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (marker != null && marker.taskCount > 0) {
                            Spacer(Modifier.height(3.dp))
                            LifeOSDot(
                                if (selected) MaterialTheme.colorScheme.onPrimary
                                else if (marker.completedTaskCount == marker.taskCount) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            if (showNav) {
                IconButton(onClick = onNextWeek) { Icon(Icons.Default.ChevronRight, contentDescription = "Next week") }
            }
        }
    }
}
