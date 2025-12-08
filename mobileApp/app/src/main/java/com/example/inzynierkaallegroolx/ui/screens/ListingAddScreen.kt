package com.example.inzynierkaallegroolx.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> vm.addPhotos(uris) }
    )

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            Toast.makeText(context, "Szkic utworzony!", Toast.LENGTH_LONG).show()
            navController.popBackStack()
            vm.resetState()
        }
    }

    Scaffold(
        topBar = { AppTopBar("Nowy Szkic", navController, showBackArrow = true, showAvatar = false) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // --- PODSTAWY ---
            OutlinedTextField(
                value = state.title,
                onValueChange = { vm.onTitleChange(it) },
                label = { Text("Tytuł") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.price,
                onValueChange = { vm.onPriceChange(it) },
                label = { Text("Cena (PLN)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // --- KATEGORIA ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.category,
                    onValueChange = { vm.onCategoryChange(it) },
                    label = { Text("ID Kategorii (np. 2)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { vm.loadParametersForCategory() },
                    enabled = !state.isLoadingParams && state.category.isNotBlank()
                ) {
                    if(state.isLoadingParams) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text("Pobierz")
                }
            }

            // --- DYNAMICZNE POLA ---
            if (state.dynamicFields.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Parametry (Wymagane)", style = MaterialTheme.typography.titleSmall)

                state.dynamicFields.forEach { field ->
                    val value = state.parameterValues[field.id] ?: ""

                    if (field.dictionary != null) {
                        // Słownik (Dropdown)
                        var expanded by remember { mutableStateOf(false) }
                        val label = field.dictionary.find { it.id == value }?.value ?: "Wybierz..."

                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            OutlinedTextField(
                                value = label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(field.name) },
                                trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                field.dictionary.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt.value) },
                                        onClick = {
                                            vm.onParameterChange(field.id, opt.id)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Zwykłe pole tekstowe
                        OutlinedTextField(
                            value = value,
                            onValueChange = { vm.onParameterChange(field.id, it) },
                            label = { Text(field.name) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = { vm.onDescriptionChange(it) },
                label = { Text("Opis") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            // --- ZDJĘCIA ---
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Icon(Icons.Default.AddAPhoto, null)
                Spacer(Modifier.width(8.dp))
                Text("Dodaj zdjęcia")
            }
            if (state.selectedPhotos.isNotEmpty()) {
                Text("Wybrano zdjęć: ${state.selectedPhotos.size}")
            }

            // --- PLATFORMY ---
            Spacer(modifier = Modifier.height(16.dp))
            Text("Wybierz platformę:", style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = state.selectedPlatform == "ALLEGRO",
                    onClick = { vm.selectPlatform("ALLEGRO") }
                )
                Text(
                    text = "Allegro",
                    modifier = Modifier.clickable { vm.selectPlatform("ALLEGRO") }
                )

                Spacer(Modifier.width(24.dp))

                RadioButton(
                    // Uwaga: Jeśli w Enumie masz EBAY, użyj "EBAY". Jeśli w UI ma być OLX,
                    // musisz to obsłużyć. Zakładam, że w Schema masz EBAY, ale w UI chcesz OLX?
                    // Jeśli w Schema masz tylko ALLEGRO i EBAY, to tutaj musisz dać "EBAY".
                    selected = state.selectedPlatform == "EBAY",
                    onClick = { vm.selectPlatform("EBAY") }
                )
                Text(
                    text = "eBay", // lub OLX, zależnie od Twojego enuma w bazie
                    modifier = Modifier.clickable { vm.selectPlatform("EBAY") }
                )
            }

            if (state.error != null) {
                Text(state.error!!, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { vm.submitListing() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.isLoading) CircularProgressIndicator(color = Color.White)
                else Text("Utwórz Szkic")
            }
        }
    }
}