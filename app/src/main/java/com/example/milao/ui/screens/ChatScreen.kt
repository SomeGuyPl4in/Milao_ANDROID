package com.example.milao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    eventId: String,
    currentUserId: String,
    currentUserName: String,
    chatViewModel: ChatViewModel = viewModel()
) {
    LaunchedEffect(eventId) {
        chatViewModel.loadMessages(eventId)
    }

    val messages by chatViewModel.messages.collectAsState()
    var newMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = true
        ) {
            items(messages.sortedByDescending { it.timestamp }) { message ->
                MessageItem(message = message, currentUserId = currentUserId)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (newMessage.isNotBlank()) {
                    val message = Message(
                        id = UUID.randomUUID().toString(),
                        senderId = currentUserId,
                        senderName = currentUserName,
                        text = newMessage,
                        timestamp = System.currentTimeMillis()
                    )
                    chatViewModel.sendMessage(eventId, message)
                    newMessage = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, currentUserId: String) {
    val isCurrentUser = message.senderId == currentUserId
    val arrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    val bubbleColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = arrangement
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                Column {
                    Text(
                        text = message.senderName ?: "Unknown User",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = message.text ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen(eventId = "sampleEventId", currentUserId = "sampleUserId", currentUserName = "sampleUserName")
}

@Preview
@Composable
fun MessageItemPreview() {
    val message = Message(
        senderName = "John Doe",
        text = "This is a sample message for preview.",
        timestamp = System.currentTimeMillis()
    )
    MessageItem(message = message, currentUserId = "sampleUserId")
}
