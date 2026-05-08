package com.example.lcb.app.weather.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lcb.app.LcbApp
import com.example.lcb.app.weather.ui.about.AboutScreen
import com.example.lcb.app.weather.ui.about.PrivacyScreen
import com.example.lcb.app.weather.ui.addcity.AddCityRoute
import com.example.lcb.app.weather.ui.cities.CityManagerRoute
import com.example.lcb.app.weather.ui.main.MainWeatherRoute
import com.example.lcb.app.weather.ui.settings.SettingsRoute

@Composable
fun WeatherNavGraph(
    startCityId: String?,
    openAddCityFirst: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (openAddCityFirst) {
        WeatherRoute.AddCity.route
    } else {
        WeatherRoute.Main.create(startCityId)
    }
    val container = (LocalContext.current.applicationContext as LcbApp).weatherContainer

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = WeatherRoute.Main.route,
            arguments = listOf(
                navArgument("cityId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")
            MainWeatherRoute(
                cityId = cityId,
                cityStore = container.cityStore,
                settingsStore = container.settingsStore,
                weatherRepository = container.weatherRepository,
                onOpenCities = { navController.navigate(WeatherRoute.CityManager.route) },
                onOpenSettings = { navController.navigate(WeatherRoute.Settings.route) },
                onAddCity = { navController.navigate(WeatherRoute.AddCity.route) }
            )
        }

        composable(WeatherRoute.CityManager.route) {
            CityManagerRoute(
                cityStore = container.cityStore,
                settingsStore = container.settingsStore,
                weatherRepository = container.weatherRepository,
                onBack = navController::popBackStack,
                onAddCity = { navController.navigate(WeatherRoute.AddCity.route) },
                onOpenCity = { cityId ->
                    navController.navigate(WeatherRoute.Main.create(cityId)) {
                        popUpTo(WeatherRoute.Main.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(WeatherRoute.AddCity.route) {
            AddCityRoute(
                cityStore = container.cityStore,
                geocodingRepository = container.geocodingRepository,
                onBack = navController::popBackStack,
                onAdded = { cityId ->
                    navController.navigate(WeatherRoute.Main.create(cityId)) {
                        popUpTo(WeatherRoute.AddCity.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(WeatherRoute.Settings.route) {
            SettingsRoute(
                settingsStore = container.settingsStore,
                onBack = navController::popBackStack,
                onAbout = { navController.navigate(WeatherRoute.About.route) },
                onPrivacy = { navController.navigate(WeatherRoute.Privacy.route) }
            )
        }

        composable(WeatherRoute.About.route) {
            AboutScreen(onBack = navController::popBackStack)
        }

        composable(WeatherRoute.Privacy.route) {
            PrivacyScreen(onBack = navController::popBackStack)
        }
    }
}

@Composable
private fun SimplePlaceholderScreen(
    title: String,
    body: String,
    primaryAction: String,
    onPrimaryAction: () -> Unit,
    secondaryAction: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    tertiaryAction: String? = null,
    onTertiaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onPrimaryAction) {
            Text(text = primaryAction)
        }
        if (secondaryAction != null && onSecondaryAction != null) {
            OutlinedButton(
                modifier = Modifier.padding(top = 10.dp),
                onClick = onSecondaryAction
            ) {
                Text(text = secondaryAction)
            }
        }
        if (tertiaryAction != null && onTertiaryAction != null) {
            OutlinedButton(
                modifier = Modifier.padding(top = 10.dp),
                onClick = onTertiaryAction
            ) {
                Text(text = tertiaryAction)
            }
        }
    }
}
