package com.lifeos.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Frequency { DAILY, WEEKLY, CUSTOM_DAYS }

/**
 * A recurring goal, e.g. "Exercise" (daily) or "Deep clean apartment" (weekly).
 * Concrete occurrences are materialized as ObjectiveInstance rows.
 */
@Entity(tableName = "objectives")
data class Objective(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val frequency: Frequency = Frequency.DAILY,
    /** Bitmask of days for CUSTOM_DAYS, 1=Mon .. 64=Sun. Ignored otherwise. */
    val customDaysMask: Int = 0,
    val active: Boolean = true,
    val createdAtEpochMillis: Long
)

enum class InstanceStatus { PENDING, COMPLETED, SKIPPED }

/** One scheduled occurrence of an Objective on a given date. */
@Entity(tableName = "objective_instances")
data class ObjectiveInstance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val objectiveId: Long,
    val dateEpochDay: Long,
    val status: InstanceStatus = InstanceStatus.PENDING
)
