package com.bbinxx.onecharge.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.bbinxx.onecharge.domain.model.AppUsageInfo
import com.bbinxx.onecharge.domain.repository.AppUsageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUsageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppUsageRepository {

    override fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.noteOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    override suspend fun getAppUsage(startTime: Long, endTime: Long): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        if (startTime >= endTime) return@withContext emptyList()
        
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        
        val pm = context.packageManager
        
        statsMap.values
            .filter { it.totalTimeInForeground > 0 }
            .map { usageStats ->
                val packageName = usageStats.packageName
                val appLabel = try {
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    packageName
                }
                AppUsageInfo(
                    packageName = packageName,
                    appName = appLabel,
                    usageDurationMs = usageStats.totalTimeInForeground
                )
            }
            .sortedByDescending { it.usageDurationMs }
    }
}
