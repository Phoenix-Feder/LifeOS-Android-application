package com.lifeos.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lifeos.app.data.dao.NotificationDao
import com.lifeos.app.data.entity.NotificationSchedule
import com.lifeos.app.data.entity.NotificationType

const val EXTRA_SCHEDULE_ID = "schedule_id"

/**
 * Single entry point for scheduling/cancelling every notification kind in
 * the app. Reusable across MORNING_PLAN, TASK_REMINDER, DAILY_REVIEW, etc. —
 * no per-notification-type class.
 */
class NotificationScheduler(
    private val context: Context,
    private val dao: NotificationDao
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Cancels any pending notification of this type+target, then schedules a fresh one. */
    suspend fun scheduleOrReplace(
        type: NotificationType,
        targetId: Long?,
        triggerAtEpochMillis: Long,
        title: String,
        body: String
    ) {
        cancel(type, targetId)
        if (triggerAtEpochMillis <= System.currentTimeMillis()) return // don't schedule things in the past
        val schedule = NotificationSchedule(
            type = type,
            targetId = targetId,
            triggerAtEpochMillis = triggerAtEpochMillis,
            title = title,
            body = body
        )
        val id = dao.insert(schedule)
        setAlarm(id, triggerAtEpochMillis)
    }

    /**
     * Single entry point for "remind me when this task starts" — used on
     * create, edit, and reschedule alike, so callers never have to remember
     * to separately cancel a stale alarm before arming a new one.
     */
    suspend fun scheduleTaskStart(
        taskId: Long,
        title: String,
        dueDateEpochDay: Long,
        dueTimeMinutes: Int?,
        enabled: Boolean
    ) {
        if (!enabled || dueTimeMinutes == null) {
            cancel(NotificationType.TASK_START, taskId)
            return
        }
        val zone = java.time.ZoneId.systemDefault()
        val trigger = java.time.LocalDate.ofEpochDay(dueDateEpochDay)
            .atTime(dueTimeMinutes / 60, dueTimeMinutes % 60)
            .atZone(zone).toInstant().toEpochMilli()
        scheduleOrReplace(NotificationType.TASK_START, taskId, trigger, "Task starting", title)
    }

    suspend fun cancel(type: NotificationType, targetId: Long?) {
        val pending = dao.findPending(type, targetId)
        pending.forEach { cancelAlarm(it.id) }
        dao.deleteForTarget(type, targetId)
    }

    /** Re-registers every un-fired, future alarm with the OS. Call after boot. */
    suspend fun rescheduleAllPending(schedules: List<NotificationSchedule>) {
        val now = System.currentTimeMillis()
        schedules.filter { !it.fired && it.triggerAtEpochMillis > now }
            .forEach { setAlarm(it.id, it.triggerAtEpochMillis) }
    }

    private fun setAlarm(scheduleId: Long, triggerAtEpochMillis: Long) {
        val intent = Intent(context, NotificationReceiver::class.java).putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        val pendingIntent = PendingIntent.getBroadcast(
            context, scheduleId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtEpochMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtEpochMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(scheduleId: Long) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, scheduleId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
