package com.example.lcb.app.weather.data.repository

import com.example.lcb.app.weather.data.remote.IpGeolocationApi
import com.example.lcb.app.weather.domain.model.SavedCity

class LocationRepository(
    private val ipGeolocationApi: IpGeolocationApi
) {
    suspend fun resolveCurrentCity(): Result<SavedCity> {
        return runCatching {
            val response = ipGeolocationApi.getLocation()
            if (response.error == true) {
                error(response.reason ?: "IP geolocation failed.")
            }
            val latitude = response.latitude ?: error("IP geolocation missing latitude.")
            val longitude = response.longitude ?: error("IP geolocation missing longitude.")
            SavedCity(
                id = CURRENT_LOCATION_ID,
                name = response.city ?: response.region ?: response.countryName ?: DEFAULT_CITY.name,
                country = response.countryName,
                countryCode = response.countryCode,
                admin1 = response.region,
                latitude = latitude,
                longitude = longitude,
                timezone = response.timezone?.takeIf { it.isNotBlank() } ?: "auto",
                sortIndex = 0
            )
        }.recoverCatching { DEFAULT_CITY }
    }

    companion object {
        const val CURRENT_LOCATION_ID = "current_location"

        // Fallback city when IP geolocation fails: New York, USA.
        private val DEFAULT_CITY = SavedCity(
            id = CURRENT_LOCATION_ID,
            name = "New York",
            country = "United States",
            countryCode = "US",
            admin1 = "New York",
            latitude = 40.7128,
            longitude = -74.0060,
            timezone = "America/New_York",
            sortIndex = 0
        )
    }
}
