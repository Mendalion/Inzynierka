package com.example.inzynierkaallegroolx.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.DeviceRegisterBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class AppFirebaseMessagingService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM","Msg received: ${message.data}")
        val title = message.data["title"] ?: "Update"
        val body = message.data["body"] ?: message.data.toString()
        val builder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= 33) {
            val perm = ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
            if (perm != PackageManager.PERMISSION_GRANTED) {
                Log.w("FCM","Notification skipped - no POST_NOTIFICATIONS permission")
                return
            }
        }
        NotificationManagerCompat.from(this).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
    }

    override fun onNewToken(token: String) {
        Log.d("FCM","New token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { ApiClient.auth.registerDevice(DeviceRegisterBody(token)) }
        }
    }
}
