package com.example.milao.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milao.ui.data.Event

@Composable
fun HomeScreen(
    onEventJoined: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val events by viewModel.events.collectAsState()

    if (events.isEmpty()) {
        HomeContent(onJoinEvent = { inviteCode ->
            viewModel.joinEvent(inviteCode)
        })
    } else {
        JoinedEventsContent(
            events = events,
            onEventClicked = onEventJoined,
            onJoinEvent = { inviteCode ->
                viewModel.joinEvent(inviteCode)
            }
        )
    }
}

@Composable
fun HomeContent(onJoinEvent: (String) -> Unit) {
    var inviteCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Join an Event",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = inviteCode,
            onValueChange = { inviteCode = it },
            label = { Text("Invite code") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (inviteCode.isNotBlank()) onJoinEvent(inviteCode) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Or")
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { /* TODO: Navigate to create event screen */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create new event")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    HomeContent(onJoinEvent = {})
}

@Composable
fun JoinedEventsContent(
    events: List<Event>,
    onEventClicked: (String) -> Unit,
    onJoinEvent: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var inviteCode by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Join another event") },
            text = {
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text("Enter invite code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inviteCode.isNotBlank()) {
                            onJoinEvent(inviteCode)
                            inviteCode = ""
                            showDialog = false
                        }
                    }
                ) { Text("Join") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your Events", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            events.forEach { event ->
                Button(
                    onClick = { onEventClicked(event.id) },
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(0.8f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = event.emoji, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = event.title, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JoinedEventsContentPreview() {
    val sampleEvents = listOf(
        Event(id = "1", title = "Event 1", emoji = "🎉"),
        Event(id = "2", title = "Event 2", emoji = "🎂"),
        Event(id = "3", title = "Event 3", emoji = "🚀")
    )
    JoinedEventsContent(
        events = sampleEvents,
        onEventClicked = {},
        onJoinEvent = {}
    )
}
