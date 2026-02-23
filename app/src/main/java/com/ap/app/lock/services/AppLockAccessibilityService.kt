package com.ap.app.lock.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.ap.app.lock.data.local.AppDatabase
import com.ap.app.lock.ui.unlock.UnlockActivity
import com.ap.app.lock.utils.UnlockManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class AppLockAccessibilityService : AccessibilityService() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private lateinit var database: AppDatabase
    private var currentForegroundApp: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventType = event?.eventType ?: return
        val packageName = event.packageName?.toString() ?: return

        // --- Tamper-Proofing Logic ---
        if (packageName == "com.android.settings" && (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)) {
            val className = event.className?.toString() ?: ""
            val eventText = event.text.joinToString(separator = "|").lowercase()

            scope.launch {
                val settings = database.settingsDao().getSettings() ?: return@launch

                val tamperDetectedMessage = when {
                    settings.disableAdminSettings && (className.contains("DeviceAdmin", true) || eventText.contains("device admin")) ->
                        "Device admin settings are protected."
                    settings.disableUsageStatsPage && (className.contains("UsageAccess", true) || eventText.contains("usage access") || eventText.contains("usage data access") || eventText.contains("apps with usage access")) ->
                        "Usage access settings are protected."
                    settings.disableAccessibilityPage && (className.contains("Accessibility", true) || eventText.contains("accessibility")) && !eventText.contains("app lock") ->
                        "Accessibility settings are protected."
                    settings.disableOverlayPage && (className.contains("Overlay", true) || className.contains("DrawOver", true) || eventText.contains("display over other apps") || eventText.contains("draw over other apps") || eventText.contains("appear on top")) ->
                        "Overlay settings are protected."
                    else -> null
                }

                tamperDetectedMessage?.let {
                    showToastAndGoHome(it)
                    return@launch
                }
            }
        }

        // --- App Lock Logic ---
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (packageName != currentForegroundApp) {
                currentForegroundApp = packageName

                if (packageName == this.packageName) {
                    return
                }

                scope.launch {
                    val isLocked = database.lockedAppDao().getLockedAppsFlow().first().any { it.packageName == packageName }
                    if (isLocked) {
                        val timeSinceUnlock = System.currentTimeMillis() - UnlockManager.unlockTimestamp
                        val isUnlockedRecently = UnlockManager.unlockedPackageName == packageName && timeSinceUnlock < UNLOCK_GRACE_PERIOD_MS

                        if (!isUnlockedRecently) {
                            val intent = Intent(this@AppLockAccessibilityService, UnlockActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra("package_name", packageName)
                            }
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    private fun showToastAndGoHome(message: String) {
        MainScope().launch {
            Toast.makeText(this@AppLockAccessibilityService, message, Toast.LENGTH_SHORT).show()
        }
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val UNLOCK_GRACE_PERIOD_MS: Long = 2000 // 2 seconds
    }
}