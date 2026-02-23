package com.ap.app.lock.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ap.app.lock.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)

    @Update
    suspend fun updateSettings(settings: SettingsEntity)

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): SettingsEntity?

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("UPDATE settings SET onboardingCompleted = :completed WHERE id = 1")
    suspend fun updateOnboardingStatus(completed: Boolean)

    @Query("UPDATE settings SET permissionsGranted = :permissions WHERE id = 1")
    suspend fun updatePermissions(permissions: String)

    @Query("UPDATE settings SET themeMode = :theme WHERE id = 1")
    suspend fun updateTheme(theme: String)

    @Query("UPDATE settings SET useDynamicColors = :useDynamicColors WHERE id = 1")
    suspend fun updateUseDynamicColors(useDynamicColors: Boolean)

    @Query("UPDATE settings SET enableBiometricForApps = :enableBiometric WHERE id = 1")
    suspend fun updateBiometricLock(enableBiometric: Boolean)

    @Query("UPDATE settings SET disableAdminSettings = :disable WHERE id = 1")
    suspend fun updateDisableAdminSettings(disable: Boolean)

    @Query("UPDATE settings SET disableSettingsPage = :disable WHERE id = 1")
    suspend fun updateDisableSettingsPage(disable: Boolean)

    @Query("UPDATE settings SET disableOverlayPage = :disable WHERE id = 1")
    suspend fun updateDisableOverlayPage(disable: Boolean)

    @Query("UPDATE settings SET disableUsageStatsPage = :disable WHERE id = 1")
    suspend fun updateDisableUsageStatsPage(disable: Boolean)

    @Query("UPDATE settings SET disableAccessibilityPage = :disable WHERE id = 1")
    suspend fun updateDisableAccessibilityPage(disable: Boolean)

    @Query("UPDATE settings SET appLockType = :lockType, appLockTypeValue = :lockTypeValue WHERE id = 1")
    suspend fun updateLockedAppsPasscode(lockType: String, lockTypeValue: String)

    @Query("UPDATE settings SET appLockAuthMethod = :method WHERE id = 1")
    suspend fun updateAppLockAuthMethod(method: String)

    @Query("UPDATE settings SET lockedAppUnlockMethod = :method WHERE id = 1")
    suspend fun updateLockedAppUnlockMethod(method: String)
}
