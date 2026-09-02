package com.lifeos.app

import android.app.Application
import com.lifeos.app.data.db.LifeOSDatabase
import com.lifeos.app.data.repository.AnalyticsRepository
import com.lifeos.app.data.repository.JournalRepository
import com.lifeos.app.data.repository.ObjectiveRepository
import com.lifeos.app.data.repository.ReviewRepository
import com.lifeos.app.data.repository.TaskRepository
import com.lifeos.app.data.settings.SettingsRepository
import com.lifeos.app.notification.NotificationHelper
import com.lifeos.app.notification.NotificationScheduler
import com.lifeos.app.notification.RecurringNotificationPlanner
import kotlinx.coroutines.launch

/**
 * Manual DI via simple lazy properties — no Hilt/Dagger. The app is
 * single-user/offline and small enough that a DI framework would only add
 * boilerplate.
 */
class LifeOSApplication : Application() {

    val database: LifeOSDatabase by lazy { LifeOSDatabase.get(this) }

    val taskRepository: TaskRepository by lazy { TaskRepository(database.taskDao()) }
    val objectiveRepository: ObjectiveRepository by lazy {
        ObjectiveRepository(database.objectiveDao(), database.objectiveInstanceDao())
    }
    val journalRepository: JournalRepository by lazy { JournalRepository(database.journalDao()) }
    val reviewRepository: ReviewRepository by lazy {
        ReviewRepository(database.dailyReviewDao(), database.periodicReviewDao())
    }
    val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepository(taskRepository, objectiveRepository, journalRepository)
    }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    val notificationScheduler: NotificationScheduler by lazy {
        NotificationScheduler(this, database.notificationDao())
    }
    val recurringNotificationPlanner: RecurringNotificationPlanner by lazy {
        RecurringNotificationPlanner(notificationScheduler, settingsRepository)
    }

    // Small app-lifetime scope for startup work that must survive the launching screen.
    private val applicationScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        // Re-arm every recurring notification on every app start — covers first
        // install, settings that changed while the app was closed, and alarms
        // that (for any reason) didn't get re-armed after their last firing.
        applicationScope.launch { recurringNotificationPlanner.scheduleAll() }
    }
}
