package com.lifeos.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val content: String,
    /** 1-5 optional mood rating, null if not recorded. */
    val mood: Int? = null,
    val createdAtEpochMillis: Long
)
