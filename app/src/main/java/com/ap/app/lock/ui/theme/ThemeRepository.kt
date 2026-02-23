package com.ap.app.lock.ui.theme

import android.content.Context
import com.ap.app.lock.data.local.AppDatabase
import com.ap.app.lock.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.first

class ThemeRepository(context: Context) {

    private val settingsDao = AppDatabase.getInstance(context).settingsDao()

    suspend fun getThemeSettings(): SettingsEntity {
        return settingsDao.getSettingsFlow().first() ?: SettingsEntity()
    }
}