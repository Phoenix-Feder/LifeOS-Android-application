package com.lifeos.app.notification

import com.lifeos.app.data.entity.NotificationType
import com.lifeos.app.data.settings.SettingsRepository
import com.lifeos.app.data.settings.UserSettings
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

// Fixed, non-configurable time for the journal nudge — kept simple rather
// than adding another time-picker setting for a "soft" reminder.
private const val JOURNAL_REMINDER_MINUTES = 20 * 60 // 8:00 PM
private const val WEEKLY_REVIEW_MINUTES = 18 * 60     // Sunday 6:00 PM
private const val MONTHLY_REVIEW_MINUTES = 18 * 60    // last day of month, 6:00 PM

/**
 * Computes the *next* trigger time for a recurring notification type and
 * (re)arms it via NotificationScheduler. Called:
 *  - on every app start (LifeOSApplication) so alarms are always correct
 *    even if settings changed while the app wasn't running
 *  - whenever the user changes a setting (SettingsViewModel)
 *  - by NotificationReceiver right after a recurring notification fires,
 *    so the next occurrence gets armed immediately instead of relying on
 *    the app being reopened
 */
class RecurringNotificationPlanner(
    private val scheduler: NotificationScheduler,
    private val settingsRepo: SettingsRepository
) {
    private val zone = ZoneId.systemDefault()

    suspend fun scheduleAll() {
        val settings = settingsRepo.settings.first()
        applyMorningPlan(settings)
        applyDailyReview(settings)
        applyJournalReminder(settings)
        applyWeeklyReview(settings)
        applyMonthlyReview(settings)
    }

    /** Called by NotificationReceiver after a recurring type fires, to arm the next occurrence. */
    suspend fun rescheduleAfterFiring(type: NotificationType) {
        val settings = settingsRepo.settings.first()
        when (type) {
            NotificationType.MORNING_PLAN -> applyMorningPlan(settings, forceNextDay = true)
            NotificationType.DAILY_REVIEW -> applyDailyReview(settings, forceNextDay = true)
            NotificationType.JOURNAL_REMINDER -> applyJournalReminder(settings, forceNextDay = true)
            NotificationType.WEEKLY_REVIEW -> applyWeeklyReview(settings, forceNextWeek = true)
            NotificationType.MONTHLY_REVIEW -> applyMonthlyReview(settings, forceNextMonth = true)
            else -> Unit // one-off task notifications aren't recurring
        }
    }

    private fun nextDailyTrigger(minutesOfDay: Int, forceNextDay: Boolean): Long {
        val now = LocalDateTime.now(zone)
        var date = now.toLocalDate()
        var candidate = date.atTime(minutesOfDay / 60, minutesOfDay % 60)
        if (forceNextDay || !candidate.isAfter(now)) {
            date = date.plusDays(1)
            candidate = date.atTime(minutesOfDay / 60, minutesOfDay % 60)
        }
        return candidate.atZone(zone).toInstant().toEpochMilli()
    }

    private suspend fun applyMorningPlan(settings: UserSettings, forceNextDay: Boolean = false) {
        if (settings.morningPlanEnabled) {
            val trigger = nextDailyTrigger(settings.morningPlanMinutes, forceNextDay)
            scheduler.scheduleOrReplace(NotificationType.MORNING_PLAN, null, trigger, "Plan your day", "Open LifeOS to review today's tasks and objectives.")
        } else {
            scheduler.cancel(NotificationType.MORNING_PLAN, null)
        }
    }

    private suspend fun applyDailyReview(settings: UserSettings, forceNextDay: Boolean = false) {
        if (settings.dailyReviewEnabled) {
            val trigger = nextDailyTrigger(settings.dailyReviewMinutes, forceNextDay)
            scheduler.scheduleOrReplace(NotificationType.DAILY_REVIEW, null, trigger, "Daily review", "Take a minute to reflect on today.")
        } else {
            scheduler.cancel(NotificationType.DAILY_REVIEW, null)
        }
    }

    private suspend fun applyJournalReminder(settings: UserSettings, forceNextDay: Boolean = false) {
        if (settings.journalReminderEnabled) {
            val trigger = nextDailyTrigger(JOURNAL_REMINDER_MINUTES, forceNextDay)
            scheduler.scheduleOrReplace(NotificationType.JOURNAL_REMINDER, null, trigger, "Journal", "Write a line or two about today.")
        } else {
            scheduler.cancel(NotificationType.JOURNAL_REMINDER, null)
        }
    }

    private suspend fun applyWeeklyReview(settings: UserSettings, forceNextWeek: Boolean = false) {
        if (settings.weeklyReviewEnabled) {
            val now = LocalDateTime.now(zone)
            var date = now.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            var candidate = date.atTime(WEEKLY_REVIEW_MINUTES / 60, WEEKLY_REVIEW_MINUTES % 60)
            if (forceNextWeek || !candidate.isAfter(now)) {
                date = date.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                candidate = date.atTime(WEEKLY_REVIEW_MINUTES / 60, WEEKLY_REVIEW_MINUTES % 60)
            }
            val trigger = candidate.atZone(zone).toInstant().toEpochMilli()
            scheduler.scheduleOrReplace(NotificationType.WEEKLY_REVIEW, null, trigger, "Weekly review", "Look back on your week in LifeOS.")
        } else {
            scheduler.cancel(NotificationType.WEEKLY_REVIEW, null)
        }
    }

    private suspend fun applyMonthlyReview(settings: UserSettings, forceNextMonth: Boolean = false) {
        if (settings.monthlyReviewEnabled) {
            val now = LocalDateTime.now(zone)
            var date = LocalDate.now(zone).with(TemporalAdjusters.lastDayOfMonth())
            var candidate = date.atTime(MONTHLY_REVIEW_MINUTES / 60, MONTHLY_REVIEW_MINUTES % 60)
            if (forceNextMonth || !candidate.isAfter(now)) {
                date = date.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
                candidate = date.atTime(MONTHLY_REVIEW_MINUTES / 60, MONTHLY_REVIEW_MINUTES % 60)
            }
            val trigger = candidate.atZone(zone).toInstant().toEpochMilli()
            scheduler.scheduleOrReplace(NotificationType.MONTHLY_REVIEW, null, trigger, "Monthly review", "Look back on your month in LifeOS.")
        } else {
            scheduler.cancel(NotificationType.MONTHLY_REVIEW, null)
        }
    }
}
