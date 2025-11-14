package com.example.milao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.milao.ui.data.ColorModel
import com.example.milao.ui.data.Event
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSettingsScreen(
    event: Event,
    viewModel: EventDetailsViewModel
) {
    var eventName by remember { mutableStateOf(event.title) }
    var eventEmoji by remember { mutableStateOf(event.emoji) }
    var showColorPicker1 by remember { mutableStateOf(false) }
    var showColorPicker2 by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.parse(event.eventDate).toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = LocalDateTime.parse(event.eventDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).hour,
        initialMinute = LocalDateTime.parse(event.eventDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).minute
    )

    Column(modifier = Modifier.padding(16.dp)) {
        // Event Name
        TextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.updateEventTitle(event.id, eventName) }) {
            Text("Update Name")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Emoji
        TextField(
            value = eventEmoji,
            onValueChange = { eventEmoji = it },
            label = { Text("Emoji") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.updateEventEmoji(event.id, eventEmoji) }) {
            Text("Update Emoji")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Colors
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            ColorPickerButton("Primary Color", event.bgColor1) { showColorPicker1 = true }
            ColorPickerButton("Secondary Color", event.bgColor2) { showColorPicker2 = true }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Time
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Time: ${event.eventDate}")
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { showDatePicker = true }) {
                Text("Change Time")
            }
        }
    }

    if (showColorPicker1) {
        ColorPickerDialog(event.bgColor1, onDismiss = { showColorPicker1 = false }) {
            viewModel.updateEventColor1(event.id, it)
        }
    }

    if (showColorPicker2) {
        ColorPickerDialog(event.bgColor2, onDismiss = { showColorPicker2 = false }) {
            viewModel.updateEventColor2(event.id, it)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    val selectedDate = Instant.ofEpochMilli(datePickerState.selectedDateMillis!!).atZone(ZoneId.systemDefault()).toLocalDate()
                    val newDateTime = LocalDateTime.of(selectedDate, LocalDateTime.of(2000, 1, 1, timePickerState.hour, timePickerState.minute).toLocalTime())
                    val formattedDate = newDateTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    viewModel.updateEventDate(event.id, formattedDate)
                    showTimePicker = false 
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun ColorPickerButton(text: String, colorModel: ColorModel?, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = colorModel?.let { Color(it.red, it.green, it.blue, it.alpha) } ?: Color.Gray)
                .clickable(onClick = onClick)
        )
        Text(text = text)
    }
}

@Composable
fun ColorPickerDialog(colorModel: ColorModel?, onDismiss: () -> Unit, onColorSelected: (ColorModel) -> Unit) {
    val controller = rememberColorPickerController()
    Dialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.background(Color.White).padding(16.dp)) {
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                controller = controller
            )
            Button(onClick = {
                val color = controller.selectedColor.value
                onColorSelected(ColorModel(color.red, color.green, color.blue, color.alpha))
                onDismiss()
            }) {
                Text("Select")
            }
        }
    }
}
