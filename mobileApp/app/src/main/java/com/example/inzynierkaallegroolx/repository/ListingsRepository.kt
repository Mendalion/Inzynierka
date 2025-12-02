package com.example.inzynierkaallegroolx.repository

import android.content.Context
import android.net.Uri
import com.example.inzynierkaallegroolx.Config
import com.example.inzynierkaallegroolx.data.AppDatabase
import com.example.inzynierkaallegroolx.data.listings.ListingEntity
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.ListingCreateBody
import com.example.inzynierkaallegroolx.network.ListingUpdateBody
import com.example.inzynierkaallegroolx.ui.model.ListingImageUi
import com.example.inzynierkaallegroolx.ui.model.ListingItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ListingsRepository(private val context: Context) {

    private val listingDao = AppDatabase.getDatabase(context).listingDao()

    private fun mapEntityToUi(entity: ListingEntity): ListingItemUi {
        return ListingItemUi(
            id = entity.id,
            title = entity.title,
            price = entity.price,
            status = entity.status,
            platforms = if (entity.platforms.isNotEmpty()) entity.platforms.split(",") else emptyList(),
            thumbnailUrl = entity.thumbnailUrl,
            description = entity.description,
            allImages = emptyList() //tylko miniaturkę w trybie offline
        )
    }

    private fun mapDtoToEntity(dto: com.example.inzynierkaallegroolx.network.ListingDto): ListingEntity {
        val platformsStr = dto.platformStates?.joinToString(",") { it.platform } ?: ""
        val rawUrl = dto.images?.firstOrNull()?.url
        val thumb = Config.imageUrl(rawUrl)

        return ListingEntity(
            id = dto.id,
            title = dto.title,
            description = dto.description ?: "",
            price = dto.price ?: "0.00",
            status = dto.status ?: "UNKNOWN",
            thumbnailUrl = thumb,
            platforms = platformsStr
        )
    }

    suspend fun fetchAll(): Result<List<ListingItemUi>> = withContext(Dispatchers.IO) {
        return@withContext try {
            //najpierw próbuje połaczyć z siecią
            val resultDto = ApiClient.listings.getListings()
            val entities = resultDto.map { mapDtoToEntity(it) }

            //jezeli się uda aktualizujemy bazę lokalną
            listingDao.upsertAll(entities)

            //zwracamy dane z sieci
            val uiList = entities.map { mapEntityToUi(it) }
            Result.success(uiList)

        } catch (e: Exception) {
            if (isNetworkError(e)) {
            val localEntities = listingDao.getAll()
                if (localEntities.isNotEmpty()) {
                    val uiList = localEntities.map { mapEntityToUi(it) }
                    //Zwracamy sukces z lokalnymi danymi
                    Result.success(uiList)
                } else {
                    //Nie ma dostępu do internetu i baza jest pusta
                    Result.failure(Exception("Brak połączenia i brak danych offline"))
                }
            } else {
                //Inny błąd
                Result.failure(e)
            }
        }
    }

    suspend fun fetchDetails(id: String): Result<ListingItemUi> = withContext(Dispatchers.IO) {
        return@withContext try {
            val dto = ApiClient.listings.get(id)

            val entity = mapDtoToEntity(dto)
            listingDao.upsertAll(listOf(entity))

            val allImagesUi = dto.images?.map {
                ListingImageUi(it.id, Config.imageUrl(it.url) ?: "")
            } ?: emptyList()

            val uiModel = ListingItemUi(
                id = dto.id,
                title = dto.title,
                price = dto.price ?: "0.00",
                status = dto.status ?: "UNKNOWN",
                platforms = dto.platformStates?.map { it.platform } ?: emptyList(),
                thumbnailUrl = entity.thumbnailUrl,
                description = dto.description ?: "",
                allImages = allImagesUi
            )
            Result.success(uiModel)
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                val localEntity = listingDao.getById(id)
                if (localEntity != null) {
                    //lokalna wersja
                    //w trybie offline nie mamy listy allImages
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

    suspend fun create(title: String, description: String, price: Double, platforms: List<String>, photos: List<Uri>) = withContext(Dispatchers.IO) {
        try {
            val createdDto = ApiClient.listings.create(ListingCreateBody(title, description, price, platforms))
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

    suspend fun update(id: String, title: String?, description: String?, price: Double?, newPhotos: List<Uri>) = withContext(Dispatchers.IO) {
        try {
            ApiClient.listings.update(id, ListingUpdateBody(title, description, price))
            if (newPhotos.isNotEmpty()) {
                uploadPhotos(id, newPhotos)
            }
            fetchDetails(id)
        } catch (e: Exception) {
            throw normalizeError(e)
        }
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        try {
            //Najpierw usuwamy z serwera, wyrzuci błąd jeśli nie ma połaczenia z serwere,
            ApiClient.listings.delete(id)

            //serwer potwierdził to usuwamy z bazy lokalnej
            listingDao.deleteById(id)

        } catch (e: Exception) {
            throw normalizeError(e)
        }
    }

    suspend fun deleteImage(listingId: String, imageId: String) = withContext(Dispatchers.IO) {
        try {
            //usuwanie na serwerze
            ApiClient.listings.deleteImage(listingId, imageId)
            //pobieranie lokalną baze
            fetchDetails(listingId)
        } catch (e: Exception) {
            throw normalizeError(e)
        }
    }

    private suspend fun uploadPhotos(listingId: String, uris: List<Uri>) {
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
                ApiClient.listings.uploadImage(listingId, body)
                tempFile.delete()
            } catch (e: Exception) { e.printStackTrace() }
        }
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