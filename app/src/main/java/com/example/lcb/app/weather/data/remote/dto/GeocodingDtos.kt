package com.example.lcb.app.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponseDto(
    val results: List<GeocodingResultDto> = emptyList(),
    @SerialName("generationtime_ms") val generationTimeMs: Double? = null
)

@Serializable
data class GeocodingResultDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    @SerialName("feature_code") val featureCode: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    @SerialName("admin1_id") val admin1Id: Long? = null,
    @SerialName("admin2_id") val admin2Id: Long? = null,
    @SerialName("admin3_id") val admin3Id: Long? = null,
    @SerialName("admin4_id") val admin4Id: Long? = null,
    val timezone: String? = null,
    val population: Long? = null,
    val country: String? = null,
    val admin1: String? = null,
    val admin2: String? = null,
    val admin3: String? = null,
    val admin4: String? = null
)
