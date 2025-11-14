package com.example.milao.ui.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Chat(
    val id: String = "",
    val title: String = "",
    val emoji: String = "",
    val messages: List<Message> = emptyList() // Kept as List for UI compatibility
)

@IgnoreExtraProperties
data class Message(
    val id: String = "",
    val text: String = "",
    val sentByMemberName: String = "",
    val timestamp: Long = 0
)
