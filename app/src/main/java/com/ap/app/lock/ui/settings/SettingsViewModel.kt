package com.ap.app.lock.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ap.app.lock.data.local.AppDatabase
import com.ap.app.lock.data.local.entity.SettingsEntity
import com.ap.app.lock.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(AppDatabase.getInstance(application).settingsDao())

    private val _theme = MutableStateFlow("system")
    val theme: StateFlow<String> = _theme

    private val _useDynamicColors = MutableStateFlow(false)
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors

    private val _appLockAuthMethod = MutableStateFlow("biometric")
    val appLockAuthMethod: StateFlow<String> = _appLockAuthMethod

    private val _lockedAppUnlockMethod = MutableStateFlow("both")
    val lockedAppUnlockMethod: StateFlow<String> = _lockedAppUnlockMethod

    private val _disableAdminSettings = MutableStateFlow(false)
    val disableAdminSettings: StateFlow<Boolean> = _disableAdminSettings

    private val _disableUsageStatsPage = MutableStateFlow(false)
    val disableUsageStatsPage: StateFlow<Boolean> = _disableUsageStatsPage

    private val _disableAccessibilityPage = MutableStateFlow(false)
    val disableAccessibilityPage: StateFlow<Boolean> = _disableAccessibilityPage

    private val _disableOverlayPage = MutableStateFlow(false)
    val disableOverlayPage: StateFlow<Boolean> = _disableOverlayPage

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            var settings = settingsRepository.getSettingsFlow().first()
            if (settings == null) {
                settings = SettingsEntity()
                settingsRepository.saveSettings(settings)
            }
            settings.let {
                _theme.value = it.themeMode
                _useDynamicColors.value = it.useDynamicColors
                _appLockAuthMethod.value = it.appLockAuthMethod
                _lockedAppUnlockMethod.value = it.lockedAppUnlockMethod
                _disableAdminSettings.value = it.disableAdminSettings
                _disableUsageStatsPage.value = it.disableUsageStatsPage
                _disableAccessibilityPage.value = it.disableAccessibilityPage
                _disableOverlayPage.value = it.disableOverlayPage
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            _theme.value = theme
            settingsRepository.updateTheme(theme)
        }
    }

    fun setUseDynamicColors(useDynamicColors: Boolean) {
        viewModelScope.launch {
            _useDynamicColors.value = useDynamicColors
            settingsRepository.updateUseDynamicColors(useDynamicColors)
        }
    }

    fun setAppLockAuthMethod(method: String) {
        viewModelScope.launch {
            _appLockAuthMethod.value = method
            settingsRepository.updateAppLockAuthMethod(method)
        }
    }

    fun setLockedAppUnlockMethod(method: String) {
        viewModelScope.launch {
            _lockedAppUnlockMethod.value = method
            settingsRepository.updateLockedAppUnlockMethod(method)
        }
    }

    fun setDisableAdminSettings(disable: Boolean) {
        viewModelScope.launch {
            _disableAdminSettings.value = disable
            settingsRepository.updateDisableAdminSettings(disable)
        }
    }

    fun setDisableUsageStatsPage(disable: Boolean) {
        viewModelScope.launch {
            _disableUsageStatsPage.value = disable
            settingsRepository.updateDisableUsageStatsPage(disable)
        }
    }

    fun setDisableAccessibilityPage(disable: Boolean) {
        viewModelScope.launch {
            _disableAccessibilityPage.value = disable
            settingsRepository.updateDisableAccessibilityPage(disable)
        }
    }

    fun setDisableOverlayPage(disable: Boolean) {
        viewModelScope.launch {
            _disableOverlayPage.value = disable
            settingsRepository.updateDisableOverlayPage(disable)
        }
    }
}