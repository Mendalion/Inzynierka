package com.example.inzynierkaallegroolx.repository

import com.example.inzynierkaallegroolx.data.auth.EncryptedTokenStore
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.Credentials
import com.example.inzynierkaallegroolx.network.RefreshBody
import com.example.inzynierkaallegroolx.network.BiometricLoginBody
import com.example.inzynierkaallegroolx.network.RegisterBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val store: EncryptedTokenStore) {
    suspend fun register(email: String, password: String, name: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp = ApiClient.auth.register(RegisterBody(email, password, name))
            val uid = resp.user?.id ?: runCatching { ApiClient.user.me().id }.getOrNull()
            store.saveAll(resp.accessToken, resp.refreshToken, uid)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun login(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp = ApiClient.auth.login(Credentials(email, password))
            val uid = resp.user?.id ?: runCatching { ApiClient.user.me().id }.getOrNull()
            store.saveAll(resp.accessToken, resp.refreshToken, uid)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        val rt = store.refresh() ?: return@withContext Result.failure(IllegalStateException("No refresh"))
        return@withContext try {
            val resp = ApiClient.auth.refresh(RefreshBody(rt))
            val uid = store.userId() ?: runCatching { ApiClient.user.me().id }.getOrNull()
            store.saveAll(resp.accessToken, resp.refreshToken, uid)
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

    fun getStoredUserId() = store.userId()
}
