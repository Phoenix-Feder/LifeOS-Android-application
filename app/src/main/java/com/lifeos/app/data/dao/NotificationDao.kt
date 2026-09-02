package com.lifeos.app.data.dao

import androidx.room.*
import com.lifeos.app.data.entity.NotificationSchedule
import com.lifeos.app.data.entity.NotificationType

@Dao
interface NotificationDao {
    // "targetId IS :targetId" rather than "=" — SQLite's `=` never matches
    // NULL even against a NULL column, so recurring notifications (which
    // always pass targetId = null) would never be found or cleaned up with
    // a plain equality check. `IS` is the NULL-safe equivalent.
    @Query("SELECT * FROM notification_schedules WHERE type = :type AND targetId IS :targetId AND fired = 0")
    suspend fun findPending(type: NotificationType, targetId: Long?): List<NotificationSchedule>

    @Query("SELECT * FROM notification_schedules WHERE id = :id")
    suspend fun getById(id: Long): NotificationSchedule?

    @Query("SELECT * FROM notification_schedules WHERE fired = 0")
    suspend fun getAllPending(): List<NotificationSchedule>

    @Insert
    suspend fun insert(schedule: NotificationSchedule): Long

    @Update
    suspend fun update(schedule: NotificationSchedule)

    @Query("DELETE FROM notification_schedules WHERE type = :type AND targetId IS :targetId")
    suspend fun deleteForTarget(type: NotificationType, targetId: Long?)

    @Delete
    suspend fun delete(schedule: NotificationSchedule)
}
