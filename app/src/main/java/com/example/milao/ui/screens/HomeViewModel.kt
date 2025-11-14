package com.example.milao.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.milao.ui.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FirebaseDatabase.getInstance().getReference("events")
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _chats = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val chats: StateFlow<Map<String, List<Message>>> = _chats

    private var currentMember: Member? = null

    init {
        viewModelScope.launch {
            loadCurrentUser()
            observeEvents()
        }
    }

    private suspend fun loadCurrentUser() {
        auth.currentUser?.let { user ->
            try {
                val document = firestore.collection("users").document(user.uid).get().await()
                if (document.exists()) {
                    currentMember = Member(
                        id = user.uid,
                        name = user.displayName ?: "Unknown",
                        locationPlaceID = document.getString("locationId")
                    )
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun saveEvent(event: Event) {
        viewModelScope.launch {
            var eventToSave = event.copy()

            if (eventToSave.places.isEmpty()) {
                eventToSave = eventToSave.copy(
                    places = listOf(
                        Place(
                            id = "placeholder",
                            name = "placeholder",
                            descriptionAI = "placeholder",
                            emoji = "📍",
                            location = Location(
                                name = "placeholder",
                                coordinate = Coordinate(latitude = 0.0, longitude = 0.0)
                            )
                        )
                    )
                )
            }
            if (eventToSave.members.isEmpty()) {
                eventToSave = eventToSave.copy(
                    members = listOf(
                        Member(id = "placeholder", name = "placeholder", locationPlaceID = "placeholder")
                    )
                )
            }
            if (eventToSave.chats.isEmpty()) {
                eventToSave = eventToSave.copy(
                    chats = listOf(
                        Chat(
                            title = "General",
                            emoji = "💬",
                            messages = listOf(
                                Message(
                                    text = "Welcome to the general chat.",
                                    sentByMemberName = "Admin",
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        )
                    )
                )
            }

            database.child(eventToSave.id).setValue(eventToSave)
        }
    }

    fun joinEvent(inviteCode: String) {
        if (currentMember == null) return

        database.orderByChild("inviteCode").equalTo(inviteCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (eventSnapshot in snapshot.children) {
                            val event = eventSnapshot.getValue(Event::class.java)
                            if (event != null) {
                                val updatedMembers = event.members.toMutableList()
                                if (updatedMembers.none { it.id == currentMember!!.id }) {
                                    updatedMembers.add(currentMember!!)
                                    eventSnapshot.ref.child("members").setValue(updatedMembers)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun observeEvents() {
        auth.currentUser?.uid?.let { userId ->
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newEvents = mutableListOf<Event>()
                    snapshot.children.forEach { child ->
                        val event = child.getValue(Event::class.java)
                        if (event != null) {
                            var modifiedEvent = event.copy(id = child.key ?: event.id)

                            val isOwnedByUser = modifiedEvent.ownerID == userId
                            val isUserInMembers = modifiedEvent.members.any { it.id == userId }

                            if (isOwnedByUser || isUserInMembers) {
                                modifiedEvent = modifiedEvent.copy(
                                    members = modifiedEvent.members.filter { it.id != "placeholder" },
                                    places = modifiedEvent.places.filter { it.id != "placeholder" }
                                )
                                newEvents.add(modifiedEvent)

                                // Observe chats for this event
                                modifiedEvent.chats.forEach { chat ->
                                    observeChatMessages(modifiedEvent.id, chat)
                                }
                            }
                        }
                    }
                    _events.value = newEvents
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun observeChatMessages(eventId: String, chat: Chat) {
        val chatRef = database.child(eventId).child("chats").child(chat.title).child("messages")
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<Message>()
                snapshot.children.forEach { child ->
                    val message = child.getValue(Message::class.java)
                    message?.let { messagesList.add(it) }
                }
                _chats.value = _chats.value.toMutableMap().apply {
                    put("${eventId}_${chat.title}", messagesList.sortedBy { it.timestamp })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun sendMessage(eventId: String, chatTitle: String, text: String) {
        val userName = currentMember?.name ?: return
        val chatRef = database.child(eventId).child("chats").child(chatTitle).child("messages")
        val messageId = chatRef.push().key ?: return
        val message = Message(
            id = messageId,
            text = text,
            sentByMemberName = userName,
            timestamp = System.currentTimeMillis()
        )
        chatRef.child(messageId).setValue(message)
    }
}
