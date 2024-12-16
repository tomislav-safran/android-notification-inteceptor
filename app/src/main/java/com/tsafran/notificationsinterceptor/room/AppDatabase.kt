package com.tsafran.notificationsinterceptor.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NotificationInfo::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationInfoDao(): NotificationInfoDao

    // singleton pattern to store only one instance of db
    companion object {
        private const val Database_NAME = "notification_info.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, Database_NAME,
                    ).build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}