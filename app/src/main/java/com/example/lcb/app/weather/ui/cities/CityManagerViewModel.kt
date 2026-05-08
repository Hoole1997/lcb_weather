package com.example.lcb.app.weather.ui.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    val errorMessage: String? = null
)

data class CityWeatherSummary(
    val temperature: Double,
    val weatherText: String,
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
                val oldById = _uiState.value.cards.associateBy { it.city.id }
                _uiState.update {
                    it.copy(
                        settings = settings,
                        cards = cities.map { city ->
                            oldById[city.id]?.copy(city = city)
                                ?: CityCardUiState(city = city, isLoading = true)
                        }
                    )
                }
                refreshSummaries(cities, settings)
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

    fun moveCity(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.cards.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        val moved = current.removeAt(fromIndex)
        current.add(toIndex, moved)
        _uiState.update { it.copy(cards = current) }
        viewModelScope.launch {
            cityStore.updateSort(current.map { it.city.id })
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
                                errorMessage = error.message ?: "加载失败"
                            )
                        }
                    )
                }
            }.map { it.await() }.toMap()
        }

        _uiState.update { state ->
            state.copy(
                isRefreshing = false,
                cards = cities.map { city -> summaries[city.id] ?: CityCardUiState(city = city) }
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
