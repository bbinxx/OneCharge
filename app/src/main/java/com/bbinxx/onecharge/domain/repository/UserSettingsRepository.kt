package com.bbinxx.onecharge.domain.repository

import com.bbinxx.onecharge.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getUserSettingsFlow(): Flow<UserSettings>
    suspend fun getUserSettings(): UserSettings
    suspend fun updateUserSettings(settings: UserSettings)
}
