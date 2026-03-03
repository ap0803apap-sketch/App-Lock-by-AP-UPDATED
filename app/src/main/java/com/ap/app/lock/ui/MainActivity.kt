package com.ap.app.lock.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ap.app.lock.services.AppMonitoringService
import com.ap.app.lock.ui.main.MainScreen
import com.ap.app.lock.ui.settings.SettingsViewModel
import com.ap.app.lock.ui.theme.AppLockTheme

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup window security
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // Start the monitoring service
        val serviceIntent = Intent(this, AppMonitoringService::class.java)
        startService(serviceIntent)

        setContent {
            val theme by settingsViewModel.theme.collectAsState()
            val useDynamicColors by settingsViewModel.useDynamicColors.collectAsState()
            
            val darkTheme = when (theme) {
                "system" -> isSystemInDarkTheme()
                "dark" -> true
                else -> false
            }

            AppLockTheme(darkTheme = darkTheme, dynamicColor = useDynamicColors) {
                MainScreen()
            }
        }
    }
}
