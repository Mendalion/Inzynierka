import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inzynierkaallegroolx.ui.components.AppTopBar
import com.example.inzynierkaallegroolx.viewmodel.ListingEditViewModel

@Composable
fun ListingEditScreen(
    navController: NavController,
    listingId: String,
    vm: ListingEditViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    //załaduj dane przy wejściu
    LaunchedEffect(listingId) {
        vm.loadListing(listingId)
    }

    //po sukcesie cofnij
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) navController.popBackStack()
    }

    Scaffold(
        topBar = { AppTopBar("Edytuj Ogłoszenie", navController, showBackArrow = true, showAvatar = false) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
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
                    value = state.description,
                    onValueChange = { vm.onDescChange(it) },
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )

                if (state.error != null) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { vm.saveChanges() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zapisz zmiany")
                }
            }
        }
    }
}