package com.codecraft.subvault.di

import android.content.Context
import androidx.room.Room
import com.codecraft.subvault.data.local.ExchangeRateDao
import com.codecraft.subvault.data.local.PriceChangeDao
import com.codecraft.subvault.data.local.SentNotificationDao
import com.codecraft.subvault.data.local.SubVaultDatabase
import com.codecraft.subvault.data.local.SubscriptionDao
import com.codecraft.subvault.data.local.UserPreferencesDao
import com.codecraft.subvault.domain.util.CurrencyConverter
import com.codecraft.subvault.domain.util.DynamicCurrencyConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SubVaultDatabase {
        return Room.databaseBuilder(
            context,
            SubVaultDatabase::class.java,
            "subvault_db"
        )
        .addMigrations(
            SubVaultDatabase.MIGRATION_2_3, 
            SubVaultDatabase.MIGRATION_3_4, 
            SubVaultDatabase.MIGRATION_4_5,
            SubVaultDatabase.MIGRATION_5_6
        )
        .build()
    }

    @Provides
    @Singleton
    fun provideSubscriptionDao(database: SubVaultDatabase): SubscriptionDao {
        return database.subscriptionDao
    }

    @Provides
    @Singleton
    fun provideUserPreferencesDao(database: SubVaultDatabase): UserPreferencesDao {
        return database.userPreferencesDao
    }

    @Provides
    @Singleton
    fun providePriceChangeDao(database: SubVaultDatabase): PriceChangeDao {
        return database.priceChangeDao
    }

    @Provides
    @Singleton
    fun provideExchangeRateDao(database: SubVaultDatabase): ExchangeRateDao {
        return database.exchangeRateDao
    }

    @Provides
    @Singleton
    fun provideSentNotificationDao(database: SubVaultDatabase): SentNotificationDao {
        return database.sentNotificationDao
    }

    @Provides
    @Singleton
    fun provideCurrencyConverter(dynamicCurrencyConverter: DynamicCurrencyConverter): CurrencyConverter {
        return dynamicCurrencyConverter
    }
}
