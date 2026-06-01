package com.example.lcb.app.weather.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.res.stringResource
import com.example.lcb.app.R
import com.example.lcb.app.LcbApp
import com.example.lcb.app.MainActivity
import com.example.lcb.app.weather.domain.model.WeatherSettings
import com.example.lcb.app.weather.ui.navigation.WeatherNavGraph
import com.example.lcb.app.weather.ui.startup.StartupUiState
import com.example.lcb.app.weather.ui.startup.StartupViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import com.example.lcb.app.weather.ui.theme.GlassOnSurface
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceMuted
import com.example.lcb.app.weather.ui.theme.SkyPalette
import com.example.lcb.app.weather.ui.theme.StaticSkyBackground
import com.example.lcb.app.weather.ui.theme.WeatherTheme

@Composable
fun WeatherApp() {
    val context = LocalContext.current
    val activity = context.findMainActivity()
    val container = (context.applicationContext as LcbApp).weatherContainer
    val settings by container.settingsStore.settings.collectAsState(initial = null)
    val darkTheme = isSystemInDarkTheme()
    val startupViewModel: StartupViewModel = viewModel(
        factory = StartupViewModel.Factory(
            cityStore = container.cityStore,
            settingsStore = container.settingsStore,
            locationRepository = container.locationRepository,
            weatherRepository = container.weatherRepository
        )
    )
    val startupState by startupViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        startupViewModel.start()
    }

    LaunchedEffect(activity, startupState.isLoading) {
        activity?.setStartupBackLaunchEnabled(startupState.isLoading)
    }

    DisposableEffect(activity) {
        onDispose {
            activity?.setStartupBackLaunchEnabled(false)
        }
    }

    LaunchedEffect(settings?.languageOption) {
        val loadedSettings = settings ?: return@LaunchedEffect
        val localeList = loadedSettings.languageOption.localeTag
            ?.let(LocaleListCompat::forLanguageTags)
            ?: LocaleListCompat.getEmptyLocaleList()
        if (AppCompatDelegate.getApplicationLocales() != localeList) {
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    WeatherTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val canOpenApp = !startupState.isLoading &&
                (startupState.activeCityId != null ||
                    (startupState.needsCitySelection &&
                        startupState.errorMessage == null &&
                        startupState.errorMessageRes == null))
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

private val StartupSky = SkyPalette(
    gradient = listOf(Color(0xFF0F1A2C), Color(0xFF1F3F60), Color(0xFF3F76A8)),
    glow = Color(0x55FFE9B0),
    glowX = 260f,
    glowY = 200f,
    glowRadius = 700f
)

@Composable
private fun StartupScreen(
    state: StartupUiState = StartupUiState(),
    onRetryLocation: () -> Unit = {},
    onChooseCity: () -> Unit = {}
) {
    StaticSkyBackground(palette = StartupSky) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = Icons.Filled.WbSunny,
                contentDescription = null,
                tint = GlassOnSurface,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                color = GlassOnSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.startup_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = GlassOnSurfaceMuted,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(28.dp))
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = GlassOnSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
            Text(
                text = when {
                    state.activeCityId != null -> stringResource(
                        R.string.startup_default_city_ready,
                        state.activeCityId
                    )
                    state.isLoading -> stringResource(state.messageRes)
                    state.needsCitySelection -> state.errorMessage
                        ?: state.errorMessageRes?.let { stringResource(it) }
                        ?: stringResource(R.string.choose_city)
                    else -> stringResource(state.messageRes)
                },
                style = MaterialTheme.typography.titleMedium,
                color = GlassOnSurface
            )
            if (state.needsCitySelection) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onChooseCity,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.92f),
                        contentColor = Color(0xFF14304D)
                    )
                ) {
                    Text(text = stringResource(R.string.search_city))
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = onRetryLocation) {
                    Text(text = stringResource(R.string.retry_location), color = GlassOnSurface)
                }
            }
        }
    }
}

private fun Context.findMainActivity(): MainActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is MainActivity) return current
        current = current.baseContext
    }
    return null
}

@Preview(showBackground = true)
@Composable
private fun WeatherAppPreview() {
    WeatherApp()
}
