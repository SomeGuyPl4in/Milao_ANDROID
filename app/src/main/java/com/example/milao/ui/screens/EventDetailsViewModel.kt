package com.example.milao.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milao.ui.data.ColorModel
import com.example.milao.ui.data.Event
import com.example.milao.ui.data.Member
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class EventState {
    object Loading : EventState()
    data class Success(val event: Event) : EventState()
    data class Error(val message: String) : EventState()
}

open class EventDetailsViewModel : ViewModel() {

    private val _eventState = MutableStateFlow<EventState>(EventState.Loading)
    open val eventState: StateFlow<EventState> = _eventState

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    open val members: StateFlow<List<Member>> = _members

    private val _eventJoined = MutableSharedFlow<String>()
    open val eventJoined = _eventJoined.asSharedFlow()

    private val database = FirebaseDatabase.getInstance().reference

    /**
     * Loads event details from Firebase Realtime Database.
     * Also fetches valid member data.
     */
    open fun loadEvent(eventId: String) {
        _eventState.value = EventState.Loading

        viewModelScope.launch {
            val eventRef = database.child("events").child(eventId)

            eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val event = snapshot.getValue(Event::class.java)
                    if (event != null) {
                        _eventState.value = EventState.Success(event)

                        // Fetch members from /users if needed, or directly from the event
                        val membersList = mutableListOf<Member>()
                        val membersSnapshot = snapshot.child("members")

                        for (memberSnap in membersSnapshot.children) {
                            val member = memberSnap.getValue(Member::class.java)
                            if (member != null) {
                                membersList.add(member)
                            }
                        }

                        _members.value = membersList
                    } else {
                        _eventState.value = EventState.Error("Event not found in database.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _eventState.value = EventState.Error("Failed to load event: ${error.message}")
                }
            })
        }
    }

    /**
     * Allows a user to join an event. Checks if user already exists in member list.
     */
    open fun joinEvent(eventId: String, currentUser: Member) {
        viewModelScope.launch {
            val eventRef = database.child("events").child(eventId)
            eventRef.get().addOnSuccessListener { snapshot ->
                val event = snapshot.getValue(Event::class.java)
                if (event != null) {
                    val updatedMembers = event.members?.toMutableList() ?: mutableListOf()

                    val alreadyJoined = updatedMembers.any { it.id == currentUser.id }
                    if (!alreadyJoined) {
                        updatedMembers.add(currentUser)
                        eventRef.child("members").setValue(updatedMembers)
                            .addOnSuccessListener {
                                viewModelScope.launch {
                                    _eventJoined.emit(eventId)
                                }
                            }
                            .addOnFailureListener {
                                _eventState.value = EventState.Error("Failed to join event: ${it.message}")
                            }
                    } else {
                        _eventState.value = EventState.Error("User already joined this event.")
                    }
                } else {
                    _eventState.value = EventState.Error("Event not found while joining.")
                }
            }.addOnFailureListener {
                _eventState.value = EventState.Error("Error accessing event: ${it.message}")
            }
        }
    }

    /** Update methods for event fields **/
    open fun updateEventTitle(eventId: String, newTitle: String) {
        database.child("events").child(eventId).child("title").setValue(newTitle)
    }

    open fun updateEventEmoji(eventId: String, newEmoji: String) {
        database.child("events").child(eventId).child("emoji").setValue(newEmoji)
    }

    open fun updateEventColor1(eventId: String, newColor: ColorModel) {
        database.child("events").child(eventId).child("bgColor1").setValue(newColor)
    }

    open fun updateEventColor2(eventId: String, newColor: ColorModel) {
        database.child("events").child(eventId).child("bgColor2").setValue(newColor)
    }

    open fun updateEventDate(eventId: String, newDate: String) {
        database.child("events").child(eventId).child("eventDate").setValue(newDate)
    }
}
