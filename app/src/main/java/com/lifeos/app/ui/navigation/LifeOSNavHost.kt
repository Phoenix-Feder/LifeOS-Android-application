package com.lifeos.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lifeos.app.ui.screens.analytics.AnalyticsScreen
import com.lifeos.app.ui.screens.calendar.CalendarScreen
import com.lifeos.app.ui.screens.journal.JournalScreen
import com.lifeos.app.ui.screens.objectives.ObjectivesScreen
import com.lifeos.app.ui.screens.reviews.ReviewsScreen
import com.lifeos.app.ui.screens.settings.SettingsScreen
import com.lifeos.app.ui.screens.today.TodayScreen

private enum class TopLevelDestination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TODAY("today", "Today", Icons.Default.CheckCircle),
    CALENDAR("calendar", "Calendar", Icons.Default.DateRange),
    OBJECTIVES("objectives", "Objectives", Icons.Default.Flag),
    JOURNAL("journal", "Journal", Icons.Default.Edit),
    REVIEWS("reviews", "Reviews", Icons.Default.RateReview),
    ANALYTICS("analytics", "Analytics", Icons.Default.BarChart),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun LifeOSNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                TopLevelDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.TODAY.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(TopLevelDestination.TODAY.route) { TodayScreen() }
            composable(TopLevelDestination.CALENDAR.route) { CalendarScreen(navController) }
            composable(TopLevelDestination.OBJECTIVES.route) { ObjectivesScreen() }
            composable(TopLevelDestination.JOURNAL.route) { JournalScreen() }
            composable(TopLevelDestination.REVIEWS.route) { ReviewsScreen() }
            composable(TopLevelDestination.ANALYTICS.route) { AnalyticsScreen() }
            composable(TopLevelDestination.SETTINGS.route) { SettingsScreen() }
        }
    }
}
