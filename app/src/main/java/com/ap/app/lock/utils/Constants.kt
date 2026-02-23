package com.ap.app.lock.utils

object Constants {
    // App Info
    const val APP_NAME = "App Lock"
    const val DEVELOPER_EMAIL = "your@email.com"
    const val GITHUB_REPO = "https://github.com/yourprofile/applock"

    // Security
    const val MIN_PIN_LENGTH = 4
    const val PASSWORD_MIN_LENGTH = 8
    const val UNLOCK_TIMEOUT_MS = 5 * 60 * 1000L
    const val MAX_FAILED_ATTEMPTS = 5

    // Detection Methods
    const val DETECTION_USAGE_STATS = "usage_stats"
    const val DETECTION_ACCESSIBILITY = "accessibility"

    // Lock Types
    const val LOCK_TYPE_4_DIGIT = "4_digit_pin"
    const val LOCK_TYPE_6_DIGIT = "6_digit_pin"
    const val LOCK_TYPE_UNLIMITED = "unlimited_pin"
    const val LOCK_TYPE_PASSWORD = "password"

    // Authentication
    const val AUTH_BIOMETRIC_ONLY = "biometric_only"
    const val AUTH_DEVICE_LOCK = "device_lock"

    // Theme
    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    // Packages
    val EXCLUDED_PACKAGES = listOf(
        "com.ap.app.lock",
        "android",
        "com.android.systemui",
        "com.google.android.gms"
    )
}