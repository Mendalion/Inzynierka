package com.example.inzynierkaallegroolx

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.inzynierkaallegroolx.ui.AppNavigation

class MainActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        val conversationId = intent.getStringExtra("conversationId")

        setContent {
            AppRoot(startConversationId = conversationId)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Wiadomości"
            val descriptionText = "Powiadomienia o nowych wiadomościach"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channelId = "default_channel_id"

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@androidx.compose.runtime.Composable
fun AppRoot(startConversationId: String? = null) {
    MaterialTheme {
        AppNavigation(startConversationId = startConversationId)
    }
}

@androidx.compose.ui.tooling.preview.Preview
@androidx.compose.runtime.Composable
fun PreviewApp() {
    AppRoot()
}