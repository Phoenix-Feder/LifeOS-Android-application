package com.lifeos.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.lifeos.app.notification.PermissionHelper
import com.lifeos.app.ui.navigation.LifeOSNavHost
import com.lifeos.app.ui.theme.LifeOSTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Whatever the outcome, offer the background-run prompt next —
            // it's a separate permission with its own dialog.
            requestBatteryOptimizationExemptionIfNeeded()
        }

    private val requestIgnoreBatteryOptimizations =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        setContent {
            LifeOSTheme {
                LifeOSNavHost()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !PermissionHelper.hasNotificationPermission(this)) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            requestBatteryOptimizationExemptionIfNeeded()
        }
    }

    /**
     * This is the permission that actually keeps reminders arriving on time —
     * without it, Android's battery optimizer can delay or drop alarms while
     * the app isn't in the foreground. Only prompts if not already granted,
     * so it stops appearing once the person allows it.
     */
    private fun requestBatteryOptimizationExemptionIfNeeded() {
        if (!PermissionHelper.isIgnoringBatteryOptimizations(this)) {
            requestIgnoreBatteryOptimizations.launch(PermissionHelper.batteryOptimizationIntent(this))
        }
    }
}
