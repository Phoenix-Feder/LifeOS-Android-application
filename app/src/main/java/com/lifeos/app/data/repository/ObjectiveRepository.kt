package com.lifeos.app.data.repository

import com.lifeos.app.data.dao.ObjectiveDao
import com.lifeos.app.data.dao.ObjectiveInstanceDao
import com.lifeos.app.data.entity.Frequency
import com.lifeos.app.data.entity.InstanceStatus
import com.lifeos.app.data.entity.Objective
import com.lifeos.app.data.entity.ObjectiveInstance
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate

class ObjectiveRepository(
    private val objectiveDao: ObjectiveDao,
    private val instanceDao: ObjectiveInstanceDao
) {
    fun observeActive(): Flow<List<Objective>> = objectiveDao.observeActive()
    fun observeAll(): Flow<List<Objective>> = objectiveDao.observeAll()
    fun observeInstancesForDate(date: LocalDate): Flow<List<ObjectiveInstance>> =
        instanceDao.observeForDate(date.toEpochDay())

    suspend fun getById(id: Long) = objectiveDao.getById(id)
    suspend fun getInstancesForObjective(objectiveId: Long) = instanceDao.getForObjective(objectiveId)
    suspend fun getInstancesForRange(start: LocalDate, end: LocalDate) =
        instanceDao.getForRange(start.toEpochDay(), end.toEpochDay())

    suspend fun create(objective: Objective): Long = objectiveDao.insert(objective)
    suspend fun update(objective: Objective) = objectiveDao.update(objective)
    suspend fun delete(objective: Objective) = objectiveDao.delete(objective)

    suspend fun setInstanceStatus(instance: ObjectiveInstance, status: InstanceStatus) =
        instanceDao.update(instance.copy(status = status))

    /** True if [objective] is scheduled to occur on [date] per its frequency rule. */
    private fun occursOn(objective: Objective, date: LocalDate): Boolean = when (objective.frequency) {
        Frequency.DAILY -> true
        Frequency.WEEKLY -> date.dayOfWeek == DayOfWeek.MONDAY
        Frequency.CUSTOM_DAYS -> {
            val bit = 1 shl (date.dayOfWeek.value - 1) // Mon=1<<0 ... Sun=1<<6
            (objective.customDaysMask and bit) != 0
        }
    }

    /**
     * Ensures ObjectiveInstance rows exist for every active objective for
     * every date in [start, end]. Idempotent — safe to call on every app open.
     */
    suspend fun ensureInstancesGenerated(start: LocalDate, end: LocalDate) {
        val active = objectiveDao.getAllActive()
        if (active.isEmpty()) return
        val toInsert = mutableListOf<ObjectiveInstance>()
        var date = start
        while (!date.isAfter(end)) {
            for (objective in active) {
                if (occursOn(objective, date) &&
                    instanceDao.findInstance(objective.id, date.toEpochDay()) == null
                ) {
                    toInsert += ObjectiveInstance(objectiveId = objective.id, dateEpochDay = date.toEpochDay())
                }
            }
            date = date.plusDays(1)
        }
        if (toInsert.isNotEmpty()) instanceDao.insertAll(toInsert)
    }
}
