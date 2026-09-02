package com.lifeos.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_reviews")
data class DailyReview(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val wentWell: String = "",
    val toImprove: String = "",
    /** 1-5 self-rating for the day. */
    val rating: Int? = null,
    val createdAtEpochMillis: Long
)

enum class ReviewPeriod { WEEKLY, MONTHLY }

@Entity(tableName = "periodic_reviews")
data class PeriodicReview(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val period: ReviewPeriod,
    val periodStartEpochDay: Long,
    val periodEndEpochDay: Long,
    val content: String = "",
    val createdAtEpochMillis: Long
)
