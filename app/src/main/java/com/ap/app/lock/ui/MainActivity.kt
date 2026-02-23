package com.ap.app.lock.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.ap.app.lock.services.AppMonitoringService
import com.ap.app.lock.ui.main.MainScreen
import com.ap.app.lock.ui.settings.SettingsViewModel

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
            AppTheme(theme = theme, useDynamicColors = useDynamicColors) {
                MainScreen()
            }
        }
    }
}

@Composable
fun AppTheme(
    theme: String,
    useDynamicColors: Boolean,
    content: @Composable () -> Unit
) {
    val darkTheme = when (theme) {
        "system" -> isSystemInDarkTheme()
        "dark" -> true
        else -> false
    }

    val amoledDarkColorScheme = darkColorScheme(
        primary = Color.Black,
        surface = Color.Black,
        background = Color.Black
    )

    val colorScheme =
        if (useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) amoledDarkColorScheme else lightColorScheme()
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}