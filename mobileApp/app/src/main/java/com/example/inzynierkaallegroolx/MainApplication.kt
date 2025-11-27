package com.example.inzynierkaallegroolx

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.inzynierkaallegroolx.work.ListingsSyncWorker
import com.example.inzynierkaallegroolx.work.MessagesSyncWorker
import com.example.inzynierkaallegroolx.work.StatsSyncWorker
//import com.example.inzynierkaallegroolx.work.CombinedSyncWorker
import java.util.concurrent.TimeUnit

private const val NOTIFICATION_CHANNEL_ID = "default"
private const val NOTIFICATION_CHANNEL_NAME = "Notifications"
private const val WORK_LISTINGS = "work_listings_sync"
private const val WORK_MESSAGES = "work_messages_sync"
private const val WORK_STATS = "work_stats_sync"

class MainApplication: Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleWorkers()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun scheduleWorkers() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val listings = PeriodicWorkRequestBuilder<ListingsSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        val messages = PeriodicWorkRequestBuilder<MessagesSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        val stats = PeriodicWorkRequestBuilder<StatsSyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
//        val combined = PeriodicWorkRequestBuilder<CombinedSyncWorker>(30, TimeUnit.MINUTES)
//            .setConstraints(constraints)
//            .build()
        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(WORK_LISTINGS, ExistingPeriodicWorkPolicy.UPDATE, listings)
        workManager.enqueueUniquePeriodicWork(WORK_MESSAGES, ExistingPeriodicWorkPolicy.UPDATE, messages)
        workManager.enqueueUniquePeriodicWork(WORK_STATS, ExistingPeriodicWorkPolicy.UPDATE, stats)
        //workManager.enqueueUniquePeriodicWork("work_combined_sync", ExistingPeriodicWorkPolicy.UPDATE, combined)
    }

    override val workManagerConfiguration: Configuration =
        Configuration.Builder().setMinimumLoggingLevel(android.util.Log.INFO).build()
}
