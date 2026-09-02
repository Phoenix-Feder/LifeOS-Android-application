package com.lifeos.app.data.dao

import androidx.room.*
import com.lifeos.app.data.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE dueDateEpochDay = :epochDay ORDER BY dueTimeMinutes IS NULL, dueTimeMinutes ASC")
    fun observeForDate(epochDay: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dueDateEpochDay ASC, dueTimeMinutes ASC")
    fun observeForRange(startEpochDay: Long, endEpochDay: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'PENDING' AND dueDateEpochDay < :todayEpochDay ORDER BY dueDateEpochDay ASC")
    fun observeOverdue(todayEpochDay: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE objectiveId = :objectiveId")
    suspend fun getByObjective(objectiveId: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE dueDateEpochDay BETWEEN :startEpochDay AND :endEpochDay")
    suspend fun getForRange(startEpochDay: Long, endEpochDay: Long): List<Task>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
