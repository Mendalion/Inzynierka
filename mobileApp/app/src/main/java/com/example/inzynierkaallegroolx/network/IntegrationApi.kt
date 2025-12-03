package com.example.inzynierkaallegroolx.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class OAuthStartResponse(val url: String)
data class OAuthCallbackBody(val code: String, val state: String)

interface IntegrationApi {
    @GET("integrations/oauth/{platform}/start")
    suspend fun startAuth(@Path("platform") platform: String): OAuthStartResponse

    @POST("integrations/oauth/{platform}/callback")
    suspend fun sendCallback(
        @Path("platform") platform: String,
        @Body body: OAuthCallbackBody
    ): Any
}