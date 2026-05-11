package com.example.lcb.app.weather.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.lcb.app.R
import com.example.lcb.app.weather.domain.model.SavedCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.roundToInt

class LocationRepository(
    private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasLocationPermission(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    suspend fun resolveCurrentCity(): Result<SavedCity> {
        return runCatching {
            val location = getCurrentLocation()
            val address = reverseGeocode(location)
            SavedCity(
                id = CURRENT_LOCATION_ID,
                name = address.cityName ?: context.getString(R.string.current_location),
                country = address.country,
                countryCode = address.countryCode,
                admin1 = address.admin1,
                latitude = location.latitude,
                longitude = location.longitude,
                timezone = "auto",
                sortIndex = 0
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location {
        check(hasLocationPermission()) { "Location permission is not granted." }

        val providers = enabledProviders()
        check(providers.isNotEmpty()) { "No enabled location provider is available." }

        bestLastKnownLocation(providers)?.let { return it }

        return withTimeout(LOCATION_TIMEOUT_MS) {
            requestSingleLocation(providers.first())
        }
    }

    private fun enabledProviders(): List<String> {
        return listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { provider ->
                runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
            }
    }

    @SuppressLint("MissingPermission")
    private fun bestLastKnownLocation(providers: List<String>): Location? {
        val now = System.currentTimeMillis()
        return providers
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .filter { now - it.time <= LAST_LOCATION_MAX_AGE_MS }
            .maxByOrNull { it.accuracyScore() }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocation(provider: String): Location {
        return suspendCancellableCoroutine { continuation ->
            val listener = @Suppress("OVERRIDE_DEPRECATION") object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }

                override fun onProviderDisabled(provider: String) = Unit
                override fun onProviderEnabled(provider: String) = Unit
                @Deprecated("Deprecated Android provider status callback.")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            }

            locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            continuation.invokeOnCancellation {
                locationManager.removeUpdates(listener)
            }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(location: Location): AddressInfo {
        return withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses?.firstOrNull()
                AddressInfo(
                    cityName = address?.locality
                        ?: address?.subAdminArea
                        ?: address?.adminArea,
                    admin1 = address?.adminArea,
                    country = address?.countryName,
                    countryCode = address?.countryCode
                )
            }.getOrDefault(AddressInfo())
        }
    }

    private fun Location.accuracyScore(): Int {
        val accuracyPart = if (hasAccuracy()) (10_000 - accuracy.roundToInt()).coerceAtLeast(0) else 0
        val providerPart = if (provider == LocationManager.NETWORK_PROVIDER) 1_000 else 500
        return accuracyPart + providerPart
    }

    private data class AddressInfo(
        val cityName: String? = null,
        val admin1: String? = null,
        val country: String? = null,
        val countryCode: String? = null
    )

    companion object {
        const val CURRENT_LOCATION_ID = "current_location"
        private const val LOCATION_TIMEOUT_MS = 10_000L
        private const val LAST_LOCATION_MAX_AGE_MS = 30 * 60 * 1000L
    }
}
