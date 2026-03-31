package com.example.finance.ui.screens.shift

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance.ui.screens.auth.AuthViewModel
import com.example.finance.ui.screens.village.VillageViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShiftSelectionScreen(
    onNavigateToVillages: (String, String) -> Unit,
    onNavigateToReports: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    villageViewModel: VillageViewModel = hiltViewModel()
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDay by remember { mutableStateOf(days[0]) }
    var selectedShift by remember { mutableStateOf("Morning") }
    val coroutineScope = rememberCoroutineScope()
    
    val userName = remember { authViewModel.getCurrentUserName() ?: "User" }
    val villageCollection by villageViewModel.getVillageCollectionToday("all").collectAsState(initial = 0.0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0), Color(0xFF0D47A1))
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Finance Dashboard", color = Color.White, fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                authViewModel.signOut()
                                onLogout()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Welcome Section
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(50.dp).background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1565C0))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Welcome back!", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Text(userName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Collection Summary
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL COLLECTION TODAY", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(
                            "₹$villageCollection", 
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = Color(0xFF1565C0))
                        )
                    }
                }

                SectionTitle(title = "Select Collection Day", icon = Icons.Default.CalendarMonth)
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    days.chunked(3).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { day ->
                                val isSelected = selectedDay == day
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedDay = day },
                                    label = { Text(day) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color.White.copy(alpha = 0.1f),
                                        labelColor = Color.White,
                                        selectedContainerColor = Color.White,
                                        selectedLabelColor = Color(0xFF0D47A1)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = Color.White.copy(alpha = 0.3f),
                                        selectedBorderColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                                )
                            }
                            if (chunk.size < 3) {
                                repeat(3 - chunk.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                SectionTitle(title = "Select Shift", icon = Icons.Default.Schedule)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ShiftCard(
                        title = "Morning",
                        subtitle = "6 AM - 12 PM",
                        icon = Icons.Default.LightMode,
                        isSelected = selectedShift == "Morning",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedShift = "Morning" }
                    )
                    ShiftCard(
                        title = "Evening",
                        subtitle = "2 PM - 8 PM",
                        icon = Icons.Default.DarkMode,
                        isSelected = selectedShift == "Evening",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedShift = "Evening" }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { onNavigateToVillages(selectedDay, selectedShift) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(12.dp, RoundedCornerShape(30.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("START COLLECTION", color = Color(0xFF0D47A1), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF0D47A1))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { onNavigateToReports() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.linearGradient(listOf(Color.White, Color.White))),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VIEW REPORTS", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ShiftCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(120.dp)
            .shadow(if (isSelected) 8.dp else 0.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.Icon(
                icon, 
                contentDescription = null, 
                tint = if (isSelected) Color(0xFF1565C0) else Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title, 
                color = if (isSelected) Color(0xFF1565C0) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                subtitle, 
                color = if (isSelected) Color(0xFF1565C0).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}
