import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inzynierkaallegroolx.ui.components.AppBottomBar
import com.example.inzynierkaallegroolx.ui.components.AppTopBar
import com.example.inzynierkaallegroolx.ui.model.ListingItemUi
import com.example.inzynierkaallegroolx.viewmodel.ListingsViewModel
import com.example.inzynierkaallegroolx.viewmodel.SortOption

@Composable
fun ListingsScreen(navController: NavController, vm: ListingsViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.loadListings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar("Twoje Ogłoszenia", navController) },
        bottomBar = { AppBottomBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            //pasek wyszukiwania
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { vm.onSearchQueryChange(it) },
                label = { Text("Szukaj ogłoszeń...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            //filtry i sortowanie
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.filterPlatform == "ALL",
                        onClick = { vm.onFilterChange("ALL") },
                        label = { Text("Wszystkie") }
                    )
                    FilterChip(
                        selected = state.filterPlatform == "ALLEGRO",
                        onClick = { vm.onFilterChange("ALLEGRO") },
                        label = { Text("Allegro") }
                    )
                    FilterChip(
                        selected = state.filterPlatform == "OLX",
                        onClick = { vm.onFilterChange("OLX") },
                        label = { Text("OLX") }
                    )
                }
                Box {
                    TextButton(onClick = { sortMenuExpanded = true }) {
                        Text("Sortuj")
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nazwa (A-Z)") },
                            onClick = {
                                vm.onSortChange(SortOption.TITLE_ASC)
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Nazwa (Z-A)") },
                            onClick = {
                                vm.onSortChange(SortOption.TITLE_DESC)
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cena rosnąco") },
                            onClick = {
                                vm.onSortChange(SortOption.PRICE_ASC)
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cena malejąco") },
                            onClick = {
                                vm.onSortChange(SortOption.PRICE_DESC)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Text("Błąd: ${state.error}", color = MaterialTheme.colorScheme.error)
            } else {
                //lista przewijana
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.filteredListings) { listing ->
                        ListingItemCard(
                            listing = listing,
                            onEditClick = {
                                //przekierowanie do edycji ogloszenia
                                navController.navigate("listing/edit/${listing.id}")
                            },
                            onDeleteClick = { vm.deleteListing(listing.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListingItemCard(listing: ListingItemUi, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Placeholder na zdjęcie
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(listing.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${listing.price} PLN", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Text(listing.status ?: "UNKNOWN", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            //ikony akcji
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = Color.Blue)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = Color.Red)
            }
        }
    }
}