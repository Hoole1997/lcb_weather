package com.example.lcb.app.weather.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

private const val NavTransitionMillis = 300

private val NavSpec = tween<androidx.compose.ui.unit.IntOffset>(
    durationMillis = NavTransitionMillis,
    easing = FastOutSlowInEasing
)

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
        modifier = modifier,
        // forward: new page enters from the trailing (right) edge
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = NavSpec
            )
        },
        // forward: previous page exits towards the leading (left) edge in lock-step
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = NavSpec
            )
        },
        // back: previous page re-enters from the leading edge
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = NavSpec
            )
        },
        // back: top page exits towards the trailing edge
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = NavSpec
            )
        }
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
