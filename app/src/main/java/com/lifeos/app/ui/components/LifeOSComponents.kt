package com.lifeos.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LifeOSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(100),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Flat card with a hairline border — quieter than a drop shadow, reads as "paper". */
@Composable
fun LifeOSCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tonal: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    val colors = if (tonal) {
        CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    } else {
        CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    }
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    val elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp, pressedElevation = 1.dp, hoveredElevation = 1.dp)
    if (onClick != null) {
        OutlinedCard(modifier = modifier, shape = shape, colors = colors, border = border, elevation = elevation, onClick = onClick) {
            Column(Modifier.padding(16.dp), content = content)
        }
    } else {
        OutlinedCard(modifier = modifier, shape = shape, colors = colors, border = border, elevation = elevation) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeOSTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun LifeOSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        shape = MaterialTheme.shapes.small
    )
}

@Composable
fun LifeOSProgressBar(progress: Float, modifier: Modifier = Modifier, label: String? = null) {
    Column(modifier) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun LifeOSMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    LifeOSCard(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun LifeOSEmptyState(message: String, modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LifeOSSectionHeader(title: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        trailing()
    }
}

/** Pill-style segmented control, used for Day/Week/Month and similar toggles. */
@Composable
fun <T> LifeOSSegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clip(RoundedCornerShape(100))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(100))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
