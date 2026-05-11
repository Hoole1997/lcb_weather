package com.example.lcb.app.weather.domain.model

import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class SavedCity(
    val id: String,
    val name: String,
    val country: String? = null,
    val countryCode: String? = null,
    val admin1: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "auto",
    val sortIndex: Int
) {
    val subtitle: String
        get() = subtitle(Locale.getDefault())

    fun subtitle(locale: Locale): String {
        return listOfNotNull(admin1, localizedCountry(locale))
            .filter { it.isNotBlank() && it != name }
            .distinct()
            .joinToString(" · ")
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
