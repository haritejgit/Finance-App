package com.example.finance.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val exportState by viewModel.exportState.collectAsState()
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    var startDate by remember { mutableStateOf(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)

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
                        Text(
                            "Export Reports", 
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                ) 
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Illustration/Icon
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )
                    }
                }

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Excel Data Export",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Download your collection history and customer payments into a professional Excel spreadsheet format.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                
                // Date Range Selection
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF1E88E5))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Choose Date Range",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Start Date
                        DateButton(
                            label = "FROM",
                            dateText = sdf.format(Date(startDate)),
                            onClick = { showStartDatePicker = true }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // End Date
                        DateButton(
                            label = "TO",
                            dateText = sdf.format(Date(endDate)),
                            onClick = { showEndDatePicker = true }
                        )
                    }
                }

                // Status & Export Action
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    when (val state = exportState) {
                        is ExportState.Idle -> {
                            Button(
                                onClick = { viewModel.exportData(startDate, endDate) },
                                modifier = Modifier.fillMaxWidth().height(60.dp).shadow(12.dp, RoundedCornerShape(30.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(30.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF0D47A1))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "GENERATE REPORT",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0D47A1)
                                        )
                                    )
                                }
                            }
                        }
                        is ExportState.Loading -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Preparing your file...", color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                        is ExportState.Success -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Report Ready!",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                    )
                                    Text(
                                        "Saved: ${state.file.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(
                                        onClick = { viewModel.resetState() },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                                    ) {
                                        Text("Export Another", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        is ExportState.Error -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Export Failed", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(state.message, color = Color.White.copy(0.8f), fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(onClick = { viewModel.resetState() }, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                                        Text("Retry", color = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
    
    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { 
                        startDatePickerState.selectedDateMillis?.let { startDate = it }
                        showStartDatePicker = false 
                    }
                ) { Text("OK") }
            }
        ) { DatePicker(state = startDatePickerState) }
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { 
                        endDatePickerState.selectedDateMillis?.let { endDate = it }
                        showEndDatePicker = false 
                    }
                ) { Text("OK") }
            }
        ) { DatePicker(state = endDatePickerState) }
    }
}

@Composable
fun DateButton(label: String, dateText: String, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color.LightGray, Color.LightGray)))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(dateText, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF1E88E5))
        }
    }
}
