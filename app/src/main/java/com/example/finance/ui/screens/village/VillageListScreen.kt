package com.example.finance.ui.screens.village

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance.data.entities.Village

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VillageListScreen(
    day: String,
    shift: String,
    onNavigateToCustomers: (String) -> Unit,
    viewModel: VillageViewModel = hiltViewModel()
) {
    val villages by viewModel.villages.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var villageToDelete by remember { mutableStateOf<Village?>(null) }
    var newVillageName by remember { mutableStateOf("") }
    
    val filteredVillages = remember(villages, day, shift) {
        villages.filter { it.dayOfWeek == day && it.shift == shift }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0))
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Villages",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "$day • $shift",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1565C0),
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD VILLAGE", fontWeight = FontWeight.Bold)
                }
            }
        ) { padding ->
            if (filteredVillages.isEmpty()) {
                EmptyVillageState(padding)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredVillages, key = { it.id }) { village ->
                        VillageItem(
                            village = village,
                            onClick = { onNavigateToCustomers(village.id) },
                            onLongClick = { villageToDelete = village }
                        )
                    }
                    // Spacer for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add New Village", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newVillageName,
                        onValueChange = { newVillageName = it },
                        label = { Text("Village Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newVillageName.isNotBlank()) {
                                viewModel.addVillage(newVillageName, day, shift)
                                newVillageName = ""
                                showAddDialog = false
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Village")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        villageToDelete?.let { village ->
            AlertDialog(
                onDismissRequest = { villageToDelete = null },
                title = { Text("Delete Village", color = MaterialTheme.colorScheme.error) },
                text = { Text("Delete '${village.name}'? This will permanently remove all associated customer data.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteVillage(village)
                            villageToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { villageToDelete = null }) {
                        Text("Keep it")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VillageItem(
    village: Village,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFF1E88E5).copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.LocationCity,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = village.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    )
                    Text(
                        text = "Tap to view customers",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EmptyVillageState(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Villages Added Yet",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "Add a village to start tracking your collections for this shift.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
        )
    }
}
