package com.example.finance.data.entities

data class PaymentWithLoanInfo(
    val id: String,
    val loanId: String,
    val amountPaid: Double,
    val paymentDate: Long,
    val weekNumber: Int,
    val paymentType: String = "REGULAR",
    val paymentMode: String = "CASH",
    val notes: String? = null,
    val loanStatus: String,
    val loanStartDate: Long
)
