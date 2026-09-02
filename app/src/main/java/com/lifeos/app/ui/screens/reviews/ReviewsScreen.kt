package com.lifeos.app.ui.screens.reviews

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifeos.app.data.entity.DailyReview
import com.lifeos.app.data.entity.PeriodicReview
import com.lifeos.app.ui.common.AppViewModelFactory
import com.lifeos.app.ui.common.lifeOSApp
import com.lifeos.app.ui.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class ReviewTab { DAILY, WEEKLY, MONTHLY }

@Composable
fun ReviewsScreen() {
    val app = lifeOSApp(LocalContext.current.applicationContext as android.app.Application)
    val viewModel: ReviewsViewModel = viewModel(factory = AppViewModelFactory(app))
    var tab by remember { mutableStateOf(ReviewTab.DAILY) }

    Scaffold(topBar = { LifeOSTopBar(title = "Reviews") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab.ordinal) {
                ReviewTab.entries.forEach { t ->
                    Tab(selected = tab == t, onClick = { tab = t }, text = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }
            when (tab) {
                ReviewTab.DAILY -> DailyReviewTab(viewModel)
                ReviewTab.WEEKLY -> PeriodicReviewTab(
                    label = "This week",
                    history = viewModel.weeklyReviews.collectAsState().value,
                    onSave = viewModel::saveWeeklyReview,
                    onDelete = viewModel::deletePeriodicReview
                )
                ReviewTab.MONTHLY -> PeriodicReviewTab(
                    label = "This month",
                    history = viewModel.monthlyReviews.collectAsState().value,
                    onSave = viewModel::saveMonthlyReview,
                    onDelete = viewModel::deletePeriodicReview
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DailyReviewTab(viewModel: ReviewsViewModel) {
    val reviews by viewModel.dailyReviews.collectAsState()
    var wentWell by remember { mutableStateOf("") }
    var toImprove by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf<Int?>(null) }
    var deleteTarget by remember { mutableStateOf<DailyReview?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            LifeOSCard {
                Text("Today's review", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LifeOSTextField(value = wentWell, onValueChange = { wentWell = it }, label = "What went well?", singleLine = false, minLines = 2)
                Spacer(Modifier.height(8.dp))
                LifeOSTextField(value = toImprove, onValueChange = { toImprove = it }, label = "What to improve?", singleLine = false, minLines = 2)
                Spacer(Modifier.height(8.dp))
                Row {
                    (1..5).forEach { r ->
                        FilterChip(selected = rating == r, onClick = { rating = r }, label = { Text(r.toString()) }, modifier = Modifier.padding(end = 6.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                LifeOSButton(text = "Save", onClick = { viewModel.saveDailyReview(wentWell, toImprove, rating) })
            }
        }
        item { LifeOSSectionHeader("History — long-press to delete") }
        if (reviews.isEmpty()) {
            item { LifeOSEmptyState("No past reviews yet.", icon = androidx.compose.material.icons.Icons.Default.RateReview) }
        } else {
            items(reviews, key = { it.id }) { review ->
                LifeOSCard(
                    modifier = Modifier.combinedClickable(onClick = {}, onLongClick = { deleteTarget = review })
                ) {
                    Text(LocalDate.ofEpochDay(review.dateEpochDay).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)), style = MaterialTheme.typography.labelSmall)
                    if (review.wentWell.isNotBlank()) Text("Went well: ${review.wentWell}")
                    if (review.toImprove.isNotBlank()) Text("To improve: ${review.toImprove}")
                    if (review.rating != null) Text("Rating: ${review.rating}/5")
                }
            }
        }
    }

    deleteTarget?.let { review ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete this review?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteDailyReview(review); deleteTarget = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PeriodicReviewTab(
    label: String,
    history: List<PeriodicReview>,
    onSave: (String) -> Unit,
    onDelete: (PeriodicReview) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<PeriodicReview?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            LifeOSCard {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LifeOSTextField(value = content, onValueChange = { content = it }, label = "Reflections", singleLine = false, minLines = 4)
                Spacer(Modifier.height(12.dp))
                LifeOSButton(text = "Save", onClick = { onSave(content) })
            }
        }
        item { LifeOSSectionHeader("History — long-press to delete") }
        if (history.isEmpty()) {
            item { LifeOSEmptyState("No past reviews yet.", icon = androidx.compose.material.icons.Icons.Default.RateReview) }
        } else {
            items(history, key = { it.id }) { review ->
                LifeOSCard(
                    modifier = Modifier.combinedClickable(onClick = {}, onLongClick = { deleteTarget = review })
                ) {
                    Text(
                        "${LocalDate.ofEpochDay(review.periodStartEpochDay)} – ${LocalDate.ofEpochDay(review.periodEndEpochDay)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(review.content)
                }
            }
        }
    }

    deleteTarget?.let { review ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete this review?") },
            confirmButton = { TextButton(onClick = { onDelete(review); deleteTarget = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }
}
