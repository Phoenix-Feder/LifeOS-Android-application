package com.lifeos.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifeos.app.notification.PermissionHelper
import com.lifeos.app.ui.common.AppViewModelFactory
import com.lifeos.app.ui.common.lifeOSApp
import com.lifeos.app.ui.components.LifeOSCard
import com.lifeos.app.ui.components.LifeOSSectionHeader
import com.lifeos.app.ui.components.LifeOSTopBar
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = lifeOSApp(context.applicationContext as android.app.Application)
    val viewModel: SettingsViewModel = viewModel(factory = AppViewModelFactory(app))
    val settings by viewModel.settings.collectAsState()
    var editingTimeFor by remember { mutableStateOf<String?>(null) }

    // Permission state doesn't come from a Flow — it's read from the OS — so
    // it's refreshed whenever the screen resumes (e.g. coming back from the
    // system settings page after granting something).
    var notificationsGranted by remember { mutableStateOf(PermissionHelper.hasNotificationPermission(context)) }
    var exactAlarmsGranted by remember { mutableStateOf(PermissionHelper.canScheduleExactAlarms(context)) }
    var batteryExempt by remember { mutableStateOf(PermissionHelper.isIgnoringBatteryOptimizations(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsGranted = PermissionHelper.hasNotificationPermission(context)
                exactAlarmsGranted = PermissionHelper.canScheduleExactAlarms(context)
                batteryExempt = PermissionHelper.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsGranted = PermissionHelper.hasNotificationPermission(context)
    }
    val exactAlarmLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        exactAlarmsGranted = PermissionHelper.canScheduleExactAlarms(context)
    }
    val batteryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        batteryExempt = PermissionHelper.isIgnoringBatteryOptimizations(context)
    }

    Scaffold(topBar = { LifeOSTopBar(title = "Settings") }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LifeOSSectionHeader("Permissions — required for reminders to actually arrive")
            LifeOSCard {
                PermissionRow(
                    title = "Notifications",
                    granted = notificationsGranted,
                    onFix = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            context.startActivity(intent)
                        }
                    }
                )
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                PermissionRow(
                    title = "Alarms & reminders",
                    subtitle = "Lets reminders fire at the exact time set",
                    granted = exactAlarmsGranted,
                    onFix = { exactAlarmLauncher.launch(PermissionHelper.exactAlarmSettingsIntent(context)) }
                )
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                PermissionRow(
                    title = "Run in background",
                    subtitle = "Stops Android from delaying or dropping reminders",
                    granted = batteryExempt,
                    onFix = { batteryLauncher.launch(PermissionHelper.batteryOptimizationIntent(context)) }
                )
            }

            LifeOSSectionHeader("Reminders")
            LifeOSCard {
                SettingRow(
                    title = "Morning plan",
                    subtitle = "Remind me at ${LocalTime.of(settings.morningPlanMinutes / 60, settings.morningPlanMinutes % 60)}",
                    checked = settings.morningPlanEnabled,
                    onCheckedChange = { viewModel.update { s -> s.copy(morningPlanEnabled = it) } },
                    onSubtitleClick = { editingTimeFor = "morning" }
                )
            }
            LifeOSCard {
                SettingRow(
                    title = "Daily review",
                    subtitle = "Remind me at ${LocalTime.of(settings.dailyReviewMinutes / 60, settings.dailyReviewMinutes % 60)}",
                    checked = settings.dailyReviewEnabled,
                    onCheckedChange = { viewModel.update { s -> s.copy(dailyReviewEnabled = it) } },
                    onSubtitleClick = { editingTimeFor = "review" }
                )
            }
            LifeOSCard {
                SettingRow(
                    title = "Journal reminder",
                    subtitle = "Nudge me to journal each evening",
                    checked = settings.journalReminderEnabled,
                    onCheckedChange = { viewModel.update { s -> s.copy(journalReminderEnabled = it) } }
                )
            }
            LifeOSCard {
                SettingRow(
                    title = "Task reminders",
                    subtitle = "Notify when a scheduled task starts",
                    checked = settings.taskRemindersEnabled,
                    onCheckedChange = { viewModel.update { s -> s.copy(taskRemindersEnabled = it) } }
                )
            }
            LifeOSCard {
                SettingRow(
                    title = "Weekly review",
                    subtitle = "Prompt a weekly reflection",
                    checked = settings.weeklyReviewEnabled,
                    onCheckedChange = { viewModel.update { s -> s.copy(weeklyReviewEnabled = it) } }
                )
            }
            LifeOSCard {
                SettingRow(
                    title = "Monthly review",
                    subtitle = "Prompt a monthly reflection",
                    checked = settings.monthlyReviewEnabled,
                    onCheckedChange = { viewModel.update { s -> s.copy(monthlyReviewEnabled = it) } }
                )
            }
        }
    }

    editingTimeFor?.let { target ->
        val initialMinutes = if (target == "morning") settings.morningPlanMinutes else settings.dailyReviewMinutes
        val timeState = rememberTimePickerState(initialHour = initialMinutes / 60, initialMinute = initialMinutes % 60)
        AlertDialog(
            onDismissRequest = { editingTimeFor = null },
            title = { Text("Choose a time") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val minutes = timeState.hour * 60 + timeState.minute
                    if (target == "morning") viewModel.update { it.copy(morningPlanMinutes = minutes) }
                    else viewModel.update { it.copy(dailyReviewMinutes = minutes) }
                    editingTimeFor = null
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { editingTimeFor = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun PermissionRow(
    title: String,
    granted: Boolean,
    onFix: () -> Unit,
    subtitle: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (granted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle ?: if (granted) "Granted" else "Not granted",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!granted) {
            TextButton(onClick = onFix) { Text("Fix") }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onSubtitleClick: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = if (onSubtitleClick != null) Modifier.clickable(onClick = onSubtitleClick) else Modifier
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
