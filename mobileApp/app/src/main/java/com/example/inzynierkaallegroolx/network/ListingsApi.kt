package com.example.inzynierkaallegroolx.network

import okhttp3.MultipartBody
import retrofit2.http.*

data class ListingDto(
    val id: String,
    val title: String,
    val description: String?,
    val price: String?,
    val status: String?,
    val platformStates: List<PlatformStateDto>?,
    val images: List<ListingImageDto>?,
    val externalDetails: ExternalDetailsDto? = null
)

data class ExternalDetailsDto(
    val allegro: AllegroExternalDto?
)

data class AllegroExternalDto(
    val id: String,
    val status: String,
    val price: String,
    val stock: Int,
    val webUrl: String
)

data class PlatformStateDto(
    val platform: String,
    val status: String
)

data class ListingImageDto(
    val id: String,
    val url: String
)

// --- ZMIANA TUTAJ ---
data class ListingCreateBody(
    val title: String,
    val description: String,
    val price: Double,
    val categoryId: String,
    val platform: String, // Było: val platforms: List<String>
    val parameterValues: Map<String, String>
)

data class CategoryParameterDto(
    val id: String,
    val name: String,
    val type: String,
    val required: Boolean,
    val unit: String?,
    val dictionary: List<DictionaryItemDto>?
)

data class DictionaryItemDto(
    val id: String,
    val value: String
)

data class ListingUpdateBody(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null
)

interface ListingsApi {
    @GET("listings")
    suspend fun getListings(): List<ListingDto>

    @GET("listings/{id}")
    suspend fun get(@Path("id") id: String): ListingDto

    @POST("listings")
    suspend fun create(@Body body: ListingCreateBody): ListingDto

    @PATCH("listings/{id}")
    suspend fun update(@Path("id") id: String, @Body body: ListingUpdateBody): ListingDto

    @DELETE("listings/{id}")
    suspend fun delete(@Path("id") id: String): Unit

    @DELETE("listings/{id}/images/{imageId}")
    suspend fun deleteImage(
        @Path("id") listingId: String,
        @Path("imageId") imageId: String
    ): Unit

    @POST("listings/{id}/archive")
    suspend fun archive(@Path("id") id: String): Any

    @GET("/listings/categories/{categoryId}/parameters")
    suspend fun getCategoryParameters(@Path("categoryId") categoryId: String): List<CategoryParameterDto>

    @Multipart
    @POST("listings/{id}/images/upload")
    suspend fun uploadImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): ListingImageDto
}