package com.example.milao.ui.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Member(
    val id: String = "",
    val name: String? = "",
    val location: String? = null, // Added for backward compatibility
    val locationPlaceID: String? = null
)
