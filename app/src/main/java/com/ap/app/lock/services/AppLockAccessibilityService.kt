package com.ap.app.lock.services

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.ap.app.lock.data.local.AppDatabase
import com.ap.app.lock.data.local.entity.SettingsEntity
import com.ap.app.lock.ui.unlock.UnlockActivity
import com.ap.app.lock.utils.UnlockManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class AppLockAccessibilityService : AccessibilityService() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private lateinit var database: AppDatabase
    private var currentForegroundApp: String? = null

    // Track actual user app, not share menu or system UI
    private var lastUserApp: String? = null
    private var lastActivityChangeTime: Long = 0
    private val activityDebounceMs = 500L  // Renamed to follow naming convention

    // Cache to avoid repeated lookups
    private val appNameCache = mutableMapOf<String, String>()
    private val unlockedAppsCache = mutableMapOf<String, Long>()

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                scope.launch {
                    val currentSettings = database.settingsDao().getSettings()
                    if (currentSettings?.relockPolicy == "screen_off") {
                        UnlockManager.unlockedPackageName = null
                        unlockedAppsCache.clear()
                    }
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventType = event?.eventType ?: return
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""

        // --- Navigation Blocking Logic (Protect Lock Screen) ---
        if (UnlockManager.isLockShowing) {
            handleNavigationWhileLocking(packageName, className)
            return
        }

        // --- Tamper-Proofing Logic (Settings Protection) ---
        if (packageName == "com.android.settings" &&
            (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                    eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)) {
            handleSettingsTampering(event, className)
            return
        }

        // --- App Lock Logic (Main locking mechanism) ---
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            handleAppSwitching(packageName, className)
        }
    }

    /**
     * Detect app switches, filtering out share menus and system overlays
     * Only track real user apps
     */
    private fun handleAppSwitching(packageName: String, className: String) {
        // Ignore system packages, keyboards, and share menus
        if (shouldIgnorePackageForLocking(packageName, className)) {
            return
        }

        // Check if this is a share menu or chooser dialog
        if (isShareMenuOrChooser(packageName, className)) {
            // Don't update currentForegroundApp for share menu
            // Just return - user is still in the previous app
            return
        }

        // Same app = internal navigation
        if (packageName == currentForegroundApp) {
            val timeSinceLastChange = System.currentTimeMillis() - lastActivityChangeTime

            if (timeSinceLastChange < activityDebounceMs) {
                return
            }

            lastActivityChangeTime = System.currentTimeMillis()
            return
        }

        // Different app = real app switch
        currentForegroundApp = packageName
        lastUserApp = packageName  // Track real user app
        lastActivityChangeTime = System.currentTimeMillis()

        // Reset lock flag if navigating to our own app
        if (packageName == this.packageName) {
            if (isOurOwnActivity(className)) {
                UnlockManager.isLockShowing = false
            }
            return
        }

        // Handle launcher/home navigation
        if (isLauncherPackage(packageName)) {
            UnlockManager.isLockShowing = false

            scope.launch {
                val currentSettings = database.settingsDao().getSettings()
                if (currentSettings?.relockPolicy == "immediately") {
                    UnlockManager.unlockedPackageName = null
                    unlockedAppsCache.clear()
                }
            }
            return
        }

        // Check if app is locked and trigger unlock screen
        scope.launch {
            val currentSettings = database.settingsDao().getSettings() ?: return@launch
            val lockedApps = database.lockedAppDao().getLockedAppsFlow().first()
            val isLocked = lockedApps.any { it.packageName == packageName }

            if (isLocked) {
                checkAndShowLockScreen(packageName, currentSettings)
            }
        }
    }

    /**
     * Detect if this is a share menu, chooser, or other system dialog
     * These should NOT trigger lock
     */
    private fun isShareMenuOrChooser(packageName: String, className: String): Boolean {
        // Known share menu packages
        if (isSystemShareMenu(packageName)) {
            return true
        }

        // Common chooser/dialog class names
        val chooserClasses = listOf(
            "ChooserActivity",
            "ResolverActivity",
            "ShareActivity",
            "SendActivity",
            "ShareCompat",
            "IntentResolver"
        )

        return chooserClasses.any {
            className.contains(it, ignoreCase = true)
        }
    }

    /**
     * Check if package is a known system share menu
     */
    private fun isSystemShareMenu(packageName: String): Boolean {
        val systemMenus = listOf(
            "com.android.systemui",
            "android",
            "com.android.intentresolver",
            "com.google.android.gms",
            "com.google.android.googlequicksearchbox"
        )
        return systemMenus.contains(packageName)
    }

    /**
     * Handle navigation attempts while lock screen is visible
     */
    private fun handleNavigationWhileLocking(packageName: String, className: String) {
        val isSystemUi = packageName == "com.android.systemui"
        val isLauncher = isLauncherPackage(packageName)
        val isRecentsOrOverview = className.contains("Recents", ignoreCase = true) ||
                className.contains("Overview", ignoreCase = true) ||
                className.contains("RecentApps", ignoreCase = true)

        if (isSystemUi || (isLauncher && isRecentsOrOverview)) {
            val timeSinceUnlock = System.currentTimeMillis() - UnlockManager.unlockTimestamp
            if (timeSinceUnlock > 1000) {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }

    /**
     * Detect and block access to sensitive settings
     */
    private fun handleSettingsTampering(event: AccessibilityEvent, className: String) {
        val eventText = event.text.joinToString(separator = "|").lowercase()

        scope.launch {
            val settings = database.settingsDao().getSettings() ?: return@launch

            val tamperDetectedMessage = when {
                settings.disableAdminSettings &&
                        (className.contains("DeviceAdmin", true) || eventText.contains("device admin")) ->
                    "Device admin settings are protected."

                settings.disableUsageStatsPage &&
                        (className.contains("UsageAccess", true) || eventText.contains("usage access") ||
                                eventText.contains("usage data access") || eventText.contains("apps with usage access")) ->
                    "Usage access settings are protected."

                settings.disableAccessibilityPage &&
                        (className.contains("Accessibility", true) || eventText.contains("accessibility")) &&
                        !eventText.contains("app lock") ->
                    "Accessibility settings are protected."

                settings.disableOverlayPage &&
                        (className.contains("Overlay", true) || className.contains("DrawOver", true) ||
                                eventText.contains("display over other apps") || eventText.contains("draw over other apps") ||
                                eventText.contains("appear on top")) ->
                    "Overlay settings are protected."

                else -> null
            }

            tamperDetectedMessage?.let {
                showToastAndGoHome(it)
                return@launch
            }
        }
    }

    /**
     * Check if lock screen should be shown based on relock policy
     */
    private fun checkAndShowLockScreen(
        packageName: String,
        settings: SettingsEntity
    ) {
        if (UnlockManager.isLockShowing) {
            return
        }

        // Check if app was recently unlocked
        val lastUnlockTime = unlockedAppsCache[packageName] ?: 0L
        val timeSinceUnlock = System.currentTimeMillis() - lastUnlockTime

        val relockTimeMs = when (settings.relockPolicy) {
            "5s" -> 5000L
            "10s" -> 10000L
            "30s" -> 30000L
            "60s" -> 60000L
            "screen_off" -> Long.MAX_VALUE
            else -> 0L
        }

        val isUnlockedRecently = lastUnlockTime > 0 && timeSinceUnlock < relockTimeMs

        // Check for recent cancellation
        val timeSinceCancel = System.currentTimeMillis() - UnlockManager.lastCancelTimestamp
        val isRecentCancel = timeSinceCancel < 1500

        if (!isUnlockedRecently && !isRecentCancel) {
            scope.launch {
                showLockScreen(packageName)
            }
        }
    }

    /**
     * Display the unlock screen
     */
    private suspend fun showLockScreen(packageName: String) {
        UnlockManager.isLockShowing = true

        val appName = getAppName(packageName)

        val intent = Intent(this@AppLockAccessibilityService, UnlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            putExtra("package_name", packageName)
            putExtra("app_name", appName)
        }
        startActivity(intent)
    }

    /**
     * Get app name with caching
     */
    private suspend fun getAppName(packageName: String): String {
        return appNameCache.getOrPut(packageName) {
            try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (_: Exception) {
                packageName
            }
        }
    }

    /**
     * Check if package should be ignored
     */
    private fun shouldIgnorePackageForLocking(packageName: String, className: String): Boolean {
        val systemPackages = listOf(
            "com.android.systemui",
            "android",
            "com.google.android.permissioncontroller",
            "com.android.permissioncontroller",
            "com.android.launcher",
            "com.android.launcher3"
        )

        if (systemPackages.contains(packageName) ||
            packageName.contains("inputmethod", ignoreCase = true) ||
            packageName.contains("keyboard", ignoreCase = true)) {
            return true
        }

        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Check if this is our own activity
     */
    private fun isOurOwnActivity(className: String): Boolean {
        return className.contains("MainActivity") ||
                className.contains("SettingsActivity") ||
                className.contains("PermissionsActivity") ||
                className.contains("OnboardingActivity")
    }

    /**
     * Check if package is the default launcher
     */
    private fun isLauncherPackage(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return packageName == resolveInfo?.activityInfo?.packageName
    }

    /**
     * Show toast and go home
     */
    private fun showToastAndGoHome(message: String) {
        MainScope().launch {
            Toast.makeText(this@AppLockAccessibilityService, message, Toast.LENGTH_SHORT).show()
        }
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    /**
     * Update unlock cache when an app is unlocked
     */
    fun updateUnlockCache(packageName: String) {
        unlockedAppsCache[packageName] = System.currentTimeMillis()
    }

    override fun onInterrupt() {}

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenOffReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(screenOffReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (_: Exception) {}
        job.cancel()
    }

    companion object {
        private const val SERVICE_TAG = "AppLockAccessibilityService"
    }
}