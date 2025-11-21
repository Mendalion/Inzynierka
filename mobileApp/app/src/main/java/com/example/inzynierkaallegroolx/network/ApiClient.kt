package com.example.inzynierkaallegroolx.network

import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.PATCH

interface StatsApi {
    @GET("stats/listings/views") suspend fun views(@Query("listingId") listingId: String): ViewsResponse
    @GET("stats/listings/sales") suspend fun sales(@Query("listingId") listingId: String): SalesResponse
    @POST("stats/view") suspend fun ingestView(@Body body: IngestBody): Map<String, Any>
    @POST("stats/sale") suspend fun ingestSale(@Body body: IngestBody): Map<String, Any>
}

data class ViewsResponse(val listingId: String, val views: List<ViewPoint>)
data class SalesResponse(val listingId: String, val sales: List<SalePoint>)
data class ViewPoint(val id: String, val timestamp: String, val count: Int)
data class SalePoint(val id: String, val timestamp: String, val count: Int)
data class IngestBody(val listingId: String)

interface ReportsApi {
    @POST("reports") suspend fun create(@Body body: ReportCreateBody): ReportDto
    @GET("reports/{id}/status") suspend fun status(@Path("id") id: String): ReportDto
}

data class ReportCreateBody(val type: String, val rangeStart: String, val rangeEnd: String)
data class ReportDto(val id: String, val userId: String, val type: String, val status: String, val filePath: String?)

interface UserApi {
    @GET("user/me") suspend fun me(): UserMeDto
    @PATCH("user/me") suspend fun update(@Body body: UserUpdateBody): Map<String, Any>
    @GET("user/me/export") suspend fun export(): Map<String, Any>
}

data class UserMeDto(val id: String, val email: String, val name: String?, val phone: String?)
data class UserUpdateBody(val name: String?, val phone: String?)

object ApiClient {
    private var tokenProvider: (() -> String?)? = null
    fun setTokenProvider(provider: () -> String?) { tokenProvider = provider }

    private val moshi = Moshi.Builder().build()
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor(Interceptor { chain ->
            val req = chain.request()
            val access = tokenProvider?.invoke()
            val newReq = if (access != null) req.newBuilder().addHeader("Authorization", "Bearer $access").build() else req
            chain.proceed(newReq)
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:4000/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(client)
        .build()

    val auth: AuthApi = retrofit.create(AuthApi::class.java)
    val listings: ListingsApi = retrofit.create(ListingsApi::class.java)
    val messages: MessagesApi = retrofit.create(MessagesApi::class.java)
    val stats: StatsApi = retrofit.create(StatsApi::class.java)
    val reports: ReportsApi = retrofit.create(ReportsApi::class.java)
    val user: UserApi = retrofit.create(UserApi::class.java)

    fun downloadReportFile(id: String): ByteArray? {
        val url = "http://10.0.2.2:4000/reports/$id/download"
        return try {
            val req = okhttp3.Request.Builder().url(url).get().build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) resp.body?.bytes() else null
        } catch (_: Exception) { null }
    }
}
