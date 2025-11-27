package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inzynierkaallegroolx.ui.components.AppTopBar
import com.example.inzynierkaallegroolx.viewmodel.ListingAddViewModel

@Composable
fun ListingAddScreen(
    navController: NavController,
    vm: ListingAddViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val scrollState = rememberScrollState()

    //Obsługa sukcesu - powrót do poprzedniego ekranu
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.popBackStack()
            vm.resetState()
        }
    }

    Scaffold(
        topBar = { AppTopBar("Nowe ogłoszenie", navController, showBackArrow = true, showAvatar = false) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState) //umożliwia przewijanie
                .padding(16.dp)
        ) {

            //sekcja Zdjęć (Placeholder)
            Text("Zdjęcia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Przycisk dodawania
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable { vm.addPhotoMock() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Dodaj", tint = MaterialTheme.colorScheme.primary)
                }

                //wyświetlanie dodanych "zdjęć" (tylko liczniki/kwadraty dla testu)
                repeat(state.photosCount) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Img ${it + 1}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Text("Dodano: ${state.photosCount} zdjęć", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            //pola Tekstowe
            OutlinedTextField(
                value = state.title,
                onValueChange = { vm.onTitleChange(it) },
                label = { Text("Tytuł ogłoszenia *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.price,
                onValueChange = { vm.onPriceChange(it) },
                label = { Text("Cena (PLN) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.category,
                onValueChange = { vm.onCategoryChange(it) },
                label = { Text("Kategoria") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.description,
                onValueChange = { vm.onDescriptionChange(it) },
                label = { Text("Opis przedmiotu") },
                modifier = Modifier.fillMaxWidth().height(150.dp), // Wyższe pole
                maxLines = 10,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Wybór Platformy
            Text("Opublikuj na:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { vm.toggleAllegro(!state.platformAllegro) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = state.platformAllegro, onCheckedChange = { vm.toggleAllegro(it) })
                        Text("Allegro")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { vm.toggleOlx(!state.platformOlx) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = state.platformOlx, onCheckedChange = { vm.toggleOlx(it) })
                        Text("OLX")
                    }
                }
            }

            //obsługa Błędów
            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(32.dp))

            //przycisk Akcji
            Button(
                onClick = { vm.submitListing() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Opublikuj ogłoszenie")
                }
            }

            //dodatkowy odstęp na dole, żeby klawiatura/pasek nawigacji nie zasłaniały
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}