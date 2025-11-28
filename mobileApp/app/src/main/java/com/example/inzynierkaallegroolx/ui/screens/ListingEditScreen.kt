package com.example.inzynierkaallegroolx.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.inzynierkaallegroolx.ui.components.AppTopBar
import com.example.inzynierkaallegroolx.viewmodel.ListingEditViewModel

@Composable
fun ListingEditScreen(
    navController: NavController,
    listingId: String,
    vm: ListingEditViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // Launcher do wyboru zdjęć z galerii
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> vm.addPhotos(uris) }
    )

    LaunchedEffect(listingId) {
        vm.loadListing(listingId)
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) navController.popBackStack()
    }

    Scaffold(
        topBar = { AppTopBar("Edytuj Ogłoszenie", navController, showBackArrow = true, showAvatar = false) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { vm.onTitleChange(it) },
                    label = { Text("Tytuł") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.price,
                    onValueChange = { vm.onPriceChange(it) },
                    label = { Text("Cena") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.category,
                    onValueChange = { vm.onCategoryChange(it) },
                    label = { Text("Kategoria") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.description,
                    onValueChange = { vm.onDescChange(it) },
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sekcja edycji zdjęć (Prawdziwe zdjęcia + Coil)
                Text("Edytuj zdjęcia", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Zmieniono na FlowRow lub poziomy scroll, tutaj prosty Row z zawijaniem jeśli zdjęć mało
                // Dla lepszej obsługi wielu zdjęć warto użyć LazyRow, ale tu zostawiam Row zgodnie ze stylem
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp), // Stała wysokość kontenera na zdjęcia
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Przycisk dodawania
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Dodaj")
                    }

                    // Wyświetlanie listy zdjęć (z servera lub lokalnych)
                    state.images.forEach { img ->
                        Box(
                            modifier = Modifier.size(80.dp)
                        ) {
                            AsyncImage(
                                model = img.localUri ?: img.remoteUrl, // Wybierz dostępne źródło
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            // Przycisk usuwania (X)
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Usuń",
                                tint = Color.Red,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(Color.White, RoundedCornerShape(50)) // Tło dla lepszej widoczności
                                    .padding(2.dp)
                                    .size(20.dp)
                                    .clickable { vm.removePhoto(img) }
                            )
                        }
                    }
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { vm.saveChanges() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zapisz zmiany")
                }

                // Dodatkowy odstęp na dole
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}