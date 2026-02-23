package com.ap.app.lock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ap.app.lock.data.local.entity.LockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: LockedAppEntity)

    @Update
    suspend fun updateApp(app: LockedAppEntity)

    @Delete
    suspend fun deleteApp(app: LockedAppEntity)

    @Query("SELECT * FROM locked_apps WHERE packageName = :packageName")
    suspend fun getAppByPackage(packageName: String): LockedAppEntity?

    @Query("SELECT * FROM locked_apps WHERE isLocked = 1 ORDER BY appName ASC")
    fun getLockedAppsFlow(): Flow<List<LockedAppEntity>>

    @Query("SELECT * FROM locked_apps WHERE isLocked = 1")
    suspend fun getLockedApps(): List<LockedAppEntity>

    @Query("SELECT * FROM locked_apps")
    suspend fun getAllApps(): List<LockedAppEntity>

    @Query("SELECT * FROM locked_apps ORDER BY appName ASC")
    fun getAllAppsFlow(): Flow<List<LockedAppEntity>>

    @Query("UPDATE locked_apps SET isLocked = :isLocked WHERE packageName = :packageName")
    suspend fun updateLockStatus(packageName: String, isLocked: Boolean)

    @Query("SELECT COUNT(*) FROM locked_apps WHERE isLocked = 1")
    suspend fun getLockedAppCount(): Int

    @Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun deleteAppByPackage(packageName: String)
}