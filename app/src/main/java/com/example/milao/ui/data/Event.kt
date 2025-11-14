package com.example.milao.ui.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Event(
    val id: String = "",
    val title: String = "",
    val emoji: String = "",
    val eventDate: String = "",
    val inviteCode: String = "",
    val ownerID: String = "",
    val bgColor1: ColorModel? = null,
    val bgColor2: ColorModel? = null,
    val members: List<Member> = emptyList(),
    val places: List<Place> = emptyList(),
    val chats: List<Chat> = emptyList()
)

@IgnoreExtraProperties
data class ColorModel(
    val red: Float = 0.0f,
    val green: Float = 0.0f,
    val blue: Float = 0.0f,
    val alpha: Float = 1.0f
)
