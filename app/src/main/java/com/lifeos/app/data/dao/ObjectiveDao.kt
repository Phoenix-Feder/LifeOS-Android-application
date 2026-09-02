package com.lifeos.app.data.dao

import androidx.room.*
import com.lifeos.app.data.entity.Objective
import com.lifeos.app.data.entity.ObjectiveInstance
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjectiveDao {
    @Query("SELECT * FROM objectives WHERE active = 1 ORDER BY createdAtEpochMillis DESC")
    fun observeActive(): Flow<List<Objective>>

    @Query("SELECT * FROM objectives ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<Objective>>

    @Query("SELECT * FROM objectives WHERE id = :id")
    suspend fun getById(id: Long): Objective?

    @Query("SELECT * FROM objectives WHERE active = 1")
    suspend fun getAllActive(): List<Objective>

    @Insert
    suspend fun insert(objective: Objective): Long

    @Update
    suspend fun update(objective: Objective)

    @Delete
    suspend fun delete(objective: Objective)
}

@Dao
interface ObjectiveInstanceDao {
    @Query("SELECT * FROM objective_instances WHERE dateEpochDay = :epochDay")
    fun observeForDate(epochDay: Long): Flow<List<ObjectiveInstance>>

    @Query("SELECT * FROM objective_instances WHERE objectiveId = :objectiveId ORDER BY dateEpochDay DESC")
    suspend fun getForObjective(objectiveId: Long): List<ObjectiveInstance>

    @Query("SELECT * FROM objective_instances WHERE objectiveId = :objectiveId AND dateEpochDay = :epochDay LIMIT 1")
    suspend fun findInstance(objectiveId: Long, epochDay: Long): ObjectiveInstance?

    @Query("SELECT * FROM objective_instances WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay")
    suspend fun getForRange(startEpochDay: Long, endEpochDay: Long): List<ObjectiveInstance>

    @Insert
    suspend fun insert(instance: ObjectiveInstance): Long

    @Insert
    suspend fun insertAll(instances: List<ObjectiveInstance>)

    @Update
    suspend fun update(instance: ObjectiveInstance)
}
