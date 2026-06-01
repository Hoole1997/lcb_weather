package com.example.lcb.app

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.lcb.app.weather.ui.WeatherApp

class MainActivity : AppCompatActivity() {
    private lateinit var startupBackCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startupBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                LcbApp.backLaunchActivity()
            }
        }
        onBackPressedDispatcher.addCallback(this, startupBackCallback)
        setContent {
            WeatherApp()
        }
    }

    fun setStartupBackLaunchEnabled(enabled: Boolean) {
        if (::startupBackCallback.isInitialized) {
            startupBackCallback.isEnabled = enabled
        }
    }
}
