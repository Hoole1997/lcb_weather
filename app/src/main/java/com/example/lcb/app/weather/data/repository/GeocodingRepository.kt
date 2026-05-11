package com.example.lcb.app.weather.data.repository

import com.example.lcb.app.weather.data.remote.OpenMeteoGeocodingApi
import com.example.lcb.app.weather.data.remote.dto.GeocodingResultDto
import com.example.lcb.app.weather.domain.model.CitySearchResult
import java.util.Locale

class GeocodingRepository(
    private val api: OpenMeteoGeocodingApi
) {
    suspend fun searchCities(keyword: String): Result<List<CitySearchResult>> {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.length < MIN_QUERY_LENGTH) {
            return Result.success(emptyList())
        }

        return runCatching {
            api.searchCities(
                name = normalizedKeyword,
                language = Locale.getDefault().language.toSupportedGeocodingLanguage()
            )
                .results
                .map { it.toCitySearchResult() }
        }
    }

    private fun GeocodingResultDto.toCitySearchResult(): CitySearchResult {
        return CitySearchResult(
            id = "openmeteo-$id",
            name = name,
            country = country,
            countryCode = countryCode,
            admin1 = admin1,
            latitude = latitude,
            longitude = longitude,
            timezone = timezone?.takeIf { it.isNotBlank() } ?: "auto"
        )
    }

    companion object {
        private const val MIN_QUERY_LENGTH = 2
        private val SupportedGeocodingLanguages = setOf("zh", "en", "ja", "ko", "fr", "de", "es", "pt", "it", "ru")

        private fun String.toSupportedGeocodingLanguage(): String {
            return takeIf { it in SupportedGeocodingLanguages } ?: "en"
        }
    }
}
