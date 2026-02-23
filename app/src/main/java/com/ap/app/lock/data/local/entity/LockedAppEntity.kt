package com.ap.app.lock.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_apps")
data class LockedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val appIcon: ByteArray? = null,
    val isLocked: Boolean = false,
    val lockType: String = "4_digit_pin",
    val lockValue: String = "",
    val enableBiometric: Boolean = false,
    val lockedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LockedAppEntity
        if (packageName != other.packageName) return false
        if (appName != other.appName) return false
        if (appIcon != null) {
            if (other.appIcon == null) return false
            if (!appIcon.contentEquals(other.appIcon)) return false
        } else if (other.appIcon != null) return false
        if (isLocked != other.isLocked) return false
        if (lockType != other.lockType) return false
        if (lockValue != other.lockValue) return false
        if (enableBiometric != other.enableBiometric) return false
        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + appName.hashCode()
        result = 31 * result + (appIcon?.contentHashCode() ?: 0)
        result = 31 * result + isLocked.hashCode()
        result = 31 * result + lockType.hashCode()
        result = 31 * result + lockValue.hashCode()
        result = 31 * result + enableBiometric.hashCode()
        return result
    }
}