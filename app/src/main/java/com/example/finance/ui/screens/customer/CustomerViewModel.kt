package com.example.finance.ui.screens.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance.data.entities.Customer
import com.example.finance.data.entities.Loan
import com.example.finance.data.entities.Payment
import com.example.finance.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: FinanceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _villageId = MutableStateFlow<String?>(null)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<Customer>> = _villageId
        .filterNotNull()
        .flatMapLatest { repository.getCustomersByVillage(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setVillageId(id: String) {
        _villageId.value = id
    }

    private val _currentCustomerId = MutableStateFlow<String?>(savedStateHandle.get<String>("customerId"))
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentCustomer: StateFlow<Customer?> = _currentCustomerId
        .filterNotNull()
        .flatMapLatest { repository.getCustomerByIdFlow(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeLoan: StateFlow<Loan?> = _currentCustomerId
        .filterNotNull()
        .flatMapLatest { repository.getActiveLoan(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val allPayments = _currentCustomerId
        .filterNotNull()
        .flatMapLatest { repository.getAllPaymentsForCustomer(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadCustomer(id: String) {
        if (_currentCustomerId.value != id) {
            _currentCustomerId.value = id
        }
    }

    fun addCustomer(
        name: String, 
        phone: String, 
        aadhar: String, 
        location: String, 
        co: String, 
        coId: Int?,
        principal: Double,
        startDate: Long,
        lat: Double? = null,
        lng: Double? = null
    ) {
        viewModelScope.launch {
            val villageId = _villageId.value ?: return@launch
            val nextId = repository.getNextNumericalId(villageId)
            val customer = Customer(
                numericalId = nextId,
                name = name,
                phone = phone,
                aadhar = aadhar,
                locationDesc = location,
                latitude = lat,
                longitude = lng,
                coName = co,
                coId = coId,
                villageId = villageId,
                userId = ""
            )
            repository.addNewCustomer(customer, principal, startDate)
        }
    }

    fun collectPayment(loan: Loan, amount: Double, date: Long, mode: String = "CASH") {
        viewModelScope.launch {
            repository.addPayment(loan, amount, date, mode)
        }
    }
    
    fun markDue(loan: Loan, date: Long) {
        viewModelScope.launch {
            repository.markDue(loan, date)
        }
    }

    fun renewLoan(oldLoan: Loan, newPrincipal: Double, date: Long) {
        viewModelScope.launch {
            repository.renewLoan(oldLoan, newPrincipal, date)
        }
    }
    
    fun updateCustomerDetails(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun updatePayment(payment: Payment, newAmount: Double, newDate: Long, newMode: String = "CASH") {
        viewModelScope.launch {
            repository.updatePayment(payment, newAmount, newDate, newMode)
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }

    suspend fun getVillageById(villageId: String) = repository.getVillageById(villageId)
}
