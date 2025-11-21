package com.example.inzynierkaallegroolx.network

import retrofit2.http.Body
import retrofit2.http.POST

data class AuthResponse(val user: UserDto?, val accessToken: String, val refreshToken: String)
data class UserDto(val id: String, val email: String)

data class Credentials(val email: String, val password: String)
data class RefreshBody(val refreshToken: String)
data class BiometricLoginBody(val userId: String)
data class DeviceRegisterBody(val token: String)

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: Credentials): AuthResponse
    @POST("auth/login")
    suspend fun login(@Body body: Credentials): AuthResponse
    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshBody): AuthResponse
    @POST("auth/login/biometric")
    suspend fun biometric(@Body body: BiometricLoginBody): AuthResponse
    @POST("devices/register")
    suspend fun registerDevice(@Body body: DeviceRegisterBody): Map<String, Any>
}
