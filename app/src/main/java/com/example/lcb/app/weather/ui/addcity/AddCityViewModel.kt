package com.example.lcb.app.weather.ui.addcity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lcb.app.weather.data.local.CityStore
import com.example.lcb.app.weather.data.repository.GeocodingRepository
import com.example.lcb.app.weather.domain.model.CitySearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddCityUiState(
    val query: String = "",
    val results: List<CitySearchItemUiState> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false
)

data class CitySearchItemUiState(
    val city: CitySearchResult,
    val isAdded: Boolean
)

class AddCityViewModel(
    private val cityStore: CityStore,
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddCityUiState())
    val uiState: StateFlow<AddCityUiState> = _uiState

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        _uiState.update {
            it.copy(query = value, errorMessage = null)
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            search(value)
        }
    }

    fun retry() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            search(_uiState.value.query)
        }
    }

    fun addCity(item: CitySearchItemUiState, onAdded: (String) -> Unit) {
        viewModelScope.launch {
            val cities = cityStore.cities.first()
            if (cities.none { it.id == item.city.id }) {
                cityStore.addCity(item.city.toSavedCity(sortIndex = cities.size))
            }
            cityStore.setSelectedCity(item.city.id)
            onAdded(item.city.id)
        }
    }

    private suspend fun search(rawKeyword: String) {
        val keyword = rawKeyword.trim()
        if (keyword.length < MIN_QUERY_LENGTH) {
            _uiState.update {
                it.copy(
                    results = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    hasSearched = false
                )
            }
            return
        }

        _uiState.update {
            it.copy(isLoading = true, errorMessage = null, hasSearched = true)
        }

        val existingCities = cityStore.cities.first()
        geocodingRepository.searchCities(keyword)
            .onSuccess { results ->
                _uiState.update {
                    it.copy(
                        results = results.map { city ->
                            CitySearchItemUiState(
                                city = city,
                                isAdded = existingCities.any { saved -> saved.id == city.id }
                            )
                        },
                        isLoading = false,
                        errorMessage = null,
                        hasSearched = true
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        results = emptyList(),
                        isLoading = false,
                        errorMessage = error.message ?: "城市搜索失败",
                        hasSearched = true
                    )
                }
            }
    }

    class Factory(
        private val cityStore: CityStore,
        private val geocodingRepository: GeocodingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddCityViewModel(cityStore, geocodingRepository) as T
        }
    }

    companion object {
        private const val MIN_QUERY_LENGTH = 2
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
