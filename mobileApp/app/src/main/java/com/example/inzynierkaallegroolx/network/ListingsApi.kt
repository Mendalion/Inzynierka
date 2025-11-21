package com.example.inzynierkaallegroolx.network

import retrofit2.http.*

data class ListingDto(val id: String, val title: String, val description: String, val price: String, val status: String)
data class ListingCreateBody(val title: String, val description: String, val price: Double)
data class ListingUpdateBody(val title: String? = null, val description: String? = null, val price: Double? = null, val status: String? = null)

interface ListingsApi {
    @GET("listings")
    suspend fun getListings(): List<ListingDto>

    @POST("listings")
    suspend fun create(@Body body: ListingCreateBody): ListingDto

    @PATCH("listings/{id}")
    suspend fun update(@Path("id") id: String, @Body body: ListingUpdateBody): ListingDto

    @DELETE("listings/{id}")
    suspend fun delete(@Path("id") id: String): Map<String, Any>

    @POST("listings/{id}/archive")
    suspend fun archive(@Path("id") id: String): ListingDto

    @GET("listings/{id}")
    suspend fun get(@Path("id") id: String): ListingDto
}
