package com.example.lcb.app.weather.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedCity(
    val id: String,
    val name: String,
    val country: String? = null,
    val admin1: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "auto",
    val sortIndex: Int
) {
    val subtitle: String
        get() = listOfNotNull(admin1, country)
            .filter { it.isNotBlank() && it != name }
            .distinct()
            .joinToString(" · ")
}
