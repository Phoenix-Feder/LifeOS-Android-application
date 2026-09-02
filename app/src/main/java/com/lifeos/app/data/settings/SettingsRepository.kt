package com.lifeos.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lifeos_settings")

data class UserSettings(
    val morningPlanEnabled: Boolean = true,
    val morningPlanMinutes: Int = 7 * 60,       // 7:00 AM
    val dailyReviewEnabled: Boolean = true,
    val dailyReviewMinutes: Int = 21 * 60,      // 9:00 PM
    val journalReminderEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val weeklyReviewEnabled: Boolean = true,
    val monthlyReviewEnabled: Boolean = true
)

/** Thin wrapper over DataStore — no DAO/Room table needed for simple key-value prefs. */
class SettingsRepository(private val context: Context) {
    private object Keys {
        val MORNING_PLAN_ENABLED = booleanPreferencesKey("morning_plan_enabled")
        val MORNING_PLAN_MINUTES = intPreferencesKey("morning_plan_minutes")
        val DAILY_REVIEW_ENABLED = booleanPreferencesKey("daily_review_enabled")
        val DAILY_REVIEW_MINUTES = intPreferencesKey("daily_review_minutes")
        val JOURNAL_REMINDER_ENABLED = booleanPreferencesKey("journal_reminder_enabled")
        val TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
        val WEEKLY_REVIEW_ENABLED = booleanPreferencesKey("weekly_review_enabled")
        val MONTHLY_REVIEW_ENABLED = booleanPreferencesKey("monthly_review_enabled")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            morningPlanEnabled = p[Keys.MORNING_PLAN_ENABLED] ?: true,
            morningPlanMinutes = p[Keys.MORNING_PLAN_MINUTES] ?: 7 * 60,
            dailyReviewEnabled = p[Keys.DAILY_REVIEW_ENABLED] ?: true,
            dailyReviewMinutes = p[Keys.DAILY_REVIEW_MINUTES] ?: 21 * 60,
            journalReminderEnabled = p[Keys.JOURNAL_REMINDER_ENABLED] ?: true,
            taskRemindersEnabled = p[Keys.TASK_REMINDERS_ENABLED] ?: true,
            weeklyReviewEnabled = p[Keys.WEEKLY_REVIEW_ENABLED] ?: true,
            monthlyReviewEnabled = p[Keys.MONTHLY_REVIEW_ENABLED] ?: true
        )
    }

    suspend fun update(transform: (UserSettings) -> UserSettings) {
        context.dataStore.edit { p ->
            val current = UserSettings(
                morningPlanEnabled = p[Keys.MORNING_PLAN_ENABLED] ?: true,
                morningPlanMinutes = p[Keys.MORNING_PLAN_MINUTES] ?: 7 * 60,
                dailyReviewEnabled = p[Keys.DAILY_REVIEW_ENABLED] ?: true,
                dailyReviewMinutes = p[Keys.DAILY_REVIEW_MINUTES] ?: 21 * 60,
                journalReminderEnabled = p[Keys.JOURNAL_REMINDER_ENABLED] ?: true,
                taskRemindersEnabled = p[Keys.TASK_REMINDERS_ENABLED] ?: true,
                weeklyReviewEnabled = p[Keys.WEEKLY_REVIEW_ENABLED] ?: true,
                monthlyReviewEnabled = p[Keys.MONTHLY_REVIEW_ENABLED] ?: true
            )
            val next = transform(current)
            p[Keys.MORNING_PLAN_ENABLED] = next.morningPlanEnabled
            p[Keys.MORNING_PLAN_MINUTES] = next.morningPlanMinutes
            p[Keys.DAILY_REVIEW_ENABLED] = next.dailyReviewEnabled
            p[Keys.DAILY_REVIEW_MINUTES] = next.dailyReviewMinutes
            p[Keys.JOURNAL_REMINDER_ENABLED] = next.journalReminderEnabled
            p[Keys.TASK_REMINDERS_ENABLED] = next.taskRemindersEnabled
            p[Keys.WEEKLY_REVIEW_ENABLED] = next.weeklyReviewEnabled
            p[Keys.MONTHLY_REVIEW_ENABLED] = next.monthlyReviewEnabled
        }
    }
}
