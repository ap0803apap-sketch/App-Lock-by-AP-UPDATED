package com.ap.app.lock.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ap.app.lock.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)

    private val _showOnboarding = MutableStateFlow(true)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding

    init {
        viewModelScope.launch {
            val settings = database.settingsDao().getSettings()
            _showOnboarding.value = settings?.onboardingCompleted != true
        }
    }
}