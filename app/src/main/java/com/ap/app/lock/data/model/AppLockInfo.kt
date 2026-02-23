package com.ap.app.lock.data.model

import android.graphics.drawable.Drawable

data class AppLockInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    val isLocked: Boolean
)