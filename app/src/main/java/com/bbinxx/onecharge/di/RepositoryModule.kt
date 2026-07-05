package com.bbinxx.onecharge.di

import com.bbinxx.onecharge.data.repository.AppUsageRepositoryImpl
import com.bbinxx.onecharge.data.repository.BatteryCycleRepositoryImpl
import com.bbinxx.onecharge.data.repository.UserSettingsRepositoryImpl
import com.bbinxx.onecharge.domain.repository.AppUsageRepository
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import com.bbinxx.onecharge.domain.repository.UserSettingsRepository
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
    abstract fun bindBatteryCycleRepository(
        impl: BatteryCycleRepositoryImpl
    ): BatteryCycleRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(
        impl: UserSettingsRepositoryImpl
    ): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindAppUsageRepository(
        impl: AppUsageRepositoryImpl
    ): AppUsageRepository
}
