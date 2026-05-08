package com.example.lcb.app.weather.domain.model

data class CitySearchResult(
    val id: String,
    val name: String,
    val country: String?,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String
) {
    val subtitle: String
        get() = listOfNotNull(admin1, country)
            .filter { it.isNotBlank() && it != name }
            .distinct()
            .joinToString(" · ")

    fun toSavedCity(sortIndex: Int): SavedCity {
        return SavedCity(
            id = id,
            name = name,
            country = country,
            admin1 = admin1,
            latitude = latitude,
            longitude = longitude,
            timezone = timezone,
            sortIndex = sortIndex
        )
    }
}
