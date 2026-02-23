package com.ap.app.lock.receivers

import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * RENAMED from DeviceAdminReceiver to AppLockDeviceAdminReceiver.
 *
 * The original class was named "DeviceAdminReceiver" which is IDENTICAL to the
 * Android framework class it extends (android.app.admin.DeviceAdminReceiver).
 * This name collision causes Kotlin/Android runtime to fail resolving the ComponentName
 * correctly, so Samsung's security framework silently rejects the Device Admin dialog.
 *
 * Fix: rename to AppLockDeviceAdminReceiver — a unique name with no collision.
 *
 * IMPORTANT: After renaming, also update:
 *   1. AndroidManifest.xml  → android:name=".receivers.AppLockDeviceAdminReceiver"
 *   2. PermissionHelper.kt  → ComponentName(context, AppLockDeviceAdminReceiver::class.java)
 *   3. OnboardingActivity.kt → ComponentName(this, AppLockDeviceAdminReceiver::class.java)
 *   4. PermissionsActivity.kt → ComponentName(this, AppLockDeviceAdminReceiver::class.java)
 */
class AppLockDeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Device admin is required for app lock protection"
    }

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, AppLockDeviceAdminReceiver::class.java)
        }
    }
}