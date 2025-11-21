package com.example.inzynierkaallegroolx.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.inzynierkaallegroolx.data.auth.EncryptedTokenStore
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.RefreshBody

class RefreshTokenWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val store = EncryptedTokenStore(applicationContext)
        val refresh = store.refresh() ?: return Result.success()
        return try {
            val resp = ApiClient.auth.refresh(RefreshBody(refreshToken = refresh))
            store.save(resp.accessToken, resp.refreshToken)
            Result.success()
        } catch (e: Exception) { Result.retry() }
    }
}
