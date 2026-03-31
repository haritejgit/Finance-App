package com.example.finance.data.entities

data class PaymentReport(
    val paymentDate: Long,
    val amountPaid: Double,
    val customerName: String,
    val villageName: String,
    val numericalId: Int,
    val coName: String?,
    val paymentType: String,
    val notes: String?
)
