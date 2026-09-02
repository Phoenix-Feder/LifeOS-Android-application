package com.lifeos.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lifeos.app.LifeOSApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        if (scheduleId == -1L) return
        val pendingResult = goAsync()
        val app = context.applicationContext as LifeOSApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = app.database.notificationDao()
                val schedule = dao.getById(scheduleId)
                if (schedule != null && !schedule.fired) {
                    NotificationHelper.ensureChannel(context)
                    NotificationHelper.show(context, scheduleId.toInt(), schedule.title, schedule.body)
                    dao.update(schedule.copy(fired = true))
                    // Recurring types (morning plan, reviews, journal nudge) re-arm
                    // their next occurrence immediately, instead of waiting for the
                    // app to be reopened.
                    app.recurringNotificationPlanner.rescheduleAfterFiring(schedule.type)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
