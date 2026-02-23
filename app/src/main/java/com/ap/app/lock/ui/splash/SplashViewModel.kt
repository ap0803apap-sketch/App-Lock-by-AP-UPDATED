package com.ap.app.lock.ui.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ap.app.lock.data.local.AppDatabase
import com.ap.app.lock.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(AppDatabase.getInstance(application).settingsDao())

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Loading)
    val navigationState: StateFlow<NavigationState> = _navigationState

    fun checkState() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsFlow().first()

            if (settings == null || !settings.onboardingCompleted) {
                _navigationState.value = NavigationState.GoToOnboarding
            } else {
                _navigationState.value = NavigationState.GoToUnlock(fromSplash = true, appLockAuthMethod = settings.appLockAuthMethod)
            }
        }
    }
}

sealed class NavigationState {
    object Loading : NavigationState()
    object GoToOnboarding : NavigationState()
    data class GoToUnlock(val fromSplash: Boolean, val appLockAuthMethod: String) : NavigationState()
    object GoToMain : NavigationState()
    object GoToPermissions : NavigationState()
}