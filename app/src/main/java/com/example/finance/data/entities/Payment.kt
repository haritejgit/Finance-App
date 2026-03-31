package com.example.finance.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Loan::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("loanId")]
)
data class Payment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val loanId: String,
    val amountPaid: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val weekNumber: Int,
    val paymentType: String = "REGULAR", // REGULAR, DUE, RENEWAL_CLOSURE
    val paymentMode: String = "CASH", // CASH, PHONE
    val notes: String? = null,
    val userId: String = ""
)
