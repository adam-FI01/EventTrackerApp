package com.example.adamfineeventtrackerapp.ui.events

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.adamfineeventtrackerapp.data.Event

// Add or edit an event (initialEvent is null when adding)
@Composable
fun EventDialog(
    title: String,
    confirmLabel: String,
    initialEvent: Event?,
    onConfirm: (name: String, date: String, time: String, location: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialEvent?.eventName.orEmpty()) }
    var date by remember { mutableStateOf(initialEvent?.eventDate.orEmpty()) }
    var time by remember { mutableStateOf(initialEvent?.eventTime.orEmpty()) }
    var location by remember { mutableStateOf(initialEvent?.eventLocation.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("What is it?") })
                TextField(value = date, onValueChange = { date = it }, label = { Text("When (Ex: 10/27/2025)") })
                TextField(value = time, onValueChange = { time = it }, label = { Text("Time (Ex: 14:30)") })
                TextField(value = location, onValueChange = { location = it }, label = { Text("Where is it? (optional)") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, date, time, location) }) { Text(confirmLabel) }
        }
    )
}
