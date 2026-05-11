package com.example.lcb.app.weather.ui.addcity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lcb.app.R
import com.example.lcb.app.weather.data.local.CityStore
import com.example.lcb.app.weather.data.repository.GeocodingRepository
import com.example.lcb.app.weather.ui.theme.GlassCard
import com.example.lcb.app.weather.ui.theme.GlassIconButton
import com.example.lcb.app.weather.ui.theme.GlassOnSurface
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceFaint
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceMuted
import com.example.lcb.app.weather.ui.theme.SkyPalette
import com.example.lcb.app.weather.ui.theme.StaticSkyBackground

private val AddCitySky = SkyPalette(
    gradient = listOf(Color(0xFF101A2C), Color(0xFF1B3454), Color(0xFF2A547F)),
    glow = Color(0x44A2C7FF),
    glowX = 220f,
    glowY = 200f,
    glowRadius = 700f
)

@Composable
fun AddCityRoute(
    cityStore: CityStore,
    geocodingRepository: GeocodingRepository,
    onBack: () -> Unit,
    onAdded: (String) -> Unit
) {
    val viewModel: AddCityViewModel = viewModel(
        factory = AddCityViewModel.Factory(cityStore, geocodingRepository)
    )
    val state by viewModel.uiState.collectAsState()

    AddCityScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::retry,
        onBack = onBack,
        onSelect = { viewModel.addCity(it, onAdded) }
    )
}

@Composable
fun AddCityScreen(
    state: AddCityUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onSelect: (CitySearchItemUiState) -> Unit
) {
    StaticSkyBackground(palette = AddCitySky) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    onClick = onBack
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.add_city),
                    style = MaterialTheme.typography.headlineMedium,
                    color = GlassOnSurface
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            AddCitySearchField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.enter_city_name)
            )

            when {
                state.isLoading -> SearchLoading()
                state.errorMessage != null || state.errorMessageRes != null -> SearchError(
                    message = state.errorMessage ?: stringResource(state.errorMessageRes ?: R.string.city_search_failed),
                    onRetry = onRetry
                )
                state.results.isEmpty() -> SearchEmpty(
                    hasSearched = state.hasSearched,
                    query = state.query
                )
                else -> SearchResults(
                    results = state.results,
                    onSelect = onSelect
                )
            }
        }
    }
}

@Composable
private fun AddCitySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = GlassOnSurfaceMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = GlassOnSurfaceFaint,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = GlassOnSurface,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    ),
                    cursorBrush = SolidColor(GlassOnSurface)
                )
            }
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear),
                        tint = GlassOnSurfaceMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<CitySearchItemUiState>,
    onSelect: (CitySearchItemUiState) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(results, key = { it.city.id }) { item ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item) },
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = GlassOnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.city.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = GlassOnSurface
                        )
                        Text(
                            text = item.city.subtitle.ifBlank {
                                stringResource(R.string.lat_lon_format, item.city.latitude, item.city.longitude)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassOnSurfaceMuted
                        )
                        Text(
                            text = stringResource(R.string.lat_lon_format, item.city.latitude, item.city.longitude),
                            style = MaterialTheme.typography.labelMedium,
                            color = GlassOnSurfaceFaint
                        )
                    }
                    if (item.isAdded) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.16f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GlassOnSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.added),
                                color = GlassOnSurface,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchLoading() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = GlassOnSurface)
        Text(
            modifier = Modifier.padding(top = 14.dp),
            text = stringResource(R.string.loading_city_search),
            style = MaterialTheme.typography.bodyMedium,
            color = GlassOnSurfaceMuted
        )
    }
}

@Composable
private fun SearchError(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = GlassOnSurface
        )
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.18f),
                contentColor = GlassOnSurface
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
private fun SearchEmpty(
    hasSearched: Boolean,
    query: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                query.length < 2 -> stringResource(R.string.type_two_chars_to_search)
                hasSearched -> stringResource(R.string.no_city_found)
                else -> stringResource(R.string.search_city)
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = GlassOnSurface
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.city_saved_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = GlassOnSurfaceMuted,
            textAlign = TextAlign.Center
        )
    }
}
