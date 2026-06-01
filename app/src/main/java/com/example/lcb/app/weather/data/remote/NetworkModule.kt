package com.example.lcb.app.weather.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkModule {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val contentType = "application/json".toMediaType()

    fun createForecastApi(): OpenMeteoForecastApi {
        return retrofit("https://api.open-meteo.com/").create(OpenMeteoForecastApi::class.java)
    }

    fun createGeocodingApi(): OpenMeteoGeocodingApi {
        return retrofit("https://geocoding-api.open-meteo.com/").create(OpenMeteoGeocodingApi::class.java)
    }

    fun createIpGeolocationApi(): IpGeolocationApi {
        return retrofit("https://ipapi.co/").create(IpGeolocationApi::class.java)
    }

    private fun retrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
