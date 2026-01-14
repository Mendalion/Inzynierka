package com.example.inzynierkaallegroolx.fcm

import android.app.PendingIntent
import android.content.Intent
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
import com.example.inzynierkaallegroolx.MainActivity
import com.example.inzynierkaallegroolx.R
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import kotlin.random.Random

class AppFirebaseMessagingService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Odebrano wiadomość: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: "Nowa wiadomość"
        val body = message.notification?.body ?: message.data["body"] ?: "Kliknij, aby zobaczyć."
        val conversationId = message.data["conversationId"]

        showNotification(title, body, conversationId)
        triggerAppRefresh()
    }

    private fun showNotification(title: String, body: String, conversationId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("conversationId", conversationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel_id"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        try {
            NotificationManagerCompat.from(this).notify(Random.nextInt(), notificationBuilder.build())
        } catch (e: SecurityException) {
            Log.e("FCM", "Brak uprawnień do powiadomień")
        }
    }

    private fun triggerAppRefresh() {
        val intent = Intent("com.example.inzynierkaallegroolx.REFRESH_MESSAGES")
        sendBroadcast(intent)
    }

    override fun onNewToken(token: String) {
        Log.d("FCM","New token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { ApiClient.auth.registerDevice(DeviceRegisterBody(token)) }
        }
    }
}
