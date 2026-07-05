package com.bbinxx.onecharge.di

import android.content.Context
import androidx.room.Room
import com.bbinxx.onecharge.data.local.BatteryCycleDao
import com.bbinxx.onecharge.data.local.ChargingSessionDao
import com.bbinxx.onecharge.data.local.OneChargeDatabase
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): OneChargeDatabase {
        return Room.databaseBuilder(
            context,
            OneChargeDatabase::class.java,
            "onecharge_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideBatteryCycleDao(db: OneChargeDatabase): BatteryCycleDao {
        return db.batteryCycleDao()
    }

    @Provides
    @Singleton
    fun provideChargingSessionDao(db: OneChargeDatabase): ChargingSessionDao {
        return db.chargingSessionDao()
    }
}
