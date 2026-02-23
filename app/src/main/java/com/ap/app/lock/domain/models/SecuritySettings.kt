package com.ap.app.lock.domain.models

data class SecuritySettings(
    val appLockMethod: String,
    val detectionMethod: String,
    val enableBiometricForApps: Boolean,
    val disableAdminSettings: Boolean,
    val disableSettingsPage: Boolean,
    val disableOverlayPage: Boolean,
    val disableUsageStatsPage: Boolean,
    val themeMode: String,
    val useDynamicColors: Boolean
)