package com.example.inzynierkaallegroolx.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Logowanie")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Listings : Screen("listings", "Ogłoszenia", Icons.AutoMirrored.Filled.List)
    object ListingAdd : Screen("listing_add", "Dodaj ogłoszenie")
    object Messages : Screen("messages", "Wiadomości", Icons.Default.Email)
    object Stats : Screen("stats", "Statystyki", Icons.Default.BarChart)
    object Profile : Screen("profile", "Profil")//ewentualnie usunac zeby klikac ten avatar profil

    object ListingDetail : Screen("listing/{id}", "Szczegóły") {
        fun createRoute(id: String) = "listing/$id"
    }
}