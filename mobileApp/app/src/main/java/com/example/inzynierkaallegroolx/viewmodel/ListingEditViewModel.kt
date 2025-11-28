package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.Config
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.ListingUpdateBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

// Reprezentacja zdjęcia (może być z serwera lub lokalne nowe)
data class ImageUi(
    val id: String? = null, // null dla nowych
    val remoteUrl: String? = null,
    val localUri: Uri? = null
)

data class ListingEditState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "",
    val images: List<ImageUi> = emptyList(), // Lista zdjęć
    val isLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class ListingEditViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(ListingEditState())
    val state = _state.asStateFlow()

    fun loadListing(id: String) {
        viewModelScope.launch {
            try {
                val listing = ApiClient.listings.get(id)
                // Mapowanie zdjęć z serwera (URL)
                // Zakładamy, że serwer zwraca pełne URL w polu 'url', jeśli nie - trzeba dodać Config.BASE_URL
                // W image.controller.ts jest '/uploads/filename'. Trzeba dodać base url.

                val mappedImages = listing.images?.map { imgDto ->
                    val fullUrl = Config.imageUrl(imgDto.url)
                    ImageUi(id = imgDto.id, remoteUrl = fullUrl)
                } ?: emptyList()

                _state.value = ListingEditState(
                    id = listing.id,
                    title = listing.title,
                    description = listing.description ?: "",
                    price = listing.price ?: "",
                    category = "Inne",
                    images = mappedImages,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Błąd pobierania: ${e.message}")
            }
        }
    }

    fun onTitleChange(v: String) { _state.value = _state.value.copy(title = v) }
    fun onDescChange(v: String) { _state.value = _state.value.copy(description = v) }
    fun onCategoryChange(v: String) { _state.value = _state.value.copy(category = v) }

    fun onPriceChange(v: String) {
        if (v.all { it.isDigit() || it == '.' || it == ',' }) {
            _state.value = _state.value.copy(price = v)
        }
    }

    fun addPhotoMock() {}
    fun removePhotoMock() {}

    fun addPhotos(uris: List<Uri>) {
        val current = _state.value.images.toMutableList()
        // Dodajemy nowe lokalne zdjęcia
        uris.forEach { current.add(ImageUi(localUri = it)) }
        _state.value = _state.value.copy(images = current)
    }

    // Usunięcie zdjęcia (jeśli lokalne - usuń z listy, jeśli remote - można by wołać API delete, ale na razie usuńmy z widoku)
    fun removePhoto(img: ImageUi) {
        val current = _state.value.images.toMutableList()
        current.remove(img)
        _state.value = _state.value.copy(images = current)

        // opcjonalnie: Jeśli to zdjęcie remote (ma ID), można od razu usunąć z serwera,
        // albo dodać do listy "toDelete" i usunąć przy Save.
        // tutaj dla uproszczenia tylko usuwam z widoku (nie usunie się fizycznie z serwera, chyba że dodasz logikę API).
    }

    fun saveChanges() {
        val s = _state.value
        val priceDouble = s.price.replace(',', '.').toDoubleOrNull()
        if (priceDouble == null) {
            _state.value = s.copy(error = "Niepoprawny format ceny")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            try {
                ApiClient.listings.update(
                    s.id,
                    ListingUpdateBody(title = s.title, description = s.description, price = priceDouble)
                )

                val newPhotos = s.images.filter { it.localUri != null }
                if (newPhotos.isNotEmpty()) {
                    val uris = newPhotos.mapNotNull { it.localUri }
                    uploadPhotos(s.id, uris)
                }

                _state.value = s.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = s.copy(isLoading = false, error = "Błąd zapisu: ${e.message}")
            }
        }
    }

    private suspend fun uploadPhotos(listingId: String, uris: List<Uri>) {
        val context = getApplication<Application>().applicationContext
        val contentResolver = context.contentResolver
        uris.forEach { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return@forEach
                val tempFile = File.createTempFile("upload_edit", ".jpg", context.cacheDir)
                val outputStream = FileOutputStream(tempFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                ApiClient.listings.uploadImage(listingId, body)
                tempFile.delete()
            } catch(e: Exception) { e.printStackTrace() }
        }
    }
}