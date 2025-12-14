package com.example.inzynierkaallegroolx.repository

import android.content.Context
import android.net.Uri
import com.example.inzynierkaallegroolx.Config
import com.example.inzynierkaallegroolx.data.AppDatabase
import com.example.inzynierkaallegroolx.data.listings.ListingEntity
import com.example.inzynierkaallegroolx.network.*
import com.example.inzynierkaallegroolx.ui.model.AllegroDetailsUi
import com.example.inzynierkaallegroolx.ui.model.ListingImageUi
import com.example.inzynierkaallegroolx.ui.model.ListingItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.FileOutputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ListingsRepository(private val context: Context) {

    private val listingDao = AppDatabase.getDatabase(context).listingDao()

    suspend fun getCategoryParameters(categoryId: String): List<CategoryParameterDto> {
        return ApiClient.listings.getCategoryParameters(categoryId)
    }

    suspend fun createListing(
        title: String,
        description: String,
        price: Double,
        platform: String,
        photos: List<Uri>,
        categoryId: String,
        parameterValues: Map<String, String>
    ) = withContext(Dispatchers.IO) {
        try {
            //tworzymy ogłoszenie
            val body = ListingCreateBody(
                title = title,
                description = description,
                price = price,
                platform = platform,
                categoryId = categoryId,
                parameterValues = parameterValues
            )
            val createdDto = ApiClient.listings.create(body)

            if (photos.isNotEmpty()) {
                uploadPhotos(createdDto.id, photos)
            }

            val entity = mapDtoToEntity(createdDto)
            listingDao.upsertAll(listOf(entity))

            createdDto
        } catch (e: Exception) {
            throw normalizeError(e)
        }
    }

    private fun mapEntityToUi(entity: ListingEntity): ListingItemUi {
        val attrs: Map<String, String> = try {
            if (entity.attributesJson != null) {
                mapAdapter.fromJson(entity.attributesJson) ?: emptyMap()
            } else emptyMap()
        } catch (e: Exception) { emptyMap() }

        return ListingItemUi(
            id = entity.id,
            title = entity.title,
            price = entity.price,
            status = entity.status,
            categoryId = entity.categoryId,
            categoryName = null,
            platforms = if (entity.platforms.isNotEmpty()) entity.platforms.split(",") else emptyList(),
            thumbnailUrl = entity.thumbnailUrl,
            description = entity.description,
            attributes = attrs,
            allImages = emptyList(),
            allegroDetails = null
        )
    }

    private fun mapDtoToEntity(dto: ListingDto): ListingEntity {
        val platformsStr = dto.platformStates?.joinToString(",") { it.platform } ?: ""
        val rawUrl = dto.images?.firstOrNull()?.url
        val thumb = Config.imageUrl(rawUrl)

        val attrsJson = if (dto.attributes != null) {
            val stringMap = dto.attributes.entries.associate { it.key to it.value.toString() }
            mapAdapter.toJson(stringMap)
        } else null

        return ListingEntity(
            id = dto.id,
            title = dto.title,
            description = dto.description ?: "",
            price = dto.price ?: "0.00",
            status = dto.status ?: "UNKNOWN",
            categoryId = dto.categoryId,
            categoryName = dto.categoryName,
            thumbnailUrl = thumb,
            platforms = platformsStr,
            attributesJson = attrsJson
        )
    }

    suspend fun fetchAll(): Result<List<ListingItemUi>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resultDto = ApiClient.listings.getListings()
            val entities = resultDto.map { mapDtoToEntity(it) }
            listingDao.upsertAll(entities)
            val uiList = entities.map { mapEntityToUi(it) }
            Result.success(uiList)
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                val localEntities = listingDao.getAll()
                if (localEntities.isNotEmpty()) {
                    val uiList = localEntities.map { mapEntityToUi(it) }
                    Result.success(uiList)
                } else {
                    Result.failure(Exception("Brak połączenia i brak danych offline"))
                }
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchDetails(id: String): Result<ListingItemUi> = withContext(Dispatchers.IO) {
        return@withContext try {
            val dto = ApiClient.listings.get(id)

            //zapis podstawowych danych do cache, Entity nie przechowuje dynamicznych danych Allegro
            val entity = mapDtoToEntity(dto)
            listingDao.upsertAll(listOf(entity))

            val allImagesUi = dto.images?.map {
                ListingImageUi(it.id, Config.imageUrl(it.url) ?: "")
            } ?: emptyList()

            val allegroDetailsUi = dto.externalDetails?.allegro?.let { allegroDto ->
                AllegroDetailsUi(
                    id = allegroDto.id,
                    status = allegroDto.status,
                    price = allegroDto.price,
                    stock = allegroDto.stock,
                    webUrl = allegroDto.webUrl
                )
            }

            val attrs = dto.attributes?.entries?.associate { it.key to it.value.toString() } ?: emptyMap()

            val uiModel = ListingItemUi(
                id = dto.id,
                title = dto.title,
                price = dto.price ?: "0.00",
                status = dto.status ?: "UNKNOWN",
                categoryId = dto.categoryId,
                categoryName = dto.categoryName,
                platforms = dto.platformStates?.map { it.platform } ?: emptyList(),
                thumbnailUrl = entity.thumbnailUrl,
                description = dto.description ?: "",
                attributes = attrs,
                allImages = allImagesUi,
                allegroDetails = allegroDetailsUi
            )
            Result.success(uiModel)
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                val localEntity = listingDao.getById(id)
                if (localEntity != null) {
                    val uiModel = mapEntityToUi(localEntity)
                    Result.success(uiModel)
                } else {
                    Result.failure(Exception("Brak połączenia i brak szczegółów offline"))
                }
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun importFromAllegro() = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.listings.importAllegroOffers()
            // Po imporcie musimy odświeżyć listę lokalną
            fetchAll()
            Result.success(response)
        } catch (e: Exception) {
            throw normalizeError(e)
        }
    }

    private val moshi = Moshi.Builder().build()
    private val mapAdapter = moshi.adapter<Map<String, String>>(
        Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    )

    suspend fun update(
        id: String,
        title: String?,
        description: String?,
        price: Double?,
        attributes: Map<String, String>?,
        currentImages: List<ListingImageUi>,
        newPhotos: List<Uri>
    ) = withContext(Dispatchers.IO) {
        try {
            val uploadedImageDtos = if (newPhotos.isNotEmpty()) {
                uploadPhotosAndReturn(id, newPhotos)
            } else {
                emptyList()
            }
            val allImagePayloads = mutableListOf<ListingImagePayload>()

            currentImages.forEach {
                allImagePayloads.add(ListingImagePayload(it.url))
            }

            uploadedImageDtos.forEach {
                allImagePayloads.add(ListingImagePayload(it.url))
            }

            val body = ListingUpdateBody(
                title = title,
                description = description,
                price = price,
                images = allImagePayloads,
                parameterValues = attributes
            )

            ApiClient.listings.update(id, body)

            fetchDetails(id)
        } catch (e: Exception) {
            throw normalizeError(e)
        }
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        try {
            ApiClient.listings.delete(id)
            listingDao.deleteById(id)
        } catch (e: Exception) {
            throw normalizeError(e)
        }
    }

    suspend fun deleteImage(listingId: String, imageId: String) = withContext(Dispatchers.IO) {
        try {
            ApiClient.listings.deleteImage(listingId, imageId)
            fetchDetails(listingId)
        } catch (e: Exception) {
            throw normalizeError(e)
        }
    }

    private suspend fun uploadPhotosAndReturn(listingId: String, uris: List<Uri>): List<ListingImageDto> {
        val uploaded = mutableListOf<ListingImageDto>()
        val contentResolver = context.contentResolver
        uris.forEach { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return@forEach
                val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
                val outputStream = FileOutputStream(tempFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                val response = ApiClient.listings.uploadImage(listingId, body)
                uploaded.add(response)

                tempFile.delete()
            } catch (e: Exception) { e.printStackTrace() }
        }
        return uploaded
    }

    private suspend fun uploadPhotos(listingId: String, uris: List<Uri>) {
        uploadPhotosAndReturn(listingId, uris)
    }

    private fun isNetworkError(e: Throwable): Boolean {
        return e is ConnectException || e is SocketTimeoutException || e is UnknownHostException
    }

    private fun normalizeError(e: Exception): Exception {
        return if (isNetworkError(e)) {
            Exception("Brak dostępu do zasobów (jesteś offline/serwer jest nieosiągalny)")
        } else {
            e
        }
    }
}