package com.lifeos.app.data.repository

import com.lifeos.app.data.dao.JournalDao
import com.lifeos.app.data.entity.JournalEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class JournalRepository(private val dao: JournalDao) {
    fun observeAll(): Flow<List<JournalEntry>> = dao.observeAll()
    fun observeForDate(date: LocalDate): Flow<List<JournalEntry>> = dao.observeForDate(date.toEpochDay())
    suspend fun getForRange(start: LocalDate, end: LocalDate) = dao.getForRange(start.toEpochDay(), end.toEpochDay())

    suspend fun create(entry: JournalEntry): Long = dao.insert(entry)
    suspend fun update(entry: JournalEntry) = dao.update(entry)
    suspend fun delete(entry: JournalEntry) = dao.delete(entry)
}
