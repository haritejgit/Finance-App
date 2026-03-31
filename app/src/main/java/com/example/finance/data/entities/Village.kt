package com.example.finance.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "villages")
data class Village(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dayOfWeek: String,
    val shift: String = "Morning", // Morning, Evening
    val employeeId: String? = null,
    val userId: String = ""
)
