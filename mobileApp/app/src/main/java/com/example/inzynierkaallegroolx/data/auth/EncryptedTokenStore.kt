package com.example.inzynierkaallegroolx.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
class EncryptedTokenStore(context: Context) {
    private val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(access: String, refresh: String) {
        prefs.edit().putString("access", access).putString("refresh", refresh).apply()
    }
    fun saveAll(access: String, refresh: String, userId: String?) {
        prefs.edit().putString("access", access).putString("refresh", refresh).apply()
        userId?.let { prefs.edit().putString("userId", it).apply() }
    }
    fun access(): String? = prefs.getString("access", null)
    fun refresh(): String? = prefs.getString("refresh", null)
    fun userId(): String? = prefs.getString("userId", null)
    fun clear() { prefs.edit().clear().apply() }
}
