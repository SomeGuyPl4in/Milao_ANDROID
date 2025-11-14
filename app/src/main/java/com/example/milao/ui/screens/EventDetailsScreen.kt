package com.example.milao.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milao.ui.data.Member
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    onNavigateUp: () -> Unit,
    viewModel: EventDetailsViewModel = viewModel()
) {
    val eventState by viewModel.eventState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    when (val state = eventState) {
        is EventState.Loading -> LoadingScreen()
        is EventState.Error -> PlaceholderScreen(state.message)
        is EventState.Success -> {
            val event = state.event
            var selectedItem by remember { mutableIntStateOf(0) }
            val members by viewModel.members.collectAsState()
            val currentUserId = currentUser?.uid ?: "unknownUser"
            val currentUserName = currentUser?.displayName ?: "Unknown User"

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(event.title) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateUp) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            val isAlreadyMember = members.any { it.id == currentUserId }
                            if (currentUser != null && !isAlreadyMember) {
                                val member = Member(id = currentUserId, name = currentUserName)
                                IconButton(onClick = { viewModel.joinEvent(eventId, member) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Join Event")
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    val items = listOf("Members", "Plan", "Chats", "Settings")
                    val selectedIcons = listOf(
                        Icons.Filled.People,
                        Icons.Filled.Map,
                        Icons.AutoMirrored.Filled.Chat,
                        Icons.Filled.Settings
                    )
                    val unselectedIcons = listOf(
                        Icons.Outlined.People,
                        Icons.Outlined.Map,
                        Icons.AutoMirrored.Outlined.Chat,
                        Icons.Outlined.Settings
                    )

                    NavigationBar {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                                        contentDescription = item
                                    )
                                },
                                label = { Text(item) },
                                selected = selectedItem == index,
                                onClick = { selectedItem = index }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    when (selectedItem) {
                        0 -> MembersScreen(members)
                        1 -> PlanScreen(event)
                        2 -> {
                            ChatScreen(
                                eventId = event.id,
                                currentUserId = currentUserId,
                                currentUserName = currentUserName,
                            )
                        }
                        3 -> {
                            if (event.ownerID == currentUserId) {
                                EventSettingsScreen(event, viewModel)
                            } else {
                                PlaceholderScreen("You do not have permission to edit this event.")
                            }
                        }
                        else -> PlaceholderScreen("Unknown screen")
                    }
                }
            }
        }
    }
}

@Composable
fun MembersScreen(members: List<Member>) {
    if (members.isEmpty()) {
        PlaceholderScreen("No members yet")
    } else {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(members) {
                MemberItem(it)
            }
        }
    }
}

@Composable
fun MemberItem(member: Member) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val initial = member.name?.firstOrNull()?.toString() ?: "?"
            Text(text = initial, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = member.name ?: "Unknown Member", style = MaterialTheme.typography.bodyLarge)
                Text(text = member.locationPlaceID ?: "Unknown location", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
