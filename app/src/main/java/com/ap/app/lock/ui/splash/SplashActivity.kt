package com.ap.app.lock.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.ap.app.lock.ui.MainActivity
import com.ap.app.lock.ui.onboarding.OnboardingActivity
import com.ap.app.lock.ui.theme.AppLockTheme
import com.ap.app.lock.ui.theme.ThemeRepository
import com.ap.app.lock.ui.unlock.UnlockActivity
import com.ap.app.lock.utils.PermissionHelper
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val themeSettings = ThemeRepository(this@SplashActivity).getThemeSettings()
            viewModel.checkState()

            setContent {
                val darkTheme = when (themeSettings.themeMode) {
                    "light" -> false
                    "dark" -> true
                    else -> isSystemInDarkTheme()
                }

                AppLockTheme(
                    darkTheme = darkTheme,
                    dynamicColor = themeSettings.useDynamicColors
                ) {
                    SplashScreen(viewModel = viewModel) { state ->
                        when (state) {
                            is NavigationState.GoToOnboarding -> {
                                val intent = Intent(this@SplashActivity, OnboardingActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            is NavigationState.GoToUnlock -> {
                                val allPermissionsGranted = PermissionHelper.hasAllPermissions(this@SplashActivity)
                                val intent = Intent(this@SplashActivity, UnlockActivity::class.java).apply {
                                    putExtra("package_name", packageName)
                                    putExtra("fromSplash", state.fromSplash)
                                    putExtra("allPermissionsGranted", allPermissionsGranted)
                                    putExtra("appLockAuthMethod", state.appLockAuthMethod)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                            }
                            is NavigationState.GoToMain -> {
                                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(viewModel: SplashViewModel, onNavigate: (NavigationState) -> Unit) {
    val navigationState by viewModel.navigationState.collectAsState()

    LaunchedEffect(navigationState) {
        if (navigationState !is NavigationState.Loading) {
            onNavigate(navigationState)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}