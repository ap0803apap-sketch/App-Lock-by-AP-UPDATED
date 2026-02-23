package com.ap.app.lock.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ap.app.lock.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = ThemeRepository(application)

    private val _themeSettings = MutableStateFlow<SettingsEntity?>(null)
    val themeSettings: StateFlow<SettingsEntity?> = _themeSettings

    init {
        viewModelScope.launch {
            _themeSettings.value = themeRepository.getThemeSettings()
        }
    }
}