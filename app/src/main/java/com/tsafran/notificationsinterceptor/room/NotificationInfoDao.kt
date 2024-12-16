package com.tsafran.notificationsinterceptor.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationInfoDao {
    @Query("SELECT * FROM notificationinfo")
    fun getAll(): Flow<List<NotificationInfo>>

    @Insert
    fun insert(notificationInfo: NotificationInfo)

    @Query("DELETE FROM notificationinfo")
    fun deleteAll()
}