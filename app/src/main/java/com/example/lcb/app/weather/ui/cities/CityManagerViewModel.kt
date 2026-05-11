package com.example.lcb.app.weather.ui.cities

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lcb.app.R
import com.example.lcb.app.weather.data.local.CityStore
import com.example.lcb.app.weather.data.local.SettingsStore
import com.example.lcb.app.weather.data.repository.WeatherRepository
import com.example.lcb.app.weather.domain.model.SavedCity
import com.example.lcb.app.weather.domain.model.WeatherIcon
import com.example.lcb.app.weather.domain.model.WeatherSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class CityManagerUiState(
    val query: String = "",
    val cards: List<CityCardUiState> = emptyList(),
    val settings: WeatherSettings = WeatherSettings(),
    val isRefreshing: Boolean = false
) {
    val filteredCards: List<CityCardUiState>
        get() {
            val normalized = query.trim()
            if (normalized.isBlank()) return cards
            return cards.filter { card ->
                card.city.name.contains(normalized, ignoreCase = true) ||
                    card.city.subtitle.contains(normalized, ignoreCase = true)
            }
        }
}

data class CityCardUiState(
    val city: SavedCity,
    val summary: CityWeatherSummary? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    @param:StringRes val errorMessageRes: Int? = null
)

data class CityWeatherSummary(
    val temperature: Double,
    val weatherText: String,
    val weatherCode: Int,
    val icon: WeatherIcon,
    val highTemperature: Double?,
    val lowTemperature: Double?
)

class CityManagerViewModel(
    private val cityStore: CityStore,
    private val settingsStore: SettingsStore,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CityManagerUiState())
    val uiState: StateFlow<CityManagerUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(cityStore.cities, settingsStore.settings) { cities, settings ->
                cities to settings
            }.collect { (cities, settings) ->
                val previous = _uiState.value
                val oldById = previous.cards.associateBy { it.city.id }
                val newCards = cities.map { city ->
                    oldById[city.id]?.copy(city = city)
                        ?: CityCardUiState(city = city, isLoading = true)
                }

                val previousIds = previous.cards.map { it.city.id }
                val newIds = cities.map { it.id }
                val sameSet = previousIds.toSet() == newIds.toSet()
                val settingsChanged = previous.settings != settings
                val needsRefresh = !sameSet || settingsChanged ||
                    previous.cards.any { it.summary == null && it.errorMessage == null }

                _uiState.update { it.copy(settings = settings, cards = newCards) }

                if (needsRefresh) {
                    refreshSummaries(cities, settings)
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun deleteCity(cityId: String) {
        viewModelScope.launch {
            cityStore.deleteCity(cityId)
        }
    }

    /**
     * Called continuously while the user drags an item. Updates the
     * in-memory order only — we do NOT persist on every pixel, otherwise
     * the cityStore flow would re-emit mid-drag and cause the list to
     * shake when ViewModel rebuilds the cards list.
     */
    fun moveCity(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.cards.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        val moved = current.removeAt(fromIndex)
        current.add(toIndex, moved)
        _uiState.update { it.copy(cards = current) }
    }

    /**
     * Called once when the drag ends. Persists the current order to disk.
     */
    fun commitOrder() {
        val ids = _uiState.value.cards.map { it.city.id }
        viewModelScope.launch {
            cityStore.updateSort(ids)
        }
    }

    private suspend fun refreshSummaries(cities: List<SavedCity>, settings: WeatherSettings) {
        if (cities.isEmpty()) {
            _uiState.update { it.copy(cards = emptyList(), isRefreshing = false) }
            return
        }

        _uiState.update { state ->
            state.copy(
                isRefreshing = true,
                cards = state.cards.map { it.copy(isLoading = true, errorMessage = null) }
            )
        }

        val summaries = supervisorScope {
            cities.map { city ->
                async {
                    val result = weatherRepository.getWeather(city, settings)
                    city.id to result.fold(
                        onSuccess = { report ->
                            CityCardUiState(
                                city = city,
                                summary = CityWeatherSummary(
                                    temperature = report.current.temperature,
                                    weatherText = report.current.weatherText,
                                    weatherCode = report.current.weatherCode,
                                    icon = report.current.icon,
                                    highTemperature = report.current.highTemperature,
                                    lowTemperature = report.current.lowTemperature
                                )
                            )
                        },
                        onFailure = { error ->
                            CityCardUiState(
                                city = city,
                                isLoading = false,
                                errorMessage = error.message,
                                errorMessageRes = if (error.message == null) {
                                    R.string.city_weather_load_failed
                                } else {
                                    null
                                }
                            )
                        }
                    )
                }
            }.map { it.await() }.toMap()
        }

        _uiState.update { state ->
            val latestById = state.cards.associateBy { it.city.id }
            state.copy(
                isRefreshing = false,
                cards = state.cards.map { card ->
                    summaries[card.city.id]
                        ?: latestById[card.city.id]
                        ?: CityCardUiState(city = card.city)
                }
            )
        }
    }

    class Factory(
        private val cityStore: CityStore,
        private val settingsStore: SettingsStore,
        private val weatherRepository: WeatherRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CityManagerViewModel(cityStore, settingsStore, weatherRepository) as T
        }
    }
}
