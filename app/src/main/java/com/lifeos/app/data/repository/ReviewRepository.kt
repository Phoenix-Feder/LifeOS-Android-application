package com.lifeos.app.data.repository

import com.lifeos.app.data.dao.DailyReviewDao
import com.lifeos.app.data.dao.PeriodicReviewDao
import com.lifeos.app.data.entity.DailyReview
import com.lifeos.app.data.entity.PeriodicReview
import com.lifeos.app.data.entity.ReviewPeriod
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ReviewRepository(
    private val dailyDao: DailyReviewDao,
    private val periodicDao: PeriodicReviewDao
) {
    fun observeDaily(): Flow<List<DailyReview>> = dailyDao.observeAll()
    fun observeWeekly(): Flow<List<PeriodicReview>> = periodicDao.observeByPeriod(ReviewPeriod.WEEKLY)
    fun observeMonthly(): Flow<List<PeriodicReview>> = periodicDao.observeByPeriod(ReviewPeriod.MONTHLY)

    suspend fun getDailyFor(date: LocalDate) = dailyDao.getForDate(date.toEpochDay())
    suspend fun upsertDaily(review: DailyReview) =
        if (review.id == 0L) dailyDao.insert(review) else { dailyDao.update(review); review.id }

    suspend fun getPeriodic(period: ReviewPeriod, start: LocalDate) = periodicDao.find(period, start.toEpochDay())
    suspend fun upsertPeriodic(review: PeriodicReview) =
        if (review.id == 0L) periodicDao.insert(review) else { periodicDao.update(review); review.id }

    suspend fun deleteDaily(review: DailyReview) = dailyDao.delete(review)
    suspend fun deletePeriodic(review: PeriodicReview) = periodicDao.delete(review)
}
