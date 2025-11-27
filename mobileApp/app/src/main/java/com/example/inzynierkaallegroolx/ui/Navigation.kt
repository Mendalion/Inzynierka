package com.example.inzynierkaallegroolx.ui

import ListingEditScreen
import ListingsScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.inzynierkaallegroolx.ui.components.AppBottomBar
import com.example.inzynierkaallegroolx.ui.components.AppTopBar
import com.example.inzynierkaallegroolx.ui.navigation.Screen
import com.example.inzynierkaallegroolx.ui.screens.LoginScreen
//import com.example.inzynierkaallegroolx.ui.screens.ListingsScreen
//import com.example.inzynierkaallegroolx.ui.screens.ListingEditScreen
//import com.example.inzynierkaallegroolx.ui.screens.MessagesScreen
//import com.example.inzynierkaallegroolx.ui.screens.ConversationDetailScreen
import com.example.inzynierkaallegroolx.ui.screens.HomeScreen
//import com.example.inzynierkaallegroolx.ui.screens.TemplatesScreen
import com.example.inzynierkaallegroolx.ui.screens.ProfileScreen
import com.example.inzynierkaallegroolx.ui.screens.StatsScreen
import com.example.inzynierkaallegroolx.ui.screens.ReportsScreen
import com.example.inzynierkaallegroolx.viewmodel.AuthViewModel

object Routes {
    const val LOGIN = "login"
    const val LISTINGS = "listings"
    const val LISTING_EDIT = "listing_edit"
    const val MESSAGES = "messages"
    const val CONVERSATION = "conversation"
    const val TEMPLATES = "templates"
    const val PROFILE = "profile"
    const val STATS = "stats"
    const val REPORTS = "reports"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    startDestination: String = Screen.Login.route // Domyślnie logowanie
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(onLoggedIn = {
                // Po zalogowaniu idziemy do Home i czyścimy historię, żeby nie cofnąć do logowania
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        composable(Screen.ListingAdd.route) {
            com.example.inzynierkaallegroolx.ui.screens.ListingAddScreen(navController)
        }

        composable("listing/edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            ListingEditScreen(navController, id)
        }
        composable("listing/detail/{id}") { backStackEntry ->
            com.example.inzynierkaallegroolx.ui.screens.ListingDetailScreen(navController)
        }

        composable(Screen.Listings.route) {
            ListingsScreen(navController)
        }
        composable(Screen.Messages.route) {
            Text("Wiadomości - TODO")
        }

        composable(Screen.Stats.route) {
            Text("Statystyki - TODO")
        }
        composable(Screen.Profile.route) {
            com.example.inzynierkaallegroolx.ui.screens.ProfileScreen(
                navController = navController,
                onLogout = {
                    //logika po wylogowaniu: Przejdź do Login i wyczyść historię
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Czyści cały stos
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

//@Composable
//fun AppNavHost() {
//    val nav = rememberNavController()
//    NavHost(navController = nav, startDestination = Routes.LOGIN) {
//        composable(Routes.LOGIN) { LoginScreen(onLoggedIn = { nav.navigate(Routes.LISTINGS) { popUpTo(Routes.LOGIN) { inclusive = true } } }) }
//        composable(Routes.LISTINGS) { ListingsScreen() }
//        composable(Routes.LISTING_EDIT) { ListingEditScreen(onDone = { nav.popBackStack() }) }
//        composable(Routes.MESSAGES) { MessagesScreen(onOpen = { nav.navigate(Routes.CONVERSATION + "/" + it) }) }
//        composable(Routes.CONVERSATION + "/{id}") { backStack ->
//            val id = backStack.arguments?.getString("id") ?: return@composable
//            ConversationDetailScreen(id, onBack = { nav.popBackStack() })
//        }
//        composable(Routes.TEMPLATES) { TemplatesScreen() }
//        composable(Routes.PROFILE) { ProfileScreen() }
//        composable(Routes.REPORTS) { ReportsScreen() }
//        composable(Routes.STATS + "/{id}") { backStack ->
//            val id = backStack.arguments?.getString("id") ?: return@composable
//            StatsScreen(listingId = id)
//        }
//    }
//}
