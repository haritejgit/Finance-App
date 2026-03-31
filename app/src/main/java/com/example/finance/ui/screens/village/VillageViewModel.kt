package com.example.finance.ui.screens.village

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance.data.entities.Village
import com.example.finance.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VillageViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    val villages: StateFlow<List<Village>> = repository.getAllVillages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addVillage(name: String, day: String, shift: String) {
        viewModelScope.launch {
            repository.insertVillage(Village(name = name, dayOfWeek = day, shift = shift, userId = ""))
        }
    }

    fun deleteVillage(village: Village) {
        viewModelScope.launch {
            repository.deleteVillage(village)
        }
    }

    fun getVillageCollectionToday(villageId: String): Flow<Double> {
        return repository.getVillageCollectionToday(villageId)
    }
}
