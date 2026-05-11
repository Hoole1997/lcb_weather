package com.example.lcb.app.weather.ui.main

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lcb.app.R
import com.example.lcb.app.weather.data.local.CityStore
import com.example.lcb.app.weather.data.local.SettingsStore
import com.example.lcb.app.weather.data.repository.WeatherRepository
import com.example.lcb.app.weather.domain.model.SavedCity
import com.example.lcb.app.weather.domain.model.WeatherReport
import com.example.lcb.app.weather.domain.model.WeatherSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainWeatherUiState(
    val city: SavedCity? = null,
    val settings: WeatherSettings = WeatherSettings(),
    val report: WeatherReport? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    @param:StringRes val errorMessageRes: Int? = null,
    val hasNoCity: Boolean = false
)

class MainWeatherViewModel(
    private val cityStore: CityStore,
    private val settingsStore: SettingsStore,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainWeatherUiState())
    val uiState: StateFlow<MainWeatherUiState> = _uiState

    private var loadJob: Job? = null
    private var currentCityId: String? = null

    fun start(cityId: String?) {
        if (currentCityId == cityId && loadJob != null) return
        currentCityId = cityId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val cities = cityStore.cities.first()
            val selectedCity = cityStore.selectedCity.first()
            val city = cityId
                ?.let { id -> cities.firstOrNull { it.id == id } }
                ?: selectedCity
                ?: cities.firstOrNull()

            if (city == null) {
                _uiState.value = MainWeatherUiState(
                    isLoading = false,
                    hasNoCity = true,
                    errorMessageRes = R.string.no_city
                )
                return@launch
            }

            cityStore.setSelectedCity(city.id)
            settingsStore.settings.collectLatest { settings ->
                fetch(city, settings)
            }
        }
    }

    fun retry() {
        val state = _uiState.value
        val city = state.city ?: return
        viewModelScope.launch {
            fetch(city, state.settings)
        }
    }

    fun refresh() {
        val state = _uiState.value
        val city = state.city ?: return
        if (state.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null, errorMessageRes = null) }
            weatherRepository.getWeather(city, state.settings)
                .onSuccess { report ->
                    _uiState.update {
                        it.copy(
                            report = report,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                            errorMessageRes = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = error.message,
                            errorMessageRes = if (error.message == null) R.string.weather_load_failed else null
                        )
                    }
                }
        }
    }

    private suspend fun fetch(city: SavedCity, settings: WeatherSettings) {
        _uiState.update {
            it.copy(
                city = city,
                settings = settings,
                isLoading = true,
                errorMessage = null,
                errorMessageRes = null,
                hasNoCity = false
            )
        }
        val result = weatherRepository.getWeather(city, settings)
        result
            .onSuccess { report ->
                _uiState.value = MainWeatherUiState(
                    city = city,
                    settings = settings,
                    report = report,
                    isLoading = false
                )
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        city = city,
                        settings = settings,
                        isLoading = false,
                        errorMessage = error.message,
                        errorMessageRes = if (error.message == null) R.string.weather_load_failed else null
                    )
                }
            }
    }

    class Factory(
        private val cityStore: CityStore,
        private val settingsStore: SettingsStore,
        private val weatherRepository: WeatherRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainWeatherViewModel(cityStore, settingsStore, weatherRepository) as T
        }
    }
}
