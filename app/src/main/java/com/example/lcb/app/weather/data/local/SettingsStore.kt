package com.example.lcb.app.weather.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.lcb.app.weather.domain.model.LanguageOption
import com.example.lcb.app.weather.domain.model.PressureUnit
import com.example.lcb.app.weather.domain.model.TemperatureUnit
import com.example.lcb.app.weather.domain.model.VisibilityUnit
import com.example.lcb.app.weather.domain.model.WeatherSettings
import com.example.lcb.app.weather.domain.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsStore(
    private val dataStore: DataStore<Preferences>
) {
    val settings: Flow<WeatherSettings> = dataStore.data.map { preferences ->
        WeatherSettings(
            temperatureUnit = enumValue(preferences[Keys.TemperatureUnit], TemperatureUnit.Celsius),
            windSpeedUnit = enumValue(preferences[Keys.WindSpeedUnit], WindSpeedUnit.KilometersPerHour),
            pressureUnit = enumValue(preferences[Keys.PressureUnit], PressureUnit.Hectopascal),
            visibilityUnit = enumValue(preferences[Keys.VisibilityUnit], VisibilityUnit.Kilometer),
            languageOption = enumValue(preferences[Keys.LanguageOption], LanguageOption.System)
        )
    }

    suspend fun setTemperatureUnit(value: TemperatureUnit) {
        set(Keys.TemperatureUnit, value.name)
    }

    suspend fun setWindSpeedUnit(value: WindSpeedUnit) {
        set(Keys.WindSpeedUnit, value.name)
    }

    suspend fun setPressureUnit(value: PressureUnit) {
        set(Keys.PressureUnit, value.name)
    }

    suspend fun setVisibilityUnit(value: VisibilityUnit) {
        set(Keys.VisibilityUnit, value.name)
    }

    suspend fun setLanguageOption(value: LanguageOption) {
        set(Keys.LanguageOption, value.name)
    }

    private suspend fun set(key: Preferences.Key<String>, value: String) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    private inline fun <reified T : Enum<T>> enumValue(raw: String?, fallback: T): T {
        if (raw.isNullOrBlank()) return fallback
        return runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)
    }

    private object Keys {
        val TemperatureUnit = stringPreferencesKey("temperature_unit")
        val WindSpeedUnit = stringPreferencesKey("wind_speed_unit")
        val PressureUnit = stringPreferencesKey("pressure_unit")
        val VisibilityUnit = stringPreferencesKey("visibility_unit")
        val LanguageOption = stringPreferencesKey("language_option")
    }
}
