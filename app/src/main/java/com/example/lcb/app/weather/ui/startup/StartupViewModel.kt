package com.example.lcb.app.weather.ui.startup

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lcb.app.R
import com.example.lcb.app.weather.data.local.CityStore
import com.example.lcb.app.weather.data.local.SettingsStore
import com.example.lcb.app.weather.data.repository.LocationRepository
import com.example.lcb.app.weather.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StartupUiState(
    val isLoading: Boolean = true,
    val needsCitySelection: Boolean = false,
    val activeCityId: String? = null,
    @param:StringRes val messageRes: Int = R.string.startup_preparing_weather,
    @param:StringRes val errorMessageRes: Int? = null,
    val errorMessage: String? = null
)

class StartupViewModel(
    private val cityStore: CityStore,
    private val settingsStore: SettingsStore,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StartupUiState())
    val uiState: StateFlow<StartupUiState> = _uiState

    private var started = false

    fun start() {
        if (started) return
        started = true
        viewModelScope.launch {
            val cities = cityStore.cities.first()
            val selected = cityStore.selectedCityId.first()
            if (cities.isNotEmpty()) {
                _uiState.value = StartupUiState(
                    isLoading = false,
                    activeCityId = selected ?: cities.first().id,
                    messageRes = R.string.startup_cities_loaded
                )
                return@launch
            }

            loadCurrentLocation()
        }
    }

    fun retryLocation() {
        loadCurrentLocation()
    }

    fun chooseCityManually() {
        _uiState.update {
            it.copy(
                isLoading = false,
                needsCitySelection = true,
                messageRes = R.string.startup_search_add_city,
                errorMessageRes = null,
                errorMessage = null
            )
        }
    }

    private fun loadCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = StartupUiState(
                isLoading = true,
                messageRes = R.string.startup_loading_current_weather
            )
            val city = locationRepository.resolveCurrentCity().getOrElse { error ->
                _uiState.value = StartupUiState(
                    isLoading = false,
                    needsCitySelection = true,
                    messageRes = R.string.startup_manual_city_available,
                    errorMessage = error.message,
                    errorMessageRes = if (error.message == null) R.string.location_unavailable else null
                )
                return@launch
            }

            val settings = settingsStore.settings.first()
            val weather = weatherRepository.getWeather(city, settings)
            if (weather.isFailure) {
                _uiState.value = StartupUiState(
                    isLoading = false,
                    needsCitySelection = true,
                    messageRes = R.string.startup_manual_city_available,
                    errorMessage = weather.exceptionOrNull()?.message,
                    errorMessageRes = if (weather.exceptionOrNull()?.message == null) {
                        R.string.current_weather_load_failed
                    } else {
                        null
                    }
                )
                return@launch
            }

            cityStore.upsertCity(city)
            cityStore.setSelectedCity(city.id)
            _uiState.value = StartupUiState(
                isLoading = false,
                activeCityId = city.id,
                messageRes = R.string.startup_current_location_loaded
            )
        }
    }

    class Factory(
        private val cityStore: CityStore,
        private val settingsStore: SettingsStore,
        private val locationRepository: LocationRepository,
        private val weatherRepository: WeatherRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StartupViewModel(
                cityStore = cityStore,
                settingsStore = settingsStore,
                locationRepository = locationRepository,
                weatherRepository = weatherRepository
            ) as T
        }
    }
}
