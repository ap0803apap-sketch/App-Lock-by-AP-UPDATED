package com.ap.app.lock.receivers

import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Renamed back to DeviceAdminReceiver from AppLockDeviceAdminReceiver.
 *
 * Samsung Knox (and some versions of Device Policy Manager) can be extremely sensitive
 * to the class name if it was previously registered. The Logcat error:
 * "java.lang.IllegalArgumentException: Unknown admin: ComponentInfo{.../com.ap.app.lock.receivers.DeviceAdminReceiver}"
 * indicates the system is specifically looking for this class name.
 *
 * We use the fully qualified name for the parent class to avoid any possible ambiguity with
 * frameworks names in the same file.
 */
class DeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {

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
            return ComponentName(context, DeviceAdminReceiver::class.java)
        }
    }
}