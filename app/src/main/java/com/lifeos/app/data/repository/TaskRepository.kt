package com.lifeos.app.data.repository

import com.lifeos.app.data.dao.TaskDao
import com.lifeos.app.data.entity.Task
import com.lifeos.app.data.entity.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TaskRepository(private val dao: TaskDao) {
    fun observeForDate(date: LocalDate): Flow<List<Task>> = dao.observeForDate(date.toEpochDay())
    fun observeForRange(start: LocalDate, end: LocalDate): Flow<List<Task>> =
        dao.observeForRange(start.toEpochDay(), end.toEpochDay())
    fun observeOverdue(today: LocalDate): Flow<List<Task>> = dao.observeOverdue(today.toEpochDay())

    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun getForRange(start: LocalDate, end: LocalDate) =
        dao.getForRange(start.toEpochDay(), end.toEpochDay())

    suspend fun create(task: Task): Long = dao.insert(task)

    suspend fun update(task: Task) = dao.update(task)

    suspend fun delete(task: Task) = dao.delete(task)

    suspend fun markCompleted(task: Task) =
        dao.update(task.copy(status = TaskStatus.COMPLETED, completedAtEpochMillis = System.currentTimeMillis()))

    suspend fun markSkipped(task: Task) = dao.update(task.copy(status = TaskStatus.SKIPPED))

    suspend fun reschedule(task: Task, newDate: LocalDate) =
        dao.update(task.copy(dueDateEpochDay = newDate.toEpochDay(), status = TaskStatus.PENDING))
}
