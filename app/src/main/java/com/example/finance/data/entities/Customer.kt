package com.example.finance.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "customers",
    foreignKeys = [
        ForeignKey(
            entity = Village::class,
            parentColumns = ["id"],
            childColumns = ["villageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("villageId")]
)
data class Customer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val numericalId: Int,
    val name: String,
    val phone: String,
    val aadhar: String,
    val locationDesc: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val coName: String? = null,
    val coId: Int? = null,
    val villageId: String,
    val userId: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
