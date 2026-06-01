package com.example.lcb.app.weather.di

import android.content.Context
import com.example.lcb.app.weather.data.local.CityStore
import com.example.lcb.app.weather.data.local.SettingsStore
import com.example.lcb.app.weather.data.local.WeatherPreferences
import com.example.lcb.app.weather.data.remote.NetworkModule
import com.example.lcb.app.weather.data.repository.GeocodingRepository
import com.example.lcb.app.weather.data.repository.LocationRepository
import com.example.lcb.app.weather.data.repository.WeatherRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = WeatherPreferences.create(appContext)

    val cityStore = CityStore(dataStore)
    val settingsStore = SettingsStore(dataStore)
    val weatherRepository = WeatherRepository(NetworkModule.createForecastApi())
    val geocodingRepository = GeocodingRepository(NetworkModule.createGeocodingApi())
    val locationRepository = LocationRepository(NetworkModule.createIpGeolocationApi())
}
