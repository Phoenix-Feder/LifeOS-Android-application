package com.lifeos.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.lifeos.app.data.entity.Frequency
import com.lifeos.app.ui.components.LifeOSButton
import com.lifeos.app.ui.components.LifeOSTextField

data class ObjectiveFormResult(val title: String, val description: String, val frequency: Frequency, val customDaysMask: Int)

private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun AddEditObjectiveDialog(
    initialTitle: String = "",
    initialDescription: String = "",
    initialFrequency: Frequency = Frequency.DAILY,
    initialCustomMask: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (ObjectiveFormResult) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var frequency by remember { mutableStateOf(initialFrequency) }
    var customMask by remember { mutableStateOf(initialCustomMask) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(20.dp)) {
                Text("Objective", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                LifeOSTextField(value = title, onValueChange = { title = it }, label = "Title")
                Spacer(Modifier.height(8.dp))
                LifeOSTextField(value = description, onValueChange = { description = it }, label = "Description (optional)", singleLine = false, minLines = 2)
                Spacer(Modifier.height(12.dp))
                Text("Frequency", style = MaterialTheme.typography.labelSmall)
                Row {
                    Frequency.entries.forEach { f ->
                        FilterChip(
                            selected = frequency == f,
                            onClick = { frequency = f },
                            label = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                if (frequency == Frequency.CUSTOM_DAYS) {
                    Spacer(Modifier.height(8.dp))
                    Row {
                        dayLabels.forEachIndexed { index, label ->
                            val bit = 1 shl index
                            FilterChip(
                                selected = (customMask and bit) != 0,
                                onClick = { customMask = customMask xor bit },
                                label = { Text(label) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    LifeOSButton(
                        text = "Save",
                        enabled = title.isNotBlank(),
                        onClick = { onConfirm(ObjectiveFormResult(title.trim(), description.trim(), frequency, customMask)) }
                    )
                }
            }
        }
    }
}
