package com.bbinxx.onecharge.ui.history

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

enum class SortType {
    DATE_DESC, DATE_ASC, SOT_DESC, SOT_ASC
}

enum class FilterType {
    ALL, AUTOMATIC, MANUAL
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: BatteryCycleRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.DATE_DESC)
    val sortType = _sortType.asStateFlow()

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType = _filterType.asStateFlow()

    val completedCycles: StateFlow<List<BatteryCycle>> = combine(
        repository.getCompletedCyclesFlow(),
        _searchQuery,
        _sortType,
        _filterType
    ) { list, query, sort, filter ->
        var filteredList = list

        // 1. Filter by manual/auto
        filteredList = when (filter) {
            FilterType.ALL -> filteredList
            FilterType.AUTOMATIC -> filteredList.filter { !it.isManual }
            FilterType.MANUAL -> filteredList.filter { it.isManual }
        }

        // 2. Filter by search query (e.g. SOT match or date description match)
        if (query.isNotEmpty()) {
            filteredList = filteredList.filter {
                val formattedSot = "${it.screenOnTimeSeconds / 3600}h"
                formattedSot.contains(query, ignoreCase = true) ||
                        it.startBatteryLevel.toString().contains(query)
            }
        }

        // 3. Sort
        when (sort) {
            SortType.DATE_DESC -> filteredList.sortedByDescending { it.startTime }
            SortType.DATE_ASC -> filteredList.sortedBy { it.startTime }
            SortType.SOT_DESC -> filteredList.sortedByDescending { it.screenOnTimeSeconds }
            SortType.SOT_ASC -> filteredList.sortedBy { it.screenOnTimeSeconds }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortType(type: SortType) {
        _sortType.value = type
    }

    fun updateFilterType(type: FilterType) {
        _filterType.value = type
    }

    fun deleteCycle(cycle: BatteryCycle) {
        viewModelScope.launch {
            repository.deleteCycle(cycle)
        }
    }

    fun exportToCSV() {
        viewModelScope.launch {
            val list = repository.getCompletedCycles()
            val csvHeader = "ID,Start Battery,End Battery,Start Time,End Time,SOT Seconds,Charging Duration,Sessions,ManualStart\n"
            val csvBody = list.joinToString("\n") {
                "${it.id},${it.startBatteryLevel},${it.endBatteryLevel},${it.startTime},${it.endTime},${it.screenOnTimeSeconds},${it.chargingDurationSeconds},${it.chargingSessionsCount},${it.isManual}"
            }
            val csvContent = csvHeader + csvBody
            shareFile(csvContent, "onecharge_history.csv", "text/csv")
        }
    }

    fun exportToJSON() {
        viewModelScope.launch {
            val list = repository.getCompletedCycles()
            val jsonArray = JSONArray()
            list.forEach {
                val jsonObject = JSONObject().apply {
                    put("id", it.id)
                    put("startBattery", it.startBatteryLevel)
                    put("endBattery", it.endBatteryLevel)
                    put("startTime", it.startTime)
                    put("endTime", it.endTime)
                    put("screenOnTimeSeconds", it.screenOnTimeSeconds)
                    put("chargingDurationSeconds", it.chargingDurationSeconds)
                    put("chargingSessionsCount", it.chargingSessionsCount)
                    put("isManual", it.isManual)
                }
                jsonArray.put(jsonObject)
            }
            shareFile(jsonArray.toString(4), "onecharge_history.json", "application/json")
        }
    }

    private fun shareFile(content: String, filename: String, mimeType: String) {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, filename)
        file.writeText(content)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Export OneCharge History")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
