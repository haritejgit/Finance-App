package com.example.finance.di

import android.content.Context
import androidx.room.Room
import com.example.finance.data.dao.FinanceDao
import com.example.finance.data.db.FinanceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinanceDatabase {
        return Room.databaseBuilder(
            context,
            FinanceDatabase::class.java,
            "finance_db"
        )
        .addMigrations(FinanceDatabase.MIGRATION_1_2, FinanceDatabase.MIGRATION_8_9)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    @Provides
    fun provideFinanceDao(database: FinanceDatabase): FinanceDao {
        return database.financeDao()
    }
}
