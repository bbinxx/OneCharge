package com.bbinxx.onecharge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BatteryCycleEntity::class, ChargingSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OneChargeDatabase : RoomDatabase() {
    abstract fun batteryCycleDao(): BatteryCycleDao
    abstract fun chargingSessionDao(): ChargingSessionDao
}
