package com.example.lcb.app.weather.data.local

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile

object WeatherPreferences {
    const val DATA_STORE_NAME = "weather_preferences"

    fun create(context: Context) = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(DATA_STORE_NAME) }
    )
}
