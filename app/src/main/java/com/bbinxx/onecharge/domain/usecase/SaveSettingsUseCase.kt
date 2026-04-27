package com.bbinxx.onecharge.domain.usecase

import com.bbinxx.onecharge.domain.model.UserSettings
import com.bbinxx.onecharge.domain.repository.UserSettingsRepository
import javax.inject.Inject

class SaveSettingsUseCase @Inject constructor(
    private val repository: UserSettingsRepository
) {
    suspend operator fun invoke(settings: UserSettings) {
        repository.updateUserSettings(settings)
    }
}
