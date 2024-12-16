package com.tsafran.notificationsinterceptor

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.tsafran.notificationsinterceptor.room.AppDatabase
import com.tsafran.notificationsinterceptor.room.NotificationInfo

class NotificationsListener : NotificationListenerService() {
    private var componentName: ComponentName? = null
    private var viewModel: MainViewModel? = null

    override fun onCreate() {
        super.onCreate()
        initialiseViewModel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if(componentName == null) {
            componentName = ComponentName(this, this::class.java)
        }

        componentName?.let {
            requestRebind(it)
            toggleNotificationListenerService(it)
        }
        return START_REDELIVER_INTENT
    }

    private fun toggleNotificationListenerService(componentName: ComponentName) {
        val pm = packageManager
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onListenerConnected(){
        Log.d("NotificationListener","onListenerConnected")
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        if (componentName == null) {
            componentName = ComponentName(this, this::class.java)
        }

        componentName?.let { requestRebind(it) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: ""
        val extras = sbn?.notification?.extras

        // Extract data from notification extras as needed
        val title = extras?.getCharSequence("android.title").toString()
        val text = extras?.getCharSequence("android.text").toString()

        viewModel?.insertNotification(NotificationInfo(title = title, text = text, packageName = packageName))
    }

    private fun initialiseViewModel() {
        if (viewModel == null) {
            val notificationInfoDao = AppDatabase.getInstance(applicationContext).notificationInfoDao()

            val newViewModel: MainViewModel by lazy {
                MainViewModel(notificationInfoDao)
            }

            viewModel = newViewModel
        }
    }
}