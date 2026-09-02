package com.lifeos.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * All notification kinds the app can fire. One enum + one scheduling table
 * instead of a bespoke system per kind.
 */
enum class NotificationType {
    MORNING_PLAN,
    TASK_REMINDER,
    TASK_START,
    TASK_OVERDUE,
    DAILY_REVIEW,
    JOURNAL_REMINDER,
    WEEKLY_REVIEW,
    MONTHLY_REVIEW,
    OBJECTIVE_MILESTONE
}

/**
 * A single scheduled (or already-fired) notification. AlarmManager is given
 * `id` as the request code so it can be uniquely cancelled/updated.
 * `targetId` points at the related Task/Objective/etc. row when applicable.
 */
@Entity(tableName = "notification_schedules")
data class NotificationSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: NotificationType,
    val targetId: Long? = null,
    val triggerAtEpochMillis: Long,
    val title: String,
    val body: String,
    val fired: Boolean = false
)
