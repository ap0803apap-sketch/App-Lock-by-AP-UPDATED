package com.ap.app.lock.utils

object UnlockManager {
    var unlockedPackageName: String? = null
    var unlockTimestamp: Long = 0

    fun recordUnlock(packageName: String) {
        unlockedPackageName = packageName
        unlockTimestamp = System.currentTimeMillis()
    }
}
