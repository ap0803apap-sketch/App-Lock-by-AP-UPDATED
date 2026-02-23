package com.ap.app.lock.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ap.app.lock.data.local.AppDatabase
import com.ap.app.lock.data.model.AppLockInfo
import com.ap.app.lock.data.repository.LockedAppRepository
import com.ap.app.lock.domain.usecases.GetInstalledAppsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val lockedAppRepository = LockedAppRepository(AppDatabase.getInstance(application).lockedAppDao())
    private val getInstalledAppsUseCase = GetInstalledAppsUseCase(application)

    private val _showSystemApps = MutableStateFlow(false)
    val showSystemApps: StateFlow<Boolean> = _showSystemApps

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortBy = MutableStateFlow("name_az")
    val sortBy: StateFlow<String> = _sortBy

    private val _installedApps = MutableStateFlow<List<AppLockInfo>>(emptyList())
    val installedApps: StateFlow<List<AppLockInfo>> = _installedApps

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val lockedAppsFlow = lockedAppRepository.getLockedApps()
            val installedAppsFlow = getInstalledAppsUseCase.execute(showSystemApps.value)

            lockedAppsFlow.combine(installedAppsFlow) { lockedApps, installedApps ->
                installedApps.map {
                    AppLockInfo(
                        appName = it.appName,
                        packageName = it.packageName,
                        icon = it.icon,
                        isLocked = lockedApps.any { lockedApp -> lockedApp.packageName == it.packageName }
                    )
                }
            }.collect { apps ->
                _installedApps.value = apps
            }
        }
    }

    fun setShowSystemApps(show: Boolean) {
        _showSystemApps.value = show
        loadInstalledApps()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    fun toggleAppLock(packageName: String, isLocked: Boolean) {
        viewModelScope.launch {
            lockedAppRepository.toggleAppLock(packageName, isLocked)
        }
    }
}