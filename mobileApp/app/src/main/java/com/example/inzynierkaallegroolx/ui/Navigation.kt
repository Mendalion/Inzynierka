package com.example.inzynierkaallegroolx.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.inzynierkaallegroolx.ui.screens.LoginScreen
import com.example.inzynierkaallegroolx.ui.screens.ListingsScreen
import com.example.inzynierkaallegroolx.ui.screens.ListingEditScreen
import com.example.inzynierkaallegroolx.ui.screens.MessagesScreen
import com.example.inzynierkaallegroolx.ui.screens.ConversationDetailScreen
import com.example.inzynierkaallegroolx.ui.screens.TemplatesScreen
import com.example.inzynierkaallegroolx.ui.screens.ProfileScreen
import com.example.inzynierkaallegroolx.ui.screens.StatsScreen
import com.example.inzynierkaallegroolx.ui.screens.ReportsScreen

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
fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) { LoginScreen(onLoggedIn = { nav.navigate(Routes.LISTINGS) { popUpTo(Routes.LOGIN) { inclusive = true } } }) }
        composable(Routes.LISTINGS) { ListingsScreen() }
        composable(Routes.LISTING_EDIT) { ListingEditScreen(onDone = { nav.popBackStack() }) }
        composable(Routes.MESSAGES) { MessagesScreen(onOpen = { nav.navigate(Routes.CONVERSATION + "/" + it) }) }
        composable(Routes.CONVERSATION + "/{id}") { backStack ->
            val id = backStack.arguments?.getString("id") ?: return@composable
            ConversationDetailScreen(id, onBack = { nav.popBackStack() })
        }
        composable(Routes.TEMPLATES) { TemplatesScreen() }
        composable(Routes.PROFILE) { ProfileScreen() }
        composable(Routes.REPORTS) { ReportsScreen() }
        composable(Routes.STATS + "/{id}") { backStack ->
            val id = backStack.arguments?.getString("id") ?: return@composable
            StatsScreen(listingId = id)
        }
    }
}
