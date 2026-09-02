package com.lifeos.app.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.LifeOSApplication
import com.lifeos.app.data.repository.AnalyticsSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AnalyticsRange(val days: Long, val label: String) {
    WEEK(7, "7 days"),
    MONTH(30, "30 days")
}

class AnalyticsViewModel(private val app: LifeOSApplication) : ViewModel() {
    private val repo = app.analyticsRepository

    private val _range = MutableStateFlow(AnalyticsRange.WEEK)
    val range: StateFlow<AnalyticsRange> = _range.asStateFlow()

    private val _summary = MutableStateFlow<AnalyticsSummary?>(null)
    val summary: StateFlow<AnalyticsSummary?> = _summary.asStateFlow()

    init { refresh() }

    fun setRange(range: AnalyticsRange) {
        _range.value = range
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            val today = LocalDate.now()
            _summary.value = repo.summaryFor(today.minusDays(_range.value.days - 1), today)
        }
    }
}
