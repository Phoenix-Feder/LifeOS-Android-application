package com.lifeos.app.data.dao

import androidx.room.*
import com.lifeos.app.data.entity.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE dateEpochDay = :epochDay ORDER BY createdAtEpochMillis DESC")
    fun observeForDate(epochDay: Long): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay")
    suspend fun getForRange(startEpochDay: Long, endEpochDay: Long): List<JournalEntry>

    @Insert
    suspend fun insert(entry: JournalEntry): Long

    @Update
    suspend fun update(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)
}
