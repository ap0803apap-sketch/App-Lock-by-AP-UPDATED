package com.ap.app.lock.utils

object UnlockManager {
    var unlockedPackageName: String? = null
    var unlockTimestamp: Long = 0
    var lastCancelTimestamp: Long = 0
    var exitedPackageName: String? = null
    var isLockShowing: Boolean = false

    fun recordUnlock(packageName: String) {
        unlockedPackageName = packageName
        unlockTimestamp = System.currentTimeMillis()
        exitedPackageName = null
        isLockShowing = false
    }

    fun recordCancel(packageName: String) {
        lastCancelTimestamp = System.currentTimeMillis()
        exitedPackageName = packageName
        isLockShowing = false
    }
}
