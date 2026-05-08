package com.example.lcb.app.weather.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.lcb.app.weather.domain.model.SavedCity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CityStore(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
) {
    val cities: Flow<List<SavedCity>> = dataStore.data.map { preferences ->
        decodeCities(preferences[Keys.CitiesJson])
    }

    val selectedCityId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[Keys.SelectedCityId]
    }

    val selectedCity: Flow<SavedCity?> = combine(cities, selectedCityId) { cityList, selectedId ->
        cityList.firstOrNull { it.id == selectedId } ?: cityList.firstOrNull()
    }

    suspend fun addCity(city: SavedCity) {
        dataStore.edit { preferences ->
            val current = decodeCities(preferences[Keys.CitiesJson])
            if (current.any { it.id == city.id }) return@edit
            val nextCity = city.copy(sortIndex = current.size)
            val next = (current + nextCity).sortedBy { it.sortIndex }
            preferences[Keys.CitiesJson] = json.encodeToString(next)
            if (preferences[Keys.SelectedCityId].isNullOrBlank()) {
                preferences[Keys.SelectedCityId] = nextCity.id
            }
        }
    }

    suspend fun upsertCity(city: SavedCity) {
        dataStore.edit { preferences ->
            val current = decodeCities(preferences[Keys.CitiesJson])
            val next = if (current.any { it.id == city.id }) {
                current.map { existing -> if (existing.id == city.id) city else existing }
            } else {
                current + city.copy(sortIndex = current.size)
            }
            preferences[Keys.CitiesJson] = json.encodeToString(normalizeSort(next))
            if (preferences[Keys.SelectedCityId].isNullOrBlank()) {
                preferences[Keys.SelectedCityId] = city.id
            }
        }
    }

    suspend fun deleteCity(cityId: String) {
        dataStore.edit { preferences ->
            val current = decodeCities(preferences[Keys.CitiesJson])
            val next = normalizeSort(current.filterNot { it.id == cityId })
            preferences[Keys.CitiesJson] = json.encodeToString(next)
            if (preferences[Keys.SelectedCityId] == cityId) {
                next.firstOrNull()?.let { preferences[Keys.SelectedCityId] = it.id }
                    ?: preferences.remove(Keys.SelectedCityId)
            }
        }
    }

    suspend fun updateSort(cityIds: List<String>) {
        dataStore.edit { preferences ->
            val current = decodeCities(preferences[Keys.CitiesJson])
            val byId = current.associateBy { it.id }
            val reordered = cityIds.mapNotNull { byId[it] }
            val missing = current.filterNot { cityIds.contains(it.id) }.sortedBy { it.sortIndex }
            preferences[Keys.CitiesJson] = json.encodeToString(normalizeSort(reordered + missing))
        }
    }

    suspend fun setSelectedCity(cityId: String) {
        dataStore.edit { preferences ->
            val current = decodeCities(preferences[Keys.CitiesJson])
            if (current.any { it.id == cityId }) {
                preferences[Keys.SelectedCityId] = cityId
            }
        }
    }

    private fun decodeCities(raw: String?): List<SavedCity> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<SavedCity>>(raw).sortedBy { it.sortIndex }
        } catch (_: IllegalArgumentException) {
            emptyList()
        } catch (_: SerializationException) {
            emptyList()
        }
    }

    private fun normalizeSort(cities: List<SavedCity>): List<SavedCity> {
        return cities
            .sortedBy { it.sortIndex }
            .mapIndexed { index, city -> city.copy(sortIndex = index) }
    }

    private object Keys {
        val CitiesJson = stringPreferencesKey("cities_json")
        val SelectedCityId = stringPreferencesKey("selected_city_id")
    }
}
