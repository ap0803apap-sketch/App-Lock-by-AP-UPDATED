package com.ap.app.lock.domain.models

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable? = null,
    val isLocked: Boolean = false,
    val isSystemApp: Boolean = false,
    val lockType: String = "",
    val enableBiometric: Boolean = false,
    val installTime: Long = 0
)