package com.example.inzynierkaallegroolx.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.inzynierkaallegroolx.repository.ListingsRepository

class ListingsSyncWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    private val repo = ListingsRepository()
    override suspend fun doWork(): Result {
        return if (repo.fetch().isSuccess) Result.success() else Result.retry()
    }
}
