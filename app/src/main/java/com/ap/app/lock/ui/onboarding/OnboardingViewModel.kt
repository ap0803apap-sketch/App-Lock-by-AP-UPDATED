package com.ap.app.lock.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ap.app.lock.data.local.AppDatabase
import com.ap.app.lock.data.local.entity.SettingsEntity
import com.ap.app.lock.data.repository.SettingsRepository
import com.ap.app.lock.utils.Encryption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SettingsRepository(database.settingsDao())

    private val _currentScreen = MutableStateFlow<OnboardingScreenState>(OnboardingScreenState.Welcome(onNext = { nextScreen() }))
    val currentScreen: StateFlow<OnboardingScreenState> = _currentScreen

    private val _onboardingComplete = MutableStateFlow(false)
    val onboardingComplete: StateFlow<Boolean> = _onboardingComplete

    private val _isChangePasscodeMode = MutableStateFlow(false)
    val isChangePasscodeMode: StateFlow<Boolean> = _isChangePasscodeMode

    private val _appLockType = MutableStateFlow("4 Digit Pin")
    val appLockType: StateFlow<String> = _appLockType

    private val _appLockAuthMethod = MutableStateFlow("biometric")
    val appLockAuthMethod: StateFlow<String> = _appLockAuthMethod

    private var appLockTypeValue = ""

    fun loadInitialScreen(isChangePasscodeMode: Boolean) {
        _isChangePasscodeMode.value = isChangePasscodeMode
        viewModelScope.launch {
            if (isChangePasscodeMode) {
                // When changing passcode, load the current settings first
                val settings = repository.getSettingsFlow().first()
                if (settings != null) {
                    _appLockType.value = settings.appLockType
                }
                _currentScreen.value = OnboardingScreenState.AppLockType
            } else {
                _currentScreen.value = OnboardingScreenState.Welcome(onNext = { nextScreen() })
            }
        }
    }

    fun nextScreen() {
        _currentScreen.value = when (_currentScreen.value) {
            is OnboardingScreenState.Welcome -> OnboardingScreenState.Permissions
            is OnboardingScreenState.Permissions -> OnboardingScreenState.AppLockAuthSetup
            is OnboardingScreenState.AppLockAuthSetup -> OnboardingScreenState.AppLockType
            is OnboardingScreenState.AppLockType -> OnboardingScreenState.SetupComplete
            is OnboardingScreenState.SetupComplete -> OnboardingScreenState.SetupComplete // Should be handled by completeOnboarding
        }
    }

    fun setAppLockAuthMethod(method: String) {
        _appLockAuthMethod.value = method
    }

    fun setAppLockType(type: String) {
        _appLockType.value = type
    }

    fun setAppLockTypeValue(value: String) {
        appLockTypeValue = value
    }

    fun saveLockSettings() {
        viewModelScope.launch {
            val isChanging = _isChangePasscodeMode.value
            withContext(Dispatchers.IO) {
                try {
                    val encryptedValue = Encryption.encrypt(appLockTypeValue) ?: appLockTypeValue

                    if (isChanging) {
                        // CHANGE PASSCODE MODE:
                        // Only update the locked-apps passcode (appLockType + appLockTypeValue).
                        // Never touch appLockAuthMethod — that would break splash authentication.
                        repository.updateLockedAppsPasscode(
                            lockType = _appLockType.value,
                            lockTypeValue = encryptedValue
                        )
                    } else {
                        // INITIAL SETUP:
                        // Save everything including app lock auth method.
                        val currentSettings = repository.getSettings() ?: SettingsEntity()
                        val updatedSettings = currentSettings.copy(
                            appLockAuthMethod = _appLockAuthMethod.value,
                            appLockType = _appLockType.value,
                            appLockTypeValue = encryptedValue,
                            lockedAppUnlockMethod = "both" // Set default for locked apps
                        )
                        repository.updateSettings(updatedSettings)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (isChanging) {
                _onboardingComplete.value = true
            } else {
                nextScreen()
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val currentSettings = repository.getSettings()
            if (currentSettings != null) {
                val updatedSettings = currentSettings.copy(onboardingCompleted = true)
                repository.updateSettings(updatedSettings)
            }
            _onboardingComplete.value = true
        }
    }
}