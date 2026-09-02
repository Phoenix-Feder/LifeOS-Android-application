package com.lifeos.app.data.repository

import com.lifeos.app.data.entity.InstanceStatus
import com.lifeos.app.data.entity.TaskStatus
import java.time.LocalDate

data class AnalyticsSummary(
    val plannedTasks: Int,
    val completedTasks: Int,
    val scheduledObjectiveInstances: Int,
    val completedObjectiveInstances: Int,
    val eligibleJournalDays: Int,
    val journaledDays: Int
) {
    val taskCompletionRate: Float
        get() = if (plannedTasks == 0) 0f else completedTasks.toFloat() / plannedTasks

    val objectiveConsistency: Float
        get() = if (scheduledObjectiveInstances == 0) 0f
        else completedObjectiveInstances.toFloat() / scheduledObjectiveInstances

    val journalConsistency: Float
        get() = if (eligibleJournalDays == 0) 0f else journaledDays.toFloat() / eligibleJournalDays
}

/**
 * Pulls raw rows for a date range and derives simple rates in Kotlin.
 * No separate "analytics engine" — just SQL + arithmetic, per spec.
 */
class AnalyticsRepository(
    private val taskRepository: TaskRepository,
    private val objectiveRepository: ObjectiveRepository,
    private val journalRepository: JournalRepository
) {
    suspend fun summaryFor(start: LocalDate, end: LocalDate): AnalyticsSummary {
        val tasks = taskRepository.getForRange(start, end)
        val instances = objectiveRepository.getInstancesForRange(start, end)
        val journalEntries = journalRepository.getForRange(start, end)

        val eligibleDays = (start.toEpochDay()..end.toEpochDay()).count { it <= LocalDate.now().toEpochDay() }
        val journaledDays = journalEntries.map { it.dateEpochDay }.distinct().size

        return AnalyticsSummary(
            plannedTasks = tasks.size,
            completedTasks = tasks.count { it.status == TaskStatus.COMPLETED },
            scheduledObjectiveInstances = instances.size,
            completedObjectiveInstances = instances.count { it.status == InstanceStatus.COMPLETED },
            eligibleJournalDays = eligibleDays,
            journaledDays = journaledDays
        )
    }
}
