package com.example.finance.ui.screens.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance.data.entities.Customer
import com.example.finance.data.entities.Loan
import com.example.finance.data.entities.Payment
import com.example.finance.data.entities.PaymentWithLoanInfo
import com.example.finance.ui.theme.MissedRed
import com.example.finance.ui.theme.PaidGreen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1E88E5))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF333333)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    customerId: String,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    // Collect data from the stable StateFlows in ViewModel
    LaunchedEffect(customerId) { viewModel.loadCustomer(customerId) }
    
    val customer by viewModel.currentCustomer.collectAsState()
    val activeLoan by viewModel.activeLoan.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    
    val context = LocalContext.current
    
    var showPayDialog by remember { mutableStateOf(false) }
    var showDueDialog by remember { mutableStateOf(false) }
    var showRenewDialog by remember { mutableStateOf(false) }
    var showEditCustomerDialog by remember { mutableStateOf(false) }
    var showDeleteCustomerDialog by remember { mutableStateOf(false) }
    
    var editingPayment by remember { mutableStateOf<Payment?>(null) }
    var deletingPayment by remember { mutableStateOf<Payment?>(null) }

    // Calculate Civil Score
    val civilScore = remember(allPayments) {
        if (allPayments.isEmpty()) 750 // Starting default
        else {
            val total = allPayments.size
            val paidOnTime = allPayments.count { it.paymentType == "REGULAR" && it.amountPaid > 0 }
            val base = 300
            val range = 600
            val score = base + (range * (paidOnTime.toDouble() / total)).toInt()
            score.coerceIn(300, 900)
        }
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
                        Text(
                            customer?.name ?: "Profile", 
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { showEditCustomerDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(onClick = { showDeleteCustomerDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Civil Score Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Civil Score", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Text(
                                    civilScore.toString(),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                    color = when {
                                        civilScore >= 750 -> PaidGreen
                                        civilScore >= 600 -> Color(0xFFFFA000)
                                        else -> MissedRed
                                    }
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val status = when {
                                    civilScore >= 750 -> "EXCELLENT"
                                    civilScore >= 600 -> "GOOD"
                                    else -> "POOR"
                                }
                                Surface(
                                    color = when(status) {
                                        "EXCELLENT" -> PaidGreen.copy(0.1f)
                                        "GOOD" -> Color(0xFFFFA000).copy(0.1f)
                                        else -> MissedRed.copy(0.1f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = when(status) {
                                            "EXCELLENT" -> PaidGreen
                                            "GOOD" -> Color(0xFFFFA000)
                                            else -> MissedRed
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Header Info Item
                item {
                    customer?.let { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1E88E5).copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                "BOOK NO: ${c.numericalId}", 
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF1E88E5)
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        InfoRow(label = "Phone", value = c.phone, icon = Icons.Default.Phone)
                                        InfoRow(label = "Aadhar", value = c.aadhar, icon = Icons.Default.Badge)
                                        InfoRow(label = "Location", value = c.locationDesc, icon = Icons.Default.Home)
                                        if (!c.coName.isNullOrBlank()) {
                                            InfoRow(label = "C/O", value = "${c.coName} (${c.coId ?: ""})", icon = Icons.Default.Person)
                                        }
                                    }
                                    
                                    if (c.latitude != null && c.longitude != null) {
                                        IconButton(
                                            onClick = {
                                                val gmmIntentUri = Uri.parse("geo:${c.latitude},${c.longitude}?q=${c.latitude},${c.longitude}(${Uri.encode(c.name)})")
                                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                mapIntent.setPackage("com.google.android.apps.maps")
                                                try { context.startActivity(mapIntent) } catch (e: Exception) { context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri)) }
                                            },
                                            modifier = Modifier.background(Color(0xFF4CAF50).copy(alpha = 0.1f), CircleShape).size(50.dp)
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = "Map", modifier = Modifier.size(24.dp), tint = Color(0xFF4CAF50))
                                        }
                                    }
                                }
                                
                                activeLoan?.let { loan ->
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF1565C0).copy(alpha = 0.05f)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("CURRENT BALANCE", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                            Text(
                                                "₹${loan.balanceAmount}", 
                                                color = Color(0xFF1565C0), 
                                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black)
                                            )
                                            LinearProgressIndicator(
                                                progress = { 
                                                    if (loan.totalPayable > 0)
                                                        ((loan.totalPayable - loan.balanceAmount) / loan.totalPayable).toFloat()
                                                    else 0f
                                                },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).padding(top = 8.dp),
                                                color = PaidGreen,
                                                trackColor = PaidGreen.copy(alpha = 0.2f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                item {
                    if (activeLoan != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showPayDialog = true },
                                modifier = Modifier.weight(1f).height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PaidGreen)
                            ) {
                                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PAY", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            
                            Button(
                                onClick = { showDueDialog = true },
                                modifier = Modifier.weight(1f).height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MissedRed)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("DUE", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }

                // History Section
                item {
                    Text(
                        "Transaction History", 
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp), 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }

                if (allPayments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                        ) {
                            Text(
                                "No transactions found", 
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                items(items = allPayments, key = { it.id }) { payment ->
                    TransactionHistoryItem(
                        payment = payment,
                        onEdit = { p -> 
                            editingPayment = Payment(id = p.id, loanId = p.loanId, amountPaid = p.amountPaid, paymentDate = p.paymentDate, weekNumber = p.weekNumber, paymentType = p.paymentType, paymentMode = p.paymentMode, notes = p.notes, userId = "")
                        },
                        onDelete = { p ->
                            deletingPayment = Payment(id = p.id, loanId = p.loanId, amountPaid = p.amountPaid, paymentDate = p.paymentDate, weekNumber = p.weekNumber, paymentType = p.paymentType, paymentMode = p.paymentMode, notes = p.notes, userId = "")
                        }
                    )
                }
                
                item {
                    activeLoan?.let { loan ->
                        OutlinedButton(
                            onClick = { showRenewDialog = true },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.linearGradient(listOf(Color.White, Color.White)))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RENEW LOAN", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Dialogs...
        if (showPayDialog) {
            val villageState = remember { mutableStateOf<com.example.finance.data.entities.Village?>(null) }
            LaunchedEffect(customer) {
                customer?.let { villageState.value = viewModel.getVillageById(it.villageId) }
            }
            PaymentDialog(
                onDismiss = { showPayDialog = false },
                dayOfWeek = villageState.value?.dayOfWeek,
                onConfirm = { amount, date, mode ->
                    activeLoan?.let { viewModel.collectPayment(it, amount, date, mode) }
                    showPayDialog = false
                }
            )
        }

        if (showDueDialog) {
            val villageState = remember { mutableStateOf<com.example.finance.data.entities.Village?>(null) }
            LaunchedEffect(customer) {
                customer?.let { villageState.value = viewModel.getVillageById(it.villageId) }
            }
            DueDialog(
                onDismiss = { showDueDialog = false },
                dayOfWeek = villageState.value?.dayOfWeek,
                onConfirm = { date ->
                    activeLoan?.let { viewModel.markDue(it, date) }
                    showDueDialog = false
                }
            )
        }
        
        if (showRenewDialog) {
            val villageState = remember { mutableStateOf<com.example.finance.data.entities.Village?>(null) }
            LaunchedEffect(customer) {
                customer?.let { villageState.value = viewModel.getVillageById(it.villageId) }
            }
            activeLoan?.let { loan ->
                RenewLoanDialog(
                    currentBalance = loan.balanceAmount,
                    dayOfWeek = villageState.value?.dayOfWeek,
                    onDismiss = { showRenewDialog = false },
                    onConfirm = { newPrincipal, date ->
                        viewModel.renewLoan(loan, newPrincipal, date)
                        showRenewDialog = false
                    }
                )
            }
        }

        if (showEditCustomerDialog) {
            customer?.let { c ->
                EditCustomerDialog(
                    customer = c,
                    onDismiss = { showEditCustomerDialog = false },
                    onConfirm = { updatedCustomer ->
                        viewModel.updateCustomerDetails(updatedCustomer)
                        showEditCustomerDialog = false
                    }
                )
            }
        }

        if (showDeleteCustomerDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteCustomerDialog = false },
                title = { Text("Delete Customer") },
                text = { Text("Are you sure you want to delete this customer? All their financial records will be lost.") },
                confirmButton = {
                    Button(
                        onClick = {
                            customer?.let { viewModel.deleteCustomer(it) }
                            showDeleteCustomerDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MissedRed)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteCustomerDialog = false }) { Text("Cancel") }
                }
            )
        }

        editingPayment?.let { payment ->
            val villageState = remember { mutableStateOf<com.example.finance.data.entities.Village?>(null) }
            LaunchedEffect(customer) {
                customer?.let { villageState.value = viewModel.getVillageById(it.villageId) }
            }
            PaymentDialog(
                initialAmount = payment.amountPaid.toString(),
                initialDate = payment.paymentDate,
                initialMode = payment.paymentMode,
                title = "Edit Payment",
                dayOfWeek = villageState.value?.dayOfWeek,
                onDismiss = { editingPayment = null },
                onConfirm = { amount, date, mode ->
                    viewModel.updatePayment(payment, amount, date, mode)
                    editingPayment = null
                }
            )
        }

        deletingPayment?.let { payment ->
            AlertDialog(
                onDismissRequest = { deletingPayment = null },
                title = { Text("Delete Payment") },
                text = { Text("Delete this transaction of ₹${payment.amountPaid}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePayment(payment)
                            deletingPayment = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MissedRed)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingPayment = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionHistoryItem(
    payment: PaymentWithLoanInfo,
    onEdit: (PaymentWithLoanInfo) -> Unit,
    onDelete: (PaymentWithLoanInfo) -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    val colors = when {
        payment.paymentType == "RENEWAL_CLOSURE" -> listOf(Color(0xFF2196F3), "LOAN RENEWED", Icons.Default.Refresh)
        payment.amountPaid <= 0 -> listOf(MissedRed, "MISSED / DUE", Icons.Default.EventBusy)
        else -> listOf(PaidGreen, "PAID", Icons.Default.CheckCircle)
    }
    
    val statusColor = colors[0] as Color
    val statusText = colors[1] as String
    val statusIcon = colors[2] as ImageVector
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onEdit(payment) },
                onLongClick = { onDelete(payment) }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(statusIcon, contentDescription = null, modifier = Modifier.size(20.dp), tint = statusColor)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sdf.format(Date(payment.paymentDate)),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                    if (payment.amountPaid > 0) {
                        Text(
                            text = " • ${payment.paymentMode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Text(
                text = "₹${payment.amountPaid}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = if (payment.amountPaid > 0) Color(0xFF333333) else MissedRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    initialAmount: String = "",
    initialDate: Long = System.currentTimeMillis(),
    initialMode: String = "CASH",
    title: String = "Record Payment",
    dayOfWeek: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double, Long, String) -> Unit
) {
    var amount by remember { mutableStateOf(initialAmount) }
    var selectedMode by remember { mutableStateOf(initialMode) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val selectableDates = remember(dayOfWeek) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                if (dayOfWeek == null) return true
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = utcTimeMillis
                val day = calendar.get(Calendar.DAY_OF_WEEK)
                val dayMap = mapOf("Sunday" to 1, "Monday" to 2, "Tuesday" to 3, "Wednesday" to 4, "Thursday" to 5, "Friday" to 6, "Saturday" to 7)
                return day == dayMap[dayOfWeek]
            }
        }
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate,
        selectableDates = selectableDates
    )
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Payment Mode", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = selectedMode == "CASH",
                        onClick = { selectedMode = "CASH" },
                        label = { Text("Cash") },
                        leadingIcon = if (selectedMode == "CASH") {
                            { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedMode == "PHONE",
                        onClick = { selectedMode = "PHONE" },
                        label = { Text("Phone") },
                        leadingIcon = if (selectedMode == "PHONE") {
                            { Icon(Icons.Default.PhoneAndroid, null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF1E88E5))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(sdf.format(Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis())))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, datePickerState.selectedDateMillis ?: System.currentTimeMillis(), selectedMode) }) {
                Text("Confirm")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDialog(onDismiss: () -> Unit, dayOfWeek: String? = null, onConfirm: (Long) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    val selectableDates = remember(dayOfWeek) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                if (dayOfWeek == null) return true
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = utcTimeMillis
                val day = calendar.get(Calendar.DAY_OF_WEEK)
                val dayMap = mapOf("Sunday" to 1, "Monday" to 2, "Tuesday" to 3, "Wednesday" to 4, "Thursday" to 5, "Friday" to 6, "Saturday" to 7)
                return day == dayMap[dayOfWeek]
            }
        }
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = selectableDates
    )
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark as Due", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("This will record a missed payment for the selected date.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF1E88E5))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(sdf.format(Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis())))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(datePickerState.selectedDateMillis ?: System.currentTimeMillis()) },
                colors = ButtonDefaults.buttonColors(containerColor = MissedRed)
            ) { Text("Mark Due") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun EditCustomerDialog(customer: Customer, onDismiss: () -> Unit, onConfirm: (Customer) -> Unit) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone) }
    var aadhar by remember { mutableStateOf(customer.aadhar) }
    var location by remember { mutableStateOf(customer.locationDesc) }
    var coName by remember { mutableStateOf(customer.coName ?: "") }
    var coId by remember { mutableStateOf(customer.coId?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Customer Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = aadhar, onValueChange = { aadhar = it }, label = { Text("Aadhar") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = coName, onValueChange = { coName = it }, label = { Text("C/O Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = coId, onValueChange = { coId = it }, label = { Text("C/O ID") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(customer.copy(name = name, phone = phone, aadhar = aadhar, locationDesc = location, coName = coName, coId = coId.toIntOrNull()))
            }) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenewLoanDialog(currentBalance: Double, dayOfWeek: String? = null, onDismiss: () -> Unit, onConfirm: (Double, Long) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val selectableDates = remember(dayOfWeek) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                if (dayOfWeek == null) return true
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = utcTimeMillis
                val day = calendar.get(Calendar.DAY_OF_WEEK)
                val dayMap = mapOf("Sunday" to 1, "Monday" to 2, "Tuesday" to 3, "Wednesday" to 4, "Thursday" to 5, "Friday" to 6, "Saturday" to 7)
                return day == dayMap[dayOfWeek]
            }
        }
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = selectableDates
    )
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renew Loan", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Current balance: ₹$currentBalance", color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold)
                        Text("This will be cleared automatically.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount, 
                    onValueChange = { amount = it }, 
                    label = { Text("New Principal Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Renewal Date", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF1E88E5))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(sdf.format(Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis())))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, datePickerState.selectedDateMillis ?: System.currentTimeMillis()) }) { Text("Renew Now") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(24.dp)
    )
}
