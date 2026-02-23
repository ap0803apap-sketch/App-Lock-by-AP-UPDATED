package com.ap.app.lock

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ap.app.lock.data.local.AppDatabase
import com.ap.app.lock.services.AppMonitoringService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppLockApplication : Application() {

    companion object {
        lateinit var instance: AppLockApplication
            private set

        fun getContext(): Context = instance
        fun getDatabase(): AppDatabase = AppDatabase.getInstance(instance)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize database
        AppDatabase.getInstance(this)

        // Start monitoring service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, AppMonitoringService::class.java))
        } else {
            startService(Intent(this, AppMonitoringService::class.java))
        }
    }
}