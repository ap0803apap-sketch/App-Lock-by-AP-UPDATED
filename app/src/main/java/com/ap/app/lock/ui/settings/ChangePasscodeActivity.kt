package com.ap.app.lock.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.ap.app.lock.ui.theme.AppLockTheme
import com.ap.app.lock.ui.theme.ThemeViewModel

class ChangePasscodeActivity : ComponentActivity() {

    private lateinit var themeViewModel: ThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeViewModel = ViewModelProvider(this)[ThemeViewModel::class.java]

        setContent {
            val themeSettings by themeViewModel.themeSettings.collectAsState()
            val darkTheme = when (themeSettings?.themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            AppLockTheme(darkTheme = darkTheme, dynamicColor = themeSettings?.useDynamicColors ?: false) {
                ChangePasscodeScreen(onPasscodeChanged = { finish() })
            }
        }
    }
}