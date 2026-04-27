package com.bbinxx.onecharge.domain.usecase

import com.bbinxx.onecharge.domain.model.UserSettings
import com.bbinxx.onecharge.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: UserSettingsRepository
) {
    operator fun invoke(): Flow<UserSettings> {
        return repository.getUserSettingsFlow()
    }

    suspend fun getDirect(): UserSettings {
        return repository.getUserSettings()
    }
}
