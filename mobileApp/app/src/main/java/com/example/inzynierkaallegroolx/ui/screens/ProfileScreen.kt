package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inzynierkaallegroolx.ui.components.AppBottomBar
import com.example.inzynierkaallegroolx.ui.components.AppTopBar
import com.example.inzynierkaallegroolx.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    //lokalne stany dla pól edycji (żeby można było pisać)
    //synchronizujemy je, gdy przyjdą dane z serwera
    var nameInput by remember(state.name) { mutableStateOf(state.name) }
    var phoneInput by remember(state.phone) { mutableStateOf(state.phone) }

    Scaffold(
        topBar = { AppTopBar("Twój Profil", navController, showBackArrow = true, showAvatar = false) },
        bottomBar = { AppBottomBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            //avatar i Nagłówek
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (state.name.isNotEmpty()) state.name else state.email,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            //sekcja edycji
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Edycja Profilu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Imię i Nazwisko") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Numer telefonu") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { vm.updateProfile(nameInput, phoneInput) },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Zapisz zmiany")
                        }
                    }
                }
            }

            //komunikaty o błędach/sukcesie
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            if (state.successMessage != null) {
                Text(state.successMessage!!, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            //przycisk ustawienia (TODO)
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.Settings, null)
                Spacer(Modifier.width(8.dp))
                Text("Ustawienia (TODO)")
            }

            Spacer(modifier = Modifier.weight(1f)) //wypycha wylogowanie na dół

            //przycisk wyloguj
            Button(
                onClick = {
                    vm.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Wyloguj", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}