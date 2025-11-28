package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.ListingCreateBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

data class ListingAddState(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: String = "",
    val platformAllegro: Boolean = true,
    val platformOlx: Boolean = false,
    val selectedPhotos: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val photosCount: Int = 0
)

class ListingAddViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ListingAddState())
    val state = _state.asStateFlow()

    fun onTitleChange(v: String) { _state.value = _state.value.copy(title = v) }
    fun onDescriptionChange(v: String) { _state.value = _state.value.copy(description = v) }
    fun onCategoryChange(v: String) { _state.value = _state.value.copy(category = v) }
    fun onPriceChange(v: String) {
        if (v.all { it.isDigit() || it == '.' || it == ',' }) {
            _state.value = _state.value.copy(price = v)
        }
    }

    fun toggleAllegro(checked: Boolean) { _state.value = _state.value.copy(platformAllegro = checked) }
    fun toggleOlx(checked: Boolean) { _state.value = _state.value.copy(platformOlx = checked) }

    // Dodawanie zdjęć (Mock) - jeśli stare przyciski są używane
    fun addPhotoMock() {
        _state.value = _state.value.copy(photosCount = _state.value.photosCount + 1)
    }

    //dodawanie prawdziwych zdjęć
    fun addPhotos(uris: List<Uri>) {
        val current = _state.value.selectedPhotos.toMutableList()
        current.addAll(uris)
        _state.value = _state.value.copy(selectedPhotos = current)
    }

    fun removePhoto(uri: Uri) {
        val current = _state.value.selectedPhotos.toMutableList()
        current.remove(uri)
        _state.value = _state.value.copy(selectedPhotos = current)
    }

    fun submitListing() {
        val s = _state.value
        if (s.title.isBlank() || s.price.isBlank()) {
            _state.value = s.copy(error = "Tytuł i cena są wymagane")
            return
        }

        val priceDouble = s.price.replace(',', '.').toDoubleOrNull()
        if (priceDouble == null) {
            _state.value = s.copy(error = "Niepoprawny format ceny")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            try {
                val createdListing = ApiClient.listings.create(
                    ListingCreateBody(
                        title = s.title,
                        description = s.description,
                        price = priceDouble
                    )
                )
                if (s.selectedPhotos.isNotEmpty()) {
                    uploadPhotos(createdListing.id, s.selectedPhotos)
                }

                _state.value = s.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = s.copy(isLoading = false, error = "Błąd: ${e.message}")
            }
        }
    }

    private suspend fun uploadPhotos(listingId: String, uris: List<Uri>) {
        val context = getApplication<Application>().applicationContext
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        _state.value = ListingAddState()
    }
}