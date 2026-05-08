package com.example.lcb.app.weather.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lcb.app.weather.data.local.SettingsStore
import com.example.lcb.app.weather.domain.model.PressureUnit
import com.example.lcb.app.weather.domain.model.TemperatureUnit
import com.example.lcb.app.weather.domain.model.ThemeMode
import com.example.lcb.app.weather.domain.model.VisibilityUnit
import com.example.lcb.app.weather.domain.model.WeatherSettings
import com.example.lcb.app.weather.domain.model.WindSpeedUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsStore: SettingsStore
) : ViewModel() {
    val settings: StateFlow<WeatherSettings> = settingsStore.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeatherSettings()
    )

    fun setTemperatureUnit(value: TemperatureUnit) = launch {
        settingsStore.setTemperatureUnit(value)
    }

    fun setWindSpeedUnit(value: WindSpeedUnit) = launch {
        settingsStore.setWindSpeedUnit(value)
    }

    fun setPressureUnit(value: PressureUnit) = launch {
        settingsStore.setPressureUnit(value)
    }

    fun setVisibilityUnit(value: VisibilityUnit) = launch {
        settingsStore.setVisibilityUnit(value)
    }

    fun setThemeMode(value: ThemeMode) = launch {
        settingsStore.setThemeMode(value)
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    class Factory(
        private val settingsStore: SettingsStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsStore) as T
        }
    }
}
