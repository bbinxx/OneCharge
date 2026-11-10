package com.bbinxx.onecharge.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.model.ChargingSession
import com.bbinxx.onecharge.ui.dashboard.formatDuration
import com.bbinxx.onecharge.ui.dashboard.formatSot
import com.bbinxx.onecharge.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val completedCycles by viewModel.completedCycles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = IosBg,
        topBar = {
            TopAppBar(
                title = { Text("History", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.2).sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IosBg.copy(alpha = 0.6f),
                    titleContentColor = IosTextPrimary,
                    actionIconContentColor = IosPrimary
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Export")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export to CSV") },
                            onClick = {
                                viewModel.exportToCSV()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export to JSON") },
                            onClick = {
                                viewModel.exportToJSON()
                                showMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                placeholder = { Text("Search by SOT...", color = IosTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IosTextSecondary) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = IosSurfaceElevated,
                    unfocusedContainerColor = IosSurfaceElevated,
                    focusedTextColor = IosTextPrimary,
                    unfocusedTextColor = IosTextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = IosPrimary
                )
            )

            // Filters & Sort options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterTypeChip(
                    selectedType = filterType,
                    onSelected = { viewModel.updateFilterType(it) }
                )
                SortTypeButton(
                    selectedSort = sortType,
                    onSelected = { viewModel.updateSortType(it) }
                )
            }

            if (completedCycles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No history records found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IosTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp) // pad for bottom nav
                ) {
                    // Group all cycles into a single iOS-style ListGroup
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(IosSurface)
                        ) {
                            completedCycles.forEachIndexed { index, cycle ->
                                HistoryListItem(
                                    cycle = cycle,
                                    viewModel = viewModel,
                                    isLastItem = index == completedCycles.lastIndex,
                                    onDelete = { viewModel.deleteCycle(cycle) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTypeChip(
    selectedType: FilterType,
    onSelected: (FilterType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterType.values().forEach { type ->
            val isSelected = selectedType == type
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) IosPrimary else IosSurface)
                    .clickable { onSelected(type) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) Color.White else IosTextSecondary
                )
            }
        }
    }
}

@Composable
fun SortTypeButton(
    selectedSort: SortType,
    onSelected: (SortType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(
            text = when (selectedSort) {
                SortType.DATE_DESC -> "Newest"
                SortType.DATE_ASC -> "Oldest"
                SortType.SOT_DESC -> "Longest"
                SortType.SOT_ASC -> "Shortest"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = IosPrimary,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortType.values().forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (type) {
                                SortType.DATE_DESC -> "Newest"
                                SortType.DATE_ASC -> "Oldest"
                                SortType.SOT_DESC -> "Longest"
                                SortType.SOT_ASC -> "Shortest"
                            }
                        )
                    },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun HistoryListItem(
    cycle: BatteryCycle,
    viewModel: HistoryViewModel,
    isLastItem: Boolean,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var sessions by remember { mutableStateOf<List<ChargingSession>>(emptyList()) }

    LaunchedEffect(expanded) {
        if (expanded && sessions.isEmpty()) {
            sessions = viewModel.getSessions(cycle.id)
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            val isGoodSot = cycle.screenOnTimeSeconds > 5 * 3600 // > 5 hours is "good"
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isGoodSot) IosSurfaceElevated else IosSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (cycle.startBatteryLevel > 80) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryAlert,
                    contentDescription = null,
                    tint = if (isGoodSot) IosGreen else IosOrange,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatSot(cycle.screenOnTimeSeconds) + " SOT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = IosTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatRelativeDay(cycle.startTime) + ", " + formatTimeOnly(cycle.startTime),
                    fontSize = 14.sp,
                    color = IosTextSecondary
                )
            }

            // Value
            val usage = cycle.startBatteryLevel - (cycle.endBatteryLevel ?: cycle.currentBatteryLevel)
            Text(
                text = "-$usage%",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = IosRed
            )

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandMore else Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = IosTextTertiary
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IosBg.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sessions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = IosTextSecondary)
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = IosRed)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                sessions.forEach { session ->
                    val gained = (session.endBattery ?: session.startBattery) - session.startBattery
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTimeOnly(session.startTime),
                            fontSize = 14.sp,
                            color = IosTextSecondary
                        )
                        Text(
                            text = "+$gained%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = IosGreen
                        )
                    }
                }

                if (sessions.isEmpty()) {
                    Text("No partial charge sessions recorded.", fontSize = 14.sp, color = IosTextTertiary)
                }
            }
        }

        if (!isLastItem) {
            Divider(
                color = IosBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 68.dp) // aligns with text
            )
        }
    }
}

// Helpers
fun formatRelativeDay(timestamp: Long): String {
    val cal = Calendar.getInstance()
    val today = cal.get(Calendar.DAY_OF_YEAR)
    cal.timeInMillis = timestamp
    val targetDay = cal.get(Calendar.DAY_OF_YEAR)

    return when {
        today == targetDay -> "Today"
        today - targetDay == 1 -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

fun formatTimeOnly(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}
