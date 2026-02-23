package com.ap.app.lock.ui.onboarding

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ap.app.lock.receivers.AppLockDeviceAdminReceiver
import com.ap.app.lock.ui.MainActivity
import com.ap.app.lock.ui.onboarding.screens.AppLockAuthSetupScreen
import com.ap.app.lock.ui.onboarding.screens.AppLockTypeScreen
import com.ap.app.lock.ui.onboarding.screens.PermissionsScreen
import com.ap.app.lock.ui.onboarding.screens.SetupCompleteScreen
import com.ap.app.lock.ui.onboarding.screens.WelcomeScreen
import com.ap.app.lock.ui.theme.AppLockTheme
import com.ap.app.lock.ui.theme.ThemeViewModel
import com.ap.app.lock.utils.BiometricAuthenticator

class OnboardingActivity : AppCompatActivity() {

    private val viewModel: OnboardingViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    companion object {
        private const val REQUEST_CODE_DEVICE_ADMIN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isChangePasscodeMode = intent.getBooleanExtra("isChangePasscodeMode", false)
        viewModel.loadInitialScreen(isChangePasscodeMode)

        setContent {
            val themeSettings by themeViewModel.themeSettings.collectAsState()
            val darkTheme = when (themeSettings?.themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            AppLockTheme(
                darkTheme = darkTheme,
                dynamicColor = themeSettings?.useDynamicColors ?: false
            ) {
                OnboardingScreen(
                    onFinish = { navigateToMain() },
                    activity = this,
                    viewModel = viewModel,
                    onPasscodeChanged = { finish() },
                    onRequestDeviceAdmin = { launchDeviceAdminRequest() }
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun launchDeviceAdminRequest() {
        val componentName = ComponentName(this, AppLockDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Required to lock apps and prevent uninstall."
        )
        startActivityForResult(intent, REQUEST_CODE_DEVICE_ADMIN)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // PermissionsScreen polls isDeviceAdminEnabled every 500ms — UI updates automatically
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel,
    activity: AppCompatActivity,
    onPasscodeChanged: () -> Unit,
    onRequestDeviceAdmin: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val appLockAuthMethod by viewModel.appLockAuthMethod.collectAsState()
    val onboardingComplete by viewModel.onboardingComplete.collectAsState()
    val isChangePasscodeMode by viewModel.isChangePasscodeMode.collectAsState()

    val biometricAuthenticator = BiometricAuthenticator(
        activity = activity,
        onAuthenticationSuccess = { viewModel.nextScreen() },
        onAuthenticationError = { }
    )

    LaunchedEffect(onboardingComplete) {
        if (onboardingComplete) {
            if (isChangePasscodeMode) onPasscodeChanged() else onFinish()
        }
    }

    Scaffold { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { -it })
                },
                label = "onboarding_transition"
            ) { screen ->
                when (screen) {
                    is OnboardingScreenState.Welcome ->
                        WelcomeScreen(onNext = screen.onNext)
                    is OnboardingScreenState.Permissions ->
                        PermissionsScreen(
                            onPermissionsGranted = { viewModel.nextScreen() },
                            onRequestDeviceAdmin = onRequestDeviceAdmin
                        )
                    is OnboardingScreenState.AppLockAuthSetup ->
                        AppLockAuthSetupScreen(
                            viewModel = viewModel,
                            onAuthenticationRequested = {
                                biometricAuthenticator.authenticate(
                                    allowDeviceCredential = appLockAuthMethod == "device_lock"
                                )
                            },
                            onAuthenticationComplete = { viewModel.nextScreen() }
                        )
                    is OnboardingScreenState.AppLockType ->
                        AppLockTypeScreen(
                            viewModel = viewModel,
                            onNext = { viewModel.saveLockSettings() }
                        )
                    is OnboardingScreenState.SetupComplete ->
                        SetupCompleteScreen(onGetStarted = { viewModel.completeOnboarding() })
                }
            }
        }
    }
}