package com.example.finance.ui.screens.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance.data.repository.FinanceRepository
import com.example.finance.util.ExcelExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FinanceRepository
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState = _exportState.asStateFlow()

    fun exportData(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            val exporter = ExcelExporter(context, repository)
            val file = exporter.exportPaymentsToExcel(startDate, endDate)
            if (file != null) {
                _exportState.value = ExportState.Success(file)
            } else {
                _exportState.value = ExportState.Error("Failed to export data")
            }
        }
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val file: File) : ExportState()
    data class Error(val message: String) : ExportState()
}
