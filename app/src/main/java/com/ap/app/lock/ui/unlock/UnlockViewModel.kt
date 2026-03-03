package com.ap.app.lock.ui.unlock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ap.app.lock.data.local.AppDatabase
import com.ap.app.lock.data.repository.SettingsRepository
import com.ap.app.lock.utils.Encryption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UnlockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SettingsRepository

    private val _activeUnlockMethod = MutableStateFlow("pin")
    val activeUnlockMethod: StateFlow<String> = _activeUnlockMethod

    private val _lockedAppUnlockMethod = MutableStateFlow("both")
    val lockedAppUnlockMethod: StateFlow<String> = _lockedAppUnlockMethod

    private val _appLockType = MutableStateFlow("4-Digit PIN")
    val appLockType: StateFlow<String> = _appLockType

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private var decryptedPasscode: String = ""

    init {
        val dao = AppDatabase.getInstance(application).settingsDao()
        repository = SettingsRepository(dao)
    }

    fun init(fromSplash: Boolean, splashAuthMethod: String?) {
        viewModelScope.launch {
            val settings = repository.getSettings() ?: run {
                _isReady.value = true
                return@launch
            }

            decryptedPasscode = Encryption.decrypt(settings.appLockTypeValue.orEmpty()) ?: ""
            _appLockType.value = settings.appLockType ?: "4-Digit PIN"
            _lockedAppUnlockMethod.value = settings.lockedAppUnlockMethod

            if (fromSplash) {
                // For splash screen, the method is fixed from onboarding
                _activeUnlockMethod.value = splashAuthMethod ?: "pin"
            } else {
                // For app unlocks, the method depends on the settings
                when (settings.lockedAppUnlockMethod) {
                    "both" -> _activeUnlockMethod.value = "biometric"
                    "biometric" -> _activeUnlockMethod.value = "biometric"
                    "passcode" -> _activeUnlockMethod.value = "pin"
                }
            }
            _isReady.value = true
        }
    }

    fun setActiveUnlockMethod(method: String) {
        _activeUnlockMethod.value = method
    }

    fun verifyPassword(input: String): Boolean {
        return input.isNotEmpty() && input == decryptedPasscode
    }
}