package com.lifeos.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskStatus { PENDING, COMPLETED, SKIPPED }
enum class Priority { LOW, MEDIUM, HIGH }

/**
 * A single actionable to-do. Optionally linked to an Objective when it was
 * generated from one (e.g. "Read 20 pages" under the "Read more" objective).
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDateEpochDay: Long,       // LocalDate.toEpochDay()
    val dueTimeMinutes: Int? = null, // minutes since midnight, null = all-day / unscheduled
    val durationMinutes: Int = 60,   // block length when placed on the hourly timeline
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val objectiveId: Long? = null,
    val createdAtEpochMillis: Long,
    val completedAtEpochMillis: Long? = null
)
