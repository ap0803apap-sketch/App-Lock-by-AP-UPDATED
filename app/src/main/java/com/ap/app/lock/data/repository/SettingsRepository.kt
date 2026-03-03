package com.ap.app.lock.data.repository

import com.ap.app.lock.data.local.dao.SettingsDao
import com.ap.app.lock.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {

    suspend fun saveSettings(settings: SettingsEntity) = settingsDao.insertSettings(settings)

    suspend fun updateSettings(settings: SettingsEntity) = settingsDao.updateSettings(settings)

    suspend fun getSettings(): SettingsEntity? = settingsDao.getSettings()

    fun getSettingsFlow(): Flow<SettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun completeOnboarding() = settingsDao.updateOnboardingStatus(true)

    suspend fun updatePermissions(permissions: String) = settingsDao.updatePermissions(permissions)

    suspend fun updateTheme(theme: String) = settingsDao.updateTheme(theme)

    suspend fun updateUseDynamicColors(useDynamicColors: Boolean) = settingsDao.updateUseDynamicColors(useDynamicColors)

    suspend fun updateBiometricLock(enableBiometric: Boolean) = settingsDao.updateBiometricLock(enableBiometric)

    suspend fun updateDisableAdminSettings(disable: Boolean) = settingsDao.updateDisableAdminSettings(disable)

    suspend fun updateDisableSettingsPage(disable: Boolean) = settingsDao.updateDisableSettingsPage(disable)

    suspend fun updateDisableOverlayPage(disable: Boolean) = settingsDao.updateDisableOverlayPage(disable)

    suspend fun updateDisableUsageStatsPage(disable: Boolean) = settingsDao.updateDisableUsageStatsPage(disable)

    suspend fun updateDisableAccessibilityPage(disable: Boolean) = settingsDao.updateDisableAccessibilityPage(disable)

    suspend fun updateLockedAppsPasscode(lockType: String, lockTypeValue: String) =
        settingsDao.updateLockedAppsPasscode(lockType, lockTypeValue)

    suspend fun updateAppLockAuthMethod(method: String) = settingsDao.updateAppLockAuthMethod(method)

    suspend fun updateLockedAppUnlockMethod(method: String) = settingsDao.updateLockedAppUnlockMethod(method)

    suspend fun updateRelockPolicy(policy: String) = settingsDao.updateRelockPolicy(policy)
}