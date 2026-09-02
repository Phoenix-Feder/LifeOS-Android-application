package com.lifeos.app.notification

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Notifications on modern Android silently fail to arrive (or arrive very
 * late) unless three separate things are true: the app has notification
 * permission, it's allowed to schedule *exact* alarms, and it's exempt from
 * battery-optimization throttling ("allowed to run in the background").
 * Each is checked and requested independently since a person can grant one
 * without the others.
 */
object PermissionHelper {

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /** Deep-links to the system page where the person grants "Alarms & reminders". */
    fun exactAlarmSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))

    /**
     * The direct system dialog ("Allow LifeOS to ignore battery optimizations?")
     * rather than a settings page — this is what actually lets alarms/notifications
     * keep working while the app is in the background or the phone is idle.
     */
    fun batteryOptimizationIntent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))

    fun allPermissionsGranted(context: Context): Boolean =
        hasNotificationPermission(context) && canScheduleExactAlarms(context) && isIgnoringBatteryOptimizations(context)
}
