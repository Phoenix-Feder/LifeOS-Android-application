package com.lifeos.app.data.dao

import androidx.room.*
import com.lifeos.app.data.entity.DailyReview
import com.lifeos.app.data.entity.PeriodicReview
import com.lifeos.app.data.entity.ReviewPeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReviewDao {
    @Query("SELECT * FROM daily_reviews ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<DailyReview>>

    @Query("SELECT * FROM daily_reviews WHERE dateEpochDay = :epochDay LIMIT 1")
    suspend fun getForDate(epochDay: Long): DailyReview?

    @Insert
    suspend fun insert(review: DailyReview): Long

    @Update
    suspend fun update(review: DailyReview)

    @Delete
    suspend fun delete(review: DailyReview)
}

@Dao
interface PeriodicReviewDao {
    @Query("SELECT * FROM periodic_reviews WHERE period = :period ORDER BY periodStartEpochDay DESC")
    fun observeByPeriod(period: ReviewPeriod): Flow<List<PeriodicReview>>

    @Query("SELECT * FROM periodic_reviews WHERE period = :period AND periodStartEpochDay = :startEpochDay LIMIT 1")
    suspend fun find(period: ReviewPeriod, startEpochDay: Long): PeriodicReview?

    @Insert
    suspend fun insert(review: PeriodicReview): Long

    @Update
    suspend fun update(review: PeriodicReview)

    @Delete
    suspend fun delete(review: PeriodicReview)
}
