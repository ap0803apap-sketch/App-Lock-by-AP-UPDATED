package com.ap.app.lock.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ap.app.lock.data.local.dao.LockedAppDao
import com.ap.app.lock.data.local.dao.SettingsDao
import com.ap.app.lock.data.local.entity.LockedAppEntity
import com.ap.app.lock.data.local.entity.SettingsEntity

@Database(
    entities = [LockedAppEntity::class, SettingsEntity::class],
    version = 3, // Incremented version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lockedAppDao(): LockedAppDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "applock_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Added migrations
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration from 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN appLockType TEXT NOT NULL DEFAULT ''")
            }
        }

        // Migration from 2 to 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN appLockTypeValue TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
