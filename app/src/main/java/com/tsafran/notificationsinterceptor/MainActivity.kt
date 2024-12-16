package com.tsafran.notificationsinterceptor

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.Manifest.permission.POST_NOTIFICATIONS
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.tsafran.notificationsinterceptor.room.AppDatabase
import com.tsafran.notificationsinterceptor.room.NotificationInfo
import com.tsafran.notificationsinterceptor.room.NotificationInfoDao
import com.tsafran.notificationsinterceptor.ui.theme.NotificationsInterceptorTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var notificationService: NotificationService
    private lateinit var notificationInfoDao: NotificationInfoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationService = NotificationService(this)
        notificationInfoDao = AppDatabase.getInstance(applicationContext).notificationInfoDao()

        val viewModel: MainViewModel by lazy {
            MainViewModel(notificationInfoDao)
        }

        notificationService.createNotificationChannel()
        getPermissions()

        enableEdgeToEdge()
        setContent {
            NotificationsInterceptorTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black,
                    floatingActionButton = {
                        CreateNotificationFAB(onClick = { notificationService.postNotification("Hello World!", "Test notification") })
                    }
                ) { innerPadding ->
                    NotificationsList(viewModel, innerPadding)
                }
            }
        }
    }

    fun getPermissions() {
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> }

        requestPostNotificationsPermissionIfNeeded()
        if (!isNotificationListenerPermissionGranted()) {
            redirectToSettings()
        }
    }

    fun redirectToSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        settingsLauncher.launch(intent)
    }

    fun isNotificationListenerPermissionGranted(): Boolean {
        val componentName = ComponentName(this, NotificationsListener::class.java)
        val enabledListeners =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(componentName.flattenToString()) == true
    }

    fun requestPostNotificationsPermissionIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(POST_NOTIFICATIONS), 1)
        }
    }

}

@Composable
fun CreateNotificationFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.border(
            BorderStroke(1.dp, Color.White),
            CircleShape
        ),
        contentColor = Color.White,
        containerColor = Color.Black,
        shape = CircleShape
    ) {
        Icon(Icons.Filled.Add, "Floating action button.")
    }
}

@Composable
fun NotificationsList(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val notifications = viewModel.notifications.collectAsState().value

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        LazyColumn(contentPadding = PaddingValues(12.dp)) {
            item {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    onClick = { viewModel.deleteAllNotifications() }
                ) {
                    Text("Clear All")
                }
            }
            items(notifications) { notification ->
                Notification(notification)
            }
        }
    }
}

@Composable
fun Notification(notificationInfo: NotificationInfo) {
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.White),
        modifier = Modifier
            .padding(bottom = 4.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = notificationInfo.packageName ?: "package name is empty")
            Text(text = notificationInfo.title ?: "title is empty")
            Text(text = notificationInfo.text ?: "text is empty")
        }
    }
}
