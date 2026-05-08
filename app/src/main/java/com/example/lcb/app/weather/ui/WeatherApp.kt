package com.example.lcb.app.weather.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lcb.app.LcbApp
import com.example.lcb.app.weather.ui.navigation.WeatherNavGraph
import com.example.lcb.app.weather.ui.startup.StartupUiState
import com.example.lcb.app.weather.ui.startup.StartupViewModel
import com.example.lcb.app.weather.ui.theme.WeatherTheme

@Composable
fun WeatherApp() {
    val context = LocalContext.current
    val container = (context.applicationContext as LcbApp).weatherContainer
    val startupViewModel: StartupViewModel = viewModel(
        factory = StartupViewModel.Factory(
            cityStore = container.cityStore,
            settingsStore = container.settingsStore,
            locationRepository = container.locationRepository,
            weatherRepository = container.weatherRepository
        )
    )
    val startupState by startupViewModel.uiState.collectAsState()
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        startupViewModel.onLocationPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        startupViewModel.start()
    }

    LaunchedEffect(startupState.needsLocationPermission) {
        if (startupState.needsLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    WeatherTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val canOpenApp = !startupState.isLoading &&
                (startupState.activeCityId != null ||
                    (startupState.needsCitySelection && startupState.errorMessage == null))
            if (canOpenApp) {
                WeatherNavGraph(
                    startCityId = startupState.activeCityId,
                    openAddCityFirst = startupState.needsCitySelection
                )
            } else {
                StartupScreen(
                    state = startupState,
                    onRetryLocation = startupViewModel::retryLocation,
                    onChooseCity = startupViewModel::chooseCityManually
                )
            }
        }
    }
}

@Composable
private fun StartupScreen(
    state: StartupUiState = StartupUiState(),
    onRetryLocation: () -> Unit = {},
    onChooseCity: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "天气",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Compose 基础已接入。接下来会逐步连接定位、城市和 Open-Meteo 天气数据。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = when {
                    state.activeCityId != null -> "默认城市已准备：${state.activeCityId}"
                    state.isLoading -> state.message
                    state.needsCitySelection -> state.errorMessage ?: "请选择城市"
                    else -> state.message
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
            if (state.needsCitySelection) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onChooseCity) {
                    Text(text = "搜索城市")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onRetryLocation) {
                    Text(text = "重试定位")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherAppPreview() {
    WeatherApp()
}
