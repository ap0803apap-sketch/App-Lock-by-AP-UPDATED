package com.ap.app.lock.data.repository

import com.ap.app.lock.data.local.dao.LockedAppDao
import com.ap.app.lock.data.local.entity.LockedAppEntity
import kotlinx.coroutines.flow.Flow

class LockedAppsRepository(private val lockedAppDao: LockedAppDao) {

    fun getAllAppsFlow(): Flow<List<LockedAppEntity>> = lockedAppDao.getAllAppsFlow()

    fun getLockedAppsFlow(): Flow<List<LockedAppEntity>> = lockedAppDao.getLockedAppsFlow()

    suspend fun addApp(app: LockedAppEntity) = lockedAppDao.insertApp(app)

    suspend fun updateApp(app: LockedAppEntity) = lockedAppDao.updateApp(app)

    suspend fun deleteApp(packageName: String) = lockedAppDao.deleteAppByPackage(packageName)

    suspend fun toggleAppLock(packageName: String, isLocked: Boolean) =
        lockedAppDao.updateLockStatus(packageName, isLocked)

    suspend fun getAppByPackage(packageName: String): LockedAppEntity? =
        lockedAppDao.getAppByPackage(packageName)

    suspend fun getLockedApps(): List<LockedAppEntity> = lockedAppDao.getLockedApps()

    suspend fun getLockedAppCount(): Int = lockedAppDao.getLockedAppCount()
}