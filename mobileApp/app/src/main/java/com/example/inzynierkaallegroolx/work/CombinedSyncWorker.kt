package com.example.inzynierkaallegroolx.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.inzynierkaallegroolx.repository.ListingsLocalRepository
import com.example.inzynierkaallegroolx.repository.MessagesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CombinedSyncWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val listingsRepo = ListingsLocalRepository(applicationContext)
        val messagesRepo = MessagesRepository(applicationContext)
        // Future: call remote sync endpoints then update local
        runCatching { messagesRepo.syncConversations() }
        // listings update is already via dedicated worker; placeholder combine
        Result.success()
    }
}
