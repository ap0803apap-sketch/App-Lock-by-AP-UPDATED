package com.ap.app.lock.utils

import android.content.Context

object PermissionUtils {

    fun hasAllPermissions(context: Context): Boolean {
        return PermissionHelper.hasNotificationPermission(context) &&
                PermissionHelper.hasOverlayPermission(context) &&
                PermissionHelper.hasUsageStatsPermission(context) &&
                PermissionHelper.hasAccessibilityPermission(context) &&
                PermissionHelper.isDeviceAdminEnabled(context) &&
                PermissionHelper.isBatteryOptimizationIgnored(context)
    }
}