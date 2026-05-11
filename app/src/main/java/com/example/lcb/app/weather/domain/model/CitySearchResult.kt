package com.example.lcb.app.weather.domain.model

import java.util.Locale

data class CitySearchResult(
    val id: String,
    val name: String,
    val country: String?,
    val countryCode: String?,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String
) {
    val subtitle: String
        get() = subtitle(Locale.getDefault())

    fun subtitle(locale: Locale): String {
        return listOfNotNull(admin1, localizedCountry(locale))
            .filter { it.isNotBlank() && it != name }
            .distinct()
            .joinToString(" · ")
    }

    fun toSavedCity(sortIndex: Int): SavedCity {
        return SavedCity(
            id = id,
            name = name,
            country = country,
            countryCode = countryCode,
            admin1 = admin1,
            latitude = latitude,
            longitude = longitude,
            timezone = timezone,
            sortIndex = sortIndex
        )
    }

    private fun localizedCountry(locale: Locale): String? {
        val normalizedCountryCode = countryCode
            ?.trim()
            ?.takeIf { it.length == 2 }
            ?.uppercase(Locale.ROOT)
        val localized = normalizedCountryCode
            ?.let { Locale.Builder().setRegion(it).build().getDisplayCountry(locale) }
            ?.takeIf { it.isNotBlank() && it != normalizedCountryCode }
        return localized ?: country
    }
}
