package com.ap.app.lock.domain.usecases

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.ap.app.lock.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GetInstalledAppsUseCase(private val context: Context) {

    fun execute(includeSystemApps: Boolean): Flow<List<AppInfo>> = flow {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appInfoList = packages.mapNotNull { app ->
            if (!includeSystemApps && (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                return@mapNotNull null
            }
            AppInfo(
                appName = app.loadLabel(packageManager).toString(),
                packageName = app.packageName,
                icon = app.loadIcon(packageManager)
            )
        }
        emit(appInfoList)
    }.flowOn(Dispatchers.IO)
}