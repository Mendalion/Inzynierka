package com.example.inzynierkaallegroolx.repository

import com.example.inzynierkaallegroolx.data.auth.EncryptedTokenStore
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.Credentials
import com.example.inzynierkaallegroolx.network.RefreshBody
import com.example.inzynierkaallegroolx.network.BiometricLoginBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val store: EncryptedTokenStore) {
    suspend fun login(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp = ApiClient.auth.login(Credentials(email, password))
            store.saveAll(resp.accessToken, resp.refreshToken, resp.user?.id)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        val rt = store.refresh() ?: return@withContext Result.failure(IllegalStateException("No refresh"))
        return@withContext try {
            val resp = ApiClient.auth.refresh(RefreshBody(rt))
            store.saveAll(resp.accessToken, resp.refreshToken, store.userId())
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun biometric(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp = ApiClient.auth.biometric(BiometricLoginBody(userId))
            store.saveAll(resp.accessToken, resp.refreshToken, userId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun isLoggedIn() = store.access() != null

    fun logout() { store.clear() }
}
