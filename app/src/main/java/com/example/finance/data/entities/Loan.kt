package com.example.finance.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class Loan(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val principalAmount: Double,
    val interestAmount: Double,
    val totalPayable: Double,
    val balanceAmount: Double,
    val userId: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE"
)
