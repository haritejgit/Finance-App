package com.example.finance.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.finance.data.dao.FinanceDao
import com.example.finance.data.entities.*

@Database(
    entities = [Village::class, Customer::class, Loan::class, Payment::class],
    version = 10,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE customers ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE customers ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE customers ADD COLUMN coId INTEGER")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE payments ADD COLUMN paymentMode TEXT NOT NULL DEFAULT 'CASH'")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE villages ADD COLUMN shift TEXT NOT NULL DEFAULT 'Morning'")
            }
        }
    }
}
