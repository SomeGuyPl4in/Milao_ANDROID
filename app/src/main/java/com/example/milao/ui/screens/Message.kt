package com.example.milao.ui.screens

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class Message(
    val id: String? = null,
    val senderId: String? = null,
    @get:PropertyName("isSentByMemberName")
    val senderName: String? = null,
    val text: String? = null,
    val timestamp: Long = 0
)
