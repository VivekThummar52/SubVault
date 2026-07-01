package com.codecraft.subvault.di

import com.codecraft.subvault.data.repository.PreferenceRepositoryImpl
import com.codecraft.subvault.data.repository.SubscriptionRepositoryImpl
import com.codecraft.subvault.domain.repository.PreferenceRepository
import com.codecraft.subvault.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        subscriptionRepositoryImpl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindPreferenceRepository(
        preferenceRepositoryImpl: PreferenceRepositoryImpl
    ): PreferenceRepository
}
