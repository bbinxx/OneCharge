package com.bbinxx.onecharge.domain.repository

import com.bbinxx.onecharge.domain.model.AppUsageInfo

interface AppUsageRepository {
    suspend fun getAppUsage(startTime: Long, endTime: Long): List<AppUsageInfo>
    fun hasUsagePermission(): Boolean
}
