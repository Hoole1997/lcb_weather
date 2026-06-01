package com.example.lcb.app.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IpLocationDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: String? = null,
    val region: String? = null,
    @SerialName("country_name") val countryName: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val timezone: String? = null,
    val error: Boolean? = null,
    val reason: String? = null
)
