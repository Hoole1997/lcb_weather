package com.example.lcb.app.weather.ui.addcity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lcb.app.weather.data.local.CityStore
import com.example.lcb.app.weather.data.repository.GeocodingRepository

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
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = "添加城市",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                value = state.query,
                onValueChange = onQueryChange,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(text = "输入城市名") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            when {
                state.isLoading -> SearchLoading()
                state.errorMessage != null -> SearchError(
                    message = state.errorMessage,
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
private fun SearchResults(
    results: List<CitySearchItemUiState>,
    onSelect: (CitySearchItemUiState) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(results, key = { it.city.id }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.city.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.city.subtitle.ifBlank {
                                "纬度 %.4f · 经度 %.4f".format(item.city.latitude, item.city.longitude)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "纬度 %.4f · 经度 %.4f".format(item.city.latitude, item.city.longitude),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.isAdded) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "已添加",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
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
        CircularProgressIndicator()
        Text(
            modifier = Modifier.padding(top = 14.dp),
            text = "正在搜索城市",
            style = MaterialTheme.typography.bodyMedium
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
            textAlign = TextAlign.Center
        )
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onRetry
        ) {
            Text(text = "重试")
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
                query.length < 2 -> "输入至少 2 个字符开始搜索"
                hasSearched -> "没有找到城市"
                else -> "搜索城市"
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "选择城市后会保存到本地城市列表",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
