package com.ap.app.lock.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ap.app.lock.data.local.AppDatabase
import com.ap.app.lock.data.repository.SettingsRepository
import kotlinx.coroutines.launch

class ChangePasscodeViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(AppDatabase.getInstance(application).settingsDao())

    fun changePasscode(newPasscode: String) {
        viewModelScope.launch {
            settingsRepository.getSettings()?.let {
                val updatedSettings = it.copy(appLockTypeValue = newPasscode)
                settingsRepository.updateSettings(updatedSettings)
            }
        }
    }
}