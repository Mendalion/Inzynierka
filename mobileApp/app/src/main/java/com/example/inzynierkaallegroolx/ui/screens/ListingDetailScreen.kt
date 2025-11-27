package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inzynierkaallegroolx.ui.screens.components.ErrorText
import com.example.inzynierkaallegroolx.ui.screens.components.LoadingBox
import com.example.inzynierkaallegroolx.viewmodel.ListingDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    navController: NavController,
    viewModel: ListingDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    //Obsługa usunięcia - cofnij do listy
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły ogłoszenia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    // Przycisk edycji
                    state.listing?.let { listing ->
                        IconButton(onClick = { navController.navigate("listing/edit/${listing.id}") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }
                    }
                    // Przycisk usuwania
                    IconButton(onClick = { viewModel.deleteListing() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                LoadingBox()
            } else if (state.error != null) {
                ErrorText(error = state.error!!, onRetry = { viewModel.loadListing() })
            } else {
                state.listing?.let { listing ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = listing.title,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = "Cena: ${listing.price} PLN",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Status: ${listing.status}")
                                Text(text = "Platforma: ${listing.platform}")
                            }
                        }

                        Text(text = "Opis", style = MaterialTheme.typography.titleMedium)
                        // Tutaj w przyszłości pełny opis pobierany z detali
                        Text(text = "Brak szczegółowego opisu w widoku listy (dane przychodzą z mappera).", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}