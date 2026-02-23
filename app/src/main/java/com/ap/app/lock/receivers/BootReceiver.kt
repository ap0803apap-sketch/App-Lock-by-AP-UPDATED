package com.ap.app.lock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ap.app.lock.services.AppMonitoringService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, AppMonitoringService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
