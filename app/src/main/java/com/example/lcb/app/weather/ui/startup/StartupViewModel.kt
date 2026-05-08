package com.example.lcb.app.weather.ui.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    val needsLocationPermission: Boolean = false,
    val needsCitySelection: Boolean = false,
    val activeCityId: String? = null,
    val message: String = "正在准备天气数据",
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
                    message = "已加载城市"
                )
                return@launch
            }

            if (locationRepository.hasLocationPermission()) {
                loadCurrentLocation()
            } else {
                _uiState.value = StartupUiState(
                    isLoading = false,
                    needsLocationPermission = true,
                    message = "需要定位权限获取当前位置天气"
                )
            }
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) {
            loadCurrentLocation()
        } else {
            _uiState.value = StartupUiState(
                isLoading = false,
                needsCitySelection = true,
                message = "可以手动搜索城市",
                errorMessage = "未获得定位权限"
            )
        }
    }

    fun retryLocation() {
        if (locationRepository.hasLocationPermission()) {
            loadCurrentLocation()
        } else {
            _uiState.value = StartupUiState(
                isLoading = false,
                needsLocationPermission = true,
                message = "需要定位权限获取当前位置天气"
            )
        }
    }

    fun chooseCityManually() {
        _uiState.update {
            it.copy(
                isLoading = false,
                needsLocationPermission = false,
                needsCitySelection = true,
                message = "搜索并添加城市",
                errorMessage = null
            )
        }
    }

    private fun loadCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = StartupUiState(
                isLoading = true,
                message = "正在获取当前位置天气"
            )
            val city = locationRepository.resolveCurrentCity().getOrElse { error ->
                _uiState.value = StartupUiState(
                    isLoading = false,
                    needsCitySelection = true,
                    message = "可以手动搜索城市",
                    errorMessage = error.message ?: "无法获取当前位置"
                )
                return@launch
            }

            val settings = settingsStore.settings.first()
            val weather = weatherRepository.getWeather(city, settings)
            if (weather.isFailure) {
                _uiState.value = StartupUiState(
                    isLoading = false,
                    needsCitySelection = true,
                    message = "可以手动搜索城市",
                    errorMessage = weather.exceptionOrNull()?.message ?: "当前位置天气获取失败"
                )
                return@launch
            }

            cityStore.upsertCity(city)
            cityStore.setSelectedCity(city.id)
            _uiState.value = StartupUiState(
                isLoading = false,
                activeCityId = city.id,
                message = "已加载当前位置"
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
