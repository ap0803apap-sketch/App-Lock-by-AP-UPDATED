package com.ap.app.lock.data.repository

import com.ap.app.lock.data.local.dao.LockedAppDao
import com.ap.app.lock.data.local.entity.LockedAppEntity
import kotlinx.coroutines.flow.Flow

class LockedAppRepository(private val lockedAppDao: LockedAppDao) {

    fun getLockedApps(): Flow<List<LockedAppEntity>> = lockedAppDao.getLockedAppsFlow()

    suspend fun insert(lockedApp: LockedAppEntity) = lockedAppDao.insertApp(lockedApp)

    suspend fun delete(packageName: String) = lockedAppDao.deleteAppByPackage(packageName)

    suspend fun toggleAppLock(packageName: String, isLocked: Boolean) {
        if (isLocked) {
            insert(LockedAppEntity(packageName = packageName, appName = "", isLocked = true))
        } else {
            delete(packageName)
        }
    }
}