package com.bbinxx.onecharge.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bbinxx.onecharge.ui.analytics.AnalyticsScreen
import com.bbinxx.onecharge.ui.dashboard.DashboardScreen
import com.bbinxx.onecharge.ui.history.HistoryScreen
import com.bbinxx.onecharge.ui.settings.SettingsScreen
import com.bbinxx.onecharge.ui.theme.IosPrimary
import com.bbinxx.onecharge.ui.theme.IosSurface
import com.bbinxx.onecharge.ui.theme.IosTextSecondary

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Rounded.Home)
    object History : Screen("history", "History", Icons.Rounded.History)
    object Analytics : Screen("analytics", "Insights", Icons.Rounded.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
}

val bottomNavigationItems = listOf(
    Screen.Dashboard,
    Screen.History,
    Screen.Analytics,
    Screen.Settings
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = IosSurface.copy(alpha = 0.85f),
                contentColor = IosTextSecondary
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavigationItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IosPrimary,
                            selectedTextColor = IosPrimary,
                            unselectedIconColor = IosTextSecondary,
                            unselectedTextColor = IosTextSecondary,
                            indicatorColor = Color.Transparent
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
