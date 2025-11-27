package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inzynierkaallegroolx.ui.components.AppBottomBar
import com.example.inzynierkaallegroolx.ui.components.AppTopBar
import com.example.inzynierkaallegroolx.ui.navigation.Screen
import com.example.inzynierkaallegroolx.viewmodel.HomeViewModel

@Composable
fun HomeScreen(navController: NavController, vm: HomeViewModel = viewModel()) {
    val stats by vm.stats.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Home",
                navController = navController,
                showAvatar = true // Tutaj decydujemy czy widać avatar
            )
        },
        bottomBar = { AppBottomBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Sekcja Synchronizacja
            Text(
                "Synchronizacja",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SyncStatusCard(name = "Allegro", isSynced = true, modifier = Modifier.weight(1f))
                SyncStatusCard(name = "OLX", isSynced = false, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Główny Panel - Kafelki (Statystyki)
            Text(
                "Przegląd",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item { HomeStatTile("Aktywne", stats.activeListings.toString(), Color(0xFFE3F2FD), Color(0xFF1565C0)) }
                item { HomeStatTile("Wyświetlenia", stats.totalViews.toString(), Color(0xFFE8F5E9), Color(0xFF2E7D32)) } // Mock
                item { HomeStatTile("Wiadomości", stats.unreadMessages.toString(), Color(0xFFFFF3E0), Color(0xFFEF6C00)) }
                item { HomeStatTile("Zarchiwizowane", stats.archivedListings.toString(), Color(0xFFECEFF1), Color(0xFF455A64)) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Przyciski Akcji na dole
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate(Screen.ListingAdd.route) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Dodaj")
                }

                OutlinedButton(
                    onClick = { /* TODO: Import logic */ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Importuj")
                }
            }
        }
    }
}

// Komponent kafelka statusu synchronizacji
@Composable
fun SyncStatusCard(name: String, isSynced: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = if (isSynced) Color(0xFFF1F8E9) else Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kropka statusu
            Surface(
                modifier = Modifier.size(10.dp),
                shape = CircleShape,
                color = if (isSynced) Color.Green else Color.Red
            ) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

//Komponent dużego kafelka statystyk
@Composable
fun HomeStatTile(label: String, value: String, bgColor: Color, contentColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(100.dp), // Stała wysokość dla równych kafelków
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}