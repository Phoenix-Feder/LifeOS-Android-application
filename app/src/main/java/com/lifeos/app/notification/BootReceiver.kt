package com.lifeos.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lifeos.app.LifeOSApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** AlarmManager alarms are cleared on reboot, so re-register every pending one. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val app = context.applicationContext as LifeOSApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pending = app.database.notificationDao().getAllPending()
                app.notificationScheduler.rescheduleAllPending(pending)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
