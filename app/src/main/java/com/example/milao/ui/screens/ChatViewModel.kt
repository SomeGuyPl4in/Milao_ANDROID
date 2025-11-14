package com.example.milao.ui.screens

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {

    private val database = Firebase.database
    // The path to messages is now inside a specific event and chat (assuming the first chat, index 0)
    private fun getMessagesRef(eventId: String) = 
        database.getReference("events").child(eventId).child("chats").child("0").child("messages")

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private var messagesListener: ValueEventListener? = null
    private var currentEventId: String? = null

    fun loadMessages(eventId: String) {
        if (currentEventId == eventId && messagesListener != null) {
            return
        }
        
        currentEventId?.let {
            messagesListener?.let { listener ->
                getMessagesRef(it).removeEventListener(listener)
            }
        }

        currentEventId = eventId
        messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = snapshot.children.mapNotNull { dataSnapshot ->
                    dataSnapshot.getValue(Message::class.java)
                }
                _messages.value = messagesList.sortedBy { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        }
        
        getMessagesRef(eventId).addValueEventListener(messagesListener!!)
    }

    fun sendMessage(eventId: String, message: Message) {
        val messagesRef = getMessagesRef(eventId)
        messagesRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val nextIndex = currentData.childrenCount
                currentData.child(nextIndex.toString()).value = message
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // Transaction completed.
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        currentEventId?.let { eventId ->
            messagesListener?.let { listener ->
                getMessagesRef(eventId).removeEventListener(listener)
            }
        }
    }
}
