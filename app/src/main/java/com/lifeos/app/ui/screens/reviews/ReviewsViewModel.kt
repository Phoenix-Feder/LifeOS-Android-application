package com.lifeos.app.ui.screens.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.LifeOSApplication
import com.lifeos.app.data.entity.DailyReview
import com.lifeos.app.data.entity.PeriodicReview
import com.lifeos.app.data.entity.ReviewPeriod
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReviewsViewModel(private val app: LifeOSApplication) : ViewModel() {
    private val repo = app.reviewRepository

    val dailyReviews: StateFlow<List<DailyReview>> = repo.observeDaily()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weeklyReviews: StateFlow<List<PeriodicReview>> = repo.observeWeekly()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val monthlyReviews: StateFlow<List<PeriodicReview>> = repo.observeMonthly()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveDailyReview(wentWell: String, toImprove: String, rating: Int?) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val existing = repo.getDailyFor(today)
            repo.upsertDaily(
                (existing ?: DailyReview(dateEpochDay = today.toEpochDay(), createdAtEpochMillis = System.currentTimeMillis()))
                    .copy(wentWell = wentWell, toImprove = toImprove, rating = rating)
            )
        }
    }

    fun saveWeeklyReview(content: String) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val start = today.minusDays(today.dayOfWeek.value - 1L)
            val existing = repo.getPeriodic(ReviewPeriod.WEEKLY, start)
            repo.upsertPeriodic(
                (existing ?: PeriodicReview(
                    period = ReviewPeriod.WEEKLY,
                    periodStartEpochDay = start.toEpochDay(),
                    periodEndEpochDay = start.plusDays(6).toEpochDay(),
                    createdAtEpochMillis = System.currentTimeMillis()
                )).copy(content = content)
            )
        }
    }

    fun saveMonthlyReview(content: String) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val start = today.withDayOfMonth(1)
            val existing = repo.getPeriodic(ReviewPeriod.MONTHLY, start)
            repo.upsertPeriodic(
                (existing ?: PeriodicReview(
                    period = ReviewPeriod.MONTHLY,
                    periodStartEpochDay = start.toEpochDay(),
                    periodEndEpochDay = start.plusMonths(1).minusDays(1).toEpochDay(),
                    createdAtEpochMillis = System.currentTimeMillis()
                )).copy(content = content)
            )
        }
    }

    fun deleteDailyReview(review: DailyReview) {
        viewModelScope.launch { repo.deleteDaily(review) }
    }

    fun deletePeriodicReview(review: PeriodicReview) {
        viewModelScope.launch { repo.deletePeriodic(review) }
    }
}
