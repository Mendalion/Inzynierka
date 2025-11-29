package com.example.inzynierkaallegroolx.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class StatsSyncWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        // TODO: implement stats sync
        return Result.success()
    }
}
