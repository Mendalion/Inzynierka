package com.example.inzynierkaallegroolx.data.auth
data class AuthState(
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
    val error: String? = null,
    val userId: String? = null
)
