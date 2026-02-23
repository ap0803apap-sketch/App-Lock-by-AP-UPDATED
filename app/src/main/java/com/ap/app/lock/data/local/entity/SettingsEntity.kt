package com.ap.app.lock.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val onboardingCompleted: Boolean = false,
    val permissionsGranted: String = "",
    val appLockAuthMethod: String = "biometric", // "biometric" or "device_lock"
    val appLockType: String = "4 Digit Pin", // "4 Digit Pin", "6 Digit Pin", "Password", etc.
    val appLockTypeValue: String? = null, // Encrypted PIN/password
    val enableBiometricForApps: Boolean = true,
    val themeMode: String = "system", // "system", "light", "dark"
    val useDynamicColors: Boolean = true,
    val disableAdminSettings: Boolean = false,
    val disableSettingsPage: Boolean = false,
    val disableOverlayPage: Boolean = false,
    val disableUsageStatsPage: Boolean = false,
    val disableAccessibilityPage: Boolean = false,
    val lockedAppUnlockMethod: String = "both" // "both", "biometric", "passcode"
)
