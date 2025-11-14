package com.example.milao.ui.data

data class Place(
    val id: String = "",
    val name: String = "",
    val emoji: String = "",
    val descriptionAI: String = "",
    val location: Location? = null
)

data class Location(
    val id: String = "",
    val name: String = "",
    val coordinate: Coordinate? = null
)

data class Coordinate(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
