package com.bbinxx.onecharge.domain.usecase

import com.bbinxx.onecharge.domain.model.AppUsageInfo
import com.bbinxx.onecharge.domain.repository.AppUsageRepository
import javax.inject.Inject

class GetAppUsageUseCase @Inject constructor(
    private val repository: AppUsageRepository
) {
    suspend operator fun invoke(startTime: Long, endTime: Long): List<AppUsageInfo> {
        if (!repository.hasUsagePermission()) return emptyList()
        return repository.getAppUsage(startTime, endTime)
    }

    fun hasPermission(): Boolean {
        return repository.hasUsagePermission()
    }
}
