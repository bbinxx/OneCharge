package com.bbinxx.onecharge.data.repository

import com.bbinxx.onecharge.data.local.PreferencesManager
import com.bbinxx.onecharge.domain.model.UserSettings
import com.bbinxx.onecharge.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : UserSettingsRepository {
    override fun getUserSettingsFlow(): Flow<UserSettings> =
        preferencesManager.userSettingsFlow

    override suspend fun getUserSettings(): UserSettings =
        preferencesManager.userSettingsFlow.first()

    override suspend fun updateUserSettings(settings: UserSettings) =
        preferencesManager.updateSettings(settings)
}
