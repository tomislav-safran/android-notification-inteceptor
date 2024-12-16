package com.tsafran.notificationsinterceptor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsafran.notificationsinterceptor.room.NotificationInfo
import com.tsafran.notificationsinterceptor.room.NotificationInfoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val notificationInfoDao: NotificationInfoDao) : ViewModel() {
    private val _notifications =
        MutableStateFlow<List<NotificationInfo>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            notificationInfoDao.getAll().collect { notificationList ->
                _notifications.value = notificationList
            }
        }
    }

    fun insertNotification(notificationInfo: NotificationInfo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                notificationInfoDao.insert(notificationInfo)
            }
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                notificationInfoDao.deleteAll()
            }
        }
    }
}