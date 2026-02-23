package com.ap.app.lock.ui.onboarding

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.ViewModelProvider
import com.ap.app.lock.receivers.AppLockDeviceAdminReceiver
import com.ap.app.lock.ui.MainActivity
import com.ap.app.lock.ui.onboarding.screens.PermissionsScreen
import com.ap.app.lock.ui.theme.AppLockTheme
import com.ap.app.lock.ui.theme.ThemeViewModel

class PermissionsActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_DEVICE_ADMIN = 1001
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeViewModel = ViewModelProvider(this)[ThemeViewModel::class.java]

        setContent {
            val themeSettings = themeViewModel.themeSettings.value
            val darkTheme = when (themeSettings?.themeMode) {
                "light" -> false
                "dark"  -> true
                else    -> isSystemInDarkTheme()
            }
            AppLockTheme(darkTheme = darkTheme, dynamicColor = themeSettings?.useDynamicColors ?: false) {
                PermissionsScreen(
                    onPermissionsGranted = {
                        val intent = Intent(this@PermissionsActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    },
                    onRequestDeviceAdmin = { launchDeviceAdminRequest() }
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun launchDeviceAdminRequest() {
        val componentName = ComponentName(this, AppLockDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Required to lock apps and prevent uninstall."
        )
        startActivityForResult(intent, REQUEST_CODE_DEVICE_ADMIN)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}